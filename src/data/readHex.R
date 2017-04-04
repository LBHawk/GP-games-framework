test <- function(){
  f <- "Hex/Scores/1000/11/SCORES1000hex11annmcts.csv"
  #d <- read_delim(f, delim=" ", col_names=FALSE)
  #d <- tbl_df(t(d))
  df <- read.delim(f, fill=TRUE, sep=' ', header=FALSE)
  df[is.na(df)] <- 0
  wins <- (20+sum(df))/2
  
  fin <- read.table(text = "",
                   colClasses = c("numeric", "character", "character",
                                  "numeric", "numeric"),
                   col.names = c("wins", "agent1",  "agent2",
                                 "time", "size"))
  
  d <- tbl_df(t(c(wins, "a1", "a2", 2, 2)))
  d.columns = c("wins", "agent1", "agent2", "time", "size")
  fin <- dplyr::bind_rows(d, fin)
  return(d)
}

consolidate <- function(){
  setwd("/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/src/data")
  times <- c(500,1000,2000,4000,8000)
  sizes <- c(9,11,14)
  agents <- c( "ann", "ga", "mcts")
  
  fin <- read.table(text = "",
                    colClasses = c("character", "character", "character",
                                   "character", "character"),
                    col.names = c("V1", "V2",  "V3",
                                  "V4", "V5"))
  
  for(t in times){
    for(s in sizes){
      # Sets the filepath for each score csv
      for(i in c(1:3)){
        for(j in c(i+1:3)){
          if(!is.na(agents[j])){
            #print(paste(agents[i], "vs", agents[j], s,t))
            f <- paste("SCORES", t, "hex", s, agents[i], agents[j], ".csv", sep='')
            final <- paste("Hex/Scores/", t, "/", s, "/", f, sep='')
            print(final)
            
            if(file.exists(final)){
              # Set temp dataframe with current filepath
              df <- read.delim(final, fill=TRUE, sep=' ', header=FALSE)
              df[is.na(df)] <- 0
              wins <- (20+sum(df))/2
              
              d <- tbl_df(t(c(wins, agents[i], agents[j], t, s)))
              fin <- dplyr::bind_rows(fin, d)
              
            }
            
          }
        }
      }
    }
  }
  colnames(fin) <- c("wins","a1","a2","time","size")
  fin <- transform(fin, wins=as.numeric(wins), time=as.numeric(time),size=as.numeric(size))
  
  return(fin)
}