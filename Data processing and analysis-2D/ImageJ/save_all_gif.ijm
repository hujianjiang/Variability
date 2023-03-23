for (x = 1; x<7; x++){
	open("D:\\Data\\MULTIMOT\\Processed\\XSP\\Geiger_lab\\3_3_SW_28DEC17_Multimot07\\2 - CellProfiler results\\all_images_well"+x+".gif");
	w = getWidth(); 
	h = getHeight(); 
	makeRectangle(5, 5, w-5, h-5); 
	run("Crop"); 
	run("Canvas Size...", "width="+w+" height="+h+" position=Center"); 
}


run("Combine...", "stack1=all_images_well1.gif stack2=all_images_well2.gif");
run("Combine...", "stack1=[Combined Stacks] stack2=all_images_well3.gif");
rename("c1");

run("Combine...", "stack1=all_images_well4.gif stack2=all_images_well5.gif");
run("Combine...", "stack1=[Combined Stacks] stack2=all_images_well6.gif");
rename("c2");

run("Combine...", "stack1=c1 stack2=c2 combine");
run("8-bit Color", "number=256");

saveAs("Gif", "D:\\Data\\MULTIMOT\\Processed\\XSP\\Geiger_lab\\3_3_SW_28DEC17_Multimot07\\2 - CellProfiler results\\all_images.gif");
close();