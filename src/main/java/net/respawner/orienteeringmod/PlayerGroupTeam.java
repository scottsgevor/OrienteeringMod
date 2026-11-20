/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.respawner.orienteeringmod.CourseTeam;
import net.respawner.orienteeringmod.GradeTeam;
import net.respawner.orienteeringmod.ICourse;
import net.respawner.orienteeringmod.IGrade;
import net.respawner.orienteeringmod.IPlayerGroup;
import net.respawner.orienteeringmod.ORTPlayer;
import org.apache.logging.log4j.LogManager;

public class PlayerGroupTeam
implements IPlayerGroup {
    private static final long serialVersionUID = 4L;
    private String groupName = "";
    private List<String> players = new ArrayList<String>();
    private String course;

    @Override
    public int size() {
        return this.players.size();
    }

    public PlayerGroupTeam(String[] players) {
        this.players = Arrays.asList(players);
    }

    @Override
    public void init(String groupName, List<String> courses) {
        this.groupName = Objects.requireNonNull(groupName);
        this.course = Objects.requireNonNull(Objects.requireNonNull(courses).get(0));
    }

    @Override
    public boolean containsPlayer(String playerName) {
        return this.players.contains(playerName);
    }

    @Override
    public String getGroupName() {
        return this.groupName;
    }

    @Override
    public String getDefaultPlayer() {
        return this.players.get(0);
    }

    @Override
    public String getDefaultCourse() {
        return this.course;
    }

    public String getCourseRequired() {
        return this.course;
    }

    @Override
    public String getCourse(String playerName) {
        if (!this.containsPlayer(playerName)) {
            return null;
        }
        return this.course;
    }

    @Override
    public List<String> getAllCourses() {
        return Arrays.asList(this.course);
    }

    @Override
    public IGrade computeGroupGrade(Map<String, ORTPlayer> players, Map<String, ICourse> courses) {
        ICourse c = courses.get(this.course);
        if (c == null || !(c instanceof CourseTeam)) {
            LogManager.getLogger().warn("Error: did not find appropriate course data with the name \"" + this.course + "\" in player group \"" + this.getGroupName() + "\".");
            return null;
        }
        ORTPlayer[] ps = new ORTPlayer[this.players.size()];
        for (int i = 0; i < this.players.size(); ++i) {
            ps[i] = players.get(this.players.get(i));
            if (ps[i] != null) continue;
            LogManager.getLogger().info("Player group " + this.getGroupName() + ": no grade exportable due to incomplete match.");
            return null;
        }
        return ((GradeTeam)c.computeGrade(ps)).setGroupName(this.groupName);
    }
}

