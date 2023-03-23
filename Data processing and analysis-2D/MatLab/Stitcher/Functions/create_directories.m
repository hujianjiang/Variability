function create_directories (File)

mkdir(horzcat(char( unique(File.files_list_s_exp.folder_processed_experiment)), File.folder_results_stitched));
mkdir(horzcat(char( unique(File.files_list_s_exp.folder_processed_experiment)), File.folder_results_errors ));


temp.folders = strcat(reshape(repmat(unique(File.files_list_s_exp.folder_processed_experiment),12,1),[],1),... % folder
reshape(repmat({[File.folder_results_stitched, 'xy']},12,1),[],1) , ... %xy
    num2str(reshape(repmat(1:6,2,1),[],1)), ... %xy pos
    reshape(repmat({'c'},12,1),[],1), ... %channel
    num2str(reshape(repmat(1:2,6,1)',[],1))); %channel number

for k=1:size(temp.folders,1),
    mkdir(char(temp.folders(k)));
end


end
