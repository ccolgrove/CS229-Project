function [ error ] = logRegTest( features, labels, theta )
%training a logistic regression classifier

featureSize = size(features);
interceptFeatures = ones(featureSize);
interceptFeatures(:,2:featureSize(2)+1) = features;

errors = 0;
for i = 1:featureSize(1)
   example = interceptFeatures(i,:)';
   label = 0;
   if g(theta, example) > 0.5
       label = 1;
   end
   if label ~= labels(i)
       errors = errors + 1;
   end
end

error = errors/featureSize(1);

end

