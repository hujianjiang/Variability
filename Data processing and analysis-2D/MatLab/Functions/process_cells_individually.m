function process_cells_individually(Files, Settings)


%% Load exper
for experiment = 1:size(Files.paths,1),
    
    a = csvread(char(Files.paths(experiment,2))); % read from R generated csv
    ob_i = unique(a(:,1)); % get cells identifiers
    
    for k=1:Files.number_of_images_each, im_3_all(:,:,:,k) = imread(char(strcat(Files.paths(experiment,1), '/Images/Image_overlap_t',num2str(k),'.jpg'))); end
    
    
    for ob_n=1:size(ob_i,1),
        
        [x,y] = meshgrid(10:Settings.PixelNumber_percell*2+10:(Settings.PixelNumber_percell*2+10)*9+10, ...
            10:Settings.PixelNumber_percell*2+10:(Settings.PixelNumber_percell*2+9)*10+10);
        
        im_all = uint8(zeros((Settings.PixelNumber_percell*2+10)*9+10,...
            (Settings.PixelNumber_percell*2+10)*10+10,...
            3));
        im_all = im_all+255;
        at = a(find(a(:,1)==ob_i(ob_n)),:);
        
        for timepoint=1:Files.number_of_images_each,
            
            if any(at(:,2)==timepoint),
                im_3 = im_3_all(:,:,:,timepoint);
                
                xpos =at(find(at(:,2)==timepoint),3);
                ypos = at(find(at(:,2)==timepoint),4);
                
                ypos_i = round(max(1,ypos-Settings.PixelNumber_percell));
                ypos_f = round(min(ypos+Settings.PixelNumber_percell,size(im_3,1)));
                xpos_i =  round(max(1,xpos-Settings.PixelNumber_percell));
                xpos_f = round(min(xpos+Settings.PixelNumber_percell,size(im_3,2)));
                
                
                im_3  = im_3(ypos_i:ypos_f,xpos_i:xpos_f,:);
                
                %                 imagesc(im_3), axis image,
                %                 truesize,
                %                 text(10,10,num2str(timepoint), 'color','w');
                %                 f = getframe(gca);
                %                 im = frame2im(f);
                if size(im_3,1) ~= Settings.PixelNumber_percell*2+2 ||  size(im_3,2) ~= Settings.PixelNumber_percell*2+2,
                    im_t = uint8(zeros(Settings.PixelNumber_percell*2+2,Settings.PixelNumber_percell*2+2,3));
                    im_t(1:size(im_3,1),1:size(im_3,2),:) = im_3;
                    im_3 = im_t;
                    im_3 = im_3(1:Settings.PixelNumber_percell*2+2 ,1:Settings.PixelNumber_percell*2+2,:);
                    
                end
                
                close();
                % paste to im_all
                
                y_paste = reshape(y',1,[]);
                y_paste = y_paste(timepoint);
                x_paste = reshape(x',1,[]);
                x_paste = x_paste(timepoint);
                
                im_all(y_paste:y_paste+(Settings.PixelNumber_percell*2),...
                    x_paste:x_paste+(Settings.PixelNumber_percell*2),:) = im_3(1:end-1, 1:end-1,:);
                
                %                 text(x(timepoint),y(timepoint)+5,num2str(timepoint), 'color' , 'w')
                
                %                 axis image,
                %                 set(gca,'xtick',[]);
                %                 set(gca,'ytick',[]);
                
                
            end
            
            
        end
        
        x_text = reshape(x',1,[]);
        x_text = x_text(1:Files.number_of_images_each);
        y_text = reshape(y',1,[]);
        y_text = y_text(1:Files.number_of_images_each);
        z_text = 1:73;
        
        figure, imagesc(im_all),
        axis image,
        truesize
        for k=1:Files.number_of_images_each
            text(x_text(k)+8,y_text(k)+8,num2str(z_text(k)),'color' , 'w')
        end
        
        set(gca,'xtick',[])
        set(gca,'xticklabel',[])
        
        set(gca,'ytick',[])
        set(gca,'yticklabel',[])
        
        
        file = char(strcat(Files.paths(experiment,1), '/Images/All_timepoints_cell',num2str(ob_i(ob_n)),'.jpg'));
        export_fig(file,'-native')
        
        ob_i(ob_n)
        experiment
        
    end
end
