/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.respawner.orienteeringmod.ICourse;
import net.respawner.orienteeringmod.IGrade;
import net.respawner.orienteeringmod.MsgMatchClient;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTIngameException;
import net.respawner.orienteeringmod.ORTMatch;
import net.respawner.orienteeringmod.ORTMatchDNEException;
import net.respawner.orienteeringmod.ORTPlayer;
import net.respawner.orienteeringmod.ORTPlayerNotLoadedException;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public class ORTWorld {
    public String matchName;
    public ORTMatch match;
    public Map<String, ICourse> courses = new HashMap<String, ICourse>();
    public Map<String, ORTPlayer> players = new HashMap<String, ORTPlayer>();
    public List<String> unloadedPlayers;
    public Map<String, IGrade> grades = new HashMap<String, IGrade>();
    public static long lastSavingTime;
    private static ORTWorld ORTWorldServer;
    private static MsgMatchClient ORTWorldClient;

    private ORTWorld() {
    }

    public static boolean hasMatchServer() {
        return ORTWorldServer != null;
    }

    public static ORTWorld getMatchInstanceServer() {
        return ORTWorldServer;
    }

    public void onPlayerJoin(String playerName) throws ORTIngameException {
        if (!this.match.isRosterContainsPlayer(playerName)) {
            this.match.registerNewPlayer(playerName);
        }
        if (!this.players.containsKey(playerName) && !this.match.isSpectator(playerName)) {
            this.players.put(playerName, ORTPlayer.createEmptyPlayer(playerName));
        }
    }

    @Nullable
    public ORTPlayer getPlayerByName(String playerName) {
        if (playerName == null) {
            return null;
        }
        return this.players.get(playerName);
    }

    @Nullable
    public ICourse getCourseByName(String courseName) {
        if (courseName == null) {
            return null;
        }
        return this.courses.get(courseName);
    }

    public static void loadMatch(String matchName, boolean doLoadPlayers) throws ORTFileException {
        ORTWorld newWorld = new ORTWorld();
        try {
            newWorld.match = ORTMatch.readFromFile(matchName);
            LogManager.getLogger().info("Orienteering match successfully loaded.");
        }
        catch (ORTFileException e) {
            throw new ORTMatchDNEException();
        }
        newWorld.matchName = matchName;
        ArrayList<String> unloadedCourses = new ArrayList<String>();
        Set<String> courseList = newWorld.match.getCourseList();
        for (String string : courseList) {
            try {
                newWorld.courses.put(string, ICourse.readFromFile(matchName, string));
                ICourse c = newWorld.courses.get(string);
                LogManager.getLogger().info("Course \"" + string + "\" loaded. Course type: " + c.getCourseType().toString());
            }
            catch (ORTFileException e) {
                unloadedCourses.add(string);
            }
        }
        if (unloadedCourses.size() > 0) {
            throw new ORTFileException(ORTFileException.FileType.Match, ORTFileException.IOType.Read, null, unloadedCourses);
        }
        newWorld.unloadedPlayers = new ArrayList<String>();
        if (doLoadPlayers) {
            File dir = new File(Util.getPlayerDataPath(matchName));
            for (File player : dir.listFiles()) {
                try {
                    newWorld.players.put(player.getName(), ORTPlayer.readFromFile(matchName, player.getName(), true));
                }
                catch (ORTFileException e) {
                    newWorld.unloadedPlayers.add(player.getName());
                }
            }
            if (newWorld.unloadedPlayers.size() > 0) {
                LogManager.getLogger().info("Some players are not successfully loaded.");
                throw new ORTPlayerNotLoadedException(ORTFileException.FileType.Match, ORTFileException.IOType.Read, newWorld.unloadedPlayers);
            }
            File file = new File(Util.getGradeDataPath(matchName));
            for (File grade : file.listFiles()) {
                try {
                    newWorld.grades.put(grade.getName(), IGrade.readFromFile(matchName, grade.getName()));
                }
                catch (ORTFileException e) {
                    LogManager.getLogger().warn("Grade file \"" + grade.getName() + "\" not loaded.");
                }
            }
        }
        LogManager.getLogger().info("Orienteering match loading complete.");
        ORTWorldServer = newWorld;
    }

    public static void setNull() {
        ORTWorldServer = null;
    }

    public static void saveAllDataToFile() throws ORTFileException {
        if (ORTWorldServer == null) {
            return;
        }
        ORTWorldServer.saveAll();
    }

    public void saveAll() throws ORTFileException {
        this.match.writeToFile(this.matchName);
        for (String course : this.courses.keySet()) {
            this.courses.get(course).writeToFile(this.matchName);
        }
        for (String player : this.players.keySet()) {
            this.players.get(player).writeToFile(this.matchName);
        }
        for (String group : this.grades.keySet()) {
            this.grades.get(group).writeToFile(this.matchName);
        }
    }

    public void clearPlayer() throws ORTFileException {
        File dir = new File(Util.getPlayerDataPath(this.matchName));
        for (File file : dir.listFiles()) {
            file.delete();
        }
        this.players = new HashMap<String, ORTPlayer>();
    }

    public static boolean hasMatchClient() {
        return ORTWorldClient != null;
    }

    public static MsgMatchClient getMatchInstanceClient() {
        return ORTWorldClient;
    }

    public static void setMatchClient(MsgMatchClient msg) {
        ORTWorldClient = msg;
    }

    static {
        ORTWorldServer = null;
        ORTWorldClient = null;
    }
}

