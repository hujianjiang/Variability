rm(list=ls())

# libraries ---------------------------------------------------------------

library(xlsx)
library(reshape2)
library(ggplot2)
library(nlme)
library(lme4)
library(dplyr)
library(foreach)
library(doSNOW)


# get data ----------------------------------------------------------------

Files <- read.xlsx2("W:/jiahu/Xavier/test/Experiments_status.xlsx",1)
Files <- Files[which(Files$R.processed == "y"),]
# Files <- Files[which(Files$MatLab.processed == "y"),]

dat_all <- list()
for (i in seq_along(Files$Analysis.folder)){
  
  load(as.character(Files$dat_file[i]))
  dat$Condition <- Files$Condition[i]
  dat$experiment <- Files$Global.experiment.number[i]
  dat$stagepos <- Files$Stagepos[i]
  dat$folder <- Files$Analysis.folder[i]
  dat_all[[i]] <- dat
  i <- i+1
  
}

dat <- do.call(rbind.data.frame, dat_all)



# Save in cache -----------------------------------------------------------
save(file="W:/jiahu/Xavier/test/dat.R",dat)

# write.csv( dat, file=gzfile("C:/Users/xavser/Box Sync/MULTIMOT/cache/dat.csv.gz") )

"C:/Users/xavser/Box Sync/MULTIMOT/cache/"