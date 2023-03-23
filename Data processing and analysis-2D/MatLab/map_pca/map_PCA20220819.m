clearvars

%% Settings
ROI_rad = 100;

% %% adpath functions to path
% % addpath ('C:/Users/xavser/Box Sync/Mech_CCM/Processed/src/2018_01_04 Phenotypic Profiling/Phenotypic profiling/MatLab functions');
% addpath ('L:\DataX\MULTIMOT\Paper\src20220308\src - Copy\MatLab\Functions\');

%% load data

path = 'L:\DataX\MULTIMOT\Paper\src20220308\src - Copy\MatLab\map_pca\results20220819\';
mkdir(path)
% dir_files = dir;
% pos2 = gunzip('L:\MULTIMOT\Paper\src20220308\src - Copy\R\v2\Figures paper\MatLab_transfer_PCA_map.csv.gz');
pos2 = readtable('L:\DataX\MULTIMOT\Paper\src20220308\src - Copy\R\v2\Figures paper with datv2\MatLab_transfer_PCA_map_grid.csv', 'Delimiter',',' );
% delete('C:\Users\xavser\Box Sync\MULTIMOT\2D cell migration\cache/MatLab_transfer_PCA_map.csv');

cell_number = size(pos2,1);

im_all = cell(cell_number,1);

for k = 1:cell_number %size(pos2,1),
%     im = imread(char(pos2.path_to_file(k)));
    im = imread(['\\abbe\user1\xavser\MULTIMOT\2D cell migration\' pos2.path_to_file{k}(38:end)]);
%     val1 = max(im(max(1,pos2.Cells_Center_Y(k)-ROI_rad) : min(size(im,1),pos2.Cells_Center_Y(k)+ROI_rad),...
%         max(1,pos2.Cells_Center_X(k)-ROI_rad) : min(size(im,2),pos2.Cells_Center_X(k)+ROI_rad)),[],'all');
%     
    
    %     im2 = imread(char(pos2.Path_to_original_data(k)));
    
    im = im(max(1,pos2.Cells_Center_Y(k)-ROI_rad) : min(size(im,1),pos2.Cells_Center_Y(k)+ROI_rad),...
        max(1,pos2.Cells_Center_X(k)-ROI_rad) : min(size(im,2),pos2.Cells_Center_X(k)+ROI_rad));
    
    val1 = pos2.ObjectNumber(k);
    im(find(im~=val1)) = 0;
    im(find(im==val1)) = 1;
       
    im = imbinarize(im);
    im_all{k} =  imdilate(im,strel('disk',2));
    %     figure, imagesc(im)
end


%% plot cell masks in the PCA space

% load&plot all PCA data
PCA = readtable('L:\DataX\MULTIMOT\Paper\src20220308\src - Copy\R\v2\Figures paper with datv2\MatLab_transfer_PCA_data_all.csv', 'Delimiter',',' );

mag = 150;

figure(1)
set(gcf,'color','w');

PCAcolor = 'gray';
plot(PCA.Comp_1*mag,PCA.Comp_2*mag,'.','MarkerEdgeColor',rgb(PCAcolor),'MarkerSize',10)

% plot cell masks
MaskColor = 'magenta';
MaskColorArray = rgb(MaskColor);

for i = 1 : size(pos2,1)
    % Make a truecolor sub mask image.
    subMask = im_all{i};
    Mask = cat(3, ones(size(subMask)).*MaskColorArray(1), ones(size(subMask)).*MaskColorArray(2), ones(size(subMask)).*MaskColorArray(3));
    temp = size(subMask)./2;
    hold on
    h = imagesc(pos2.Comp_1(i)*mag-temp(1),pos2.Comp_2(i)*mag-temp(2),Mask);
    hold off
    % Use sub image as the AlphaData for the solid mask image.
    set(h, 'AlphaData', subMask)
end
% for i = 1 : size(pos2,1)
%     hold on
%     plot(pos2.Comp_1(i)*mag,pos2.Comp_2(i)*mag,'g.')
% end
axis([-1000 3000 -2000 1500])
% axis off
xticklabels([])
yticklabels([])
% xlabel('Comp.1(41.4%)')
% ylabel('Comp.2(23.6%)')
xlabel('Comp.1')
ylabel('Comp.2')
set(gca,'FontWeight','bold','FontSize',40)
figure(1);set(gcf,'position',[0 0 2000 1500]);export_fig(gcf,[path 'PCAwithCellMask.png'], '-r600');
close(1)


% plot cell mask only
figure(2)
set(gcf,'color','w');

hold on
for i = 1 : size(pos2,1)
    imagesc(pos2.Comp_1(i)*mag,pos2.Comp_2(i)*mag,im_all{i});
end
figure(2);set(gcf,'position',[0 0 2000 1500]);export_fig(gcf,[path 'CellMaskOnlyOnPCASpace.png'], '-r600');
close(2)


%% plot individual cell trajectory in the PCA space

figure(3)
set(gcf,'color','w');

PCAcolor = 'gray';
plot(PCA.Comp_1,PCA.Comp_2,'.','MarkerEdgeColor',rgb(PCAcolor),'MarkerSize',10)

% plot cell trajectory
TrajColor = {'orange','blue'};

hold on

Cells = [5,35];
for j = 1 : length(Cells)
    i = Cells(j);
    temp = PCA(PCA.Lab==string(pos2.Lab(i))&PCA.Person==string(pos2.Person(i))&PCA.Experiment==string(pos2.Experiment(i))&PCA.Technical_replicate==string(pos2.Technical_replicate(i))&PCA.Cell_id==double(pos2.Cell_id(i)),:);
    plot(temp.Comp_1,temp.Comp_2,'.','MarkerEdgeColor',rgb(TrajColor{j}),'MarkerSize',20);
end
axis([-1000 3000 -2000 1500]./mag)
% axis off
xticklabels([])
yticklabels([])
% xlabel('Comp.1(41.4%)')
% ylabel('Comp.2(23.6%)')
xlabel('Comp.1')
ylabel('Comp.2')
set(gca,'FontWeight','bold','FontSize',40)
figure(3);set(gcf,'position',[0 0 2000 1500]);export_fig(gcf,[path 'PCAwithCellTrajectory.png'], '-r600');
close(3)




%% 
% % merge figures
% first_fig = figure(2);
%  second_fig = figure(1);
%  first_ax = findobj(first_fig, 'type', 'axes');
%  second_ax = findobj(second_fig, 'type', 'axes');
%  if length(first_ax) ~= 1 || length(second_ax) ~= 1
%     error('this code requires the two figures to have exactly one axes each');
%  end
%  ch2 = get(second_ax, 'children');     %direct children only and don't try to find the hidden ones
%  copyobj(ch2, first_ax);         %beam them over
%  
%  

