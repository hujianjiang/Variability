define_names <- function(Files) {
  
  Files$files_description <- data.frame(image_number = 1:Files$number_of_images_total,
                                       xy_pos = rep(Files$stage_positions, each=Files$number_of_images_each),
                                       frame = rep(1:Files$number_of_images_each, length(Files$stage_positions)),
                                       outlines = c(paste(Files$path, Files$outlines_folder, "Nuc_000",1:9, ".jpg", sep=""),
                                                    paste(Files$path, Files$outlines_folder, "Nuc_00",10:99, ".jpg", sep=""),
                                                    paste(Files$path, Files$outlines_folder, "Nuc_0",100:Files$number_of_images_total, ".jpg", sep="")),
                                       tracking = c(paste(Files$path, Files$tracking_folder, "Tracked_cells000",1:9, ".jpeg", sep=""),
                                                    paste(Files$path, Files$tracking_folder, "Tracked_cells00",10:99, ".jpeg", sep=""),
                                                    paste(Files$path, Files$tracking_folder, "Tracked_cells0",100:Files$number_of_images_total, ".jpeg", sep="")),
                                       outlines_new = rep(c(paste("Nuc_000",1:9, ".jpg", sep=""),paste("Nuc_00",10:Files$number_of_images_each, ".jpg", sep="")), 6),
                                       tracking_new = rep(c(paste("Tracked_cells000",1:9, ".jpg", sep=""),paste("Tracked_cells00",10:Files$number_of_images_each, ".jpg", sep="")), 6))
  
  return(Files)
}
