create_directories_and_save <- function(Files)

for (i in seq_along(Files$stage_positions)){
  # create directories
  setwd(Files$path)
  dir.create(Files$stage_positions[i])
  setwd(Files$stage_positions[i])
  
  dir.create("Outlines")
  setwd("./Outlines")
  
  filestocopy <- Files$files_description$outlines[which(Files$files_description$xy_pos==Files$stage_positions[i])]
  targetdir <- paste(Files$path,Files$stage_positions[i],"/Outlines", sep="")
  
  file.copy(from=filestocopy, to=targetdir)
  file.rename(list.files(), as.character(Files$files_description$outlines_new[1:Files$number_of_images_each]))
  
  
  dir.create("../Tracking")
  setwd("../Tracking")
  
  filestocopy <- Files$files_description$tracking[which(Files$files_description$xy_pos==Files$stage_positions[i])]
  targetdir <- paste(Files$path,Files$stage_positions[i],"/Tracking", sep="")
  
  file.copy(from=filestocopy, to=targetdir)
  file.rename(list.files(), as.character(Files$files_description$tracking_new[1:Files$number_of_images_each]))
  
  dir.create("../Plots")
}

