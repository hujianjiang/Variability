nameFiles <- function(Files) {
  # This function will structure and assign paths to files to be analyzed, both for the 
  # decompressed images
  # Cellprofiler results
  # Postprocessing results
  
  # It will also create directories and copy paste the files from cellprofiler results to the corresponding folders in postprocessing folder
  # Finally, The function also saves the necessary files information to excel files, so that it can be used for the MatLab routine. I have done it
  # using an accessory excel file as exporting the R list structure to MatLab fails.
  
  temp <- Files # I assign Files to temp so that I can overwrite temp
  
  
  # DECOMPRESSED IMAGES: This creates the basic structure of the data for the Files structure with the basic information of the decompressed images (path, positions, number of images)
  Files <-  list(root = temp$expPath,
                 stagepos = temp$stagepos,
                 Decompressed_images = list(path = paste(temp$expPath, "1 - Decompressed_images", sep=""),
                                            stagepos = setNames(vector("list", length(temp$stagepos)), paste("stagepos", as.character(1:6), sep="")),
                                            number_of_images_total = temp$number_of_images_total,
                                            number_of_images_each = temp$number_of_images_each))
  
  # this creates the list of images that have been analyzed for each stage position folder
  for (i in seq_along(temp$stagepos)){
    Files$Decompressed_images$stagepos[[i]] <- list(c1 = list.files(paste(Files$Decompressed_images$path,"/xy",i,"c1/", sep=""), full.names = TRUE),
                                                    c2 = list.files(paste(Files$Decompressed_images$path,"/xy",i,"c2", sep=""), full.names = TRUE))
    
  }
  
  
  # CELLPROFILER: Create basic structure of the Cell Profiler results, including path, folders that have been created, number of images, etc
  Files$CellProfiler_results <- list(path = paste(Files$root, "2 - CellProfiler results", sep=""),
                                     outlinesfolder = "/CellMasks/",
                                     Trackingfolder = "/Tracking/",
                                     
                                     image_number = 1:temp$number_of_images_total,
                                     xy_pos = rep(temp$stagepos, each=temp$number_of_images_each),
                                     frame = rep(1:temp$number_of_images_each,length(temp$stagepos)))
  
  Files$CellProfiler_results$nuclei = paste0(Files$CellProfiler_results$path,"/MyExpt_FilteredNuclei.csv") # Nuclei Results, output from cellprofiler
  Files$CellProfiler_results$cells = paste0(Files$CellProfiler_results$path,"/MyExpt_Cells.csv") # Cell Results file, output from cellprofiler
  
  
  Files$CellProfiler_results$outlines <- list.files(paste0(Files$CellProfiler_results$path,Files$CellProfiler_results$outlinesfolder), full.names=TRUE) # list and path of outlines results images
  Files$CellProfiler_results$Tracking <- list.files(paste0(Files$CellProfiler_results$path,Files$CellProfiler_results$Trackingfolder), full.names=TRUE) # list and path of tracking results images

  # MatLab results: defines path to matlab membrane dynamics results
  Files$MatLab_results <- paste0(Files$root, "3 - MatLab results/", temp$stagepos, "/Membrane_dynamics.csv")
  
  
  # POSTPROCESSING: This creates the basic structure of the postprocessing results  
  Files$Postprocessing <- list(path = paste0(Files$root, "4 - Postprocessing results"))
  Files$Postprocessing$stagepos <- temp$stagepos
  Files$Postprocessing$stagepos_folders <- paste0(Files$Postprocessing$path,"/",temp$stagepos)
  
  
  
  # we create directories here
  dir.create(Files$Postprocessing$path)
  for (i in seq_along(Files$stagepos)){
    # create directories
    setwd(Files$Postprocessing$path)
    dir.create(Files$stagepos[i])
    setwd(Files$stagepos[i])
    
    dir.create("Outlines")
    dir.create("Tracking")
    dir.create("Plots")
    dir.create("MatLab")
    dir.create("Images")
  }
  
  # This is to copy images from cell profiler folder to postprocessing folder and rename them appropiately
  temp2 <- data.frame(Original_files = c(Files$CellProfiler_results$outlines,Files$CellProfiler_results$Tracking),
                      path = Files$Postprocessing$path,
                      stagepos = paste0("/",Files$CellProfiler_results$xy_pos),
                      Folder = c(rep("/Outlines/",Files$Decompressed_images$number_of_images_total),rep("/Tracking/",Files$Decompressed_images$number_of_images_total)),
                      File_sequence = 1:Files$Decompressed_images$number_of_images_each,
                      Name = c(rep("Outlines_",Files$Decompressed_images$number_of_images_total),rep("Tracking",Files$Decompressed_images$number_of_images_total)),
                      extension = do.call(rbind,strsplit(as.character(c(Files$CellProfiler_results$outlines,Files$CellProfiler_results$Tracking)), "\\."))[,2])
  
  
  temp2$Final_files <- paste0(temp2$path,temp2$stagepos, temp2$Folder, temp2$Name, temp2$File_sequence, ".", temp2$extension)

  Files$Postprocessing$Files <- data.frame(Original_files = temp2$Original_files, 
                                           Final_files = temp2$Final_files,
                                           Original_number = 1:Files$Decompressed_images$number_of_images_total,
                                           Final_number = temp2$File_sequence)
  
  filestocopy <- as.character(Files$Postprocessing$Files$Original_files)
  targetdir <-  as.character(Files$Postprocessing$Files$Final_files)
  
  file.copy(from=filestocopy, to=targetdir, overwrite = TRUE)
  
  
  # OUTPUT MATLAB FILES
  Files$Postprocessing$Matlab_files$Files <- paste0(Files$Postprocessing$stagepos_folders,"/MatLab/",temp$MatLab_dat_file)
  Files$Postprocessing$Matlab_files$ExcelFile <- temp$MatLab_ExcelFile
  
  # OUTPUT ERROR FILES, this is at least for the na values found in location_x and location_y
  Files$Postprocessing$Error_files$Folder <- Files$Postprocessing$stagepos_folders
  Files$Postprocessing$Error_files$Files <- paste0(Files$Postprocessing$Error_files$Folder, "/", temp$Error_file)
  
  
  # excel files export
  write.xlsx2(Files$root,
              file = paste0(Files$Postprocessing$path,Files$Postprocessing$Matlab_files$ExcelFile), 
              sheetName = "root", col.names=FALSE, row.names=FALSE)
  
  
  write.xlsx2(data.frame(Folders = Files$Postprocessing$stagepos_folders, 
                         Matlab_folders = Files$Postprocessing$Matlab_files$Files),
              file = paste0(Files$Postprocessing$path,Files$Postprocessing$Matlab_files$ExcelFile),
              "experiments", col.names=FALSE, row.names=FALSE, append = TRUE)
  
  write.xlsx2(as.data.frame(rbind(number_of_images_each = Files$Decompressed_images$number_of_images_each, 
                                  number_of_images_total = Files$Decompressed_images$number_of_images_total)),
              file = paste0(Files$Postprocessing$path,Files$Postprocessing$Matlab_files$ExcelFile),
              "Image_numbers", col.names=FALSE, row.names=TRUE, append = TRUE)
  
  write.xlsx2(data.frame(Files$Decompressed_images$stagepos),
              file = paste0(Files$Postprocessing$path,Files$Postprocessing$Matlab_files$ExcelFile),
              "Original_files", col.names=FALSE, row.names=FALSE, append = TRUE)
  
  
  
  return(Files)
  
}