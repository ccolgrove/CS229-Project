function [ theta ] = logRegTrain( features, labels )
% trains a logistic regression classifier on the examples given in
% features/examples

featureSize = size(features);
interceptFeatures = ones(featureSize);
interceptFeatures(:,2:featureSize(2)+1) = features;

iterations = 20;
theta = zeros(size(interceptFeatures, 2), 1);

for i = 1:20
    H = hessian(theta, interceptFeatures);
    g = gradient(theta, interceptFeatures, labels);
    theta = theta - inv(H)*g;
end


end

