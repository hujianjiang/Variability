
clear all

%% Define File and Settings
Settings.AutoComputeShift = 1; % 1 to automatically compute shift, 0 otherwise
Settings.shift_x = []; %Specify shift in x if AutoComputeShift is 0;
Settings.shift_y = []; %Specify shift in y if AutoComputeShift is 0;

Settings.ImagesOverlap = % Do images overlap? 1 yes, 0 no
Settings.

Settings.Overlap_span =10;% round(1024*0.05);  %Approximate overlap in pixels
Settings.usfac = 100; % dftregistration algorithm value
Settings.Snake = 1; %are images acquired
Settings.init_border = 100; % Border margins
Settings.n_cols = 5; % number of columns
Settings.n_rows = 5; % number of rows
Settings.min_RsquaredAdjusted = 0.9; %min value of RsquaredAdjusted test between images
Settings.pixelsize_reference = 0.83; % pixel size reference
Settings.pixelsize_experiment = 0.3225; % pixel size experiment
Settings.resize_factor = Settings.pixelsize_experiment/Settings.pixelsize_reference; %This is the resize factor to get the images with the same pixelsize. It's obtained from derived the pixelsize of the first experiments (considered reference, the ones carried out at KI) divided by the new experiments
Settings.illumination_correction_diam = 100;
Settings.illumination_correction_gaus_s = 50; % illumination correction imgaussfilter sigma
Settings.lab = 'Geiger';
Settings.im_c_correctionfactor = 0.4; %this is the correction factor for the overlap of nuclei and cell images for the gifs that are created, applied to the cell images


%% Load files and define some parameters
File.size_im_1 = 1024; % images row size
File.size_im_2 = 1024; % images col size
File.nrows = 5; % number of rows
File.ncols = 5; % number of cols
File.image_final_size = 4828; %this is the final sizes of the stitched images. If the final stitched images are bigger than this, they will be cropped. Otherwise, they will be kept with their final size. 
File.experiment_details = 'D:/Data/MULTIMOT/Original data/Geiger Lab/IG/Book1.xlsx';
File.folder_results_stitched = '1 - Stitched_images/';
File.folder_results_errors = '0 - Stitching_results/';


temp.matrix = reshape(1:Settings.n_cols*Settings.n_rows,Settings.n_cols,[])';
if Settings.Snake, temp.matrix(2:2:end,:) = temp.matrix (2:2:end,end:-1:1);end
File.im_order.rows = [reshape(temp.matrix(1:end,1:end-1)',[],1) ,reshape(temp.matrix(1:end,2:end)',[],1)];
File.im_order.cols = [reshape(temp.matrix(1:end-1,1:end),[],1) ,reshape(temp.matrix(2:end,1:end),[],1)];

File.files_list = generate_file_names(File,Settings); %this generates file names and creates directories

%% run stitcher functions
stitcher_no_overlap(File,Settings)
stitcher_no_overlap_compute_shifts(File,Settings)
stitcher_no_overlap_stitch(File, Settings);
  
  
% apply average shift values
stitcher_error_corrector(File,Settings)
create_gifs(File, Settings)


%% run create gifs after cell profiler
create_gifs_cp(File, Settings)



