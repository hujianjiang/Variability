clearvars

File.folder_root = 'D:\Data\MULTIMOT\Processed\XSP\L1\';
File.experiments = dir(File.folder_root);
File.experiments = {File.experiments.name}';
File.experiments =  File.experiments(3:11); %L3: File.experiments([4:13]); %L2: File.experiments([4,5,6,7,9,10,11,12,14,15,16]);%L1: File.experiments(3:11);

File.nfiles = 73;
File.folder_original = '1 - Images\';
File.folder_Segmented = '2 - CellProfiler results\Nuclei\';
File.folder_Masks = '2 - CellProfiler results\CellMasks\';
File.folder_resultsFile_Cells = '2 - CellProfiler results\MyExpt_Cells.csv';
File.folder_resultsFile_Nuclei = '2 - CellProfiler results\MyExpt_FilteredNuclei.csv';
File.folder_results = '3 - MatLab results\';
File.Destination = '3 - MatLab results\all_images\';

File.size1 = 1871; %L3: 2451; ; L1: 2540, L2: 1871
File.size2 = 1871; %L3: 2451; ; L1: 2540, L2: 1871

File.stagepos = {'C1','C2', 'C3', 'T1', 'T2', 'T3' };

Settings.Stepsize = 1;

T = table();

for folder = 5:size(File.experiments,1)%1,
    
    folder_base = [char(File.folder_root) char(File.experiments(folder)) '\']
    cp_results_c = readtable([char(File.folder_root) char(File.experiments(folder)) '\' File.folder_resultsFile_Cells]);
    cp_results_n = readtable([char(File.folder_root) char(File.experiments(folder)) '\' File.folder_resultsFile_Nuclei]);
    
    for stagepos = 1:6
        
        % this is to combine the two sources of information. TrackObjects_Label is only in nucleus dataset
        cp_results_sp_c = cp_results_c(strcmp(cp_results_c.Metadata_stagepos, File.stagepos(stagepos)),:);
        cp_results_sp_n = cp_results_n(strcmp(cp_results_n.Metadata_stagepos, File.stagepos(stagepos)),:);
        
        A = table(cp_results_sp_n.ObjectNumber, cp_results_sp_n.ImageNumber);
        B = table(cp_results_sp_c.ObjectNumber, cp_results_sp_c.ImageNumber);
        
        [~, idx] = ismember(A,B);
        
        cp_results_sp_c.TrackObjects_Label = cp_results_sp_n.TrackObjects_Label_35(idx);
        
        stagepos
        
        Lab_descriptor = File.folder_root(1,size(File.folder_root,2)-1:size(File.folder_root,2)-1);
        User_descriptor = folder_base(1,size(folder_base,2)-3:size(folder_base,2)-3);
        Experiment_descriptor = folder_base(1,size(folder_base,2)-1:size(folder_base,2)-1);
        
        % define struct here for images
        %im_bu = zeros(File.size1, File.size2, max(unique(cp_results_sp_c.TrackObjects_Label)));
        to_lu = unique(cp_results_sp_c.TrackObjects_Label);
        to_lu = to_lu(~isnan(to_lu));
        
        im_bu = struct();
        for k2 = 1:size(to_lu,1)
            im_bu(to_lu(k2)).im = zeros(File.size1,File.size2);
        end
        
        
        error_reg = struct();
        count = 1;
        
        for timepoint_n = 1+Settings.Stepsize:Settings.Stepsize:File.nfiles - Settings.Stepsize
            
            ['experiment_'  char(File.experiments(folder)) ' stagepos_' char(File.stagepos(stagepos)) ' timepoint_' num2str(timepoint_n)]
            
            filename_im1 = [folder_base File.folder_Masks 'CellMasks_0' num2str(timepoint_n+ File.nfiles*(stagepos-1) - Settings.Stepsize,'%03.f'),  '.tif'];
            filename_im2 = [folder_base File.folder_Masks 'CellMasks_0' num2str(timepoint_n+ File.nfiles*(stagepos-1),                    '%03.f'),  '.tif'];
            filename_im3 = [folder_base File.folder_Masks 'CellMasks_0' num2str(timepoint_n+ File.nfiles*(stagepos-1) + Settings.Stepsize,'%03.f'),  '.tif'];
            
            im_1 = imread(filename_im1);
            im_2 = imread(filename_im2);
            im_3 = imread(filename_im3);
            
            cp_results_sp_t_c = cp_results_sp_c(cp_results_sp_c.ImageNumber == timepoint_n+ File.nfiles*(stagepos-1),:);
            
            cell_list = unique(cp_results_sp_t_c.TrackObjects_Label);
            cell_list = cell_list(~isnan(cell_list));
            
            
            for cell_n = 1:size(cell_list,1)
                
                cp_results_sp_tcell_n_c = cp_results_sp_c(cp_results_sp_c.ImageNumber == (timepoint_n+ File.nfiles*(stagepos-1))& cp_results_sp_c.TrackObjects_Label == cell_list(cell_n),:);
                cp_results_sp_tmi_c =     cp_results_sp_c(cp_results_sp_c.ImageNumber == (timepoint_n+ File.nfiles*(stagepos-1) - Settings.Stepsize) & cp_results_sp_c.TrackObjects_Label == cell_list(cell_n),:);
                cp_results_sp_tpl_c =     cp_results_sp_c(cp_results_sp_c.ImageNumber == (timepoint_n+ File.nfiles*(stagepos-1) + Settings.Stepsize) & cp_results_sp_c.TrackObjects_Label == cell_list(cell_n),:);
                
                % only proceed if cell is in 3 consecutive images
                if all(size(cp_results_sp_tmi_c,1) == 1 & size(cp_results_sp_tpl_c,1) == 1 & size(cp_results_sp_tcell_n_c,1) == 1 )
                    
                    try
                        % check values
                        val1 = im_1(cp_results_sp_tmi_c.AreaShape_Center_Y,cp_results_sp_tmi_c.AreaShape_Center_X);
                        val2 = im_2(cp_results_sp_tcell_n_c.AreaShape_Center_Y,cp_results_sp_tcell_n_c .AreaShape_Center_X);
                        val3 = im_3(cp_results_sp_tpl_c.AreaShape_Center_Y,cp_results_sp_tpl_c.AreaShape_Center_X);
                        
                        % crop
                        [i1,j1] = find(im_1 == val1);
                        [i2,j2] = find(im_2 == val2);
                        [i3,j3] = find(im_3 == val3);
                        
                        min_i = min([min(i1), min(i2), min(i3)]);
                        max_i = max([max(i1), max(i2), max(i3)]);
                        min_j = min([min(j1), min(j2), min(j3)]);
                        max_j = max([max(j1), max(j2), max(j3)]);
                        
                        im1t = im_1(min_i:max_i, min_j:max_j);
                        im2t = im_2(min_i:max_i, min_j:max_j);
                        im3t = im_3(min_i:max_i, min_j:max_j);
                        
                        % binarize
                        im1t(find(im1t~=val1)) = 0; im1t(find(im1t==val1)) = 1; im1t = imbinarize(im1t);
                        im2t(find(im2t~=val2)) = 0; im2t(find(im2t==val2)) = 1; im2t = imbinarize(im2t);
                        im3t(find(im3t~=val3)) = 0; im3t(find(im3t==val3)) = 1; im3t = imbinarize(im3t);
                        
                        
                        % this generates images that will be plotted
                        im_2_bu = im_2;
                        im_2_bu (find(im_2_bu~=val2)) = 0; im_2_bu(find(im_2_bu==val2)) = 1; im_2_bu = imbinarize(im_2_bu);
                        
                        b = uint8(boundarymask(im_2_bu));
                        b(find(b)) = timepoint_n;
                        
                        
                        b_t = im_bu(cell_list(cell_n)).im;
                        % b_t = im_bu(:,:,cell_list(cell_n));
                        b_t(find(b)) = b(find(b));
                        
                        im_bu(cell_list(cell_n)).im = b_t;
%                         im_bu(:,:,cell_list(cell_n)) = b_t;
                        
                        % Compute dynamics and save to table
                        imf = zeros(size(im1t));
                        imf(find(im2t)) = 1;
                        imf(find(im2t.*imcomplement(im1t))) = 2;
                        imf(find(im1t.*imcomplement(im2t))) = 3;
                        imf(find(im2t.*imcomplement(im1t).*imcomplement(im3t))) = 4;
                        
                        a = unique(imf);
                        frequencies = table(a,histc(imf(:),a));
                        frequencies.Properties.VariableNames = {'Dynamic', 'Number_of_pixels'};
                        
                        frequencies = frequencies(ismember(frequencies.Dynamic,[2:5]),:);
                        frequencies.newname(:,1) = {''};
                        
                        frequencies.newname(frequencies.Dynamic == 2) = {'Protrusions'};
                        frequencies.newname(frequencies.Dynamic == 3) = {'Retractions'};
                        frequencies.newname(frequencies.Dynamic == 4) = {'Short Lived Regions'};
                        
                        frequencies.Lab = repmat(Lab_descriptor, size(frequencies,1),1);
                        frequencies.User = repmat(User_descriptor, size(frequencies,1),1);
                        frequencies.Experiment = repmat(Experiment_descriptor, size(frequencies,1),1);
                        frequencies.stagepos = repmat(char(File.stagepos(stagepos)), size(frequencies,1),1);
                        frequencies.timepoint = repmat(timepoint_n, size(frequencies,1),1);
                        frequencies.WindowSize = repmat(Settings.Stepsize, size(frequencies,1),1);
                        frequencies.Cell_id = repmat(cell_list(cell_n), size(frequencies,1),1);
                        
                        if size(frequencies,1)>0
                            T = vertcat(T, frequencies);
                        end
                        
                    catch ME
                        
                        error_reg(count).cell_n = cell_n;
                        error_reg(count).cell_list_desc = cell_list(cell_n);
                        error_reg(count).timepoint = timepoint_n;
                        error_reg(count).stagepos = stagepos;
                        error_reg(count).message = ME.message;
                        
                        count = count+1;
                        continue;
                    end
                    
                end
            end
        end
        
        writetable(T, [char(File.folder_root), char(File.experiments(folder)),'\',...
            File.folder_results,char(File.stagepos(stagepos)), '\Membrane_dynamics.csv' ]);
        
        T = table();
        
        writetable( struct2table(error_reg), [char(File.folder_root), char(File.experiments(folder)),'\',...
            File.folder_results,char(File.stagepos(stagepos)), '\Membrane_dynamics_errors.csv' ]);
        
        % this is to plot
        for cell_n = 1:size(cell_list,1)
            figh = figure('pos',[10 10 1200 500], 'visible','off');
            %             figh = figure('pos',[10 10 1200 500]);
            
            subplot = @(m,n,p) subtightplot (m, n, p, [0.05 0.01], [0.1 0.1], [0.1 0.01]);
            
            im_plot = im_bu(cell_list(cell_n)).im;
            %             [i, j] = find(im_bu(:,:,cell_list(cell_n)));
            [i, j] = find(im_plot );
            
%             subplot(1,2,1), imagesc(im_bu(:,:,cell_list(cell_n))), axis image, axis off,
            subplot(1,2,1), imagesc(im_plot ), axis image, axis off,
            
            title(['TrackObjectLabel = ', num2str(cell_list(cell_n))])
%             subplot(1,2,2), imagesc(im_bu(min(i):max(i), min(j):max(j),cell_list(cell_n))), axis image, axis off, colorbar

            subplot(1,2,2), imagesc(im_plot(min(i):max(i), min(j):max(j))), axis image, axis off, colorbar
            
            
            print(figh, '-dpng', '-r100', [char(File.folder_root), char(File.experiments(folder)),'\',...
                File.folder_results,char(File.stagepos(stagepos)), '\Membrane_dynamics_track_obj_lab_', num2str(cell_list(cell_n)),'.png' ]);
            close all
            
        end
        
        
    end
    
end


