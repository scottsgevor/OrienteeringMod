package net.respawner.orienteeringmod;

import net.minecraft.util.FoodStats;

public final class OrienteeringFoodHooks {
    private OrienteeringFoodHooks() {
    }

    public static void setFoodLevelIfEnabled(FoodStats stats, int level) {
        if (stats != null && OrienteeringConfig.isForceFoodLevel()) {
            stats.func_75114_a(level);
        }
    }
}