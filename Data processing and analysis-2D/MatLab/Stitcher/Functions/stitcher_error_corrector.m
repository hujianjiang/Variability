function stitcher_error_corrector(File, Settings);

temp.experiment = unique(File.files_list.experiment);


for experiment_n = 2:2%size(temp.experiment,1) %loop over experiments
    
    File.files_list_s_exp = File.files_list(strcmp(File.files_list.experiment, temp.experiment(experiment_n)),:);
    temp.wells = unique(File.files_list_s_exp.well);
    
    % cd ([char(unique(File.files_list.folder_processed_Person)), char(temp.experiment(experiment_n)), '2 - stitching_results'])
    cd([char(unique( File.files_list_s_exp.folder_processed_experiment)) File.folder_results_errors]);
    list_files = dir;
    
    
    for k= 3: size(list_files,1)
        list_files(k).type = list_files(k).name(1:5)
    end
    list_files = struct2table(list_files);
    list_files = list_files(3:end,:);
    
    results_all = table();
    for loresults = 1:size(list_files(strcmp(list_files.type,'resul'),:),1)
        
        list_files_t = list_files(strcmp(list_files.type,'resul'),:);
        load(char(list_files_t.name(loresults)));
        results.output.all.well_tp(:,1) = list_files_t.name(loresults);
        results_all = [results_all ; results.output.all];
    end
    
    
    %1) create standard results variable
    clear results_st
    
    results_st.output.cols = results.output.cols(:,1:11);
    results_st.output.rows = results.output.rows(:,1:11);
    
    results_st.output.cols.tformEstimate_col(:,1) = mean(results_all.tformEstimate_col(results_all.RsquaredAdjusted>Settings.min_RsquaredAdjusted & strcmp(results_all.direction, 'cols')));
    results_st.output.cols.tformEstimate_row(:,1) = mean(results_all.tformEstimate_row(results_all.RsquaredAdjusted>Settings.min_RsquaredAdjusted & strcmp(results_all.direction, 'cols')));
    
    results_st.output.rows.tformEstimate_col(:,1) = mean(results_all.tformEstimate_col(results_all.RsquaredAdjusted>Settings.min_RsquaredAdjusted & strcmp(results_all.direction, 'rows')));
    results_st.output.rows.tformEstimate_row(:,1) = mean(results_all.tformEstimate_row(results_all.RsquaredAdjusted>Settings.min_RsquaredAdjusted & strcmp(results_all.direction, 'rows')));
    
    %     results_st.output.cols.tformEstimate_col_cor(:,1) = mean(results_all.tformEstimate_col(results_all.RsquaredAdjusted>Settings.min_RsquaredAdjusted & strcmp(results_all.direction, 'cols')));
    %     results_st.output.cols.tformEstimate_row_cor(:,1) = mean(results_all.tformEstimate_row(results_all.RsquaredAdjusted>Settings.min_RsquaredAdjusted & strcmp(results_all.direction, 'cols')));
    %
    %     results_st.output.rows.tformEstimate_col_cor(:,1) = mean(results_all.tformEstimate_col(results_all.RsquaredAdjusted>Settings.min_RsquaredAdjusted & strcmp(results_all.direction, 'rows')));
    %     results_st.output.rows.tformEstimate_row_cor(:,1) = mean(results_all.tformEstimate_row(results_all.RsquaredAdjusted>Settings.min_RsquaredAdjusted & strcmp(results_all.direction, 'rows')));
    
    
    
    %2) loop over error files, get error, experiment, well, timepoint,
    
    error_all = table();
    for loerrors= 1:size(list_files(strcmp(list_files.type,'error'),:),1)
        
        list_files_t = list_files(strcmp(list_files.type,'error'),:);
        error_temp = readtable(char(list_files_t.name(loerrors)));
        
        error_temp.experiment_n = experiment_n;
        error_temp.well_name = extractBefore(extractAfter(char(list_files_t.name(loerrors)),'error_well'),'_time');
        error_temp.well_n = find(contains(temp.wells,error_temp.well_name));
        error_all = [error_all ; error_temp];
        
    end
    
    
    %3) loop over well_n of error files and load images
    
    File.files_list_s_exp = File.files_list(strcmp(File.files_list.experiment, temp.experiment(experiment_n)),:);
    temp.wells = unique(File.files_list_s_exp.well);
    
    
    for well_n = 1:size(temp.wells,1) %loop over wells
        try
            
            error_list = error_all(error_all.well_n == well_n,:);
            
            if size(error_list,1)>0
                
                File.files_list_s = File.files_list_s_exp (strcmp(File.files_list_s_exp.well,temp.wells(well_n)),:);
                
                for k=1:File.nrows*File.ncols,
                    File.im_all{k,1} = bfopen(File.files_list_s.file_path{k});
                end
                
                temp.timepoints = unique(error_list.timepoint_counter);
                
                % loop and assign timepoint of error file
                
                for timepoint_k2 = 1 : size(temp.timepoints,1)
                    
                    timepoint = temp.timepoints(timepoint_k2);
                    
                    temp.lamda_cell = unique(File.files_list_s.Lambda_cell)+timepoint-1;
                    temp.lamda_nuc = unique(File.files_list_s.Lambda_nuc)+timepoint-1;
                    for k=1:25,
                        im{k,1} = File.im_all{k,1}{1,1}{temp.lamda_cell ,1};  % File.im_all{pos,1}{1,1}{timeandchannel,1}. Load here cell images
                        im{k,2} = File.im_all{k,1}{1,1}{temp.lamda_nuc,1};  % File.im_all{pos,1}{1,1}{timeandchannel,1}. Load here nucleus images
                    end
                    
                    %  then apply results to stitcher functions
                    
                    results_st = post_process_results_stitching(File,Settings,results_st); % need some work here on defining rows and cols used
                    
                    im = correct_illumination (File, Settings, im, timepoint);
                    
                    im_res = stitch_mosaic(File,Settings,results_st,im); %stitch mosaics and save images
                    im_res =  crop_images (File, results, im_res);
                    im_res = resize_images(Settings,im_res);
                    save_images_tif  (File, well_n, timepoint, im_res, results);
                    
                    export_results (File, timepoint, im_res, results);
                    
                end
            end
            
        end
    end
end


