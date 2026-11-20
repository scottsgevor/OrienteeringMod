/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.respawner.orienteeringmod.CourseClassic;
import net.respawner.orienteeringmod.GradeClassic;
import net.respawner.orienteeringmod.GradeSP;
import net.respawner.orienteeringmod.GradeTeam;
import net.respawner.orienteeringmod.ICourse;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTPlayer;
import org.apache.logging.log4j.LogManager;

public class CourseTeam
extends CourseClassic {
    private static final long serialVersionUID = 4L;
    private List<Integer> CPList2 = new ArrayList<Integer>();

    public CourseTeam(JsonObject jsonCourse, String worldName, int deathPenalty) throws ORTFileException {
        super(jsonCourse, worldName, deathPenalty);
        try {
            JsonArray jsonCPList2 = jsonCourse.get("partitionedcp").getAsJsonArray();
            for (int k = 0; k < jsonCPList2.size(); ++k) {
                this.CPList2.add(jsonCPList2.get(k).getAsInt());
            }
        }
        catch (Throwable e) {
            if (e instanceof ORTFileException) {
                throw (ORTFileException)e;
            }
            LogManager.getLogger().error(e.toString());
            throw new ORTFileException(ORTFileException.FileType.Matchseed, ORTFileException.IOType.Read, e.getMessage());
        }
    }

    @Override
    public ICourse.CourseType getCourseType() {
        return ICourse.CourseType.Team;
    }

    @Override
    public GradeTeam computeGrade(ORTPlayer[] players) {
        GradeSP[] grades = new GradeClassic[players.length];
        boolean isQualified = true;
        for (int i = 0; i < players.length; ++i) {
            grades[i] = super.computeGrade2(new ORTPlayer[]{players[i]});
            if (grades[i] == null) {
                return null;
            }
            if (grades[i].isQualified()) continue;
            isQualified = false;
        }
        int correctCPNumber = 0;
        HashSet<Integer> previousCP = new HashSet<Integer>();
        for (int i = 0; i < players.length; ++i) {
            List<Integer> l = players[i].getCPHistory();
            for (int j = 0; j < l.size(); ++j) {
                if (previousCP.contains(l.get(j)) || this.CPList2.indexOf(l.get(j)) == -1) continue;
                ++correctCPNumber;
                previousCP.add(l.get(j));
            }
        }
        if (correctCPNumber < this.CPList2.size()) {
            isQualified = false;
        }
        return new GradeTeam(grades, this.matchName, isQualified);
    }

    @Override
    public boolean displayTime() {
        return false;
    }
}

