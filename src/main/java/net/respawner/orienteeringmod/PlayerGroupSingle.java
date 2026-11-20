/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.respawner.orienteeringmod.ICourse;
import net.respawner.orienteeringmod.IGrade;
import net.respawner.orienteeringmod.IPlayerGroup;
import net.respawner.orienteeringmod.ORTPlayer;
import org.apache.logging.log4j.LogManager;

public class PlayerGroupSingle
implements IPlayerGroup {
    private static final long serialVersionUID = 4L;
    private String thePlayer = null;
    private String theCourse = null;

    public String toString() {
        return this.thePlayer;
    }

    @Override
    public boolean containsPlayer(String playerName) {
        return this.thePlayer.equals(playerName);
    }

    @Override
    public String getGroupName() {
        return this.thePlayer;
    }

    @Override
    public String getDefaultPlayer() {
        return this.thePlayer;
    }

    @Override
    public void init(String groupName, List<String> courses) {
        this.thePlayer = groupName;
        this.theCourse = courses.get(0);
    }

    @Override
    public String getDefaultCourse() {
        return this.theCourse;
    }

    @Override
    public String getCourse(String playerName) {
        return this.theCourse;
    }

    public void setCourse(String courseName) {
        this.theCourse = courseName;
    }

    @Override
    public List<String> getAllCourses() {
        return new ArrayList<String>(Arrays.asList(this.theCourse));
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public IGrade computeGroupGrade(Map<String, ORTPlayer> players, Map<String, ICourse> courses) {
        ICourse c = courses.get(this.theCourse);
        if (c == null) {
            LogManager.getLogger().warn("Error: did not find course data with the name \"" + this.theCourse + "\" in player group \"" + this.getGroupName() + "\".");
            return null;
        }
        return c.computeGrade(new ORTPlayer[]{players.get(this.thePlayer)});
    }
}

