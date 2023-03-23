rm(list=ls())

## libraries----------
library(ggplot2)
library(lme4)
library(dplyr)
library(cowplot)
library(foreach)
library(doSNOW)
library(reshape2)
library(factoextra)
library(raster)
library(ggbeeswarm)
library(ggrepel)

## load data and prepare ----------

load("L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/dat_v2.R")

d1 <- data.frame(dat)


d1[,c("Cells_ICS",              
      "Nuc_INS")] <- d1[,c("Cells_ICS",              
                                 "Nuc_INS")]*0.8260495552435883/6
d1[,c("Timepoint")] <- (d1[,c("Timepoint")]-1)/12

levels(d1$Lab) <- paste0("L", levels(d1$Lab))
levels(d1$Person) <- paste0("P", levels(d1$Person))
levels(d1$Experiment) <- paste0("E", levels(d1$Experiment))





## Fig 1b ICS average amb boxplots   ------------------------------------------------------
file_name = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 1/Fig 1b.png'

d2 <- d1[,c("Timepoint", "Person", "Lab", "Experiment", "Condition", "Technical_replicate", "Cells_ICS")]

d2$Timepoint <- factor(d2$Timepoint)
d2 <- d2 %>% filter(Condition == "C")

d3 <- d2 %>% group_by(Lab, Person, Timepoint) %>% summarise(IQR_top = quantile(Cells_ICS, 0.75, na.rm = TRUE),
                                                            IQR_bottom = quantile(Cells_ICS, 0.25, na.rm = TRUE))
ggplot(d2, aes(Timepoint, Cells_ICS))+
  geom_boxplot(data = d2, aes(factor(Timepoint), Cells_ICS), coef = 0, outlier.shape = NA, col = "gray", size = 0.1)+
  geom_smooth(aes(col = Experiment, group = interaction(Experiment,Technical_replicate), linetype = Technical_replicate), size=0.8, se = FALSE)+
  geom_line(data = d3, aes(Timepoint, IQR_top, group = 1), linetype = "dashed", size = 0.1)+
  geom_line(data = d3, aes(Timepoint, IQR_bottom, group = 1), linetype = "dashed", size = 0.1)+
  facet_grid(Person~Lab)+
  # scale_x_continuous(breaks = seq(min(as.numeric(d2$Timepoint)), max(as.numeric(d2$Timepoint)), by = 5))+
  scale_x_discrete(breaks = seq(1, max(as.numeric(d2$Timepoint)), by = 1))+
  theme(legend.position = "bottom")+
  coord_cartesian(ylim = c(floor(min(d3$IQR_bottom, na.rm=TRUE)),
                           ceiling(max(d3$IQR_top, na.rm=TRUE))-0.8),
                  xlim = c(5,73))+
  scale_color_brewer(palette = "Dark2")

ggsave(file_name,  width = 20, height = 12, units = "cm", dpi = 600)

## Fig 1c PCA combine ------------------------------------------------------

file_name = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 1/Fig 1c/Fig 1c.png'
file_name_inset = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 1/Fig 1c/Fig 1c_inset.png'

#Perform PCA
d2 <- d1[,c("Cells_Area",             
            "Cells_Compactness",
            "Cells_Eccentricity",     
            "Cells_MajorAxisLength",
            "Cells_MaxFeretDiameter", 
            "Cells_MaximumRadius",
            "Cells_MeanRadius",   
            "Cells_MinFeretDiameter",
            "Cells_MinorAxisLength",
            "Cells_Perimeter",   
            "Cells_Solidity",         
            "Cells_Protrusions",
            "Cells_Retractions",      
            "Cells_ShortLivedRegions", 
            "Cells_ICS",              
            "Nuc_Area",
            "Nuc_INS",
            "Nuc_Perimeter",
            "Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint", "Folder", "image_number_old", "Cells_Center_X", "Cells_Center_Y", "ObjectNumber")]


d2 <- na.omit(d2)
d2 <- d2 %>% filter(Condition == "C")

d3 <- data.frame(scale(d2[,1:18], center = TRUE, scale = TRUE))
d3.pca <- princomp(d3)


scores <- data.frame(d3.pca$scores)

scores <- data.frame(scores, d2[,c("Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint","Folder", "image_number_old", "Cells_Center_X", "Cells_Center_Y", "ObjectNumber")])

# plot

xlim_p <- c(min(scores$Comp.1)-0.3, max(scores$Comp.1)+0.3)
ylim_p <- c(min(scores$Comp.2)-0.3, max(scores$Comp.2)+0.3)

scores_s <- scores # this is to create a "light" dataset that will be used for plotting, to avoid plotting too many overlapping observations. This has been callibrated for the figures to show the same plot as the full dataset would show.
scores_s$Comp.1_r <- round(scores_s$Comp.1,1)
scores_s$Comp.2_r <- round(scores_s$Comp.2,1)

scores_s <- scores_s %>% group_by(Comp.1_r, Comp.2_r) %>% sample_n(1)

ggplot(scores_s, aes(Comp.1, Comp.2))+
  geom_point( col = "gray")+
  coord_fixed(ratio = 1, xlim = xlim_p, ylim = ylim_p)+
  theme_cowplot()

ggsave(file_name,  width = 12, units = "cm", dpi = 600)

# this is to plot inset
ggplot(scores, aes(Comp.1, Comp.2))+  
  geom_point(data = scores_s, col = "gray")+
  stat_density2d(aes(alpha=..level.., fill=..level..), size=2, bins=20, geom="polygon") + 
  scale_fill_gradient(low = "yellow", high = "red") +
  scale_alpha(range = c(0.00, 0.5), guide = FALSE) +
  geom_density2d(colour="black", bins = 20)+
  coord_fixed(ratio = 0.75, xlim = xlim_p, ylim = ylim_p)
ggsave(file_name_inset,  width = 12, units = "cm", dpi = 600)

  


# option 1: generate grid

v <- seq(min(c(scores$Comp.1, scores$Comp.2)),
         max(c(scores$Comp.1, scores$Comp.2)), 2)

scores$quant_Comp.1 <- findInterval(scores$Comp.1, v)
scores$quant_Comp.2 <- findInterval(scores$Comp.2, v)


scores_3 <- scores

# this is to select regularly spaced observations
scores_3$dif_comp.1 <- scores_3$Comp.1-v[scores_3$quant_Comp.1]
scores_3$dif_comp.2 <- scores_3$Comp.2-v[scores_3$quant_Comp.2]
scores_3 <- scores_3 %>% filter(dif_comp.1 < 0.25 & dif_comp.2 < 0.25)

scores_3 <- scores_3 %>% 
  group_by(quant_Comp.1, quant_Comp.2) %>%
  sample_n(1)

# plot to help manually combine
file_name = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 1/Fig 1c/Fig 1c_helper.png'


# ggplot(scores_3, aes(Comp.1, Comp.2))+geom_point(data = scores, aes(Comp.1, Comp.2, col = "gray"))+geom_point()+coord_equal()
ggplot(scores_3, aes(Comp.1, Comp.2, col=factor(quant_Comp.1)))+
  geom_point(data = scores, aes(Comp.1, Comp.2), col = "gray")+
  geom_point()+  
  coord_fixed(ratio = 1, xlim = xlim_p, ylim = ylim_p)+
  theme(legend.position = "none")


ggsave(file_name,  width = 12, units = "cm", dpi = 600)


# save to file for matlab processing
scores_3$path_to_file <- paste0(gsub("\\\\", "/", as.character(scores_3$Folder)), "2 - CellProfiler results/CellMasks/CellMasks_0",
                                formatC(scores_3$image_number_old, width = 3, format = "d", flag = "0"), ".tif")


MatLab_transfer2 <- scores_3[,c("Cells_Center_X", "Cells_Center_Y", "path_to_file", "Comp.1", "Comp.2", "Cell_id","ObjectNumber","Lab","Person", "Experiment","Technical_replicate","Condition")]
write.csv( MatLab_transfer2, file="L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/MatLab_transfer_PCA_map_grid.csv",  row.names=FALSE, quote = FALSE )

scores$path_to_file <- paste0(gsub("\\\\", "/", as.character(scores$Folder)), "2 - CellProfiler results/CellMasks/CellMasks_0",
                                formatC(scores$image_number_old, width = 3, format = "d", flag = "0"), ".tif")
MatLab_transfer1 <-scores[,c("Cells_Center_X", "Cells_Center_Y", "path_to_file", "Comp.1", "Comp.2", "Cell_id","ObjectNumber","Lab","Person", "Experiment","Technical_replicate","Condition")]
write.csv(MatLab_transfer1, file = "L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/MatLab_transfer_PCA_data_all.csv",  row.names=FALSE, quote = FALSE)





## Fig 1d plot PCA labs person exeriments  --------------
file_name = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 1/Fig 1d.png'

d2 <- d1[,c("Cells_Area",             
            "Cells_Compactness",
            "Cells_Eccentricity",     
            "Cells_MajorAxisLength",
            "Cells_MaxFeretDiameter", 
            "Cells_MaximumRadius",
            "Cells_MeanRadius",   
            "Cells_MinFeretDiameter",
            "Cells_MinorAxisLength",
            "Cells_Perimeter",   
            "Cells_Solidity",         
            "Cells_Protrusions",
            "Cells_Retractions",      
            "Cells_ShortLivedRegions", 
            "Cells_ICS",              
            "Nuc_Area",
            "Nuc_INS",
            "Nuc_Perimeter",
            "Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint")]


d2 <- na.omit(d2)
d2 <- d2 %>% filter(Condition == "C")

# d3 <- data.frame(scale(d2[,which(substr(colnames(d2),1,5) == "Cells")], center = TRUE, scale = TRUE))
nmax <- 18
d3 <- data.frame(scale(d2[,1:nmax], center = TRUE, scale = TRUE))

# perform PCA 

d3.pca <- princomp(d3)

print(d3.pca)

plot(d3.pca , type = "l")
summary(d3.pca )
d3.pca $loadings

scores <- data.frame(d3.pca $scores)
scores <- data.frame(scores, d2[,c("Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint")])

scores_s <- scores
# scores_s <- scores[sample(1:length(scores$Comp.1),5000),]
scores_s$Person <- NULL
scores_s$Lab <- NULL

ggplot(scores, aes(Comp.1, Comp.2))+
  geom_point(data = scores_s, aes(Comp.1, Comp.2), col = "#303030", size = 0.7)+
  stat_density2d(geom="density2d", aes(color = Experiment),
                 size=0.6,
                 bins = 12,
                 contour=TRUE) +   
  coord_cartesian(ylim = c(-10,10), xlim = c(-5,24))+
  facet_grid(Person~Lab)+
  theme(legend.position = "none")+
  scale_color_brewer(palette = "Dark2")

ggsave(file_name,  width = 20, height = 12, units = "cm", dpi = 600)

## Fig 2a LMER --------------------------------------------------------------
file_name = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 2/Fig 2a.png'
file_name1 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 2/Fig 2a_1.png'
file_name2 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 2/Fig 2a_2.png'
file_name3 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 2/Fig 2a_3.png'
file_name4 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 2/Fig 2a_4.png'
file_name_boxplots1 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 2/Fig 2a_boxplots1.png'
file_name_boxplots2 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 2/Fig 2a_boxplots2.png'

#Perform PCA
d2 <- d1[,c("Cells_ICS",              
            "Cells_Area",             
            "Cells_Compactness",
            "Cells_Eccentricity",     
            "Cells_MajorAxisLength",
            "Cells_MaxFeretDiameter", 
            "Cells_MaximumRadius",
            "Cells_MeanRadius",   
            "Cells_MinFeretDiameter",
            "Cells_MinorAxisLength",
            "Cells_Perimeter",   
            "Cells_Solidity",         
            "Cells_Protrusions",
            "Cells_Retractions",      
            "Cells_ShortLivedRegions", 
            "Nuc_INS",
            "Nuc_Area",
            "Nuc_Perimeter",
            "Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint", "Folder", "image_number_old", "Cells_Center_X", "Cells_Center_Y")]

nmax <- 18 #define column up to which pca will be performed
n_max_obs <- 50000 
d2 <- na.omit(d2)
d2 <- d2 %>% filter(Condition == "C")

d3 <- data.frame(scale(d2[,1:nmax], center = TRUE, scale = TRUE))
d3.pca <- princomp(d3)

scores <- data.frame(d3.pca$scores)

d2$Cell_id2 <- paste0(d2$Lab,d2$Person,d2$Experiment,d2$Technical_replicate,d2$Condition,d2$Cell_id)


d2[,1:nmax] <- data.frame(scale(d2[,1:nmax], center = TRUE, scale = TRUE))
d2$Comp.1 <- scores$Comp.1
d2$Comp.2 <- scores$Comp.2
d2 <- d2[,c(1:nmax,31,32,19:30)]


d2 <- d2[sample(1:length(d2$Cells_Area), n_max_obs),]

# temp_parallel <- foreach(i=1:3, .packages = "lme4") %dopar% {
d2_results <- list()
for (i in 1:(nmax+2)){
  print(i)
  
  d3 <- d2[,i]
  d3 <- cbind(d3,d2[,c("Lab", "Person", "Experiment", "Technical_replicate", "Cell_id2")])
  
  colnames(d3)[1] <- "d3"
  
  data.d1.lmer <- lmer(d3~1+(1|Lab/Person/Experiment/Technical_replicate/Cell_id2),d3,REML=FALSE)    
  
  d2_results[[i]] <- data.d1.lmer
}




res_f <- data.frame()

for (i in 1:(nmax+2)){
  res_f <- rbind(res_f, data.frame(variable = colnames(d2)[i],
                                   VarCorr(d2_results[[i]])))
  
}
res <- res_f
#res$variable <- as.character(rownames(res))
res$variable2 <- res$variable



res2 <- melt(res[,c("variable2","sdcor","grp")])


temp <- res2
temp <- temp %>% group_by(variable2) %>% mutate(value2 = value/sum(value)) %>% as.data.frame

temp$grp <- factor(temp$grp)
levels(temp$grp) <- c("Cell", "Experiment", "Lab",  "Person", "Residual", "Technical_replicate")
# levels(temp$grp) <- c("Residual", "Cell", "Technical_replicate", "Experiment", "Person", "Lab")
temp$grp <- factor(temp$grp, levels = c("Residual", "Cell", "Technical_replicate", "Experiment", "Person", "Lab"))


temp$variable3 <- temp$variable2
temp$variable3 <- factor(temp$variable3, levels = as.character(temp %>% filter(grp %in% c("Lab", "Person", "Experiment", "Technical_replicate")) %>% group_by(variable2) %>% summarise(sum_val = sum(value)) %>% arrange(sum_val) %>% .$variable2))


temp$order <- temp$grp
levels(temp$order) <- 6:1
temp$order <- as.numeric(as.character(temp$order))
temp <- temp[with(temp, order(order)),]


custom_palette <- c("#f6ff62","#eca736","#9570d4","#55834a","#d2e3f2","#f9dede"     )

# plot variability as bars
p1 <- ggplot(temp, aes(x = (variable3), y = value, fill=factor(order)))+
  geom_bar(stat="identity", position= "fill", alpha=1, color = "black", width = 0.8)+ 
  coord_flip()+ 
  scale_fill_manual(values = custom_palette)+
  xlab("")+
  theme_cowplot()+
  theme(legend.position = "none",axis.text.y = element_text(size=20),axis.text.x = element_text(size=20))
ggsave(file_name1,  height = 20, width = 23, units = "cm", dpi = 600)

p2 <- ggplot(temp, aes(x = (variable3), y = value, fill=factor(order)))+
  geom_bar(stat="identity", alpha=1, color = "black", width = 0.8)+
  coord_flip()+
  scale_fill_manual(values = custom_palette)+
  theme(legend.position = "none")+
  xlab("") + 
  theme_cowplot()+
  theme(legend.position = "none",axis.text.y = element_text(size=20),axis.text.x = element_text(size=20))
ggsave(file_name2,  height = 20, width = 23, units = "cm", dpi = 600)

p3 <- ggplot(temp %>% filter(grp %in% c("Lab", "Person", "Experiment", "Technical_replicate")), aes(x = (variable3), y = value, fill=factor(order)))+geom_bar(stat="identity", alpha=1, color = "black", width = 0.8)+coord_flip()+scale_fill_manual(values = custom_palette[1:4])+theme(legend.position = "none")+xlab("") +  theme_cowplot() + theme(legend.position = "none",axis.text.y = element_text(size=20),axis.text.x = element_text(size=20))
ggsave(file_name3,  height = 20, width = 23, units = "cm", dpi = 600)

p4 <- ggplot(temp %>% filter(grp %in% c("Lab", "Person", "Experiment", "Technical_replicate")), aes(x = (variable3), y = value, fill=factor(order)))+geom_bar(stat="identity", position= "fill", alpha=1, color = "black", width = 0.8)+coord_flip()+scale_fill_manual(values = custom_palette[1:4])+theme(legend.position = "none")+xlab("") +  theme_cowplot() + theme(legend.position = "none",axis.text.y = element_text(size=20),axis.text.x = element_text(size=20))
ggsave(file_name4,  height = 20, width = 23, units = "cm", dpi = 600)


plot_grid(p1,p2, p3,p4, nrow = 2)
ggsave(file_name,  height = 14, width = 23, units = "cm", dpi = 600)

temp$source <- factor(temp$grp)
levels(temp$source) <- c("biological", "biological", "technical", "technical", "technical", "technical")
temp2 <- temp %>% group_by(variable2, source) %>% summarise(sum_val = sum(value2))
ggplot(temp2, aes(x = source, y = sum_val))+geom_boxplot()+geom_beeswarm(cex = 5)
ggsave(file_name_boxplots1,   height = 9, width = 7, units = "cm", dpi = 600)


levels(temp$grp) <- c("Temporal", "Cell", "Replicate", "Experiment", "Person", "Lab")
temp$grp <- factor(temp$grp, levels = c("Cell","Temporal", "Lab", "Person",  "Experiment", "Replicate"))
ggplot(temp, aes(x = grp, y = value2))+geom_boxplot()+geom_beeswarm(cex = 1.5)


ggsave(file_name_boxplots2,  height = 9, width = 14, units = "cm", dpi = 600)






## Fig 2b Cumulative variability. Also Supplementary fig 5 -----------------

# prepare
file_name_2 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 2/Fig 2b.png'
file_name_sup = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig Sup/Fig 5.png'
file_name_sup2 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig Sup/Fig 5_2.png'
file_name_sup3 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig Sup/Fig 5_3.png'
file_name_sup4 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig Sup/Fig 5_4.png'

#Select variables and Perform PCA
d2 <- d1[,c("Cells_Area",             
            "Cells_Compactness",
            "Cells_Eccentricity",     
            "Cells_MajorAxisLength",
            "Cells_MaxFeretDiameter", 
            "Cells_MaximumRadius",
            "Cells_MeanRadius",   
            "Cells_MinFeretDiameter",
            "Cells_MinorAxisLength",
            "Cells_Perimeter",   
            "Cells_Solidity",         
            "Cells_Protrusions",
            "Cells_Retractions",      
            "Cells_ShortLivedRegions", 
            "Cells_ICS",              
            "Nuc_Area",
            "Nuc_INS",
            "Nuc_Perimeter",
            "Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint", "Folder", "image_number_old")]

#define column up to which pca will be performed

n_max_obs <- 50000 
d2 <- na.omit(d2)

d2 <- d2 %>% filter(Condition == "C") %>% as.data.frame()


nmax <- 18
d3 <- data.frame(scale(d2[,1:nmax], center = TRUE, scale = TRUE))
d3.pca <- princomp(d3)
scores <- data.frame(d3.pca$scores)

d2 <- cbind(scores$Comp.1, d2)
colnames(d2)[1] <- "Comp.1"

d2$Cells_Comp.1 <- scores$Comp.1

d2$Cell_id2 <- paste0(d2$Lab,d2$Person,d2$Experiment,d2$Technical_replicate,d2$Condition,d2$Cell_id)

nmax <- 19 
d2[,2:nmax] <- data.frame(scale(d2[,2:nmax], center = TRUE, scale = TRUE))


# # process only for Comp.1 and ICS
# d2 <- d2[,c(1,16,20:32,2)]
# nmax <- 2

# Compute. this takes time
res <- list()
resr <- list()
lab <- list()
for (vars in 1:nmax){
  print(vars)
  res1 <- list(); res2 <- list(); res3 <- list(); res4 <- list(); res5 <- list(); res6 <- list(); res7 <- list(); res8 <- list()
  res1r <- list(); res2r <- list(); res3r <- list(); res4r <- list(); res5r <- list(); res6r <- list(); res7r <- list(); res8r <- list()
  lab_info1 <- list(); lab_info2 <- list(); lab_info3 <- list(); lab_info4 <- list(); lab_info5 <- list(); lab_info6 <- list(); lab_info7 <- list(); lab_info8 <- list()
  # Technical replicates 
  
  d2$cat <- paste0(d2$Lab,d2$Person, d2$Experiment, d2$Condition, d2$Technical_replicate)
  
  # 2 Technical replicates
  a <- d2 %>% group_by(Lab, Person, Experiment, Condition) %>% distinct(Technical_replicate) %>% arrange(Lab, Person, Experiment, Technical_replicate)
  # a$tr2 <- c("C2", "C3", "C1")
  a$tr2 <- rep(c("C2","C3", "C1"),dim(a)[1]/3)
  
  a$cat1 <- paste0(a$Lab,a$Person, a$Experiment, a$Condition, a$Technical_replicate)
  a$cat2 <- paste0(a$Lab,a$Person, a$Experiment, a$Condition, a$tr2)
  for (i in 1:length(a$Lab)){
    d2_temp <- d2[which(d2$cat %in% c(a$cat1[i], a$cat2[i])),]
    d2_temp$Var_int <- d2_temp[,vars] 
    if (dim(d2_temp)[1] > n_max_obs){d2_temp <- d2_temp[sample(1:length(d2_temp$Cells_Area), n_max_obs),]}  
    
    d1.lme <- lmer(Var_int~(+1|Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res1[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    lab_info1[[i]] <- unique(d2_temp$Lab)
    
    # random
    d2_temp$Var_int <- sample(d2_temp$Var_int, length(d2_temp$Var_int))
    d1.lme <- lmer(Var_int~(+1|Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res1r[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    
  }
  res$res1[[vars]] <- res1
  resr$res1r[[vars]] <- res1r
  lab$lab_info1[[vars]] <- lab_info1
  
  # 3 technical replicates
  a <- d2 %>% group_by(Lab, Person, Experiment) %>% distinct(Condition) %>% arrange(Lab, Person, Experiment)
  a$cat <- paste0(a$Lab,a$Person, a$Experiment, a$Condition)
  d2$cat <- paste0(d2$Lab,d2$Person, d2$Experiment, d2$Condition)
  for (i in 1:length(a$Lab)){
    d2_temp <- d2[which(d2$cat == a$cat[i]),]
    d2_temp$Var_int <- d2_temp[,vars] 
    if (dim(d2_temp)[1] > n_max_obs){d2_temp <- d2_temp[sample(1:length(d2_temp$Cells_Area), n_max_obs),]}
    
    d1.lme <- lmer(d2_temp[,vars]~(+1|Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res2[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    lab_info2[[i]] <- unique(d2_temp$Lab)
    
    
    # random
    d2_temp$Var_int <- sample(d2_temp$Var_int, length(d2_temp$Var_int))
    d1.lme <- lmer(Var_int~(+1|Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res2r[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    
  }
  
  res$res2[[vars]] <- res2
  resr$res2r[[vars]] <- res2r
  lab$lab_info2[[vars]] <- lab_info2
  
  # 2 experiments
  a <- d2 %>% group_by(Lab, Person, Condition) %>% distinct(Experiment) %>% arrange(Lab, Person, Experiment)
  # a$e2 <- c("E2", "E3", "E1")
  a$e2 <- rep(c("E2","E3", "E1"),dim(a)[1]/3)
  
  
  a$cat1 <- paste0(a$Lab,a$Person, a$Experiment, a$Condition)
  a$cat2 <- paste0(a$Lab,a$Person, a$e2, a$Condition)
  
  d2$cat <- paste0(d2$Lab,d2$Person, d2$Experiment, d2$Condition)
  
  for (i in 1:length(a$Lab)){
    d2_temp <- d2[which(d2$cat %in% c(a$cat1[i], a$cat2[i])),]
    d2_temp$Var_int <- d2_temp[,vars] 
    if (dim(d2_temp)[1] > n_max_obs){d2_temp <- d2_temp[sample(1:length(d2_temp$Cells_Area), n_max_obs),]}
    
    d1.lme <- lmer(Var_int~(+1|Experiment/Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res3[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    lab_info3[[i]] <- unique(d2_temp$Lab)
    
    # random
    d2_temp$Var_int <- sample(d2_temp$Var_int, length(d2_temp$Var_int))
    d1.lme <- lmer(Var_int~(+1|Experiment/Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res3r[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    
  }
  
  res$res3[[vars]] <- res3
  resr$res3r[[vars]] <- res3r
  lab$lab_info3[[vars]] <- lab_info3
  
  
  # 3 experiments
  a <- d2 %>% group_by(Lab, Person) %>% distinct(Condition) %>% arrange(Lab, Person)
  a$cat <- paste0(a$Lab,a$Person, a$Condition)
  d2$cat <- paste0(d2$Lab,d2$Person, d2$Condition)
  for (i in 1:length(a$Lab)){
    d2_temp <- d2[which(d2$cat == a$cat[i]),]
    d2_temp$Var_int <- d2_temp[,vars] 
    if (dim(d2_temp)[1] > n_max_obs){d2_temp <- d2_temp[sample(1:length(d2_temp$Cells_Area), n_max_obs),]}
    
    d1.lme <- lmer(Var_int~(+1|Experiment/Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res4[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    lab_info4[[i]] <- unique(d2_temp$Lab)
    
    # random
    d2_temp$Var_int <- sample(d2_temp$Var_int, length(d2_temp$Var_int))
    d1.lme <- lmer(Var_int~(+1|Experiment/Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res4r[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    
  }
  
  res$res4[[vars]] <- res4
  resr$res4r[[vars]] <- res4r
  lab$lab_info4[[vars]] <- lab_info4  
  
  # 2 persons
  a <- d2 %>% group_by(Lab, Condition) %>% distinct(Person) %>% arrange(Lab, Person)
  # a$p2 <- c("P2", "P3", "P1")
  a$p2 <- rep(c("P2","P3", "P1"),dim(a)[1]/3)
  
  
  a$cat1 <- paste0(a$Lab, a$Person, a$Condition)
  a$cat2 <- paste0(a$Lab, a$p2, a$Condition)
  
  d2$cat <- paste0(d2$Lab,d2$Person, d2$Condition)
  
  for (i in 1:length(a$Lab)){
    d2_temp <- d2[which(d2$cat %in% c(a$cat1[i], a$cat2[i])),]
    d2_temp$Var_int <- d2_temp[,vars] 
    if (dim(d2_temp)[1] > n_max_obs){d2_temp <- d2_temp[sample(1:length(d2_temp$Cells_Area), n_max_obs),]}
    
    d1.lme <- lmer(Var_int~(+1|Person/Experiment/Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res5[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    lab_info5[[i]] <- unique(d2_temp$Lab)
    
    # random
    d2_temp$Var_int <- sample(d2_temp$Var_int, length(d2_temp$Var_int))
    d1.lme <- lmer(Var_int~(+1|Person/Experiment/Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res5r[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    
  }
  # 3 persons
  res$res5[[vars]] <- res5
  resr$res5r[[vars]] <- res5r
  lab$lab_info5[[vars]] <- lab_info5
  
  a <- d2 %>% group_by(Lab) %>% distinct(Condition) %>% arrange(Lab)
  a$cat <- paste0(a$Lab, a$Condition)
  d2$cat <- paste0(d2$Lab,d2$Condition)
  for (i in 1:length(a$Lab)){
    d2_temp <- d2[which(d2$cat == a$cat[i]),]
    d2_temp$Var_int <- d2_temp[,vars] 
    if (dim(d2_temp)[1] > n_max_obs){d2_temp <- d2_temp[sample(1:length(d2_temp$Cells_Area), n_max_obs),]}
    
    d1.lme <- lmer(Var_int~(+1|Person/Experiment/Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res6[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    lab_info6[[i]] <- unique(d2_temp$Lab)
    
    # random
    d2_temp$Var_int <- sample(d2_temp$Var_int, length(d2_temp$Var_int))
    d1.lme <- lmer(Var_int~(+1|Person/Experiment/Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res6r[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    
  }
  res$res6[[vars]] <- res6
  resr$res6r[[vars]] <- res6r
  lab$lab_info6[[vars]] <- lab_info6
  
  
  # 2 labs
  
  a <- d2 %>% group_by(Condition) %>% distinct(Lab) %>% arrange(Lab)
  # a$l2 <- c("L2", "L3", "L1")
  a$l2 <- rep(c("L2","L3", "L1"),dim(a)[1]/3)
  
  
  a$cat1 <- paste0(a$Lab, a$Condition)
  a$cat2 <- paste0(a$l2, a$Condition)
  
  d2$cat <- paste0(d2$Lab, d2$Condition)
  
  for (i in 1:length(a$Lab)){
    d2_temp <- d2[which(d2$cat %in% c(a$cat1[i], a$cat2[i])),]
    d2_temp$Var_int <- d2_temp[,vars] 
    if (dim(d2_temp)[1] > n_max_obs){d2_temp <- d2_temp[sample(1:length(d2_temp$Cells_Area), n_max_obs),]}
    
    d1.lme <- lmer(Var_int~(+1|Lab/Person/Experiment/Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res7[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    lab_info7[[i]] <- NA
    
    # random
    d2_temp$Var_int <- sample(d2_temp$Var_int, length(d2_temp$Var_int))
    d1.lme <- lmer(Var_int~(+1|Lab/Person/Experiment/Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res7r[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    
  }
  res$res7[[vars]] <- res7
  resr$res7r[[vars]] <- res7r
  lab$lab_info7[[vars]] <- lab_info7
  
  
  # 3 labs
  for (i in 1:length(a$Lab)){
    d2_temp <- d2
    d2_temp$Var_int <- d2_temp[,vars] 
    if (dim(d2_temp)[1] > n_max_obs){d2_temp <- d2_temp[sample(1:length(d2_temp$Cells_Area), n_max_obs),]}
    
    d1.lme <- lmer(Var_int~(+1|Lab/Person/Experiment/Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res8[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    lab_info8[[i]] <- NA
    
    # random
    d2_temp$Var_int <- sample(d2_temp$Var_int, length(d2_temp$Var_int))
    d1.lme <- lmer(Var_int~(+1|Lab/Person/Experiment/Technical_replicate/Cell_id2), d2_temp, REML=TRUE)
    res8r[[i]] <- data.frame(VarCorr(d1.lme),iter = i)
    
  }
  
  res$res8[[vars]] <- res8
  resr$res8r[[vars]] <- res8r
  lab$lab_info8[[vars]] <- lab_info8
}

save(resr,file = "L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/simulations/resr")
save(res,file = "L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/simulations/res")
save(lab,file = "L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/simulations/lab")
# postprocess

load("L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/simulations/resr")
load("L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/simulations/res")
load("L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/simulations/lab")

res_f <- data.frame()
res_fr <- data.frame()

for (i_level in 1:8){ #simulation level
  for (i_vars in 1:length(res[[i_level]])){ #variable level
    for (i_simulations in 1:length(res[[i_level]][[i_vars]])){ #sub_simulation level
      res_f <- rbind(res_f, data.frame(res[[i_level]][[i_vars]][[i_simulations]],
                                       i_level = i_level,
                                       i_vars = i_vars,
                                       i_simulations = i_simulations,
                                       lab = lab[[i_level]][[i_vars]][[i_simulations]]))
      res_fr <- rbind(res_fr, data.frame(resr[[i_level]][[i_vars]][[i_simulations]],
                                         i_level = i_level,
                                         i_vars = i_vars,
                                         i_simulations = i_simulations,
                                         lab = lab[[i_level]][[i_vars]][[i_simulations]]))
      
      
      
    }
  }
}

res_f$random <- "NO"
res_fr$random <- "YES"

res_f <- rbind(res_f, res_fr)

## this is to name levels
res_f$i_vars <- factor(res_f$i_vars)
levels(res_f$i_vars) <- colnames(d2)[1:nmax]


res_f$i_level <- factor(res_f$i_level)
levels(res_f$i_level) <- paste0("L", levels(res_f$i_level))

res_f$i_simulations <- factor(res_f$i_simulations)
levels(res_f$i_simulations) <- paste0("Sim_", levels(res_f$i_simulations))


res_f$level <- factor(substr(res_f$grp,1,4))

## compute technical contribution and prepare for plotting
res_f$source <- "technical"
res_f$source[which(res_f$level %in% c("Cell","Resi"))] <- "biological"



res_all2 <- res_f %>% group_by(lab, random, i_level, i_vars, i_simulations, source) %>% summarise(sd_val = sum(sdcor))

res_all2 <- dcast(res_all2[,c("lab", "random", "i_level",  "i_vars", "i_simulations", "source", "sd_val")], 
                  lab+random+i_level+ i_vars+ i_simulations ~ source, value.var="sd_val")

res_all2$technical_contribution <- res_all2$technical/(res_all2$biological+res_all2$technical)


##  this is to plot, conversion of i_level to numeric
res_all2$round <- res_all2$i_level
res_all2$round <- as.numeric(as.character(gsub((res_all2$round), pattern = "L", replacement = "")))


final <- res_all2 %>% group_by(random,round, i_vars) %>% summarise(mean_sd_val = mean(technical_contribution)) #this is to show average line
col123 <- c("#ff0000", "#C0C0C0") 

custom_palette <- c("#f9dede", "#d2e3f2", "#55834a", "#9570d4", "#eca736", "#f6ff62")


# plot 1
ggplot(res_all2 %>% filter(random == "NO", i_vars %in% c("Cells_ICS","Comp.1")), aes(factor(round), technical_contribution))+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = -0.5, xmax=2.5, aes(group = 1), fill="#55834a", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 2.5, xmax=4.5, aes(group = 1), fill="#9570d4", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 4.5, xmax=6.5, aes(group = 1), fill="#eca736", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 6.5, xmax=8.5, aes(group = 1), fill="#f6ff62", alpha = 0.5)+
  geom_boxplot(outlier.shape = NA)+
  geom_point(data = final %>%  filter(i_vars %in% c("Cells_ICS","Comp.1")), aes(round, mean_sd_val, col = random), size=2)+
  geom_line(data = final %>%  filter(i_vars %in% c("Cells_ICS","Comp.1")), aes(round, mean_sd_val, col = random), size=1.5)+
  # geom_jitter(size=3, aes(col=lab))+
  scale_x_discrete(labels = rep(2:3,4))+
  # scale_color_manual(values=col123)+
  xlab("")+
  ylab("")+
  theme_bw()+
  theme(panel.border = element_rect(colour="black", size=2, fill=NA),
        text = element_text(size=16),
        legend.position = "none",
        legend.title=element_blank())+
  coord_cartesian(ylim = c(0,0.5))+
  # ggtitle("Area")+
  facet_wrap(~i_vars, ncol = 1)

ggsave(file_name_2, height = 10, units = "cm", dpi = 600)

# plot supplementary

ggplot(res_all2 %>% filter(random == "NO", !i_vars %in% c("Cells_ICS","Comp.1")), aes(factor(round), technical_contribution))+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = -0.5, xmax=2.5, aes(group = 1), fill="#55834a", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 2.5, xmax=4.5, aes(group = 1), fill="#9570d4", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 4.5, xmax=6.5, aes(group = 1), fill="#eca736", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 6.5, xmax=8.5, aes(group = 1), fill="#f6ff62", alpha = 0.5)+
  geom_boxplot(outlier.shape = NA)+
  geom_point(data = final %>%  filter(!i_vars %in% c("Cells_ICS","Comp.1")), aes(round, mean_sd_val, col = random), size=2)+
  geom_line(data = final %>%  filter(!i_vars %in% c("Cells_ICS","Comp.1")), aes(round, mean_sd_val, col = random), size=1.5)+
  # geom_jitter(size=3, aes(col=lab))+
  scale_x_discrete(labels = rep(2:3,4))+
  # scale_color_manual(values=col123)+
  xlab("")+
  ylab("")+
  theme_bw()+
  theme(panel.border = element_rect(colour="black", size=2, fill=NA),
        text = element_text(size=16),
        legend.position = "none",
        legend.title=element_blank())+
  coord_cartesian(ylim = c(0,0.5))+
  # ggtitle("Area")+
  facet_wrap(~i_vars, ncol = 4)

ggsave(file_name_sup, height = 28, width = 28, units = "cm", dpi = 600)


# plot supplementary with different y scale
ggplot(res_all2 %>% filter(random == "NO", i_vars %in% c("Nuc_Area")), aes(factor(round), technical_contribution))+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = -0.5, xmax=2.5, aes(group = 1), fill="#55834a", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 2.5, xmax=4.5, aes(group = 1), fill="#9570d4", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 4.5, xmax=6.5, aes(group = 1), fill="#eca736", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 6.5, xmax=8.5, aes(group = 1), fill="#f6ff62", alpha = 0.5)+
  geom_boxplot(outlier.shape = NA)+
  geom_point(data = final %>%  filter(i_vars %in% c("Nuc_Area")), aes(round, mean_sd_val, col = random), size=2)+
  geom_line(data = final %>%  filter(i_vars %in% c("Nuc_Area")), aes(round, mean_sd_val, col = random), size=1.5)+
  # geom_jitter(size=3, aes(col=lab))+
  scale_x_discrete(labels = rep(2:3,4))+
  # scale_color_manual(values=col123)+
  xlab("")+
  ylab("")+
  theme_bw()+
  theme(panel.border = element_rect(colour="black", size=2, fill=NA),
        text = element_text(size=16),
        legend.position = "none",
        legend.title=element_blank())+
  coord_cartesian(ylim = c(0,0.9))+
  # ggtitle("Area")+
  facet_wrap(~i_vars, ncol = 1)

ggsave(file_name_sup2, height = 10, units = "cm", dpi = 600)


ggplot(res_all2 %>% filter(random == "NO", i_vars %in% c("Nuc_Perimeter")), aes(factor(round), technical_contribution))+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = -0.5, xmax=2.5, aes(group = 1), fill="#55834a", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 2.5, xmax=4.5, aes(group = 1), fill="#9570d4", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 4.5, xmax=6.5, aes(group = 1), fill="#eca736", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 6.5, xmax=8.5, aes(group = 1), fill="#f6ff62", alpha = 0.5)+
  geom_boxplot(outlier.shape = NA)+
  geom_point(data = final %>%  filter(i_vars %in% c("Nuc_Perimeter")), aes(round, mean_sd_val, col = random), size=2)+
  geom_line(data = final %>%  filter(i_vars %in% c("Nuc_Perimeter")), aes(round, mean_sd_val, col = random), size=1.5)+
  # geom_jitter(size=3, aes(col=lab))+
  scale_x_discrete(labels = rep(2:3,4))+
  # scale_color_manual(values=col123)+
  xlab("")+
  ylab("")+
  theme_bw()+
  theme(panel.border = element_rect(colour="black", size=2, fill=NA),
        text = element_text(size=16),
        legend.position = "none",
        legend.title=element_blank())+
  coord_cartesian(ylim = c(0,0.6))+
  # ggtitle("Area")+
  facet_wrap(~i_vars, ncol = 1)

ggsave(file_name_sup3, height = 10, units = "cm", dpi = 600)

ggplot(res_all2 %>% filter(random == "NO", i_vars %in% c("Cells_Solidity")), aes(factor(round), technical_contribution))+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = -0.5, xmax=2.5, aes(group = 1), fill="#55834a", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 2.5, xmax=4.5, aes(group = 1), fill="#9570d4", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 4.5, xmax=6.5, aes(group = 1), fill="#eca736", alpha = 0.5)+
  geom_rect(ymin = -Inf, ymax = Inf, xmin = 6.5, xmax=8.5, aes(group = 1), fill="#f6ff62", alpha = 0.5)+
  geom_boxplot(outlier.shape = NA)+
  geom_point(data = final %>%  filter(i_vars %in% c("Cells_Solidity")), aes(round, mean_sd_val, col = random), size=2)+
  geom_line(data = final %>%  filter(i_vars %in% c("Cells_Solidity")), aes(round, mean_sd_val, col = random), size=1.5)+
  # geom_jitter(size=3, aes(col=lab))+
  scale_x_discrete(labels = rep(2:3,4))+
  # scale_color_manual(values=col123)+
  xlab("")+
  ylab("")+
  theme_bw()+
  theme(panel.border = element_rect(colour="black", size=2, fill=NA),
        text = element_text(size=16),
        legend.position = "none",
        legend.title=element_blank())+
  coord_cartesian(ylim = c(0,0.8))+
  # ggtitle("Area")+
  facet_wrap(~i_vars, ncol = 1)

ggsave(file_name_sup4, height = 10, units = "cm", dpi = 600)

## Fig Supp 1 PCA ------------------------------------------------------------

file_name = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig Sup/Fig Sup 1a.png'
# subplot 1, variables plot

d2 <- d1[,c("Cells_Area",             
            "Cells_Compactness",
            "Cells_Eccentricity",     
            "Cells_MajorAxisLength",
            "Cells_MaxFeretDiameter", 
            "Cells_MaximumRadius",
            "Cells_MeanRadius",   
            "Cells_MinFeretDiameter",
            "Cells_MinorAxisLength",
            "Cells_Perimeter",   
            "Cells_Solidity",         
            "Cells_Protrusions",
            "Cells_Retractions",      
            "Cells_ShortLivedRegions", 
            "Cells_ICS",              
            "Nuc_Area",
            "Nuc_INS",
            "Nuc_Perimeter",
            "Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint")]


d2 <- na.omit(d2)
d2 <- d2 %>% filter(Condition == "C")

# d3 <- data.frame(scale(d2[,which(substr(colnames(d2),1,5) == "Cells")], center = TRUE, scale = TRUE))
nmax <- 18
d3 <- data.frame(scale(d2[,1:nmax], center = TRUE, scale = TRUE))
# perform PCA 

# colnames(d3)  <- gsub("Cells_", "", colnames(d3))
d3.pca <- princomp(d3)
# d3.pca <- prcomp(d3)


df.var <- facto_summarize(d3.pca,element = "var")
df.var$origin <- 0

ggplot(df.var, aes(Dim.1,Dim.2))+
  geom_point()+
  geom_segment(aes(x = origin, y = origin, xend = Dim.1, yend = Dim.2),  arrow = arrow(length = unit(0.03, "npc")))+
  geom_text_repel(label = df.var$name, color = "red")+
  geom_vline(xintercept = 0, linetype = "dashed")+
  geom_hline(yintercept = 0, linetype = "dashed")+
  xlim(c(-1.2, 1.2))+
  ylim(c(-1,1))+
  coord_equal()+
  # theme(axis.line = element_blank())+
  xlab("Comp.1")+
  ylab("Comp.2")

ggsave(file_name,  width = 20, height = 20, units = "cm", dpi = 600)  

# subplot 2, proportion of variance explained
file_name = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig Sup/Fig Sup 1b.png'

eig <- (d3.pca$sdev)^2/sum((d3.pca$sdev)^2)*100
eig <- eig[1:10, drop = FALSE]

df.eig <- data.frame(dim = factor(1:length(eig)), eig = eig)

ggplot(df.eig, aes(dim, eig, group = 1))+
  geom_bar(stat = "identity")+
  ylab("Percentage of explained variances")+
  xlab("Dimensions")

ggsave(file_name,  width = 20, height = 12, units = "cm", dpi = 600)

# subplot 3, PCA plot for labs, persons and experiments in treatment

file_name = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig Sup/Fig Sup 1c.png'

d2 <- d1[,c("Cells_Area",             
            "Cells_Compactness",
            "Cells_Eccentricity",     
            "Cells_MajorAxisLength",
            "Cells_MaxFeretDiameter", 
            "Cells_MaximumRadius",
            "Cells_MeanRadius",   
            "Cells_MinFeretDiameter",
            "Cells_MinorAxisLength",
            "Cells_Perimeter",   
            "Cells_Solidity",         
            "Cells_Protrusions",
            "Cells_Retractions",      
            "Cells_ShortLivedRegions", 
            "Cells_ICS",              
            "Nuc_Area",
            "Nuc_INS",
            "Nuc_Perimeter",
            "Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint")]


d2 <- na.omit(d2)
d2 <- d2 %>% filter(Condition == "T")
set.seed(123)
d2 <- d2[sample(1:length(d2$Cell_id), length(d2$Cell_id)*0.5),]

# d3 <- data.frame(scale(d2[,which(substr(colnames(d2),1,5) == "Cells")], center = TRUE, scale = TRUE))
nmax <- 18
d3 <- data.frame(scale(d2[,1:nmax], center = TRUE, scale = TRUE))

# perform PCA 


d3.pca <- princomp(d3)

scores <- data.frame(d3.pca $scores)
scores <- data.frame(scores, d2[,c("Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint")])

scores_s <- scores
# scores_s <- scores[sample(1:length(scores$Comp.1),5000),]
scores_s$Person <- NULL
scores_s$Lab <- NULL

ggplot(scores, aes(Comp.1, Comp.2))+
  geom_point(data = scores_s, aes(Comp.1, Comp.2), col = "#303030", size = 0.7)+
  stat_density2d(geom="density2d", aes(color = Experiment),
                 size=0.6,
                 bins = 6,
                 contour=TRUE) +   
  coord_cartesian(ylim = c(-10,10), xlim = c(-5,24))+
  facet_grid(Person~Lab)+
  theme(legend.position = "none")+
  scale_color_brewer(palette = "Dark2")


ggsave(file_name,  width = 20, height = 12, units = "cm", dpi = 600)








## Fig Supp 2 PCA stagepos detail ------------------------------------------

file_name = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig Sup/Fig 2.png'

d2 <- d1[,c("Cells_Area",             
            "Cells_Compactness",
            "Cells_Eccentricity",     
            "Cells_MajorAxisLength",
            "Cells_MaxFeretDiameter", 
            "Cells_MaximumRadius",
            "Cells_MeanRadius",   
            "Cells_MinFeretDiameter",
            "Cells_MinorAxisLength",
            "Cells_Perimeter",   
            "Cells_Solidity",         
            "Cells_Protrusions",
            "Cells_Retractions",      
            "Cells_ShortLivedRegions", 
            "Cells_ICS",              
            "Nuc_Area",
            "Nuc_INS",
            "Nuc_Perimeter",
            "Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint")]


d2 <- na.omit(d2)
d2 <- d2 %>% filter(Condition == "C")

# d3 <- data.frame(scale(d2[,which(substr(colnames(d2),1,5) == "Cells")], center = TRUE, scale = TRUE))
nmax <- 18
d3 <- data.frame(scale(d2[,1:nmax], center = TRUE, scale = TRUE))

# perform PCA 

d3.pca <- princomp(d3)

pl <- list()
for (i in 1:length(unique(d2$Lab))){
  
  
  scores <- data.frame(d3.pca $scores)
  scores <- data.frame(scores, d2[,c("Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint")])
  
  scores <- scores[which(scores$Lab == paste0("L", i)),]
  
  scores_s <- scores
  # scores_s <- scores[sample(1:length(scores$Comp.1),5000),]
  scores_s$Person <- NULL
  scores_s$Lab <- NULL
  scores_s$Experiment <- NULL
  
  scores_s$Technical_replicate <- NULL
  
  pl[[i]] <- ggplot(scores, aes(Comp.1, Comp.2))+
    geom_point(data = scores_s, aes(Comp.1, Comp.2), col = "#303030", size = 0.7)+
    stat_density2d(geom="density2d", aes(color = Technical_replicate),
                   size=0.6,
                   bins = 6,
                   contour=TRUE) +   
    coord_cartesian(ylim = c(-10,10), xlim = c(-5,24))+
    facet_grid(Experiment~Person)+
    theme(legend.position = "none")+
    ggtitle(paste0("L", i))
  
}

plot_grid(plotlist = pl, ncol = 1)


ggsave(file_name,  width = 20, height = 36, units = "cm", dpi = 600)









## Fig Supp 3 ------------------------------------------------------
colnames(d1)

vars_of_interest <- c(4,7,8,10,13:30, 37:41)
for (i in 1:length(vars_of_interest)){
  file_name <- paste0(file_name = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig Sup/Fig 3/Fig 3_',
                      i,'_',colnames(d1)[vars_of_interest[i]], '.png')
  cols_n <- c(which(colnames(d1) %in% c("Timepoint", "Person", "Lab", "Experiment", "Condition", "Technical_replicate")),vars_of_interest[i])
  d2 <- d1[,cols_n]
  d2$Timepoint <- factor(d2$Timepoint)
  d2 <- d2 %>% filter(Condition == "C")
  colnames(d2)[7] <- "var_temp"
  d3 <- d2 %>% group_by(Lab, Person, Timepoint) %>% summarise(IQR_top = quantile(var_temp, 0.75, na.rm = TRUE),
                                                              IQR_bottom = quantile(var_temp, 0.25, na.rm = TRUE))
  ggplot(d2, aes(Timepoint, var_temp))+
    geom_boxplot(data = d2, aes(factor(Timepoint), var_temp), coef = 0, outlier.shape = NA, col = "gray", size = 0.1)+
    geom_smooth(aes(col = Experiment, group = interaction(Experiment,Technical_replicate), linetype = Technical_replicate), size=0.8, se = FALSE)+
    geom_line(data = d3, aes(Timepoint, IQR_top, group = 1), linetype = "dashed", size = 0.1)+
    geom_line(data = d3, aes(Timepoint, IQR_bottom, group = 1), linetype = "dashed", size = 0.1)+
    facet_grid(Person~Lab)+
    # scale_x_continuous(breaks = seq(min(as.numeric(d2$Timepoint)), max(as.numeric(d2$Timepoint)), by = 5))+
    scale_x_discrete(breaks = seq(1, max(as.numeric(d2$Timepoint)), by = 1))+
    theme(legend.position = "bottom")+
    coord_cartesian(ylim = c(floor(min(d3$IQR_bottom, na.rm=TRUE)),
                             ceiling(max(d3$IQR_top, na.rm=TRUE))),
                    xlim = c(5,73))+
    scale_color_brewer(palette = "Dark2")+
    # ggtitle(colnames(d1)[vars_of_interest[i]])+
    ylab(colnames(d1)[vars_of_interest[i]])+
    xlab("time(h)")
  ggsave(file_name,  width = 20, height = 12, units = "cm", dpi = 600)
}

# Fig Supp 4 LMER number of cells -----------------------------------------


file_name = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig Sup/Fig 4.png'

d_count <- d1 %>% filter(Condition == "C") %>%  group_by(Lab, Person, Experiment, Technical_replicate) %>% summarise(n_val = length(unique(Cell_id)))

p1 <- ggplot(d_count, aes(Experiment, n_val, col = Technical_replicate))+
  geom_bar(stat = "Identity",  size = 1,position = position_dodge(width = 0.85), width = 0.75)+
  facet_grid(Person~Lab)+
  theme(legend.position = "bottom")+
  ylab("number of identified cells")

d1.lme <- lmer(n_val  ~1 +(1|Lab/Person/Experiment ), d_count, REML = TRUE)

a <- data.frame(VarCorr(d1.lme))
a$grp <- factor(a$grp)

levels(a$grp) <- c("Experiment", "Lab", "Person", "Replicate")
a$grp <- factor(a$grp, levels = c("Lab", "Person", "Experiment", "Replicate"))

p2 <-ggplot(a, aes(grp,sdcor))+
  geom_bar(stat= "Identity")+
  ylab("Variance Components")+
  xlab("Levels")

plot_grid(p1,p2, ncol = 2)

ggsave(file_name, height = 12,  units = "cm", dpi = 600)


## Fig 3 code
library(lattice)
library(latticeExtra)
library(RColorBrewer)

file_name1 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 3/Fig 3a_1.png'
file_name2 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 3/Fig 3a_2.png'
file_name3 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 3/Fig 3a_3.png'
file_name4 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 3/Fig 3a_4.png'




#Select variables and plot
d2 <- d1[,c("Cells_ICS",              
            "Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint")]


d2 <- na.omit(d2)

d2$Cell_id2 <- paste0(d2$Lab,d2$Person,d2$Experiment,d2$Technical_replicate,d2$Condition,d2$Cell_id)


n_max_obs = dim(d2)[1] #50000

set.seed(123)
d3 <- d2[sample(1:length(d2$Cells_ICS), n_max_obs),]


d3$cat <- paste0(d3$Lab,d3$Person, d3$Experiment, d3$Technical_replicate, d3$Condition)
d3_h <- d3 %>% group_by(Lab, Person, Experiment, Technical_replicate, Condition, cat) %>% summarise(mean_val = mean(Cells_ICS))

d3_h$cat <- factor(d3_h$cat)
d3_h$cat <- factor(d3_h$cat, levels = d3_h$cat[order(d3_h$mean_val)])

d3$cat <- factor(d3$cat, levels = levels(d3_h$cat))


# lmer
d1.lme <- lmer(Cells_ICS ~Condition +(1|Lab/Person/Experiment/Technical_replicate/ Cell_id2), d3, REML = TRUE)

val1 <- data.frame(ranef(d1.lme)[2], Technical_replicate = rownames(data.frame(ranef(d1.lme)[2])), row.names=NULL)
val2 <- data.frame(ranef(d1.lme)[3], Experiment = rownames(data.frame(ranef(d1.lme)[3])))
val3 <- data.frame(ranef(d1.lme)[4], Person = rownames(data.frame(ranef(d1.lme)[4])))
val4 <- data.frame(ranef(d1.lme)[5], Lab = rownames(data.frame(ranef(d1.lme)[5])))

val1$Experiment <- substr(val1$Technical_replicate,4,unique(nchar(as.character(val1$Technical_replicate))))
val1$Person <- substr(val1$Experiment, 4, unique(nchar(as.character(val1$Experiment))))
# val1$Technical_replicate <- substr(val1$Technical_replicate,4,3)
val1$Lab <- substr(val1$Person, 4, unique(nchar(as.character(val1$Person))))

# val2$Experiment <- substr(val2$experiment,1,7)
# val3$Person <- substr(val3$person,1,3)


val1$X.Intercept.experiment <- val2$X.Intercept.[match(val1$Experiment, val2$Experiment)]
val1$X.Intercept.person <- val3$X.Intercept.[match(val1$Person, val3$Person)]
val1$X.Intercept.lab <- val4$X.Intercept.[match(val1$Lab, val4$Lab)]



val1$sum_Intercepts <- val1$X.Intercept. + val1$X.Intercept.experiment + val1$X.Intercept.person+val1$X.Intercept.lab

d3$fixef_c1 <- 0
d3$fixef_c1[which(substr(d3$Technical_replicate, 1, 1) == "T")] <- as.numeric(fixef(d1.lme)[2])


d3$cat2 <- paste(d3$Technical_replicate, d3$Experiment, d3$Person,  d3$Lab, sep = ":")
d3$Cells_ICS_predicted1 <- d3$Cells_ICS-val1$sum_Intercepts[match(d3$cat2, val1$Technical_replicate)]+d3$fixef_c1




d3$cat3 <- paste0(d3$Lab,d3$Person, d3$Experiment, d3$Technical_replicate, d3$Condition)
d3_h <- d3 %>% group_by(Lab, Person, Experiment, Technical_replicate, Condition, cat3) %>% summarise(mean_val = mean(Cells_ICS_predicted1))

d3_h$cat3 <- factor(d3_h$cat3)
d3_h$cat3 <- factor(d3_h$cat3, levels = d3_h$cat3[order(d3_h$mean_val)])

d3$cat3 <- factor(d3$cat3, levels = levels(d3_h$cat3))

write.csv(d3, file = "L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/ICS_data_JJ.csv",  row.names=FALSE, quote = FALSE)


p1 <- ggplot(d3, aes(cat,Cells_ICS, col = Condition))+#,  col = Lab, fill=Condition))+
  geom_boxplot(outlier.shape =  NA, size = 0.5)+
  coord_cartesian(ylim = c(0,4.2))+
  scale_color_manual(values = c("black", "red"))+
  stat_summary(fun.y = mean, geom="point",shape = 8, size=0.5)+
  theme(axis.text.x = element_text(angle = 90, hjust = 1, vjust = 0.5))


p2 <- ggplot(d3, aes(cat3,Cells_ICS_predicted1, col = Condition))+
  geom_boxplot(outlier.shape =  NA, size = 0.5)+
  coord_cartesian(ylim = c(0,4.2))+
  scale_color_manual(values = c("black", "red"))+
  stat_summary(fun.y = mean, geom="point",shape = 8, size=0.5)+
  theme(axis.text.x = element_text(angle = 90, hjust = 1, vjust = 0.5))



d4 <- data.frame(Var = c(substr(levels(d3$cat),1,2), substr(levels(d3$cat),9,9)),
                 yval = rep(1:2, each = (length(levels(d3$cat)))),
                 xval = rep(1:length(levels(d3$cat)),2))

d5 <- data.frame(Var = c(substr(levels(d3$cat3),1,2), substr(levels(d3$cat3),9,9)),
                 yval = rep(1:2, each = (length(levels(d3$cat3)))),
                 xval = rep(1:length(levels(d3$cat3)),2))


p3 <- ggplot(d4, aes(x =xval,y=yval, fill = Var))+
  geom_tile(height = 0.9, width =0.9)+
  scale_fill_manual(values = c("black", brewer.pal(3,"Dark2"), "red"))+
  coord_fixed()

p4 <- ggplot(d5, aes(x =xval,y=yval, fill = Var))+
  geom_tile(height = 0.9, width =0.9)+
  scale_fill_manual(values = c("black", brewer.pal(3,"Dark2"), "red"))+
  coord_fixed()


plot_grid(p1,p2, ncol = 1)
ggsave(file_name1, width = 20, height = 20, units = "cm", dpi = 600)

plot_grid(p3,p4, ncol = 1)
ggsave(file_name2, width = 20,  units = "cm", dpi = 600)


ggplot(d3, aes(Condition, Cells_ICS, col = Condition))+
  geom_boxplot(notch = TRUE, size = 2)

ggplot(d3, aes(Condition, Cells_ICS_predicted1, col = Condition))+
  geom_boxplot(notch = TRUE, size = 2)


# geom_boxplot(, size = 1, alpha = 0, notch = TRUE)



d4 <- d3 %>% group_by(Lab, Person, Experiment, Technical_replicate, Condition) %>% summarise(mean_val = median(Cells_ICS),
                                                                                             mean_val_c = median(Cells_ICS_predicted1))

p5 <- ggplot(d4, aes(Condition, mean_val))+
  geom_boxplot(aes(col=Condition), notch = TRUE, size = 1.5)+
  geom_boxplot(aes(col=Lab), size = 1, alpha = 0, notch = TRUE)+
  scale_color_manual(values = c("black", brewer.pal(3,"Dark2"), "red"))


p6 <- ggplot(d4, aes(Condition, mean_val_c))+
  geom_boxplot(aes(col=Condition), notch = TRUE, size = 1.5)+
  geom_boxplot(aes(col=Lab), size = 1, alpha = 0, notch = TRUE)+
  scale_color_manual(values = c("black", brewer.pal(3,"Dark2"), "red"))

plot_grid(p5,p6, ncol = 1)
ggsave(file_name3, width = 12,  units = "cm", dpi = 600)



## Fig 3b2 code
library(ggplot2)
library(reshape2)
library(cowplot)
library(dplyr)
library(lattice)
library(latticeExtra)
library(lme4)

file_name1 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 3/Fig 3b_1.pdf'
file_name2 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 3/Fig 3b_2.png'
file_name3 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 3/Fig 3b_3.pdf'
file_name4 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 3/Fig 3b_4.png'



#Perform PCA
d2 <- d1[,c("Cells_Area",             
            "Cells_Compactness",
            "Cells_Eccentricity",     
            "Cells_MajorAxisLength",
            "Cells_MaxFeretDiameter", 
            "Cells_MaximumRadius",
            "Cells_MeanRadius",   
            "Cells_MinFeretDiameter",
            "Cells_MinorAxisLength",
            "Cells_Perimeter",   
            "Cells_Solidity",         
            "Cells_Protrusions",
            "Cells_Retractions",      
            "Cells_ShortLivedRegions", 
            "Cells_ICS",              
            "Nuc_Area",
            "Nuc_INS",
            "Nuc_Perimeter",
            "Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint", "Folder", "image_number_old")]
# "Cells_Center_X", "Cells_Center_Y")]#check if this helps convergence problems



d2 <- na.omit(d2)
set.seed(1234)
n_max_obs <- length(d2$Cells_Area)
d2 <- d2[sample(1:length(d2$Cells_Area), n_max_obs),]

# d3 <- data.frame(scale(d2[,which(substr(colnames(d2),1,5) == "Cells")], center = TRUE, scale = TRUE))
nmax <- 18
d3 <- data.frame(scale(d2[,1:nmax], center = TRUE, scale = TRUE))

d3.pca <- princomp(d3)


scores <- data.frame(d3.pca$scores)
scores <- data.frame(scores, d2[,c("Lab","Person", "Experiment","Technical_replicate","Condition")])

scores_m <- melt(scores, id.vars = c("Technical_replicate", "Experiment", "Condition", "Lab", "Person") )
scores_m <- scores_m %>% group_by(Lab, Person, Experiment, Technical_replicate, Condition, variable) %>% summarise(mean_val = mean(value))
scores_m <- dcast(scores_m, Lab+Person+Experiment+Technical_replicate  +Condition~variable, value.var = "mean_val")


temp <- dist(scores_m[,c("Comp.1","Comp.2")], diag = TRUE, upper = TRUE)


# prepare and plot 

temp <- as.matrix(temp)

dd.row <- as.dendrogram(hclust(as.dist(1-cor((temp), method = "spearman")), method = "complete")) #compute hierarchical cluster for rows
row.ord <- order.dendrogram(dd.row)

dd.col <- as.dendrogram(hclust(as.dist(1-cor(t(temp), method = "spearman")), method = "complete")) #compute hierarchical cluster for rows
col.ord <- order.dendrogram(dd.col)


# temp <- as.matrix(temp)

colnames(temp) <- paste0(scores_m$Lab,scores_m$Person, scores_m$Experiment, scores_m$Technical_replicate,scores_m$Condition)
rownames(temp) <- paste0(scores_m$Lab,scores_m$Person, scores_m$Experiment, scores_m$Technical_replicate,scores_m$Condition)


fill_col_right <- scores_m$Condition
levels(fill_col_right) <- c("black", "red")
fill_col_right <- as.character(fill_col_right)

scale_color_brewer(palette = "Dark2")

fill_col_top <- scores_m$Lab
levels(fill_col_top) <- brewer.pal(3,"Dark2")
fill_col_top <- as.character(fill_col_top)

pdf(file_name1)

print(levelplot((temp[row.ord, col.ord]),
                aspect = "fill",
                scales = list(x = list(rot = 90)),
                colorkey = list(space = "left"),
                legend =
                  list(right =
                         list(fun = dendrogramGrob,
                              args =
                                list(x = dd.col, ord = col.ord,
                                     side = "right",
                                     size = 2, 
                                     size.add = 0.5,
                                     add = list(rect=list(col = "transparent",
                                                          fill = fill_col_right)))),
                       top =
                         list(fun = dendrogramGrob,
                              args =
                                list(x = dd.row,
                                     side = "top",
                                     size = 2,
                                     size.add=0.5,
                                     add = list(rect = list(col = "transparent",
                                                            fill = fill_col_top)))))))

dev.off()


temp <- scores %>% group_by(Lab, Person, Experiment, Technical_replicate, Condition) %>% summarise(mean_val_Comp1=mean(Comp.1), mean_val_Comp2 = mean(Comp.2))


ggplot(temp, aes(mean_val_Comp1, mean_val_Comp2))+
  geom_point(aes(fill=Lab, colour = Condition),size=6, shape = 21, stroke=2)+
  scale_shape_manual(values = c(21))+
  scale_color_manual(values = c("black","red"))+
  # scale_fill_manual(values = c("green","blue"))+
  scale_fill_brewer(palette = "Dark2")+
  theme(legend.position = "bottom",
        legend.key.size = unit(1.2,"cm"))+
  coord_fixed(ratio = 1, ylim = c(-3,2), xlim = c(-3,4))

ggsave(file_name2, width = 16,  units = "cm", dpi = 600)


# normalize ---------------------------------------------------------------

scores_s <- data.frame(d3.pca$scores)
scores_s <- data.frame(scores_s, d2[,c("Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id")])

scores_s$Cell_id2 <- paste0(scores_s$Lab,scores_s$Person,scores_s$Experiment,scores_s$Technical_replicate,scores_s$Condition,scores_s$Cell_id)


# 1st PC
# set.seed(321)
# scores_s <- scores_s[sample(1:length(scores_s$Comp.1),15000,FALSE),]
d2.lme <- lmer(Comp.1 ~Condition +(1|Lab/Person/Experiment/Technical_replicate / Cell_id2), scores_s, REML = TRUE)


val1 <- data.frame(ranef(d2.lme)[2], Technical_replicate = rownames(data.frame(ranef(d2.lme)[2])), row.names=NULL)
val2 <- data.frame(ranef(d2.lme)[3], Experiment = rownames(data.frame(ranef(d2.lme)[3])))
val3 <- data.frame(ranef(d2.lme)[4], Person = rownames(data.frame(ranef(d2.lme)[4])))
val4 <- data.frame(ranef(d2.lme)[5], Lab = rownames(data.frame(ranef(d2.lme)[5])))

val1$Experiment <- substr(val1$Technical_replicate,4,unique(nchar(as.character(val1$Technical_replicate))))
val1$Person <- substr(val1$Experiment, 4, unique(nchar(as.character(val1$Experiment))))
# val1$Technical_replicate <- substr(val1$Technical_replicate,4,3)
val1$Lab <- substr(val1$Person, 4, unique(nchar(as.character(val1$Person))))

# val2$Experiment <- substr(val2$experiment,1,7)
# val3$Person <- substr(val3$person,1,3)


val1$X.Intercept.experiment <- val2$X.Intercept.[match(val1$Experiment, val2$Experiment)]
val1$X.Intercept.person <- val3$X.Intercept.[match(val1$Person, val3$Person)]
val1$X.Intercept.lab <- val4$X.Intercept.[match(val1$Lab, val4$Lab)]



val1$sum_Intercepts <- val1$X.Intercept. + val1$X.Intercept.experiment + val1$X.Intercept.person+val1$X.Intercept.lab

scores_s$fixef_c1 <- 0
scores_s$fixef_c1[which(substr(scores_s$Technical_replicate, 1, 1) == "T")] <- as.numeric(fixef(d2.lme)[2])


scores_s$cat2 <- paste(scores_s$Technical_replicate, scores_s$Experiment, scores_s$Person,  scores_s$Lab, sep = ":")
# scores_s$cat2[which(scores_s$cat2 == "C1:E1:P1:L1")] <- "T1:E1:P1:L1" # this was a test
scores_s$Comp.1_predicted1 <- scores_s$Comp.1-val1$sum_Intercepts[match(scores_s$cat2, val1$Technical_replicate)]+scores_s$fixef_c1


rm(d2.lme)

# 2nd PC
d2.lme <- lmer(Comp.2 ~Condition +(1|Lab/Person/Experiment/Technical_replicate / Cell_id2), scores_s, REML = TRUE)
# d2.lme <- lmer(Comp.2 ~Condition +(1|Lab/Person/Experiment/Technical_replicate / Cell_id2), scores_s, REML = FALSE) #changed on 2020_11_22 as models did not converge


val1 <- data.frame(ranef(d2.lme)[2], Technical_replicate = rownames(data.frame(ranef(d2.lme)[2])), row.names=NULL)
val2 <- data.frame(ranef(d2.lme)[3], Experiment = rownames(data.frame(ranef(d2.lme)[3])))
val3 <- data.frame(ranef(d2.lme)[4], Person = rownames(data.frame(ranef(d2.lme)[4])))
val4 <- data.frame(ranef(d2.lme)[5], Lab = rownames(data.frame(ranef(d2.lme)[5])))

val1$Experiment <- substr(val1$Technical_replicate,4,unique(nchar(as.character(val1$Technical_replicate))))
val1$Person <- substr(val1$Experiment, 4, unique(nchar(as.character(val1$Experiment))))
# val1$Technical_replicate <- substr(val1$Technical_replicate,4,3)
val1$Lab <- substr(val1$Person, 4, unique(nchar(as.character(val1$Person))))

# val2$Experiment <- substr(val2$experiment,1,7)
# val3$Person <- substr(val3$person,1,3)


val1$X.Intercept.experiment <- val2$X.Intercept.[match(val1$Experiment, val2$Experiment)]
val1$X.Intercept.person <- val3$X.Intercept.[match(val1$Person, val3$Person)]
val1$X.Intercept.lab <- val4$X.Intercept.[match(val1$Lab, val4$Lab)]



val1$sum_Intercepts <- val1$X.Intercept. + val1$X.Intercept.experiment + val1$X.Intercept.person+val1$X.Intercept.lab

scores_s$fixef_c2 <- 0
scores_s$fixef_c2[which(substr(scores_s$Technical_replicate, 1, 1) == "T")] <- as.numeric(fixef(d2.lme)[2])

scores_s$cat2 <- paste(scores_s$Technical_replicate, scores_s$Experiment, scores_s$Person,  scores_s$Lab, sep = ":")
# scores_s$cat2[which(scores_s$cat2 == "C1:E1:P1:L1")] <- "T1:E1:P1:L1" # this was a test
scores_s$Comp.2_predicted1 <- scores_s$Comp.2-val1$sum_Intercepts[match(scores_s$cat2, val1$Technical_replicate)]+scores_s$fixef_c2





# prepare for plot
scores_m <- scores_s[,c("Lab", "Person", "Experiment", "Condition", "Technical_replicate", "Comp.1_predicted1", "Comp.2_predicted1")]
scores_m <- melt(scores_m, id.vars = c("Technical_replicate", "Experiment", "Condition", "Lab", "Person") ) ##add cell id here??????
scores_m <- scores_m %>% group_by(Lab, Person, Experiment, Technical_replicate, Condition, variable) %>% summarise(mean_val = mean(value))
scores_m <- dcast(scores_m, Lab+Person+Experiment+Technical_replicate  +Condition~variable, value.var = "mean_val")


# prepare and plot --------------------------------------------------------

temp <- dist(scores_m[,c("Comp.1_predicted1","Comp.2_predicted1")], diag = TRUE, upper = TRUE)

temp <- as.matrix(temp)

dd.row <- as.dendrogram(hclust(as.dist(1-cor((temp), method = "spearman")), method = "complete")) #compute hierarchical cluster for rows
row.ord <- order.dendrogram(dd.row)

dd.col <- as.dendrogram(hclust(as.dist(1-cor(t(temp), method = "spearman")), method = "complete")) #compute hierarchical cluster for rows
col.ord <- order.dendrogram(dd.col)


# temp <- as.matrix(temp)

colnames(temp) <- paste0(scores_m$Lab,scores_m$Person, scores_m$Experiment, scores_m$Technical_replicate,scores_m$Condition)
rownames(temp) <- paste0(scores_m$Lab,scores_m$Person, scores_m$Experiment, scores_m$Technical_replicate,scores_m$Condition)


fill_col_right <- scores_m$Condition
levels(fill_col_right) <- c("black", "red")
fill_col_right <- as.character(fill_col_right)

fill_col_top <- scores_m$Lab
levels(fill_col_top) <- brewer.pal(3,"Dark2")
fill_col_top <- as.character(fill_col_top)

pdf(file_name3)

print(levelplot((temp[row.ord, col.ord]),
                aspect = "fill",
                scales = list(x = list(rot = 90)),
                colorkey = list(space = "left"),
                legend =
                  list(right =
                         list(fun = dendrogramGrob,
                              args =
                                list(x = dd.col, ord = col.ord,
                                     side = "right",
                                     size = 2, 
                                     size.add = 0.5,
                                     add = list(rect=list(col = "transparent",
                                                          fill = fill_col_right)))),
                       top =
                         list(fun = dendrogramGrob,
                              args =
                                list(x = dd.row,
                                     side = "top",
                                     size = 2,
                                     size.add=0.5,
                                     add = list(rect = list(col = "transparent",
                                                            fill = fill_col_top)))))))

dev.off()

temp <- scores_m %>% group_by(Lab, Person, Experiment, Technical_replicate, Condition) %>% summarise(mean_val_Comp1=mean(Comp.1_predicted1), mean_val_Comp2 = mean(Comp.2_predicted1))
# temp <- scores_m %>% group_by(Lab, Person, Experiment, Condition) %>% summarise(mean_val_Comp1=mean(Comp.1_predicted1), mean_val_Comp2 = mean(Comp.2_predicted1))


ggplot(temp, aes(mean_val_Comp1, mean_val_Comp2))+
  geom_point(aes(fill=Lab, colour = Condition),size=6, shape = 21, stroke=2)+
  scale_shape_manual(values = c(21))+
  scale_color_manual(values = c("black","red"))+
  # scale_fill_manual(values = c("green","blue"))+
  scale_fill_brewer(palette = "Dark2")+
  theme(legend.position = "bottom",
        legend.key.size = unit(1.2,"cm"))+
  coord_fixed(ratio = 1, ylim = c(-3,2), xlim = c(-3,4))

ggsave(file_name4, width = 16,  units = "cm", dpi = 600)


scores_s_save_JJ <- scores_s[,c("Lab", "Person", "Experiment", "Technical_replicate", "Condition", "Cell_id2", "Comp.1", "Comp.2", "Comp.1_predicted1", "Comp.2_predicted1")]
write.csv(scores_s_save_JJ, file = "L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/PCA_data_JJ.csv",  row.names=FALSE, quote = FALSE)


## Fig 3b2 sup code
library(ggplot2)
library(reshape2)
library(cowplot)
library(dplyr)
library(lattice)
library(latticeExtra)
library(lme4)

file_name1 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 3/Fig 3b_1_sup.pdf'
file_name2 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 3/Fig 3b_2_sup.png'
file_name3 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 3/Fig 3b_3_sup.pdf'
file_name4 = 'L:/DataX/MULTIMOT/Paper/src20220308/src - Copy/R/v2/Figures paper with datv2/Fig 3/Fig 3b_4_sup.png'



#Perform PCA
d2 <- d1[,c("Cells_Area",             
            "Cells_Compactness",
            "Cells_Eccentricity",     
            "Cells_MajorAxisLength",
            "Cells_MaxFeretDiameter", 
            "Cells_MaximumRadius",
            "Cells_MeanRadius",   
            "Cells_MinFeretDiameter",
            "Cells_MinorAxisLength",
            "Cells_Perimeter",   
            "Cells_Solidity",         
            "Cells_Protrusions",
            "Cells_Retractions",      
            "Cells_ShortLivedRegions", 
            "Cells_ICS",              
            "Nuc_Area",
            "Nuc_INS",
            "Nuc_Perimeter",
            "Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id", "Timepoint", "Folder", "image_number_old")]
# "Cells_Center_X", "Cells_Center_Y")]#check if this helps convergence problems



d2 <- na.omit(d2)
set.seed(1234)
n_max_obs <- length(d2$Cells_Area)
d2 <- d2[sample(1:length(d2$Cells_Area), n_max_obs),]

# d3 <- data.frame(scale(d2[,which(substr(colnames(d2),1,5) == "Cells")], center = TRUE, scale = TRUE))
nmax <- 18
d3 <- data.frame(scale(d2[,1:nmax], center = TRUE, scale = TRUE))

d3.pca <- princomp(d3)


scores <- data.frame(d3.pca$scores)
scores <- data.frame(scores, d2[,c("Lab","Person", "Experiment","Condition")])

scores_m <- melt(scores, id.vars = c("Experiment", "Condition", "Lab", "Person") )
scores_m <- scores_m %>% group_by(Lab, Person, Experiment, Condition, variable) %>% summarise(mean_val = mean(value))
scores_m <- dcast(scores_m, Lab+Person+Experiment +Condition~variable, value.var = "mean_val")


temp <- dist(scores_m[,c("Comp.1","Comp.2")], diag = TRUE, upper = TRUE)


# prepare and plot 

temp <- as.matrix(temp)

dd.row <- as.dendrogram(hclust(as.dist(1-cor((temp), method = "spearman")), method = "complete")) #compute hierarchical cluster for rows
row.ord <- order.dendrogram(dd.row)

dd.col <- as.dendrogram(hclust(as.dist(1-cor(t(temp), method = "spearman")), method = "complete")) #compute hierarchical cluster for rows
col.ord <- order.dendrogram(dd.col)


# temp <- as.matrix(temp)

colnames(temp) <- paste0(scores_m$Lab,scores_m$Person, scores_m$Experiment, scores_m$Technical_replicate,scores_m$Condition)
rownames(temp) <- paste0(scores_m$Lab,scores_m$Person, scores_m$Experiment, scores_m$Technical_replicate,scores_m$Condition)


fill_col_right <- scores_m$Condition
levels(fill_col_right) <- c("black", "red")
fill_col_right <- as.character(fill_col_right)

scale_color_brewer(palette = "Dark2")

fill_col_top <- scores_m$Lab
levels(fill_col_top) <- brewer.pal(3,"Dark2")
fill_col_top <- as.character(fill_col_top)

pdf(file_name1)

print(levelplot((temp[row.ord, col.ord]),
                aspect = "fill",
                scales = list(x = list(rot = 90)),
                colorkey = list(space = "left"),
                legend =
                  list(right =
                         list(fun = dendrogramGrob,
                              args =
                                list(x = dd.col, ord = col.ord,
                                     side = "right",
                                     size = 2, 
                                     size.add = 0.5,
                                     add = list(rect=list(col = "transparent",
                                                          fill = fill_col_right)))),
                       top =
                         list(fun = dendrogramGrob,
                              args =
                                list(x = dd.row,
                                     side = "top",
                                     size = 2,
                                     size.add=0.5,
                                     add = list(rect = list(col = "transparent",
                                                            fill = fill_col_top)))))))

dev.off()


temp <- scores %>% group_by(Lab, Person, Experiment, Condition) %>% summarise(mean_val_Comp1=mean(Comp.1), mean_val_Comp2 = mean(Comp.2))


ggplot(temp, aes(mean_val_Comp1, mean_val_Comp2))+
  geom_point(aes(fill=Lab, colour = Condition),size=6, shape = 21, stroke=2)+
  scale_shape_manual(values = c(21))+
  scale_color_manual(values = c("black","red"))+
  # scale_fill_manual(values = c("green","blue"))+
  scale_fill_brewer(palette = "Dark2")+
  theme(legend.position = "bottom",
        legend.key.size = unit(1.2,"cm"))+
  coord_fixed(ratio = 1, ylim = c(-3,2), xlim = c(-3,4))

ggsave(file_name2, width = 16,  units = "cm", dpi = 600)


# normalize ---------------------------------------------------------------

scores_s <- data.frame(d3.pca$scores)
scores_s <- data.frame(scores_s, d2[,c("Lab","Person", "Experiment","Technical_replicate","Condition", "Cell_id")])

scores_s$Cell_id2 <- paste0(scores_s$Lab,scores_s$Person,scores_s$Experiment,scores_s$Technical_replicate,scores_s$Condition,scores_s$Cell_id)


# 1st PC
# set.seed(321)
# scores_s <- scores_s[sample(1:length(scores_s$Comp.1),15000,FALSE),]
d1.lme <- lmer(Comp.1 ~Condition +(1|Lab/Person/Experiment/Technical_replicate / Cell_id2), scores_s, REML = TRUE)


val1 <- data.frame(ranef(d1.lme)[2], Technical_replicate = rownames(data.frame(ranef(d1.lme)[2])), row.names=NULL)
val2 <- data.frame(ranef(d1.lme)[3], Experiment = rownames(data.frame(ranef(d1.lme)[3])))
val3 <- data.frame(ranef(d1.lme)[4], Person = rownames(data.frame(ranef(d1.lme)[4])))
val4 <- data.frame(ranef(d1.lme)[5], Lab = rownames(data.frame(ranef(d1.lme)[5])))

val1$Experiment <- substr(val1$Technical_replicate,4,unique(nchar(as.character(val1$Technical_replicate))))
val1$Person <- substr(val1$Experiment, 4, unique(nchar(as.character(val1$Experiment))))
# val1$Technical_replicate <- substr(val1$Technical_replicate,4,3)
val1$Lab <- substr(val1$Person, 4, unique(nchar(as.character(val1$Person))))

# val2$Experiment <- substr(val2$experiment,1,7)
# val3$Person <- substr(val3$person,1,3)


val1$X.Intercept.experiment <- val2$X.Intercept.[match(val1$Experiment, val2$Experiment)]
val1$X.Intercept.person <- val3$X.Intercept.[match(val1$Person, val3$Person)]
val1$X.Intercept.lab <- val4$X.Intercept.[match(val1$Lab, val4$Lab)]



val1$sum_Intercepts <- val1$X.Intercept. + val1$X.Intercept.experiment + val1$X.Intercept.person+val1$X.Intercept.lab

scores_s$fixef_c1 <- 0
scores_s$fixef_c1[which(substr(scores_s$Technical_replicate, 1, 1) == "T")] <- as.numeric(fixef(d1.lme)[2])


scores_s$cat2 <- paste(scores_s$Technical_replicate, scores_s$Experiment, scores_s$Person,  scores_s$Lab, sep = ":")
# scores_s$cat2[which(scores_s$cat2 == "C1:E1:P1:L1")] <- "T1:E1:P1:L1" # this was a test
scores_s$Comp.1_predicted1 <- scores_s$Comp.1-val1$sum_Intercepts[match(scores_s$cat2, val1$Technical_replicate)]+scores_s$fixef_c1


rm(d1.lme)

# 2nd PC
d1.lme <- lmer(Comp.2 ~Condition +(1|Lab/Person/Experiment/Technical_replicate / Cell_id2), scores_s, REML = TRUE)
# d1.lme <- lmer(Comp.2 ~Condition +(1|Lab/Person/Experiment/Technical_replicate / Cell_id2), scores_s, REML = FALSE) #changed on 2020_11_22 as models did not converge


val1 <- data.frame(ranef(d1.lme)[2], Technical_replicate = rownames(data.frame(ranef(d1.lme)[2])), row.names=NULL)
val2 <- data.frame(ranef(d1.lme)[3], Experiment = rownames(data.frame(ranef(d1.lme)[3])))
val3 <- data.frame(ranef(d1.lme)[4], Person = rownames(data.frame(ranef(d1.lme)[4])))
val4 <- data.frame(ranef(d1.lme)[5], Lab = rownames(data.frame(ranef(d1.lme)[5])))

val1$Experiment <- substr(val1$Technical_replicate,4,unique(nchar(as.character(val1$Technical_replicate))))
val1$Person <- substr(val1$Experiment, 4, unique(nchar(as.character(val1$Experiment))))
# val1$Technical_replicate <- substr(val1$Technical_replicate,4,3)
val1$Lab <- substr(val1$Person, 4, unique(nchar(as.character(val1$Person))))

# val2$Experiment <- substr(val2$experiment,1,7)
# val3$Person <- substr(val3$person,1,3)


val1$X.Intercept.experiment <- val2$X.Intercept.[match(val1$Experiment, val2$Experiment)]
val1$X.Intercept.person <- val3$X.Intercept.[match(val1$Person, val3$Person)]
val1$X.Intercept.lab <- val4$X.Intercept.[match(val1$Lab, val4$Lab)]



val1$sum_Intercepts <- val1$X.Intercept. + val1$X.Intercept.experiment + val1$X.Intercept.person+val1$X.Intercept.lab

scores_s$fixef_c2 <- 0
scores_s$fixef_c2[which(substr(scores_s$Technical_replicate, 1, 1) == "T")] <- as.numeric(fixef(d1.lme)[2])

scores_s$cat2 <- paste(scores_s$Technical_replicate, scores_s$Experiment, scores_s$Person,  scores_s$Lab, sep = ":")
# scores_s$cat2[which(scores_s$cat2 == "C1:E1:P1:L1")] <- "T1:E1:P1:L1" # this was a test
scores_s$Comp.2_predicted1 <- scores_s$Comp.2-val1$sum_Intercepts[match(scores_s$cat2, val1$Technical_replicate)]+scores_s$fixef_c2





# # prepare for plot
# scores_m <- scores_s[,c("Lab", "Person", "Experiment", "Condition", "Technical_replicate", "Comp.1_predicted1", "Comp.2_predicted1")]
# scores_m <- melt(scores_m, id.vars = c("Technical_replicate", "Experiment", "Condition", "Lab", "Person") ) ##add cell id here??????
# scores_m <- scores_m %>% group_by(Lab, Person, Experiment, Technical_replicate, Condition, variable) %>% summarise(mean_val = mean(value))
# scores_m <- dcast(scores_m, Lab+Person+Experiment+Technical_replicate  +Condition~variable, value.var = "mean_val")

# prepare for plot
scores_m <- scores_s[,c("Lab", "Person", "Experiment", "Condition", "Comp.1_predicted1", "Comp.2_predicted1")]
scores_m <- melt(scores_m, id.vars = c("Experiment", "Condition", "Lab", "Person") ) ##add cell id here??????
scores_m <- scores_m %>% group_by(Lab, Person, Experiment, Condition, variable) %>% summarise(mean_val = mean(value))
scores_m <- dcast(scores_m, Lab+Person+Experiment+Condition~variable, value.var = "mean_val")

# prepare and plot --------------------------------------------------------

temp <- dist(scores_m[,c("Comp.1_predicted1","Comp.2_predicted1")], diag = TRUE, upper = TRUE)

temp <- as.matrix(temp)

dd.row <- as.dendrogram(hclust(as.dist(1-cor((temp), method = "spearman")), method = "complete")) #compute hierarchical cluster for rows
row.ord <- order.dendrogram(dd.row)

dd.col <- as.dendrogram(hclust(as.dist(1-cor(t(temp), method = "spearman")), method = "complete")) #compute hierarchical cluster for rows
col.ord <- order.dendrogram(dd.col)


# temp <- as.matrix(temp)

colnames(temp) <- paste0(scores_m$Lab,scores_m$Person, scores_m$Experiment, scores_m$Technical_replicate,scores_m$Condition)
rownames(temp) <- paste0(scores_m$Lab,scores_m$Person, scores_m$Experiment, scores_m$Technical_replicate,scores_m$Condition)


fill_col_right <- scores_m$Condition
levels(fill_col_right) <- c("black", "red")
fill_col_right <- as.character(fill_col_right)

fill_col_top <- scores_m$Lab
levels(fill_col_top) <- brewer.pal(3,"Dark2")
fill_col_top <- as.character(fill_col_top)

pdf(file_name3)

print(levelplot((temp[row.ord, col.ord]),
                aspect = "fill",
                scales = list(x = list(rot = 90)),
                colorkey = list(space = "left"),
                legend =
                  list(right =
                         list(fun = dendrogramGrob,
                              args =
                                list(x = dd.col, ord = col.ord,
                                     side = "right",
                                     size = 2, 
                                     size.add = 0.5,
                                     add = list(rect=list(col = "transparent",
                                                          fill = fill_col_right)))),
                       top =
                         list(fun = dendrogramGrob,
                              args =
                                list(x = dd.row,
                                     side = "top",
                                     size = 2,
                                     size.add=0.5,
                                     add = list(rect = list(col = "transparent",
                                                            fill = fill_col_top)))))))

dev.off()

# temp <- scores_m %>% group_by(Lab, Person, Experiment, Technical_replicate, Condition) %>% summarise(mean_val_Comp1=mean(Comp.1_predicted1), mean_val_Comp2 = mean(Comp.2_predicted1))
temp <- scores_m %>% group_by(Lab, Person, Experiment, Condition) %>% summarise(mean_val_Comp1=mean(Comp.1_predicted1), mean_val_Comp2 = mean(Comp.2_predicted1))


ggplot(temp, aes(mean_val_Comp1, mean_val_Comp2))+
  geom_point(aes(fill=Lab, colour = Condition),size=6, shape = 21, stroke=2)+
  scale_shape_manual(values = c(21))+
  scale_color_manual(values = c("black","red"))+
  # scale_fill_manual(values = c("green","blue"))+
  scale_fill_brewer(palette = "Dark2")+
  theme(legend.position = "bottom",
        legend.key.size = unit(1.2,"cm"))+
  coord_fixed(ratio = 1, ylim = c(-3,2), xlim = c(-3,4))

ggsave(file_name4, width = 16,  units = "cm", dpi = 600)


