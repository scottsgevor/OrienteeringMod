/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.respawner.orienteeringmod.GradeMP;
import net.respawner.orienteeringmod.GradeSP;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.Util;

public class GradeTeam
extends GradeMP {
    private static final long serialVersionUID = 4L;
    private final long groupTotalTime;
    private boolean isTeamQualified;

    public GradeTeam(GradeSP[] grades, String matchName, boolean isTeamQualified) {
        super(grades, matchName, "");
        long time = 0L;
        for (int i = 0; i < grades.length; ++i) {
            time = Math.max(time, grades[i].getTotalTime());
        }
        this.groupTotalTime = time;
        this.isTeamQualified = isTeamQualified;
    }

    public GradeTeam setGroupName(String name) {
        this.groupName = name;
        return this;
    }

    @Override
    public void printGradeStripToScreen(EntityPlayer player) {
        int index = -1;
        for (int i = 0; i < this.grades.length; ++i) {
            if (!this.grades[i].getGroupName().equals(player.func_70005_c_())) continue;
            index = i;
        }
        if (index == -1) {
            throw new IllegalArgumentException("player.playerName");
        }
        Util.sendMessage(player, "Team match info:");
        Util.sendMessage(player, "Qualified: " + (this.isTeamQualified ? "Yes" : "No") + ". Total time: " + Util.timeTranslation(this.groupTotalTime));
        Util.sendMessage(player, "------------------------------");
        if (this.grades[index] == null) {
            Util.sendMessage(player, "Your individual grade is currently not visible.");
        } else {
            this.grades[index].printGradeStripToScreen(player);
        }
    }

    @Override
    public void exportGradeStripToFile(String playerName) throws ORTFileException {
        String filePath = Util.getRootPath() + this.matchName + "_" + playerName + "_teammatch.txt";
        File path = new File(filePath);
        if (path.exists()) {
            path.delete();
        }
        try {
            path.createNewFile();
            FileWriter file = new FileWriter(filePath);
            this.writeGrade(file);
            file.flush();
            file.close();
        }
        catch (IOException e) {
            throw new ORTFileException("cannot print the grade strip.");
        }
    }

    @Override
    public void writeGrade(FileWriter file) throws IOException {
        file.write("==============================\r\n");
        file.write("Team match info:\r\n");
        file.write("Qualified: " + (this.isTeamQualified ? "yes" : "no") + "\r\n");
        file.write("Total Time: " + Util.timeTranslation(this.groupTotalTime) + "\r\n\r\n");
        file.write("==============================\r\n");
        file.write("\r\n");
        file.write("Below is the grade strip of all players:\r\n");
        file.write("\r\n");
        for (int i = 0; i < this.grades.length; ++i) {
            this.grades[i].writeGrade(file);
            file.write("\r\n==============================\r\n");
        }
    }

    @Override
    public String getGeneralGradeString() {
        return this.groupName + "\t" + (this.isTeamQualified ? "T" : "F") + "\t" + Util.timeTranslation(this.groupTotalTime);
    }

    @Override
    @Deprecated
    public String getCPDetailString() {
        return "";
    }

    @Override
    public boolean isQualified() {
        return this.isTeamQualified;
    }

    @Override
    public long getTotalTime() {
        return this.groupTotalTime;
    }
}

