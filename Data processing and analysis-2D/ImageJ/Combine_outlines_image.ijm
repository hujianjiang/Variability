


labs = newArray("L1", "L2", "L3");
experiments = newArray("U1E1", "U1E2", "U1E3", "U2E1", "U2E2", "U2E3", "U3E1", "U3E2", "U3E3"); 
condition = newArray("C1", "C2", "C3", "T1", "T2", "T3"); 

  
for (labs_n = 0;labs_n < 1; labs_n++){
	for (experiments_n = 0;experiments_n < 9; experiments_n++){
		for (condition_n = 0;condition_n < 6; condition_n++){


string = "D:\\Data\\MULTIMOT\\Processed\\XSP\\"+labs[labs_n]+"\\"+experiments[experiments_n]+"\\4 - Postprocessing results\\all_images\\Outlines_pos"+condition[condition_n]+".gif";
print(string);

open(string);
rename("Outlines");
selectWindow("Outlines");
makeRectangle(3, 21, 1467, 1467);
run("Crop");


string2 = "open=[D:\\Data\\MULTIMOT\\Processed\\XSP\\"+labs[labs_n]+"\\"+experiments[experiments_n]+"\\1 - Images\\"+condition[condition_n]+"\\Chan_1\\C1_T01.tif] sort use";
run("Image Sequence...", string2);
selectWindow("Chan_1");
run("Scale...", "x=- y=- z=1.0 width=1467 height=1467 depth=73 interpolation=Bilinear average process create");
selectWindow("Chan_1");
close();
selectWindow("Chan_1-1");
//run("Brightness/Contrast...");
run("Enhance Contrast", "saturated=0.35");
run("Apply LUT", "stack");
run("RGB Color");


string3 = "open=[D:\\Data\\MULTIMOT\\Processed\\XSP\\"+labs[labs_n]+"\\"+experiments[experiments_n]+"\\1 - Images\\"+condition[condition_n]+"\\Chan_2\\C2_T01.tif] sort use";
run("Image Sequence...", string3);
run("Scale...", "x=- y=- z=1.0 width=1467 height=1467 depth=73 interpolation=Bilinear average process create");
selectWindow("Chan_2");
close();
selectWindow("Chan_2-1");
//run("Brightness/Contrast...");
run("Enhance Contrast", "saturated=0.35");
run("Apply LUT", "stack");
run("RGB Color");

imageCalculator("Add create stack", "Chan_1-1","Chan_2-1");
selectWindow("Chan_1-1");
close();
selectWindow("Chan_2-1");
close();

rename("Composite");
run("Combine...", "stack1=Results of Chan_1-1 stack2=Composite");
run("8-bit Color", "number=256");


saveAs("Gif", "D:\\Data\\MULTIMOT\\Processed\\XSP\\L1\\U1E1\\4 - Postprocessing results\\all_images\\Outlines_"+condition[condition_n]+".gif");

close();


}
}
}