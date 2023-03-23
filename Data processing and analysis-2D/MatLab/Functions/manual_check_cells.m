function manual_check_cells(Files)

alldata = [];
for experiment =1:size(Files.paths,1),
    
    a = csvread(char(Files.paths(experiment,2))); % read from R generated csv
    ob_i = unique(a(:,1)); % get cells identifiers
    
    for ob_n=1:size(ob_i,1),
        
        im = imread(char(strcat(Files.paths(experiment,1), '/Images/All_timepoints_cell',num2str(ob_i(ob_n)),'.jpg')));
        figure('units','normalized','outerposition',[0 0 1 1])
        imagesc(im), axis image
        ui = input('Which cells are not ok? \n(If more than one, enter them in square brackets (example: [1 2 3]). 0 = exclude all. When done / if everything is ok, hit Enter)\n');
        
        if isempty(ui)==0,
            
            if ui == 0,
                ui = 1:73
            end
            
            alldata = [alldata; [linspace(experiment, experiment, size(ui,2));...
                linspace(ob_i(ob_n), ob_i(ob_n), size(ui,2));...
                ui]'];
            
        end
        close all
        save alldata alldata
        ob_i(ob_n)
        experiment
    end
    
    
end
csvwrite('cells_to_exclude.csv',alldata)


%