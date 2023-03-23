clearvars

File.folder_root = '\\bionut2128\MULTIMOT\Processed\XSP\L1\';
File.experiments = dir(File.folder_root);
File.experiments = {File.experiments.name}';
File.experiments =   File.experiments(3:11);%% File.experiments([4,5,6,7,9,10,11,12,14,15,16]); %L3: File.experiments([4:13]); %L2: File.experiments([4,5,6,7,9,10,11,12,14,15,16]);%L1: File.experiments(3:11);

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

folder = 1; %for folder = 1:size(File.experiments,1)%1,

folder_base = [char(File.folder_root) char(File.experiments(folder)) '\']
cp_results_c = readtable([char(File.folder_root) char(File.experiments(folder)) '\' File.folder_resultsFile_Cells]);
cp_results_n = readtable([char(File.folder_root) char(File.experiments(folder)) '\' File.folder_resultsFile_Nuclei]);

stagepos = 1; %for stagepos = 1:6

% this is to combine the two sources of information. TrackObjects_Label is only in nucleus dataset
cp_results_sp_c = cp_results_c(strcmp(cp_results_c.Metadata_stagepos, File.stagepos(stagepos)),:);
cp_results_sp_n = cp_results_n(strcmp(cp_results_n.Metadata_stagepos, File.stagepos(stagepos)),:);

A = table(cp_results_sp_n.ObjectNumber, cp_results_sp_n.ImageNumber);
B = table(cp_results_sp_c.ObjectNumber, cp_results_sp_c.ImageNumber);

[~, idx] = ismember(A,B);

cp_results_sp_c.TrackObjects_Label = cp_results_sp_n.TrackObjects_Label_35(idx);

Lab_descriptor = File.folder_root(1,size(File.folder_root,2)-1:size(File.folder_root,2)-1);
User_descriptor = folder_base(1,size(folder_base,2)-3:size(folder_base,2)-3);
Experiment_descriptor = folder_base(1,size(folder_base,2)-1:size(folder_base,2)-1);



for timepoint_n = 1:73
    
    ['experiment_'  char(File.experiments(folder)) ' stagepos_' char(File.stagepos(stagepos)) ' timepoint_' num2str(timepoint_n)]
    
    filename_im1 = [folder_base File.folder_Masks 'CellMasks_0' num2str(timepoint_n+ File.nfiles*(stagepos-1),                    '%03.f'),  '.tif'];
    
    filename_cell_im1 = [folder_base '1 - Images\C1\Chan_1\C1_T' num2str(timepoint_n+ File.nfiles*(stagepos-1), '%02.f'), '.tif'];
    filename_cell_im2 = [folder_base '1 - Images\C1\Chan_2\C2_T' num2str(timepoint_n+ File.nfiles*(stagepos-1), '%02.f'), '.tif'];
    
    im{timepoint_n}.im_1 = imread(filename_im1);
    
    im{timepoint_n}.im_c1 = imread(filename_cell_im1);
    im{timepoint_n}.im_c2 = imread(filename_cell_im2);
end

for k2 = 17:17;%         for k2 =16:30
    
    cell_list = unique(cp_results_sp_c.TrackObjects_Label);
    cell_list = cell_list(~isnan(cell_list));
    cell_list = cell_list(k2);
    
    cp_results_sp_tcell_n_c = cp_results_sp_c(cp_results_sp_c.TrackObjects_Label==cell_list,:);
    count = 1;
    clear final_im
    clear val
    cp_results_sp_tcell_n_c.AreaShape_Center_X2 =  medfilt1(cp_results_sp_tcell_n_c.AreaShape_Center_X,3);
    cp_results_sp_tcell_n_c.AreaShape_Center_Y2 =  medfilt1(cp_results_sp_tcell_n_c.AreaShape_Center_Y,3);
    
    for timepoint_n = 1:73
        try
            cp_results_sp_tcell_n_c_t = cp_results_sp_tcell_n_c(cp_results_sp_tcell_n_c.ImageNumber == timepoint_n,:);
            cp_results_sp_tcell_n_c_t = cp_results_sp_tcell_n_c_t(1,:);
            
            
            im_1 = im{timepoint_n}.im_1;
            
            im_c1 = im{timepoint_n}.im_c1;
            im_c2 = im{timepoint_n}.im_c2;
            
            val1 = im_1(cp_results_sp_tcell_n_c_t.AreaShape_Center_Y,cp_results_sp_tcell_n_c_t .AreaShape_Center_X)
            
            
            % val3 = im_3(cp_results_sp_tpl_c.AreaShape_Center_Y,cp_results_sp_tpl_c.AreaShape_Center_X);
            
            % crop
            % [i1,j1] = find(im_1 == val1);
            [i1,j1] = find(im_1 == val1);
            % [i3,j3] = find(im_3 == val3);
            
            val(timepoint_n).min_i = min(i1);
            val(timepoint_n).max_i = max(i1);
            val(timepoint_n).min_j = min(j1);
            val(timepoint_n).max_j = max(j1);
            val(timepoint_n).dif_i = max(i1) - min(i1);
            val(timepoint_n).dif_j = max(j1) - min(j1);
            
        end
    end
    
    for timepoint_n = 1:73
        try
            
            ind_table = struct2table(val);
            max_val = max([ind_table.dif_i ; ind_table.dif_j])
            
            cp_results_sp_tcell_n_c_t = cp_results_sp_tcell_n_c(cp_results_sp_tcell_n_c.ImageNumber == timepoint_n,:);
            cp_results_sp_tcell_n_c_t = cp_results_sp_tcell_n_c_t(1,:);
            
            
            im_1 = im{timepoint_n}.im_1;
            
            im_c1 = im{timepoint_n}.im_c1;
            im_c2 = im{timepoint_n}.im_c2;
            
            im_rgba = zeros([size(im_c1), 3]);
            im_rgba(:,:,1) = mat2gray(im_c1);
            im_rgba(:,:,2) = mat2gray(im_c2);
            
            %                 figh = figure, imagesc(im_rgba), axis image,
            %                 hold on, plot(cp_results_sp_tcell_n_c_t.AreaShape_Center_X2,cp_results_sp_tcell_n_c_t .AreaShape_Center_Y, 'wo')
            
            val1 = im_1(cp_results_sp_tcell_n_c_t.AreaShape_Center_Y,cp_results_sp_tcell_n_c_t .AreaShape_Center_X)
            
            
            min_i = cp_results_sp_tcell_n_c_t.AreaShape_Center_Y2-20 - round(max_val/2);
            max_i = cp_results_sp_tcell_n_c_t.AreaShape_Center_Y2+20 + round(max_val/2);
            min_j = cp_results_sp_tcell_n_c_t.AreaShape_Center_X2-20 - round(max_val/2);
            max_j = cp_results_sp_tcell_n_c_t.AreaShape_Center_X2+20 + round(max_val/2);
            
            im1t_c = im_1(min_i:max_i, min_j:max_j);
            im_c1_c = im_c1(min_i:max_i, min_j:max_j);
            im_c2_c = im_c2(min_i:max_i, min_j:max_j);
            
            a = bwperim(im1t_c);
            im_rgb = zeros([size(im_c1_c), 3]);
            im_rgb(:,:,1) = mat2gray(im_c1_c);
            im_rgb(:,:,2) = mat2gray(im_c2_c);
            im_rgb(find(a)) = 1;
            
            
            
            
            final_im{count}.im_rgb=im_rgb;
        end
        count = count+1;
    end
    
    
    figure,            title(num2str(k2))
    
    subplot = @(m,n,p) subtightplot (m, n, p, [0.001 0.001], [0.001 0.001], [0.001 0.001]);
    for timepoint = 1:73
        try
            subplot(9,9,timepoint), imagesc(final_im{timepoint}.im_rgb)
            axis off, axis image
        end
    end
    
    
end
