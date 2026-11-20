/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.respawner.orienteeringmod.ORTException;
import net.respawner.orienteeringmod.Util;

class ORTFileException
extends ORTException {
    private static final long serialVersionUID = 1L;

    public ORTFileException(String msg) {
        super(msg);
    }

    public ORTFileException(FileType file, IOType type, String specificMsg, List<String> names) {
        this(ORTFileException.generateErrorString(file, type, names) + "Message detail: " + specificMsg);
    }

    public ORTFileException(FileType file, IOType type, String specificMsg) {
        this(file, type, specificMsg, null);
    }

    public ORTFileException(EntityPlayer player, String msg) {
        super(msg);
        Util.sendMessage(player, msg);
    }

    public static String generateErrorString(FileType file, IOType type, List<String> names) {
        String ioType = type == IOType.Read ? "read" : "write";
        switch (file) {
            case Matchseed: {
                return "File Error: cannot " + ioType + " matchseed.json. ";
            }
            case Match: {
                return "File Error: cannot " + ioType + " the match data. ";
            }
            case Course: {
                return "File Error: cannot " + ioType + " the course data" + ORTFileException.stringArrayToString(names);
            }
            case Player: {
                return "File Error: cannot " + ioType + " the player data" + ORTFileException.stringArrayToString(names);
            }
            case Grade: {
                return "File Error: cannot " + ioType + " the grade data" + ORTFileException.stringArrayToString(names);
            }
            case Roster: {
                return "File Error: cannot " + ioType + " the player roster. ";
            }
            case Stat: {
                return "File Error: cannot " + ioType + " the stat file. ";
            }
            case Cartography: {
                return "File Error: cannot " + ioType + " cartography.json. ";
            }
        }
        return "";
    }

    private static String stringArrayToString(List<String> names) {
        if (names == null) {
            return ". ";
        }
        String str = "";
        for (int i = 0; i < names.size(); ++i) {
            if (i >= 5) {
                str = str + " (" + (names.size() - 5) + " more).";
                break;
            }
            str = str + names.get(i) + (i < names.size() - 1 ? ", " : ". ");
        }
        return str;
    }

    public static enum IOType {
        Read,
        Write;

    }

    public static enum FileType {
        Matchseed,
        Match,
        Course,
        Player,
        Roster,
        Grade,
        Stat,
        Cartography;

    }
}

