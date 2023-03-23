function    stitcher_no_overlap_compute_shifts(File, Settings);

temp.experiment = unique(File.files_list.experiment);

for experiment_n = 1:size(temp.experiment,1)
    
    File.files_list_s_exp = File.files_list(strcmp(File.files_list.experiment, temp.experiment(experiment_n)),:);
    
    res_all_r = table();
    res_all_c = table();
    
    files = struct2table(dir(horzcat(char(unique(File.files_list_s_exp.folder_processed_experiment)),char(File.folder_results_errors))));
    files.substr(:,1) = ( cellfun(@(str) str(1:1), files.name, 'UniformOutput', false));
    files.select = strcmp(files.substr, 'r');
    ind = find(files.select);
    
    for k=1:length(ind)
        load(horzcat(char(files.folder(ind(k))), '\', char(files.name(ind(k)))));
        
        res_all_r = vertcat(res_all_r ,results.output.rows);
        res_all_c = vertcat(res_all_c ,results.output.cols);
        
    end
    
    names = {'tformEstimate_row'; 'tformEstimate_col'};
    vals = [median(res_all_r.tformEstimate_row(res_all_r.RsquaredAdjusted>Settings.min_RsquaredAdjusted));...
            median(res_all_c.tformEstimate_col(res_all_c.RsquaredAdjusted>Settings.min_RsquaredAdjusted))];
    
    final_table = table(names, vals);
    writetable(final_table ,horzcat(char(unique(File.files_list_s_exp.folder_processed_experiment)),char(File.folder_results_errors), 'stitch_vals.csv'))
    
end


end
