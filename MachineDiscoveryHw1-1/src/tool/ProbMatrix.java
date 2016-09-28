package tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProbMatrix {

	public static ProbMatrix instance;
	private Bigram bigram;
	private Encoder encoder;
	private Map<String, Double> probMap;
	
	public ProbMatrix(Bigram _bigram, Encoder _encoder) {
		probMap = new HashMap<String, Double>();
		bigram = _bigram;
		encoder = _encoder;
		List<Character> chars = new ArrayList<Character>();
		for(char c = 'a' ; c <= 'z' ; c++)	chars.add(c);
		for(char c = '0' ; c <= '9' ; c++)	chars.add(c);
		chars.add(' ');
		for(char pre : chars)
			for(char ch : chars)
				for(char code : chars)	probMap.put("" + pre + ch + code, bigram.getProb(pre,  ch) * encoder.getProb(ch, code));
	}
	
	// return the instance if it exists
	public static ProbMatrix getInstance(Bigram _bigram, Encoder _encoder) {
		if(instance == null || instance.bigram != _bigram || instance.encoder != _encoder)	instance = new ProbMatrix(_bigram, _encoder);
		return instance;
	}
	
	// get the probability of the specific encoding event with the previous character
	public double getProb(char _pre, char _ch, char _code) {
		Double prob = probMap.get("" + _pre + _ch + _code);
		if(prob == null)	return 0d;
		return prob;
	}
}
