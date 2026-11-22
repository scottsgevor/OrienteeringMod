/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public class ORTPlayer
implements Serializable {
    private static final long serialVersionUID = 3L;
    public static final int BACK_TARGET_START = -2;
    public static final int BACK_TARGET_SPAWN = -3;
    public final String playerName;
    private List<Integer> CPHistory = new ArrayList<Integer>();
    private List<Long> timeHistory = new ArrayList<Long>();
    private long beginningTime = 0L;
    private long lastCPTime = 0L;
    private PlayerStatus playerStatus = PlayerStatus.Unstarted;
    private int deathCount = 0;
    public boolean drawCompass = false;
    public boolean nightVision = false;
    public boolean displayTime = false;
    public long lastShovelUsedTime = 0L;
    private boolean hasLastCPPosition = false;
    private double lastCPX;
    private double lastCPY;
    private double lastCPZ;
    private boolean hasStartPosition = false;
    private double startX;
    private double startY;
    private double startZ;
    private long extraTimePenalty = 0L;
    private List<BackRecord> backRecords = new ArrayList<BackRecord>();

    public long getBeginningTime() {
        return this.beginningTime;
    }

    public PlayerStatus getPlayerStatus() {
        return this.playerStatus;
    }

    public boolean isInMatchProgress() {
        return this.playerStatus == PlayerStatus.InProgress;
    }

    public boolean isEnded() {
        return this.playerStatus == PlayerStatus.Finished || this.playerStatus == PlayerStatus.Aborted;
    }

    public boolean isFinished() {
        return this.playerStatus == PlayerStatus.Finished;
    }

    public boolean isStarted() {
        return this.playerStatus != PlayerStatus.Unstarted;
    }

    public void addDeath() {
        ++this.deathCount;
    }

    public List<Integer> getCPHistory() {
        return this.CPHistory;
    }

    public List<Long> getTimeHistory() {
        return this.timeHistory;
    }

    public long getLastCPTime() {
        return this.lastCPTime;
    }

    public int getDeathCount() {
        return this.deathCount;
    }

    public int getLastCPCode() {
        return this.CPHistory.isEmpty() ? Integer.MIN_VALUE
                : this.CPHistory.get(this.CPHistory.size() - 1).intValue();
    }

    public List<BackRecord> getBackRecords() {
        if (this.backRecords == null) {
            this.backRecords = new ArrayList<BackRecord>();
        }
        return this.backRecords;
    }

    public void addBackRecord(long matchTime, int targetCode, double targetX, double targetY,
                              double targetZ, double fromX, double fromY, double fromZ) {
        this.getBackRecords().add(new BackRecord(matchTime, targetCode, targetX, targetY, targetZ,
                fromX, fromY, fromZ));
    }

    public boolean hasLastCPPosition() {
        return this.hasLastCPPosition;
    }

    public double[] getLastCPPosition() {
        return this.hasLastCPPosition ? new double[]{this.lastCPX, this.lastCPY, this.lastCPZ} : null;
    }

    public boolean hasStartPosition() {
        return this.hasStartPosition;
    }

    public double[] getStartPosition() {
        return this.hasStartPosition ? new double[]{this.startX, this.startY, this.startZ} : null;
    }

    public void recordStartPosition(double x, double y, double z) {
        this.hasStartPosition = true;
        this.startX = x;
        this.startY = y;
        this.startZ = z;
    }

    public long getExtraTimePenalty() {
        return this.extraTimePenalty;
    }

    public void addTimePenalty(long penalty) {
        if (penalty > 0L) {
            this.extraTimePenalty += penalty;
        }
    }

    private ORTPlayer(String name) {
        this.playerName = name;
    }

    @Nonnull
    public static ORTPlayer createEmptyPlayer(String playerName) {
        return new ORTPlayer(playerName);
    }

    public String getLastCP() {
        if (this.CPHistory.isEmpty()) {
            return "none";
        }
        return "" + this.CPHistory.get(this.CPHistory.size() - 1);
    }

    public boolean start(long time) {
        this.playerStatus = PlayerStatus.InProgress;
        this.deathCount = 0;
        this.CPHistory = new ArrayList<Integer>();
        this.timeHistory = new ArrayList<Long>();
        this.beginningTime = time;
        this.lastCPTime = time;
        this.hasLastCPPosition = false;
        this.hasStartPosition = false;
        this.extraTimePenalty = 0L;
        this.backRecords = new ArrayList<BackRecord>();
        return true;
    }

    public void checkpoint(int code, long time, double x, double y, double z) {
        this.hasLastCPPosition = true;
        this.lastCPX = x;
        this.lastCPY = y;
        this.lastCPZ = z;
        this.checkpoint(code, time);
    }

    public void checkpoint(int code, long time) {
        int loc;
        if (this.CPHistory.size() > 0 && this.CPHistory.get(loc = this.CPHistory.size() - 1) == code && time < this.lastCPTime + 20L) {
            this.timeHistory.set(loc, this.timeHistory.get(loc) + (time - this.lastCPTime) / 2L);
            this.lastCPTime = time;
            return;
        }
        this.CPHistory.add(code);
        this.timeHistory.add((time - this.lastCPTime) / 2L);
        this.lastCPTime = time;
    }

    public boolean end(long time) {
        this.playerStatus = PlayerStatus.Finished;
        this.CPHistory.add(-1);
        this.timeHistory.add((time - this.lastCPTime) / 2L);
        this.lastCPTime = time;
        return true;
    }

    public void abort() {
        this.playerStatus = PlayerStatus.Aborted;
    }

    public static ORTPlayer readFromFile(String matchName, String playerName, boolean doCreate) throws ORTFileException {
        ORTPlayer player = null;
        String fileName = Util.getPlayerDataPath(matchName) + playerName;
        File path = new File(fileName);
        if (!path.exists()) {
            if (doCreate) {
                player = ORTPlayer.createEmptyPlayer(playerName);
                player.writeToFile(matchName);
                return player;
            }
            throw new ORTFileException("The player data does not exist.");
        }
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
            player = (ORTPlayer)in.readObject();
            in.close();
        }
        catch (Throwable e) {
            LogManager.getLogger().error(e.toString());
            throw new ORTFileException(ORTFileException.FileType.Player, ORTFileException.IOType.Read, e.getMessage());
        }
        return player;
    }

    public static void writeToFile(String matchName, ORTPlayer player) throws ORTFileException {
        String fileName = Util.getPlayerDataPath(matchName) + player.playerName;
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(player);
            out.flush();
            out.close();
        }
        catch (Throwable e) {
            LogManager.getLogger().error(e.toString());
            throw new ORTFileException(ORTFileException.FileType.Player, ORTFileException.IOType.Write, e.getMessage());
        }
    }

    public void writeToFile(String matchName) throws ORTFileException {
        ORTPlayer.writeToFile(matchName, this);
    }

    public static enum PlayerStatus {
        Unstarted,
        InProgress,
        Finished,
        Aborted;

    }

    public static class BackRecord
    implements Serializable {
        private static final long serialVersionUID = 1L;
        public final long matchTime;
        public final int targetCode;
        public final double targetX;
        public final double targetY;
        public final double targetZ;
        public final double fromX;
        public final double fromY;
        public final double fromZ;

        public BackRecord(long matchTime, int targetCode, double targetX, double targetY,
                          double targetZ, double fromX, double fromY, double fromZ) {
            this.matchTime = matchTime;
            this.targetCode = targetCode;
            this.targetX = targetX;
            this.targetY = targetY;
            this.targetZ = targetZ;
            this.fromX = fromX;
            this.fromY = fromY;
            this.fromZ = fromZ;
        }

        public String getTargetName() {
            if (this.targetCode >= 0) {
                return "CP " + this.targetCode;
            }
            return this.targetCode == ORTPlayer.BACK_TARGET_START ? "START" : "SPAWN";
        }
    }
}

