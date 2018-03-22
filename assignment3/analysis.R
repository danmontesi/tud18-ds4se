setwd("e:\\tud18\\tud18-ds4se\\assignment3\\")
ds <- read.csv("..\\assignment2\\results.tsv",header = TRUE, sep = "\t", quote = "",na.strings = "null")
control_vars <- list("churn", "size")
independent_vars <- list("minor", "major", "total", "ownership")
dependent_var <- "defects"
filenames <- as.vector(ds$filename)

library(stringr)
test <- str_match(filenames, ".*(\\.\\w*)$")
table(test[,2])
good_extensions <- list(".c", ".cpp", ".h", ".rs", ".json", ".toml", ".mk")