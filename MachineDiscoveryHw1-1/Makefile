JC = javac
SRCPATH = src
BINPATH = bin
JFLAGS = -d $(BINPATH) -sourcepath $(SRCPATH) -encoding utf8

all:
	@echo Compiling Source Codes From src/ Into bin/
	$(JC) $(JFLAGS) src/launch/Main.java

run:
	java -Dfile.encoding=UTF-8 -cp bin launch.Main

