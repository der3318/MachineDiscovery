#### Machine Discovery 2016
###### Student ID
B03902015


###### Prerequisite
* JDK/JRE-1.8


###### Homework 1-1
* Directory: `MachineDiscoveryHw1-1/`
* Source: `MachineDiscoveryHw1-1/src/`
* Compile & Run
	* `Makefile` is available
		* `MachineDiscoveryHw1-1$ make`
		* `MachineDiscoveryHw1-1$ make run`
	* Commands
		* `MachineDiscoveryHw1-1$ javac -d bin -sourcepath src src/launch/Main.java`
		* `MachineDiscoveryHw1-1$ java -Xmx1024M -cp bin launch.Main`


###### Homework 1-2
* Directory: `MachineDiscoveryHw1-2/`
* Source: `MachineDiscoveryHw1-2/src/`
* Compile & Run
	* `Makefile` is available
		* `MachineDiscoveryHw1-2$ make`
		* `MachineDiscoveryHw1-2$ make run_valid1`
		* `MachineDiscoveryHw1-2$ make run_valid2`
		* `MachineDiscoveryHw1-2$ make run_test1`
		* `MachineDiscoveryHw1-2$ make run_test2`
	* Commands
		* `MachineDiscoveryHw1-2$ javac -d bin -sourcepath src src/launch/Main.java`
		* `MachineDiscoveryHw1-2$ java -Xmx4096M -cp bin launch.Main train ./docs/valid/encode.bin ./docs/valid/test.num`
		* `MachineDiscoveryHw1-2$ java -Xmx4096M -cp bin launch.Main valid ./pred.num ./docs/valid/ans.num`

