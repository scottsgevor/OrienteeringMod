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
import java.util.Objects;
import javax.annotation.Nullable;
import net.respawner.orienteeringmod.IGrade;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTPlayer;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public interface ICourse
extends Serializable {
    public String getName();

    public String getMatchName();

    public CourseType getCourseType();

    public int[] getStartPos();

    public boolean needTeleport();

    @Nullable
    public String getWelcomeString();

    public boolean displayTime();

    @Nullable
    public IGrade computeGrade(ORTPlayer[] var1);

    public static ICourse readFromFile(String matchName, String courseName) throws ORTFileException {
        Objects.requireNonNull(matchName);
        Objects.requireNonNull(courseName);
        ICourse course = null;
        String fileName = ICourse.getCourseDataPath(matchName) + courseName;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
            course = (ICourse)in.readObject();
            in.close();
        }
        catch (Throwable e) {
            LogManager.getLogger().error(e.toString());
            throw new ORTFileException(ORTFileException.FileType.Course, ORTFileException.IOType.Read, e.getMessage());
        }
        return course;
    }

    public static void writeToFile(String matchName, ICourse course) throws ORTFileException {
        Objects.requireNonNull(matchName);
        Objects.requireNonNull(course);
        String fileName = ICourse.getCourseDataPath(matchName) + course.getName();
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(course);
            out.flush();
            out.close();
        }
        catch (Throwable e) {
            LogManager.getLogger().error(e.toString());
            throw new ORTFileException(ORTFileException.FileType.Course, ORTFileException.IOType.Read, e.getMessage());
        }
    }

    public static String getCourseDataPath(String matchName) {
        return Util.getBasicDataPath(matchName) + "courses" + File.separator;
    }

    public void writeToFile(String var1) throws ORTFileException;

    public static enum CourseType {
        Classic,
        Score,
        FullPoint,
        Team;


        public String toString() {
            switch (this) {
                case Classic: {
                    return "Classic";
                }
                case Score: {
                    return "Score";
                }
                case FullPoint: {
                    return "Full point";
                }
                case Team: {
                    return "Team";
                }
            }
            throw new IllegalArgumentException();
        }

        @Nullable
        public static CourseType fromString(String str) {
            return str.equals("classic") ? Classic : (str.equals("score") ? Score : (str.equals("fullpoint") ? FullPoint : (str.equals("team") ? Team : null)));
        }
    }
}

