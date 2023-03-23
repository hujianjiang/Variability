clear all

%% to do

%save Settings
%save results



%% Define File and Settings
Settings.AutoComputeShift = 1; % 1 to automatically compute shift, 0 otherwise. This needs to be developed in define_shifts, but should be relatively easy to do.
Settings.shift_x = []; %Specify shift in x if AutoComputeShift is 0;
Settings.shift_y = []; %Specify shift in y if AutoComputeShift is 0;



Settings.Overlap_span = 51;% round(1024*0.05);  %Approximate overlap in pixels if images overlap (defined in "book1.xlsx document)
Settings.no_overlap_shift_compute = 50;% round(1024*0.05);  %Number of pixels used for shift compute if images do not overlap 
Settings.shift_compute_interval = 50; %shift between sets of experiments to compute shifts, used in "define_shifts".

Settings.usfac = 100; % dftregistration algorithm value
Settings.Snake = 1; %are images acquired in "snake" form ? (
Settings.init_border = 100; % Border margins
Settings.n_cols = 5; % number of columns
Settings.n_rows = 5; % number of rows
Settings.min_RsquaredAdjusted = 0.9; %min value of RsquaredAdjusted test between images
Settings.pixelsize_reference = 0.8260495552435883% 0.83; % pixel size reference
Settings.pixelsize_experiment = 0.3225; % pixel size experiment
Settings.resize_factor = Settings.pixelsize_experiment/Settings.pixelsize_reference; %This is the resize factor to get the images with the same pixelsize. It's obtained from derived the pixelsize of the first experiments (considered reference, the ones carried out at KI) divided by the new experiments
Settings.illumination_correction_diam = 100;
Settings.illumination_correction_gaus_s = 50; % illumination correction imgaussfilter sigma
Settings.illumination_correction_substract_mode_val = 1; %this is also part of the illumination correction, but performed after generating mosaic. It will substract the mode val of the images *3 to the images to completly get rid of background. For comparison, it will also save the original images without substracting mode val. Need to finalize code if val != 1.
Settings.illumination_correction_substract_mode_step_control = 10;
Settings.lab = 'Geiger';
Settings.im_c_correctionfactor = 0.4; %this is the correction factor for the overlap of nuclei and cell images for the gifs that are created, applied to the cell images
Settings.image_final_size = 1870; %this is the final sizes of the stitched images. If the final stitched images are bigger than this, they will be cropped. Otherwise, they will be kept with their final size. 


%% Load files and define some parameters
File.size_im_1 = 1024; % images row size
File.size_im_2 = 1024; % images col size
File.nrows = 5; % number of rows
File.ncols = 5; % number of cols

File.experiment_details = '\\abbe\user1\xavser\MULTIMOT\2D cell migration\Original data\Geiger Lab\IG\Book1.xlsx';
File.folder_results = '0 - Preprocessing/';
File.folder_resized = '1 - resized/';
File.folder_illumination_corrected = '2 - il_cor/';
File.folder_final = '1 - Images/';
File.folder_final_control = '4 - Final_images_control/';
File.folder_gifs = '5 - GIFs/';

temp.matrix = reshape(1:Settings.n_cols*Settings.n_rows,Settings.n_cols,[])';
if Settings.Snake, temp.matrix(2:2:end,:) = temp.matrix (2:2:end,end:-1:1);end
File.im_order.rows = [reshape(temp.matrix(1:end,1:end-1)',[],1) ,reshape(temp.matrix(1:end,2:end)',[],1)];
File.im_order.cols = [reshape(temp.matrix(1:end-1,1:end),[],1) ,reshape(temp.matrix(2:end,1:end),[],1)];

File.files_list = generate_file_names(File,Settings); %this generates file names and creates directories
make_folders(File);

File.files_list = File.files_list(File.files_list.person_n == 1 & File.files_list.experiment_n == 1,:)

%% run functions
File = resize_and_sort(File, Settings); %resizes images and saves them to "1 - resized" folder in a logical manner (multi tif, in independent folders xy1c1 xy1c2 etc). It also saves File in File.files_list.folder_processed_Person(1)) folder
correct_illumination_save(File, Settings); % corrects illumination and saves to 2 - Illumination corrected folder
results = define_shifts(File,Settings); %Computes shifts or assigns one if already stated in Settings.Autocompute Shift
results = Process_positions( File, Settings,temp, results); %Process average x and y positions for stitching
generate_mosaic(File, Settings,results) %generates mosaic images
create_gifs( File, Settings)% Generate gifs to quickly assess results

