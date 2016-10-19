package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import config.Const;

public class EMValidation {
	
	public static void calcAccuracy(String _predPath, String _ansPath) {
		try {
			BufferedReader brPred = new BufferedReader( new FileReader(_predPath) ), brAns = new BufferedReader( new FileReader(_ansPath) );
			String linePred = null, lineAns = null;
			int size = 0, correct = 0;
			while( ( linePred = brPred .readLine() ) != null ) {
				lineAns = brAns.readLine();
				String[] predWords = linePred.trim().split(" ");
				String[] ansWords = lineAns.trim().split(" ");
				size += predWords.length;
				if(ansWords.length < size) {
					System.err.println("[Error] Unmatch: Number of the words");
					brPred.close();
					brAns.close();
					return;
				}
				for(int i = 0 ; i < size ; i++)	if(predWords[i].equals( ansWords[i] ) == true)	correct++;
			}
			brPred .close();
			brAns.close();
			System.out.println("[Complete] Accuracy = " + correct + "/" + size + " (" + (double)correct * 100 / (double)size + "%)");
		} catch (IOException e) {
			System.err.println("[Error] Fail to read from " + Const.PREDICT_PATH + " or " + Const.ANS_PATH);
			e.printStackTrace();
		}
	}

}
