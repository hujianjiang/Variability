rm(list=ls())

# libraries ---------------------------------------------------------------

library(xlsx)
# library(reshape2)
# library(ggplot2)
# library(nlme)
# library(lme4)
library(dplyr)
# library(foreach)
# library(doSNOW)


# Define Files and Settings -----------------------------------------------


Files <- read.xlsx2("W:/jiahu/Xavier/test/Experiments_status.xlsx",1)
Files <- Files[which(Files$R.processed == "y"),]

# Files <- Files[which(Files$MatLab.processed == "y"),]
Variables <- read.xlsx2("W:/jiahu/Xavier/test/Variables_to_select.xlsx",1)

Settings <- list()
Settings$time_interval <- 5 #interval in minutes
Settings$pixel_size <- 0.8260495552435883


# get data ----------------------------------------------------------------


dat_all <- list()
for (i in seq_along(Files$Analysis.folder)){
  
  load(as.character(Files$dat_file[i]))
  dat$Condition <- Files$Condition[i]
  dat$experiment <- Files$Experiment.number[i]
  dat$stagepos <- Files$Stagepos[i]
  dat$folder <- Files$Analysis.folder[i]
  dat$lab <- Files$Lab[i]
  dat$person <- Files$Person[i]
  dat_all[[i]] <- dat
  print(i)
}

dat <- do.call(rbind.data.frame, dat_all)


# select only some columns -----------------------------

# dat2 <- dat
# dat <- dat[,!substr(colnames(dat),1,3) %in% c("Loc", "Int")]
dat <- dat[,colnames(dat) %in% Variables$Variable[which(Variables$ToSelect == "x")]]


# Convert units -----------------------------------------------------------

dat2 <- dat
col_c1 <- colnames(dat) %in% as.character(Variables$Variable[which(Variables$ConversionType == 1)])
col_c2 <- colnames(dat) %in% as.character(Variables$Variable[which(Variables$ConversionType == 2)])
col_c3 <- colnames(dat) %in% as.character(Variables$Variable[which(Variables$ConversionType == 3)])


dat[,col_c1] <- dat[,col_c1]*Settings$pixel_size
dat[,col_c2] <- dat[,col_c2]*(Settings$pixel_size^2)
dat[,col_c3] <- (dat[,col_c3]/Settings$time_interval)*(Settings$pixel_size/1)*(60/1)




# Rename ------------------------------------------------------------------

names <- colnames(dat)

colnames(dat) <- as.character(Variables$NewName[(match(colnames(dat), Variables$Variable))])


# Compute extra variables -------------------------------------------------

dat <- dat %>% group_by(Lab, Person, Experiment, Technical_replicate, Cell_id) %>% arrange(Timepoint) %>% 
  mutate(Cells_Area_dif = c(NA, diff(Cells_Area)),
         Cells_Perimeter_dif = c(NA, diff(Cells_Perimeter)),
         Cells_Solidity_dif = c(NA, diff(Cells_Solidity)),
         Nuc_Area_dif = c(NA, diff(Nuc_Area)),
         Nuc_Perimeter_dif = c(NA, diff(Nuc_Perimeter)))





# Discard experiments -----------------------------------------------------
dat <- dat[which(!(dat$Lab == 3 & dat$Person == 3 & dat$Experiment == 1)),]
dat <- dat[which(!(dat$Lab == 2 & dat$Person == 1 & dat$Experiment == 3)),]
dat <- dat[which(!(dat$Lab == 2 & dat$Person == 2 & dat$Experiment == 1)),]

dat$Experiment[which((dat$Lab == 3 & dat$Person == 3 & dat$Experiment == 4))] = 1
dat$Experiment[which((dat$Lab == 2 & dat$Person == 1 & dat$Experiment == 4))] = 3
dat$Experiment[which((dat$Lab == 2 & dat$Person == 2 & dat$Experiment == 4))] = 1


# Apply outliers exclusions --------------------------------------------------------

val <- 1.5*IQR(dat$Cells_Area)+quantile(dat$Cells_Area,0.75)
dat <- dat[which(dat$Cells_Area<val),]

val <- 100
dat <- dat[which(dat$Nuc_Area>val),]


# Convert string to factor ----------------------------------------------
dat$Lab <- as.factor(dat$Lab)
dat$Person <- as.factor(dat$Person)
dat$Experiment <- as.factor(dat$Experiment)
dat$Condition <- as.factor(dat$Condition)
dat$Technical_replicate <- as.factor(dat$Technical_replicate)


# This is for Matteo ------------------------------------------------------


var_mat <- data.frame(variables = as.character(Variables$NewName)[which(!is.na(as.numeric(as.character(Variables$Order_for_Matteo))))],
           order = as.vector(na.omit(unique(as.numeric(as.character(Variables$Order_for_Matteo))))))

var_mat <- var_mat %>% arrange(order)

dat_m <- dat[,as.character(var_mat$variables)]


# Save in cache -----------------------------------------------------------
save(file="W:/jiahu/Xavier/test/dat_v2.R",dat)
save(file="W:/jiahu/Xavier/test/dat_m_v2.R",dat_m)



write.table(dat_m, file = "W:/jiahu/Xavier/test/dat_multimot_v2.csv", row.names = FALSE, col.names = TRUE, sep = ",", quote = FALSE)




