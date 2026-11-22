package net.respawner.orienteeringmod;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public final class MapInspectionController {
    private static boolean active;
    private static MapImageRef ref;

    private MapInspectionController() {
    }

    public static boolean isActive() {
        return active;
    }

    public static MapImageRef getRef() {
        return ref;
    }

    public static float getZoom() {
        return ref == null ? MapImageClientCache.MIN_ZOOM : MapImageClientCache.zoom(ref);
    }

    public static void toggle(Minecraft mc) {
        if (mc == null || mc.field_71439_g == null) {
            return;
        }
        if (active) {
            close(mc);
            return;
        }
        if (mc.field_71462_r != null) {
            return;
        }
        MapImageRef twoHanded = MapImageHands.primaryTwoHanded(mc.field_71439_g);
        if (twoHanded == null) {
            return;
        }
        ref = twoHanded;
        active = true;
        MapImageProjection.clear();
        MapImageClientCache.retry(ref);
        mc.func_71364_i();
    }

    public static void close(Minecraft mc) {
        active = false;
        ref = null;
        MapImageProjection.clear();
        if (mc != null && mc.field_71462_r == null) {
            mc.func_71381_h();
        }
    }

    public static void tick(Minecraft mc) {
        if (mc == null || mc.field_71439_g == null) {
            active = false;
            ref = null;
            MapImageProjection.clear();
            return;
        }
        if (!active) {
            for (MapImageRef held : MapImageHands.held(mc.field_71439_g)) {
                MapImageClientCache.ensureRequested(held);
            }
            return;
        }
        if (mc.field_71441_e == null || mc.field_71462_r != null) {
            active = false;
            ref = null;
            MapImageProjection.clear();
            return;
        }
        if (ref == null || !MapImageHands.isTwoHanded(mc.field_71439_g, ref)) {
            close(mc);
            return;
        }
        MapImageClientCache.ensureRequested(ref);
    }

    public static void rotateHeld(Minecraft mc, int delta) {
        if (mc == null || mc.field_71439_g == null) {
            return;
        }
        for (MapImageRef held : MapImageHands.held(mc.field_71439_g)) {
            MapImageClientCache.rotate(held, delta);
        }
    }

    public static void scroll(Minecraft mc, int wheel) {
        if (!active || ref == null || wheel == 0) {
            return;
        }
        float[] local = MapImageProjection.cursorLocal(Mouse.getX(), Mouse.getY());
        double factor = Math.pow(1.2D, (double) wheel / 120.0D);
        MapImageClientCache.zoomAt(ref, (float) factor, local[0], local[1]);
    }
}