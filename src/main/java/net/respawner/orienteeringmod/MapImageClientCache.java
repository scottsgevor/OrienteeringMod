package net.respawner.orienteeringmod;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public final class MapImageClientCache {
    public static final float MIN_ZOOM = 1.0f;
    public static final float MAX_ZOOM = 8.0f;
    private static final Map<String, Entry> ENTRIES = new HashMap<String, Entry>();
    private static final AtomicInteger NEXT_TEXTURE_ID = new AtomicInteger();
    private static final int MAX_TEXTURE_SIZE = 4096;
    private static volatile int maxTextureSize = MAX_TEXTURE_SIZE;
    private static final ExecutorService DECODER = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "ORT-MapImage-Decoder");
            thread.setDaemon(true);
            return thread;
        }
    });
    private static boolean networkRegistered;

    private MapImageClientCache() {
    }

    public static void ensureRequested(MapImageRef ref) {
        if (ref == null) {
            return;
        }
        ensureNetworkRegistered();
        Entry entry;
        synchronized (ENTRIES) {
            entry = getOrCreate(ref);
            if (entry.texture != null || entry.decoding || entry.requested) {
                return;
            }
            if (entry.failed) {
                if (System.currentTimeMillis() - entry.lastFailureTime < 3000L) {
                    return;
                }
                entry.failed = false;
                entry.bytes = null;
                entry.received = 0;
                entry.total = 0;
            }
            entry.requested = true;
        }
        try {
            OrienteeringMod.NETHANDLER.sendToServer(new MsgMapImageRequest(ref.getMatch(), ref.getImage()));
        }
        catch (Throwable e) {
            synchronized (ENTRIES) {
                entry.requested = false;
            }
            LogManager.getLogger().warn("Failed to request map image " + ref.getImage() + ": " + e.getMessage());
        }
    }

    public static void retry(MapImageRef ref) {
        if (ref == null) {
            return;
        }
        synchronized (ENTRIES) {
            Entry entry = getOrCreate(ref);
            if (entry.texture != null || entry.decoding || (entry.requested && !entry.failed)) {
                return;
            }
            entry.failed = false;
            entry.requested = false;
            entry.bytes = null;
            entry.received = 0;
            entry.total = 0;
        }
        ensureRequested(ref);
    }

    public static void receiveChunk(String matchName, String imageName, int totalSize, int offset, byte[] data) {
        if (matchName == null || imageName == null || matchName.isEmpty() || imageName.isEmpty() || data == null) {
            return;
        }
        byte[] completed = null;
        synchronized (ENTRIES) {
            Entry entry = getOrCreate(new MapImageRef(matchName, imageName));
            if (entry.texture != null || entry.failed || entry.decoding) {
                return;
            }
            if (totalSize <= 0) {
                markFailed(entry);
                LogManager.getLogger().debug("Map image not available: " + matchName + "/" + imageName);
                return;
            }
            if (entry.bytes == null) {
                if (offset != 0 || totalSize > 128 * 1024 * 1024) {
                    markFailed(entry);
                    return;
                }
                entry.total = totalSize;
                entry.bytes = new byte[totalSize];
            }
            if (entry.total != totalSize || offset < 0 || offset + data.length > entry.total || offset != entry.received) {
                markFailed(entry);
                return;
            }
            System.arraycopy(data, 0, entry.bytes, offset, data.length);
            entry.received += data.length;
            if (entry.received >= entry.total) {
                completed = entry.bytes;
                entry.bytes = null;
                entry.decoding = true;
                entry.requested = false;
            }
        }
        if (completed != null) {
            decodeAsync(matchName, imageName, completed);
        }
    }

    public static ResourceLocation texture(MapImageRef ref) {
        Entry entry = get(ref);
        return entry == null ? null : entry.texture;
    }

    public static boolean failed(MapImageRef ref) {
        Entry entry = get(ref);
        return entry != null && entry.failed;
    }

    public static boolean decoding(MapImageRef ref) {
        Entry entry = get(ref);
        return entry != null && entry.decoding;
    }

    public static int rotation(MapImageRef ref) {
        Entry entry = get(ref);
        return entry == null ? 0 : entry.rotation;
    }

    public static int rotate(MapImageRef ref, int delta) {
        if (ref == null) {
            return 0;
        }
        synchronized (ENTRIES) {
            Entry entry = getOrCreate(ref);
            entry.rotation = ((entry.rotation + delta) % 360 + 360) % 360;
            return entry.rotation;
        }
    }

    public static float zoom(MapImageRef ref) {
        Entry entry = get(ref);
        return entry == null ? MIN_ZOOM : entry.zoom;
    }

    public static float[] displayQuad(MapImageRef ref, float longSide) {
        if (ref == null) {
            return new float[] { longSide, longSide };
        }
        synchronized (ENTRIES) {
            Entry entry = getOrCreate(ref);
            return quad(entry, longSide, entry.zoom);
        }
    }

    public static float[] window(MapImageRef ref) {
        if (ref == null) {
            return new float[] { 0.0f, 0.0f, 1.0f, 1.0f };
        }
        synchronized (ENTRIES) {
            Entry entry = getOrCreate(ref);
            float uHalf = halfU(entry, entry.zoom);
            float vHalf = halfV(entry, entry.zoom);
            float centerU = Math.max(uHalf, Math.min(1.0f - uHalf, entry.centerU));
            float centerV = Math.max(vHalf, Math.min(1.0f - vHalf, entry.centerV));
            return new float[] { centerU - uHalf, centerV - vHalf, centerU + uHalf, centerV + vHalf };
        }
    }

    public static float zoomAt(MapImageRef ref, float factor, float s, float tDown) {
        if (ref == null) {
            return MIN_ZOOM;
        }
        synchronized (ENTRIES) {
            Entry entry = getOrCreate(ref);
            float oldZoom = entry.zoom;
            float oldHalfU = halfU(entry, oldZoom);
            float oldHalfV = halfV(entry, oldZoom);
            float oldCenterU = Math.max(oldHalfU, Math.min(1.0f - oldHalfU, entry.centerU));
            float oldCenterV = Math.max(oldHalfV, Math.min(1.0f - oldHalfV, entry.centerV));
            float tUp = 1.0f - tDown;
            float imageU = oldCenterU - oldHalfU + s * oldHalfU * 2.0f;
            float imageV = oldCenterV - oldHalfV + tUp * oldHalfV * 2.0f;
            float newZoom = Math.max(MIN_ZOOM, Math.min(maxZoom(entry), oldZoom * factor));
            if (Math.abs(newZoom - oldZoom) < 0.0001f) {
                return oldZoom;
            }
            float newHalfU = halfU(entry, newZoom);
            float newHalfV = halfV(entry, newZoom);
            float[] oldQuad = quad(entry, 1.0f, oldZoom);
            float[] newQuad = quad(entry, 1.0f, newZoom);
            float s1 = 0.5f + (s - 0.5f) * oldQuad[0] / newQuad[0];
            float t1 = 0.5f + (tDown - 0.5f) * oldQuad[1] / newQuad[1];
            float t1Up = 1.0f - t1;
            entry.centerU = Math.max(newHalfU, Math.min(1.0f - newHalfU,
                    imageU - s1 * newHalfU * 2.0f + newHalfU));
            entry.centerV = Math.max(newHalfV, Math.min(1.0f - newHalfV,
                    imageV - t1Up * newHalfV * 2.0f + newHalfV));
            entry.zoom = newZoom;
            return newZoom;
        }
    }

    public static void panBy(MapImageRef ref, float du, float dv) {
        if (ref == null) {
            return;
        }
        synchronized (ENTRIES) {
            Entry entry = getOrCreate(ref);
            float uHalf = halfU(entry, entry.zoom);
            float vHalf = halfV(entry, entry.zoom);
            entry.centerU = Math.max(uHalf, Math.min(1.0f - uHalf,
                    entry.centerU - du * uHalf * 2.0f));
            entry.centerV = Math.max(vHalf, Math.min(1.0f - vHalf,
                    entry.centerV - dv * vHalf * 2.0f));
        }
    }

    private static float fillZoom(Entry entry) {
        if (entry.width <= 0 || entry.height <= 0) {
            return 1.0f;
        }
        return (float) Math.max(entry.width, entry.height) / (float) Math.min(entry.width, entry.height);
    }

    private static float maxZoom(Entry entry) {
        return Math.max(MAX_ZOOM, Math.min(fillZoom(entry), 32.0f));
    }

    private static float halfU(Entry entry, float zoom) {
        if (entry.width > 0 && entry.height > 0 && entry.width < entry.height) {
            return 0.5f * Math.min(1.0f, fillZoom(entry) / zoom);
        }
        return 0.5f / zoom;
    }

    private static float halfV(Entry entry, float zoom) {
        if (entry.width > 0 && entry.height > 0 && entry.width < entry.height) {
            return 0.5f / zoom;
        }
        return 0.5f * Math.min(1.0f, fillZoom(entry) / zoom);
    }

    private static float[] quad(Entry entry, float longSide, float zoom) {
        float shortSide = longSide * Math.min(1.0f, zoom / fillZoom(entry));
        if (entry.width > 0 && entry.height > 0 && entry.width < entry.height) {
            return new float[] { shortSide, longSide };
        }
        return new float[] { longSide, shortSide };
    }

    private static void markFailed(Entry entry) {
        entry.failed = true;
        entry.requested = false;
        entry.bytes = null;
        entry.received = 0;
        entry.total = 0;
        entry.lastFailureTime = System.currentTimeMillis();
    }

    public static synchronized void ensureNetworkRegistered() {
        if (!networkRegistered) {
            try {
                maxTextureSize = Math.min(MAX_TEXTURE_SIZE, GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE));
            }
            catch (Throwable ignored) {
                maxTextureSize = MAX_TEXTURE_SIZE;
            }
            if (maxTextureSize <= 0) {
                maxTextureSize = 256;
            }
            OrienteeringMod.NETHANDLER.registerMessage(MsgMapImageData.MsgMIDHandler.class, MsgMapImageData.class, 9, Side.CLIENT);
            networkRegistered = true;
        }
    }

    private static Entry get(MapImageRef ref) {
        if (ref == null) {
            return null;
        }
        synchronized (ENTRIES) {
            return ENTRIES.get(ref.key());
        }
    }

    private static Entry getOrCreate(MapImageRef ref) {
        Entry entry = ENTRIES.get(ref.key());
        if (entry == null) {
            entry = new Entry();
            ENTRIES.put(ref.key(), entry);
        }
        return entry;
    }

    private static void decodeAsync(final String matchName, final String imageName, final byte[] data) {
        DECODER.execute(new Runnable() {
            @Override
            public void run() {
                BufferedImage image = null;
                try {
                    image = ImageIO.read(new ByteArrayInputStream(data));
                    if (image != null) {
                        image = prepareImage(image, maxTextureSize);
                    }
                }
                catch (Throwable e) {
                    LogManager.getLogger().warn("Failed to decode map image " + imageName + ": " + e.getMessage());
                }
                final BufferedImage decoded = image;
                Minecraft mc = Minecraft.func_71410_x();
                if (mc == null) {
                    return;
                }
                mc.func_152344_a(new Runnable() {
                    @Override
                    public void run() {
                        finishDecode(matchName, imageName, decoded);
                    }
                });
            }
        });
    }

    private static void finishDecode(String matchName, String imageName, BufferedImage image) {
        MapImageRef ref = new MapImageRef(matchName, imageName);
        synchronized (ENTRIES) {
            Entry existing = getOrCreate(ref);
            if (existing.failed) {
                existing.decoding = false;
                return;
            }
            if (image == null) {
                markFailed(existing);
                existing.decoding = false;
                return;
            }
        }
        try {
            Minecraft mc = Minecraft.func_71410_x();
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation location = mc.func_110434_K().func_110578_a(
                    "ort_map_" + NEXT_TEXTURE_ID.incrementAndGet(), texture);
            synchronized (ENTRIES) {
                Entry entry = getOrCreate(ref);
                entry.texture = location;
                entry.width = image.getWidth();
                entry.height = image.getHeight();
                entry.decoding = false;
            }
            LogManager.getLogger().info("Loaded map image " + matchName + "/" + imageName
                    + " (" + image.getWidth() + "x" + image.getHeight() + ").");
        }
        catch (Throwable e) {
            synchronized (ENTRIES) {
                Entry entry = getOrCreate(ref);
                markFailed(entry);
                entry.decoding = false;
            }
            LogManager.getLogger().warn("Failed to upload map image " + imageName + ": " + e.getMessage());
        }
    }

    private static BufferedImage prepareImage(BufferedImage source, int maxSize) {
        double scale = Math.min(1.0D, Math.min((double) maxSize / (double) source.getWidth(),
                (double) maxSize / (double) source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, width, height, null);
        }
        finally {
            graphics.dispose();
        }
        return result;
    }

    private static final class Entry {
        volatile ResourceLocation texture;
        volatile int width;
        volatile int height;
        volatile int rotation;
        volatile float zoom = MIN_ZOOM;
        volatile float centerU = 0.5f;
        volatile float centerV = 0.5f;
        volatile boolean requested;
        volatile boolean decoding;
        volatile boolean failed;
        long lastFailureTime;
        int total;
        int received;
        byte[] bytes;
    }
}