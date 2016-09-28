package launch;

import tool.ArticleParser;
import tool.Bigram;
import tool.Decoder;
import tool.Encoder;

public class Main {
	
	public static void main(String[] args) throws Exception {
		new ArticleParser( Decoder.getInstance( Bigram.getInstance("./bigram.txt"), Encoder.getInstance("./encode.txt") ) ).parse("./test.txt", "./pred.txt");
	}
	
}
