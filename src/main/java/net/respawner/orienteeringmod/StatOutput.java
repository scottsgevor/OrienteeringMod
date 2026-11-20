/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.io.File;
import java.io.FileWriter;
import net.respawner.orienteeringmod.IGrade;
import net.respawner.orienteeringmod.IPlayerGroup;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public class StatOutput {
    public static void output(String matchName) throws ORTFileException {
        if (!ORTWorld.hasMatchServer()) {
            return;
        }
        ORTWorld instance = ORTWorld.getMatchInstanceServer();
        try {
            File file = new File(Util.getBasicDataPath(matchName) + "0_stat.txt");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileWriter out = new FileWriter(file);
            File fileDetail = new File(Util.getBasicDataPath(matchName) + "1_statdetail.txt");
            if (fileDetail.exists()) {
                fileDetail.delete();
            }
            fileDetail.createNewFile();
            FileWriter outDetail = new FileWriter(fileDetail);
            for (String p : instance.players.keySet()) {
                IGrade g;
                IPlayerGroup group;
                if (!instance.match.isRosterContainsPlayer(p) || instance.grades.containsKey((group = instance.match.getGroupByPlayer(p, false)).getGroupName()) || (g = group.computeGroupGrade(instance.players, instance.courses)) == null) continue;
                instance.grades.put(group.getGroupName(), g);
            }
            for (IGrade grade : instance.grades.values()) {
                out.write(grade.getGeneralGradeString() + "\r\n");
                outDetail.write(grade.getCPDetailString() + "\r\n");
            }
            out.flush();
            out.close();
            outDetail.flush();
            outDetail.close();
            LogManager.getLogger().info("Stat export completed.");
        }
        catch (Throwable e) {
            e.printStackTrace();
            throw new ORTFileException(ORTFileException.FileType.Stat, ORTFileException.IOType.Write, null);
        }
    }
}

