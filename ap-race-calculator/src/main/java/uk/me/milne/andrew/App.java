package uk.me.milne.andrew;

import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App 
{
    private static boolean AreAssignmentsSame(HashMap<String, ArrayList<String>> originalTeams, HashMap<String, ArrayList<String>> newTeams) {
        boolean isSame = true;
        for (String key : originalTeams.keySet()) {
            if (!newTeams.containsKey(key)) {
                isSame = false;
                break;
            }
            if (originalTeams.get(key).size() != newTeams.get(key).size()) {
                isSame = false;
                break;
            }
            for (String participant : originalTeams.get(key)) {
                if (!newTeams.get(key).contains(participant)) {
                    isSame = false;
                    break;
                }
            }
        }
        return isSame;
    }

    private static HashMap<String, ArrayList<String>> copyLists(HashMap<String, ArrayList<String>> source) {
        HashMap<String, ArrayList<String>> copy = new HashMap<>();
        for (Map.Entry<String, ArrayList<String>> entry : source.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    private static HashMap<String, ArrayList<String>> PermuteTeams(String permutedPlayer, String permutedGame, ArrayList<String> allPlayers, HashMap<String, ArrayList<String>> gamePlayers, Integer teamCount, ArrayList<String> placedPlayers, ArrayList<String> includedGames, HashMap<String, ArrayList<String>> currentBestAssignment, HashMap<String, ArrayList<String>> playerGames, HashMap<String, Integer> scores, int layer) {
        if (currentBestAssignment.containsKey(permutedGame)) {
            currentBestAssignment.get(permutedGame).add(permutedPlayer);
        } else {
            ArrayList<String> playerList = new ArrayList<>();
            playerList.add(permutedPlayer);
            currentBestAssignment.put(permutedGame, playerList);
        }
        placedPlayers.add(permutedPlayer);
        for (ArrayList<String> playerList : gamePlayers.values()) {
            playerList.remove(permutedPlayer);
        }
        allPlayers.remove(permutedPlayer);
        for (String game : currentBestAssignment.keySet()) {
            if (game != permutedGame) {
                currentBestAssignment.get(game).remove(permutedPlayer);
            }
        }
        if (!includedGames.contains(permutedGame)) {
            includedGames.add(permutedGame);
        }
        HashMap<String, ArrayList<String>> resultTeam = GetBestTeamLineup(allPlayers, gamePlayers, teamCount, placedPlayers, includedGames,currentBestAssignment, playerGames, scores, layer+1);
        return resultTeam;
    }

    private static int GetScore(HashMap<String, ArrayList<String>> team, HashMap<String, Integer> scores, Integer totalPlayerCount) {
        int playerCount = 0;
        int size;
        int totalScore = 0;
        for (String game : team.keySet()) {
            size = team.get(game).size();
            playerCount += size;
            totalScore += scores.get(game) * size;
        }
        if (totalPlayerCount > playerCount) {
            totalScore -= 100 * (totalPlayerCount - playerCount);
        }
        return totalScore;
    }

    private static HashMap<String, ArrayList<String>> GetBestTeamLineup(ArrayList<String> allPlayers, HashMap<String, ArrayList<String>> gamePlayers, Integer teamCount, ArrayList<String> placedPlayers, ArrayList<String> includedGames, HashMap<String, ArrayList<String>> currentBestAssignment, HashMap<String, ArrayList<String>> playerGames, HashMap<String, Integer> scores, int layer) {
        boolean gameAssigned;
        boolean finalOutputFound = false;
        HashMap<String, ArrayList<String>> oldBestAssignment;
        if (layer <= (placedPlayers.size() + allPlayers.size())/6) {
            String tab = "   ";
            String tabs = "";
            for (int i = 0; i < layer; i++) {
                tabs += tab;
            }
            System.out.println(tabs+"Starting Layer "+layer);
        }

        while (!finalOutputFound) {
            oldBestAssignment = copyLists(currentBestAssignment);
            gameAssigned = false;
            allPlayers.removeAll(placedPlayers);
            // If allPlayers is empty, return incomplete games to No Game and end
            if (allPlayers.size() == 0) {
                for (String game : includedGames) {
                    while (currentBestAssignment.get(game).size() % teamCount != 0) {
                        String randomPlayer = currentBestAssignment.get(game).get(new Random().nextInt(currentBestAssignment.get(game).size()));
                        currentBestAssignment.get(game).remove(randomPlayer);
                        if (!currentBestAssignment.containsKey("No Game")) {
                            ArrayList<String> newNoGame = new ArrayList<>();
                            newNoGame.add(randomPlayer);
                            currentBestAssignment.put("No Game", newNoGame);
                        } else if (currentBestAssignment.get("No Game").equals(null)) {
                            ArrayList<String> newNoGame = new ArrayList<>();
                            newNoGame.add(randomPlayer);
                            currentBestAssignment.put("No Game", newNoGame);
                        }
                        else {
                            currentBestAssignment.get("No Game").add(randomPlayer);
                        }
                    }
                }
                finalOutputFound = true;
                continue;
            }
            // If allPlayers has size under teamCount and all games are complete, end
            if (allPlayers.size() < teamCount) {
                boolean allValid = true;
                for (String game : includedGames) {
                    if (currentBestAssignment.get(game).size() % teamCount != 0) {
                        allValid = false;
                    }
                }
                if (allValid) {
                    finalOutputFound = true;
                    continue;
                }
            }
            
            // If someone only has a single eligible game, put them on it unless it would result in a number of players not a multiple of teamCount being guaranteed for a game
            ArrayList<String> remainingPlayers = new ArrayList<>();
            remainingPlayers.addAll(allPlayers);
            for (String player : remainingPlayers) {
                if (playerGames.get(player).size() == 1) {
                    boolean spaceExists = true;
                    ArrayList<String> listForGame = new ArrayList<>();
                    String gameForPlayer = playerGames.get(player).get(0);
                    if (currentBestAssignment.containsKey(gameForPlayer)) {
                        listForGame = currentBestAssignment.get(gameForPlayer);
                        if (listForGame.size() + teamCount > gamePlayers.get(gameForPlayer).size() && listForGame.size() % teamCount == 0) {
                            spaceExists = false;
                        }
                    }
                    if (spaceExists) {
                        listForGame.add(player);
                        currentBestAssignment.put(gameForPlayer, listForGame);
                        if (!includedGames.contains(gameForPlayer)) {
                            includedGames.add(gameForPlayer);
                        }
                    }
                    placedPlayers.add(player);
                    allPlayers.remove(player);
                    gamePlayers.get(gameForPlayer).remove(player);
                    for (String game : currentBestAssignment.keySet()) {
                        if (game != gameForPlayer) {
                            currentBestAssignment.get(game).remove(player);
                        }
                    }
                    gameAssigned = true;
                }
            }
            if (gameAssigned) {
                continue;
            }

            // If a game has a number of players not a multiple of teamCount and exactly enough remaining eligible players to put it onto a multiple of teamCount, put them on it
            for (String game : includedGames) {
                if ((currentBestAssignment.get(game).size() % teamCount > 0) && (gamePlayers.get(game).size() + (currentBestAssignment.get(game).size() % teamCount)) == teamCount) {
                    String player = gamePlayers.get(game).get(0);
                    currentBestAssignment.get(game).add(player);
                    placedPlayers.add(player);
                    for (ArrayList<String> playerList : gamePlayers.values()) {
                        playerList.remove(player);
                    }
                    for (String gameIterate : currentBestAssignment.keySet()) {
                        if (gameIterate != game) {
                            currentBestAssignment.get(gameIterate).remove(player);
                        }
                    }
                    gameAssigned = true;
                }
            }
            if (gameAssigned) {
                continue;
            }
            
            // Get everyone tied for the lowest number of eligible games remaining and get the best score from each permutation of assigning one eligible game to one of them
            int minimumRemaining = 2;
            ArrayList<String> permutingPlayers = new ArrayList<>();
            while (!gameAssigned) {
                for (String player : allPlayers) {
                    if (playerGames.get(player).size() == minimumRemaining) {
                        permutingPlayers.add(player);
                    }
                }
                if (permutingPlayers.size() == 0) {
                    minimumRemaining++;
                    if (minimumRemaining < 40) {
                        continue;
                    }
                    finalOutputFound = true;
                }
                gameAssigned = true;
                int bestScore = -100 * (allPlayers.size() + placedPlayers.size());
                HashMap<String, ArrayList<String>> bestTeam = new HashMap<>();
                ArrayList<String> everyPlayer = new ArrayList<>();
                everyPlayer.addAll(allPlayers);
                everyPlayer.addAll(placedPlayers);
                String playerToCheck = permutingPlayers.get(new Random().nextInt(permutingPlayers.size()));
                ArrayList<String> gamesToCheck = new ArrayList<>();
                gamesToCheck.addAll(playerGames.get(playerToCheck));
                Collections.shuffle(gamesToCheck);
                for (int i = 0; i < gamesToCheck.size(); i++) {
                    String game = gamesToCheck.get(i);
                    ArrayList<String> newAllPlayers = new ArrayList<>();
                    newAllPlayers.addAll(allPlayers);
                    HashMap<String, ArrayList<String>> newGamePlayers = copyLists(gamePlayers);
                    Integer newTeamCount = Integer.valueOf(teamCount);
                    ArrayList<String> newPlacedPlayers = new ArrayList<>();
                    newPlacedPlayers.addAll(placedPlayers);
                    ArrayList<String> newIncludedGames = new ArrayList<>();
                    newIncludedGames.addAll(includedGames);
                    HashMap<String, ArrayList<String>> newCurrentBestAssignment = copyLists(currentBestAssignment);
                    HashMap<String, ArrayList<String>> newPlayerGames = copyLists(playerGames);
                    HashMap<String, Integer> newScores = new HashMap<>();
                    newScores.putAll(scores);

                    HashMap<String, ArrayList<String>> resultTeam = PermuteTeams(playerToCheck, game, newAllPlayers, newGamePlayers, newTeamCount, newPlacedPlayers, newIncludedGames, newCurrentBestAssignment, newPlayerGames, newScores, layer);
                    int resultScore = GetScore(resultTeam, scores, placedPlayers.size() + allPlayers.size());
                    if (resultScore > bestScore) {
                        bestScore = resultScore;
                        bestTeam = resultTeam;
                    }
                }
                if (AreAssignmentsSame(currentBestAssignment, bestTeam)) {
                    finalOutputFound = true;
                    continue;
                }
                currentBestAssignment = bestTeam;
                includedGames = new ArrayList<>();
                includedGames.addAll(currentBestAssignment.keySet());
                includedGames.remove("No Game");
                placedPlayers = new ArrayList<>();
                allPlayers = new ArrayList<>();
                for (String game : currentBestAssignment.keySet()) {
                    if (game.equals("No Game")) {
                        allPlayers.addAll(currentBestAssignment.get(game));
                    }
                    else {
                        placedPlayers.addAll(currentBestAssignment.get(game));
                    }
                }
            }
            if (oldBestAssignment.equals(currentBestAssignment)) {
                finalOutputFound = true;
                continue;
            }
        }
        if (layer <= (placedPlayers.size() + allPlayers.size())/6) {
            String tab = "   ";
            String tabs = "";
            for (int i = 0; i < layer; i++) {
                tabs += tab;
            }
            System.out.println(tabs+"Finishing Layer "+layer+"; Best Score = "+GetScore(currentBestAssignment, scores, placedPlayers.size() + allPlayers.size()));
        }
        return currentBestAssignment;
    }

    private static HashMap<String, ArrayList<String>> DetermineTeams(ArrayList<String> allPlayers, HashMap<String, ArrayList<String>> gamePlayers, HashMap<String, Integer> playCount, Integer teamCount) {
        // If a game has a single player, remove it
        ArrayList<String> gamesToRemove = new ArrayList<>();
        for (String game : gamePlayers.keySet()) {
            if (gamePlayers.get(game).size() <= 1) {
                gamesToRemove.add(game);
            }
        }
        for (String game : gamesToRemove) {
            gamePlayers.remove(game);
        }

        // Set up "no game" if it does not already exist
        if (!gamePlayers.containsKey("No Game")) {
            gamePlayers.put("No Game", allPlayers);
        }
        if (!playCount.containsKey("No Game")) {
            playCount.put("No Game", 1000);
        }
        
        // Determine game scores
        HashMap<String, Integer> scores = new HashMap<>();
        for(String key : playCount.keySet()) {
            scores.put(key, gamePlayers.get(key).size()-playCount.get(key));
        }
        System.out.println(scores);

        // Set up HashMap of games per player
        HashMap<String, ArrayList<String>> playerGames = new HashMap<>();
        for (String player : allPlayers) {
            ArrayList<String> games = new ArrayList<>();
            for (String game : gamePlayers.keySet()) {
                if (gamePlayers.get(game).contains(player) && !game.equals("No Game")) {
                    games.add(game);
                }
            }
            playerGames.put(player, games);
        }
        
        // Set an initial setup where nobody has a game
        HashMap<String, ArrayList<String>> currentBestAssignment = new HashMap<>();
        currentBestAssignment.put("No Game", allPlayers);
        
        // Determine the best scoring setup
        return GetBestTeamLineup(allPlayers, gamePlayers, teamCount, new ArrayList<>(), new ArrayList<>(), currentBestAssignment, playerGames, scores, 0);
    }

    public static void main( String[] args )
    {
        // Handle team count
        int teamCount;
        if (args.length == 0) {
            throw new IllegalArgumentException("The following arguments must exist in the following order: 1. Filename of the CSV file containing the player-game info; 2. Number of teams (defaults to 2 if not present)");
        }
        if (args.length == 1) {
            teamCount = 2;
        }
        else {
            teamCount = Integer.parseInt(args[1]);
        }

        // Set up other variables for DetermineTeams()
        String csvFileName = args[0];
        HashMap<String, ArrayList<String>> gamePlayers = new HashMap<>();
        ArrayList<String> allPlayers = new ArrayList<>();
        HashMap<String, Integer> playCount = new HashMap<>();

        // Import the CSV
        try (BufferedReader br = new BufferedReader(new FileReader(csvFileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] linesArray = line.split(",");
                ArrayList<String> players = new ArrayList<>();
                for (int i = 2; i < linesArray.length; i++) {
                    String player = linesArray[i];
                    players.add(player);
                    if (!allPlayers.contains(player)) {
                        allPlayers.add(player);
                    }
                }
                gamePlayers.put(linesArray[0], players);
                playCount.put(linesArray[0], Integer.parseInt(linesArray[1]));
            }
            
            // Determine the optimal teams
            HashMap<String, ArrayList<String>> optimalTeams = DetermineTeams(allPlayers, gamePlayers, playCount, teamCount);
            ArrayList<String> emptyGames = new ArrayList<>();
            for (String game : optimalTeams.keySet()) {
                if (optimalTeams.get(game).size() == 0) {
                    emptyGames.add(game);
                }
            }
            for (String game : emptyGames) {
                optimalTeams.remove(game);
            }
            System.out.println("Teams: ");
            for (String game : optimalTeams.keySet()) {
                System.out.println(game + ": " + optimalTeams.get(game));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
