function correct_illumination_save (File, Settings)
%This functions will generate a background image based on all the input
%files, substract the background to the input files and save them in output
%folder


File.files_sorted.sep = repmat('_', size(File.files_sorted,1),1);
File.files_sorted.cat = cellstr([num2str(File.files_sorted.experiment_n) File.files_sorted.sep num2str(File.files_sorted.well_n) File.files_sorted.sep num2str(File.files_sorted.channel) File.files_sorted.sep num2str(File.files_sorted.timepoint)]);


temp = unique(File.files_sorted.cat);
for k = 1:size(temp,1),
    
    temp_files = File.files_sorted(strcmp(File.files_sorted.cat,temp(k)),:);
    
    
    for k2 = 1:size(temp_files,1),
        I(:,:,k2) = imread([char(temp_files.Path_subfolder(k2)) char(temp_files.Name(k2))]);
        background(:,:,k2) = imopen(I(:,:,k2),strel('disk',round(Settings.illumination_correction_gaus_s)));
    end
    
    im_b1 = (imgaussfilt(mean(background,3),Settings.illumination_correction_gaus_s));
    
    for k2=1:size(temp_files,1),
        I_temp = I(:,:,k2)-uint16(im_b1);
        
%         filename = [char(temp_files.Path(k2)) File.folder_illumination_corrected 'xy' num2str(temp_files.well_n(k2)) 'c' num2str(temp_files.channel(k2)) '/',...
%             'C' num2str(temp_files.channel(k2)), '_T',num2str(temp_files.timepoint(k2)), '_M', num2str(temp_files.Mosaic_Image_Number(k2)), '.tif'];
        
        filename = [char(temp_files.Path_subfolder(k2)) char(temp_files.Name(k2))];
        filename = strrep(filename, File.folder_resized, File.folder_illumination_corrected);
                                
        imwrite(uint16(I_temp), filename,'tif');
        
    end
    
    k
    
end

end

