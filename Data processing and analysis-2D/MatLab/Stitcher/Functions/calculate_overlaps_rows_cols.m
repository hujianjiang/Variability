function [results] = calculate_overlaps_rows_cols(File, Settings, im);

% this calculates overlaps for rows
for i = 1:size(File.im_order.rows,1),
    
    im1 = im{File.im_order.rows(i,1),1};
    im2 = im{File.im_order.rows(i,2),1};
    
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
    
    % save results
    results.output.rows(i,1:10) = array2table([i, File.im_order.rows(i,:), output,mdl.Rsquared.Adjusted,  -tformEstimate.T(6), -tformEstimate.T(3) ] );
    
end

temp.varnames = {'round','im1','im2','error', 'diffphase','net_row_shift', 'net_col_shift', 'RsquaredAdjusted', 'tformEstimate_row', 'tformEstimate_col'};
results.output.rows.Properties.VariableNames = temp.varnames;
results.output.rows.direction(:,1) = {'rows'};


% this calculates overlaps for cols
for i = 1:size(File.im_order.cols,1),
    
    im1 = im{File.im_order.cols(i,1),1};
    im2 = im{File.im_order.cols(i,2),1};
    
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
    
    
    %     im2_ot = (imtranslate(im2_o,[-5,-10], 'nearest'));
    %     im1_ot = im1_o;
    %     im2_ot = im2_ot(round(abs(10))+1:end-round(abs(10)), round(abs(5))+1:end-round(abs(5)));
    %     im1_ot = im1_ot(round(abs(10))+1:end-round(abs(10)), round(abs(5))+1:end-round(abs(5)));
    %
    y = double(reshape(im2_ot,[],1));
    x = double(reshape(im1_ot,[],1));
    
    mdl = fitlm (y , x);
    
    % save results
    results.output.cols(i,1:10) = array2table([i, File.im_order.cols(i,:), output,mdl.Rsquared.Adjusted, -tformEstimate.T(6), -tformEstimate.T(3) ] ); %I switch here rows and cols because images were transposed
    
end
temp.varnames = {'round','im1','im2','error', 'diffphase','net_row_shift', 'net_col_shift', 'RsquaredAdjusted', 'tformEstimate_row', 'tformEstimate_col'};
results.output.cols.Properties.VariableNames = temp.varnames;
results.output.cols.direction(:,1) = {'cols'};



end

