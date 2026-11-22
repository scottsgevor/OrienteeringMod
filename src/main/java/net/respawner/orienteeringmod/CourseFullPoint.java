/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import net.respawner.orienteeringmod.CourseBasic;
import net.respawner.orienteeringmod.GradeFullPoint;
import net.respawner.orienteeringmod.ICourse;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTPlayer;

public class CourseFullPoint
extends CourseBasic {
    private static final long serialVersionUID = 4L;

    public CourseFullPoint(JsonObject jsonCourse, String worldName, int deathPenalty) throws ORTFileException {
        super(jsonCourse, worldName, deathPenalty);
    }

    @Override
    public ICourse.CourseType getCourseType() {
        return ICourse.CourseType.FullPoint;
    }

    @Override
    public GradeFullPoint computeGrade(ORTPlayer[] players) {
        long totalTime;
        long totalTimeWithPenalty;
        ORTPlayer player = players[0];
        if (player == null) {
            return null;
        }
        if (!player.isEnded()) {
            return null;
        }
        boolean isQualified = true;
        int correctCPNumber = 0;
        ArrayList<Boolean> correctnessList = new ArrayList<Boolean>();
        HashSet<Integer> previousCP = new HashSet<Integer>();
        for (int i = 0; i < player.getCPHistory().size(); ++i) {
            if (player.getCPHistory().get(i) == -1) {
                if (correctCPNumber >= this.CPList.size()) {
                    correctnessList.add(true);
                    ++correctCPNumber;
                    break;
                }
                correctnessList.add(false);
                break;
            }
            if (previousCP.contains(player.getCPHistory().get(i))) {
                correctnessList.add(false);
                continue;
            }
            if (this.CPList.indexOf((int)player.getCPHistory().get(i)) == -1) {
                correctnessList.add(false);
            } else {
                correctnessList.add(true);
                ++correctCPNumber;
                previousCP.add(player.getCPHistory().get(i));
            }
            if (correctCPNumber > this.CPList.size()) break;
        }
        if ((totalTimeWithPenalty = (totalTime = (player.getLastCPTime() - player.getBeginningTime()) / 2L) + (long)(player.getDeathCount() * this.deathPenalty * 600) + player.getExtraTimePenalty()) > (long)(this.validTime * 600)) {
            isQualified = false;
        }
        if (correctCPNumber < this.CPList.size()) {
            isQualified = false;
        }
        if (!player.isFinished()) {
            isQualified = false;
        }
        return new GradeFullPoint(this.matchName, this.courseName, player, correctnessList, correctCPNumber, totalTime, totalTimeWithPenalty, isQualified);
    }

    @Override
    public boolean displayTime() {
        return false;
    }
}

