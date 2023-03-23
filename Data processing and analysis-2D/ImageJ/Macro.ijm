pathfileim1="C:/Users/xavser/Box Sync/Mech_CCM/Processed/data/2018_01_Data/Output/MatLab_Images/file1.txt"
filestring=File.openAsString(pathfileim1); 
rows=split(filestring, "\n"); 


for (i=3; i<64; i++) {

folders = rows[i];

//folders = 
//"[C:\\Users\\xavser\\Box Sync\\Mech_CCM\\Processed\\data\\2018_01_Data\\Output\\MatLab_Images\\20170616 A431 RH30\\RH30\\xy16\\new_im1277.png]"
//"[C:\\Users\\xavser\\Box Sync\\Mech_CCM\\Processed\\data\\2018_01_Data\\Output\\MatLab_Images\\20170616 A431 RH30\\RH30\\xy16\\Protrusions\\Windowsize_1\\im_protrusions_new_im1278.png]"
//"C:\\Users\\xavser\\Box Sync\\Mech_CCM\\Processed\\data\\2018_01_Data\\Output\\MatLab_Images\\20170616 A431 RH30\\RH30\\xy16\\Combined Stacks.gif";

delimiter = "*";
parts = split(folders,delimiter);
dir0 = parts[0];
dir1 = parts[1];
dir2 = parts[2];
print(dir0);
print(dir1);
print(dir2);

// this loads first image
run("Image Sequence...", "open="+dir0+ " file=new sort");
//run("Image Sequence...", "open=[C:\\Users\\xavser\\Box Sync\\Mech_CCM\\Processed\\data\\2018_01_Data\\Output\\MatLab_Images\\20170616 A431 RH30\\A431\\xy01\\new_im0003.png] file=new sort");
makeRectangle(902, 110, 1296, 1292);
run("Crop");
rename("im1");

// this loads second image
run("Image Sequence...", "open="+dir1+ " file=new sort");
//run("Image Sequence...", "open=[C:\\Users\\xavser\\Box Sync\\Mech_CCM\\Processed\\data\\2018_01_Data\\Output\\MatLab_Images\\20170616 A431 RH30\\RH30\\xy16\\Protrusions\\Windowsize_1\\im_protrusions_new_im1278.png] file=sort");
rename("im2");
makeRectangle(184, 46, 541, 542);
run("Crop");

run("Scale...", "x=- y=- z=1.0 width=1296 height=1298 depth=82 interpolation=Bilinear average process create");
rename("im2_r");
selectWindow("im2");
close();

run("Combine...", "stack1=im1 stack2=im2_r");


run("8-bit Color", "number=256");

saveAs("Gif", dir2);
close();
 }