function [ results ] = post_process_results_stitching( File, Settings, results)


%% impose here corrected values for RsquaredAdjusted < %Settings.min_RsquaredAdjusted

%rows
results.output.rows.tformEstimate_row_cor = results.output.rows.tformEstimate_row;
results.output.rows.tformEstimate_row_cor (results.output.rows.RsquaredAdjusted<Settings.min_RsquaredAdjusted) = repmat(mean(results.output.rows.tformEstimate_row (results.output.rows.RsquaredAdjusted>=Settings.min_RsquaredAdjusted)) , size(find(results.output.rows.RsquaredAdjusted<Settings.min_RsquaredAdjusted),1),1);

results.output.rows.tformEstimate_col_cor = results.output.rows.tformEstimate_col;
results.output.rows.tformEstimate_col_cor (results.output.rows.RsquaredAdjusted<Settings.min_RsquaredAdjusted) = repmat(mean(results.output.rows.tformEstimate_col (results.output.rows.RsquaredAdjusted>=Settings.min_RsquaredAdjusted)) , size(find(results.output.rows.RsquaredAdjusted<Settings.min_RsquaredAdjusted),1),1);

%cols
results.output.cols.tformEstimate_row_cor = results.output.cols.tformEstimate_row;
results.output.cols.tformEstimate_row_cor (results.output.cols.RsquaredAdjusted<Settings.min_RsquaredAdjusted) = repmat(mean(results.output.cols.tformEstimate_row (results.output.cols.RsquaredAdjusted>=Settings.min_RsquaredAdjusted)) , size(find(results.output.cols.RsquaredAdjusted<Settings.min_RsquaredAdjusted),1),1);

results.output.cols.tformEstimate_col_cor = results.output.cols.tformEstimate_col;
results.output.cols.tformEstimate_col_cor (results.output.cols.RsquaredAdjusted<Settings.min_RsquaredAdjusted) = repmat(mean(results.output.cols.tformEstimate_col (results.output.cols.RsquaredAdjusted>=Settings.min_RsquaredAdjusted)) , size(find(results.output.cols.RsquaredAdjusted<Settings.min_RsquaredAdjusted),1),1);



%% define regions

results.output.all = vertcat(results.output.rows,results.output.cols);

results.output.all.init_border(:,1) = repmat(Settings.init_border,height(results.output.all),1);

% define x and y theoretical positions
x_pos_theor_init_rows = repmat(File.size_im_1:(File.size_im_1-Settings.Overlap_span):(File.size_im_1-Settings.Overlap_span)*File.nrows-1,1,File.ncols);
x_pos_theor_final_rows = x_pos_theor_init_rows + File.size_im_1;

y_pos_theor_init_rows = reshape(repmat(0:(File.size_im_1-Settings.Overlap_span):(File.size_im_1-Settings.Overlap_span)*(File.nrows-1), File.nrows-1, 1),1,[]);
y_pos_theor_final_rows = y_pos_theor_init_rows + File.size_im_1;

x_pos_theor_init_cols = reshape(repmat(0:(File.size_im_2-Settings.Overlap_span):(File.size_im_2-Settings.Overlap_span)*(File.ncols-1), File.ncols-1, 1),1,[]);
x_pos_theor_final_cols = x_pos_theor_init_cols + File.size_im_2;

y_pos_theor_init_cols = repmat(File.size_im_2:(File.size_im_2-Settings.Overlap_span):(File.size_im_2-Settings.Overlap_span)*File.ncols-1,1,File.nrows);
y_pos_theor_final_cols = y_pos_theor_init_cols + File.size_im_1;

results.output.all.x_pos_thero_init(:,1) = [x_pos_theor_init_rows x_pos_theor_init_cols]';
results.output.all.x_pos_thero_final(:,1) = [x_pos_theor_final_rows x_pos_theor_final_cols]';
results.output.all.y_pos_thero_init(:,1) = [y_pos_theor_init_rows y_pos_theor_init_cols]';
results.output.all.y_pos_thero_final(:,1) = [y_pos_theor_final_rows y_pos_theor_final_cols]';

% define overlaps
results.output.all.overlapx(:,1) = [repmat(Settings.Overlap_span,20,1); repmat(0,20,1)];
results.output.all.overlapy(:,1) = [repmat(0,20,1); repmat(Settings.Overlap_span,20,1)];
       
% compute cumulated overlap
counter = 1;                                        
for k = 1:4:40                                        
    for k2 = 0:3
        results.output.all.cum_shift_row(counter) = sum(results.output.all.tformEstimate_row_cor(k:k+k2));
        results.output.all.cum_shift_col(counter) = sum(results.output.all.tformEstimate_col_cor(k:k+k2));
 
        counter = counter+1;
    end
end



% find proper displacements for each row and create new column with this info
results.output.cols2 =  results.output.all(strcmp(results.output.all.direction, 'cols'),:);
results.output.cols2 = results.output.cols2(1:4,:);
results.output.cols2.rownumber(:,1) = 2:File.nrows;
results.output.cols2.cum_shift_rows = cumsum(results.output.cols2.tformEstimate_row_cor);
results.output.cols2.cum_shift_cols = cumsum(results.output.cols2.tformEstimate_col_cor);

results.output.all.rownumber(:,1) = [reshape(repmat(1:File.nrows,File.ncols-1,1),1,[]), reshape(repmat(2:File.nrows,File.ncols,1)',1,[])];
results.output.all.colnumber(:,1) = [[reshape(repmat(2:File.ncols, File.nrows,1)',1,[]), reshape(repmat(1:File.nrows, File.ncols-1,1),1,[])]];


for k=1:height(results.output.all),
%     need to update this with cumulated shifts in 'cols'
    if ismember(results.output.all.rownumber(k), results.output.cols2.rownumber) && results.output.all.colnumber(k) ~=1 && strcmp(results.output.all.direction{k},'rows'),
        
        results.output.all.shift_cols(k,1) = results.output.cols2.cum_shift_col( results.output.cols2.rownumber == results.output.all.rownumber(k));
        results.output.all.shift_rows(k,1) = results.output.cols2.cum_shift_row( results.output.cols2.rownumber == results.output.all.rownumber(k));
   
    end
     
    
end
% for k=1:height(results.output.all),
%     if results.output.all.colnumber(k)==1 & results.output.all.rownumber(k)>2,
%         results.output.all.shift_cols(k,1) = results.output.cols2.cum_shift_col( results.output.cols2.rownumber == results.output.all.rownumber(k)-1);
%         results.output.all.shift_rows(k,1) = results.output.cols2.cum_shift_row( results.output.cols2.rownumber == results.output.all.rownumber(k)-1);
%     
%     end
% end






% assign real init and final positions
results.output.all.x_pos_real_init(:,1) = results.output.all.init_border+...
                                            results.output.all.x_pos_thero_init+...
                                            -results.output.all.overlapx+...
                                            results.output.all.cum_shift_col+...
                                            results.output.all.shift_cols;

results.output.all.x_pos_real_final(:,1) = results.output.all.init_border+...
                                            results.output.all.x_pos_thero_final+...
                                            -results.output.all.overlapx+...
                                            results.output.all.cum_shift_col+...
                                            results.output.all.shift_cols-1;

results.output.all.y_pos_real_init(:,1) = results.output.all.init_border+...
                                            results.output.all.y_pos_thero_init+...
                                            -results.output.all.overlapy+...
                                            results.output.all.cum_shift_row+...
                                            results.output.all.shift_rows;

results.output.all.y_pos_real_final(:,1) = results.output.all.init_border+...
                                            results.output.all.y_pos_thero_final+...
                                            -results.output.all.overlapy+...
                                            results.output.all.cum_shift_row+...
                                            results.output.all.shift_rows-1;



end

