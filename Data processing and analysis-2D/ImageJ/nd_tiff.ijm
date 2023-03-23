

run("Bio-Formats Importer", "open=[D:\\Data\\MULTIMOT\\Original data\\Jianjiang\\Migration20160616\\Migration20160616.nd2] color_mode=Default rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT series_1");

run("Split Channels");

rename("C3_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy1c3\\C3_0001.tif]");
close();

rename("C2_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy1c2\\C2_0001.tif]");
close();

rename("C1_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy1c1\\C1_0001.tif]");
close();





run("Bio-Formats Importer", "open=[D:\\Data\\MULTIMOT\\Original data\\Jianjiang\\Migration20160616\\Migration20160616.nd2] color_mode=Default rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT series_2");

run("Split Channels");

rename("C3_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy2c3\\C3_0001.tif]");
close();

rename("C2_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy2c2\\C2_0001.tif]");
close();

rename("C1_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy2c1\\C1_0001.tif]");
close();



run("Bio-Formats Importer", "open=[D:\\Data\\MULTIMOT\\Original data\\Jianjiang\\Migration20160616\\Migration20160616.nd2] color_mode=Default rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT series_3");

run("Split Channels");

rename("C3_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy3c3\\C3_0001.tif]");
close();

rename("C2_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy3c2\\C2_0001.tif]");
close();

rename("C1_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy3c1\\C1_0001.tif]");
close();





run("Bio-Formats Importer", "open=[D:\\Data\\MULTIMOT\\Original data\\Jianjiang\\Migration20160616\\Migration20160616.nd2] color_mode=Default rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT series_4");

run("Split Channels");

rename("C3_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy4c3\\C3_0001.tif]");
close();

rename("C2_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy4c2\\C2_0001.tif]");
close();

rename("C1_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy4c1\\C1_0001.tif]");
close();





run("Bio-Formats Importer", "open=[D:\\Data\\MULTIMOT\\Original data\\Jianjiang\\Migration20160616\\Migration20160616.nd2] color_mode=Default rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT series_5");

run("Split Channels");

rename("C3_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy5c3\\C3_0001.tif]");
close();

rename("C2_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy5c2\\C2_0001.tif]");
close();

rename("C1_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy5c1\\C1_0001.tif]");
close();






run("Bio-Formats Importer", "open=[D:\\Data\\MULTIMOT\\Original data\\Jianjiang\\Migration20160616\\Migration20160616.nd2] color_mode=Default rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT series_6");

run("Split Channels");

rename("C3_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy6c3\\C3_0001.tif]");
close();

rename("C2_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy6c2\\C2_0001.tif]");
close();

rename("C1_");
run("Image Sequence... ", "format=TIFF save=[D:\\Data\\MULTIMOT\\Processed\\XSP\\JianJiang_experiment3\\1 - Decompressed_images\\xy6c1\\C1_0001.tif]");
close();
