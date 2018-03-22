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

# Add binary features ####
cleaned_ds$has_defect <- as.integer(cleaned_ds$defects > 0)

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
plot(model)
confint(model)
coef(model)
summary(model)

cleaned_ds <- cleaned_ds[cleaned_ds$ownership < 1, ]
model <- lm(cleaned_ds$defects ~ cleaned_ds$ownership )
plot(model)
summary(model)
pairs(cleaned_ds)


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
