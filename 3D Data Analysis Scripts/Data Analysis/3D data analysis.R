rm(list=ls())


# Load libraries ----------------------------------------------------------

library(ggplot2)
library(cowplot)
library(lme4)
library(dplyr)





# Load specific functions ------------------------------------------------------

# give.n <- function(x){
#   return(c(y = max(x)*1.1, label = length(x))) 
#   # experiment with the multiplier to find the perfect position
# }



# Define File and Settings ------------------------------------------------


File <- list()
File$experiment_list_filepath <- "C:/Users/xavser/Box Sync/MULTIMOT/3D cell migration/data/v6/files.xlsx"
File$experiment_list <- openxlsx::readWorkbook(File$experiment_list_filepath,1)



# Load data ---------------------------------------------------------------


res <- list()
for (k in 1:length(File$experiment_list$File)){
  res[[k]] <- data.frame(read.table(File$experiment_list$File[k],    header = TRUE, sep= "\t"),
                         lab = File$experiment_list$Lab[k],
                         cell	= File$experiment_list$cell[k], 
                         set	= File$experiment_list$set[k],
                         ECM	= File$experiment_list$ECM[k],
                         # id_exp	= File$experiment_list$id_exp[k],
                         Media	= File$experiment_list$Drug[k],
                         t_repeat	= File$experiment_list$t_repeat[k],
                         cat= File$experiment_list$cat[k],
                         include = File$experiment_list$include[k])
  
  
  
  
}

res_df <- do.call(rbind,res)



# Postprocessing ----------------------------------------------------------

res_df$set <- factor(res_df$set)
# res_df$set2 <-res_df$set

res_df <- res_df %>% mutate(set = recode(set,"N=4" = "N=1"))
# table(res_df$xºset)
res_df$set <- factor(res_df$set, levels(res_df$set)[c(3,1,2)])
res_df <- res_df %>% filter(include == "y")

# plots -------------------------------------------------------------------

freq_d <- res_df %>% group_by(lab, cell,set, ECM, Media, t_repeat) %>% summarise(mean_val = length(cell))

ggplot(freq_d, aes(t_repeat, mean_val, col=Media))+
  geom_bar(stat = "identity", position = "dodge")+
  facet_grid(lab+ECM~cell+as.factor(set), scales = "free_x", switch = "y")+
  theme_bw()+
  theme(legend.position = "bottom")+
  xlab("")+
  ylab("number of observations")




## 3 - Focus 1: Migration distance ----------------

ggplot(res_df %>% filter(Media == "medium"), aes(factor(ECM), (Migration.distance), col=as.factor(lab), fill = factor(t_repeat)))+
  geom_boxplot(notch = TRUE, size = 1.2)+
  facet_grid(.~cell+set, scales = "free_x")+
  scale_fill_manual(values = rep("white",6))+
  ylim(c(0,350))+
  guides(fill=FALSE)+
  theme_bw()+
  theme(legend.position = "bottom")+
  scale_color_manual(values = c("#E69F00","#3FB1CD"))

# focus on HT1080
res_df2 <- res_df %>% filter(Media == "medium", cell == "HT1080")

m1 <- lmer(Migration.distance~ECM+(1|lab/set/t_repeat), data = res_df2)
# !!WARNING MODEL FAILED TO CONVERGE!!

val1 <- data.frame(ranef(m1)[1], t_repeat = rownames(data.frame(ranef(m1)[1])), row.names=NULL)
val2 <- data.frame(ranef(m1)[2], set = rownames(data.frame(ranef(m1)[2])))
val3 <- data.frame(ranef(m1)[3], lab = rownames(data.frame(ranef(m1)[3])))



val1$set <- substr(val1$t_repeat,3,unique(nchar(as.character(val1$t_repeat))))
val1$lab <- substr(val1$t_repeat, 7,unique(nchar(as.character(val1$t_repeat))))

val1$X.Intercept.experiment <- val2$X.Intercept.[match(val1$set, val2$set)]
val1$X.Intercept.lab <- val3$X.Intercept.[match(val1$lab, val3$lab)]


val1$sum_Intercepts <- val1$X.Intercept. + val1$X.Intercept.experiment +val1$X.Intercept.lab


res_df2$fixef_c1 <- 0
res_df2$fixef_c1[which(substr(res_df2$ECM, 1, 1) == 6)] <- as.numeric(fixef(m1)[2])


res_df2$cat2 <- paste(res_df2$t_repeat, res_df2$set, res_df2$lab, sep = ":")
res_df2$Migration.distance_predicted1 <-  res_df2$Migration.distance-
                                          val1$sum_Intercepts[match(res_df2$cat2, val1$t_repeat)]+
                                          res_df2$fixef_c1

 
 
res_df2$cat3 <- paste0(res_df2$lab, "ECM = ", res_df2$ECM,"set = ", res_df2$set, "t_repeat = ",res_df2$t_repeat)




d3_h <- res_df2 %>%
  group_by(lab, set, t_repeat, ECM, cat3) %>%
  summarise(mean_val = mean(Migration.distance))
# 
d3_h$cat3 <- factor(d3_h$cat3)
d3_h$cat3 <- factor(d3_h$cat3, levels = d3_h$cat3[order(d3_h$mean_val)])
# 
res_df2$cat4 <- factor(res_df2$cat3, levels = levels(d3_h$cat3))



ggplot(res_df2, aes(cat4,Migration.distance, col = factor(ECM)))+#,  col = Lab, fill=Condition))+
  geom_boxplot(outlier.shape =  NA, size = 0.5)+
  # geom_hline(yintercept=c(81.8,43), col=c("black","red"))+
  coord_cartesian(ylim = c(0,400))+
  scale_color_manual(values = c("black", "red"))+
  stat_summary(fun.y = mean, geom="point",shape = 8, size=0.5)+
  theme(axis.text.x = element_text(angle = 90, hjust = 1, vjust = 0.5))
 



d3_h <- res_df2 %>%
  group_by(lab, set, t_repeat, ECM, cat3) %>%
  summarise(mean_val = mean(Migration.distance_predicted1))
# 
d3_h$cat3 <- factor(d3_h$cat3)
d3_h$cat3 <- factor(d3_h$cat3, levels = d3_h$cat3[order(d3_h$mean_val)])
# 
res_df2$cat3 <- factor(res_df2$cat3, levels = levels(d3_h$cat3))

res_df2$cat3


ggplot(res_df2, aes(cat3,Migration.distance_predicted1, col = factor(ECM)))+#,  col = Lab, fill=Condition))+
  geom_boxplot(outlier.shape =  NA, size = 0.5)+
  # geom_hline(yintercept=c(81.8,43), col=c("black","red"))+
  coord_cartesian(ylim = c(0,400))+
  scale_color_manual(values = c("black", "red"))+
  stat_summary(fun.y = mean, geom="point",shape = 8, size=0.5)+
  theme(axis.text.x = element_text(angle = 90, hjust = 1, vjust = 0.5))


d4 <- res_df2 %>% group_by(lab, set, t_repeat, ECM) %>% summarise(mean_val = median(Migration.distance),
                                                                  mean_val_c = median(Migration.distance_predicted1))

library(RColorBrewer)                                                                                             

ggplot(d4, aes(factor(ECM), mean_val))+
  geom_boxplot(aes(col=factor(ECM)), notch = TRUE, size = 1)+
  geom_boxplot(aes(col=lab), size = 1, alpha = 0, notch = TRUE)+
  scale_color_manual(values = c("black","red", brewer.pal(3,"Dark2")[1:2]))



ggplot(d4, aes(factor(ECM), mean_val_c))+
  geom_boxplot(aes(col=factor(ECM)), notch = TRUE, size = 1)+
  geom_boxplot(aes(col=lab), size = 1, alpha = 0, notch = TRUE)+
  scale_color_manual(values = c("black","red", brewer.pal(3,"Dark2")[1:2]))


































