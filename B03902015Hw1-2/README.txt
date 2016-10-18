* Prerequisite
	* JDK/JRE-1.8
* `Makefile` is available
	* `b03902015$ make`
	* `b03902015$ make run_valid1` (Use "./docs/valid/encode.bin" and "./docs/valid/test.num" to generate "./pred.num")
	* `b03902015$ make run_valid2` (Use "./docs/valid2/encode.bin" and "./docs/valid2/test.num" to generate "./pred.num")
	* `b03902015$ make run_test1` (Use "./docs/test1/encode.bin" and "./docs/test1/test.num" to generate "./pred.num")
	* `b03902015$ make run_test2` (Use "./docs/test2/encode.bin" and "./docs/test2/test.num" to generate "./pred.num")
* Commands
	* `b03902015$ javac -d bin -sourcepath src src/launch/Main.java`
	* `b03902015$ java -Xmx4096M -cp bin launch.Main train ./docs/valid/encode.bin ./docs/valid/test.num` (Generate "./pred.num")
	* `b03902015$ java -Xmx4096M -cp bin launch.Main valid ./pred.num ./docs/valid/ans.num` (Optional, for testing accuracy)
