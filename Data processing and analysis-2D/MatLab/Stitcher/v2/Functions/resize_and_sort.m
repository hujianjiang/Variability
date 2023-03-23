function [File] = resize_and_sort (File, Settings);
%this functions will load images from specified files, resize them, and
%save to [File.folder_results File.folder_resized folder] in a logical
%manner. Specifically, it will save each stage position and illumination
%channel in independent folders (xy1c1, xy1c2, xy2c1, ...) and also
%generate an output structure identifying File locations and details

temp.experiment = unique(File.files_list.experiment);
File.files_sorted = table();
temp_table = table();
temp_table2 = table();

for experiment_n = 1:size(temp.experiment,1) %loop over experiments
    
    File.files_list_s_exp = File.files_list(strcmp(File.files_list.experiment, temp.experiment(experiment_n)),:);
    temp.wells = unique(File.files_list_s_exp.well);
    
    
    for well_n = 1:size(temp.wells,1) %loop over wells
        well_n
        File.files_list_s = File.files_list_s_exp (strcmp(File.files_list_s_exp.well,temp.wells(well_n)),:);
        
          for k=1:File.nrows*File.ncols,
              temp.im_all{k,1} = bfopen(File.files_list_s.file_path{k});
              ['Exp_' num2str(experiment_n) ' well_' num2str(well_n) ' Mosaic Image Number_' num2str(k)]
              
          end
        
        for timepoint_n=1:unique(File.files_list_s.Lambda_n):size(temp.im_all{1,1}{1,1},1), %loop over timepoints %219 op %146 ig %146 op
             [num2str(timepoint_n) '_' num2str(size(temp.im_all{1,1}{1,1},1))]
            % load files timepoint analyzed
            temp.lamda_cell = unique(File.files_list_s.Lambda_cell)+timepoint_n-1;
            temp.lamda_nuc = unique(File.files_list_s.Lambda_nuc)+timepoint_n-1;
             for k=1:25,
                 im{k,1} = temp.im_all{k,1}{1,1}{temp.lamda_cell ,1};  % File.im_all{pos,1}{1,1}{timeandchannel,1}. Load here cell images
                 im{k,2} = temp.im_all{k,1}{1,1}{temp.lamda_nuc,1};  % File.im_all{pos,1}{1,1}{timeandchannel,1}. Load here nucleus images
             end
%             
            for Mosaic_Image_Number=1:File.nrows*File.ncols,
                
                 im1 = imresize(im{Mosaic_Image_Number,1}, Settings.resize_factor);
                 im2 = imresize(im{Mosaic_Image_Number,2}, Settings.resize_factor);
                
                timepoint = ((timepoint_n+unique(File.files_list_s.Lambda_n)-1)/unique(File.files_list_s.Lambda_n));
                
                channel = 1;
                Path = {horzcat( char( unique(File.files_list_s.folder_processed_experiment)), File.folder_results)};
                
                Path_subfolder = {horzcat(char(Path), (File.folder_resized), char(unique(File.files_list_s.Condition)), '/Chan_', num2str(channel),'/')};
                
                %                 Path_subfolder = {horzcat(char(Path), (File.folder_resized), 'xy', num2str(well_n),'c1/')};
                Name = {(horzcat('C1_T',...
                    num2str(timepoint),...
                    '_M',num2str(Mosaic_Image_Number),... %Mosaic image number
                    '.tif'))};
                
                Condition = unique(File.files_list_s.Condition);
                experiment = unique(File.files_list_s.experiment);
                well = unique(File.files_list_s.well);
                overlap = unique(File.files_list_s.Overlap);
                Final_folder = unique(File.files_list_s.folder_processed_experiment);
                temp_table = [temp_table ; table(experiment_n, experiment, well_n, well, overlap, Condition,Final_folder, channel, timepoint, Mosaic_Image_Number, Path, Path_subfolder, Name )];
                 imwrite(uint16(im1), [char(Path_subfolder) char(Name)],'tif');
                
                
                channel = 2;
                %                 Path_subfolder = {horzcat(char(Path), (File.folder_resized), 'xy', num2str(well_n),'c2/')};
                Path_subfolder = {horzcat(char(Path), (File.folder_resized), char(unique(File.files_list_s.Condition)), '/Chan_', num2str(channel),'/')};
                
                Name = {(horzcat('C2_T',...
                    num2str(timepoint),...
                    '_M',num2str(Mosaic_Image_Number),... %Mosaic image number
                    '.tif'))};
                
                temp_table = [temp_table ; table(experiment_n, experiment, well_n, well, overlap, Condition, Final_folder,channel, timepoint, Mosaic_Image_Number, Path, Path_subfolder, Name )];
                 imwrite(uint16(im2), [char(Path_subfolder) char(Name)],'tif');
                
                
            end
            
            temp_table2 = [temp_table2; temp_table];
            temp_table = table();
        end
        File.files_sorted = [File.files_sorted; temp_table2];
        temp_table2 = table();
    end
end

save([char(File.files_list.folder_processed_Person(1)) 'File'], 'File')


end

