
labs = newArray("L1", "L2", "L3");
experiments = newArray("U1E1", "U1E2", "U1E3", "U1E4", "U2E1", "U2E2", "U2E3", "U2E4", "U3E1", "U3E2", "U3E3"); 

  
for (labs_n = 1;labs_n < 2; labs_n++){
	for (experiments_n = 0;experiments_n < 11; experiments_n++){

string = "open=[D:\\Data\\MULTIMOT\\Processed\\XSP\\"+labs[labs_n]+"\\"+experiments[experiments_n]+"\\2 - CellProfiler results_noise\\Nuclei\\Nuc0001.jpeg]  scale=50 sort";
print(string);
		
			run("Image Sequence...", string);
			run("Stack Splitter", "number=6");

for (i=1; i<7; i++) {

selectWindow("stk_000"+i+"_Nuclei");

w = getWidth(); 
h = getHeight(); 
makeRectangle(3, 3, w-5, h-5); 
run("Crop"); 
run("Canvas Size...", "width="+w+" height="+h+" position=Center"); 

}


run("Combine...", "stack1=stk_0001_Nuclei stack2=stk_0002_Nuclei");
rename("tl");
run("Combine...", "stack1=tl stack2=stk_0003_Nuclei");
rename("t");

run("Combine...", "stack1=stk_0004_Nuclei stack2=stk_0005_Nuclei");
rename("bl");
run("Combine...", "stack1=bl stack2=stk_0006_Nuclei");
rename("b");
run("Combine...", "stack1=b stack2=t combine");
run("8-bit Color", "number=256");
saveAs("Gif", "C:\\Users\\xavser\\Desktop\\segmentation results\\"+labs[labs_n]+"\\"+experiments[experiments_n]+".gif");

close();
selectWindow("Nuclei");
close();
  call("java.lang.System.gc"); 
    call("java.lang.System.gc"); 
      call("java.lang.System.gc"); 


}
}