#!/usr/bin/python
import random
import numpy
import tensorflow as tf

def matrix_factorization(R, P, Q, K, existPairs, steps = 50, alpha = 0.0002, beta = 0.02):
    Q = Q.T
    for step in range(steps):
        for (i, j) in existPairs:
            eij = R[i][j] - numpy.dot(P[i,:],Q[:,j])
            for k in range(K):
                P[i][k] = P[i][k] + alpha * (2 * eij * Q[k][j] - beta * P[i][k])
                Q[k][j] = Q[k][j] + alpha * (2 * eij * P[i][k] - beta * Q[k][j])
        eR = numpy.dot(P,Q)
        e = 0
        for (i, j) in existPairs:
            e = e + pow(R[i][j] - numpy.dot(P[i,:],Q[:,j]), 2)
            for k in range(K):
                e = e + (beta/2) * ( pow(P[i][k],2) + pow(Q[k][j],2) )
        print("\rstep, err = " + str(step) + ", " + str(e), end = "")
        if e < 0.001:
            break
    print()
    return P, Q.T

# deal with input data
# key = {userID in source, -(userID + 1) in target}, value = input matrix's userIndex
userDict = {}
# key = {itemID in source, -(itemID + 1) in target}, value = input matrix's itemIndex
itemDict = {}
userIdx = 0
itemIdx = 0
maxUID = 0
maxIID = 0
sourcePairs = []
trainPairs = []
trainUsers = set()
trainItems = set()
with open("../test3/source.txt", "r") as f:
    for line in f:
        u, i, r = list( map( int, line.split() ) )
        if u not in userDict:
            userDict[u] = userIdx
            userIdx += 1
        if i not in itemDict:
            itemDict[i] = itemIdx
            itemIdx += 1
        sourcePairs.append( (u, i, r) )
with open("../test3/train.txt", "r") as f:
    for line in f:
        u, i, r = list( map( int, line.split() ) )
        trainUsers.add(u)
        trainItems.add(i)
        maxUID = u if u > maxUID else maxUID
        maxIID = i if i > maxIID else maxIID
        if -(u + 1) not in userDict:
            userDict[ -(u + 1) ] = userIdx
            userIdx += 1
        if -(i + 1) not in itemDict:
            itemDict[ -(i + 1) ] = itemIdx
            itemIdx += 1
        trainPairs.append( (u, i, r) )

# setup data with size = (#user + #item) x #trainPair and lable with size 5 x #trainPair
userDictLen = len(userDict)
sourcePairLen = len(sourcePairs)
print( "[Size] #Labled Users =", userDictLen, "#Labled Items =", len(itemDict), "#Total Pairs=", sourcePairLen + len(trainPairs) )
data = numpy.float32( numpy.zeros( (len(itemDict) + userDictLen, len(trainPairs) + sourcePairLen) ) )
lable = numpy.float32( numpy.zeros( (5, len(trainPairs) + sourcePairLen) ) )
for pair in range( len(sourcePairs) ):
    (u, i, r) = sourcePairs[pair]
    data[userDict[u], pair] = 1
    data[userDictLen + itemDict[i], pair] = 1
    lable[r - 1, pair] = 1
for pair in range( len(trainPairs) ):
    (u, i, r) = trainPairs[pair]
    data[userDict[ -(u + 1) ], sourcePairLen + pair]= 1
    data[userDictLen + itemDict[ -(i + 1) ], sourcePairLen + pair] = 1
    lable[r - 1, sourcePairLen + pair] = 1

# setup tensorflow variables
tf.set_random_seed(3318)
random.seed(3318)
# W: weight matrix with size 5(dimension of output layer) x 10(embedding dimension)
W = tf.Variable( tf.random_uniform([5, 10], -1.0, 1.0) )
# E: embedding matrix with size 10(embedding dimension) x (#user + #item)
E = tf.Variable( tf.random_uniform([10, len(itemDict) + userDictLen]) )
# potential: potiential maxtix with size 5(dimension of output layer) x #trainPair
potential = tf.matmul( W, tf.matmul(E, data) )
# prob: softmax results with size 5(dimension of output layer x #trainPair
prob = tf.nn.softmax(potential, dim = 0)
# lost, optimizer and trin function
loss = tf.reduce_mean( tf.square(prob - lable) )
optimizer = tf.train.GradientDescentOptimizer(0.5)
train = optimizer.minimize(loss)

# start the training process
init = tf.global_variables_initializer()
sess = tf.Session()
sess.run(init)
for step in range(0, 50):
    print("\r[Training] Step " + str(step), end = "")
    sess.run(train)
print("\nW: weight matrix with size 5(dimension of output layer) x 10(embedding dimension)")
print( sess.run(W) )
print("E: embedding matrix with size 10(embedding dimension) x (#user + #item)")
print( E.eval(session = sess)[:,1:5], "...")

# add new records to rating matrix
print("[New Record] Adding 10000 New Records")
embedding = E.eval(session = sess)
weight = W.eval(session = sess)
R = numpy.zeros( (maxUID + 1, maxIID + 1) )
existPairs = []
for (u, i, r) in trainPairs:
    R[u, i] = r
    existPairs.append( (u, i) )
for sample in range(10000):
    u = random.choice( list(trainUsers) )
    i = random.choice( list(trainItems) )
    if R[u, i] > 0:
        continue
    newData = numpy.float32( numpy.zeros( (len(itemDict) + userDictLen, 1) ) )
    newData[ [userDict[ -(u + 1) ], itemDict[ -(i + 1) ] + userDictLen], 0 ] = 1
    newPotential = numpy.dot( weight, numpy.dot(embedding, newData) )
    newRate = newPotential.argsort(axis = 0)[4, 0] + 1
    newProb = numpy.exp(newPotential) / numpy.sum( numpy.exp(newPotential) )
    if newProb[newRate - 1, 0] > 0.4:
        R[u, i] = newRate
        existPairs.append( (u, i) )

# MF Process
N = len(R)
M = len( R[0] )
print("[Starting MF] N, M =", N, M)
K = 2
rndState = numpy.random.RandomState(3318)
P = rndState.rand(N, K)
Q = rndState.rand(M, K)
nP, nQ = matrix_factorization(R, P, Q, K, existPairs)
nR = numpy.dot(nP, nQ.T)
print(R[:10][:10])
print(nR[:10][:10])

# output
with open("../test3/test.txt", 'r') as testFile, open("../test3/pred.txt", "w") as outputFile:
    for test in testFile:
        u, i = list( map( int, test.split()[:2] ) )
        if R[u, i] > 0:
            outputFile.write(str(u) + " " + str(i) + " " + str( R[u, i] ) + "\n")
        else:
            nR[u, i] = 5 if nR[u, i] > 5 else nR[u, i]
            nR[u, i] = 1 if nR[u, i] < 1 else nR[u, i]
            outputFile.write(str(u) + " " + str(i) + " " + str( nR[u, i] ) + "\n")

