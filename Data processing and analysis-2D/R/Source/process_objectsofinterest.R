process_objectsofinterest <- function(nuclei,cells, mem_dyn, Files, i) {
  
  # process_objectsofinterest will perform several operations to the Nuclei file:
  #   1) Rename columns
  #   2) merge nuclei data with cell data
  #   3) remove NaN nuclei from the original file
  #   4) Identify multiple objects and remove if necessary
  #   5) Identify duplicated events and merge
  #   6) apply rolling window to cell centroids and calculate distance
  
  # Also, it will plot a representation of the cells considered before step 4,5 and after 5.
  
  # Change label of track variables (remove "_X", where X is a cellprofiler parameter (pixel distance to consider matches))
  colnames(nuclei)[which(grepl("TrackOb",names(nuclei)))]<- substr(colnames(nuclei[,grepl("TrackOb",names(nuclei))]),1,nchar(colnames(nuclei[,grepl("TrackOb",names(nuclei))]))-3)
  nuclei$TrackObjects_Label <- factor(nuclei$TrackObjects_Label)
  
  # Combine two sources of data in dat dataframe
  dat <- merge_cells_nuclei_tables(nuclei,cells)
  
  # create new image_number
  dat$image_number_old <- dat$ImageNumber
  # dat$ImageNumber <- Files$files_description$frame[match(dat$ImageNumber,Files$files_description$image_number)]
  
  dat$ImageNumber <- Files$Postprocessing$Files$Final_number[match(dat$ImageNumber,Files$Postprocessing$Files$Original_number)]
  
  # Combine dat with mem_dyn data
  dat <- merge_dat_mem_dyn_tables(dat,mem_dyn)
  
  
  # this section here is to identify which cells have been excluded at each step, to be plot latter on.
  dat$id <- 1: dim(dat)[1]
  dat_old <- dat
  dat_old$excluded <- 0

  # Remove NaN nuclei (these nuclei are not assigned a "TrackObjects_Label", I assume as tracks don't last longer than 12 frames (cell profiler settings))
  dat <- dat[which(!dat$TrackObjects_Label=="NaN"),]
  dat_old$excluded[which(!dat_old$id %in% dat$id)] <- 1
    
  
  # sort based on object identified
  dat <- dat[with(dat, order(TrackObjects_Label,ImageNumber)),]
  plot_cells_matrix(dat, "1-all.pdf")
  
  # Identify multiple objects and remove if necessary
  dat <-remove_multiple_objects(dat)
  plot_cells_matrix(dat, "2-unique.pdf")
  dat_old$excluded[which(!dat_old$id %in% dat$id & dat_old$excluded == 0)] <- 2
  
  # Identify duplicated events and merge (assign sum, mean, or first value to variables)
  dat <- merge_duplicated_nucleus(dat)
  dat_old$excluded[which(!dat_old$id %in% dat$id & dat_old$excluded == 0)] <- 3
  plot_cells_matrix(dat, "3-merged.pdf")
  
  # apply rolling window function to centroids "Location_Center_X_cells" and "Location_Center_Y_cells". See function for more details.
  dat <- distance_window_average(dat,"TrackObjects_Label","ImageNumber",9, i)
  
  
  
  
  a2 <- list(dat, dat_old)
  
  return(a2)
  # 
}


