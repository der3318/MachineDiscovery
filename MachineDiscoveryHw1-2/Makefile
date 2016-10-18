JC = javac
SRCPATH = src
BINPATH = bin
JFLAGS = -d $(BINPATH) -sourcepath $(SRCPATH)

all:
	@echo Compiling Source Codes From src/ Into bin/
	$(JC) $(JFLAGS) src/launch/Main.java

run_valid1:
	java -Xmx4096M -cp bin launch.Main train ./docs/valid/encode.bin ./docs/valid/test.num
	java -Xmx4096M -cp bin launch.Main valid ./pred.num ./docs/valid/ans.num

run_valid2:
	java -Xmx4096M -cp bin launch.Main train ./docs/valid2/encode.bin ./docs/valid2/test.num
	java -Xmx4096M -cp bin launch.Main valid ./pred.num ./docs/valid2/ans.num

run_test1:
	java -Xmx4096M -cp bin launch.Main train ./docs/test1/encode.bin ./docs/test1/test.num

run_test2:
	java -Xmx4096M -cp bin launch.Main train ./docs/test2/encode.bin ./docs/test2/test.num

