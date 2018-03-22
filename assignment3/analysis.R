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
