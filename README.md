# tud18-ds4se (Data Science for Software Engineering during Athens Week March '18 at TU DELFT)

## Group members
* Daniele Montesi (Politechnico di Milano) <daniele.montesi@mail.polimi.it>
* Maciej Kedzielski (Politechnika Warszawska) <m.a.kedzielski@stud.elka.pw.edu.pl>
* Michael Schwarz  (Technische Universität München) <m.schwarz@tum.de>
* Sebastian Ober (Technische Universität München) <sebastian.ober@tum.de>

## Introduction
The project is part of the Data Science for Software Engineering course during Athens Student Exchange Programme, March '18 edition. The course is led by Alberto Bacchelli, Professor of Empirical Software Engineering at the University of Zurich (UZH), Switzerland. The main objective of the project was to perform a study of the influence of ownership on the number of defects in program code. It was inspired by the work "Don’t Touch My Code! Examining the Effects of Ownership on Software Quality" by Bird et al. (2011). 
The project's scope is divided into four assignments:
1. Calculate minor, major, total and ownership features (as described in "Don't Touch My Code!") for components in selected GitHub repository.
2. Calculate churn and size features (as described in "Don't Touch My Code!") for components in selected Git repository. Find number of defects in every file discovered in post-release period.
3. Perform linear regression analysis and cross-validation for obtained feautures to check if they are significantly influential on number of defects in code.
4. TBC

The final goal was to prepare a presentation of results of analysis, compare it with "Don't Touch My Code!" and form conclusions. 

## Description of work and operational decisions
We considered the repository of the Rust programming language https://github.com/rust-lang/rust for our analysis. Rust is an open-source programing language, aimed on prevention of segmentation faults and thread safety.

As a component we considered single file. It was motivated by clear segregation of project modules into files and not complicated ways of mining statisctics and parameters of a GitHub file. 

The range of dates we considered was between commits `3d7cd77e442ce34eaac8a176ae8be17669498ebc` (12/10/15) and `e8a0123241f0d397d39cd18fcc4e5e7edde22730` (12/22/16).
This corresponds to release version 1.14.0 and all the changes in the year before leading up to it.
We did not analyze submodules that were included in main project. 

Mentioned period was considered in calculations of minor, major, total and ownership parameters (as described in "Don't Touch My Code"), as well as the control variables: churn size (churn) and Lines of Code (LoC). Churn is the sum of added and deleted lines in a file during the whole considered period. LoC are measured at the moment of version 1.14.0 release.

To obtain the number of post-release bugs, we considered all commits starting from the release 1.14.0 until release `766bd11c8a3c019ca53febdcd77b2215379dd67d`(01/04/18). To get an estimate of the number of bugs, we looked for commit messages containing words related to bug fixing, such as fix, bug, correction, etc. Then we used blame function to find out when the changes were introduced. If they were introduced in the time period between release and 06/22/2017 (6 months after) we considered them to be post-release bugs. In blame function we skipped lines considered as comments, e.g. starting with '/' or '\*'. 

File renames were considered. To track them we used functionality of RepoDriller that allows checking old and new name of every file in a commit. 

To improve the performance of mining code we implemented following code changes:
* Paralellize Visitor methods - we assigned BugVisitor to a new thread, so it could work independently from other Visitors
* Merge methods that work on the same or similar data - chained SizeVisitor, ChurnVisitor and OwnerVisitor, since they use commits within the same time scope
* Exclude bugfixes that changed more than 10 lines of code
* Exclude bugfixes with churn value higher than 50
* Make list of excluded commits and add there every found commit containing defects that is not in the relevant period - comparing further results of defects tracking to the list allowed us to skip irrelevant commits immediately

In order to analyze only code containing components, we decided to choose only files with following extensions for analysis:
* .c
* .cpp
* .h
* .rs
* .json
* .tuml
* .mk

## Analysis
To perform the analysis, we employed R to generate a multilinear regression model. To validate that our findings were actually significant, we compared our model with a model using just the control variable.

The total number of files after filtering is 5595. There was a total of 2335 bugs spread across 477 files.

// Histogram

First, we consider the number of *post-release bugs* in a file as a function of the control variables *churn* and *LoC*. We find positive correlations of both features with *post-release bugs*. These correlations are highly significant (p < 0.001). 

```Coefficients:
                  Estimate Std. Error t value Pr(>|t|)    
(Intercept)      -3.66e-02   2.26e-02   -1.62      0.1    
cleaned_ds$churn  1.04e-03   2.72e-05   38.31   <2e-16 ***
cleaned_ds$size   1.60e-03   9.14e-05   17.52   <2e-16 ***
---
Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1

Residual standard error: 1.58 on 5592 degrees of freedom
Multiple R-squared:  0.494,	Adjusted R-squared:  0.494 
F-statistic: 2.73e+03 on 2 and 5592 DF,  p-value: <2e-16
```

The coefficient of determination (adjusted r^2) is 0.494.

Next, we analyze the number of *post-release bugs* in a file as a function of just the independent variables *minor*, *major*, *ownership*, and *total*. We find highly-significant positive correlations for major and minor to the number of post-release bugs and a significant correlation between ownership and post-release bugs. Total as the sum of major and minor is ignored.

```
Coefficients: (1 not defined because of singularities)
                     Estimate Std. Error t value Pr(>|t|)    
(Intercept)          -0.39452    0.12820   -3.08  0.00210 ** 
cleaned_ds$minor      0.25760    0.00448   57.51  < 2e-16 ***
cleaned_ds$major      0.06915    0.01871    3.70  0.00022 ***
cleaned_ds$ownership  0.38927    0.12610    3.09  0.00203 ** 
cleaned_ds$total           NA         NA      NA       NA    
---
Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1

Residual standard error: 1.74 on 5591 degrees of freedom
Multiple R-squared:  0.386,	Adjusted R-squared:  0.386 
F-statistic: 1.17e+03 on 3 and 5591 DF,  p-value: <2e-16
```

The adjusted r^2 is poor at 0.386.

Combining both control and independent variables does not meaningfully improve the adjusted r^2 (0.502). The only IV that is significant is minor with a p-value < 0.001.

```
Coefficients: (1 not defined because of singularities)
                      Estimate Std. Error t value Pr(>|t|)    
(Intercept)          -1.69e-01   1.16e-01   -1.47     0.14    
cleaned_ds$churn      8.78e-04   3.19e-05   27.49   <2e-16 ***
cleaned_ds$size       1.23e-03   9.84e-05   12.51   <2e-16 ***
cleaned_ds$minor      6.39e-02   6.74e-03    9.47   <2e-16 ***
cleaned_ds$major      2.34e-02   1.69e-02    1.38     0.17    
cleaned_ds$total            NA         NA      NA       NA    
cleaned_ds$ownership  9.51e-02   1.14e-01    0.84     0.40    
---
Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1

Residual standard error: 1.57 on 5589 degrees of freedom
Multiple R-squared:  0.503,	Adjusted R-squared:  0.502 
F-statistic: 1.13e+03 on 5 and 5589 DF,  p-value: <2e-16
```

Running a 5-fold cross-validation on the combined model yields a RSS of 2.6 and a mean square of 2.14. The generated model seems to be quite robust.

// picture

// what does total on its own give us?


**Our analysis did not show meaningful improvements compared to the control variables. We could therefore not show that in Rust there is a influence of the ownership parameters as compared to just the control variables**. 

We do however believe that our result might very well be caused by a suboptimal method for finding post-release bugs.
In the future, it would be meaningful to consider maybe issues from the bugtracker and link them to the code via the issue number
