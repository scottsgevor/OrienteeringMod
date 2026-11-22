package net.respawner.orienteeringmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public final class MapInspectionController {
    private static final float MAX_ZOOM = 8.0f;
    private static boolean active;
    private static MapImageRef ref;
    private static float zoom = 1.0f;
    private static float panX;
    private static float panY;

    private MapInspectionController() {
    }

    public static boolean isActive() {
        return active;
    }

    public static MapImageRef getRef() {
        return ref;
    }

    public static float getZoom() {
        return zoom;
    }

    public static float getPanX() {
        return panX;
    }

    public static float getPanY() {
        return panY;
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
        MapImageRef primary = MapImageHands.primary(mc.field_71439_g);
        if (primary == null) {
            return;
        }
        ref = primary;
        active = true;
        zoom = 1.0f;
        panX = 0.0f;
        panY = 0.0f;
        MapImageClientCache.retry(ref);
        mc.func_71364_i();
    }

    public static void close(Minecraft mc) {
        active = false;
        ref = null;
        zoom = 1.0f;
        panX = 0.0f;
        panY = 0.0f;
        if (mc != null && mc.field_71462_r == null) {
            mc.func_71381_h();
        }
    }

    public static void tick(Minecraft mc) {
        if (mc == null || mc.field_71439_g == null) {
            active = false;
            ref = null;
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
            return;
        }
        if (ref == null || !MapImageHands.holds(mc.field_71439_g, ref)) {
            close(mc);
            return;
        }
        MapImageClientCache.ensureRequested(ref);
    }

    public static void rotateHeld(Minecraft mc, int delta) {
        if (mc == null || mc.field_71439_g == null) {
            return;
        }
        boolean rotated = false;
        for (MapImageRef held : MapImageHands.held(mc.field_71439_g)) {
            MapImageClientCache.rotate(held, delta);
            rotated = true;
        }
        if (active && rotated) {
            panX = 0.0f;
            panY = 0.0f;
        }
    }

    public static void scroll(Minecraft mc, int wheel) {
        if (!active || ref == null || wheel == 0) {
            return;
        }
        int imageWidth = MapImageClientCache.width(ref);
        int imageHeight = MapImageClientCache.height(ref);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }
        ScaledResolution resolution = new ScaledResolution(mc);
        int screenWidth = resolution.func_78326_a();
        int screenHeight = resolution.func_78328_b();
        float fit = fitScale(screenWidth, screenHeight);
        float currentScale = fit * zoom;
        double factor = Math.pow(1.2, (double) wheel / 120.0);
        float nextScale = (float) Math.max((double) fit, Math.min((double) fit * (double) MAX_ZOOM, (double) currentScale * factor));
        if (Math.abs(nextScale - currentScale) < 0.0001f) {
            return;
        }
        int mouseX = cursorX(mc, resolution);
        int mouseY = cursorY(mc, resolution);
        float centerX = (float) screenWidth / 2.0f + panX;
        float centerY = (float) screenHeight / 2.0f + panY;
        double radians = Math.toRadians((double) MapImageClientCache.rotation(ref));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double dx = (double) mouseX - (double) centerX;
        double dy = (double) mouseY - (double) centerY;
        double localX = (dx * cos + dy * sin) / (double) currentScale;
        double localY = (-dx * sin + dy * cos) / (double) currentScale;
        double rotatedX = cos * localX * (double) nextScale - sin * localY * (double) nextScale;
        double rotatedY = sin * localX * (double) nextScale + cos * localY * (double) nextScale;
        zoom = nextScale / fit;
        panX = (float) ((double) mouseX - rotatedX) - (float) screenWidth / 2.0f;
        panY = (float) ((double) mouseY - rotatedY) - (float) screenHeight / 2.0f;
        clampPan(screenWidth, screenHeight, fit);
    }

    public static float fitScale(int screenWidth, int screenHeight) {
        if (ref == null) {
            return 1.0f;
        }
        int imageWidth = MapImageClientCache.width(ref);
        int imageHeight = MapImageClientCache.height(ref);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return 1.0f;
        }
        int rotatedWidth = MapImageClientCache.rotation(ref) % 180 == 0 ? imageWidth : imageHeight;
        int rotatedHeight = MapImageClientCache.rotation(ref) % 180 == 0 ? imageHeight : imageWidth;
        return Math.min((float) screenWidth * 0.92f / (float) rotatedWidth,
                (float) screenHeight * 0.92f / (float) rotatedHeight);
    }

    public static void clampPan(int screenWidth, int screenHeight, float fit) {
        float scale = fit * zoom;
        int imageWidth = MapImageClientCache.width(ref);
        int imageHeight = MapImageClientCache.height(ref);
        int rotatedWidth = MapImageClientCache.rotation(ref) % 180 == 0 ? imageWidth : imageHeight;
        int rotatedHeight = MapImageClientCache.rotation(ref) % 180 == 0 ? imageHeight : imageWidth;
        float halfWidth = (float) rotatedWidth * scale / 2.0f;
        float halfHeight = (float) rotatedHeight * scale / 2.0f;
        if (zoom <= 1.0001f) {
            panX = 0.0f;
            panY = 0.0f;
            return;
        }
        float limitX = Math.abs((float) screenWidth / 2.0f - halfWidth);
        float limitY = Math.abs((float) screenHeight / 2.0f - halfHeight);
        panX = Math.max(-limitX, Math.min(limitX, panX));
        panY = Math.max(-limitY, Math.min(limitY, panY));
    }

    public static int cursorX(Minecraft mc, ScaledResolution resolution) {
        return Mouse.getX() * resolution.func_78326_a() / Math.max(1, mc.field_71443_c);
    }

    public static int cursorY(Minecraft mc, ScaledResolution resolution) {
        return resolution.func_78328_b()
                - Mouse.getY() * resolution.func_78328_b() / Math.max(1, mc.field_71440_d) - 1;
    }
}