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

    private static ArrayList<HashMap<String, ArrayList<String>>> PermuteTeams(String permutedPlayer, String permutedGame, ArrayList<String> allPlayers, HashMap<String, ArrayList<String>> gamePlayers, Integer teamCount, ArrayList<String> placedPlayers, ArrayList<String> includedGames, HashMap<String, ArrayList<String>> currentBestAssignment, HashMap<String, ArrayList<String>> playerGames, HashMap<String, Integer> scores, int layer, Integer bestScore) {
        int remainingAssignments = Math.floorDiv(allPlayers.size()+placedPlayers.size(),teamCount);
        HashMap<String, Integer> scoreSet = new HashMap<>();
        HashMap<String, Integer> countSet = new HashMap<>();
        for (String game : gamePlayers.keySet()) {
            if (!"No Game".equals(game)) {
                for (int i = 0; i < gamePlayers.get(game).size(); i++) {
                    if (scoreSet.containsKey(game)) {
                        countSet.put(game, countSet.get(game)+1);
                    }
                    else {
                        scoreSet.put(game, scores.get(game));
                        countSet.put(game, 1);
                    }
                }
            }
        }
        ArrayList<String> notIncludedGames = new ArrayList<>();
        for (String game : includedGames) {
            if (!"No Game".equals(game)) {
                if (!currentBestAssignment.containsKey(game)) {
                    notIncludedGames.add(game);
                }
                else {
                    for (int i = 0; i < currentBestAssignment.get(game).size(); i++) {
                        if (scoreSet.containsKey(game)) {
                            countSet.put(game, countSet.get(game)+1);
                        }
                        else {
                            scoreSet.put(game, scores.get(game));
                            countSet.put(game, 1);
                        }
                    }
                }
            } 
        }
        includedGames.removeAll(notIncludedGames);
        int maxScore = -100 * (allPlayers.size() + placedPlayers.size());
        for (String game : includedGames) {
            int playersForGame = currentBestAssignment.get(game).size();
            if (playersForGame == 0) {
                continue;
            }
            int gamePlayersPerTeam;
            if (playersForGame % teamCount == 0) {
                gamePlayersPerTeam = playersForGame / teamCount;
            }
            else {
                gamePlayersPerTeam = Math.floorDiv(playersForGame, teamCount) + 1;
            }
            maxScore += (100 + scores.get(game)) * gamePlayersPerTeam * teamCount;
            remainingAssignments -= gamePlayersPerTeam;
            if (!"No Game".equals(game)) {
                countSet.put(game, countSet.get(game)-playersForGame);
                if (countSet.get(game) < teamCount) {
                    scoreSet.remove(game);
                    countSet.remove(game);
                }
            }
        }
        for (int assignment = 0; assignment < remainingAssignments; assignment++) {
            boolean foundValue = false;
            String addedGame = "";
            int addedScore = -100000;
            while (!foundValue) {
                if (countSet.size() == 0) {
                    foundValue = true;
                    continue;
                }
                addedGame = Collections.max(scoreSet.entrySet(), Map.Entry.comparingByValue()).getKey();
                addedScore = scoreSet.get(addedGame);
                if (countSet.get(addedGame) < 0) {
                    if (layer+1 <= (placedPlayers.size() + allPlayers.size())/6) {
                        String tab = "   ";
                        String tabs = "";
                        for (int i = 0; i < layer+1; i++) {
                            tabs += tab;
                        }
                        System.out.println(tabs+"Layer "+(layer+1)+" Unable To Create Perfect Match");
                        ArrayList<HashMap<String, ArrayList<String>>> result = new ArrayList<>();
                        result.add(new HashMap<>());
                        result.add(new HashMap<>());
                        return result;
                    }
                }
                if (countSet.get(addedGame) < teamCount) {
                    scoreSet.remove(addedGame);
                    countSet.remove(addedGame);
                }
                else {
                    foundValue = true;
                }
            }
            if (scoreSet.size() > 0 && addedScore >= -100) {
                maxScore += addedScore * teamCount;
                maxScore += 100 * teamCount;
                if (countSet.get(addedGame) == teamCount) {
                    scoreSet.remove(addedGame);
                    countSet.remove(addedGame);
                }
                else {
                    countSet.put(addedGame, countSet.get(addedGame)-teamCount);
                }
            }
        }
        if (maxScore <= bestScore) {
            if (layer+1 <= (placedPlayers.size() + allPlayers.size())/6) {
                String tab = "   ";
                String tabs = "";
                for (int i = 0; i < layer+1; i++) {
                    tabs += tab;
                }
                System.out.println(tabs+"Layer "+(layer+1)+" Unable To Exceed Best Score "+bestScore);
            }
            ArrayList<HashMap<String, ArrayList<String>>> result = new ArrayList<>();
            result.add(new HashMap<>());
            result.add(new HashMap<>());
            return result;
        }
        else if (layer+1 <= (placedPlayers.size() + allPlayers.size())/6) {
            String tab = "   ";
            String tabs = "";
            for (int i = 0; i < layer+1; i++) {
                tabs += tab;
            }
            System.out.println(tabs+"Layer "+(layer+1)+" has Max Score "+maxScore+"; the Best Score is currently "+bestScore);
        }
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
            if (!game.equals(permutedGame)) {
                currentBestAssignment.get(game).remove(permutedPlayer);
            }
        }
        if (!includedGames.contains(permutedGame)) {
            includedGames.add(permutedGame);
        }
        ArrayList<HashMap<String, ArrayList<String>>> result = GetBestTeamLineup(allPlayers, gamePlayers, teamCount, placedPlayers, includedGames,currentBestAssignment, playerGames, scores, layer+1, bestScore);
        return result;
    }

    private static int GetScore(HashMap<String, ArrayList<String>> team, HashMap<String, Integer> scores, Integer totalPlayerCount) {
        int playerCount = 0;
        int size;
        int totalScore = 0;
        for (String game : team.keySet()) {
            if (!game.equals("No Game")) {
                size = team.get(game).size();
                playerCount += size;
                totalScore += scores.get(game) * size;
            }
        }
        if (totalPlayerCount > playerCount) {
            totalScore -= 100 * (totalPlayerCount - playerCount);
        }
        return totalScore;
    }

    private static ArrayList<HashMap<String, ArrayList<String>>> GetBestTeamLineup(ArrayList<String> allPlayers, HashMap<String, ArrayList<String>> gamePlayers, Integer teamCount, ArrayList<String> placedPlayers, ArrayList<String> includedGames, HashMap<String, ArrayList<String>> currentBestAssignment, HashMap<String, ArrayList<String>> playerGames, HashMap<String, Integer> scores, int layer, Integer bestScore) {
        boolean gameAssigned;
        boolean finalOutputFound = (placedPlayers.size() + allPlayers.size() == 0);
        HashMap<String, ArrayList<String>> oldBestAssignment;
        boolean printLayer = (layer <= (placedPlayers.size() + allPlayers.size())/6);
        boolean invalid = false;
        if (printLayer) {
            String tab = "   ";
            String tabs = "";
            for (int i = 0; i < layer; i++) {
                tabs += tab;
            }
            System.out.println(tabs+"Starting Layer "+layer);
        }

        while (!finalOutputFound && !invalid) {
            oldBestAssignment = copyLists(currentBestAssignment);
            gameAssigned = false;
            allPlayers.removeAll(placedPlayers);

            // If a game has no remaining players and an incomplete set, reject this iteration
            for (String game : currentBestAssignment.keySet()) {
                if (currentBestAssignment.get(game).size() % teamCount != 0) {
                    if (!gamePlayers.containsKey(game)) {
                        invalid = true;
                        continue;
                    }
                    if (gamePlayers.get(game).size() == 0) {
                        invalid = true;
                        continue;
                    }
                }
            }
            if (invalid) {
                continue;
            }

            // If allPlayers is empty, return incomplete games to No Game and end
            if (allPlayers.size() == 0) {
                for (String game : includedGames) {
                    while (currentBestAssignment.get(game).size() % teamCount != 0) {
                        String randomPlayer = currentBestAssignment.get(game).get(new Random().nextInt(currentBestAssignment.get(game).size()));
                        currentBestAssignment.get(game).remove(randomPlayer);
                        currentBestAssignment.get("No Game").add(randomPlayer);
                    }
                }
                finalOutputFound = true;
                continue;
            }
            // If allPlayers has size under teamCount and all games are complete, end
            if (allPlayers.size() < teamCount) {
                boolean allValid = true;
                for (String game : currentBestAssignment.keySet()) {
                    if (currentBestAssignment.get(game).size() % teamCount != 0 && !"No Game".equals(game)) {
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
                    if (!gamePlayers.containsKey(gameForPlayer)) {
                        invalid = true;
                        continue;
                    }
                    if (currentBestAssignment.containsKey(gameForPlayer)) {
                        listForGame = currentBestAssignment.get(gameForPlayer);
                        int alreadyAssigned = listForGame.size();
                        int currentlyEligible = gamePlayers.get(gameForPlayer).size();
                        int requiredOtherPlayersToCompleteGame = teamCount - ((alreadyAssigned + 1) % teamCount) % teamCount;
                        spaceExists = (requiredOtherPlayersToCompleteGame <= (currentlyEligible - 1));
                    }
                    if (spaceExists) {
                        listForGame.add(player);
                        currentBestAssignment.put(gameForPlayer, listForGame);
                        if (!includedGames.contains(gameForPlayer)) {
                            includedGames.add(gameForPlayer);
                        }
                        placedPlayers.add(player);
                        allPlayers.remove(player);
                        gamePlayers.get(gameForPlayer).remove(player);
                        for (String game : currentBestAssignment.keySet()) {
                            if (!game.equals(gameForPlayer)) {
                                currentBestAssignment.get(game).remove(player);
                            }
                        }
                        gameAssigned = true;
                    }
                }
            }
            if (gameAssigned || invalid) {
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
                        if (!gameIterate.equals(game)) {
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
                int oldBestScore = bestScore;
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
                HashMap<String, ArrayList<String>> bestTeam = new HashMap<>();
                HashMap<String, ArrayList<String>> bestGamePlayers = new HashMap<>();
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

                    ArrayList<HashMap<String, ArrayList<String>>> results = PermuteTeams(playerToCheck, game, newAllPlayers, newGamePlayers, newTeamCount, newPlacedPlayers, newIncludedGames, newCurrentBestAssignment, playerGames, scores, layer, bestScore);
                    HashMap<String, ArrayList<String>> resultTeam = results.get(0);
                    HashMap<String, ArrayList<String>> resultGamePlayers = results.get(1);
                    int resultScore = GetScore(resultTeam, scores, everyPlayer.size());
                    if (resultScore > bestScore) {
                        bestScore = resultScore;
                        bestTeam = resultTeam;
                        bestGamePlayers = resultGamePlayers;
                    }
                }
                if (AreAssignmentsSame(currentBestAssignment, bestTeam)) {
                    finalOutputFound = true;
                    continue;
                }
                if (bestScore > oldBestScore) {
                    currentBestAssignment = bestTeam;
                    gamePlayers = bestGamePlayers;
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
            }
            if (oldBestAssignment.equals(currentBestAssignment)) {
                for (String game : oldBestAssignment.keySet()) {
                    ArrayList<String> players = oldBestAssignment.get(game);
                    if (players.size() % teamCount != 0) {
                        String randomPlayer = currentBestAssignment.get(game).get(new Random().nextInt(currentBestAssignment.get(game).size()));
                        currentBestAssignment.get(game).remove(randomPlayer);
                        currentBestAssignment.get("No Game").add(randomPlayer);
                    }
                }
                finalOutputFound = true;
                continue;
            }
        }
        // If a game has no remaining players and an incomplete set, reject this iteration
        if (invalid) {
            if (printLayer) {
                String tab = "   ";
                String tabs = "";
                for (int i = 0; i < layer; i++) {
                    tabs += tab;
                }
                System.out.println(tabs+"Finishing Layer "+layer+"; Invalid Configuration");
            }
            currentBestAssignment = new HashMap<>();
            ArrayList<String> everyPlayer = new ArrayList<>();
            everyPlayer.addAll(allPlayers);
            everyPlayer.addAll(placedPlayers);
            currentBestAssignment.put("No Game", everyPlayer);
            gamePlayers = new HashMap<>();
            ArrayList<HashMap<String, ArrayList<String>>> result = new ArrayList<>();
            result.add(currentBestAssignment);
            result.add(gamePlayers);
            return result;
        }
        if (printLayer) {
            String tab = "   ";
            String tabs = "";
            for (int i = 0; i < layer; i++) {
                tabs += tab;
            }
            if (placedPlayers.size() + allPlayers.size() == 0) {
                System.out.println(tabs+"Finishing Layer "+layer+"; Invalid Configuration");
            }
            else {
                System.out.println(tabs+"Finishing Layer "+layer+"; Best Score = "+GetScore(currentBestAssignment, scores, placedPlayers.size() + allPlayers.size()));
            }
        }
        ArrayList<HashMap<String, ArrayList<String>>> result = new ArrayList<>();
        result.add(currentBestAssignment);
        result.add(gamePlayers);
        return result;
    }

    private static HashMap<String, ArrayList<String>> DetermineTeams(ArrayList<String> allPlayers, HashMap<String, ArrayList<String>> gamePlayers, HashMap<String, Integer> playCount, Integer teamCount, Integer bestScore) {
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
        return GetBestTeamLineup(allPlayers, gamePlayers, teamCount, new ArrayList<>(), new ArrayList<>(), currentBestAssignment, playerGames, scores, 0, bestScore).get(0);
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
            HashMap<String, ArrayList<String>> optimalTeams = DetermineTeams(allPlayers, gamePlayers, playCount, teamCount, -10000*allPlayers.size());
            ArrayList<String> noGamePlayers = new ArrayList<>();
            noGamePlayers.addAll(allPlayers);
            for (ArrayList<String> players : optimalTeams.values()) {
                for (String player : players) {
                    noGamePlayers.remove(player);
                }
            }
            for (String player : noGamePlayers) {
                if (!optimalTeams.containsKey("No Game")) {
                    ArrayList<String> newNoGame = new ArrayList<>();
                    newNoGame.add(player);
                    optimalTeams.put("No Game", newNoGame);
                } else if (optimalTeams.get("No Game").equals(null)) {
                    ArrayList<String> newNoGame = new ArrayList<>();
                    newNoGame.add(player);
                    optimalTeams.put("No Game", newNoGame);
                }
                else {
                    optimalTeams.get("No Game").add(player);
                }
            }
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
