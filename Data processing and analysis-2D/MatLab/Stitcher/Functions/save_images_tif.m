function save_images_tif (File, well_n, timepoint, im_res, results)
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here





filename_im1 = horzcat( char( unique(File.files_list_s.folder_processed_experiment)), ...
    File.folder_results_stitched, 'xy', num2str(well_n),'c1/C1',... 
    num2str((timepoint+unique(File.files_list_s.Lambda_n)-1)/unique(File.files_list_s.Lambda_n),'%04.f'),...
    '.tif');

filename_im2 = horzcat( char( unique(File.files_list_s.folder_processed_experiment)), ...
    File.folder_results_stitched, 'xy', num2str(well_n),'c2/C2',...
    num2str((timepoint+unique(File.files_list_s.Lambda_n)-1)/unique(File.files_list_s.Lambda_n),'%04.f'),...
    '.tif');

imwrite(uint16(im_res{1,1}), filename_im1,'tif');
imwrite(uint16(im_res{1,2}), filename_im2,'tif');
% save(filename_results,'results') ;


end
