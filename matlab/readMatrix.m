function [ matrix ] = readMatrix( filename )
%Reads a matrix in from a file

fid = fopen(filename);

%number of rows and columns
dimensions = fscanf(fid, '%d %d\n', 2);

matrix = zeros(dimensions(1), dimensions(2));

for i = 1:dimensions(1)
    line = fgetl(fid);
    nums = sscanf(line, '%d');
    matrix(i,:) = nums;
end

fclose(fid);

end

