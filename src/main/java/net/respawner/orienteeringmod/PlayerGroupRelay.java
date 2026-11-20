/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.respawner.orienteeringmod.GradeRelay;
import net.respawner.orienteeringmod.GradeSP;
import net.respawner.orienteeringmod.ICourse;
import net.respawner.orienteeringmod.IGrade;
import net.respawner.orienteeringmod.IPlayerGroup;
import net.respawner.orienteeringmod.ORTPlayer;
import org.apache.logging.log4j.LogManager;

public class PlayerGroupRelay
implements IPlayerGroup {
    private static final long serialVersionUID = 1L;
    private String groupName = "";
    private List<String> courses = new ArrayList<String>();

    @Override
    public int size() {
        return this.courses.size();
    }

    public String toString() {
        return this.groupName;
    }

    @Override
    public boolean containsPlayer(String playerName) {
        if (!this.groupName.equals(this.getGroupName(playerName))) {
            return false;
        }
        return this.getTurn(playerName) >= 0 && this.getTurn(playerName) < this.courses.size();
    }

    @Override
    public String getGroupName() {
        return this.groupName;
    }

    @Override
    public String getDefaultPlayer() {
        return this.groupName + "-1";
    }

    @Override
    public void init(String groupName, List<String> courses) {
        this.groupName = groupName;
        this.courses = courses;
    }

    @Override
    public String getDefaultCourse() {
        return this.courses.get(0);
    }

    @Override
    @Nullable
    public String getCourse(String playerName) {
        if (!this.containsPlayer(playerName)) {
            return null;
        }
        try {
            return this.courses.get(this.getTurn(playerName));
        }
        catch (Throwable e) {
            return null;
        }
    }

    @Override
    public List<String> getAllCourses() {
        return this.courses;
    }

    @Nullable
    public String getGroupName(String playerName) {
        return PlayerGroupRelay.getGroupNameByPlayer(playerName);
    }

    public static String getGroupNameByPlayer(String playerName) {
        int i;
        for (i = playerName.length() - 1; i >= 0 && playerName.charAt(i) != '-'; --i) {
        }
        if (i <= 0) {
            return null;
        }
        return String.copyValueOf(playerName.toCharArray(), 0, i);
    }

    public static String getTurnByPlayer(String playerName) {
        int i;
        for (i = playerName.length() - 1; i >= 0 && playerName.charAt(i) != '-'; --i) {
        }
        if (i <= 0) {
            return null;
        }
        return String.copyValueOf(playerName.toCharArray(), i + 1, playerName.length() - i - 1);
    }

    public static String composePlayerName(String groupName, int turn) {
        return groupName + "-" + (turn + 1);
    }

    public String composePlayerName(int turn) {
        return PlayerGroupRelay.composePlayerName(this.groupName, turn);
    }

    public int getTurn(String playerName) {
        int i;
        for (i = playerName.length() - 1; i >= 0 && playerName.charAt(i) != '-'; --i) {
        }
        try {
            return Integer.parseInt(String.copyValueOf(playerName.toCharArray(), i + 1, playerName.length() - i - 1)) - 1;
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }

    public String getPlayerByTurn(int turn) {
        if (turn < 0 || turn >= this.courses.size()) {
            return null;
        }
        return this.groupName + "-" + (turn + 1);
    }

    @Override
    public IGrade computeGroupGrade(Map<String, ORTPlayer> players, Map<String, ICourse> courses) {
        GradeSP[] gs = new GradeSP[this.courses.size()];
        for (int i = 0; i < this.courses.size(); ++i) {
            ICourse c = courses.get(this.courses.get(i));
            if (c == null) {
                LogManager.getLogger().warn("Error: did not find course data with the name \"" + this.courses.get(i) + "\" in player group \"" + this.getGroupName() + "\".");
                return null;
            }
            IGrade g = c.computeGrade(new ORTPlayer[]{players.get(this.composePlayerName(i))});
            gs[i] = g instanceof GradeSP ? (GradeSP)g : null;
        }
        return new GradeRelay(gs, courses.get(this.courses.get(0)).getMatchName(), this.groupName);
    }
}

