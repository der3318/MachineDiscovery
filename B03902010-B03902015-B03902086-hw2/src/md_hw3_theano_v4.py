#!/usr/bin/python
import sys
import numpy
import theano.tensor as T
import theano
from theano.tensor.shared_randomstreams import RandomStreams

if len(sys.argv) < 5:
	print('python3 this.py folder nbEpoch output1 output2')
	exit()

def add2SetInDic(dic, x, y):
	if x in dic:
		dic[x].add(y)
	else:
		dic[x] = {y}

# read data from files
folder = sys.argv[1] + '/'
uList = []
u2UDic = {}
u2UInvDic = {}
u2IDic = {}
u2CatDic = {}
i2UDic = {}
i2CatDic = {}
i2CntDic = {}
inputData = []
answer = set()  # { (user, item) }
with open(folder + 'user.txt', 'r') as f:
	for line in f:
		uList.append(int(line))
with open(folder + 'relation.txt', 'r') as f:
	for line in f:
		u1, u2 = map(int, line.split())
		add2SetInDic(u2UDic, u1, u2)
		add2SetInDic(u2UInvDic, u1, u2)
with open(folder + 'message.txt', 'r') as f:
	for line in f:
		u, i, cat, cnt = map(int, line.split())
		add2SetInDic(u2IDic, u, i)
		add2SetInDic(u2CatDic, u, cat)
		add2SetInDic(i2UDic, i, u)
		add2SetInDic(i2CatDic, i, cat)
		if i not in i2CntDic:
			i2CntDic[i] = cnt
nbTest = 0
zeroLikeItemList = []
with open(folder + 'pred.id', 'r') as testFile:
	for test in testFile:
		u, i = list(map(int, test.split()[:2]))
		iLikeCnt = i2CntDic[i] / len(uList) if i in i2CntDic else 0
		if iLikeCnt == 0:
			zeroLikeItemList.append(nbTest)
		else:
			iCntOfU = len(u2IDic[u]) / len(i2CntDic) if u in u2IDic else 0
			isUOwner = int(u in u2IDic and i in u2IDic[u])
			isU2Owner = int(u in u2UDic and i in i2UDic and bool(u2UDic[u] & i2UDic[i]))
			isOwner2U = int(u in u2UInvDic and i in i2UDic and bool(u2UInvDic[u] & i2UDic[i]))
			hasSameCat = int(u in u2CatDic and i in i2CatDic and bool(u2CatDic[u] & i2CatDic[i]))
			u2OtherCnt = len(u2UDic[u]) / len(uList) if u in u2UDic else 0
			other2UCnt = len(u2UInvDic[u]) / len(uList) if u in u2UInvDic else 0
			inputData.append([iCntOfU, isUOwner, isU2Owner, isOwner2U, hasSameCat, iLikeCnt, u2OtherCnt, other2UCnt])
		nbTest += 1
nbLikeInX = nbTest // 2 - len(zeroLikeItemList)
nbNotLikeInX = nbTest // 2

# config
featureDimension = 8
learningRate = 0.0005
trainingSteps = int(sys.argv[2])

# input data
featureVectors = numpy.array(inputData)
numOfPred = featureVectors.shape[0]

# model parameters
# x = feature vectors associated with an item (size = number of users * feature dimension)
x = T.dmatrix()
# y = the count of an item
# y = T.iscalar()
nbLike = T.iscalar()
nbNotLike = T.iscalar()
# th = threshold for prediction only
th = T.dscalar()
# w = weighting vector to be trained, b = bias
w = theano.shared(numpy.ones(featureDimension) , name = "w")
b = theano.shared(-1., name = "b")

# model functions
# probabilities of the pairs (u_1, item), (u_2, item), ..., (u_n, item)
prob = 1 / ( 1 + T.exp(-T.dot(x, w) - b) )
# cost function = -sum(best y pairs) + sum(worst y pairs)
cost = -prob[ prob.argsort()[-nbLike:] ].sum() + prob[ prob.argsort()[:nbNotLike] ].sum()
# predicting function
prediction1 = prob > th
prediction2 = prob >= th
# gradiants
gw, gb = T.grad(cost, [w, b])
# train and predict
train = theano.function(
		inputs = [x, nbLike, nbNotLike],
		outputs = [prob, cost],
		updates = ( (w, w - learningRate * gw), (b, b - learningRate * gb) )
		)
calcProb = theano.function(inputs = [x], outputs = prob)
predict1 = theano.function(inputs = [x, th], outputs = prediction1)
predict2 = theano.function(inputs = [x, th], outputs = prediction2)

# main process (training using GD and predict)
threshold = 0.5
print("Initial Model:", "\n", w.get_value(), b.get_value(), "\n")
for i in range(trainingSteps):
	print("Step =", i + 1)
	p, err = train(featureVectors, nbLikeInX, nbNotLikeInX)
	print("Prob of the events =\n", p, "\nCost =", err, "\n")
print("Final model:", "\n", w.get_value(), b.get_value(), "\n")
p = calcProb(featureVectors)
threshold = p[ p.argsort()[nbNotLikeInX] ]
pred1 = predict1(featureVectors, threshold)
pred2 = predict2(featureVectors, threshold)
for i in zeroLikeItemList:
	pred1.insert(i, False)
	pred2.insert(i, False)

# validation and output
if sys.argv[1] == 'valid':
	with open(folder + 'response.txt', 'r') as response_f:
		for line in response_f:
			u, i = map(int, line.split())
			answer.add( (u, i) )
total = 0
positive1 = positive2 = 0
correct1 = correct2 = 0
with open(folder + 'pred.id', 'r') as testFile, open(folder + sys.argv[3], 'w') as outputFile1, open(folder + sys.argv[4], 'w') as outputFile2:
	for test in testFile:
		u, i = list(map(int, test.split()[:2]))
		if pred1[total]:
			positive1 += 1
		if pred2[total]:
			positive2 += 1
		if pred1[total] and (u, i) in answer or not pred1[total] and (u, i) not in answer:
			correct1 += 1
		if pred2[total] and (u, i) in answer or not pred2[total] and (u, i) not in answer:
			correct2 += 1
		outputFile1.write(str(u) + " " + str(i) + " " + str( int( pred1[total] ) ) + "\n")
		outputFile2.write(str(u) + " " + str(i) + " " + str( int( pred2[total] ) ) + "\n")
		total += 1
if sys.argv[1] == 'valid':
	print("Accuracy 1:", correct1 / total)
	print("Accuracy 2:", correct2 / total)
print("Positive 1:", positive1, positive1 / total)
print("Positive 2:", positive2, positive2 / total)
print("Total:", total)
print("Threshold:", threshold)

