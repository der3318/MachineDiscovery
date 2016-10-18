package tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Encoder {

	public static Encoder instance;
	private String filePath;
	private Map<String, Double> probMap;
	
	public Encoder(String _filePath) {
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
	public static Encoder getInstance(String _filePath) {
		if( instance == null || !instance.filePath.equals(_filePath) )	instance = new Encoder(_filePath);
		return instance;
	}
	
	// get the probability of the specific encoding event
	public double getProb(char _ch, char _code) {
		Double prob = probMap.get(_ch + " " + _code);
		if(prob == null)	return 0d;
		return prob;
	}
	
}
