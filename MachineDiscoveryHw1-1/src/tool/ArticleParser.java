package tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jazzy.JazzySpellChecker;

public class ArticleParser {

	private ProbMatrix probMatrix;
	private JazzySpellChecker checker;
	List<Character> chars = new ArrayList<Character>();
	
	public ArticleParser(ProbMatrix _probMatrix, JazzySpellChecker _checker) {
		probMatrix = _probMatrix;
		checker = _checker;
		for(char c = 'a' ; c <= 'z' ; c++)	chars.add(c);
		for(char c = '0' ; c <= '9' ; c++)	chars.add(c);
		chars.add(' ');
	}
	
	// decode the input file to output file
	public void parse(String _inputFile, String _outputFile) {
		try {
			BufferedReader br = new BufferedReader( new FileReader(_inputFile) );
			BufferedWriter bw = new BufferedWriter( new FileWriter( new File(_outputFile) ) );
			String line = null;
			int lineCnt = 1;
			final long startTime = System.currentTimeMillis();
			while( ( line = br.readLine() ) != null ) {
				String[] words = line.split(" +");
				int size = words.length;
				if(words.length > 0)	bw.write( this.parseWord( words[0] ) );
				System.out.print("\rDecoding \"" + _inputFile + "\", Line " + lineCnt + ", [" + 1 + "/" + size + "]");
				for(int i = 1 ; i < size ; i++) {
					bw.write( " " + this.parseWord( words[i] ) );
					System.out.print("\rDecoding \"" + _inputFile + "\", Line " + lineCnt + ", [" + (i + 1) + "/" + size + "]");
				}
				bw.write("\n");
				lineCnt++;
			}
			br.close();
			bw.close();
			System.out.println("\nComplete in " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
		} catch (IOException e) {
			System.out.println("[Error] Fail to access " + _inputFile + " or " +  _outputFile);
			e.printStackTrace();
		}
	}
	
	// decode the specific code
	private String parseWord(String _inputCode) {
		// create table and initialize
		_inputCode = _inputCode + " ";
		int cTb[][] = new int[ _inputCode.length() ][ chars.size() ], colSize = chars.size(), rowSize = _inputCode.length();
		double[][] pTb = new double[ rowSize ][ colSize ];
		for(int col = 0 ; col < chars.size() ; col++)
			pTb[0][col] = probMatrix.getProb( ' ', chars.get(col), _inputCode.charAt(0) );
		// finish the table
		for(int row = 1 ; row < rowSize ; row++) {
			char currCode = _inputCode.charAt(row);
			for(int col = 0 ; col < colSize ; col++) {
				pTb[row][col] = 0d;
				char currChar = chars.get(col);
				for(int lastCol = 0 ; lastCol < colSize ; lastCol++) {
					double prob = pTb[row - 1][lastCol] * probMatrix.getProb(chars.get(lastCol), currChar, currCode);
					if(prob > pTb[row][col]) {
						pTb[row][col] = prob;
						cTb[row][col] = lastCol;
					}
				}
			}
		}
		// rebuild the word from the end
		int curCol = chars.indexOf(' ');
		String output = "";
		for(int row = rowSize - 1 ; row > 0 ; row--) {
			curCol = cTb[row][curCol];
			output = chars.get(curCol) + output;
		}
		// use JAZZY to check
		List<String> suggestions = checker.getSuggestions(output);
		for(String corrected : suggestions)	if(corrected.length() == rowSize - 1)	return corrected;
		return output;
	}
	
}
