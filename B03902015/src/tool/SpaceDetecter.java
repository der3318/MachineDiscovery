package tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import config.Const;

public class SpaceDetecter {

	public static void setUp(String _encodeFilePath) {
		try {
			BufferedReader br = new BufferedReader( new FileReader(_encodeFilePath) );
			String line = null;
			Const.NUM_OF_SYMBOL = 0;
			while( ( line = br.readLine() ) != null ) {
				String[] splitLine = line.split(" ");
				int ch = Integer.parseInt( splitLine[0] ), code = Integer.parseInt( splitLine[1] );
				if(ch > Const.NUM_OF_SYMBOL)	Const.NUM_OF_SYMBOL = ch;
				if(code > Const.NUM_OF_SYMBOL)	Const.NUM_OF_SYMBOL = code;
			}
			br.close();
			Const.NUM_OF_SYMBOL++;
		} catch (IOException e) {
			System.err.println("[Error] Fail to read from " + _encodeFilePath);
			e.printStackTrace();
		}
	}
	
}
