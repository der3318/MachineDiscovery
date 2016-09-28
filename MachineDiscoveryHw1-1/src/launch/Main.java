package launch;

import jazzy.JazzySpellChecker;
import tool.ArticleParser;
import tool.Bigram;
import tool.ProbMatrix;
import tool.Encoder;

public class Main {

	public static void main(String[] args) throws Exception {
		new ArticleParser(
				ProbMatrix.getInstance( Bigram.getInstance("./docs/bigram.txt"), Encoder.getInstance("./docs/encode.txt") ),
				JazzySpellChecker.getInstance("./docs/dictionary.txt")
				).parse("./docs/test.txt", "./docs/pred.txt");
	}

}
	