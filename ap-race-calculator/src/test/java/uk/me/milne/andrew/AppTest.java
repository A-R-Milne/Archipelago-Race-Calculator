package uk.me.milne.andrew;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.Test;

public class AppTest {
    @Test
    public void maximizesAssignedPlayersBeforeGamePreference() {
        ArrayList<String> players = new ArrayList<>(Arrays.asList("p1", "p2", "p3", "p4"));
        HashMap<String, ArrayList<String>> gamePlayers = new HashMap<>();
        gamePlayers.put("Highly preferred", new ArrayList<>(Arrays.asList("p1", "p2")));
        gamePlayers.put("Alternative 1", new ArrayList<>(Arrays.asList("p1", "p3")));
        gamePlayers.put("Alternative 2", new ArrayList<>(Arrays.asList("p2", "p4")));
        HashMap<String, Integer> playCount = new HashMap<>();
        playCount.put("Highly preferred", -1000);
        playCount.put("Alternative 1", 0);
        playCount.put("Alternative 2", 0);

        HashMap<String, ArrayList<String>> result =
                App.determineTeams(players, gamePlayers, playCount, 2);

        assertEquals(2, result.get("Alternative 1").size());
        assertEquals(2, result.get("Alternative 2").size());
        assertEquals(0, result.getOrDefault("Highly preferred", new ArrayList<>()).size());
        assertEquals(0, result.get("No Game").size());
    }

    @Test
    public void maximizesGamePreferenceAfterAssignedPlayerCount() {
        ArrayList<String> players = new ArrayList<>(Arrays.asList("p1", "p2"));
        HashMap<String, ArrayList<String>> gamePlayers = new HashMap<>();
        gamePlayers.put("Less preferred", new ArrayList<>(Arrays.asList("p1", "p2")));
        gamePlayers.put("More preferred", new ArrayList<>(Arrays.asList("p1", "p2")));
        HashMap<String, Integer> playCount = new HashMap<>();
        playCount.put("Less preferred", 2);
        playCount.put("More preferred", 0);

        HashMap<String, ArrayList<String>> result =
                App.determineTeams(players, gamePlayers, playCount, 2);

        assertEquals(0, result.getOrDefault("Less preferred", new ArrayList<>()).size());
        assertEquals(2, result.get("More preferred").size());
        assertEquals(0, result.get("No Game").size());
    }
}
