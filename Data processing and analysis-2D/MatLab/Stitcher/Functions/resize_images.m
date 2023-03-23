function [im_res] =  resize_images (Settings, im_res)
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here


im_res{1,1} = imresize(im_res{1,1},Settings.resize_factor);
im_res{1,2} = imresize(im_res{1,2},Settings.resize_factor);


end




