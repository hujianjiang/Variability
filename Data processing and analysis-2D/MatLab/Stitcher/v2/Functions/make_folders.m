function make_folders(File, Settings);

%generate general folder if it doesn't exist
mkdir(char(unique(File.files_list.folder_Processed_main)));

%generate person folder where File and Settings and results will be saved
mkdir(char(unique(File.files_list.folder_processed_Person)));

%generate experiment folders and subfolders if they don't exist
temp = (unique(File.files_list.folder_processed_experiment));

for k = 1: size((temp),1)
    mkdir(char(temp(k,:)));
    
    % generate results folder
    mkdir([char(temp(k,:)) File.folder_results]);
    
    positions = {'C1', 'C2', 'C3', 'T1', 'T2', 'T3'};
    
    % resized folder and subfolders within results folder
    mkdir([char(temp(k,:)) File.folder_results File.folder_resized]);
    
    
    for sp = 1:6
        mkdir(horzcat(char(temp(k,:)), char(File.folder_results), char(File.folder_resized),  char(positions(sp))));
        for chan = 1:2
            mkdir(horzcat(char(temp(k,:)), char(File.folder_results), char(File.folder_resized),  char(positions(sp)), '/Chan_', num2str(chan)));
        end
    end
    
    
    % illumination corrected folder and subfolders within results folder
    mkdir([char(temp(k,:)) File.folder_results File.folder_illumination_corrected]);
    
    for sp = 1:6
        mkdir(horzcat(char(temp(k,:)), char(File.folder_results), char(File.folder_illumination_corrected),  char(positions(sp))));
        for chan = 1:2
            mkdir(horzcat(char(temp(k,:)), char(File.folder_results), char(File.folder_illumination_corrected),  char(positions(sp)), '/Chan_', num2str(chan)));
        end
    end
    
    
    % final images folder and subfolders within results folder
    mkdir([char(temp(k,:)) File.folder_final]);
    
    for sp = 1:6
        mkdir(horzcat(char(temp(k,:)), char(File.folder_final),  char(positions(sp))));
        for chan = 1:2
            mkdir(horzcat(char(temp(k,:)), char(File.folder_final),  char(positions(sp)), '/Chan_', num2str(chan)));
        end
    end
    
    
    % control images folder and subfolders within results folder if
    % Settings.illumination_correction_substract_mode_val  == 1;
    mkdir([char(temp(k,:)) File.folder_results File.folder_final_control]);
    
    for sp = 1:6
        mkdir(horzcat(char(temp(k,:)), char(File.folder_results), char(File.folder_final_control),  char(positions(sp))));
        for chan = 1:2
            mkdir(horzcat(char(temp(k,:)), char(File.folder_results), char(File.folder_final_control),  char(positions(sp)), '/Chan_', num2str(chan)));
        end
    end
    
    % gifs
    mkdir([char(temp(k,:)) File.folder_results File.folder_gifs ]);
    
end

end
