#### Machine Discovery 2016
###### Student ID
B03902015


###### Prerequisite
* JDK/JRE-1.8
* Python3
* Theano


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
		* `MachineDiscoveryHw1-2$ java -Xmx4096M -cp bin launch.Main train ./valid/encode.bin ./valid/test.num`
		* `MachineDiscoveryHw1-2$ java -Xmx4096M -cp bin launch.Main valid ./pred.num ./valid/ans.num`


###### Homework 2
* Directory: `B03902010-B03902015-B03902086-hw2/`
* Source: `B03902010-B03902015-B03902086-hw2/src/`
* Install and Run (Linux)
	* Install
		* `B03902010-B03902015-B03902086-hw2$ git clone git://github.com/Theano/Theano.git`
		* `B03902010-B03902015-B03902086-hw2$ cd Theano`
		* `B03902010-B03902015-B03902086-hw2$ python3 setup.py develop --user`
	* Run
		* `B03902010-B03902015-B03902086-hw2$ python3 src/md_hw3_theano_v4.py valid 30 pred1.txt pred2.txt`
		* `B03902010-B03902015-B03902086-hw2$ python3 src/md_hw3_theano_v4.py test1 30 pred1.txt pred2.txt`
		* `B03902010-B03902015-B03902086-hw2$ python3 src/md_hw3_theano_v4.py test2 30 pred1.txt pred2.txt`

