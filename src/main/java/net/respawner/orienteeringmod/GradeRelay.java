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
import net.respawner.orienteeringmod.PlayerGroupRelay;
import net.respawner.orienteeringmod.Util;

public class GradeRelay
extends GradeMP {
    private static final long serialVersionUID = 4L;
    private final int qualifiedNumber;
    private final long groupTotalTime;

    public GradeRelay(GradeSP[] grades, String matchName, String groupName) {
        super(grades, matchName, groupName);
        int q = 0;
        long t = 0L;
        for (int i = 0; i < grades.length; ++i) {
            if (grades[i] == null) continue;
            t += grades[i].getTotalTime();
            if (!grades[i].isQualified()) continue;
            ++q;
        }
        this.qualifiedNumber = q;
        this.groupTotalTime = t;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void printGradeStripToScreen(EntityPlayer player) {
        int turn;
        Util.sendMessage(player, "Relay match info:");
        Util.sendMessage(player, "Qualified number: " + this.qualifiedNumber + ". Total time: " + Util.timeTranslation(this.groupTotalTime));
        Util.sendMessage(player, "------------------------------");
        try {
            turn = Integer.parseInt(PlayerGroupRelay.getTurnByPlayer(player.func_70005_c_()));
            if (this.grades[turn - 1] != null) {
                // empty if block
            }
        }
        catch (Throwable e) {
            Util.sendMessage(player, "Error: invalid player name format.");
            return;
        }
        if (this.grades[turn - 1] == null) {
            Util.sendMessage(player, "Your individual grade is currently not visible.");
            return;
        }
        this.grades[turn - 1].printGradeStripToScreen(player);
    }

    @Override
    public void exportGradeStripToFile(String playerName) throws ORTFileException {
        String filePath = Util.getRootPath() + this.matchName + "_" + playerName + ".txt";
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
        catch (Throwable e) {
            e.printStackTrace();
            throw new ORTFileException("cannot print the grade strip.");
        }
    }

    @Override
    public void writeGrade(FileWriter file) throws IOException {
        int i;
        file.write("==============================\r\n");
        file.write("Relay match info:\r\n");
        for (i = 0; i < this.grades.length; ++i) {
            if (this.grades[i] != null) {
                file.write(this.grades[i].getGroupName() + ": \tTime=" + Util.timeTranslation(this.grades[i].getTotalTime()) + ", \tQualified: " + (this.grades[i].isQualified() ? "T" : "F") + "\r\n");
                continue;
            }
            file.write(PlayerGroupRelay.composePlayerName(PlayerGroupRelay.getGroupNameByPlayer(this.groupName), i) + ": (grade not available.)\r\n");
        }
        file.write("Qualified player: " + this.qualifiedNumber + "\r\n");
        file.write("Total Time: " + Util.timeTranslation(this.groupTotalTime) + "\r\n\r\n");
        file.write("==============================\r\n");
        file.write("\r\n");
        file.write("Below is the grade strip of all players:\r\n");
        file.write("\r\n");
        for (i = 0; i < this.grades.length; ++i) {
            if (this.grades[i] == null) {
                file.write("==============================\r\n");
                file.write(PlayerGroupRelay.composePlayerName(PlayerGroupRelay.getGroupNameByPlayer(this.groupName), i) + ":\t(grade not available.)\r\n");
            } else {
                this.grades[i].writeGrade(file);
            }
            file.write("\r\n==============================\r\n");
        }
    }

    @Override
    public String getGeneralGradeString() {
        return this.getGroupName() + "\t" + this.qualifiedNumber + "\t" + Util.timeTranslation(this.groupTotalTime);
    }

    @Override
    @Deprecated
    public String getCPDetailString() {
        return "";
    }

    @Override
    public long getTotalTime() {
        return this.groupTotalTime;
    }

    @Override
    public String getGroupName() {
        return PlayerGroupRelay.getGroupNameByPlayer(this.grades[0].getGroupName());
    }

    @Override
    public boolean isQualified() {
        return this.qualifiedNumber == this.grades.length;
    }
}

