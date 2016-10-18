JC = javac
SRCPATH = src
BINPATH = bin
JFLAGS = -d $(BINPATH) -sourcepath $(SRCPATH)

all:
	@echo Compiling Source Codes From src/ Into bin/
	$(JC) $(JFLAGS) src/launch/Main.java

run:
	java -Xmx4096M -cp bin launch.Main

