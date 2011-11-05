function y = gradient(theta, xs, ys)
s = size(theta);
numEx = size(xs,1);
gradient = zeros(s(1), 1);
for j = 1:s(1)
    sum = 0;
    for i = 1:numEx
        x_i = xs(i,:)';
        add = ys(i) - g(theta, x_i);
        add = add*x_i(j);
        sum = sum + add;
    end
    gradient(j, 1) = sum;
end
y = gradient;