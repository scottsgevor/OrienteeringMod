/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.util.Objects;
import net.respawner.orienteeringmod.GradeSP;
import net.respawner.orienteeringmod.IGrade;
import net.respawner.orienteeringmod.ORTFileException;

public abstract class GradeMP
implements IGrade {
    private static final long serialVersionUID = 4L;
    protected final String matchName;
    protected String groupName;
    protected final GradeSP[] grades;

    public GradeMP(GradeSP[] grades, String matchName, String groupName) {
        this.matchName = Objects.requireNonNull(matchName);
        this.grades = Objects.requireNonNull(grades);
        if (grades.length == 0 || grades[0] == null) {
            throw new IllegalArgumentException("grades");
        }
        this.groupName = Objects.requireNonNull(groupName);
    }

    @Override
    public String getMatchName() {
        return this.matchName;
    }

    @Override
    public String getGroupName() {
        return this.groupName;
    }

    @Override
    public void writeToFile(String matchName) throws ORTFileException {
        IGrade.writeToFile(matchName, this);
    }
}

