function   results = post_process_results_stitching_no_overlap(File, Settings);


final_table = readtable(horzcat(char(unique(File.files_list_s_exp.folder_processed_experiment)),char(File.folder_results_errors), 'stitch_vals.csv'));

row_shift = final_table.vals(1);
col_shift = final_table.vals(2);

init_border = (  repmat(Settings.init_border,24,1));



% define x and y theoretical positions
x_pos_theor_init_rows  = [( repmat(File.size_im_1:File.size_im_1:File.size_im_1*File.nrows-1,1,File.ncols)'); ...
                          repmat(0,4,1)]  ; % this is to add info for four first images of each row starting from the second
x_pos_theor_final_rows = [x_pos_theor_init_rows + File.size_im_1];

y_pos_theor_init_rows = [(reshape(repmat(0:File.size_im_1:File.size_im_1*(File.nrows-1), File.nrows-1, 1),1,[])');...
   ( File.size_im_1:File.size_im_1:File.size_im_1*(File.nrows-1))'];% this is to add info for four first images of each row starting from the second

y_pos_theor_final_rows = y_pos_theor_init_rows + File.size_im_1;

results.output.all = table(init_border, x_pos_theor_init_rows, x_pos_theor_final_rows, y_pos_theor_init_rows, y_pos_theor_final_rows);


% define constant increases
results.output.all.y_pos_increase (:,1)  = [reshape(repmat(row_shift:row_shift:row_shift*(File.nrows-1),File.ncols,1)',1,[])';...
    repmat(0,4,1)]; % this is to add info for four first images of each row starting from the second
results.output.all.x_pos_increase (:,1)  = [reshape(repmat(0:col_shift:col_shift*(File.ncols-1),File.nrows-1,1),1,[])';...
    (col_shift:col_shift:col_shift*4)']; % this is to add info for four first images of each row starting from the second




% assign real init and final positions
results.output.all.x_pos_real_init(:,1) = results.output.all.init_border+...
    results.output.all.x_pos_theor_init_rows+...
results.output.all.x_pos_increase;

results.output.all.x_pos_real_final(:,1) = results.output.all.init_border+...
    results.output.all.x_pos_theor_final_rows+...
results.output.all.x_pos_increase-1;

results.output.all.y_pos_real_init(:,1) = results.output.all.init_border+...
    results.output.all.y_pos_theor_init_rows+...
results.output.all.y_pos_increase;

results.output.all.y_pos_real_final(:,1) = results.output.all.init_border+...
    results.output.all.y_pos_theor_final_rows+...
results.output.all.y_pos_increase-1;



% need to finish this
results.output.all.im2 = [File.im_order.rows(:,2); [10:11 20:21]'];



rownumber = [reshape(repmat(1:File.nrows,File.ncols-1,1),1,[]), reshape(repmat(2:File.nrows,File.ncols,1)',1,[])];
colnumber = [reshape(repmat(2:File.ncols, File.nrows,1)',1,[]), reshape(repmat(1:File.nrows, File.ncols-1,1),1,[])];

results.output.all.rownumber(:,1) = rownumber(1:24);
results.output.all.colnumber(:,1) = colnumber(1:24);


end