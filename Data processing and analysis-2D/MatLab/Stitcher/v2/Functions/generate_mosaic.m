function generate_mosaic( File, Settings, results)
%UNTITLED10 Summary of this function goes here
%   Detailed explanation goes here


File.files_sorted.sep = repmat('_', size(File.files_sorted,1),1);
File.files_sorted.cat = cellstr([num2str(File.files_sorted.experiment_n) File.files_sorted.sep num2str(File.files_sorted.well_n) File.files_sorted.sep num2str(File.files_sorted.channel) File.files_sorted.sep num2str(File.files_sorted.timepoint)]);

temp = unique(File.files_sorted.cat); %

parfor k_set = 1:size(temp,1)
    [num2str(k_set) ' of ' num2str(size(temp,1))]
    
    temp_files = File.files_sorted(strcmp(File.files_sorted.cat,temp(k_set)),:);
    
    imf = zeros(Settings.init_border*2+round(File.size_im_1 * Settings.resize_factor) * File.ncols, ...
        Settings.init_border*2+round(File.size_im_2 * Settings.resize_factor) * File.nrows);
    
    for k_mosaic = 1:File.nrows*File.ncols
        
        temp_results = results.output.finaltablecat(results.output.finaltablecat.experiment == temp_files.experiment_n(1),:);
        
        im_temp_files_n = find(temp_files.Mosaic_Image_Number == temp_results.Image(k_mosaic));
        
        
        filename1 = [char(temp_files.Path(im_temp_files_n)) File.folder_illumination_corrected char(temp_files.Condition(im_temp_files_n)) '/Chan_' num2str(temp_files.channel(im_temp_files_n)), ...
                '/' 'C' num2str(temp_files.channel(im_temp_files_n)), '_T',num2str(temp_files.timepoint(im_temp_files_n)), '_M', num2str(temp_files.Mosaic_Image_Number(im_temp_files_n)), '.tif'];
        
        
%         filename1 = [char(temp_files.Path(im_temp_files_n )) File.folder_illumination_corrected char(temp_files.Condition(im_1_n)) '/Chan_' num2str(temp_files.channel(im_1_n)), ...
%             '/C' num2str(temp_files.channel(im_temp_files_n )), '_T',num2str(temp_files.timepoint(im_temp_files_n )), '_M', num2str(temp_files.Mosaic_Image_Number(im_temp_files_n )), '.tif'];
        
        im = imread(filename1);
        
        y1 = round(temp_results.y_val_init(k_mosaic));
        y2 = y1+round(File.size_im_1 * Settings.resize_factor)-1;
        x1 = round(temp_results.x_val_init(k_mosaic));
        x2 = x1+(round(File.size_im_1 * Settings.resize_factor))-1;
        
        imf(y1:y2,x1:x2) = im;
    end
    
    %this part crops image
    
    y1 = round(max(temp_results.y_val_init(temp_results.row_n == 1,:)));
    y2 = y1+Settings.image_final_size;
    x1 = round(max(temp_results.x_val_init(temp_results.col_n == 1,:)));
    x2 = x1+Settings.image_final_size;
    
    
    imf = imf(y1:y2,x1:x2);
    
    
    % and this part saves it
    if Settings.illumination_correction_substract_mode_val
        
        imf2 = imf - mode(reshape(imf,[],1)*3);
        
%         filename_save = [char(temp_files.Path(im_temp_files_n )) File.folder_final 'xy' num2str(temp_files.well_n(im_temp_files_n )) 'c' num2str(temp_files.channel(im_temp_files_n )) '/',...
%             'C' num2str(temp_files.channel(im_temp_files_n )), '_T',num2str(temp_files.timepoint(im_temp_files_n )), '.tif'];
        
          
        filename_save = [char(temp_files.Final_folder(im_temp_files_n)) File.folder_final char(temp_files.Condition(im_temp_files_n)) '/Chan_' num2str(temp_files.channel(im_temp_files_n)), ...
                '/' 'C' num2str(temp_files.channel(im_temp_files_n)), '_T',num2str(temp_files.timepoint(im_temp_files_n),'%02.f'), '.tif'];
     
        
        imwrite(uint16(imf2), filename_save ,'tif');
        
        if ismember(k_set,1:Settings.illumination_correction_substract_mode_step_control:size(temp,1))
            
%             filename_save = [char(temp_files.Path(im_temp_files_n )) File.folder_final_control 'xy' num2str(temp_files.well_n(im_temp_files_n )) 'c' num2str(temp_files.channel(im_temp_files_n )) '/',...
%                 'C' num2str(temp_files.channel(im_temp_files_n )), '_T',num2str(temp_files.timepoint(im_temp_files_n )), '.tif'];
            filename_save = [char(temp_files.Path(im_temp_files_n)) File.folder_final_control char(temp_files.Condition(im_temp_files_n)) '/Chan_' num2str(temp_files.channel(im_temp_files_n)), ...
                '/' 'C' num2str(temp_files.channel(im_temp_files_n)), '_T',num2str(temp_files.timepoint(im_temp_files_n),'%02.f'), '.tif'];
          
            imwrite(uint16(imf), filename_save ,'tif');
            
            
        end
    end
end


end




