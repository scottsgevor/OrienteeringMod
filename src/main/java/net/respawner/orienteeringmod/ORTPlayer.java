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
        return true;
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
}

