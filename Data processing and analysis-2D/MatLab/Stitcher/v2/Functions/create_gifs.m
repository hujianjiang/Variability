function create_gifs( File, Settings)
%UNTITLED10 Summary of this function goes here
%   Detailed explanation goes here


File.files_sorted.sep = repmat('_', size(File.files_sorted,1),1);
File.files_sorted.cat = cellstr([num2str(File.files_sorted.experiment_n) File.files_sorted.sep num2str(File.files_sorted.well_n) File.files_sorted.sep num2str(File.files_sorted.channel) ]);

temp = unique(File.files_sorted.cat(File.files_sorted.channel == 1)); %only perform for channel = 1; THen we will imread channel 2 as well to generate tif image


for k_set = 1:size(temp,1)
    
    [num2str(k_set) ' of ' num2str(size(temp,1))]
    
    temp_files = File.files_sorted(strcmp(File.files_sorted.cat,temp(k_set)),:);
    temp_files.Mosaic_Image_Number = [];
    temp_files.Name = [];
    
    temp_files = unique(temp_files);
    
    for timepoint_n = 1:size(temp_files,1)
        %
        %
        %         filename1 = [char(temp_files.Path(timepoint_n )) File.folder_final 'xy' num2str(temp_files.well_n(timepoint_n )) 'c' num2str(1) '/',...
        %             'C', num2str(1), '_T',num2str(temp_files.timepoint(timepoint_n )), '.tif'];
        %         filename2 = [char(temp_files.Path(timepoint_n )) File.folder_final 'xy' num2str(temp_files.well_n(timepoint_n )) 'c' num2str(2) '/',...
        %             'C',num2str(2), '_T',num2str(temp_files.timepoint(timepoint_n )), '.tif'];
        %
        filename1 = [char(temp_files.Final_folder(timepoint_n)) File.folder_final char(temp_files.Condition(timepoint_n)) '/Chan_' num2str(temp_files.channel(timepoint_n)), ...
            '/' 'C' num2str(temp_files.channel(1)), '_T',num2str(temp_files.timepoint(timepoint_n),'%02.f'), '.tif'];
        filename2 = [char(temp_files.Final_folder(timepoint_n)) File.folder_final char(temp_files.Condition(timepoint_n)) '/Chan_' num2str(temp_files.channel(timepoint_n)), ...
            '/' 'C' num2str(temp_files.channel(2)), '_T',num2str(temp_files.timepoint(timepoint_n),'%02.f'), '.tif'];
        
        im_n = imread(filename1);
        im_c = imread(filename2);
        
        
        im_n = im_n.* max(uint16(1),max(max(im_c))/max(max(im_n)));
        im_c = im_c  .* max(uint16(1),max(max(im_n))/max(max(im_c)));
        
        
        %             im_t = uint8(cat(3,im_n.*Settings.im_n_correctionfactor,im_c.*Settings.im_c_correctionfactor,zeros(size(im_n))));
        im_t = uint8(cat(3,im_n,im_c,zeros(size(im_n))));
        %             im_t = imadd(im_t, imb); %add two rgb images
        
        
        figh = figure('pos',[10 10 1800 1000], 'visible','off');
        imagesc(im_t), axis image, axis off,
        set(gcf,'color','w');
        frame = getframe(figh);
        
        
        im = frame2im(frame);
        
        y1 = min(find(im(:,round(size(im,2)/2),1) ~= 255));
        y2 = size(im,1)-min(find(flipud(im(:,round(size(im,2)/2),1)) ~= 255));
        x1 = min(find(im(round(size(im,1)/2),:,1) ~= 255));
        x2 = size(im,2)-min(find(fliplr(im(round(size(im,1)/2),:,1)) ~= 255));
        
        im = im(y1:y2,x1:x2,:);
        
        
        [imind,cm] = rgb2ind(im,256);
        
        
        
        
%         filename = [char(temp_files.Path(timepoint_n )) File.folder_gifs 'xy' num2str(temp_files.well_n(timepoint_n )) '.gif'];
        filename = horzcat(char( unique(File.files_list_s.folder_processed_experiment)), char(File.folder_results),...
            '/', char(File.folder_gifs), char(unique(File.files_list_s.Condition)), '.gif');
        
        if timepoint_n == 1
            imwrite(imind,cm,filename,'gif', 'Loopcount',inf);
        else
            imwrite(imind,cm,filename,'gif','WriteMode','append', 'DelayTime',0.06);
        end
        clf %clear figure. Not closing it because this improves speed
        
        
    end
    close all;
end
end





