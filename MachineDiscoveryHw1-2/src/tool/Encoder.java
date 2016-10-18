package tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import config.Const;

public class Encoder {

	public static Encoder instance;
	private String filePath;
	private double[][] probMap;
	private List< List<Integer> > thetaSets;
	private Map<Integer, Integer> thetaMaps;
	
	public Encoder(String _filePath) {
		probMap = new double[Const.NUM_OF_SYMBOL][Const.NUM_OF_SYMBOL];
		thetaSets = new ArrayList< List<Integer> >();
		for(int i = 0 ; i < Const.NUM_OF_SYMBOL ; i++)	thetaSets.add( new ArrayList<Integer>() );
		thetaMaps = new HashMap<Integer, Integer>();
		filePath = _filePath;
		try {
			BufferedReader br = new BufferedReader( new FileReader(filePath) );
			String line = null;
			while( ( line = br.readLine() ) != null ) {
				String[] splitLine = line.split(" ");
				int ch = Integer.parseInt( splitLine[0] ), code = Integer.parseInt( splitLine[1] );
				probMap[ch][code] = Double.parseDouble( splitLine[2] );
				if(Integer.parseInt( splitLine[2] ) != 0) {
					if(thetaSets.get(code).isEmpty() == false)
						thetaMaps.put(code * Const.NUM_OF_SYMBOL + thetaSets.get(code).get(thetaSets.get(code).size() - 1), ch);	
					thetaSets.get(code).add(ch);
				}
			}
			for(int ch = 0 ; ch < Const.NUM_OF_SYMBOL ; ch++) {
				double sum = 0d;
				for(int code = 0 ; code < Const.NUM_OF_SYMBOL ; code++)	sum += probMap[ch][code];
				for(int code = 0 ; code < Const.NUM_OF_SYMBOL ; code++)	probMap[ch][code] /= sum;
			}
			br.close();
		} catch (IOException e) {
			System.err.println("[Error] Fail to read from " + filePath);
			e.printStackTrace();
		}
	}
	
	// return the instance if it exists
	public static Encoder getInstance(String _filePath) {
		if( instance == null || !instance.filePath.equals(_filePath) )	instance = new Encoder(_filePath);
		return instance;
	}
	
	// random the parameters
	public void random(long _seed) {
		Random rand = new Random(_seed);
		try {
			BufferedReader br = new BufferedReader( new FileReader(filePath) );
			String line = null;
			while( ( line = br.readLine() ) != null ) {
				String[] splitLine = line.split(" ");
				int ch = Integer.parseInt( splitLine[0] ), code = Integer.parseInt( splitLine[1] );
				probMap[ch][code] = 0d;
				if(Integer.parseInt( splitLine[2] ) != 0)	probMap[ch][code] = rand.nextDouble() + 0.1d;
			}
			for(int ch = 0 ; ch < Const.NUM_OF_SYMBOL ; ch++) {
				double sum = 0d;
				for(int code = 0 ; code < Const.NUM_OF_SYMBOL ; code++)	sum += probMap[ch][code];
				for(int code = 0 ; code < Const.NUM_OF_SYMBOL ; code++)	probMap[ch][code] /= sum;
			}
			br.close();
		} catch (IOException e) {
			System.err.println("[Error] Fail to read from " + filePath);
			e.printStackTrace();
		}
	}
	
	// get the probability of the specific encoding event
	public double getProb(int _ch, int _code) {
		return probMap[_ch][_code];
	}
	
	// set the probability of the specific encoding event
	public void setProb(int _ch, int _code, double _prob) {
		probMap[_ch][_code] = _prob;
	}
	
	// return all possible characters of a code
	public List<Integer> getPossibleChars(int _code) {
		return thetaSets.get(_code);
	}
	
	// return the next possible character given the code and the previous trial (-1 if none) (return first if previous is -1)
	public int getNextPossibleChar(int _code, int _previousTrial) {
		if(_previousTrial == -1)	return thetaSets.get(_code).get(0);
		Integer ch = thetaMaps.get(_code * Const.NUM_OF_SYMBOL + _previousTrial);
		if(ch == null)	return -1;
		return ch;
	}
	
}
