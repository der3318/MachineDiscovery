JC = javac
SRCPATH = src
BINPATH = bin
JFLAGS = -d $(BINPATH) -sourcepath $(SRCPATH) -cp libs/jazzy-core-0.5.2.jar

all:
	@echo Compiling Source Codes From src/ Into bin/
	$(JC) $(JFLAGS) src/launch/Main.java

run:
	java -Xmx1024M -cp bin:libs/jazzy-core-0.5.2.jar launch.Main

