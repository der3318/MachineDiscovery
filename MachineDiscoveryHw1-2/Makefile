JC = javac
SRCPATH = src
BINPATH = bin
JFLAGS = -d $(BINPATH) -sourcepath $(SRCPATH)

all:
	@echo Compiling Source Codes From src/ Into bin/
	$(JC) $(JFLAGS) src/launch/Main.java

run_valid1:
	java -Xmx4096M -cp bin launch.Main train ./valid/encode.bin ./valid/test.num
	java -Xmx4096M -cp bin launch.Main valid ./pred.num ./valid/ans.num

run_valid2:
	java -Xmx4096M -cp bin launch.Main train ./valid2/encode.bin ./valid2/test.num
	java -Xmx4096M -cp bin launch.Main valid ./pred.num ./valid2/ans.num

run_test1:
	java -Xmx4096M -cp bin launch.Main train ./test1/encode.bin ./test1/test.num

run_test2:
	java -Xmx4096M -cp bin launch.Main train ./test2/encode.bin ./test2/test.num

