package net.respawner.orienteeringmod;

import java.util.function.IntFunction;
import java.util.function.Predicate;

/** Finds the first ground block visible from the top of a column. */
final class SurfaceHeight {
    static int find(int highestY, IntFunction<String> blockAtY,
                    Predicate<String> isGround) {
        for (int y = Math.min(highestY, 255); y > 50; y--) {
            if (isGround.test(blockAtY.apply(y))) {
                return y;
            }
        }
        // Fall back below the threshold only if the upper column has no ground.
        for (int y = Math.min(highestY, 50); y >= 0; y--) {
            if (isGround.test(blockAtY.apply(y))) {
                return y;
            }
        }
        return -1;
    }
}
