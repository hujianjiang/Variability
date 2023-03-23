% check files

cd 'D:\Data\MULTIMOT\Processed\XSP\Geiger_lab'
files = dir;

fin_size = table();

for k=3:15
    for xy = 1:6
        
        temp = table( size(dir([files(k).folder '\' files(k).name '\1 - Stitched_images\xy' num2str(xy), 'c1\']),1), ...
            size(dir([files(k).folder '\' files(k).name '\1 - Stitched_images\xy' num2str(xy), 'c2\']),1));
        
        temp.Properties.VariableNames = {'size_c1', 'size_c2'};
        
        temp.folder = {[files(k).folder '\' files(k).name '\1 - Stitched_images\xy' num2str(xy)]};
        
        fin_size = [fin_size; temp];
        
        
    end
end





% copy paste files 

cd 'D:\Data\MULTIMOT\Processed\XSP\Geiger_lab'

files = dir;

for k = 12:10% size(files,1)
    for k2 = 1:6,
        
        
        mkdir(['C:/Users/xavser/Desktop/transfer/' files(k).name]);
        copyfile ( [files(k).folder '\' files(k).name '\2 - CellProfiler results\all_images_well',num2str(k2),'.gif'],...
            ['C:/Users/xavser/Desktop/transfer/' files(k).name '/all_images_well' num2str(k2) '.gif']);
    
    
    
    end
        
end
