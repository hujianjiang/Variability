merge_duplicated_nucleus <- function(dat) {
  
  
  # identify cells and frames
  dat.duplicated <- as.data.frame(table(dat$TrackObjects_Label,dat$ImageNumber))
  dat.duplicated <- dat.duplicated %>% filter(Freq==2)
  dat.duplicated <- dat.duplicated[,1:2]
  colnames(dat.duplicated) <- c("Object_Number", "Frame")
  dat.duplicated <- dat.duplicated[with(dat.duplicated,order(Object_Number,Frame)),]
  
  
  # compute values (mean, sum, or first) and modify dat
  for (i in seq_along(dat.duplicated[,1])){
    
    temp <- dat %>% 
      filter(as.character(TrackObjects_Label)==as.character(dat.duplicated$Object_Number[i]) & 
               as.character(ImageNumber) == as.character(dat.duplicated$Frame[i]))
    
    var_sum <- c("AreaShape_Area", "AreaShape_Area_cells")
    
    var_first <- c("ImageNumber", "ObjectNumber", "Children_Cells_Count", "Number_Object_Number", "Parent_Nuclei", 
                   "TrackObjects_ParentImageNumber", "TrackObjects_ParentObjectNumber", "TrackObjects_Label",
                  "ImageNumber_cells", "ObjectNumber_cells", "Number_Object_Number_cells", "Parent_Nuclei_cells", 
                  "Metadata_stagepos", "Metadata_stagepos.1", "Metadata_stagepos_cells", "Metadata_stagepos.1_cells",
                  "cat")

    var_mean <- colnames(temp)[!colnames(temp) %in% c(var_sum,var_first)]
    
    temp[3,] <- NA
    temp[3,var_mean] <- colMeans(temp[1:2,var_mean])
    temp[3,var_sum] <- colSums(temp[1:2,var_sum])
    temp[3,var_first] <- temp[1,var_first]
    
    dat2 <- dat %>% 
      filter(!(as.character(TrackObjects_Label)==as.character(dat.duplicated$Object_Number[i]) & 
                 as.character(ImageNumber) == as.character(dat.duplicated$Frame[i])))
    
    dat <- rbind(dat2,temp[3,])
  }
 
  return(dat) 
}