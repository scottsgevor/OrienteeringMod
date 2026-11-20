/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.respawner.orienteeringmod.CourseBasic;
import net.respawner.orienteeringmod.GradeScore;
import net.respawner.orienteeringmod.ICourse;
import net.respawner.orienteeringmod.IllegalSourceException;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTPlayer;
import org.apache.logging.log4j.LogManager;

public class CourseScore
extends CourseBasic {
    private static final long serialVersionUID = 4L;
    private final List<Integer> scoreList;
    private final boolean defaultScoreSetting;
    private final int overtimePermit;
    private final int overtimePenalty;

    public CourseScore(JsonObject jsonCourse, String worldName, int deathPenalty) throws ORTFileException {
        super(jsonCourse, worldName, deathPenalty);
        try {
            this.scoreList = new ArrayList<Integer>();
            this.defaultScoreSetting = jsonCourse.get("defaultscoresetting").getAsBoolean();
            if (this.defaultScoreSetting) {
                for (int k = 0; k < this.CPList.size(); ++k) {
                    this.scoreList.add((Integer)this.CPList.get(k) / 10);
                }
            } else {
                JsonArray jsonScoreList = jsonCourse.get("scorelist").getAsJsonArray();
                if (jsonScoreList.size() != this.CPList.size()) {
                    throw new IllegalSourceException(ORTFileException.FileType.Matchseed, "the score list has different size from the checkpoint list.");
                }
                for (int k = 0; k < this.CPList.size(); ++k) {
                    this.scoreList.add(jsonScoreList.get(k).getAsInt());
                }
            }
            this.overtimePermit = jsonCourse.get("overtimepermit").getAsInt();
            this.overtimePenalty = jsonCourse.get("overtimepenalty").getAsInt();
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
        return ICourse.CourseType.Score;
    }

    @Override
    public GradeScore computeGrade(ORTPlayer[] players) {
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
        HashSet<Integer> scoredCP = new HashSet<Integer>();
        int score = 0;
        for (int i = 0; i < player.getCPHistory().size(); ++i) {
            if (player.getCPHistory().get(i) == -1) {
                correctnessList.add(true);
                ++correctCPNumber;
                break;
            }
            boolean isCorrect = false;
            for (int j = 0; j < this.CPList.size(); ++j) {
                if (!player.getCPHistory().get(i).equals(this.CPList.get(j))) continue;
                if (scoredCP.contains(this.CPList.get(j))) break;
                scoredCP.add(this.CPList.get(j));
                score += this.scoreList.get(j).intValue();
                correctnessList.add(true);
                ++correctCPNumber;
                isCorrect = true;
                break;
            }
            if (!isCorrect) {
                correctnessList.add(false);
            }
            if (correctCPNumber > this.CPList.size()) break;
        }
        if ((totalTimeWithPenalty = (totalTime = (player.getLastCPTime() - player.getBeginningTime()) / 2L) + (long)(player.getDeathCount() * this.deathPenalty * 600)) > (long)((this.validTime + this.overtimePermit) * 600)) {
            isQualified = false;
        }
        if (totalTimeWithPenalty > (long)(this.validTime * 600)) {
            score = (int)((long)score - ((totalTimeWithPenalty + 599L) / 600L - (long)this.validTime) * (long)this.overtimePenalty);
        }
        if (!player.isFinished()) {
            isQualified = false;
        }
        return new GradeScore(this.matchName, this.courseName, player, correctnessList, correctCPNumber, totalTime, totalTimeWithPenalty, isQualified, score);
    }

    @Override
    public boolean displayTime() {
        return true;
    }
}

