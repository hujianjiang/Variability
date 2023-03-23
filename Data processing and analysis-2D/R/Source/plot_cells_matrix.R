plot_cells_matrix <- function(dat, filename) {
  
  temp <- table(dat$ImageNumber, dat$TrackObjects_Label)
  temp[which(temp==0)] <- NA
  
  temp2 <- melt(temp)
  temp2$Var1 <- factor(temp2$Var1)
  lev <- rev(unique(temp2$Var2))
  temp2$Var2 <- factor(temp2$Var2, levels = lev)
  
  
  ggplot(temp2, aes(Var1,Var2, fill=cut(value,c(0,1,2,3,4,5,NA)))) +     
    geom_tile(colour="white")+
    scale_fill_brewer(type="seq",palette = "Set2")+
    theme_classic()+
    theme(legend.position="none")
  
  ggsave(paste("Plots/", filename, sep=""), width = 12, height = 12)
  
  
}