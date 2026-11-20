/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import net.respawner.orienteeringmod.CourseBasic;
import net.respawner.orienteeringmod.CourseClassic;
import net.respawner.orienteeringmod.CourseFullPoint;
import net.respawner.orienteeringmod.CourseScore;
import net.respawner.orienteeringmod.CourseTeam;
import net.respawner.orienteeringmod.ICourse;
import net.respawner.orienteeringmod.IPlayerGroup;
import net.respawner.orienteeringmod.IllegalSourceException;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTMatch;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public class MatchGeneration {
    public static void generateAndSave(String matchName, String playerName) throws ORTFileException {
        JsonParser parser = new JsonParser();
        try {
            JsonObject seed = (JsonObject)parser.parse(new FileReader(Util.getBasicDataPath(matchName) + "matchseed.json"));
            try {
                int n = seed.get("version").getAsInt();
                if (n != 4) {
                    throw new IllegalStateException();
                }
            }
            catch (Exception e) {
                throw new IllegalSourceException(ORTFileException.FileType.Matchseed, "matchseed version not found or is not correct.");
            }
            if (!seed.get("matchname").getAsString().equals(matchName)) {
                throw new IllegalSourceException(ORTFileException.FileType.Matchseed, "world name and match name are not the same.");
            }
            ORTMatch match = new ORTMatch(seed, matchName);
            LogManager.getLogger().info("Match data exported: " + matchName + ". Match type: " + match.getMatchType().toString() + ". Use roster: " + match.useRoster());
            File dirCourse = new File(Util.getBasicDataPath(matchName) + "courses");
            if (dirCourse.exists()) {
                for (File file : dirCourse.listFiles()) {
                    file.delete();
                }
                dirCourse.delete();
            }
            dirCourse.mkdir();
            File dirPlayer = new File(Util.getBasicDataPath(matchName) + "players");
            if (dirPlayer.exists()) {
                for (File file : dirPlayer.listFiles()) {
                    file.delete();
                }
                dirPlayer.delete();
                LogManager.getLogger().info("Past player data erased.");
            }
            dirPlayer.mkdir();
            JsonArray jsonCourses = seed.get("courses").getAsJsonArray();
            HashMap<String, ICourse.CourseType> courseTypes = new HashMap<String, ICourse.CourseType>();
            for (int i = 0; i < jsonCourses.size(); ++i) {
                CourseBasic course;
                ICourse.CourseType type = Objects.requireNonNull(ICourse.CourseType.fromString(jsonCourses.get(i).getAsJsonObject().get("coursetype").getAsString()));
                switch (type) {
                    case Classic: {
                        course = new CourseClassic(jsonCourses.get(i).getAsJsonObject(), matchName, match.getDeathPenalty());
                        break;
                    }
                    case Score: {
                        course = new CourseScore(jsonCourses.get(i).getAsJsonObject(), matchName, match.getDeathPenalty());
                        break;
                    }
                    case FullPoint: {
                        course = new CourseFullPoint(jsonCourses.get(i).getAsJsonObject(), matchName, match.getDeathPenalty());
                        break;
                    }
                    case Team: {
                        course = new CourseTeam(jsonCourses.get(i).getAsJsonObject(), matchName, match.getDeathPenalty());
                        break;
                    }
                    default: {
                        throw new IllegalStateException("please contact the mod designer.");
                    }
                }
                match.addCourse(course.getName());
                courseTypes.put(course.getName(), course.getCourseType());
                course.writeToFile(matchName);
                LogManager.getLogger().info("Course data exported: " + course.getName() + ". Course type: " + course.getCourseType().toString());
            }
            if (!match.getCourseList().contains(match.getDefaultCourse())) {
                throw new IllegalSourceException(ORTFileException.FileType.Matchseed, "the default course is not in the course list.");
            }
            for (IPlayerGroup group : match.getPlayerRoster()) {
                List<String> courses = group.getAllCourses();
                for (String course : courses) {
                    if (!match.getCourseList().contains(course)) {
                        throw new IllegalSourceException(ORTFileException.FileType.Roster, "invalid course name " + group.getGroupName() + ".\"" + course + "\". Not contained in the course list.");
                    }
                    if (match.getMatchType() != ORTMatch.MatchType.Team || courseTypes.get(course) == ICourse.CourseType.Team) continue;
                    throw new IllegalSourceException(ORTFileException.FileType.Roster, "invalid course name " + group.getGroupName() + ".\"" + course + "\". All courses in a team match must be team courses.");
                }
            }
            match.writeToFile(matchName);
        }
        catch (ORTFileException e) {
            throw e;
        }
        catch (Throwable e) {
            LogManager.getLogger().error(e.toString());
            throw new ORTFileException(ORTFileException.FileType.Matchseed, ORTFileException.IOType.Read, e.getMessage());
        }
    }
}

