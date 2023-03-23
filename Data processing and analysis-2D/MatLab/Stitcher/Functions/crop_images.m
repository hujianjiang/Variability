function [im_res2] =  crop_images (File, results, im_res)
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here
y1 = round(max([100; results.output.all.y_pos_real_init(results.output.all.rownumber(1:24) == 1)]));
y2 = round(min(results.output.all.y_pos_real_final(results.output.all.rownumber(1:24) == File.nrows)));
x1 = round(max([100; results.output.all.x_pos_real_init(results.output.all.colnumber(1:24) == 1)]));
x2 = round(min(results.output.all.x_pos_real_final(results.output.all.colnumber(1:24) ==  File.ncols)));


im_res2{1,1} = im_res{1,1}(y1:y2,x1:x2);
im_res2{1,2} = im_res{1,2}(y1:y2,x1:x2);

im_res2{1,1} = im_res2{1,1}(1:File.image_final_size,1:File.image_final_size);
im_res2{1,2} = im_res2{1,2}(1:File.image_final_size,1:File.image_final_size);


end




