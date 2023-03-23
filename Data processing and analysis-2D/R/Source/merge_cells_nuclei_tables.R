merge_cells_nuclei_tables <- function(nuclei, cells){
  
  nuclei$cat <- paste(nuclei$ImageNumber,"_",nuclei$ObjectNumber)
  cells$cat <- paste(cells$ImageNumber, "_", cells$ObjectNumber)

  
  # colnames(cells) <- paste( "Cel_", colnames(cells), sep="")
  # colnames(cells)[which(names(cells) %in% "Cel_cat")] <- "cat"
  colnames(cells) <- paste( colnames(cells), "_cells", sep="")
  colnames(cells)[which(names(cells) %in% "cat_cells")] <- "cat"
  
  # colnames(nuclei) <- paste( "Nuc_", colnames(nuclei), sep="")
  # colnames(nuclei)[which(names(nuclei) %in% "Nuc_cat")] <- "cat"
  
  dat <- merge(nuclei,cells,by = "cat")
  
  # colnames(dat) <- gsub("AreaShape_", "",colnames(dat))
  
  return(dat)
  
}
