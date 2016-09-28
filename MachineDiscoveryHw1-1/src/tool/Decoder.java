package tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Decoder {

	public static Decoder instance;
	private Bigram bigram;
	private Encoder encoder;
	private Map<String, Double> probMap;
	
	public Decoder(Bigram _bigram, Encoder _encoder) {
		probMap = new HashMap<String, Double>();
		bigram = _bigram;
		encoder = _encoder;
		List<Character> chars = new ArrayList<Character>();
		for(char c = 'a' ; c <= 'z' ; c++)	chars.add(c);
		for(char c = '0' ; c <= '9' ; c++)	chars.add(c);
		chars.add(' ');
		for(char pre : chars)
			for(char code : chars) {
				double sum = 0d;
				for(char ch : chars)	sum += bigram.getProb(pre, ch) * encoder.getProb(ch, code);
				if(Math.abs(sum) < 2 * Double.MIN_VALUE)	continue;
				for(char ch : chars)	probMap.put("" + pre + code + ch, bigram.getProb(pre,  ch) * encoder.getProb(ch, code) / sum);
			}
	}
	
	// return the instance if it exists
	public static Decoder getInstance(Bigram _bigram, Encoder _encoder) {
		if(instance == null || instance.bigram != _bigram || instance.encoder != _encoder)	instance = new Decoder(_bigram, _encoder);
		return instance;
	}
	
	// get the probability of the specific decoding event
	public double getProb(char _pre, char _code, char _ch) {
		Double prob = probMap.get("" + _pre + _code + _ch);
		if(prob == null)	return 0d;
		return prob;
	}
}
