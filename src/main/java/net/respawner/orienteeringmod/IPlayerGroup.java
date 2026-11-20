/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.respawner.orienteeringmod.ICourse;
import net.respawner.orienteeringmod.IGrade;
import net.respawner.orienteeringmod.ORTPlayer;

public interface IPlayerGroup
extends Serializable {
    public void init(String var1, List<String> var2);

    public boolean containsPlayer(String var1);

    public String getGroupName();

    public String getDefaultPlayer();

    public int size();

    @Nonnull
    public String getDefaultCourse();

    @Nullable
    public String getCourse(String var1);

    @Nullable
    public IGrade computeGroupGrade(Map<String, ORTPlayer> var1, Map<String, ICourse> var2);

    public List<String> getAllCourses();
}

