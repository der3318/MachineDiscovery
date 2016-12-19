Prerequisite
	
	JDK/JRE-1.8


Makefile is available
	
	B03902015$ make
		Compile the source code in "src/" to "bin/"

	B03902015$ make run_valid1
		Input: "./valid/encode.bin" and "./valid/test.num"
		Ouptut: "./pred.num" and the accuracy dumped by standard-out

	B03902015$ make run_valid2
		Input: "./valid2/encode.bin" and "./valid2/test.num"
		Ouptut: "./pred.num" and the accuracy dumped by standard-out

	B03902015$ make run_test1
		Input: "./test1/encode.bin" and "./test1/test.num"
		Ouptut: "./pred.num"

	B03902015$ make run_test2
		Input: "./test2/encode.bin" and "./test2/test.num"
		Ouptut: "./pred.num"


Commands

	B03902015$ javac -d bin -sourcepath src src/launch/Main.java
		Compile the source code in "src/" to "bin/"
	
	B03902015$ java -Xmx4096M -cp bin launch.Main train ./valid/encode.bin ./valid/test.num
		Input: "./valid/encode.bin" and "./valid/test.num"
		Ouptut: "./pred.num"
		It's avaliable to modify the input arguments when testing different data sets

	B03902015$ java -Xmx4096M -cp bin launch.Main valid ./pred.num ./valid/ans.num
		Input: "./pred.num" and "./valid/ans.num"
		Output: The accuracy dumped by standard-out
		Optional, since there mihgt not be an answer file


The process will generate "./pred.txt" according to the given test data and it takes about 3 hours and at most 4G RAM
