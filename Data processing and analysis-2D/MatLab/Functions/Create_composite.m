clear all

File.folder_root = 'D:\Data\MULTIMOT\Processed\XSP\L1\';
File.experiments = dir(File.folder_root);
File.experiments = ({File.experiments.name})';
File.experiments = File.experiments(3:11); %L3? :File.experiments([4:13]); %L2: File.experiments([4,5,6,7,9,10,11,12,14,15,16]);%L1: File.experiments(3:11);


File.nfiles = 73;
File.folder_original = '1 - Images\';
File.folder_Segmented = '2 - CellProfiler results\Nuclei\';
File.folder_Masks = '2 - CellProfiler results\CellMasks\';
File.folder_results = '4 - Postprocessing results\';
File.Destination = '4 - Postprocessing results\all_images\';


File.stagepos = {'C1','C2', 'C3', 'T1', 'T2', 'T3' };

File.final_name = ['Composite_'];
File.final_name_overlay = ['Outlines_'];

Settings.applygauussfilt = 1;
Settings.maxArea = 4070;
Settings.minNucArea = 100;

%% all composite
for folder = 1:size(File.experiments,1)%1,
    
    folder_base = [char(File.folder_root) char(File.experiments(folder)) '\']
    mkdir ( [folder_base File.Destination] );
    
    for stagepos = 1:6
        
        for timepoint_n = 1:1:File.nfiles
            ['stagepos_' char(File.stagepos(stagepos)) ' timepoint_' num2str(timepoint_n)]
            
            %         prepare top left
            
            filename1 = [folder_base File.folder_original char(File.stagepos(stagepos)) '\Chan_1\C1_T' num2str(timepoint_n,'%02.f'), '.tif'];
            filename2 = [folder_base File.folder_original char(File.stagepos(stagepos)) '\Chan_2\C2_T' num2str(timepoint_n,'%02.f'), '.tif'];
            %
            
            %             filename1 = [folder_base File.folder_original 'xy' num2str(stagepos) 'c1\C1_00' num2str(timepoint_n-1,'%02.f'), '.tif'];
            %             filename2 = [folder_base File.folder_original 'xy' num2str(stagepos) 'c2\C2_00' num2str(timepoint_n-1,'%02.f'), '.tif'];
            
            im_n = imread(filename1);
            im_c = imread(filename2);
            
            if Settings.applygauussfilt,
                         im_n = imgaussfilt(im_n,3);
                         im_c = imgaussfilt(im_c,3);
            end
            
            
            im_n = im_n - median(im_n);
            im_c = im_c - median(im_c);
            
            
            %         im_n = im_n.* max(uint16(1),max(max(im_c))/max(max(im_n)));
            %         im_c = im_c  .* max(uint16(1),max(max(im_n))/max(max(im_c)));
            %             im_t = uint8(cat(3,im_n.*Settings.im_n_correctionfactor,im_c.*Settings.im_c_correctionfactor,zeros(size(im_n))));
            im_tl = uint8(cat(3,im_n,im_c,zeros(size(im_n))));
            
            
            %         prepare top right
            
            filename_im3 = [folder_base File.folder_Segmented 'Nuc0' num2str(timepoint_n+ File.nfiles*(stagepos-1) ,'%03.f'),  '.jpeg'];
            
            imb = imread(filename_im3);
            im_tr = imadd(im_tl.*0.3, imb);
            
            %          prepare bottom left (exclusions)
            
            %         load image
            filename_im4 = [folder_base File.folder_Masks 'CellMasks_0' num2str(timepoint_n+ File.nfiles*(stagepos-1) ,'%03.f'),  '.tif'];
            
            im_cm = imread(filename_im4);
            
            %         load r dat_old file
            filename_dat = [folder_base File.folder_results char(File.stagepos(stagepos)) '\dat_old.csv'];

            obj_table = readtable(filename_dat);
            obj_table.excluded(obj_table.AreaShape_Area_cells>Settings.maxArea) = 4;
            obj_table.excluded(obj_table.AreaShape_Area<Settings.minNucArea) = 5;
            
            obj_table = obj_table(obj_table.ImageNumber == timepoint_n,:);
            im_cm2 = im_cm;
            for k = 1:size(obj_table,1)
                im_cm2(im_cm == im_cm(obj_table.AreaShape_Center_Y(k), obj_table.AreaShape_Center_X(k))) = obj_table.excluded(k);
            end
            
            
            %          prepare bottom right (retained)
            im_cm3 = im_cm;
            
            obj_table.included(:,1) = 1;
            obj_table.included(obj_table.excluded ~= 0 ) = 0;
%             figure
            for k = 1:size(obj_table,1)
                im_cm3(im_cm3 == im_cm(obj_table.AreaShape_Center_Y(k), obj_table.AreaShape_Center_X(k))) = obj_table.included(k)*1000;
%                 imagesc(im_cm3), text(obj_table.AreaShape_Center_X(k), obj_table.AreaShape_Center_Y(k), num2str(k)), title([k obj_table.AreaShape_Center_X(k)])
%                   pause(0.5)
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
            
            
            filename = [folder_base File.Destination File.final_name 'pos' char(File.stagepos(stagepos)) '.gif'];

            if timepoint_n == 1
                imwrite(imind,cm,filename,'gif', 'Loopcount',inf);
            else
                imwrite(imind,cm,filename,'gif','WriteMode','append', 'DelayTime',0.06);
            end
            clf %clear figure. Not closing it because this improves speed
            
        end
        close all
    end
end


%% only outlines overlay
for folder = 1:size(File.experiments,1)%1,
    
    folder_base = [char(File.folder_root) char(File.experiments(folder)) '\']
    mkdir ( [folder_base File.Destination] );
    
    for stagepos = 1:6
        
        for timepoint_n = 1:1:File.nfiles
            ['stagepos_' char(File.stagepos(stagepos)) ' timepoint_' num2str(timepoint_n)]
            
            filename1 = [folder_base File.folder_original char(File.stagepos(stagepos)) '\Chan_1\C1_T' num2str(timepoint_n,'%02.f'), '.tif'];
            filename2 = [folder_base File.folder_original char(File.stagepos(stagepos)) '\Chan_2\C2_T' num2str(timepoint_n,'%02.f'), '.tif'];
            
            im_n = imread(filename1);
            im_c = imread(filename2);
            
            if Settings.applygauussfilt,
                im_n = imgaussfilt(im_n,3);
                im_c = imgaussfilt(im_c,3);
            end
                        
            im_n = im_n - median(im_n);
            im_c = im_c - median(im_c);
                        
            im_tl = uint8(cat(3,im_n,im_c,zeros(size(im_n))));
                                   
            filename_im3 = [folder_base File.folder_Segmented 'Nuc0' num2str(timepoint_n+ File.nfiles*(stagepos-1) ,'%03.f'),  '.jpeg'];
            
            imb = imread(filename_im3);
            im_tr = imadd(im_tl.*0.3, imb);
            
            figh = figure('pos',[10 10 3000 1800], 'visible','off');
            imagesc(im_tr), axis image, axis off, title('overlap')
            
            set(gcf,'color','w');
            frame = getframe(figh);
            im = frame2im(frame);
                        
            [i,j] = find(im(:,:,1)~=255);
            im = im(min(i)-3:max(i)+3, min(j)-3:max(j)+3,:);
            
            [imind,cm] = rgb2ind(im,256);
                        
            filename = [folder_base File.Destination File.final_name_overlay 'pos' char(File.stagepos(stagepos)) '.gif'];
            
            if timepoint_n == 1
                imwrite(imind,cm,filename,'gif', 'Loopcount',inf);
            else
                imwrite(imind,cm,filename,'gif','WriteMode','append', 'DelayTime',0.06);
            end
%             clf %clear figure. Not closing it because this improves speed
            close;
        end
        close all
    end
end
