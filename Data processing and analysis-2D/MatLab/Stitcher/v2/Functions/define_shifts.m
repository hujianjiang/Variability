function [results] = define_shifts(File, Settings)

File.files_sorted.sep = repmat('_', size(File.files_sorted,1),1);
File.files_sorted.cat = cellstr([num2str(File.files_sorted.experiment_n) File.files_sorted.sep num2str(File.files_sorted.well_n) File.files_sorted.sep num2str(File.files_sorted.channel) File.files_sorted.sep num2str(File.files_sorted.timepoint)]);

temp = unique(File.files_sorted.cat(File.files_sorted.channel == 1)); %only perform for channel = 1;
temp_output= struct();
temp_output.rows = table();
temp_output.rows_all = table();
temp_output.cols = table();
temp_output.cols_all = table();



for k_set = 1:Settings.shift_compute_interval:size(temp,1)
    k_set
    temp_files = File.files_sorted(strcmp(File.files_sorted.cat,temp(k_set)),:);
    
     try
        %% rows compute
        for k_mosaic = 1:size(File.im_order.rows,1)
            
            %     load images
            im_1_n = File.im_order.rows(k_mosaic,1);
            im_2_n = File.im_order.rows(k_mosaic,2);
            
            
            filename1 = [char(temp_files.Path(im_1_n)) File.folder_illumination_corrected char(temp_files.Condition(im_1_n)) '/Chan_' num2str(temp_files.channel(im_1_n)), ...
                '/' 'C' num2str(temp_files.channel(im_1_n)), '_T',num2str(temp_files.timepoint(im_1_n)), '_M', num2str(temp_files.Mosaic_Image_Number(im_1_n)), '.tif'];
            
            filename2 = [char(temp_files.Path(im_2_n)) File.folder_illumination_corrected char(temp_files.Condition(im_2_n)) '/Chan_' num2str(temp_files.channel(im_2_n)), ...
                '/' 'C' num2str(temp_files.channel(im_2_n)), '_T',num2str(temp_files.timepoint(im_2_n)), '_M', num2str(temp_files.Mosaic_Image_Number(im_2_n)), '.tif'];
            
            %             filename2 = [char(temp_files.Path(im_2_n)) File.folder_illumination_corrected 'xy' num2str(temp_files.well_n(im_2_n)) 'c' num2str(temp_files.channel(im_2_n)) '/',...
            %                 'C' num2str(temp_files.channel(im_2_n)), '_T',num2str(temp_files.timepoint(im_2_n)), '_M', num2str(temp_files.Mosaic_Image_Number(im_2_n)), '.tif'];
            
            im1 = imread(filename1);
            im2 = imread(filename2);
            
            % define overlapping region images. Different overlap depending on if there was overlap or not during acquisition
            if temp_files.overlap == 0,
                im1_o = im1(:,end-round(Settings.no_overlap_shift_compute*Settings.resize_factor) +1:end);
                im2_o = im2(:,1:round(Settings.no_overlap_shift_compute*Settings.resize_factor) );
            end
            
            if temp_files.overlap == 1,
                im1_o = im1(:,end-round(Settings.Overlap_span*Settings.resize_factor) +1:end);
                im2_o = im2(:,1:round(Settings.Overlap_span*Settings.resize_factor) );
            end
            
            
            %             perform im registration
            [output, Greg] = dftregistration(fft2(im1_o), fft2(im2_o), Settings.usfac);
            tformEstimate = imregcorr(im1_o,im2_o, 'translation');
            
            
            %       check similarity
            im2_ot = (imtranslate(im2_o,[-tformEstimate.T(3),-tformEstimate.T(6)], 'nearest'));
            im1_ot = im1_o;
            
            im2_ot = im2_ot(round(abs(tformEstimate.T(6)))+1:end-round(abs(tformEstimate.T(6))), round(abs(tformEstimate.T(3)))+1:end-round(abs(tformEstimate.T(3))));
            im1_ot = im1_ot(round(abs(tformEstimate.T(6)))+1:end-round(abs(tformEstimate.T(6))), round(abs(tformEstimate.T(3)))+1:end-round(abs(tformEstimate.T(3))));
            
            y = double(reshape(im2_ot,[],1));
            x = double(reshape(im1_ot,[],1));
            
            mdl = fitlm (y , x);
            
            temp_output.rows( k_mosaic,1:10) = array2table([k_mosaic, File.im_order.rows(k_mosaic,:), output,mdl.Rsquared.Adjusted,  -tformEstimate.T(6), -tformEstimate.T(3) ] );
            
        end
        
        varnames = {'round','im1','im2','error', 'diffphase','net_row_shift', 'net_col_shift', 'RsquaredAdjusted', 'tformEstimate_row', 'tformEstimate_col'};
        temp_output.rows.Properties.VariableNames = varnames;
        temp_output.rows.direction(:,1) = {'rows'};
        
        temp_output.rows.experiment_n (:,1) = unique(temp_files.experiment_n);
        temp_output.rows.well_n(:,1) = unique(temp_files.well_n);
        temp_output.rows.timepoint(:,1) = unique(temp_files.timepoint);
        
        temp_output.rows_all = [temp_output.rows_all ; temp_output.rows];
        temp_output.rows = table();
        
        
        
        %% cols compute
        for k_mosaic = 1:size(File.im_order.rows,1)
            
            %     load images
            im_1_n = File.im_order.cols(k_mosaic,1);
            im_2_n = File.im_order.cols(k_mosaic,2);
            
            filename1 = [char(temp_files.Path(im_1_n)) File.folder_illumination_corrected char(temp_files.Condition(im_1_n)) '/Chan_' num2str(temp_files.channel(im_1_n)), ...
                '/' 'C' num2str(temp_files.channel(im_1_n)), '_T',num2str(temp_files.timepoint(im_1_n)), '_M', num2str(temp_files.Mosaic_Image_Number(im_1_n)), '.tif'];
            
            filename2 = [char(temp_files.Path(im_2_n)) File.folder_illumination_corrected char(temp_files.Condition(im_2_n)) '/Chan_' num2str(temp_files.channel(im_2_n)), ...
                '/' 'C' num2str(temp_files.channel(im_2_n)), '_T',num2str(temp_files.timepoint(im_2_n)), '_M', num2str(temp_files.Mosaic_Image_Number(im_2_n)), '.tif'];
            
            % %
            %
            %
            % filename1 = [char(temp_files.Path(im_1_n)) File.folder_illumination_corrected 'xy' num2str(temp_files.well_n(im_1_n)) 'c' num2str(temp_files.channel(im_1_n)) '/',...
            %                 'C' num2str(temp_files.channel(im_1_n)), '_T',num2str(temp_files.timepoint(im_1_n)), '_M', num2str(temp_files.Mosaic_Image_Number(im_1_n)), '.tif'];
            %
            %             filename2 = [char(temp_files.Path(im_2_n)) File.folder_illumination_corrected 'xy' num2str(temp_files.well_n(im_2_n)) 'c' num2str(temp_files.channel(im_2_n)) '/',...
            %                 'C' num2str(temp_files.channel(im_2_n)), '_T',num2str(temp_files.timepoint(im_2_n)), '_M', num2str(temp_files.Mosaic_Image_Number(im_2_n)), '.tif'];
            %
            im1 = imread(filename1);
            im2 = imread(filename2);
            
            
            % define overlapping region images. Different overlap depending on if there was overlap or not during acquisition
            if temp_files.overlap == 0,
                im1_o = im1(end-round(Settings.no_overlap_shift_compute*Settings.resize_factor) +1:end,:);
                im2_o = im2(1:round(Settings.no_overlap_shift_compute*Settings.resize_factor) ,:);
            end
            
            if temp_files.overlap == 1,
                im1_o = im1(end-round(Settings.Overlap_span*Settings.resize_factor) +1:end,:);
                im2_o = im2(1:round(Settings.Overlap_span*Settings.resize_factor) ,:);
            end
            
            
            %             perform im registration
            [output, Greg] = dftregistration(fft2(im1_o), fft2(im2_o), Settings.usfac);
            tformEstimate = imregcorr(im1_o,im2_o, 'translation');
            
            
            %       check similarity
            im2_ot = (imtranslate(im2_o,[-tformEstimate.T(3),-tformEstimate.T(6)], 'nearest'));
            im1_ot = im1_o;
            
            im2_ot = im2_ot(round(abs(tformEstimate.T(6)))+1:end-round(abs(tformEstimate.T(6))), round(abs(tformEstimate.T(3)))+1:end-round(abs(tformEstimate.T(3))));
            im1_ot = im1_ot(round(abs(tformEstimate.T(6)))+1:end-round(abs(tformEstimate.T(6))), round(abs(tformEstimate.T(3)))+1:end-round(abs(tformEstimate.T(3))));
            
            y = double(reshape(im2_ot,[],1));
            x = double(reshape(im1_ot,[],1));
            
            mdl = fitlm (y , x);
            
            temp_output.cols( k_mosaic,1:10) = array2table([k_mosaic, File.im_order.rows(k_mosaic,:), output,mdl.Rsquared.Adjusted,  -tformEstimate.T(6), -tformEstimate.T(3) ] );
            
        end
        
        varnames = {'round','im1','im2','error', 'diffphase','net_row_shift', 'net_col_shift', 'RsquaredAdjusted', 'tformEstimate_row', 'tformEstimate_col'};
        temp_output.cols.Properties.VariableNames = varnames;
        temp_output.cols.direction(:,1) = {'cols'};
        
        temp_output.cols.experiment_n (:,1) = unique(temp_files.experiment_n);
        temp_output.cols.well_n(:,1) = unique(temp_files.well_n);
        temp_output.cols.timepoint(:,1) = unique(temp_files.timepoint);
        
        temp_output.cols_all = [temp_output.cols_all ; temp_output.cols];
        temp_output.cols = table();
     end
    
    
    
end

results.output.med_val_col = round(median(temp_output.cols_all.tformEstimate_col));
results.output.med_val_row = round(median(temp_output.rows_all.tformEstimate_row));

results.output.mean_val_col = round(mean(temp_output.cols_all.tformEstimate_col));
results.output.mean_val_row = round(mean(temp_output.rows_all.tformEstimate_row));

results.output.final = table(splitapply(@mean, temp_output.cols_all.tformEstimate_col, findgroups(temp_output.cols_all.experiment_n)),...
    splitapply(@mean, temp_output.rows_all.tformEstimate_row, findgroups(temp_output.rows_all.experiment_n)),...
    splitapply(@median, temp_output.cols_all.tformEstimate_col, findgroups(temp_output.cols_all.experiment_n)),...
    splitapply(@median, temp_output.rows_all.tformEstimate_row, findgroups(temp_output.rows_all.experiment_n)),...
    unique(findgroups(temp_output.cols_all.experiment_n)));

results.output.final.Properties.VariableNames = {'med_val_col', 'med_val_row', 'mean_val_col', 'mean_val_row', 'experiment_n'};


[sharedVals,idxsIntoA] = intersect(File.files_sorted.experiment_n,unique(File.files_sorted.experiment_n));

results.output.final.overlap = File.files_sorted.overlap(idxsIntoA);


end








