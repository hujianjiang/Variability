//run("Bio-Formats Importer", "open=D:\\4by4_resize\\9_20170903\\20170903CA1a_transfection_4d_with_overlay1_12_10X_9_resize.nd2 autoscale color_mode=Default view=Hyperstack stack_order=XYCZT series_1");

name=getTitle; 
dir = getDirectory("image"); 

run("Split Channels");
rename("C2");
run("Put Behind [tab]");
close();
selectWindow("C2");
//run("Brightness/Contrast...");
run("Enhance Contrast", "saturated=0.35");

makeRectangle(484, 484, 2904, 2904);
run("Crop");

waitForUser("set threshold") 
setAutoThreshold("Default");
//run("Threshold...");setOption("BlackBackground", false);




run("Make Binary");
run("Duplicate...", " ");
run("Analyze Particles...", "pixel show=Outlines display clear summarize in_situ");
 
path = dir+name+"_values.txt";
saveAs("Results", path);

path = dir+name+"_outlines.txt";
saveAs("Jpeg", path);
selectWindow("C2");

path = dir+name+"_image.txt";
saveAs("Tiff", path);
run("Close All");
run("Close");
selectWindow("Results");
run("Close");