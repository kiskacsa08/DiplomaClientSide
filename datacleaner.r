library(RJDBC)

TEAMIDS <- 0:22

# Convert result string to result class (1=home win, 0=draw, -1=away win)
result2class <- function(goals){
  # Convert to string
  goals <- as.character(goals)
  # Split the string
  g <- strsplit(goals, "–")
  # Convert to number
  g <- as.numeric(g[[1]])
  # Determine class
  if (g[1] > g[2]) {
    class <-1
  }
  else if (g[1] < g[2]) {
    class <- -1
  }
  else {
    class <- 0
  }
  class
}

# Negate a number
negatenumber <- function(number){
  -number
}

# Extract results per team from match data
extractteams <- function(matches){
  # Create a list
  teams <- list()
  for (t in TEAMIDS) {
    # Select the team's home matches
    df1 <- subset(matches, matches[,"HOME_TEAM_ID"] == t)
    # Convert the result to class
    results <- df1[,'RESULT']
    CLASS <- sapply(results, FUN = result2class)
    # Append the CLASS column
    df1 <- cbind(df1, CLASS)
    # Do the same with the team's away matches
    df2 <- subset(matches, matches[,"AWAY_TEAM_ID"] == t)
    results <- df2[,'RESULT']
    CLASS <- sapply(results, FUN = result2class)
    CLASS <- sapply(CLASS, FUN = negatenumber)
    df2 <- cbind(df2, CLASS)
    # Join the two dataframes
    df <- rbind(df1, df2)
#     # We don't want two matches with the same round number
#     df <- df[order(df$ROUND, df$MATCH_DATE),]
#     # If there is more than 38 (one season) matches for a team
#     if (nrow(df) > 38) {
#       for (j in 1:38) {
#         # Get the matches with the same round number
#         rows <- which(df$ROUND == j)
#         if (length(rows) > 1) {
#           for (k in 2:length(rows)) {
#             # Assign a new round number to the latest matches
#             df[rows[k],"ROUND"] <- df[rows[k],"ROUND"] + (k-1)*38
#           }
#         }
#       }
#     }
    # Order the dataframe by ROUND
    df <- df[order(df$ROUND),]
    # Add the dataframe to the list
    teams[[t+1]] <- df
  }
  return(teams)
}

#----------------------------------------------------------------------
# Load the database driver
jdbcDriver <- JDBC(driverClass = "org.apache.derby.jdbc.ClientDriver", classPath = "./derbyclient.jar")
# Connect to database
jdbcConnection <- dbConnect(jdbcDriver, "jdbc:derby://localhost:1527/DiplomaOddsDatabase", "kiskacsa08", "kiskacsa")
# Read odds data
odds <- dbReadTable(jdbcConnection, "ODDS")
# odds <- read.csv("odds.csv")
# Remove the REFRESH_DATE column
odds <- odds[,!(names(odds) %in% c("REFRESH_DATE"))]
# Order by MATCH_ID
odds <- odds[order(odds$MATCH_ID),]
# Change the missing values (-1) to NAs
odds[odds == -1] <- NA
# Remove the rows with all NAs
MATCH_ID <- odds$MATCH_ID
odds <- odds[,-1]
MATCH_ID <- MATCH_ID[apply(odds,1,function(x)any(!is.na(x)))]
odds <- odds[apply(odds,1,function(x)any(!is.na(x))),]
odds <- cbind(MATCH_ID, odds)
# Remove false data
odds <- odds[apply(odds[,2:ncol(odds)] < 15 | is.na(odds[,2:ncol(odds)]), 1, all),]
# Reset the row indexes
rownames(odds) <- NULL
# Save the column names
ocolumns <- names(odds)
# Read matches data
# matches <- dbReadTable(jdbcConnection, "MATCHES")
matches <- read.csv("matches.csv")
# Change the missing values (N/A) to NAs
matches[matches == "N/A"] <- NA
# Remove the matches without result
matches <- matches[complete.cases(matches),]
# We don't want two matches with the same round number
matches <- matches[order(matches$ROUND, matches$MATCH_DATE),]
# If there is more than 38 (one season) matches for a team
for (j in 1:38) {
  # If there are more than 10 matches in one round
  if (length(which(matches$ROUND == j)) > 10) {
    # Get the number of seasons
    seasons <- length(which(matches$ROUND == j))/10
    # Save the actual round
    round <- matches[which(matches$ROUND == j),]
    for (k in seasons) {
      # get the actual seasons matches
      act_round <- round[((k-1)*10+1):(k*10),]
      for (l in 1:10) {
        act_round[l,"ROUND"] <- act_round[l,"ROUND"] + (k-1)*38
      }
      round[((k-1)*10+1):(k*10),] <- act_round
    }
    matches[which(matches$ROUND == j),] <- round
  }
}
matches <- matches[order(matches$ROUND),]
# Extract results by team
teams <- extractteams(matches)

# Extract features
# Create new dataframe
df <- data.frame()
# Variable for tracking the complete cases
j <- 1
# Iterate through the matches
for (i in 1:nrow(matches)) {
  row <- matches[i,]
  # If there is odds data for the match
  if (row$ID %in% odds$MATCH_ID) {
    # Save the odds for the match
    o1 <- odds[odds$MATCH_ID == row$ID,]
    # Save the last odds data for the match
    o2 <- o1[nrow(o1),]
    # Add the result class to the dataframe
    df[j,"CLASS"] <- result2class(row$RESULT)
    # Add the odds to the dataframe
    for (column in ocolumns) {
      df[j,column] <- o2[,column]
    }
    # Get the home team's matches
    hteam <- teams[row$HOME_TEAM_ID + 1]
    # I need only the round and the result
    if (length(hteam) > 0) {
      hteam2 <- hteam[[1]][,c("ROUND", "CLASS")]
    }
    # Get the home team's previous matches' result
    if (row$ROUND > 1 && length(hteam2[hteam2$ROUND == row$ROUND - 1, "CLASS"]) != 0) {
      df[j,"HPREV1"] <- hteam2[hteam2$ROUND == row$ROUND - 1, "CLASS"]
    }
    if (row$ROUND > 2 && length(hteam2[hteam2$ROUND == row$ROUND - 2, "CLASS"]) != 0) {
      df[j,"HPREV2"] <- hteam2[hteam2$ROUND == row$ROUND - 2, "CLASS"]
    }
    if (row$ROUND > 3 && length(hteam2[hteam2$ROUND == row$ROUND - 3, "CLASS"]) != 0) {
      df[j,"HPREV3"] <- hteam2[hteam2$ROUND == row$ROUND - 3, "CLASS"]
    }
    if (row$ROUND > 4 && length(hteam2[hteam2$ROUND == row$ROUND - 4, "CLASS"]) != 0) {
      df[j,"HPREV4"] <- hteam2[hteam2$ROUND == row$ROUND - 4, "CLASS"]
    }
    # Do the same to the away team
    ateam <- teams[row$AWAY_TEAM_ID + 1]
    if (length(ateam) > 0) {
      ateam2 <- ateam[[1]][,c("ROUND", "CLASS")]
    }
    if (row$ROUND > 1 && length(ateam2[ateam2$ROUND == row$ROUND - 1, "CLASS"]) != 0) {
      df[j,"APREV1"] <- ateam2[ateam2$ROUND == row$ROUND - 1, "CLASS"]
    }
    if (row$ROUND > 2 && length(ateam2[ateam2$ROUND == row$ROUND - 2, "CLASS"]) != 0) {
      df[j,"APREV2"] <- ateam2[ateam2$ROUND == row$ROUND - 2, "CLASS"]
    }
    if (row$ROUND > 3 && length(ateam2[ateam2$ROUND == row$ROUND - 3, "CLASS"]) != 0) {
      df[j,"APREV3"] <- ateam2[ateam2$ROUND == row$ROUND - 3, "CLASS"]
    }
    if (row$ROUND > 4 && length(ateam2[ateam2$ROUND == row$ROUND - 4, "CLASS"]) != 0) {
      df[j,"APREV4"] <- ateam2[ateam2$ROUND == row$ROUND - 4, "CLASS"]
    }
    # Increment the tracking variable
    j <- j + 1
  }
}

# Remove the MATCH_ID column from the dataframe
# df <- df[,!(names(df) %in% c("MATCH_ID"))]

# Get the bookmakers' name
home_bookmakers <- sapply(names(df), function(text) {
  grep("HOME", names(df), value = TRUE)
})[,1]
draw_bookmakers <- sapply(names(df), function(text) {
  grep("DRAW", names(df), value = TRUE)
})[,1]
away_bookmakers <- sapply(names(df), function(text) {
  grep("AWAY", names(df), value = TRUE)
})[,1]

# Create 'average' bookmaker
df[,"HOME_AVG"] <- apply(df[,home_bookmakers], 1, mean, na.rm=TRUE)
df[,"DRAW_AVG"] <- apply(df[,draw_bookmakers], 1, mean, na.rm=TRUE)
df[,"AWAY_AVG"] <- apply(df[,away_bookmakers], 1, mean, na.rm=TRUE)



# Write out the table
dbWriteTable(jdbcConnection, "CLEANEDDATA", df)

# Close the database connection
dbDisconnect(jdbcConnection) 

# write.csv(df, "./cleaned.csv")
