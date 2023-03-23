function [im_res] = stitch_mosaic( File, Settings, results, im)
%UNTITLED6 Summary of this function goes here
%   Detailed explanation goes here


imf = zeros(Settings.init_border*2+File.size_im_1*File.ncols,Settings.init_border*2+File.size_im_2*File.nrows);
imf_n = zeros(Settings.init_border*2+File.size_im_1*File.ncols,Settings.init_border*2+File.size_im_2*File.nrows);

imf(Settings.init_border:Settings.init_border+File.size_im_1-1,...
    Settings.init_border:Settings.init_border+File.size_im_2-1) = im{1,1};
imf_n(Settings.init_border:Settings.init_border+File.size_im_1-1,...
    Settings.init_border:Settings.init_border+File.size_im_2-1) = im{1,2};


File.files_sorted.sep = repmat('_', size(File.files_sorted,1),1);
File.files_sorted.cat = cellstr([num2str(File.files_sorted.experiment_n) File.files_sorted.sep num2str(File.files_sorted.well_n) File.files_sorted.sep num2str(File.files_sorted.channel) File.files_sorted.sep num2str(File.files_sorted.timepoint)]);

temp = unique(File.files_sorted.cat(File.files_sorted.channel == 1)); %only perform for channel = 1;


for k_set = 1:size(temp,1)
    k_set

        temp_files = File.files_sorted(strcmp(File.files_sorted.cat,temp(k_set)),:);

        
for k = 1:24, %this needs to be properly defined
    
    y1 = round(results.output.all.y_pos_real_init(k));
    y2 = round(results.output.all.y_pos_real_final(k));
    x1 = round(results.output.all.x_pos_real_init(k));
    x2 = round(results.output.all.x_pos_real_final(k));
    imf(y1:y2,x1:x2) = im{results.output.all.im2(k),1};
    imf_n(y1:y2,x1:x2) = im{results.output.all.im2(k),2};
  
end

im_res{1,1} = imf;
im_res{1,2} = imf_n;


end

