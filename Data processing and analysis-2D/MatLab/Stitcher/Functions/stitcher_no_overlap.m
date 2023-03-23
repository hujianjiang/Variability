function    stitcher_no_overlap(File, Settings);



%% run stitcher over experiments, wells, timepoints

temp.experiment = unique(File.files_list.experiment);

% get average shift values
for experiment_n = 1:size(temp.experiment,1) %loop over experiments
    
    File.files_list_s_exp = File.files_list(strcmp(File.files_list.experiment, temp.experiment(experiment_n)),:);
    temp.wells = unique(File.files_list_s_exp.well);
    
    create_directories(File);
    
    for well_n = 1:size(temp.wells,1) %loop over wells
        File.files_list_s = File.files_list_s_exp (strcmp(File.files_list_s_exp.well,temp.wells(well_n)),:);
        
        for k=1:File.nrows*File.ncols,
            File.im_all{k,1} = bfopen(File.files_list_s.file_path{k});
        end
        
        for timepoint=1:unique(File.files_list_s.Lambda_n):size(File.im_all{1,1}{1,1},1), %loop over timepoints
            
            % load files timepoint analyzed
            temp.lamda_cell = unique(File.files_list_s.Lambda_cell)+timepoint-1;
            temp.lamda_nuc = unique(File.files_list_s.Lambda_nuc)+timepoint-1;
            for k=1:25,
                im{k,1} = File.im_all{k,1}{1,1}{temp.lamda_cell ,1};  % File.im_all{pos,1}{1,1}{timeandchannel,1}. Load here cell images
                im{k,2} = File.im_all{k,1}{1,1}{temp.lamda_nuc,1};  % File.im_all{pos,1}{1,1}{timeandchannel,1}. Load here nucleus images
            end
            
            
            % Perform calculations
            
            clear results
            
            try
                im = correct_illumination (File, Settings, im, timepoint);
                results = calculate_overlaps_rows_cols(File,Settings,im);
                
                
                save(horzcat( char( unique(File.files_list_s.folder_processed_experiment)),File.folder_results_errors, 'results', char(unique(File.files_list_s.well)),'_time', ...
                    num2str((timepoint+unique(File.files_list_s.Lambda_n)-1)/unique(File.files_list_s.Lambda_n))),...
                    'results');
                
                %                   results = post_process_results_stitching(File,Settings,results); % need some work here on defining rows and cols used
                %                 im_res = stitch_mosaic(File,Settings,results,im); %stitch mosaics and save images
                %                 im_res =  crop_images (File, results, im_res);
                %                 im_res = resize_images(Settings,im_res);
                %                 save_images_tif (File, well_n, timepoint, im_res, results);
                %                 export_results (File, timepoint, im_res, results);
                
            catch ME
                
                
                error_reg.timepoint_counter = timepoint;
                error_reg.timepoint_real = num2str((timepoint+unique(File.files_list_s.Lambda_n)-1)/unique(File.files_list_s.Lambda_n),'%02.f');
                error_reg.well_n = well_n;
                error_reg.experiment_n = experiment_n;
                
                error_reg.message = ME.message;
                
                writetable( struct2table(error_reg), horzcat( char( unique(File.files_list_s.folder_processed_experiment)),File.folder_results_errors, 'error_well', char(unique(File.files_list_s.well)),'_time', num2str((timepoint+unique(File.files_list_s.Lambda_n)-1)/unique(File.files_list_s.Lambda_n),'%02.f'),'.csv'));
                
            end
            
            
            
        end
        
    end
    
end




end


