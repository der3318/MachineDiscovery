package launch;

import config.Const;
import tool.Bigram;
import tool.Encoder;
import tool.ExpectationMaximization;

public class Main {

	public static void main(String[] args) throws Exception {
		Encoder encoder = Encoder.getInstance(Const.ENCODE_TABLE_PATH);
		Bigram bigram = Bigram.getInstance();
		new ExpectationMaximization(bigram, encoder, Const.TEST_DATA_PATH, Const.PREDICT_PATH).proceed();
	}

}
	