function save_images (File, timepoint, im_res, results)
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here

filename_im1 = horzcat( char( unique(File.files_list_s.folder_processed_experiment)), 'im1_well', char(unique(File.files_list_s.well)),'_time', num2str((timepoint+unique(File.files_list_s.Lambda_n)-1)/unique(File.files_list_s.Lambda_n),'%02.f'),'.tif');
filename_im2 = horzcat( char( unique(File.files_list_s.folder_processed_experiment)), 'im2_well', char(unique(File.files_list_s.well)),'_time', num2str((timepoint+unique(File.files_list_s.Lambda_n)-1)/unique(File.files_list_s.Lambda_n),'%02.f'),'.tif');
filename_results = horzcat( char( unique(File.files_list_s.folder_processed_experiment)), 'results_well', char(unique(File.files_list_s.well)),'_time', num2str((timepoint+unique(File.files_list_s.Lambda_n)-1)/unique(File.files_list_s.Lambda_n),'%02.f'),'.m');

imwrite(uint16(im_res{1,1}), filename_im1,'tif');
imwrite(uint16(im_res{1,2}), filename_im2,'tif');
save(filename_results,'results') ;


end
