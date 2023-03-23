merge_dat_mem_dyn_tables <- function(dat, mem_dyn){

  mem_dyn$Dynamic <- NULL
  
  mem_dyn <- dcast(mem_dyn, ...~newname, value.var="Number_of_pixels")
  
  mem_dyn$cat <- paste0(mem_dyn$timepoint, "_", mem_dyn$Cell_id)
  
  mem_dyn_t <- mem_dyn[,c("Protrusions", "Retractions", "Short Lived Regions", "cat", "timepoint")]

  dat$TrackObjects_Label <- as.integer(as.character(dat$TrackObjects_Label))
  dat$cat <- paste0(dat$ImageNumber,"_", dat$TrackObjects_Label)
  
  dat <- merge(dat,mem_dyn_t,by = "cat", all.x = TRUE)
  
  return(dat)
  
}