For changing cache policy - trying with P6
___________________________________________
P6.SFT.C64K.M3.HB20.Lru | cost = 2.7982 | indSize = 414998, uInterval = 21796 |  FP = 0.1119, FN = 0.0106
P6.SFT.C64K.M3.HB20.Lfu | cost = 1.9065 | indSize = 414998, uInterval = 21796 |  FP = 0.0208, FN = 0.0035

P6.SFT.C64K.M3.HB20.Lru | cost = 3.0320 | indSize = 960000, uInterval = 14331 |  FP = 0.2686, FN = 0.0492
P6.SFT.C64K.M3.HB20.Lfu | cost = 1.8871 | indSize = 960000, uInterval = 14331 |  FP = 0.0013, FN = 0.0036

Alternatively, use:
gradle.SFT.C4K.M3.HB20.Lru | cost = 1.2008 | indSize = 10000, uInterval = 10 |  FP = 0.0259, FN = 0.0012
gradle.SFT.C4K.M3.HB20.Lfu | cost = 1.5288 | indSize = 10000, uInterval = 10 |  FP = 0.0701, FN = 0.0027

gradle.SFT.C4K.M3.HB20.Lru | cost = 1.1766 | indSize = 41772, uInterval = 10 |  FP = 0.0006, FN = 0.0018
gradle.SFT.C4K.M3.HB20.Lfu | cost = 1.5940 | indSize = 41772, uInterval = 10 |  FP = 0.0053, FN = 0.0677

For changing traces
___________________

scarab.SFT.C16K.M3.HB20.Lru | cost = 1.8699 | indSize = 40000, uInterval = 10 |  FP = 0.1132, FN = 0.0035
wiki.SFT.C16K.M3.HB20.Lru | cost = 1.8256 | indSize = 140000, uInterval = 10 |  FP = 0.0162, FN = 0.0113

scarab.SFT.C16K.M3.HB20.Lru | cost = 1.9501 | indSize = 140000, uInterval = 10 |  FP = 0.0309, FN = 0.0848
wiki.SFT.C16K.M3.HB20.Lru | cost = 1.9054 | indSize = 40000, uInterval = 10 |  FP = 0.1186, FN = 0.0000



For changing cache sizes
_________________________
F2.SFT.C16K.M3.HB20.Lru | cost = 2.0695 | indSize = 103749, uInterval = 5387 |  FP = 0.0322, FN = 0.0531
F2.SFT.C64K.M3.HB20.Lru | cost = 1.6346 | indSize = 103749, uInterval = 5387 |  FP = 0.1131, FN = 0.0148

F2.SFT.C16K.M3.HB20.Lru | cost = 2.1376 | indSize = 283449, uInterval = 14331 |  FP = 0.0282, FN = 0.0891
F2.SFT.C64K.M3.HB20.Lru | cost = 1.5944 | indSize = 283449, uInterval = 14331 |  FP = 0.0321, FN = 0.0352





gradle.static.C4K.M3.HB20.Lru | cost = 1.177 | ind Size = 41772, uInterval = 10, FP = 0.0006, FN = 0.0018 slack = 0.00
gradle.static.C4K.M3.HB20.Lfu | cost = 1.485 | ind Size = 19487, uInterval = 10, FP = 0.0243, FN = 0.0034 slack = 0.01

wiki.SFT.C4K.M3.HB20.Lru | cost = 2.0323 | indSize = 10000, uInterval = 10 |  FP = 0.1347, FN = 0.0000
wiki.SFT.C4K.M3.HB20.Frd | cost = 1.9236 | indSize = 10000, uInterval = 10 |  FP = 0.1199, FN = 0.0000

wiki.SFT.C4K.M3.HB20.Lru | cost = 1.9389 | indSize = 40000, uInterval = 10 |  FP = 0.0159, FN = 0.0128
wiki.SFT.C4K.M3.HB20.Frd | cost = 1.8125 | indSize = 40000, uInterval = 10 |  FP = 0.0049, FN = 0.0020




//8.5 bpe
F2.SFT.C4K.M3.HB20.Lru  | cost = 2.4664 | indSize = 34000, uInterval = 1761 |  FP = 0.0252, FN = 0.0558 
F2.SFT.C64K.M3.HB20.Lru | cost = 1.6014 | indSize = 544000, uInterval = 1761 |  FP = 0.0107, FN = 0.0393 

// 4.4 bpe
F2.SFT.C4K.M3.HB20.Lru  | cost = 2.6932 | indSize = 17600, uInterval = 14331 |  FP = 0.1338, FN = 0.1149
F2.SFT.C64K.M3.HB20.Lru | cost = 1.6113 | indSize = 281600, uInterval = 14331 |  FP = 0.0298, FN = 0.0347

//2.5 bpe 
F2.SFT.C4K.M3.HB20.Lru | cost = 2.5767 | indSize = 10000, uInterval = 500 |  FP = 0.2046, FN = 0.0213
F2.SFT.C64K.M3.HB20.Lru | cost = 1.6024 | indSize = 160000, uInterval = 500 |  FP = 0.0675, FN = 0.0114


F2.static.C16K.M3.HB20.Lru | cost = 2.0695 | ind Size = 103749, uInterval = 5387, FP = 0.0322, FN = 0.0531 slack = 1.04
F2.static.C64K.M3.HB20.Lru | cost = 1.5944 | ind Size = 283449, uInterval = 14331, FP = 0.0322, FN = 0.0352 slack = 1.01


F2.SFT.C64K.M3.HB20.Lru | cost = 1.6444 | indSize = 103749, uInterval = 5387 |  FP = 0.1031, FN = 0.0146
F2.SFT.C16K.M3.HB20.Lru | cost = 2.0718 | indSize = 103749, uInterval = 5387 |  FP = 0.0318, FN = 0.0528

F2.SFT.C64K.M3.HB20.Lru | cost = 1.6108 | indSize = 283449, uInterval = 14331 |  FP = 0.0294, FN = 0.0346
F2.SFT.C16K.M3.HB20.Lru | cost = 2.1396 | indSize = 283449, uInterval = 14331 |  FP = 0.0280, FN = 0.0886



******************** The conf's to run are:
C1: F2.static.C16K.M3.HB20.Lru | cost = 2.0695 | ind Size = 103749, uInterval = 5387, FP = 0.0322, FN = 0.0531 slack = 1.04
C2: scarab.static.C16K.M3.HB20.Lru | cost = 1.8124 | ind Size = 64420, uInterval = 10, FP = 0.0541, FN = 0.0043 slack = 0.00

C1: F2.static.C16K.M3.HB20.Lru | cost = 2.0695 | ind Size = 103749, uInterval = 5387, FP = 0.0322, FN = 0.0531 slack = 1.04
C3: F2.static.C16K.M3.HB20.Lfu | cost = 1.9035 | ind Size = 125537, uInterval = 1158, FP = 0.0134, FN = 0.0246 slack = 0.18

C1: F2.static.C16K.M3.HB20.Lru | cost = 2.0695 | ind Size = 103749, uInterval = 5387, FP = 0.0322, FN = 0.0531 slack = 1.04
C4 :F2.static.C64K.M3.HB20.Lru | cost = 1.5944 | ind Size = 283449, uInterval = 14331, FP = 0.0322, FN = 0.0352 slack = 1.01
*****************************

F2.SFT.C16K.M3.HB20.Lru     | cost = 2.0718 | indSize = 103749, uInterval = 5387 |  FP = 0.0318, FN = 0.0528
scarab.SFT.C16K.M3.HB20.Lru | cost = 1.9722 | indSize = 103749, uInterval = 5387 |  FP = 0.0229, FN = 0.0877
F2.SFT.C16K.M3.HB20.Lru 	| cost = 2.0718 | indSize = 103749, uInterval = 5387 |  FP = 0.0318, FN = 0.0528
scarab.SFT.C16K.M3.HB20.Lru | cost = 1.9722 | indSize = 103749, uInterval = 5387 |  FP = 0.0229, FN = 0.0877
F2.SFT.C16K.M3.HB20.Hyper   | cost = 2.0341 | indSize = 103749, uInterval = 5387 |  FP = 0.0294, FN = 0.0386
F2.SFT.C16K.M3.HB20.Frd 	| cost = 1.9614 | indSize = 103749, uInterval = 5387 |  FP = 0.0267, FN = 0.0481

C2: indSize = 64420, uInterval = 10
F2.SFT.C16K.M3.HB20.Lru     | cost = 2.0134 | indSize = 64420, uInterval = 10 |  FP = 0.0687, FN = 0.0052 pp
scarab.SFT.C16K.M3.HB20.Lru | cost = 1.8355 | indSize = 64420, uInterval = 10 |  FP = 0.0531, FN = 0.0042
F2.SFT.C16K.M3.HB20.Hyper   | cost = 1.9995 | indSize = 64420, uInterval = 10 |  FP = 0.0666, FN = 0.0027
F2.SFT.C16K.M3.HB20.Frd     | cost = 1.9016 | indSize = 64420, uInterval = 10 |  FP = 0.0597, FN = 0.0017

ind Size = 40000, uInterval = 10
F2.SFT.C16K.M3.HB20.Lru 	| cost = 2.0752 | indSize = 40000, uInterval = 10 |  FP = 0.1391, FN = 0.0009
scarab.SFT.C16K.M3.HB20.Lru | cost = 1.8918 | indSize = 40000, uInterval = 10 |  FP = 0.1109, FN = 0.0035  **
wiki.SFT.C16K.M3.HB20.Lru   | cost = 1.9093 | indSize = 40000, uInterval = 10 |  FP = 0.1181, FN = 0.0000
wiki.SFT.C16K.M3.HB20.Frd   | cost = 1.8292 | indSize = 40000, uInterval = 10 |  FP = 0.1078, FN = 0.0000
F2.SFT.C16K.M3.HB20.Frd     | cost = 1.9641 | indSize = 40000, uInterval = 10 |  FP = 0.1245, FN = 0.0005

ind Size = 140000, uInterval = 10
F2.SFT.C16K.M3.HB20.Lru 	| cost = 2.1826 | indSize = 140000, uInterval = 10 |  FP = 0.0470, FN = 0.1006
scarab.SFT.C16K.M3.HB20.Lru | cost = 1.9705 | indSize = 140000, uInterval = 10 |  FP = 0.0303, FN = 0.0831
wiki.SFT.C16K.M3.HB20.Lru   | cost = 1.8300 | indSize = 140000, uInterval = 10 |  FP = 0.0162, FN = 0.0113 **
wiki.SFT.C16K.M3.HB20.Frd   | cost = 1.7336 | indSize = 140000, uInterval = 10 |  FP = 0.0070, FN = 0.0026
wiki.SFT.C16K.M3.HB20.Lfu   | cost = 1.7196 | indSize = 140000, uInterval = 10 |  FP = 0.0055, FN = 0.0007
F2.SFT.C16K.M3.HB20.Frd     | cost = 1.9834 | indSize = 140000, uInterval = 10 |  FP = 0.0235, FN = 0.0607


ind Size = 240000, uInterval = 10
F2.SFT.C16K.M3.HB20.Lru     | cost = 2.2674 | indSize = 240000, uInterval = 10 |  FP = 0.0577, FN = 0.1376
scarab.SFT.C16K.M3.HB20.Lru | cost = 2.0523 | indSize = 240000, uInterval = 10 |  FP = 0.0374, FN = 0.1205
wiki.SFT.C16K.M3.HB20.Lru   | cost = 1.8401 | indSize = 240000, uInterval = 10 |  FP = 0.0156, FN = 0.0167 **
wiki.SFT.C16K.M3.HB20.Frd   | cost = 1.7347 | indSize = 240000, uInterval = 10 |  FP = 0.0035, FN = 0.0049
wiki.SFT.C16K.M3.HB20.Lfu   | cost = 1.7164 | indSize = 240000, uInterval = 10 |  FP = 0.0008, FN = 0.0015
F2.SFT.C16K.M3.HB20.Frd     | cost = 2.0584 | indSize = 240000, uInterval = 10 |  FP = 0.0282, FN = 0.0958


ind Size = 60000, uInterval = 1000
F2.SFT.C16K.M3.HB20.Lru     | cost = 2.0648 | indSize = 60000, uInterval = 1000 |  FP = 0.0802, FN = 0.0251
scarab.SFT.C16K.M3.HB20.Lru | cost = 1.9370 | indSize = 60000, uInterval = 1000 |  FP = 0.0622, FN = 0.0504
F2.SFT.C16K.M3.HB20.Frd     | cost = 1.9557 | indSize = 60000, uInterval = 1000 |  FP = 0.0707, FN = 0.0232


F2.SFT.C16K.M3.HB20.Lru | cost = 2.0880 | indSize = 60000, uInterval = 3000 |  FP = 0.0824, FN = 0.0356
F2.SFT.C16K.M3.HB20.Frd | cost = 1.9788 | indSize = 60000, uInterval = 3000 |  FP = 0.0733, FN = 0.0335

F2.SFT.C16K.M3.HB20.Frd | cost = 1.9955 | indSize = 60000, uInterval = 5000 |  FP = 0.0756, FN = 0.0407

F2.SFT.C16K.M3.HB20.Lru | cost = 2.1235 | indSize = 240000, uInterval = 12000 |  FP = 0.0243, FN = 0.0824
F2.SFT.C16K.M3.HB20.Frd | cost = 1.9870 | indSize = 240000, uInterval = 12000 |  FP = 0.0140, FN = 0.0672
F2.SFT.C16K.M3.HB20.Lfu | cost = 1.9300 | indSize = 240000, uInterval = 12000 |  FP = 0.0031, FN = 0.0398

F2.SFT.C16K.M3.HB20.Lru | cost = 2.2848 | indSize = 240000, uInterval = 1000 |  FP = 0.0586, FN = 0.1459
F2.SFT.C16K.M3.HB20.Frd | cost = 2.0773 | indSize = 240000, uInterval = 1000 |  FP = 0.0293, FN = 0.1047
F2.SFT.C16K.M3.HB20.Lfu | cost = 1.9237 | indSize = 240000, uInterval = 1000 |  FP = 0.0045, FN = 0.0359

wiki.SFT.C16K.M3.HB20.Lfu   | cost = 1.7149 | indSize = 240000, uInterval = 1000 |  FP = 0.0006, FN = 0.0009


C1:
F2.SFT.C16K.M3.HB20.Lfu | cost = 1.9333 | indSize = 103749, uInterval = 5387 |  FP = 0.0215, FN = 0.0322

C2:
F2.SFT.C16K.M3.HB20.Lfu | cost = 1.9087 | indSize = 64420, uInterval = 10 |  FP = 0.0599, FN = 0.0007














P3.static.C16K.M3.HB20.Lru | cost = 2.9893 | ind Size = 240000, uInterval = 12462, FP = 0.0042, FN = 0.0130 slack = 1.04
wiki.static.C16K.M3.HB20.Lru | cost = 1.804 | ind Size = 183798, uInterval = 9423, FP = 0.0056, FN = 0.0058 slack = 1.03


C1 - Best for Scarab 16k: ind Size = 64420, uInterval = 10
scarab.SFT.C16K.M3.HB20.Lru | cost = 1.8355
P3.SFT.C16K.M3.HB20.Lru     | cost = 3.1309
wiki.SFT.C16K.M3.HB20.Lru | cost = 1.8476 | FP = 0.0564, FN = 0.0000

C2 - Best for P3: ind Size = 240000, uInterval = 12462
scarab.SFT.C16K.M3.HB20.Lru | cost = 2.0070 | FP = 0.0153, FN = 0.1089
P3.SFT.C16K.M3.HB20.Lru | cost = 2.9893 | FP = 0.0041, FN = 0.0129
wiki.SFT.C16K.M3.HB20.Lru | cost = 1.8114 | FP = 0.0055, FN = 0.0074


C3 - best for F1 16K: ind Size = 414998, uInterval = 21796, FP = 0.0342, FN = 0.1255 slack = 1.05

Try C1, C3 for Scarab Vs. F1, Lru, 16k.

C4 - best for F2: ind Size = 283449, uInterval = 14331, FP = 0.0322, FN = 0.0352 slack = 1.01
 
C5 - best for scarab C64K Lru  | cost = 1.4582 | ind Size = 414998, uInterval = 10, FP = 0.0096, FN = 0.0038 slack = 0.00
scarab.static.C16K.M3.HB20.Lru | cost = 1.8124 | ind Size = 64420, uInterval = 10, FP = 0.0541, FN = 0.0043 slack = 0.00

******************** The conf's to run are:
F2.static.C16K.M3.HB20.Lru | cost = 2.0695 | ind Size = 103749, uInterval = 5387, FP = 0.0322, FN = 0.0531 slack = 1.04
scarab.static.C16K.M3.HB20.Lru | cost = 1.8124 | ind Size = 64420, uInterval = 10, FP = 0.0541, FN = 0.0043 slack = 0.00

F2.static.C16K.M3.HB20.Lru | cost = 2.0695 | ind Size = 103749, uInterval = 5387, FP = 0.0322, FN = 0.0531 slack = 1.04
F2.static.C16K.M3.HB20.Lfu | cost = 1.9035 | ind Size = 125537, uInterval = 1158, FP = 0.0134, FN = 0.0246 slack = 0.18

F2.static.C16K.M3.HB20.Lru | cost = 2.0695 | ind Size = 103749, uInterval = 5387, FP = 0.0322, FN = 0.0531 slack = 1.04
F2.static.C64K.M3.HB20.Lru | cost = 1.5944 | ind Size = 283449, uInterval = 14331, FP = 0.0322, FN = 0.0352 slack = 1.01
*****************************




With F1, F2 the trick doesn't go well, because both of them has many spikes, causing the lowest Phit (and the maxBw) of F2 to be similar to that of F1.
F1.static.C64K.M3.HB20.Lru | cost = 2.4239 | ind Size = 414998, uInterval = 21796, FP = 0.0342, FN = 0.1255 slack = 1.05
F2.static.C64K.M3.HB20.Lru | cost = 1.5944 | ind Size = 283449, uInterval = 14331, FP = 0.0322, FN = 0.0352 slack = 1.01

F1.SFT.C64K.M3.HB20.Lru | cost = 2.4849 | meanBw used = 19.03 | hit ratio = 0.3891
F2.SFT.C64K.M3.HB20.Lru | cost = 1.6182 | meanBw used = 13.79 | hit ratio = 0.7439

F2.SFT.C64K.M3.HB20.Lru | cost = 1.6108 | meanBw used = 11.15 | hit ratio = 0.7439


F1.SFT.C64K.M3.HB20.Lru | cost = 2.4974 | meanBw used = 19.71 | hit ratio = 0.3891
F2.SFT.C64K.M3.HB20.Lru | cost = 1.6108 | meanBw used = 11.15 | hit ratio = 0.7439


F1.SFT.C16K.M3.HB20.Lru | cost = 2.6350 | meanBw used = 19.24 | hit ratio = 0.3415
F2.SFT.C16K.M3.HB20.Lru | cost = 2.0718 | meanBw used = 18.76 | hit ratio = 0.5328

F1.SFT.C16K.M3.HB20.Lru | cost = 2.6297 | meanBw used = 19.03 | hit ratio = 0.3415
F2.SFT.C16K.M3.HB20.Lru | cost = 2.0750 | meanBw used = 17.75 | hit ratio = 0.5328


F2.static.C16K.M3.HB20.Lru | cost = 2.0695 | ind Size = 103749, uInterval = 5387, FP = 0.0322, FN = 0.0531 slack = 1.04

F1.static.C16K.M3.HB20.Lru | cost = 2.6157 | ind Size = 77948, uInterval = 4073, FP = 0.0658, FN = 0.1274 slack = 1.05













//F1.static.C16K.M3.HB20.Lru | cost = 2.6157 		| ind Size = 77948, uInterval = 4073, FP = 0.0658, FN = 0.1274 slack = 1.05
//F2.static.C16K.M3.HB20.Lru | cost = 2.070 			| ind Size = 103749, uInterval = 5387, FP = 0.0322, FN = 0.0531 slack = 1.04
//scarab.static.C16K.M3.HB20.Lru | cost = 1.8124 	| ind Size = 64420, uInterval = 10, FP = 0.0541, FN = 0.0043 slack = 0.00
//gradle.static.C16K.M3.HB20.Lru | cost = 1.153 | ind Size = 202178, uInterval = 10, FP = 0.0002, FN = 0.0016 slack = 0.00

C0: (40000,   1761) //F1.C16K.Lru | cost = 2.6434 | maxBw = 21.07 *** Uses too much BW  //P3.C16K.Lru | cost = 3.2677 | maxBw = 22.75 *** Uses too much BW 
// In F2, scarab, wiki, C0 isn't good enough

//   C1: (40000,   2025)
C2: (77948, 4073)
C3: (183798,     9423)
C4: (240000, 12462)
C5: 

								//   				F1.C16K.Lru | cost = 2.6505 | maxBw = 19.14
F1.C16K.Lru | cost = 2.6157 | maxBw = 19.24 *
F1.C16K.Lru | cost = 2.6477 | maxBw = 19.53
F1.C16K.Lru | cost = 2.6648 | maxBw = 19.50

													//F2.C16K.Lru | cost = 2.1263 | maxBw = 19.99
F2.C16K.Lru | cost = 2.0729 | maxBw = 19.49 ***
F2.C16K.Lru | cost = 2.0963 | maxBw = 19.53
F2.C16K.Lru | cost = 2.1228 | maxBw = 19.50

													scarab.C16K.Lru | cost = 1.9699 | maxBw = 10.97
scarab.C16K.Lru | cost = 1.9503 | maxBw = 19.46 *
scarab.C16K.Lru | cost = 1.9719 | maxBw = 19.53
scarab.C16K.Lru | cost = 1.9873 | maxBw = 19.50

													wiki.C16K.Lru | cost = 1.9080 | maxBw = 11.17
wiki.C16K.Lru | cost = 1.8312 | maxBw = 19.24
wiki.C16K.Lru | cost = 1.8039 | maxBw = 19.53 *
wiki.C16K.Lru | cost = 1.8071 | maxBw = 19.50

													P3.C16K.Lru | cost = 3.2683 | maxBw = 19.75
P3.C16K.Lru | cost = 3.0734 | maxBw = 19.49
P3.C16K.Lru | cost = 2.9898 | maxBw = 19.53
P3.C16K.Lru | cost = 2.9893 | maxBw = 19.50 *

													P8.C16K.Lru | cost = 3.1459 | maxBw = 20.00
P8.C16K.Lru | cost = 2.9636 | maxBw = 19.49
P8.C16K.Lru | cost = 2.9034 | maxBw = 19.53 *
P8.C16K.Lru | cost = 2.9181 | maxBw = 19.50



wiki.SFT.C16K.M3.HB20.Lru | cost = 1.8082 | FP = 0.0056, FN = 0.0057
F1.SFT.C16K.M3.HB20.Lru 	| cost = 2.6154 | indSize = 60000, uInterval = 1000 |  FP = 0.1118, FN = 0.0933
F1.SFT.C16K.M3.HB20.Lru        | cost = 2.7885 | indSize = 103749, uInterval = 105387 |  FP = 0.0538, FN = 0.2088
F1.SFT.C16K.M3.HB20.Lru     | cost = 2.5175 | indSize = 64420, uInterval = 10 |  FP = 0.0983, FN = 0.0510

F2.SFT.C16K.M3.HB20.Lru 	   | cost = 2.3754 | indSize = 103749, uInterval = 105387 |  FP = 0.0915, FN = 0.1747
wiki.SFT.C16K.M3.HB20.Lru      | cost = 1.8990 | indSize = 103749, uInterval = 105387 |  FP = 0.0366, FN = 0.0356
F2.SFT.C16K.M3.HB20.Hyperbolic | cost = 2.2014 | indSize = 103749, uInterval = 105387 |  FP = 0.0558, FN = 0.1090


