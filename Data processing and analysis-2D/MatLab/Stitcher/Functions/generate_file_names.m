function [all_files] = generate_file_names (File, Settings);


dat = readtable(File.experiment_details);

all_files = table();
for k=1:size(dat.Path,1),
    
    directory = dat(k,:);
    
    % list files and path
    a_temp = dir(fullfile(char(directory.original), '*.dv'))
    a_temp= struct2table(a_temp);
    
    a = cell2table([strcat(a_temp.folder, '/', a_temp.name), a_temp.name]);
    
    a.Properties.VariableNames = {'file_path', 'file_name'};
    
    a.file_name = strrep(a.file_name,'.dv','');
    a.round(:,1) = 1:size(a.file_name,1);
    
    % define folder
    a.folder_processed_experiment(:,1) = directory.folder_processed_experiment;
    
    % define other parameters
    a.lab(:,1) = {Settings.lab};
    a.person(:,1) = directory.Person;
    a.experiment(:,1) = directory.experiment;
    a.Control_ROCKin(:,1) = NaN;
    a.folder_Processed_main(:,1) = directory.folder_Processed_main;
    a.folder_processed_Person(:,1) = directory.folder_processed_Person;
    
    
    a.well = extractBefore(extractAfter(a.file_name,cell2mat(   regexp(a.file_name,'_P_'))-4),4);
    
    a.Lambda_nuc(:,1) = directory.Lambda_nuc;
    a.Lambda_cell(:,1) = directory.Lambda_cell;
    a.Lambda_n(:,1) = directory.Lambda_n;
    
    
    mkdir(char(unique(a.folder_Processed_main)));
    mkdir(char(unique(a.folder_processed_Person)));
    mkdir(char(unique(a.folder_processed_experiment)));
    
    all_files = [all_files;a];
    
    
end


end
