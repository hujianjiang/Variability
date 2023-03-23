
%% Settings
Settings.Overlap_span = round(1024*0.05);  %Approximate overlap in pixels
Settings.usfac = 100; % dftregistration algorithm value
Settings.Snake = 1; %are images acquired 
Settings.init_border = 100; % Border margins
Settings.n_cols = 5; % number of columns
Settings.n_rows = 5; % number of rows
Settings.min_RsquaredAdjusted = 0.9; %min value of RsquaredAdjusted test between images

%% Files
files = readtable('C:\Users\xavser\Desktop\Geiger_Lab\Processed\files.xlsx');


%% Load files
temp.experiment = unique(files.experiment);
files_s = files(strcmp(files.experiment, temp.experiment(1)),:);
temp.wells = unique(files_s.well);
files_s = files_s (strcmp(files_s.well,temp.wells(1)),:);

for k=1:25,
    im{k,1} = bfopen(files_s.file_path{k});
end
%
% if Settings.Snake; File.im_order = [1:5,fliplr(6:10),11:15,fliplr(16:20),21:25]'; end
% if ~Settings.Snake; File.im_order = (1:25)'; end


%% im{pos,1}{1,1}{timeandchannel,1}




%% extra 1

clear results
temp.matrix = reshape(1:Settings.n_cols*Settings.n_rows,Settings.n_cols,[])';
if Settings.Snake, temp.matrix(2:2:end,:) = temp.matrix (2:2:end,end:-1:1);end

File.im_order.rows = [reshape(temp.matrix(1:end,1:end-1)',[],1) ,reshape(temp.matrix(1:end,2:end)',[],1)];
File.im_order.cols = [reshape(temp.matrix(1:end-1,1:end),[],1) ,reshape(temp.matrix(2:end,1:end),[],1)];





% this calculates overlaps for rows
for i = 1:size(File.im_order.rows,1),
    im1 = im{File.im_order.rows(i,1),1}{1,1}{1,1};
    im2 = im{File.im_order.rows(i,2),1}{1,1}{1,1};
    
    % define overlapping region images
    im1_o = im1(:,end-Settings.Overlap_span +1:end);
    im2_o = im2(:,1:Settings.Overlap_span);
    
    % compute disp
    [output, Greg] = dftregistration(fft2(im1_o), fft2(im2_o), Settings.usfac);
    tformEstimate = imregcorr(im1_o,im2_o, 'translation');
    
    
    
    %       check similarity
    % im2_ot = (imtranslate(im2_o,[output(4),output(3)], 'nearest'));
    im2_ot = (imtranslate(im2_o,[-tformEstimate.T(3),-tformEstimate.T(6)], 'nearest'));
    im1_ot = im1_o;
    
    
    im2_ot = im2_ot(round(abs(tformEstimate.T(6)))+1:end-round(abs(tformEstimate.T(6))), round(abs(tformEstimate.T(3)))+1:end-round(abs(tformEstimate.T(3))));
    im1_ot = im1_ot(round(abs(tformEstimate.T(6)))+1:end-round(abs(tformEstimate.T(6))), round(abs(tformEstimate.T(3)))+1:end-round(abs(tformEstimate.T(3))));
    
    y = double(reshape(im2_ot,[],1));
    x = double(reshape(im1_ot,[],1));
    
    mdl = fitlm (y , x);
    output_val(i).RsquaredAdjusted = mdl.Rsquared.Adjusted;
    
    % save results
    results.output.rows(i,1:10) = array2table([i, File.im_order.rows(i,:), output,mdl.Rsquared.Adjusted,  -tformEstimate.T(6), -tformEstimate.T(3) ] );
    %         results.output.rows(i,1:4) = output;
end
temp.varnames = {'round','im1','im2','error', 'diffphase','net_row_shift', 'net_col_shift', 'RsquaredAdjusted', 'tformEstimate_row', 'tformEstimate_col'};
results.output.rows.Properties.VariableNames = temp.varnames;
results.output.rows.direction(:,1) = {'rows'};

%impose here corrected values for RsquaredAdjusted < %Settings.min_RsquaredAdjusted
results.output.rows.tformEstimate_row_cor = results.output.rows.tformEstimate_row;
results.output.rows.tformEstimate_row_cor (results.output.rows.RsquaredAdjusted<Settings.min_RsquaredAdjusted) = repmat(mean(results.output.rows.tformEstimate_row (results.output.rows.RsquaredAdjusted>=Settings.min_RsquaredAdjusted)) , size(find(results.output.rows.RsquaredAdjusted<Settings.min_RsquaredAdjusted),1),1);

results.output.rows.tformEstimate_col_cor = results.output.rows.tformEstimate_col;
results.output.rows.tformEstimate_col_cor (results.output.rows.RsquaredAdjusted<Settings.min_RsquaredAdjusted) = repmat(mean(results.output.rows.tformEstimate_col (results.output.rows.RsquaredAdjusted>=Settings.min_RsquaredAdjusted)) , size(find(results.output.rows.RsquaredAdjusted<Settings.min_RsquaredAdjusted),1),1);


% this calculates overlaps for cols
for i = 1:size(File.im_order.cols,1),
    im1 = im{File.im_order.cols(i,1),1}{1,1}{1,1};
    im2 = im{File.im_order.cols(i,2),1}{1,1}{1,1};
    
%     im1 = im1';
%     im2 = im2';
    
    % define overlapping region images
    im1_o = im1(end-Settings.Overlap_span +1:end,:);
    im2_o = im2(1:Settings.Overlap_span,:);
    
    % compute disp
    [output, Greg] = dftregistration(fft2(im1_o), fft2(im2_o), Settings.usfac);
    
    tformEstimate = imregcorr(im1_o,im2_o, 'translation');
    
    
    %       check similarity
    %     im2_ot = (imtranslate(im2_o,[output(4),output(3)], 'nearest'));
    im2_ot = (imtranslate(im2_o,[-tformEstimate.T(3),-tformEstimate.T(6)], 'nearest'));
    im1_ot = im1_o;
    
    im2_ot = im2_ot(round(abs(tformEstimate.T(6)))+1:end-round(abs(tformEstimate.T(6))), round(abs(tformEstimate.T(3)))+1:end-round(abs(tformEstimate.T(3))));
    im1_ot = im1_ot(round(abs(tformEstimate.T(6)))+1:end-round(abs(tformEstimate.T(6))), round(abs(tformEstimate.T(3)))+1:end-round(abs(tformEstimate.T(3))));
    
    y = double(reshape(im2_ot,[],1));
    x = double(reshape(im1_ot,[],1));
    
    mdl = fitlm (y , x);
    output_val(i).RsquaredAdjusted = mdl.Rsquared.Adjusted;
    
    % save results
    results.output.cols(i,1:10) = array2table([i, File.im_order.cols(i,:), output,mdl.Rsquared.Adjusted, -tformEstimate.T(6), -tformEstimate.T(3) ] ); %I switch here rows and cols because images were transposed
    
end
temp.varnames = {'round','im1','im2','error', 'diffphase','net_row_shift', 'net_col_shift', 'RsquaredAdjusted', 'tformEstimate_row', 'tformEstimate_col'};
results.output.cols.Properties.VariableNames = temp.varnames;
results.output.cols.direction(:,1) = {'cols'};

%impose here corrected values for RsquaredAdjusted < %Settings.min_RsquaredAdjusted
results.output.cols.tformEstimate_row_cor = results.output.cols.tformEstimate_row;
results.output.cols.tformEstimate_row_cor (results.output.cols.RsquaredAdjusted<Settings.min_RsquaredAdjusted) = repmat(mean(results.output.cols.tformEstimate_row (results.output.cols.RsquaredAdjusted>=Settings.min_RsquaredAdjusted)) , size(find(results.output.cols.RsquaredAdjusted<Settings.min_RsquaredAdjusted),1),1);

results.output.cols.tformEstimate_col_cor = results.output.cols.tformEstimate_col;
results.output.cols.tformEstimate_col_cor (results.output.cols.RsquaredAdjusted<Settings.min_RsquaredAdjusted) = repmat(mean(results.output.cols.tformEstimate_col (results.output.cols.RsquaredAdjusted>=Settings.min_RsquaredAdjusted)) , size(find(results.output.cols.RsquaredAdjusted<Settings.min_RsquaredAdjusted),1),1);


File.size_im_1 = 1024; % images row size
File.size_im_2 = 1024; % images col size
File.nrows = 5; % number of rows
File.ncols = 5; % number of cols

% im1 = im{File.im_order.rows(1,1),1}{1,1}{1,1};
% im2 = im{File.im_order.rows(1,2),1}{1,1}{1,1};

% imf = zeros(1024*5+200,1024*5+200);


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

                                        
                                        
imf = zeros(Settings.init_border*2+File.size_im_1*File.ncols,Settings.init_border*2+File.size_im_2*File.nrows);

imf(Settings.init_border:Settings.init_border+File.size_im_1-1,...
        Settings.init_border:Settings.init_border+File.size_im_2-1) = im{results.output.all.im1(1),1}{1,1}{1,1};

   
for k = 1:24,
    
    y1 = results.output.all.y_pos_real_init(k);
    y2 = results.output.all.y_pos_real_final(k);
    x1 = results.output.all.x_pos_real_init(k);
    x2 = results.output.all.x_pos_real_final(k);
    imf(y1:y2,x1:x2) = im{results.output.all.im2(k),1}{1,1}{1,1};
     figure, imagesc(imf), axis image

end

           
