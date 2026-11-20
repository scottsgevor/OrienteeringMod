/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public interface IGrade
extends Serializable {
    public String getMatchName();

    public String getGroupName();

    public void printGradeStripToScreen(EntityPlayer var1);

    public void exportGradeStripToFile(String var1) throws ORTFileException;

    public void writeGrade(FileWriter var1) throws IOException;

    public String getGeneralGradeString();

    public String getCPDetailString();

    public boolean isQualified();

    public long getTotalTime();

    @Nonnull
    public static IGrade readFromFile(String matchName, String playerName) throws ORTFileException {
        IGrade grade = null;
        String fileName = Util.getGradeDataPath(matchName) + playerName;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
            grade = (IGrade)in.readObject();
            in.close();
        }
        catch (Throwable e) {
            LogManager.getLogger().info(e.getMessage());
            throw new ORTFileException(ORTFileException.FileType.Grade, ORTFileException.IOType.Read, null);
        }
        return grade;
    }

    public static void writeToFile(String matchName, IGrade grade) throws ORTFileException {
        String fileName = Util.getGradeDataPath(matchName) + grade.getGroupName();
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(grade);
            out.flush();
            out.close();
        }
        catch (Throwable e) {
            LogManager.getLogger().info(e.getMessage());
            throw new ORTFileException(ORTFileException.FileType.Grade, ORTFileException.IOType.Write, null);
        }
    }

    public void writeToFile(String var1) throws ORTFileException;
}

