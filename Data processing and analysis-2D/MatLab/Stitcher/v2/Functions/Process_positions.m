function [ results ] = Process_positions( File, Settings, temp, results)
%UNTITLED8 Summary of this function goes here
%   Detailed explanation goes here

%% define vals
im_size1 = round(File.size_im_1*Settings.resize_factor);

results.output.finaltables = struct();

for k = 1:size(results.output.final,1),
    
    %     define Image number, col and row numbers
    results.output.finaltables(k).tab = array2table([reshape(temp.matrix',[],1),repmat(1:File.ncols,1,File.nrows)',reshape(repmat(1:File.nrows,File.ncols,1),[],1)]);
    results.output.finaltables(k).tab.Properties.VariableNames = {'Image', 'col_n', 'row_n'};
    
    %     specify calculated shifts
    results.output.finaltables(k).tab.shift_col(:,1) = results.output.final.med_val_col(k);
    results.output.finaltables(k).tab.shift_row(:,1) = results.output.final.med_val_row(k);
    
    %     specify overlap if there is overlap
    if results.output.final.overlap(k) == 1
        results.output.finaltables(k).tab.overlap(:,1) = round(Settings.Overlap_span*Settings.resize_factor);
    end
    if results.output.final.overlap(k) == 0
        results.output.finaltables(k).tab.overlap(:,1) = 0;
    end
    
    %     calculate cumulative overlap over rows and cols
    results.output.finaltables(k).tab.cum_overlap_row(:,1) = results.output.finaltables(k).tab.overlap .* (results.output.finaltables(k).tab.row_n-1);
    results.output.finaltables(k).tab.cum_overlap_col(:,1) = results.output.finaltables(k).tab.overlap .* (results.output.finaltables(k).tab.col_n-1);
    
    %     calculate cumulative shift for rows and cols
    if results.output.final.overlap(k) == 1
    results.output.finaltables(k).tab.cum_shift_row(:,1) = results.output.finaltables(k).tab.shift_row .* (results.output.finaltables(k).tab.row_n-1);
    results.output.finaltables(k).tab.cum_shift_col(:,1) = results.output.finaltables(k).tab.shift_col .* (results.output.finaltables(k).tab.col_n-1);
    end
    
    if results.output.final.overlap(k) == 0
    results.output.finaltables(k).tab.cum_shift_row(:,1) = 0;
    results.output.finaltables(k).tab.cum_shift_col(:,1) = 0;
    end
    
    %     calculate initial shift for rows and cols
    results.output.finaltables(k).tab.init_shift_row(:,1) = (results.output.finaltables(k).tab.row_n-1) .* results.output.finaltables(k).tab.shift_col;
    results.output.finaltables(k).tab.init_shift_col(:,1) = (results.output.finaltables(k).tab.col_n-1) .* results.output.finaltables(k).tab.shift_row;
    
%     Define initial border
    results.output.finaltables(k).tab.init_val(:,1) = Settings.init_border;
    
    
    results.output.finaltables(k).tab.x_val_init(:,1) = round(Settings.resize_factor*File.size_im_2)*(results.output.finaltables(k).tab.col_n-1) - ...
        results.output.finaltables(k).tab.cum_overlap_col + ...
        results.output.finaltables(k).tab.cum_shift_col + ...
        results.output.finaltables(k).tab.init_shift_row + ...
        results.output.finaltables(k).tab.init_val;
    
    
    results.output.finaltables(k).tab.y_val_init(:,1) = round(Settings.resize_factor*File.size_im_1)*(results.output.finaltables(k).tab.row_n-1) - ...
        results.output.finaltables(k).tab.cum_overlap_row + ...
        results.output.finaltables(k).tab.cum_shift_row + ...
        results.output.finaltables(k).tab.init_shift_col + ...
        results.output.finaltables(k).tab.init_val;
    
    
    results.output.finaltables(k).tab.experiment(:,1) = results.output.final.experiment_n(k);
    
    
end

results.output.finaltablecat = cat(1,results.output.finaltables.tab);
% 
% 
% results.output.final = array2table([reshape(temp.matrix',[],1),repmat(1:File.ncols,1,File.nrows)',reshape(repmat(1:File.nrows,File.ncols,1),[],1)]);
% results.output.final.Properties.VariableNames = {'Image', 'col_n', 'row_n'};
% results.output.final.shift_col(:,1) = results.output.med_val_col;
% results.output.final.shift_row(:,1) = results.output.med_val_row;
% results.output.final.overlap(:,1) = round(Settings.Overlap_span*Settings.resize_factor);
% results.output.final.cum_overlap_row(:,1) = results.output.final.overlap .* (results.output.final.row_n-1);
% results.output.final.cum_overlap_col(:,1) = results.output.final.overlap .* (results.output.final.col_n-1);
% results.output.final.cum_shift_row(:,1) = results.output.final.shift_row .* (results.output.final.row_n-1);
% results.output.final.cum_shift_col(:,1) = results.output.final.shift_col .* (results.output.final.col_n-1);
% results.output.final.init_shift_row(:,1) = (results.output.final.row_n-1) .* results.output.final.shift_col;
% results.output.final.init_shift_col(:,1) = (results.output.final.col_n-1) .* results.output.final.shift_row;
% 
% results.output.final.init_val(:,1) = Settings.init_border;
% 
% 
% results.output.final.x_val_init(:,1) = round(Settings.resize_factor*File.size_im_2)*(results.output.final.col_n-1) - ...
%     results.output.final.cum_overlap_col + ...
%     results.output.final.cum_shift_col + ...
%     results.output.final.init_shift_row + ...
%     results.output.final.init_val;
% 
% 
% results.output.final.y_val_init(:,1) = round(Settings.resize_factor*File.size_im_1)*(results.output.final.row_n-1) - ...
%     results.output.final.cum_overlap_row + ...
%     results.output.final.cum_shift_row + ...
%     results.output.final.init_shift_col + ...
%     results.output.final.init_val;
% 
% 





end