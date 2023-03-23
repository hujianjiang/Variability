rm(list=ls())

# Libraries and functions ---------------------------------------------------------------
library(ggplot2)
library(reshape2)
library(zoo) # rolling average
library(dplyr)
library(lazyeval)
library(R.utils) # sourceDirectory
library(xlsx)


sourceDirectory("C:/Users/xavser/Box Sync/MULTIMOT/2D cell migration/src/R/v2/Source/") #load custom made functions 
setwd("C:/Users/xavser/Box Sync/MULTIMOT/2D cell migration/src/R/v2/Source/")
source_list <- list.files()

for (i in seq_along(source_list)){
  source(source_list[i])
  
}



# Settings ----------------------------------------------------------------


# Files -------------------------------------------------------

loe <- list.files("D:/Data/MULTIMOT/Processed/XSP/L1/", full.names = TRUE)
# loe <- loe[c(3:6,8:11,13:15)] #L2
# loe <- loe[2:11] #L3



# for (exp_list in seq_along(loe)){
for (exp_list in 1:length(loe)) { 
  print(loe[exp_list])
  
  Files <- list(expPath = paste0(loe[exp_list],"/"),#"D:/Data/MULTIMOT/Processed/XSP/Ghent_lab/U1_E1/",
                stagepos = c("C1", "C2", "C3", "T1", "T2", "T3"),
                channels = c("c1","c2","c3"),
                number_of_images_total = 438, 
                number_of_images_each = 73,
                MatLab_dat_file = "Matlab_export_dat.csv",
                Error_file = "errors.csv",
                MatLab_ExcelFile = "/Files.xlsx")
  
  Files <- nameFiles(Files)
  
  
  # Load data
  nuclei <- read.csv(Files$CellProfiler_results$nuclei)
  cells <- read.csv(Files$CellProfiler_results$cells)
  
  
  # Data curation for each stage position------------------------------------
  for (i in seq_along(Files$stagepos)){
    print(i)
    setwd(Files$Postprocessing$stagepos_folders[i])
    
    mem_dyn <- read.csv(Files$MatLab_results[i])
    
    nuclei_t <- nuclei[which(nuclei$Metadata_stagepos==unique(Files$stagepos)[i]),]
    cells_t <- cells[which(cells$Metadata_stagepos==unique(Files$stagepos)[i]),]
    
    a2 <- process_objectsofinterest(nuclei_t,cells_t, mem_dyn, Files, i)

    
    dat <- a2[[1]]
    dat_old <- a2[[2]]
    # visual inspection (to be run in matlab)
    dat_temp <- dat[,c("TrackObjects_Label","ImageNumber","Location_Center_X_cells2","Location_Center_Y_cells2")]
    write.table(dat_temp,Files$Postprocessing$Matlab_files$File[i], row.names = FALSE, col.names=FALSE, sep=",", quote = FALSE)

    # save for further use
    save(file="dat.R",dat)
    write.table(dat_old[,c("AreaShape_Center_X","AreaShape_Center_Y","AreaShape_Area_cells","AreaShape_Area", "excluded", "ImageNumber", "TrackObjects_Label")], "dat_old.csv", row.names = FALSE)

  }
  
  
  
}



