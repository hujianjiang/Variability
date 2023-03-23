function export_results (File, timepoint, im_res, results)
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here


% this exports results variable

filename_results = horzcat( char( unique(File.files_list_s.folder_processed_experiment)), File.folder_results_errors, '/results_well', char(unique(File.files_list_s.well)),'_time', num2str((timepoint+unique(File.files_list_s.Lambda_n)-1)/unique(File.files_list_s.Lambda_n),'%02.f'),'.mat');
save(filename_results,'results') ;


end
