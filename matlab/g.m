function y = g(theta, x)
trans = transpose(theta)*x;
y = 1/(1+exp(-1*trans));