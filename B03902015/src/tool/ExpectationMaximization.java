package tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import config.Const;

public class ExpectationMaximization {

	private Bigram bigram;
	private Encoder encoder;
	private String inputPath, outputPath;
	private List<Integer> observations, predictions, segments;
	private double enCntTable[][], biCntTable[][], pTb[][], alpha[][], beta[][];
	private int size, cTb[][], maxSeed;
	private boolean stopFlag;
	BigDecimal maxProb;
	
	public ExpectationMaximization(Bigram _bigram, Encoder _encoder, String _inputPath, String _outputPath) {
		bigram = _bigram;
		encoder = _encoder;
		inputPath = _inputPath;
		outputPath = _outputPath;
		observations = new ArrayList<Integer>();
		predictions = new ArrayList<Integer>();
		segments = new ArrayList<Integer>();
		enCntTable = new double[Const.NUM_OF_SYMBOL][Const.NUM_OF_SYMBOL];
		biCntTable = new double[Const.NUM_OF_SYMBOL][Const.NUM_OF_SYMBOL];
		cTb = new int[Const.MAX_WORD_LEN][Const.NUM_OF_SYMBOL];
		pTb = new double[Const.MAX_WORD_LEN][Const.NUM_OF_SYMBOL];
	}
	
	// proceed the EM
	public void proceed() {
		this.readObservations();
		alpha = new double[size][Const.NUM_OF_SYMBOL];
		beta =  new double[size][Const.NUM_OF_SYMBOL];
		maxProb = new BigDecimal("-1");
		// VITERBI
		for(int seed = 1 ; seed <= Const.MAX_SEED ; seed++) {
			bigram.random(seed);
			encoder.random(seed);
			for(int round = 1 ; round <= Const.MAX_ROUND ; round ++) {
				stopFlag = true;
				for(int i = 0 ; i < Const.NUM_OF_SYMBOL ; i++)
					for(int j = 0 ; j < Const.NUM_OF_SYMBOL ; j++) {
						enCntTable[i][j] = Double.NEGATIVE_INFINITY;
						biCntTable[i][j] = Double.NEGATIVE_INFINITY;
					}
				System.out.println("[Training] Seed No." + seed + ", Round No." + round);
				ExecutorService threadExecutor = Executors.newCachedThreadPool();
				threadExecutor.execute( new AlphaThread() );
				this.constructBeta();
				threadExecutor.shutdown();
				while(threadExecutor.isTerminated() == false);
				this.updateCntTables();
				this.updateModels();
				if(stopFlag)	break;
			}
			this.constructBestPrediction();
			BigDecimal result = this.calcProb();
			if(result.compareTo(maxProb) < 0)	continue;
			System.out.println("[New Seed] Seed No." + seed);
			maxProb = result;
			maxSeed = seed;
			this.dump();
		}
		System.out.println("[Complete] maxSeed = " + maxSeed + ", perdiction file = \"" +  Const.PREDICT_PATH + "\"\n");
	}
	
	// read observations from input file
	private void readObservations() {
		try {
			BufferedReader br = new BufferedReader( new FileReader(inputPath) );
			String line = null;
			while( ( line = br.readLine() ) != null ) {
				String[] words = line.trim().split(" ");
				for(String s : words)	observations.add( Integer.parseInt(s) );
			}
			br.close();
			size = observations.size();
		} catch (IOException e) {
			System.err.println("[Error] Fail to read from " + inputPath);
			e.printStackTrace();
		}
	}
	
	private void updateCntTables() {
		System.out.println("[E] Updating the counts");
		int curCode = observations.get(0), preCode = 0;
		for( int ch : encoder.getPossibleChars(curCode) ) {
			double logp2 = alpha[0][ch] + beta[0][ch];
			if(enCntTable[ch][curCode] == Double.NEGATIVE_INFINITY)
				enCntTable[ch][curCode] = logp2;
			else	enCntTable[ch][curCode] += Math.log( 1 + Math.exp( logp2 - enCntTable[ch][curCode] ) );
		}
		for(int t = 1 ; t < size ; t++) {
			curCode = observations.get(t);
			preCode = observations.get(t - 1);
			for( int ch : encoder.getPossibleChars(curCode) ) {
				double logp2 = alpha[t][ch] + beta[t][ch];
				if(enCntTable[ch][curCode] == Double.NEGATIVE_INFINITY)
					enCntTable[ch][curCode] = logp2;
				else	enCntTable[ch][curCode] += Math.log( 1 + Math.exp( logp2 - enCntTable[ch][curCode] ) );
				for( int preCh : encoder.getPossibleChars(preCode) ) {
					logp2 = alpha[t - 1][preCh] + beta[t][ch] + Math.log( bigram.getProb(preCh, ch) * encoder.getProb(ch, curCode) );
					if(biCntTable[preCh][ch] == Double.NEGATIVE_INFINITY)
						biCntTable[preCh][ch] = logp2;
					else	biCntTable[preCh][ch] += Math.log( 1 + Math.exp( logp2 - biCntTable[preCh][ch] ) );
				}
			}
		}
	}
	
	// normalize the table and update the model
	private void updateModels() {
		System.out.println("[M] Updating the model");
		for(int i = 0 ; i < Const.NUM_OF_SYMBOL ; i++) {
			double biLogSum = Double.NEGATIVE_INFINITY, enLogSum = Double.NEGATIVE_INFINITY;
			for(int j = 0 ; j < Const.NUM_OF_SYMBOL ; j++) {
				if(biLogSum == Double.NEGATIVE_INFINITY)
					biLogSum = biCntTable[i][j];
				else	biLogSum += Math.log( 1 + Math.exp(biCntTable[i][j] - biLogSum) );
				if(enLogSum == Double.NEGATIVE_INFINITY)
					enLogSum = enCntTable[i][j];
				else	enLogSum += Math.log( 1 + Math.exp(enCntTable[i][j] - enLogSum) );
			}
			for(int j = 0 ; j < Const.NUM_OF_SYMBOL ; j++) {
				if(biLogSum != Double.NEGATIVE_INFINITY) {
					double prob = Math.exp(biCntTable[i][j] - biLogSum);
					if(Math.abs(bigram.getProb(i, j) - prob) > Const.MAX_ERR)	stopFlag = false;
					bigram.setProb(i, j, prob);
				}
				if(enLogSum != Double.NEGATIVE_INFINITY) {
					double prob = Math.exp(enCntTable[i][j] - enLogSum);
					if(Math.abs(encoder.getProb(i, j) - prob) > Const.MAX_ERR)	stopFlag = false;
					encoder.setProb(i, j, prob);
				}
			}
		}
	}
	
	// construct the best prediction
	private void constructBestPrediction() {
		System.out.println("[Test] Constructing the best prediction");
		predictions.clear();
		int preIdx = 0;
		for(int idx = 0 ; idx < size ; idx++)
			if( observations.get(idx) == (Const.NUM_OF_SYMBOL - 1) ) {
				System.out.print("\r\tProcessing [" + (idx + 1) + "/" + size + "]");
				this.constructBestSegment( observations.subList(preIdx, idx) );
				for(Integer i : segments)	predictions.add(i);
				predictions.add(Const.NUM_OF_SYMBOL - 1);
				preIdx = idx + 1;
			}
		if(preIdx != size) {
			System.out.println("\r\tProcessing [" + size + "/" + size + "]");
			this.constructBestSegment( observations.subList(preIdx, size) );
			for(Integer i : segments)	predictions.add(i);
		}
	}
	
	// construct the best segment
	private void constructBestSegment(List<Integer> _observations) {
		segments.clear();
		if( _observations.isEmpty() )	return;
		int colSize = Const.NUM_OF_SYMBOL, rowSize = _observations.size() + 1;
		for(int col = 0 ; col < colSize ; col++)
			pTb[0][col] = bigram.getProb(Const.NUM_OF_SYMBOL - 1, col) * encoder.getProb( col, _observations.get(0) );
		// finish the table
		for(int row = 1 ; row < rowSize ; row++) {
			int currentCode = ( row == rowSize - 1 ? Const.NUM_OF_SYMBOL - 1 : _observations.get(row) );
			for(int col = 0 ; col < colSize ; col++) {
				pTb[row][col] = 0d;
				for(int lastCol = 0 ; lastCol < colSize ; lastCol++) {
					double prob = pTb[row - 1][lastCol] * bigram.getProb(lastCol, col) * encoder.getProb(col, currentCode);
					if(prob > pTb[row][col]) {
						pTb[row][col] = prob;
						cTb[row][col] = lastCol;
					}
				}
			}
		}
		// rebuild the word from the end
		int currentCol = Const.NUM_OF_SYMBOL - 1;
		for(int row = rowSize - 1 ; row > 0 ; row--) {
			currentCol = cTb[row][currentCol];
			segments.add(0, currentCol);
		}
	}
	
	// calculate the probability
	private BigDecimal calcProb() {
		System.out.println("[Calc] Calculating the Probability");
		BigDecimal output = new BigDecimal("1");
		double tmp = 1d;
		for(int index = 0 ; index < size ; index++) {
			System.out.print("\r\tProcessing [" + (index + 1) + "/" + size + "]");
			if(index == 0)	tmp *= bigram.getProb( Const.NUM_OF_SYMBOL - 1,  predictions.get(0) );
			else	tmp *= bigram.getProb( predictions.get(index - 1),  predictions.get(index) ); 
			tmp *= encoder.getProb( predictions.get(index), observations.get(index) );
			if(index % 10 == 0) {
				output = output.multiply( new BigDecimal(tmp + "") );
				tmp = 1d;
			}
		}
		output = output.multiply( new BigDecimal(tmp + "") );
		System.out.println();
		return output;
	}
	
	// dump the result to output file
	private void dump() {
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter( new File(outputPath) ) );
			for(int index = 0 ; index < size ; index++)	bw.write(predictions.get(index) + " ");
			bw.close();
		} catch (IOException e) {
			System.err.println("[Error] Fail to wirte data into " + outputPath);
			e.printStackTrace();
		}
	}
	
	// construct alpha
	private void constructAlpha() {
		for(int ch = 0 ; ch < Const.NUM_OF_SYMBOL ; ch++)
			alpha[0][ch] = Math.log( bigram.getProb(Const.NUM_OF_SYMBOL - 1, ch) * encoder.getProb( ch, observations.get(0) ) );
		for(int t = 1 ; t < size ; t++) {
			int preCode = observations.get(t - 1);
			for(int ch = 0 ; ch < Const.NUM_OF_SYMBOL ; ch++) {
				int curCode = observations.get(t);
				if(encoder.getPossibleChars(curCode).contains(ch) == false) {
					alpha[t][ch] = Double.NEGATIVE_INFINITY;
					continue;
				}
				double sumLog = Double.NEGATIVE_INFINITY;
				for(int preCh : encoder.getPossibleChars(preCode) ) {
					double logp2 = ( alpha[t - 1][preCh] + Math.log( bigram.getProb(preCh, ch) ) );
					if(sumLog == Double.NEGATIVE_INFINITY)	
						sumLog = logp2;
					else	sumLog += Math.log( 1 + Math.exp(logp2 - sumLog) );
				}
				alpha[t][ch] = sumLog + Math.log( encoder.getProb(ch, curCode) );	
			}
		}
	}
	
	// construct beta
	private void constructBeta() {
		System.out.println("[Calc] Calculating the alpha/beta table");
		for(int ch = 0 ; ch < Const.NUM_OF_SYMBOL ; ch++)
			beta[size - 1][ch] = 0d;
		for(int t = size - 2 ; t >= 0 ; t--) {
			if(t % 13318 == 0 || t == 0)
				System.out.print("\r\tProcessing [" + (size - t) + "/" + size + "]");
			int nextCode = observations.get(t + 1);
			for(int ch = 0 ; ch < Const.NUM_OF_SYMBOL ; ch++) {
				double sumLog = Double.NEGATIVE_INFINITY;
				for(int nextCh : encoder.getPossibleChars(nextCode) ) {
					double logp2 = ( beta[t + 1][nextCh] + Math.log( encoder.getProb(nextCh, nextCode) * bigram.getProb(ch, nextCh) ) );
					if(sumLog == Double.NEGATIVE_INFINITY)
						sumLog = logp2;
					else	sumLog += Math.log( 1 + Math.exp(logp2 - sumLog) );
				}
				beta[t][ch] = sumLog;	
			}
		}
		System.out.println("");
	}
	
	// threads to run alpha
	private class AlphaThread implements Runnable {

		@Override
		public void run() {
			ExpectationMaximization.this.constructAlpha();
		}
		
	}
	
}
