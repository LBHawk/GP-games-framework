# Read data
# @export
require(zoo, ggplot2, dplyr, readr)

# Currently working to plot a single scores file as a set of lines on a graph
test <- function() {
    f <- "Go/Scores/500/5/SCORES500go5annrandom.csv"
    #d <- read_delim(f, delim=" ", col_names=FALSE)
    #d <- tbl_df(t(d))
    df <- read.delim(f, fill=TRUE, sep=' ', header=FALSE)
    df <- tbl_df(t(df))
    #df <- na.locf(df)
    t <- df
    #myplot <- matplot(row(df), df, type='l', xlab='Turn', ylab='Score', col=1:20)
    #plot2 <- ggplot(d, aes(row(d), d)) + geom_line()
    df <- dplyr::mutate(df, turn=c(1:250))
    df <- melt(df, id.vars=c("turn"))
    df <- dcast(df, turn ~ variable)
    df <- dplyr::select(df, starts_with("V"))
    #myplot <- matplot(row(df), df, type='l', xlab='Turn', ylab='Score', col=1:20)
    return(tbl_df(t))
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
# Note this dataframe has been saved to "fullmeltedgo.csv" in the data dir
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
             
             # Carry last score forward over NAs (uses Zoo package)
             temp <- na.locf(temp)
             
             # Add turn column and melt to temp
             print(paste(i,j,s,t))
             temp <- dplyr::mutate(temp, turn=c(1:250))
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

# This function visualizes the data from our final dataset as specified in the arguments
visualize <- function(a1, a2, s, t){
  # Load consolidated go scores
  #df <- read.csv("fullmeltedgo.csv")
  df <- fullset2
  
  # Get whichever, game, agents, time, size we want
  df <- dplyr::filter(df, game=='go', agent1==a1, agent2==a2, time==t, size==s)
  
  # cast gathered data back to graphable dataframe
  #df <- dcast(df, turn ~ variable)
  #df <- dplyr::select(df, starts_with("V"))
  
  #plot
  #p <- myplot <- matplot(row(df), df, type='l', xlab='Turn', ylab='Score', col=1:20)
  ptitle <- paste(toupper(a1), " vs ", toupper(a2), " ", s, "x", s, " Go ", t, "ms", sep='')
  p <- ggplot2::ggplot(df, aes(x=turn, y=value)) +
      geom_line(aes(colour=variable)) +
      geom_smooth(aes(colour=NA)) +
      scale_x_continuous(limits = c(0,250), expand = c(0,0)) +
      scale_y_continuous(limits = c(s*s*-3/4, s*s*3/4), expand = c(0, 0), sec.axis = dup_axis()) + 
      xlab("Move number") +
      ylab("Score") +
      ggtitle(ptitle) +
      theme_bw() +
      theme(plot.title = element_text(hjust = 0.5, size=12)) +
      theme(axis.title.x = element_text(size = 11)) +
      theme(axis.title.y = element_text(size = 11)) +
      theme(plot.margin=margin(10,10,10,10)) +
      theme(legend.position="none")
  #p <- qplot(turn, value, data=df, group=col, geom="line")
  
  return(p)
}

plotallscores <- function(){
  times <- c(500,1000,2000,4000,8000)
  sizes <- c(5,7,9,11,11)
  agents <- c("ga", "ann", "mcts", "random")
  # Sets the filepath for each score csv
  for(i in c(1:4)){
    for(j in c(i+1:4)){
       if(!is.na(agents[j])){
         for(t in times){
           for(s in sizes){
             dir <- paste(toupper(agents[i]), toupper(agents[j]), sep="vs")
             name <- paste(t, "ms", s, "x", s, ".png", sep='')
             print("hooray")
             fp <- paste("./Go/Visualizations", dir, name, sep="/")
             plot <- visualize(agents[i], agents[j], s, t)
             png(filename=fp)
             show(plot)
             dev.off()
             print(paste("Saved plot to",fp))
          }
        }
      }
    }
  }
}