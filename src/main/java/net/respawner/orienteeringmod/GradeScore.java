/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import net.respawner.orienteeringmod.GradeSP;
import net.respawner.orienteeringmod.ORTPlayer;

public class GradeScore
extends GradeSP {
    private static final long serialVersionUID = 4L;
    private final int score;

    public GradeScore(String worldName, String courseName, ORTPlayer player, List<Boolean> correctnessList, int correctCPNumber, long totalTime, long totalTimeWithPenalty, boolean isQualified, int score) {
        super(worldName, courseName, player, correctnessList, correctCPNumber, totalTime, totalTimeWithPenalty, isQualified);
        this.score = score;
    }

    @Override
    protected void writeHeader2(FileWriter file) throws IOException {
        file.write("Score: " + this.score + "\r\n");
        file.write("------------------------------\r\n");
        file.write("\r\n");
    }

    @Override
    public String getGeneralGradeString() {
        return super.getGeneralGradeString() + "\t" + this.score;
    }

    @Override
    protected String type() {
        return "Score";
    }
}

