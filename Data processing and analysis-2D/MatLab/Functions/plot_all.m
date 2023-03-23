function plot_all(Files, Settings)


for experiment = 1:size(Files.paths,1),
    
    
%     a = csvread(char(Files.paths(experiment,2))); % read from R generated csv
    a = xlsread(char(Files.paths(experiment,2))); % read from R generated csv

    ob_i = unique(a(:,1)); % get cells identifiers
    
    maxy = ceil(length(ob_i)/10)*(Settings.PixelNumber+20)+20; %20 = frame and top padding
    [y,x] = meshgrid(20:220:maxy, 20:220:2000);
    
    im_3_all = zeros(2540,2540,3,73);
    for k=1:73, im_3_all(:,:,:,k) = imread(char(strcat(Files.paths(experiment,1), '/Images/Image_overlap_t',num2str(k),'.jpg'))); end
    
    for timepoint=1:Files.number_of_images_each,
        % define white background
        im_all = uint8(zeros(maxy,2220,3));
        im_all = im_all+255;
        
%         im_n = imread(char(Files.Original_files(timepoint,(experiment-1)*1+experiment)));
%         im_c = imread(char(Files.Original_files(timepoint,(experiment-1)*1+experiment+1)));
%         im_o = imread(char(strcat(Files.paths(experiment,1),'/Outlines/Outlines_',num2str(timepoint),'.jpg')));
%         
         im_3_tp = im_3_all(:,:,:,timepoint);

            
        for ob_n=1:size(ob_i,1),
            
            at = a(find(a(:,1)==ob_i(ob_n)),:);
            if any(at(:,2)==timepoint),
                
                xpos = at(1,3);
                ypos = at(1,4);
                
                % define cropping region
                ypos_i = round(max(1,ypos-Settings.PixelNumber));
                ypos_f = round(min(ypos+Settings.PixelNumber,size(im_3_tp,1)));
                xpos_i =  round(max(1,xpos-Settings.PixelNumber));
                xpos_f = round(min(xpos+Settings.PixelNumber,size(im_3_tp,2)));
                
                %                 if ob_n == 43,
                %                     asdf = 1;
                %                 end
                
                % define track position and save them to plot afterwards
                x2 = at(1:find(at(:,2)==timepoint),3); %vector of x positions that will be plotted
                y2 = at(1:find(at(:,2)==timepoint),4); %vector of y positions that will be plotted
                
                x_offset = 0;
                y_offset = 0;
                
                if xpos<200, x_offset = xpos - Settings.PixelNumber; end  % apply x offset in case they are close to the left boundary
                if ypos<200, y_offset = ypos - Settings.PixelNumber; end  % apply y offset in case they are close to the top boundary
                
                x2 = Settings.PixelNumber + x2-x2(1) + x_offset; % mid position + changes from first position + offset
                y2 = Settings.PixelNumber + y2-y2(1) + y_offset; % mid position + changes from first position + offset
                
%                track_all = struct();
                track_all(ob_n).x2 = x2;
                track_all(ob_n).y2 = y2;
                
                % crop region of interest
                im_3 = im_3_tp(ypos_i:ypos_f,xpos_i:xpos_f,:);
%                 im_o_t  = im_o(ypos_i:ypos_f,xpos_i:xpos_f,:);
%                 im_n_t = im_n(ypos_i:ypos_f,xpos_i:xpos_f);
%                 im_c_t = im_c(ypos_i:ypos_f,xpos_i:xpos_f);
%                 
%                 im_t = uint8(cat(3,im_n_t,im_c_t,zeros(size(im_n_t))));
%                 im_3 = (im_t+uint8(im_o_t));
%                 
                
                
                % paste to im_all
                im_t = im_3;
                
                
                im_t = imresize(im_t,0.5);
                
                if (size(im_t,1)~=202 || size(im_t,2) ~= 202),
                    im_t2 = uint8(zeros(202,202,3));
                    im_t2(1:size(im_t,1),1:size(im_t,2),:) = im_t;
                    im_t = im_t2;
                end
                
                im_all(y(ob_n):y(ob_n)+202-1,x(ob_n):x(ob_n)+202-1,:) = im_t;
                
                
            end
        end
        
        figh1 = figure;
        imagesc(im_all), axis image,
        truesize
        axis([0 size(im_all,2) 0 size(im_all,1)]) %keep axis
        
        for ob_n=1:size(ob_i,1),
            
            at = a(find(a(:,1)==ob_i(ob_n)),:);
            
            if any(at(:,2)==timepoint),
                hold on,
                x_temp = reshape(x,1,[]);
                y_temp = reshape(y,1,[]);
                
                x_temp = x_temp(ob_n);
                y_temp = y_temp(ob_n);
                
                x2 = (track_all(ob_n).x2)/2+x_temp;
                y2 = (track_all(ob_n).y2)/2+y_temp;
                
                plot(x2,y2,'LineWidth',1, 'color', 'w');
                 
                text(x_temp,y_temp+15,num2str(ob_i(ob_n)), 'color' , 'w')
                
            end
            
        timepoint
        experiment
        
        end
        set(gca,'xtick',[])
        set(gca,'xticklabel',[])
        
        set(gca,'ytick',[])
        set(gca,'yticklabel',[])
                
        file = char(strcat(Files.paths(experiment,1), '/Images/All_t',num2str(timepoint),'.jpg'));
        export_fig(file,'-native')
        
        close all;
        
    end
end

