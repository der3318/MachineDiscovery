package launch;

import config.Const;
import test.EMValidation;
import tool.Bigram;
import tool.Encoder;
import tool.ExpectationMaximization;

public class Main {

	public static void main(String[] args) throws Exception {
		if(args.length < 3) {
			System.out.println("[Usage(Training)] java -cp bin launch.Main train ENCODE_PATH DATA_PATH");
			System.out.println("[Usage(Validation)] java -cp bin launch.Main valid PRED_PATH ANS_PATH");
			return;
		}
		if( args[0].equals("train") ) {
			Const.ENCODE_TABLE_PATH = args[1];
			Const.TEST_DATA_PATH = args[2];
			Encoder encoder = Encoder.getInstance(Const.ENCODE_TABLE_PATH);
			Bigram bigram = Bigram.getInstance();
			new ExpectationMaximization(bigram, encoder, Const.TEST_DATA_PATH, Const.PREDICT_PATH).proceed();
		}
		else if( args[0].equals("valid") ) {
			Const.PREDICT_PATH = args[1];
			Const.ANS_PATH = args[2];
			EMValidation.calcAccuracy(Const.PREDICT_PATH, Const.ANS_PATH);
		}
	}

}
	