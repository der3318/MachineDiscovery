package launch;

import tool.ArticleParser;
import tool.Bigram;
import tool.Encoder;
import tool.ProbMatrix;

public class Main {

	public static void main(String[] args) throws Exception {
		new ArticleParser(
				ProbMatrix.getInstance( Bigram.getInstance("./docs/bigram.txt"), Encoder.getInstance("./docs/encode.txt") )
				).parse("./docs/test.txt", "./pred.txt");
	}

}
	