package net.respawner.orienteeringmod;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

@SideOnly(Side.CLIENT)
public final class MapImageProjection {
    private static final Snapshot MAIN = new Snapshot();
    private static final Snapshot OFF = new Snapshot();
    private static final FloatBuffer MODELVIEW = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer PROJECTION = BufferUtils.createFloatBuffer(16);
    private static final IntBuffer VIEWPORT = BufferUtils.createIntBuffer(16);
    private static final FloatBuffer PROJECTED = BufferUtils.createFloatBuffer(3);

    private MapImageProjection() {
    }

    public static void update(MapImageRef imageRef, float left, float top, float right, float bottom, float z) {
        update(imageRef, left, top, right, bottom, z, EnumHand.MAIN_HAND);
    }

    public static void update(MapImageRef imageRef, float left, float top, float right, float bottom, float z, EnumHand hand) {
        if (!MapInspectionController.isActive() || imageRef == null) {
            return;
        }
        Snapshot snapshot = snapshot(hand);
        if (snapshot == null) {
            return;
        }
        MODELVIEW.clear();
        PROJECTION.clear();
        VIEWPORT.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT);
        if (project(snapshot, left, top, z, 0) & project(snapshot, right, top, z, 1)
                & project(snapshot, right, bottom, z, 2) & project(snapshot, left, bottom, z, 3)) {
            snapshot.ref = imageRef;
            snapshot.valid = true;
            snapshot.homographyValid = computeHomography(snapshot);
        }
        else {
            snapshot.valid = false;
            snapshot.homographyValid = false;
        }
    }

    public static float[] local(EnumHand hand, MapImageRef wanted, float mouseX, float mouseY) {
        Snapshot snapshot = snapshot(hand);
        if (!matches(snapshot, wanted) || !MapInspectionController.isActive()) {
            return null;
        }
        float[] first = triangle(snapshot, mouseX, mouseY, 0, 1, 2);
        if (first != null) {
            float w0 = first[0];
            float w1 = first[1];
            float w2 = first[2];
            float base = w0 + w1;
            float s = base > 0.00001f ? w1 / base : 0.5f;
            return new float[] { clamp01(s), clamp01(w2) };
        }
        float[] second = triangle(snapshot, mouseX, mouseY, 0, 2, 3);
        if (second != null) {
            float w0 = second[0];
            float w2 = second[1];
            float w3 = second[2];
            float base = w2 + w3;
            float s = base > 0.00001f ? w2 / base : 0.5f;
            return new float[] { clamp01(s), clamp01(w2 + w3) };
        }
        return null;
    }

    public static float[] rawLocal(EnumHand hand, MapImageRef wanted, float mouseX, float mouseY) {
        Snapshot snapshot = snapshot(hand);
        if (!matches(snapshot, wanted) || !MapInspectionController.isActive() || !snapshot.homographyValid) {
            return null;
        }
        double[] inverse = snapshot.inverseHomography;
        double x = inverse[0] * mouseX + inverse[1] * mouseY + inverse[2];
        double y = inverse[3] * mouseX + inverse[4] * mouseY + inverse[5];
        double w = inverse[6] * mouseX + inverse[7] * mouseY + inverse[8];
        if (Math.abs(w) < 0.000001) {
            return null;
        }
        return new float[] { (float) (x / w), (float) (y / w) };
    }

    public static float centerDistanceSq(EnumHand hand, MapImageRef wanted, float mouseX, float mouseY) {
        Snapshot snapshot = snapshot(hand);
        if (!matches(snapshot, wanted) || !MapInspectionController.isActive()) {
            return Float.MAX_VALUE;
        }
        float centerX = (snapshot.x[0] + snapshot.x[1] + snapshot.x[2] + snapshot.x[3]) / 4.0f;
        float centerY = (snapshot.y[0] + snapshot.y[1] + snapshot.y[2] + snapshot.y[3]) / 4.0f;
        float dx = mouseX - centerX;
        float dy = mouseY - centerY;
        return dx * dx + dy * dy;
    }

    public static void clear() {
        MAIN.clear();
        OFF.clear();
    }

    private static boolean computeHomography(Snapshot snapshot) {
        double x0 = snapshot.x[0];
        double y0 = snapshot.y[0];
        double x1 = snapshot.x[1];
        double y1 = snapshot.y[1];
        double x2 = snapshot.x[2];
        double y2 = snapshot.y[2];
        double x3 = snapshot.x[3];
        double y3 = snapshot.y[3];
        double dx1 = x1 - x2;
        double dx2 = x3 - x2;
        double sx = x0 - x1 + x2 - x3;
        double dy1 = y1 - y2;
        double dy2 = y3 - y2;
        double sy = y0 - y1 + y2 - y3;
        double den = dx1 * dy2 - dx2 * dy1;
        double g = 0.0D;
        double h = 0.0D;
        if (Math.abs(den) > 0.000000001D) {
            g = (sx * dy2 - dx2 * sy) / den;
            h = (dx1 * sy - sx * dy1) / den;
        }
        else if (Math.abs(sx) > 0.000001D || Math.abs(sy) > 0.000001D) {
            return false;
        }
        double a = x1 - x0 + g * x1;
        double b = x3 - x0 + h * x3;
        double c = x0;
        double d = y1 - y0 + g * y1;
        double e = y3 - y0 + h * y3;
        double f = y0;
        double det = a * (e - f * h) - b * (d - f * g) + c * (d * h - e * g);
        if (Math.abs(det) < 0.000000001D) {
            return false;
        }
        double[] inverse = snapshot.inverseHomography;
        inverse[0] = (e - f * h) / det;
        inverse[1] = (c * h - b) / det;
        inverse[2] = (b * f - c * e) / det;
        inverse[3] = (f * g - d) / det;
        inverse[4] = (a - c * g) / det;
        inverse[5] = (c * d - a * f) / det;
        inverse[6] = (d * h - e * g) / det;
        inverse[7] = (b * g - a * h) / det;
        inverse[8] = (a * e - b * d) / det;
        return true;
    }

    private static Snapshot snapshot(EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND) {
            return MAIN;
        }
        if (hand == EnumHand.OFF_HAND) {
            return OFF;
        }
        return null;
    }

    private static boolean matches(Snapshot snapshot, MapImageRef wanted) {
        return snapshot != null && snapshot.valid && snapshot.ref != null && wanted != null
                && snapshot.ref.getMatch().equals(wanted.getMatch())
                && snapshot.ref.getImage().equals(wanted.getImage());
    }

    private static boolean project(Snapshot snapshot, float x, float y, float z, int index) {
        if (!GLU.gluProject(x, y, z, MODELVIEW, PROJECTION, VIEWPORT, PROJECTED)) {
            return false;
        }
        snapshot.x[index] = PROJECTED.get(0);
        snapshot.y[index] = PROJECTED.get(1);
        return true;
    }

    private static float[] triangle(Snapshot snapshot, float px, float py, int a, int b, int c) {
        float ax = snapshot.x[a];
        float ay = snapshot.y[a];
        float bx = snapshot.x[b];
        float by = snapshot.y[b];
        float cx = snapshot.x[c];
        float cy = snapshot.y[c];
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

    private static final class Snapshot {
        final float[] x = new float[4];
        final float[] y = new float[4];
        final double[] inverseHomography = new double[9];
        MapImageRef ref;
        boolean valid;
        boolean homographyValid;

        void clear() {
            ref = null;
            valid = false;
            homographyValid = false;
        }
    }
}