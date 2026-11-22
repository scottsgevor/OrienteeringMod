package net.respawner.orienteeringmod;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

@SideOnly(Side.CLIENT)
public final class MapImageProjection {
    private static final float[] SCREEN_X = new float[4];
    private static final float[] SCREEN_Y = new float[4];
    private static final FloatBuffer MODELVIEW = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer PROJECTION = BufferUtils.createFloatBuffer(16);
    private static final IntBuffer VIEWPORT = BufferUtils.createIntBuffer(16);
    private static final FloatBuffer PROJECTED = BufferUtils.createFloatBuffer(3);
    private static MapImageRef ref;
    private static boolean valid;

    private MapImageProjection() {
    }

    public static void update(MapImageRef imageRef, float left, float top, float right, float bottom, float z) {
        if (!MapInspectionController.isActive()) {
            return;
        }
        MapImageRef active = MapInspectionController.getRef();
        if (active == null || imageRef == null
                || !active.getMatch().equals(imageRef.getMatch())
                || !active.getImage().equals(imageRef.getImage())) {
            return;
        }
        MODELVIEW.clear();
        PROJECTION.clear();
        VIEWPORT.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT);
        if (project(left, top, z, 0) & project(right, top, z, 1)
                & project(right, bottom, z, 2) & project(left, bottom, z, 3)) {
            ref = imageRef;
            valid = true;
        }
        else {
            valid = false;
        }
    }

    public static float[] cursorLocal(float mouseX, float mouseY) {
        if (!valid || ref == null || !MapInspectionController.isActive()) {
            return new float[] { 0.5f, 0.5f };
        }
        float[] first = triangle(mouseX, mouseY, 0, 1, 2);
        if (first != null) {
            float w0 = first[0];
            float w1 = first[1];
            float w2 = first[2];
            float base = w0 + w1;
            float s = base > 0.00001f ? w1 / base : 0.5f;
            return new float[] { clamp01(s), clamp01(w2) };
        }
        float[] second = triangle(mouseX, mouseY, 0, 2, 3);
        if (second != null) {
            float w0 = second[0];
            float w2 = second[1];
            float w3 = second[2];
            float base = w2 + w3;
            float s = base > 0.00001f ? w2 / base : 0.5f;
            return new float[] { clamp01(s), clamp01(w2 + w3) };
        }
        return new float[] { 0.5f, 0.5f };
    }

    public static void clear() {
        ref = null;
        valid = false;
    }

    private static boolean project(float x, float y, float z, int index) {
        if (!GLU.gluProject(x, y, z, MODELVIEW, PROJECTION, VIEWPORT, PROJECTED)) {
            return false;
        }
        SCREEN_X[index] = PROJECTED.get(0);
        SCREEN_Y[index] = PROJECTED.get(1);
        return true;
    }

    private static float[] triangle(float px, float py, int a, int b, int c) {
        float ax = SCREEN_X[a];
        float ay = SCREEN_Y[a];
        float bx = SCREEN_X[b];
        float by = SCREEN_Y[b];
        float cx = SCREEN_X[c];
        float cy = SCREEN_Y[c];
        float det = (by - cy) * (ax - cx) + (cx - bx) * (ay - cy);
        if (Math.abs(det) < 0.00001f) {
            return null;
        }
        float w0 = ((by - cy) * (px - cx) + (cx - bx) * (py - cy)) / det;
        float w1 = ((cy - ay) * (px - cx) + (ax - cx) * (py - cy)) / det;
        float w2 = 1.0f - w0 - w1;
        if (w0 < -0.001f || w1 < -0.001f || w2 < -0.001f) {
            return null;
        }
        return new float[] { w0, w1, w2 };
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}