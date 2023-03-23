function merge_images(Files, Settings)

parfor experiment =1:size(Files.paths,1),
    for timepoint = 1:Files.number_of_images_each
        im_n = imread(char(Files.Original_files(timepoint,(experiment-1)*1+experiment)));
        im_c = imread(char(Files.Original_files(timepoint,(experiment-1)*1+experiment+1)));
        im_o = imread(char(strcat(Files.paths(experiment,1),'/Outlines/Outlines_',num2str(timepoint),'.jpg')));
        
        im_t = uint8(cat(3,im_n,im_c,zeros(size(im_n))));
        im_3 = (im_t+uint8(im_o));
        
        file = char(strcat(Files.paths(experiment,1), '/Images/Image_overlap_t',num2str(timepoint),'.jpg'));
        imwrite(im_3, file, 'jpg');
        timepoint
        experiment
    end
    
end

end
