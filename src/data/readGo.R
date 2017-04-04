# Read data
# @export

# Currently working to plot a single scores file as a set of lines on a graph
test <- function() {
    f <- "Go/Scores/1000/7/SCORES1000go7annmcts.txt"
    #d <- read_delim(f, delim=" ", col_names=FALSE)
    #d <- tbl_df(t(d))
    df <- read.delim(f, fill=TRUE, sep=' ', header=FALSE)
    df <- tbl_df(t(df))
    t <- df
    #myplot <- matplot(row(df), df, type='l', xlab='Turn', ylab='Score', col=1:20)
    #plot2 <- ggplot(d, aes(row(d), d)) + geom_line()
    df <- dplyr::mutate(df, turn=c(1:251))
    df <- melt(df, id.vars=c("turn"))
    df <- dcast(df, turn ~ variable)
    df <- dplyr::select(df, starts_with("V"))
    myplot <- matplot(row(df), df, type='l', xlab='Turn', ylab='Score', col=1:20)
    return(t)
}

# Demonstrate creating a dataframe from a file, formatting it appropriately
# (add turn col, melt, etc.).  Then to the melted data, add appropriate gametype,
# agents, size, time tags.  Filter for the data we want, cast it back to graphable dataframe, graph.
# HOORAY IT WORKS
meltcasttest <- function(filename){
  # read file, create dataframe
  f <- filename
  df <- read.delim(f, fill=TRUE, sep=' ', header=FALSE)
  df <- tbl_df(t(df))
  
  # Add turn column and melt
  df <- dplyr::mutate(df, turn=c(1:251))
  df <- melt(df, id.vars=c("turn"))
  
  # Add columns specifying game, agents, time, size
  df <- dplyr::mutate(df, game='go', agent1='ann', agent2='mcts', time=1000, size=7)
  
  # Get whichever, game, agents, time, size we want
  x <- dplyr::filter(df, game=='go', agent1=='ann', agent2=='mcts', time==1000, size==7)
  
  # cast gathered data back to graphable dataframe
  x <- dcast(x, turn ~ variable)
  x <- dplyr::select(x, starts_with("V"))
  
  #plot
  myplot <- matplot(row(x), x, type='l', xlab='Turn', ylab='Score', col=1:20)
  return(x)
}

# Consolidates all data for go games in melted format
consolidate <- function(){
  setwd("/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/src/data")
  times <- c(500,1000,2000,4000,8000)
  sizes <- c(5,7,9,11,13)
  agents <- c("ga", "ann", "mcts", "random")
  
  # Initialize final dataframe (empty)
  df <- read.table(text = "",
                   colClasses = c("numeric", "character", "numeric", "character",
                                  "character", "character", "numeric", "numeric"),
                   col.names = c("turn", "variable", "value", "game", "agent1",
                                 "agent2", "time", "size"))
  
  for(t in times){
   for(s in sizes){
     # Sets the filepath for each score csv
     for(i in c(1:4)){
       for(j in c(i+1:4)){
         if(!is.na(agents[j])){
           #print(paste(agents[i], " vs ", agents[j]))
           f <- paste("SCORES", t, "go", s, agents[i], agents[j], ".csv", sep='')
           final <- paste("Go/Scores/", t, "/", s, "/", f, sep='')
           
           if(file.exists(final)){
             # Set temp dataframe with current filepath
             temp <- read.delim(final, fill=TRUE, sep=' ', header=FALSE)
             temp <- tbl_df(t(temp))
             # Add turn column and melt to temp
             temp <- dplyr::mutate(temp, turn=c(1:251))
             temp <- melt(temp, id.vars=c("turn"))
             
             # Add gametype, size, time, agents to temp
             temp <- dplyr::mutate(temp, game='go', agent1=agents[i], 
                                   agent2=agents[j], time=t, size=s)
             
             # Append temp dataframe to final dataframe
             df <- dplyr::bind_rows(df, temp)
           }
         }
       }
     }
   }
  }
  
  return(df)
}