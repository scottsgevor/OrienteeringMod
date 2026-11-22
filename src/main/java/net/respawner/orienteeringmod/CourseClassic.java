/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.respawner.orienteeringmod.CourseBasic;
import net.respawner.orienteeringmod.GradeClassic;
import net.respawner.orienteeringmod.ICourse;
import net.respawner.orienteeringmod.IGrade;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTPlayer;

public class CourseClassic
extends CourseBasic {
    private static final long serialVersionUID = 4L;

    public CourseClassic(JsonObject jsonCourse, String matchName, int deathPenalty) throws ORTFileException {
        super(jsonCourse, matchName, deathPenalty);
    }

    @Override
    public ICourse.CourseType getCourseType() {
        return ICourse.CourseType.Classic;
    }

    @Override
    @Nullable
    public IGrade computeGrade(ORTPlayer[] players) {
        return this.computeGrade2(players);
    }

    @Nullable
    protected GradeClassic computeGrade2(ORTPlayer[] players) {
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
        for (int i = 0; i < player.getCPHistory().size(); ++i) {
            if (player.getCPHistory().get(i) == -1) {
                if (correctCPNumber == this.CPList.size()) {
                    correctnessList.add(true);
                    ++correctCPNumber;
                    break;
                }
                correctnessList.add(false);
                break;
            }
            if (correctCPNumber <= this.CPList.size() - 1 && player.getCPHistory().get(i).equals(this.CPList.get(correctCPNumber))) {
                correctnessList.add(true);
                ++correctCPNumber;
            } else {
                correctnessList.add(false);
            }
            if (correctCPNumber > this.CPList.size()) break;
        }
        if ((totalTimeWithPenalty = (totalTime = (player.getLastCPTime() - player.getBeginningTime()) / 2L) + (long)(player.getDeathCount() * this.deathPenalty * 600) + player.getExtraTimePenalty()) > (long)(this.validTime * 600)) {
            isQualified = false;
        }
        if (correctCPNumber < this.CPList.size() + 1) {
            isQualified = false;
        }
        if (!player.isFinished()) {
            isQualified = false;
        }
        return new GradeClassic(this.matchName, this.courseName, player, correctnessList, correctCPNumber, totalTime, totalTimeWithPenalty, isQualified);
    }

    @Override
    public boolean displayTime() {
        return false;
    }
}

