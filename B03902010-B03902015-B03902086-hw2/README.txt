Install Theano
	git clone git://github.com/Theano/Theano.git
	cd Theano
	python3 setup.py develop --user
	cd ..


Run
	python3 src/md_hw3_theano_v4.py valid 30 pred1.txt pred2.txt
	python3 src/md_hw3_theano_v4.py test1 30 pred1.txt pred2.txt
	python3 src/md_hw3_theano_v4.py test2 30 pred1.txt pred2.txt


