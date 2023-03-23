function [im_c] =  correct_illumination (File, Settings, im, timepoint)
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here


        
for k=1:size(im,1)
    I = imresize(im{k,1},Settings.resize_factor);
    background(:,:,k) = imopen(I,strel('disk',round(Settings.illumination_correction_diam*Settings.resize_factor)));
end

im_b1 = (imgaussfilt(mean(background,3),Settings.illumination_correction_gaus_s));
im_b1 = imresize(im_b1, [size(im{1,1},1), size(im{1,1},2)]);

for k=1:size(im,1),
    im_c{k,1} = im{k,1}-uint16(im_b1);
end



for k=1:size(im,1)
    I = imresize(im{k,2},Settings.resize_factor);
    background(:,:,k) = imopen(I,strel('disk',round(Settings.illumination_correction_diam*Settings.resize_factor)));
end

im_b2 = (imgaussfilt(mean(background,3),Settings.illumination_correction_gaus_s));
im_b2 = imresize(im_b2, [size(im{1,2},1), size(im{1,2},2)]);

for k=1:size(im,1),
    im_c{k,2} = im{k,2}-uint16(im_b2);
end

dir_name = horzcat(char( unique(File.files_list_s.folder_processed_experiment)), '0 - stitching_results/');
if ~exist(dir_name)
    mkdir(dir_name)
end

filename1 = horzcat( char( unique(File.files_list_s.folder_processed_experiment)), '0 - stitching_results/im_time',num2str(timepoint),'_background1.tif');
filename2 = horzcat( char( unique(File.files_list_s.folder_processed_experiment)), '0 - stitching_results/im_time',num2str(timepoint),'_background2.tif');
imwrite(im_b1, filename1,'tif');
imwrite(im_b2, filename2,'tif');

end


