setwd("/Users/danmontesi/Desktop/tud18-ds4se/assignment3")
ds <- read.csv("../assignment2/results.tsv",header = TRUE, sep = "\t", quote = "",na.strings = "null")
control_vars <- list("churn", "size")
independent_vars <- list("minor", "major", "total", "ownership")
dependent_var <- "defects"

#Filter file extension ####
filenames <- as.vector(ds$filename)
#install.packages("stringr")

library(stringr)
extension <- str_match(filenames, ".*(\\.\\w*)$")
table(extension[,2])
good_extensions <- list(".c", ".cpp", ".h", ".rs", ".json", ".toml", ".mk")
cleaned_filenames <- filenames[extension[,2] %in% good_extensions]
cleaned_ds <- ds[ds$filename%in%cleaned_filenames, ]

# Filter size ####
head(cleaned_ds[order(cleaned_ds$size, decreasing = TRUE), ], n = 100)
cleaned_ds <- cleaned_ds[cleaned_ds$size < 4000, ]
 
# Filter defect count ####
head(cleaned_ds[order(cleaned_ds$defects, decreasing = TRUE), ], n = 100)

# Add binary features ####
cleaned_ds$has_defect <- as.integer(cleaned_ds$defects > 0)

# Plot ####
pairs(cleaned_ds)

# Linear model on control variables
model <- lm(cleaned_ds$defects ~ cleaned_ds$churn + cleaned_ds$size)

# Linear Model ####
model <- lm(cleaned_ds$defects ~ cleaned_ds$minor + cleaned_ds$major + cleaned_ds$ownership)

# Linear Model combined ####
model <- lm(cleaned_ds$defects ~ cleaned_ds$churn + cleaned_ds$size + cleaned_ds$minor + cleaned_ds$major + cleaned_ds$ownership)
plot(model)
confint(model)
coef(model)
summary(model)

cleaned_ds <- cleaned_ds[cleaned_ds$ownership < 1, ]
model <- lm(cleaned_ds$defects ~ cleaned_ds$ownership )
plot(model)
summary(model)
pairs(cleaned_ds)
