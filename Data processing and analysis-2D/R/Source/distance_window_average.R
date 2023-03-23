distance_window_average <- function(dat, group, group2, width, i) {
  
# This will apply rolling average of width "width" on "Location_Center_X_cells" and "Location_Center_Y_cells" and calculate distance travelled based on it (variable "dt")
# In case Location_Center_X_cells is na, it will assign the nucleus value and keep a record of this
  
  # dat$Location_Center_X_cells2 <- dat$Location_Center_X_cells
  # dat$Location_Center_Y_cells2 <- dat$Location_Center_Y_cells
  # 
  # dat$Location_Center_X_cells_NA <- is.na(dat$Location_Center_X_cells2)
  # dat$Location_Center_Y_cells_NA <- is.na(dat$Location_Center_Y_cells2)
  # 
  # dat <- dat %>%
  #   arrange_(group,group2) %>%
  #   group_by_(group) %>%
  #   mutate_(Location_Center_X_cells2 = interp(~(na.approx(Location_Center_X_cells2, maxgap = 3, na.rm = FALSE))),
  #          Location_Center_Y_cells2 = interp(~(na.approx(Location_Center_Y_cells2, maxgap = 3, na.rm = FALSE)))) %>%
  #   as.data.frame()
  # 
  # 
  # # Deal with leading or trailing NAs: assign nucleus value
  # dat$Location_Center_X_cells2[which(is.na(dat$Location_Center_X_cells2))] <- dat$Location_Center_X[which(is.na(dat$Location_Center_X_cells2))]
  # dat$Location_Center_Y_cells2[which(is.na(dat$Location_Center_X_cells2))] <- dat$Location_Center_Y[which(is.na(dat$Location_Center_Y_cells2))]
  
   dat$Location_Center_X_cells2 <- dat$Location_Center_X_cells
   dat$Location_Center_Y_cells2 <- dat$Location_Center_Y_cells
  # 
  # dat$Location_Center_X_cells2[which(is.na(dat$Location_Center_X_cells2))] <- dat$Location_Center_X[which(is.na(dat$Location_Center_X_cells2))]
  # dat$Location_Center_Y_cells2[which(is.na(dat$Location_Center_X_cells2))] <- dat$Location_Center_Y[which(is.na(dat$Location_Center_Y_cells2))]
  
  
    # write error table if any of the location_center_x or y from cells or nuclei are NA
   if(any(c(is.na(dat$Location_Center_X_cells),is.na(dat$Location_Center_Y_cells)), is.na(dat$Location_Center_X), is.na(dat$Location_Center_Y))){
    write.table(dat[which(is.na(dat$Location_Center_X_cells) | is.na(dat$Location_Center_Y_cells) | is.na(dat$Location_Center_X) | is.na(dat$Location_Center_Y)),
                    c("ImageNumber", "Metadata_stagepos", "TrackObjects_Label","Location_Center_X_cells",  "Location_Center_Y_cells", "Location_Center_X",  "Location_Center_Y")],
               Files$Postprocessing$Error_files$Files[i], row.names = FALSE, col.names = TRUE, sep = ",", quote = FALSE)
   }
   

   # this computes distance travelled for cell centroid and nuc centroid
  dat <- dat %>% 
    arrange_(group, group2) %>%
    group_by_(group) %>%
    mutate(dt_cells = sqrt(((Location_Center_X_cells2 - lag(Location_Center_X_cells2,n = 1))^2)+((Location_Center_Y_cells2 - lag(Location_Center_Y_cells2,n = 1))^2)),
           dt_nuc  = sqrt(((Location_Center_X - lag(Location_Center_X,n = 1))^2)+((Location_Center_Y - lag(Location_Center_Y,n = 1))^2))) %>%
    mutate(dt_cells_ws = rollapply(dt_cells, width, mean, align="center", fill = NA),
           dt_nuc_ws = rollapply(dt_nuc, width, mean, align="center", fill = NA)) %>%
    as.data.frame()
  
  
  
  return(dat)
}


