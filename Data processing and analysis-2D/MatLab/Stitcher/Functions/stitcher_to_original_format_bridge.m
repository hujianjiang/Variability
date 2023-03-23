clear all


experiment = 'C:\Users\xavser\Desktop\Geiger_Lab\Processed\OP\OP_Multimot_4\';

cd ([experiment '1 - tif\cropped']);

mkdir([experiment '1 - Decompressed images'])

files = dir;
files = struct2table(dir);
files = files(3:end,:);

for k=1:size(files.name,1),
    files.C(k) = extractBetween(files.name{k},'im','_we');
    files.stagepos(k) = extractBetween(files.name{k},'well','_time');
%     files.stagepos(k) = {files.name{k}(9:11)};
    files.timepoint(k) = extractBetween(files.name{k},'time','.tif');
end

temp.wells = table(unique(files.stagepos), {'xy1' ; 'xy3'; 'xy5'; 'xy2'; 'xy4'; 'xy6'}); % check here control / drug and xy positions in original images
temp.C = table(unique(files.C), [2 ; 1]);

[tf, loc] = ismember(files.stagepos, temp.wells.Var1);
files.stagepos_c = temp.wells.Var2(loc);

[tf, loc] = ismember(files.C, temp.C.Var1);
files.C_c = temp.C.Var2(loc);

for k = 1:size(files.name,1),
        files.directory(k) = {horzcat(char(experiment ), '1 - Decompressed images\'  , char(files.stagepos_c(k)), 'c', num2str(files.C_c(k)), '\')};
        files.path(k) = {horzcat(char(experiment ), '1 - Decompressed images\'  , char(files.stagepos_c(k)), 'c', num2str(files.C_c(k)), '\C', num2str(files.C_c(k)), '_00', num2str(str2num(char(files.timepoint(k)))-1,'%02.f'),'.tif')};
end

temp.direcotries_unique = unique(files.directory)
for k= 1: size(temp.direcotries_unique,1)
    mkdir(char(temp.direcotries_unique(k)))
end

for k=1:size(files.name,1),
    copyfile(char(files.name(k)), char(files.path(k)))
    k
end

