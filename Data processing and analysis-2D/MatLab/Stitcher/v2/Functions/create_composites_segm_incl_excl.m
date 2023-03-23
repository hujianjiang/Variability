function create_composites_segm_incl_excl (File, Settings)



% load('D:\Data\MULTIMOT\Processed\XSP\Geiger_lab\SW\File')
File.files_sorted.sep = repmat('_', size(File.files_sorted,1),1);
File.files_sorted.cat = cellstr([num2str(File.files_sorted.experiment_n) File.files_sorted.sep num2str(File.files_sorted.well_n) File.files_sorted.sep num2str(File.files_sorted.channel) ]);
temp = unique(File.files_sorted.cat(File.files_sorted.channel == 1)); %only perform for channel = 1; THen we will imread channel 2 as well to generate tif image

for k_set = 1:size(temp,1)
    [num2str(k_set) ' of ' num2str(size(temp,1))]
    temp_files = File.files_sorted(strcmp(File.files_sorted.cat,temp(k_set)),:);
    temp_files.Mosaic_Image_Number = [];
    temp_files.Name = [];
    a = unique(File.files_list.folder_processed_experiment);
    temp_files.folder_processed_experiment(:,1) = {char(a(unique(temp_files.experiment_n)))};
    temp_files = unique(temp_files);
    for timepoint_n = 1:size(temp_files,1)
        timepoint_n
        %         prepare top left
        
        filename1 = [char(temp_files.Path(timepoint_n )) File.folder_final 'xy' num2str(temp_files.well_n(timepoint_n )) 'c' num2str(1) '/',...
            'C', num2str(1), '_T',num2str(temp_files.timepoint(timepoint_n ), '%02.f'), '.tif'];
        filename2 = [char(temp_files.Path(timepoint_n )) File.folder_final 'xy' num2str(temp_files.well_n(timepoint_n )) 'c' num2str(2) '/',...
            'C',num2str(2), '_T',num2str(temp_files.timepoint(timepoint_n ), '%02.f'), '.tif'];
        
        im_n = imread(filename1);
        im_c = imread(filename2);
        
        im_n = im_n.* max(uint16(1),max(max(im_c))/max(max(im_n)));
        im_c = im_c  .* max(uint16(1),max(max(im_n))/max(max(im_c)));
        %             im_t = uint8(cat(3,im_n.*Settings.im_n_correctionfactor,im_c.*Settings.im_c_correctionfactor,zeros(size(im_n))));
        im_tl = uint8(cat(3,im_n,im_c,zeros(size(im_n))));
        
        
        %         prepare top right
        
        filename_im3 = [char(temp_files.folder_processed_experiment(timepoint_n)), ...
            '2 - CellProfiler results/Nuclei/Nuc0', num2str(size(temp_files,1)*(temp_files.well_n(timepoint_n)-1)+temp_files.timepoint(timepoint_n),'%03.f'),...
            '.jpeg'];
        imb = imread(filename_im3);
        im_tr = imadd(im_tl.*0.3, imb);
        
        %          prepare bottom left (exclusions)
        
        %         load image
        filename_im4 = [char(temp_files.folder_processed_experiment(timepoint_n)), ...
            '2 - CellProfiler results/CellMasks/CellMasks_0', num2str(size(temp_files,1)*(temp_files.well_n(timepoint_n)-1)+temp_files.timepoint(timepoint_n),'%03.f'),...
            '.tif'];
        im_cm = imread(filename_im4);
        
        %         load r dat_old file
        filename_dat = [char(temp_files.folder_processed_experiment(timepoint_n)), ...
            '3 - Postprocessing results/xy',num2str(temp_files.well_n(timepoint_n)) ,'/dat_old.csv'];
        obj_table = readtable(filename_dat);
        obj_table.excluded(obj_table.AreaShape_Area_cells>3850) = 4;
        obj_table.excluded(obj_table.AreaShape_Area_cells<100) = 5;
        
        obj_table = obj_table(obj_table.ImageNumber == timepoint_n,:);
        im_cm2 = im_cm;
        for k = 1:size(obj_table,1)
            im_cm2(im_cm == im_cm(obj_table.AreaShape_Center_Y(k), obj_table.AreaShape_Center_X(k))) = obj_table.excluded(k);
        end
        
        
        %          prepare bottom right (retained)
        im_cm3 = im_cm;
        
        obj_table.included(:,1) = 1;
        obj_table.included(obj_table.excluded ~= 0 ) = 0;
        
        for k = 1:size(obj_table,1)
            im_cm3(im_cm3 == im_cm(obj_table.AreaShape_Center_Y(k), obj_table.AreaShape_Center_X(k))) = obj_table.included(k);
        end
        
        
        im_cm_bw = im_cm;
        im_cm_bw(find(im_cm_bw)) = 1;
        
        %         plot
        figh = figure('pos',[10 10 1400 1000], 'visible','off');
        %         figh = figure('pos',[10 10 1400 1000]);
        
        subplot = @(m,n,p) subtightplot (m, n, p, [0.05 0.01], [0.1 0.02], [0.1 0.01]);
        
        
        
        subplot(2,3,1), imagesc(im_tl), axis image, axis off, title('original')
        subplot(2,3,2), imagesc(im_cm_bw), axis image, axis off, title('segmented')
        subplot(2,3,3), imagesc(im_tr), axis image, axis off, title('overlap')
        subplot(2,3,4),
        %         imagesc(im_cm2), axis image, axis off, title('excluded'),
        
        N = 7;
        cmap = [0 0 0; 0 1 0;1 0 1;0 0 1; 1 1 0;0 1 1;1 0.2 0.2];
        
        im_cm2_t = im_cm2;
        im_cm2_t(find(boundarymask(im_cm))) = 6;
        
        imagesc(im_cm2_t),axis image, axis off, title('excluded')
        colormap(cmap)
        
        subplot(2,3,5)
        axis off, axis image
        L = line(ones(N),ones(N), 'LineWidth',2);               % generate line
        set(L,{'color'},mat2cell(cmap,ones(1,N),3));            % set the colors according to cmap
        legend('-','<1h', 'multiple','merged','Area>3850 px', 'Area <100px', '-', 'Location','west')
        
        
        subplot(2,3,6), imagesc(im_cm3),axis image, axis off, title('included')
        
        set(gcf,'color','w');
        frame = getframe(figh);
        im = frame2im(frame);
        
        
        [i,j] = find(im(:,:,1)~=255);
        
        im = im(min(i)-3:max(i)+3, min(j)-3:max(j)+3,:);
        
        
        [imind,cm] = rgb2ind(im,256);
        
        filename = [char(temp_files.folder_processed_experiment(timepoint_n)), ...
            '3 - Postprocessing results/all_images/Composite_', num2str(temp_files.well_n(timepoint_n)) ,...
            '.gif'];
        
        if timepoint_n == 1
            imwrite(imind,cm,filename,'gif', 'Loopcount',inf);
        else
            imwrite(imind,cm,filename,'gif','WriteMode','append', 'DelayTime',0.06);
        end
        clf %clear figure. Not closing it because this improves speed
        
    end
    close all
end

