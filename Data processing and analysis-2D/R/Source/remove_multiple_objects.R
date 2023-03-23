remove_multiple_objects <- function(dat) {
  
  temp <- table(dat$TrackObjects_Label, dat$ImageNumber)
  
  temp[which(temp==0)] <- NA
  temp <- temp-1
  
  if (sum(temp, na.rm=TRUE) != 0) {
    
    if (length(which(rowSums(temp, na.rm=TRUE)>0)) == 1){
      
      rname <- rownames(temp)[which(rowSums(temp, na.rm=TRUE)>0)]
      temp <- temp[rowSums(temp, na.rm=TRUE)>0,]
      
      
      temp <- rbind(temp, temp)
      
      rownames(temp) <- c(rname, "to_delete")
      
      temp <- as.data.frame.matrix(temp)   
      temp <- temp[1,]
      
    }else{
      temp <- temp[rowSums(temp, na.rm=TRUE)>0,]
      temp <- as.data.frame.matrix(temp)   
      
    }
    
    
    
    
    temp[is.na(temp)] <- 0 #we don't care about the NAs so we convert to 0 for subsequent operations
    
    temp2 <- apply(temp, 1, function(x) cumsum(diff(x)))
    temp3 <- apply(temp2,2,function(x) paste(x,sep="", collapse=""))
    
    exclude <- names(temp3[grep("11",temp3)])
    exclude2 <- names(temp3[grep("2",temp3)]) #this is to exclude cases in which one object becomes 3
    exclude3 <- names(temp3[grep("3",temp3)]) #this is to exclude cases in which one object becomes 4
    
    dat <- dat[!dat$TrackObjects_Label %in% exclude,]
    dat <- dat[!dat$TrackObjects_Label %in% exclude2,]
    dat <- dat[!dat$TrackObjects_Label %in% exclude3,]
  }else{
    
    dat <- dat
  }
  
  
  
}


