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

import config.Const;

public class ExpectationMaximization {

	private Bigram bigram;
	private Encoder encoder;
	private String inputPath, outputPath;
	private List<Integer> observations, predictions, segments;
	private double enCntTable[][], biCntTable[][], pTb[][];
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
		maxProb = new BigDecimal("-1");
		// VITERBI
		for(int seed = 1 ; seed < 5 ; seed++) {
			bigram.random(seed);
			encoder.random(seed);
			int round = 1;
			while(true) {
				for(int i = 0 ; i < Const.NUM_OF_SYMBOL ; i++)
					for(int j = 0 ; j < Const.NUM_OF_SYMBOL ; j++) {
						enCntTable[i][j] = 0.0d;
						biCntTable[i][j] = 0.0d;
					}
				stopFlag = true;
				System.out.println("[Training] Seed No." + seed + ", Round No." + round++);
				this.constructBestPrediction();
				this.updateCntTables();
				this.updateModels();
				if(stopFlag)	break;
			}
			BigDecimal result = this.calcProb();
			if(result.compareTo(maxProb) < 0)	continue;
			System.out.println("[New Seed] Seed No." + seed + ", Prob = " + result);
			maxProb = result;
			maxSeed = seed;
			this.dump();
		}
		System.out.println("[Complete] maxSeed = " + maxSeed + ", maxProb = " + maxProb + "\n");
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
		for(int index = 0 ; index < size ; index++) {
			if(index == 0)	biCntTable[36][ predictions.get(0) ] += 1.0d;
			else	biCntTable[ predictions.get(index - 1) ][ predictions.get(index) ] += 1.0d; 
			enCntTable[ predictions.get(index) ][ observations.get(index) ] += 1.0d;
		}
	}
	
	// normalize the table and update the model
	private void updateModels() {
		System.out.println("[M] Updating the model");
		for(int i = 0 ; i < Const.NUM_OF_SYMBOL ; i++) {
			double biSum = 0, enSum = 0;
			for(int j = 0 ; j < Const.NUM_OF_SYMBOL ; j++) {
				biSum += biCntTable[i][j];
				enSum += enCntTable[i][j];
			}
			for(int j = 0 ; j < Const.NUM_OF_SYMBOL ; j++) {
				if(Math.abs(biSum) > 2 * Double.MIN_VALUE) {
					if( Math.abs(bigram.getProb(i, j) - biCntTable[i][j] / biSum) > 2 * Double.MIN_VALUE )	stopFlag = false;
					bigram.setProb(i, j, biCntTable[i][j] / biSum);
				}
				if(Math.abs(enSum) > 2 * Double.MIN_VALUE) {
					if( Math.abs(encoder.getProb(i, j) - enCntTable[i][j] / enSum) > 2 * Double.MIN_VALUE )	stopFlag = false;
					encoder.setProb(i, j, enCntTable[i][j] / enSum);
				}
			}
		}
	}
	
	// construct the best prediction
	private void constructBestPrediction() {
		System.out.println("[E] Constructing the best prediction");		
		predictions.clear();
		int preIdx = 0;
		for(int idx = 0 ; idx < size ; idx++)
			if(observations.get(idx) == 36) {
				System.out.print("\rProcessing [" + (idx + 1) + "/" + size + "]");
				this.constructBestSegment( observations.subList(preIdx, idx) );
				for(Integer i : segments)	predictions.add(i);
				predictions.add(36);
				preIdx = idx + 1;
			}
		if(preIdx != size) {
			System.out.println("\rProcessing [" + size + "/" + size + "]");
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
			pTb[0][col] = bigram.getProb(36, col) * encoder.getProb( col, _observations.get(0) );
		// finish the table
		for(int row = 1 ; row < rowSize ; row++) {
			int currentCode = ( row == rowSize - 1 ? 36 : _observations.get(row) );
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
		int currentCol = 36;
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
			System.out.print("\rProceessing [" + (index + 1) + "/" + size + "]");
			if(index == 0)	tmp *= bigram.getProb( 36,  predictions.get(0) );
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
			for(int index = 0 ; index < size ; index++) {
				if(index != size - 1)	bw.write(predictions.get(index) + " ");
				else	bw.write( predictions.get(index) );
			}
			bw.close();
		} catch (IOException e) {
			System.err.println("[Error] Fail to wirte data into " + outputPath);
			e.printStackTrace();
		}
	}
	
}
