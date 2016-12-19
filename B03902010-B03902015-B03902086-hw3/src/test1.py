
# coding: utf-8

import numpy as np
from sys import argv

if len(argv) < 3:
    print("python3 test1.py validation lamb")
    exit()

def line_to_uir(line):
    uir = line.split()
    return int(uir[0]), int(uir[1]), float(uir[2])

def file_to_mat(filename, m, n, valid):
    mat = np.zeros( (m, n) )
    user = []
    item = []
    rats = []
    
    with open(filename) as f:
        lines = f.readlines()
        if valid:
            from random import shuffle
            shuffle(lines)
            bound = len(lines) // 10 * 9
            valids = lines[bound:]
            lines = lines[:bound]
            validList = [line_to_uir(valid) for valid in valids]
        for line in lines:
            u, i, r = line_to_uir(line)
            user.append(u)
            item.append(i)
            rats.append(r)
            mat[u, i] = r

    lst = list(zip(user, item))
    
    if valid:
        return mat, lst, validList
    return mat, lst


M = 50000
N = 5000

validation = int(argv[1])

if validation:
    R1, R1_lst, validList = file_to_mat('../test1/train.txt', M, N, True)   # target domain
else:
    R1, R1_lst = file_to_mat('../test1/train.txt', M, N, False)
R2, R2_lst = file_to_mat('../test1/source.txt', M, N, False)  # source domain

print("finish parsing file")

rate = 0.05
lamb = 0.01 # different for P, Q ???
eps = 20
K = 2


from time import time

def PQ(R, user_item, k=10, maxiter=1000):    
    m, n = R.shape
    P = np.random.rand(m, k)
    Q = np.random.rand(n, k)
    prevsum = -1
    iters = 0
    while iters < maxiter:
        start = time()

        diffsum = 0
        iters += 1
        for i, j in user_item:
            diff = P[i].dot(Q[j]) - R[i, j]
            gP = diff * Q[j] + lamb * P[i]
            gQ = diff * P[i] + lamb * Q[j]
            diffsum += diff ** 2

            P[i] -= rate * gP
            Q[j] -= rate * gQ

        end = time()
        print('iter {} diff {} time {}'.format(iters, diffsum, end - start))

        if abs(prevsum - diffsum) < eps:
            break
        prevsum = diffsum
    
    return P, Q


def error(R, P, Q, user_item):
    s = 0
    for i, j in user_item:
        s += (P[i].dot(Q[j]) - R[i, j]) ** 2
    
    return s


P1, Q1 = PQ(R1, R1_lst, maxiter=2000)
P2, Q2 = PQ(R2, R2_lst, maxiter=2000)

R1_from_PQ = P1.dot(Q1.T)

# e_val = 0
# for u, i, r in validList:
#     e_val += (R1_from_PQ[u, i] - r) ** 2
# print("baseline_err:", str(sqrt(e_val / len(validList))))
# exit()

print(error(R1, P1, Q1, R1_lst))
print(error(R2, P2, Q2, R2_lst))

R2 = None

print("finish MF")

def SVD(X):
    U, d, V = np.linalg.svd(X, full_matrices=False) # full_matrices ???
    D = np.diag(d)
    V = V.T
    
    return U, D, V


def PQ2SVD(P, Q):
    U_P, D_P, V_P = SVD(P)
    U_Q, D_Q, V_Q = SVD(Q)

    X = D_P.dot(V_P.T).dot(V_Q).dot(D_Q.T)
    U_X, D_X, V_X = SVD(X)

    U = U_P.dot(U_X)
    D = D_X
    V = U_Q.dot(V_X)

    return U, D, V


U1, D1, V1 = PQ2SVD(P1, Q1)
U2, D2, V2 = PQ2SVD(P2, Q2)

print("finish SVD")

# without using T for caching
def NN(Z1, Z2):
    # G = argmin | G * Z2 - Z1 |
    #   = argmin | Z2^T * G^T - Z1^T |
    # G^T ~ (Z2^T^+) * Z1^T
    # G ~ [ (Z2^T^+) * Z1^T ]^T
    G = np.linalg.pinv(Z2.T).dot(Z1.T).T
    
    # alpha = min | G * Z2 - Z1 |
    alpha = np.linalg.norm(G.dot(Z2) - Z1) # without squaring
    
    return alpha, G


# without using T for caching
def Matching(Z1, Z2):
    S = np.eye(K)
    G = []
    for k in range(K):
        S[k, k] = +1
        alpha_plus,  G_plus  = NN(Z1[:, :k+1], Z2[:, :k+1].dot(S[:k+1, :k+1]))
        S[k, k] = -1
        alpha_minus, G_minus = NN(Z1[:, :k+1], Z2[:, :k+1].dot(S[:k+1, :k+1]))
        
        if alpha_plus <= alpha_minus:
            G.append(G_plus)
            S[k, k] = +1
        else:
            G.append(G_minus)
            S[k, k] = -1
    
    return G


def Algo1(U1, D1, V1, U2, D2, V2, R1, R1_lst):
    G_user = Matching(U1.dot(np.sqrt(D1)), U2.dot(np.sqrt(D2)))
    G_item = Matching(V1.dot(np.sqrt(D1)), V2.dot(np.sqrt(D2)))

    print("finish matching")

    if K == 1:
        return G_user[0], G_item[0]
    
    errors = []
    R2_hat = U2.dot(D2).dot(V2.T)
    for k in range(K):
        print('iter {} starts'.format(k))
        R1_hat = G_user[k].dot(R2_hat).dot(G_item[k].T)
        diff = R1 - R1_hat
        error = 0
        for i, j in R1_lst:
            error += diff[i, j] ** 2
        errors.append(error)
        print('iter {} ends'.format(k))
    
    k_min = np.argmin(errors)
    return G_user[k_min], G_item[k_min]


Gu, Gi = Algo1(U1, D1, V1, U2, D2, V2, R1, R1_lst)

R1 = None

print("finish algo1")

R1_hat = U1.dot(D1).dot(V1.T)
R2_hat = U2.dot(D2).dot(V2.T)

U1 = D1 = V1 = U2 = D2 = V2 = None

if validation:
    from math import sqrt
    nb_step = 10
    for lamb in range(nb_step + 1):
        lamb /= nb_step
        R = R1_hat * lamb + Gu.dot(R2_hat).dot(Gi.T) * (1 - lamb)
        e_val = 0
        for u, i, r in validList:
            e_val += (R[u, i] - r) ** 2
        print("lamb", str(lamb), "val_err", str(sqrt(e_val / len(validList))))
    e_val = 0
    for u, i, r in validList:
        e_val += (R1_from_PQ[u, i] - r) ** 2
    print("baseline_err", str(sqrt(e_val / len(validList))))
else:
    lamb = float(argv[2]) # 0.38 is good
    R = R1_hat * lamb + Gu.dot(R2_hat).dot(Gi.T) * (1 - lamb)

def predict(R):
    with open('../test1/test.txt', 'r') as test, open('../test1/pred.txt', 'w') as output:
        for line in test:
            u, i, q = line.split()
            u = int(u)
            i = int(i)
            # output.write('%d %d %.3f\n' % (u, i, P[u].dot(Q[i])))
            output.write('%d %d %.3f\n' % (u, i, max(min(R[u, i], 1.0), 0.0)))


##### Baseline 
# predict(R1_from_PQ)

##### Paper's approach
if not validation:
    predict(R)

# https://hackmd.io/CbDsoFkgOBaA2ARgQzhAzAUwKy0QTgDNNZ8AGfUM6TZAJkPQGMg=?view#