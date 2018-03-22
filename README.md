# tud18-ds4se (Data Science for Software Engineering during Athens Week March '18 at TU DELFT)

## Group members:
* Daniele Montesi (Politechnico di Milano) <daniele.montesi@mail.polimi.it>
* Maciej Kedzielski (Politechnika Warszawska) <m.a.kedzielski@stud.elka.pw.edu.pl>
* Michael Schwarz  (Technische Universität München) <m.schwarz@tum.de>
* Sebastian Ober (Technische Universität München) <sebastian.ober@tum.de>

## System under analysis
We considered the repository of the Rust programming language https://github.com/rust-lang/rust for our analysis.

The range we considered was between commits `3d7cd77e442ce34eaac8a176ae8be17669498ebc` (12/10/15) and `e8a0123241f0d397d39cd18fcc4e5e7edde22730` (12/22/16).
This corresponds to version 1.14.0 and all the changes in the year before leading up to it.

This was used to calculate the various measures of ownership (see Microsoft paper) and the control variables churn and size.

To obtain the  number of post-release bugs, we considered all commits starting from the release until `766bd11c8a3c019ca53febdcd77b2215379dd67d`(01/04/18). To get an estimate of the number of bugs, we looked for commit messages containing words such as fix, bug, etc. We then used blame to find out when the changes were introduced. If they were introduced in the time period between release and 06/22/2017 we consider them to be post-release bugs.

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

## Design decisions
* Software component == file
* Git Submodules are out of scope
* Exclude lines of code starting with '/' or '\*', considering them as comments 

# Future work
* Prepare linear regression models for acquired data
