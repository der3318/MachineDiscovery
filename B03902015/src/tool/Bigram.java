package tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Bigram {

	private static Bigram instance;
	private String filePath;
	private Map<String, Double> probMap;
	
	public Bigram(String _filePath) {
		probMap = new HashMap<String, Double>();
		filePath = _filePath;
		try {
			BufferedReader br = new BufferedReader( new FileReader(filePath) );
			String line = null;
			while( ( line = br.readLine() ) != null )
				probMap.put( line.substring(0, 3), Double.parseDouble( line.substring(4) ) );
			br.close();
		} catch (IOException e) {
			System.out.println("[Error] Fail to read from " + filePath);
			e.printStackTrace();
		}
	}
	
	// return the instance if it exists
	public static Bigram getInstance(String _filePath) {
		if( instance == null || !instance.filePath.equals(_filePath) )	instance = new Bigram(_filePath);
		return instance;
	}
	
	// get the probability of the specific BIGRAM
	public double getProb(char _gram1, char _gram2) {
		Double prob = probMap.get(_gram1 + " " + _gram2);
		if(prob == null)	return 0d;
		return prob;
	}
	
}
