function y = hessian(theta, xs)
s = size(theta);
numEx = size(xs,1);
hessian = zeros(s(1));
for j = 1:s
    for k = 1:s
        sum = 0;
        for i = 1:numEx
            x_i = xs(i,:)';
            add = g(theta, x_i)*(1-g(theta, x_i))*x_i(j)*x_i(k);
            sum = sum + add;
        end
        hessian(j,k) = -1*sum;
    end
end
y = hessian;
        