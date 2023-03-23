dat = readtable('C:/Users/xavser/Desktop/temp.csv');

temp.folder= {'1 - Decompressed_images\'; '0 - Preprocessing\3 - Final_images\';'1 - Images\'};
temp.lab  = {'L1'; 'L2';'L3'};
acc_tab = table(temp.lab, temp.folder);
[a, b] = ismember(dat.lab, acc_tab.Var1)

dat.folder_extra = acc_tab.Var2(b);

count_exc = 1;
count_inc = 1;

for i = 1:size(dat,1)
    try
        filename1 = ([char(dat.folder(i)) char(dat.folder_extra(i)) char(dat.stagepos(i)) 'c1\C1_T' num2str(dat.ImageNumber(i),'%02.f') '.tif']);
        filename2 = ([char(dat.folder(i)) char(dat.folder_extra(i)) char(dat.stagepos(i)) 'c2\C2_T' num2str(dat.ImageNumber(i),'%02.f') '.tif']);
        
        filename_m1 = ([char(dat.folder(i)) '2 - CellProfiler results\CellMasks\CellMasks_' num2str(dat.image_number_old(i),'%04.f') '.tif']);
        
        if char(dat.lab(i)) == 'L1'
            filename1 = ([char(dat.folder(i)) char(dat.folder_extra(i)) char(dat.stagepos(i)) 'c1\C1_' num2str(dat.ImageNumber(i),'%04.f') '.tif']);
            filename2 = ([char(dat.folder(i)) char(dat.folder_extra(i)) char(dat.stagepos(i)) 'c2\C2_' num2str(dat.ImageNumber(i),'%04.f') '.tif']);
        end
        
        im1 = imread(filename1);
        im2 = imread(filename2);
        im_m = imread(filename_m1);
        
        %find pixels containing cell
        [i_idx, j_idx] = find(im_m == dat.ObjectNumber(i));
        im_m2 = zeros(size(im_m));
        im_m2(find(im_m == dat.ObjectNumber(i))) = 1;
        
        im_b = edge((im2bw(im_m2)));
        
        
        %filter if necessary
        
        if char(dat.lab(i)) == 'L3'
            im1 = imgaussfilt(im1,2);
            im2 = imgaussfilt(im2,2);
        end
        
        %crop around these pixels
        
        
        im_1c = im1(min(i_idx)-20:max(i_idx)+20, min(j_idx)-20:max(j_idx)+20);
        im_2c = im2(min(i_idx)-20:max(i_idx)+20, min(j_idx)-20:max(j_idx)+20);
        im_b2 = im_b(min(i_idx)-20:max(i_idx)+20, min(j_idx)-20:max(j_idx)+20);
        
        im_1c = im_1c - median(median(im_1c));
        im_2c = im_2c - median(median(im_2c));
        
        im_all = uint8(cat(3,im_1c,im_2c,zeros(size(im_1c))));
        
        im_mask = uint8(cat(3,im_b2, im_b2, im_b2));
        
        
        im_all = imadd(im_all, im_mask.*255);
        
        %save for later plot
        
        im(i).all = im_all;
        
        if isequal(dat.inc(i) , cellstr('TRUE'))
            im_inc(count_inc).all = im_all;
            count_inc = count_inc+1;
        end
        
        if isequal(dat.inc(i) , cellstr('FALSE'))
            im_exc(count_exc).all = im_all;
            count_exc = count_exc+1;
        end
    end
    i
end


%% plot selected
figure
subplot = @(m,n,p) subtightplot (m, n, p, [0.001 0.001], [0.001 0.001], [0.001 0.001]);

for i = 1:20,
    subplot(3,7,i),
    imagesc(im_inc(i).all), axis image, axis off,
end
set(gcf,'color','k');


figure
subplot = @(m,n,p) subtightplot (m, n, p, [0.001 0.001], [0.001 0.001], [0.001 0.001]);

for i = 1:20,
    subplot(3,7,i),
    imagesc(im_exc(i).all), axis image, axis off,
end
set(gcf,'color','k');

%% plot randomm spaning all

figure
subplot = @(m,n,p) subtightplot (m, n, p, [0.001 0.001], [0.001 0.001], [0.001 0.001]);

index = reshape(1:100, 20, 5).'; %this is accomodate to subplot order

for i = 1:size(im,2),
    %     subplot(20,5,i),
    subplot(5,20,index(i)),
    
    imagesc(im(i).all), axis image, axis off,
    title(['i_' num2str(i)]);
    pause(0.05);
end
set(gcf,'color','k');

