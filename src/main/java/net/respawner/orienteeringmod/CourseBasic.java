/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.respawner.orienteeringmod.ICourse;
import net.respawner.orienteeringmod.ORTFileException;
import org.apache.logging.log4j.LogManager;

public abstract class CourseBasic
implements ICourse {
    protected static final long serialVersionUID = 4L;
    protected final String matchName;
    protected final String courseName;
    protected final List<Integer> CPList;
    protected final int validTime;
    protected final int[] start;
    protected final boolean needTeleport;
    @Nullable
    protected final String welcomeString;
    protected final int deathPenalty;

    public CourseBasic(JsonObject jsonCourse, String matchName, int deathPenalty) throws ORTFileException {
        try {
            this.matchName = Objects.requireNonNull(matchName);
            this.deathPenalty = deathPenalty;
            this.courseName = jsonCourse.get("name").getAsString();
            JsonArray jsonCPList = jsonCourse.get("cplist").getAsJsonArray();
            this.CPList = new ArrayList<Integer>();
            for (int k = 0; k < jsonCPList.size(); ++k) {
                this.CPList.add(jsonCPList.get(k).getAsInt());
            }
            this.validTime = jsonCourse.get("validtime").getAsInt();
            this.start = new int[3];
            JsonArray jsonStart = jsonCourse.get("start").getAsJsonArray();
            for (int i = 0; i < 3; ++i) {
                this.start[i] = jsonStart.get(i).getAsInt();
            }
            this.needTeleport = jsonCourse.get("needteleport").getAsBoolean();
            this.welcomeString = jsonCourse.get("welcome") == null ? null : jsonCourse.get("welcome").getAsString();
        }
        catch (Throwable e) {
            if (e instanceof ORTFileException) {
                throw (ORTFileException)e;
            }
            LogManager.getLogger().error(e.toString());
            throw new ORTFileException(ORTFileException.FileType.Matchseed, ORTFileException.IOType.Read, e.getMessage());
        }
    }

    @Override
    public String getName() {
        return this.courseName;
    }

    @Override
    public String getMatchName() {
        return this.matchName;
    }

    @Override
    public int[] getStartPos() {
        return this.start;
    }

    @Override
    public boolean needTeleport() {
        return this.needTeleport;
    }

    @Override
    public void writeToFile(String matchName) throws ORTFileException {
        ICourse.writeToFile(matchName, this);
    }

    @Override
    public String getWelcomeString() {
        return this.welcomeString;
    }
}

