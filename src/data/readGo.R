# Read data
# @export

test <- function() {
    f <- "Go/Scores/1000/7/SCORES1000go7annmcts.txt"
    d <- read_delim(f, delim=" ", col_names=FALSE)
    d <- t(d)
    #df = read.delim(f, fill=TRUE, sep=' ', header=FALSE)
    myplot <- matplot(row(d), d, type='l', xlab='Turn', ylab='Score', col=1:20)
    return(dplyr::tbl_df(d))
}
