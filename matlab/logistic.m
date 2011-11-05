trainFeatures = readMatrix('MATRIX.TRAIN.X');
trainLabels = readMatrix('MATRIX.TRAIN.Y');

testFeatures = readMatrix('MATRIX.TEST.X');
testLabels = readMatrix('MATRIX.TEST.Y');

theta = logRegTrain(trainFeatures, trainLabels);
error = logRegTest(testFeatures, testLabels, theta);

sprintf('Error: %d', error)