function [File] = nameFiles(File,Setings);

% Build names, create output folders, save File and Settings


temp = struct();
for i=1:73
    if i<11
        temp(i).c1 = strcat('C1_000',num2str(i-1),'.tif');
        temp(i).c2 = strcat('C2_000',num2str(i-1),'.tif');
        
        results(i).nuclei = strcat('Nuc_000',num2str(i),'.jpg');
        results(i).tracking= strcat('Tracked_cells000',num2str(i),'.jpg');
        
    end
    if i>=11
        temp(i).c1 = strcat('C1_00',num2str(i-1),'.tif');
        temp(i).c2 = strcat('C2_00',num2str(i-1),'.tif');
        
        results(i).nuclei = strcat('Nuc_00',num2str(i),'.jpg');
        results(i).tracking= strcat('Tracked_cells00',num2str(i),'.jpg');
    end
end

File.Originaldata.FileNames = temp;
File.Results.FileNames = results












for i=1:File.Numberoffiles
    if i<10
        File.Name.Cells(i).cat = strcat(File.Path.Cells.Data,File.Name.Cells.prefix,'0',num2str(i),File.Name.Cells.sufix);
    end
    if i>=10
        File.Name.Cells(i).cat = strcat(File.Path.Cells.Data,File.Name.Cells.prefix,num2str(i),File.Name.Cells.sufix);
    end
end
for i=1:File.Numberoffiles
    if i<10
        File.Name.Nuclei(i).cat = strcat(File.Path.Nuclei.Data,File.Name.Nuclei.prefix,'0',num2str(i),File.Name.Nuclei.sufix);
    end
    if i>=10
        File.Name.Nuclei(i).cat = strcat(File.Path.Nuclei.Data,File.Name.Nuclei.prefix,num2str(i),File.Name.Nuclei.sufix);
    end
end
for i=1:File.Numberoffiles
    if i<10
        File.Name.Outlines(i).cat = strcat(File.Path.Outlines,File.Name.Outlines.prefix,'0',num2str(i),File.Name.Outlines.sufix);
    end
    if i>=10
        File.Name.Outlines(i).cat = strcat(File.Path.Outlines,File.Name.Outlines.prefix,num2str(i),File.Name.Outlines.sufix);
    end
end

cd(File.Path.Stagepos(1,:))

mkdir('Images\')
save File File
save Settings Settings





