package net.respawner.orienteeringmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public final class MapInspectionController {
    private static boolean active;
    private static MapImageRef ref;
    private static EnumHand inspectionHand;
    private static boolean dragging;
    private static EnumHand dragHand;
    private static MapImageRef dragRef;
    private static float dragS;
    private static float dragT;
    private static boolean leftDown;

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
        MapImageRef target = MapImageHands.primary(mc.field_71439_g);
        EnumHand hand = MapImageHands.primaryHand(mc.field_71439_g);
        if (target == null || hand == null) {
            return;
        }
        ref = target;
        inspectionHand = hand;
        active = true;
        clearDrag();
        leftDown = Mouse.isButtonDown(0);
        MapImageProjection.clear();
        MapImageClientCache.retry(ref);
        mc.func_71364_i();
    }

    public static void close(Minecraft mc) {
        active = false;
        ref = null;
        inspectionHand = null;
        clearDrag();
        leftDown = false;
        MapImageProjection.clear();
        if (mc != null && mc.field_71462_r == null) {
            mc.func_71381_h();
        }
    }

    public static void tick(Minecraft mc) {
        if (mc == null || mc.field_71439_g == null) {
            active = false;
            ref = null;
            inspectionHand = null;
            clearDrag();
            leftDown = false;
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
            inspectionHand = null;
            clearDrag();
            leftDown = false;
            MapImageProjection.clear();
            return;
        }
        if (ref == null || inspectionHand == null
                || !MapImageHands.handHolds(mc.field_71439_g, inspectionHand, ref)) {
            close(mc);
            return;
        }
        if (dragging && !MapImageHands.handHolds(mc.field_71439_g, dragHand, dragRef)) {
            clearDrag();
        }
        for (MapImageRef held : MapImageHands.held(mc.field_71439_g)) {
            MapImageClientCache.ensureRequested(held);
        }
    }

    public static void updateDrag(Minecraft mc) {
        if (!active) {
            leftDown = false;
            clearDrag();
            return;
        }
        if (mc == null || mc.field_71439_g == null || mc.field_71462_r != null) {
            leftDown = false;
            clearDrag();
            return;
        }
        boolean down = Mouse.isButtonDown(0);
        if (!down) {
            leftDown = false;
            clearDrag();
            return;
        }
        if (!leftDown) {
            leftDown = true;
            beginDrag(mc);
            return;
        }
        if (!dragging) {
            return;
        }
        if (!MapImageHands.handHolds(mc.field_71439_g, dragHand, dragRef)) {
            clearDrag();
            return;
        }
        float[] current = MapImageProjection.rawLocal(dragHand, dragRef, Mouse.getX(), Mouse.getY());
        if (current == null) {
            return;
        }
        float du = current[0] - dragS;
        float dv = dragT - current[1];
        dragS = current[0];
        dragT = current[1];
        MapImageClientCache.panBy(dragRef, du, dv);
    }

    private static void beginDrag(Minecraft mc) {
        EntityPlayerSP player = mc.field_71439_g;
        MapImageRef mainRef = MapImageRef.fromStack(player.func_184586_b(EnumHand.MAIN_HAND));
        MapImageRef offRef = MapImageRef.fromStack(player.func_184586_b(EnumHand.OFF_HAND));
        if (mainRef == null && offRef == null) {
            return;
        }
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        float[] mainLocal = mainRef == null ? null
                : MapImageProjection.rawLocal(EnumHand.MAIN_HAND, mainRef, mouseX, mouseY);
        float[] offLocal = offRef == null ? null
                : MapImageProjection.rawLocal(EnumHand.OFF_HAND, offRef, mouseX, mouseY);
        boolean mainInside = inside(mainLocal);
        boolean offInside = inside(offLocal);
        EnumHand hand;
        MapImageRef target;
        float[] local;
        if (mainInside && offInside) {
            float mainDistance = MapImageProjection.centerDistanceSq(
                    EnumHand.MAIN_HAND, mainRef, mouseX, mouseY);
            float offDistance = MapImageProjection.centerDistanceSq(
                    EnumHand.OFF_HAND, offRef, mouseX, mouseY);
            if (offDistance < mainDistance) {
                hand = EnumHand.OFF_HAND;
                target = offRef;
                local = offLocal;
            }
            else {
                hand = EnumHand.MAIN_HAND;
                target = mainRef;
                local = mainLocal;
            }
        }
        else if (mainInside) {
            hand = EnumHand.MAIN_HAND;
            target = mainRef;
            local = mainLocal;
        }
        else if (offInside) {
            hand = EnumHand.OFF_HAND;
            target = offRef;
            local = offLocal;
        }
        else {
            return;
        }
        dragging = true;
        dragHand = hand;
        dragRef = target;
        dragS = local[0];
        dragT = local[1];
    }

    private static boolean inside(float[] local) {
        return local != null && local[0] >= -0.01f && local[0] <= 1.01f
                && local[1] >= -0.01f && local[1] <= 1.01f;
    }

    private static void clearDrag() {
        dragging = false;
        dragHand = null;
        dragRef = null;
    }

    public static void rotateHeld(Minecraft mc, int delta) {
        if (mc == null || mc.field_71439_g == null) {
            return;
        }
        if (active && dragging) {
            clearDrag();
        }
        for (MapImageRef held : MapImageHands.held(mc.field_71439_g)) {
            MapImageClientCache.rotate(held, delta);
        }
    }

    public static void scroll(Minecraft mc, int wheel) {
        if (!active || wheel == 0 || mc == null || mc.field_71439_g == null) {
            return;
        }
        EntityPlayerSP player = mc.field_71439_g;
        MapImageRef mainRef = MapImageRef.fromStack(player.func_184586_b(EnumHand.MAIN_HAND));
        MapImageRef offRef = MapImageRef.fromStack(player.func_184586_b(EnumHand.OFF_HAND));
        if (mainRef == null && offRef == null) {
            return;
        }
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        double factor = Math.pow(1.2D, (double) wheel / 120.0D);
        float zoomFactor = (float) factor;

        if (mainRef != null && offRef != null) {
            float[] mainLocal = MapImageProjection.local(EnumHand.MAIN_HAND, mainRef, mouseX, mouseY);
            float[] offLocal = MapImageProjection.local(EnumHand.OFF_HAND, offRef, mouseX, mouseY);
            if (mainLocal != null || offLocal != null) {
                if (mainLocal != null && offLocal != null) {
                    float mainDistance = MapImageProjection.centerDistanceSq(
                            EnumHand.MAIN_HAND, mainRef, mouseX, mouseY);
                    float offDistance = MapImageProjection.centerDistanceSq(
                            EnumHand.OFF_HAND, offRef, mouseX, mouseY);
                    if (offDistance < mainDistance) {
                        MapImageClientCache.zoomAt(offRef, zoomFactor, offLocal[0], 1.0f - offLocal[1]);
                    }
                    else {
                        MapImageClientCache.zoomAt(mainRef, zoomFactor, mainLocal[0], 1.0f - mainLocal[1]);
                    }
                }
                else if (mainLocal != null) {
                    MapImageClientCache.zoomAt(mainRef, zoomFactor, mainLocal[0], 1.0f - mainLocal[1]);
                }
                else {
                    MapImageClientCache.zoomAt(offRef, zoomFactor, offLocal[0], 1.0f - offLocal[1]);
                }
                return;
            }
            MapImageClientCache.zoomAt(mainRef, zoomFactor, 0.5f, 0.5f);
            if (!sameRef(mainRef, offRef)) {
                MapImageClientCache.zoomAt(offRef, zoomFactor, 0.5f, 0.5f);
            }
            return;
        }

        MapImageRef ref = mainRef != null ? mainRef : offRef;
        EnumHand hand = mainRef != null ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        float[] local = MapImageProjection.local(hand, ref, mouseX, mouseY);
        float s = local != null ? local[0] : 0.5f;
        float t = local != null ? 1.0f - local[1] : 0.5f;
        MapImageClientCache.zoomAt(ref, zoomFactor, s, t);
    }

    private static boolean sameRef(MapImageRef left, MapImageRef right) {
        return left != null && right != null
                && left.getMatch().equals(right.getMatch())
                && left.getImage().equals(right.getImage());
    }
}