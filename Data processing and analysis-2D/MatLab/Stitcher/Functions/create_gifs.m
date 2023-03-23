function create_gifs(File, Settings);

temp.experiment = unique(File.files_list.experiment);

for experiment_n = 3:3% size(temp.experiment,1) %loop over experiments
    
    File.files_list_s_exp = File.files_list(strcmp(File.files_list.experiment, temp.experiment(experiment_n)),:);
    temp.wells = unique(File.files_list_s_exp.well);
    
    
    for well_n = 1:size(temp.wells,1) %loop over wells
        
        File.files_list_s = File.files_list_s_exp (strcmp(File.files_list_s_exp.well,temp.wells(well_n)),:);
        
        list_files = dir((horzcat( char( unique(File.files_list_s.folder_processed_experiment)), File.folder_results_stitched, 'xy', num2str(well_n),'c1')));
        
        
        for k= 1: size(list_files,1)-2
            
            
            filename_im1 = horzcat( char( unique(File.files_list_s.folder_processed_experiment)), ...
                File.folder_results_stitched, 'xy', num2str(well_n),'c1/C1',...
                num2str(k,'%04.f'),...
                '.tif');
            
            filename_im2 = horzcat( char( unique(File.files_list_s.folder_processed_experiment)), ...
                File.folder_results_stitched, 'xy', num2str(well_n),'c2/C2',...
                num2str(k,'%04.f'),...
                '.tif');
           
        
            im_n = imread(filename_im2);
            im_c = imread(filename_im1);
            
            im_t = uint8(cat(3,im_n.*Settings.im_n_correctionfactor,im_c.*Settings.im_c_correctionfactor,zeros(size(im_n))));
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
            close
            
            filename = horzcat( char( unique(File.files_list_s.folder_processed_experiment)), ...
                '0 - Stitching_results/all_images_well', num2str(well_n),'.gif');
            
            
            if k == 1
                imwrite(imind,cm,filename,'gif', 'Loopcount',inf);
            else
                imwrite(imind,cm,filename,'gif','WriteMode','append', 'DelayTime',0.06);
            end
            
            
        end
        well_n
    end
end
