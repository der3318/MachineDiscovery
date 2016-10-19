package tool;

import java.util.Random;

import config.Const;

public class Bigram {

	private static Bigram instance;
	private double[][] probMap;
	
	public Bigram() {
		probMap = new double[Const.NUM_OF_SYMBOL][Const.NUM_OF_SYMBOL];
		for(int ch = 0 ; ch < Const.NUM_OF_SYMBOL ; ch++)
			for(int code = 0 ; code < Const.NUM_OF_SYMBOL ; code++)	probMap[ch][code] = 1 / (double)Const.NUM_OF_SYMBOL;
	}
	
	// return the instance if it exists
	public static Bigram getInstance() {
		if(instance == null)	instance = new Bigram();
		return instance;
	}
	
	// random the parameters
	public void random(long _seed) {
		Random rand = new Random(_seed);
		for(int ch = 0 ; ch < Const.NUM_OF_SYMBOL ; ch++) {
			double sum = 0d;
			for(int code = 0 ; code < Const.NUM_OF_SYMBOL ; code++) {
				double rndnum= rand.nextDouble() + 0.1d;
				probMap[ch][code] = rndnum;
				sum += rndnum;
			}
			for(int code = 0 ; code < Const.NUM_OF_SYMBOL ; code++)	probMap[ch][code] /= sum;
		}
	}
	
	// get the probability of the specific BIGRAM
	public double getProb(int _gram1, int _gram2) {
		return probMap[_gram1][_gram2];
	}
	
	// set the probability of the specific BIGRAM
	public void setProb(int _gram1, int _gram2, double _prob) {
		probMap[_gram1][_gram2] = _prob;
	}
 
	
}
