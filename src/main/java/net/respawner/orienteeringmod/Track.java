/*
 * Decompiled with CFR 0.152; reworked to make track export reliable.
 */
package net.respawner.orienteeringmod;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(value=Side.CLIENT)
public class Track {
    private static final String configPathName = "config" + File.separator + "orienteering";
    private static final int IMAGE_MARGIN = 2;
    private static final int MAX_IMAGE_DIMENSION = 8192;
    private static final Logger LOGGER = LogManager.getLogger("OrienteeringMod-Track");
    private static final Track track = new Track();

    private final List<double[]> points = new ArrayList<double[]>();
    private int minX = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int minZ = Integer.MAX_VALUE;
    private int maxZ = Integer.MIN_VALUE;
    public long previousTime = 0L;
    @Nullable
    private String matchName = null;
    private boolean exporting = false;

    public static Track getTrack() {
        return track;
    }

    public synchronized void load(String matchName) {
        this.matchName = matchName;
        ensureDirectory();
        loadPointsFromFile();
    }

    public synchronized void reset() {
        if (this.matchName == null) {
            return;
        }
        ensureDirectory();
        File trackFile = new File(this.getTrackFileName(this.matchName));
        try {
            if (trackFile.exists() && !trackFile.delete()) {
                LOGGER.warn("Could not delete old track file: " + trackFile.getAbsolutePath());
            }
            if (!trackFile.createNewFile() && !trackFile.exists()) {
                LOGGER.warn("Could not create track file: " + trackFile.getAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.error("Could not reset track file: " + trackFile.getAbsolutePath(), e);
        }
        this.points.clear();
        this.resetBounds();
        this.previousTime = 0L;
        this.saveConfig();
    }

    public synchronized void update(double x, double z, long time) {
        if (this.matchName == null || !Double.isFinite(x) || !Double.isFinite(z)) {
            return;
        }
        ensureDirectory();
        this.points.add(new double[]{x, z});
        this.updateBounds(x, z);
        this.previousTime = time;
        File trackFile = new File(this.getTrackFileName(this.matchName));
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(trackFile, true))) {
            out.writeDouble(x);
            out.writeDouble(z);
        } catch (IOException e) {
            LOGGER.error("Could not append track point to " + trackFile.getAbsolutePath(), e);
        }
        this.saveConfig();
    }

    public synchronized void setMatchName(String name) {
        this.matchName = name;
        ensureDirectory();
    }

    public void export() {
        final String name;
        final List<double[]> snapshot;
        synchronized (this) {
            if (this.matchName == null || this.matchName.isEmpty()) {
                LOGGER.warn("Cannot export track: no match name.");
                return;
            }
            if (this.exporting) {
                LOGGER.warn("Cannot export track: another export is already running.");
                return;
            }
            if (this.points.isEmpty()) {
                this.loadPointsFromFile();
            }
            if (this.points.isEmpty()) {
                LOGGER.warn("Cannot export track for " + this.matchName + ": no track points recorded.");
                return;
            }
            name = this.matchName;
            snapshot = new ArrayList<double[]>(this.points.size());
            for (double[] point : this.points) {
                snapshot.add(new double[]{point[0], point[1]});
            }
            this.exporting = true;
        }

        Thread thread = new Thread(() -> {
            try {
                exportSnapshot(name, snapshot);
            } catch (Throwable t) {
                LOGGER.error("Failed to export track for " + name, t);
            } finally {
                synchronized (Track.this) {
                    Track.this.exporting = false;
                }
            }
        }, "Orienteering-Track-Export");
        thread.setDaemon(true);
        thread.start();
    }

    private static void exportSnapshot(String matchName, List<double[]> points) throws IOException {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (double[] point : points) {
            int x = (int)Math.floor(point[0]);
            int z = (int)Math.floor(point[1]);
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (z < minZ) {
                minZ = z;
            }
            if (z > maxZ) {
                maxZ = z;
            }
        }
        int width = maxX - minX + 1 + IMAGE_MARGIN * 2;
        int height = maxZ - minZ + 1 + IMAGE_MARGIN * 2;
        if (width <= 0 || height <= 0) {
            throw new IOException("Invalid track bounds: " + width + "x" + height);
        }

        double scale = 1.0D;
        int largest = Math.max(width, height);
        if (largest > MAX_IMAGE_DIMENSION) {
            scale = MAX_IMAGE_DIMENSION / (double)largest;
        }
        int imageWidth = Math.max(1, (int)Math.ceil(width * scale));
        int imageHeight = Math.max(1, (int)Math.ceil(height * scale));
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

        double previousX = points.get(0)[0];
        double previousZ = points.get(0)[1];
        for (int i = 0; i < points.size(); ++i) {
            double x = points.get(i)[0];
            double z = points.get(i)[1];
            double speed = i == 0 ? 0.0D : Math.sqrt((x - previousX) * (x - previousX) + (z - previousZ) * (z - previousZ)) * 2.0D;
            int px = (int)Math.round((x - minX + IMAGE_MARGIN) * scale);
            int pz = (int)Math.round((z - minZ + IMAGE_MARGIN) * scale);
            int radius = scale >= 1.0D ? 1 : 0;
            int color = speedToARGB(speed);
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    int ix = px + dx;
                    int iz = pz + dz;
                    if (ix >= 0 && ix < imageWidth && iz >= 0 && iz < imageHeight) {
                        image.setRGB(ix, iz, color);
                    }
                }
            }
            previousX = x;
            previousZ = z;
        }

        String safeName = sanitizeFileName(matchName);
        File directory = new File(Util.getRootPath());
        if (!directory.isDirectory() && !directory.mkdirs() && !directory.isDirectory()) {
            throw new IOException("Could not create output directory: " + directory.getAbsolutePath());
        }
        File output = new File(directory, safeName + "_track.png");
        File temporary = new File(directory, safeName + "_track.png.tmp");
        if (!ImageIO.write(image, "png", temporary)) {
            throw new IOException("No PNG writer available.");
        }
        image.flush();
        Files.move(temporary.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
        LOGGER.info("Exported track to " + output.getAbsolutePath());
    }

    private static String sanitizeFileName(String name) {
        String safe = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
        return safe.isEmpty() ? "track" : safe;
    }

    private static int speedToARGB(double speed) {
        if ((int)speed >= 12) {
            return -16711681;
        }
        if ((int)speed >= 8) {
            return -16711936 + (int)((12.0 - speed) * 255.0 / 4.0);
        }
        if ((int)speed >= 4) {
            return -16777216 + ((int)((8.0 - speed) * 255.0 / 4.0) << 16) + 65280;
        }
        return -65536 + ((int)(speed * 255.0 / 4.0) << 8);
    }

    private void loadPointsFromFile() {
        this.points.clear();
        this.resetBounds();
        if (this.matchName == null) {
            return;
        }
        File trackFile = new File(this.getTrackFileName(this.matchName));
        if (!trackFile.isFile()) {
            return;
        }
        try (DataInputStream in = new DataInputStream(new FileInputStream(trackFile))) {
            while (true) {
                double x = in.readDouble();
                double z = in.readDouble();
                if (!Double.isFinite(x) || !Double.isFinite(z)) {
                    continue;
                }
                this.points.add(new double[]{x, z});
                this.updateBounds(x, z);
            }
        } catch (EOFException e) {
            // Normal end of file, or a partially written final point.
        } catch (IOException e) {
            LOGGER.error("Could not read track file: " + trackFile.getAbsolutePath(), e);
        }
    }

    private void resetBounds() {
        this.minX = Integer.MAX_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.minZ = Integer.MAX_VALUE;
        this.maxZ = Integer.MIN_VALUE;
    }

    private void updateBounds(double x, double z) {
        int ix = (int)Math.floor(x);
        int iz = (int)Math.floor(z);
        if (ix < this.minX) {
            this.minX = ix;
        }
        if (ix > this.maxX) {
            this.maxX = ix;
        }
        if (iz < this.minZ) {
            this.minZ = iz;
        }
        if (iz > this.maxZ) {
            this.maxZ = iz;
        }
    }

    private static boolean ensureDirectory() {
        File directory = new File(configPathName);
        if (directory.isDirectory()) {
            return true;
        }
        if (directory.mkdirs() || directory.isDirectory()) {
            return true;
        }
        LOGGER.error("Could not create track data directory: " + directory.getAbsolutePath());
        return false;
    }

    private void saveConfig() {
        if (this.matchName == null || !ensureDirectory()) {
            return;
        }
        File configFile = new File(this.getConfigFileName(this.matchName));
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(configFile))) {
            out.writeInt(this.minX);
            out.writeInt(this.maxX);
            out.writeInt(this.minZ);
            out.writeInt(this.maxZ);
            out.writeLong(this.previousTime);
        } catch (IOException e) {
            LOGGER.error("Could not save track bounds to " + configFile.getAbsolutePath(), e);
        }
    }

    private String getTrackFileName(String matchName) {
        return configPathName + File.separator + sanitizeFileName(matchName) + "_track.cfg";
    }

    private String getConfigFileName(String matchName) {
        return configPathName + File.separator + sanitizeFileName(matchName) + "_config.cfg";
    }
}