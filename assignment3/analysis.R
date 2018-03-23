setwd("e:\\tud18\\tud18-ds4se\\assignment3")
ds <- read.csv("..\\assignment2\\results.tsv",header = TRUE, sep = "\t", quote = "",na.strings = "null")
control_vars <- list("churn", "size")
independent_vars <- list("minor", "major", "total", "ownership")
dependent_var <- "defects"

#Filter file extension ####
filenames <- as.vector(ds$filename)
#install.packages("stringr")

library(stringr)
extension <- str_match(filenames, ".*(\\.\\w*)$")
table(extension[,2])
good_extensions <- list(".c", ".cpp", ".h", ".rs", ".json", ".toml", ".mk", ".po")
cleaned_filenames <- filenames[extension[,2] %in% good_extensions]
cleaned_ds <- ds[ds$filename%in%cleaned_filenames, ]

# Filter size ####
#head(cleaned_ds[order(cleaned_ds$size, decreasing = TRUE), ], n = 100)
# does not improve r² to remove outliers
#cleaned_ds <- cleaned_ds[cleaned_ds$size < 4000, ]
 
# Filter defect count ####
#head(cleaned_ds[order(cleaned_ds$defects, decreasing = TRUE), ], n = 100)

#filter files with outlyingly high churn #####
#cleaned_ds = cleaned_ds[cleaned_ds$churn<15000,]
#This reduces r² by quite a bit. For cross validation, it yields better results (overall mse of 2.08 as opposed to 2.6), but the reduction in r² outweighs that

# Add binary features ####
cleaned_ds$has_defect <- as.integer(cleaned_ds$defects > 0)
#try decision tree
# Can't get them to work
# library(rpart)
# tree1 <- rpart(cleaned_ds$has_defect ~ cleaned_ds$churn + cleaned_ds$size + cleaned_ds$minor + cleaned_ds$major + cleaned_ds$total + cleaned_ds$ownership, method="class")
# printcp(tree1)
# plotcp(tree1)
# plot(tree1, compress=TRUE)
# text(tree1, use.n=TRUE)
# 
# tree2 <- rpart(cleaned_ds$defects ~ cleaned_ds$churn + cleaned_ds$size + cleaned_ds$minor + cleaned_ds$major + cleaned_ds$total + cleaned_ds$ownership, method="anova")
# printcp(tree2)
# plotcp(tree2)
# plot(tree2, compress=TRUE)
# text(tree2, use.n=TRUE)
# Plot ####

pairs(cleaned_ds)


# Linear model on control variables
modelc <- lm(cleaned_ds$defects ~ cleaned_ds$churn + cleaned_ds$size)
summary(modelc)
# Linear Model ####
modeli <- lm(cleaned_ds$defects ~ cleaned_ds$minor + cleaned_ds$major + cleaned_ds$ownership + cleaned_ds$total)
summary(modeli)
# Linear Model combined ####
model <- lm(cleaned_ds$defects ~ cleaned_ds$churn + cleaned_ds$size + cleaned_ds$minor + cleaned_ds$major + cleaned_ds$total + cleaned_ds$ownership)
#plot(model)
confint(model)
coef(model)
summary(model)
plot(cleaned_ds$defects~cleaned_ds$churn+cleaned_ds$minor)

# install.packages("plotly")
# library(plotly)
# plot_ly(x=cleaned_ds$churn,y=cleaned_ds$minor,z=cleaned_ds$defects, type="surface")

##for one of the charts, remove all files with 100% ownership
cleaned_ds1 <- cleaned_ds[cleaned_ds$ownership < 1, ]
model <- lm(cleaned_ds1$defects ~ cleaned_ds1$ownership )
plot(model)
summary(model)


# model binary flag ####
model_bin <- lm(cleaned_ds$has_defect ~ cleaned_ds$minor + cleaned_ds$major + cleaned_ds$ownership)
summary(model_bin)

# model binary combined ####
# Linear Model combined ####
model.bin.combined <- lm(cleaned_ds$has_defect ~ cleaned_ds$churn + cleaned_ds$size + cleaned_ds$minor + cleaned_ds$major + cleaned_ds$total + cleaned_ds$ownership)
summary(model.bin.combined)


# Cross validation####
#install.packages("DAAG")
library(DAAG)

cvmodel <- cv.lm(cleaned_ds, m = 5,form.lm = formula(defects ~ churn + size + minor + major + total + ownership))
summary(cvmodel)

#hists #####
hist(cleaned_ds$defects[cleaned_ds$defects>0 & cleaned_ds$defects<30],breaks = 28)
hist(cleaned_ds$size[cleaned_ds$size>0],breaks = 50)
hist(cleaned_ds$size[cleaned_ds$size>150],breaks = 50)

# total vs defects ###
totalmodel <- lm(cleaned_ds$defects ~ cleaned_ds$total)
summary(totalmodel)

#overlord churn ####
churnmodel <- lm(cleaned_ds$defects ~ cleaned_ds$churn)
plot(cleaned_ds$defects ~ cleaned_ds$churn)
abline(churnmodel,col="red")

#ownership#####
#cleaned_ds2 <- cleaned_ds[cleaned_ds$ownership<1,]
#cleaned_ds2 <- cleaned_ds[cleaned_ds$defects<60,]
ownershipmodel <- lm(cleaned_ds$defects ~ cleaned_ds$ownership)
summary(ownershipmodel)
plot(log(cleaned_ds$defects) ~ cleaned_ds$ownership)
abline(ownershipmodel, col="red")

# 3d?####
#install.packages("plot3D")
library(plot3D)
cleaned_ds3 <- cleaned_ds[cleaned_ds$defects<50,]
scatter3D(x=cleaned_ds3$churn, y=cleaned_ds3$size, z=cleaned_ds3$defects, xlab="churn", ylab="size", zlab="defects")

