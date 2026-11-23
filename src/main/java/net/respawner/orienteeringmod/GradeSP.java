/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.entity.player.EntityPlayer;
import net.respawner.orienteeringmod.IGrade;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTPlayer;
import net.respawner.orienteeringmod.Util;

public abstract class GradeSP
implements IGrade {
    protected static final long serialVersionUID = 4L;
    protected final String matchName;
    protected final String playerName;
    protected final String courseName;
    protected final ORTPlayer.PlayerStatus playerStatus;
    protected final int deathCount;
    protected final List<Integer> CPHistory;
    protected final List<Boolean> correctnessList;
    protected final List<Long> timeHistory;
    protected final List<ORTPlayer.BackRecord> backRecords;
    protected final int correctCPNumber;
    protected final long totalTime;
    protected final long totalTimeWithPenalty;
    protected final boolean isQualified;

    public GradeSP(String matchName, String courseName, ORTPlayer player, List<Boolean> correctnessList, int correctCPNumber, long totalTime, long totalTimeWithPenalty, boolean isQualified) {
        this.matchName = matchName;
        this.playerName = player.playerName;
        this.courseName = courseName;
        this.playerStatus = player.getPlayerStatus();
        this.deathCount = player.getDeathCount();
        this.CPHistory = new ArrayList<Integer>(player.getCPHistory());
        this.correctnessList = correctnessList;
        this.timeHistory = new ArrayList<Long>(player.getTimeHistory());
        this.backRecords = new ArrayList<ORTPlayer.BackRecord>(player.getBackRecords());
        this.correctCPNumber = correctCPNumber;
        this.totalTime = totalTime;
        this.totalTimeWithPenalty = totalTimeWithPenalty;
        this.isQualified = isQualified;
    }

    @Override
    public String getMatchName() {
        return this.matchName;
    }

    @Override
    public String getGroupName() {
        return this.playerName;
    }

    @Override
    public boolean isQualified() {
        return this.isQualified;
    }

    @Override
    public long getTotalTime() {
        return this.totalTimeWithPenalty;
    }

    @Override
    public void printGradeStripToScreen(EntityPlayer player) {
        Util.sendMessage(player, "# " + this.playerName + " in the course " + this.courseName + ".");
        Util.sendMessage(player, "Your match status is: " + (Object)((Object)this.playerStatus));
        Util.sendMessage(player, "Qualified: " + (this.isQualified ? "Yes." : "No."));
        Util.sendMessage(player, "Your total time: " + Util.timeTranslation(this.totalTimeWithPenalty) + " (death: " + this.deathCount + ")");
        Util.sendMessage(player, "Back uses: " + this.backRecordsView().size() + ".");
        Util.sendMessage(player, "For more details, see the file \".minecraft\\.orienteering\\" + this.matchName + "_" + this.playerName + ".txt\"");
    }

    @Override
    public void exportGradeStripToFile(String playerName) throws ORTFileException {
        String filePath = Util.getRootPath() + this.matchName + "_" + this.playerName + "_" + this.courseName + ".txt";
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
        this.writeHeader1(file);
        this.writeHeader2(file);
        this.writeDetailPart(file);
    }

    protected void writeHeader1(FileWriter file) throws IOException {
        file.write("------------------------------\r\n");
        file.write("Player: " + this.playerName + "\r\n");
        file.write("World: " + this.matchName + "\r\n");
        file.write("Course: " + this.courseName + "\r\n");
        file.write("The status of the player is: " + (Object)((Object)this.playerStatus) + "\r\n");
        file.write("The player is " + (this.isQualified ? "" : "not ") + "qualified.\r\n");
        file.write("The game time is " + Util.timeTranslation(this.totalTime) + "\r\n");
        file.write("Death count: " + this.deathCount + "\r\n");
        file.write("Back count: " + this.backRecordsView().size() + "\r\n");
        file.write("The total time is " + Util.timeTranslation(this.totalTimeWithPenalty) + "\r\n");
    }

    protected abstract void writeHeader2(FileWriter var1) throws IOException;

    protected void writeDetailPart(FileWriter file) throws IOException {
        file.write("Here is the checkpoint list of the player:\r\n");
        for (int i = 0; i < this.CPHistory.size(); ++i) {
            String Line = "";
            Line = Line + (i < 9 ? "  " : (i < 99 ? " " : ""));
            Line = Line + (i + 1);
            Line = Line + "   ";
            Line = Line + (this.CPHistory.get(i) == -1 ? " EN" : (this.CPHistory.get(i) < 100 ? " " + this.CPHistory.get(i) : (Serializable)this.CPHistory.get(i)));
            Line = Line + "   ";
            Line = Line + (this.correctnessList.get(i) != false ? "T" : "F");
            Line = Line + "   ";
            Line = Line + Util.timeTranslation(this.timeHistory.get(i));
            file.write(Line + "\r\n");
        }
        if (!this.backRecordsView().isEmpty()) {
            file.write("\r\nBack records:\r\n");
            List<ORTPlayer.BackRecord> records = this.backRecordsView();
            for (int i = 0; i < records.size(); ++i) {
                ORTPlayer.BackRecord record = records.get(i);
                file.write("  " + (i + 1) + "   " + Util.timeTranslation(record.matchTime)
                        + "   " + record.getTargetName()
                        + "   target (" + GradeSP.formatPosition(record.targetX, record.targetY, record.targetZ) + ")"
                        + "   from (" + GradeSP.formatPosition(record.fromX, record.fromY, record.fromZ) + ")\r\n");
            }
        }
    }

    private static String formatPosition(double x, double y, double z) {
        return String.format(Locale.ROOT, "%.1f, %.1f, %.1f", x, y, z);
    }

    private List<ORTPlayer.BackRecord> backRecordsView() {
        return this.backRecords == null ? Collections.<ORTPlayer.BackRecord>emptyList() : this.backRecords;
    }

    @Override
    public String getGeneralGradeString() {
        return this.matchName + "\t" + this.type() + "\t" + this.courseName + "\t" + this.playerName + "\t" + Util.timeTranslation(this.totalTime) + "\t" + this.deathCount + "\t" + this.backRecordsView().size() + "\t" + Util.timeTranslation(this.totalTimeWithPenalty) + "\t" + (this.isQualified ? "T" : "F");
    }

    protected abstract String type();

    @Override
    public String getCPDetailString() {
        String str = "";
        str = str + this.matchName + "\t" + this.courseName + "\t" + this.playerName + "\t";
        for (int j = 0; j < this.CPHistory.size(); ++j) {
            str = str + (this.CPHistory.get(j) == -1 ? "EN" : (Serializable)this.CPHistory.get(j)) + "\t" + Util.timeTranslation(this.timeHistory.get(j)) + "\t";
        }
        str = str + "\r\n";
        return str;
    }

    @Override
    public void writeToFile(String matchName) throws ORTFileException {
        IGrade.writeToFile(matchName, this);
    }
}

