/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import net.respawner.orienteeringmod.GradeSP;
import net.respawner.orienteeringmod.ORTPlayer;

public class GradeFullPoint
extends GradeSP {
    private static final long serialVersionUID = 1L;

    public GradeFullPoint(String worldName, String courseName, ORTPlayer player, List<Boolean> correctnessList, int correctCPNumber, long totalTime, long totalTimeWithPenalty, boolean isQualified) {
        super(worldName, courseName, player, correctnessList, correctCPNumber, totalTime, totalTimeWithPenalty, isQualified);
    }

    @Override
    protected void writeHeader2(FileWriter file) throws IOException {
        file.write("------------------------------\r\n");
        file.write("\r\n");
    }

    @Override
    protected String type() {
        return "Full point";
    }
}

