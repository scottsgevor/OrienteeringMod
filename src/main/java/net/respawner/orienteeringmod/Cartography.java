/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public class Cartography {
    private int NW_X = 0;
    private int NW_Z = 0;
    private int SE_X = 0;
    private int SE_Z = 0;
    private int maxAirGap = 0;
    private int decrease = 0;
    private Set<String> displayingGap = new HashSet<String>();
    private Set<String> penetrate = new HashSet<String>();
    private Set<String> emph = new HashSet<String>();
    private Map<String, Integer> ground = new HashMap<String, Integer>();
    private Map<String, Integer> foliage = new HashMap<String, Integer>();
    private static Thread thread = null;

    @SideOnly(value=Side.CLIENT)
    public static synchronized String sketch(World world, EntityPlayer player) {
        if (thread != null) {
            return "Another sketching mission is running.";
        }
        Cartography instance = new Cartography();
        try {
            instance.importFromFile(world.func_72912_H().func_76065_j());
            LogManager.getLogger().info("Mapping info successfully imported from file.");
            thread = new Thread(instance.new Draw(world, player), "Orienteering map export");
            thread.setDaemon(true);
            thread.start();
        }
        catch (Throwable e) {
            thread = null;
            LogManager.getLogger().error("Cannot start mapping.", e);
            return "Cannot start mapping: " + e.getMessage();
        }
        return "Mapping started. Wait for the export-complete message.";
    }

    private void importFromFile(String worldName) throws ORTFileException {
        JsonParser parser = new JsonParser();
        try (FileReader reader = new FileReader(Util.getBasicDataPath(worldName) + "cartography.json")) {
            JsonObject json = (JsonObject)parser.parse(reader);
            JsonArray jsonNW = json.get("regionNW").getAsJsonArray();
            this.NW_X = jsonNW.get(0).getAsInt();
            this.NW_Z = jsonNW.get(1).getAsInt();
            JsonArray jsonSE = json.get("regionSE").getAsJsonArray();
            this.SE_X = jsonSE.get(0).getAsInt();
            this.SE_Z = jsonSE.get(1).getAsInt();
            this.maxAirGap = json.get("maxairgap").getAsInt();
            this.decrease = json.has("decrease") ? json.get("decrease").getAsInt() : 0;
            if (NW_X > SE_X || NW_Z > SE_Z || maxAirGap < 1) {
                throw new IllegalArgumentException("regionNW must be <= regionSE on both axes; maxairgap must be positive.");
            }
            JsonArray jsonDisplayGap = json.get("displaygap").getAsJsonArray();
            for (int i = 0; i < jsonDisplayGap.size(); ++i) {
                this.displayingGap.add(jsonDisplayGap.get(i).getAsString());
            }
            JsonArray jsonEmph = json.get("emph").getAsJsonArray();
            for (int i = 0; i < jsonEmph.size(); ++i) {
                this.emph.add(jsonEmph.get(i).getAsString());
            }
            JsonArray jsonGround = json.get("ground").getAsJsonArray();
            for (int j = 0; j < jsonGround.size(); ++j) {
                JsonObject item = jsonGround.get(j).getAsJsonObject();
                this.ground.put(item.get("material").getAsString(), this.makeRGB(item.get("R").getAsInt(), item.get("G").getAsInt(), item.get("B").getAsInt()));
            }
            JsonArray jsonPenetrate = json.get("penetrate").getAsJsonArray();
            for (int k = 0; k < jsonPenetrate.size(); ++k) {
                this.penetrate.add(jsonPenetrate.get(k).getAsString());
            }
            this.penetrate.add("minecraft:air");
            JsonArray jsonFoliage = json.get("foliage").getAsJsonArray();
            for (int l = 0; l < jsonFoliage.size(); ++l) {
                JsonObject item = jsonFoliage.get(l).getAsJsonObject();
                this.foliage.put(item.get("material").getAsString(),
                    this.makeRGB(item.get("R").getAsInt(), item.get("G").getAsInt(),
                                 item.get("B").getAsInt()));
            }
            if (!foliage.containsKey("minecraft:leaves2")
                && foliage.containsKey("minecraft:leaves")) {
                foliage.put("minecraft:leaves2", foliage.get("minecraft:leaves"));
            }
        }
        catch (Throwable e) {
            if (e instanceof ORTFileException) {
                throw (ORTFileException)e;
            }
            LogManager.getLogger().error(e.toString());
            throw new ORTFileException(ORTFileException.FileType.Cartography, ORTFileException.IOType.Read, e.getMessage());
        }
    }

    private int makeRGB(int R, int G, int B) {
        return 0xFF000000 | R << 16 | G << 8 | B;
    }

    private class Draw implements Runnable {
        private final World world;
        private final EntityPlayer player;
        private final net.minecraft.server.MinecraftServer server;
        private final String outputPath;

        Draw(World world, EntityPlayer player) {
            this.world = world;
            this.player = player;
            this.server = world.func_73046_m();
            this.outputPath = Util.getBasicDataPath(world.func_72912_H().func_76065_j());
            if (server == null) {
                throw new IllegalArgumentException("Run sketch in a local world with an integrated server.");
            }
        }

        private boolean worldIsRunning() {
            return server.func_71278_l()
                && server.func_71218_a(world.field_73011_w.getDimension()) == world;
        }

        private void onServer(Runnable task) throws Exception {
            java.util.concurrent.Future<?> future = server.func_152344_a(() -> {
                if (worldIsRunning()) {
                    task.run();
                }
            });
            try {
                while (true) {
                    if (!worldIsRunning()) {
                        throw new InterruptedException("World closed during mapping.");
                    }
                    try {
                        future.get(1, java.util.concurrent.TimeUnit.SECONDS);
                        return;
                    } catch (java.util.concurrent.TimeoutException waiting) {
                        // Check shutdown again while waiting for the server thread.
                    }
                }
            } finally {
                future.cancel(false);
            }
        }

        @Override
        public void run() {
            try {
                int firstX = Math.floorDiv(NW_X, 16);
                int firstZ = Math.floorDiv(NW_Z, 16);
                int lastX = Math.floorDiv(SE_X, 16);
                int lastZ = Math.floorDiv(SE_Z, 16);
                int width = Math.multiplyExact(lastX - firstX + 1, 16);
                int height = Math.multiplyExact(lastZ - firstZ + 1, 16);
                BufferedImage imgGround = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                BufferedImage imgFoliage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                int[][] heights = new int[height][width];
                for (int[] row : heights) {
                    java.util.Arrays.fill(row, -1);
                }
                int total = Math.multiplyExact(lastX - firstX + 1, lastZ - firstZ + 1);
                int completed = 0;
                for (int i = firstX; i <= lastX; i++) {
                    for (int j = firstZ; j <= lastZ; j++) {
                        final int chunkX = i;
                        final int chunkZ = j;
                        onServer(() -> scanChunk(chunkX, chunkZ, firstX, firstZ,
                                                imgGround, imgFoliage, heights));
                        completed++;
                        if (completed % 100 == 0 || completed == total) {
                            final String message = "Mapping: " + (completed * 100L / total)
                                + "% (" + completed + "/" + total + ")";
                            onServer(() -> Util.sendMessage(player, message));
                        }
                        // Allow the normal server tick to proceed between chunk reads.
                        Thread.sleep(5);
                    }
                }
                ContourRenderer.overlay(imgFoliage, heights);
                if (!worldIsRunning()) {
                    throw new InterruptedException("World closed during mapping.");
                }
                writeImage(imgGround, "ground.png");
                writeImage(imgFoliage, "foliage.png");
                LogManager.getLogger().info("ground.png and foliage.png exported.");
                try {
                    writeImage(HeightMapExporter.render(heights, firstX * 16, firstZ * 16),
                               "ground-height.png");
                    HeightMapExporter.writeCsv(
                        new File(outputPath + "ground-height.csv").toPath(),
                        heights, firstX * 16, firstZ * 16);
                    onServer(() -> Util.sendMessage(player,
                        "Export complete: ground.png, foliage.png, ground-height.png and ground-height.csv."));
                } catch (Exception heightError) {
                    LogManager.getLogger().error("Height export failed; ground.png and foliage.png were saved.",
                                                 heightError);
                    onServer(() -> Util.sendMessage(player,
                        "ground.png and foliage.png saved. Height export failed; see game log."));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LogManager.getLogger().info("Mapping cancelled: " + e.getMessage());
            } catch (Throwable e) {
                LogManager.getLogger().error("Mapping failed.", e);
                server.func_152344_a(() -> {
                    if (worldIsRunning()) {
                        Util.sendMessage(player, "Mapping failed. See the game log for details.");
                    }
                });
            } finally {
                synchronized (Cartography.class) {
                    if (thread == Thread.currentThread()) {
                        thread = null;
                    }
                }
            }
        }

        private void writeImage(BufferedImage image, String name) throws IOException {
            java.nio.file.Path target = new File(outputPath + name).toPath();
            java.nio.file.Path temp = java.nio.file.Files.createTempFile(target.getParent(), "mapping-", ".png");
            try {
                if (!ImageIO.write(image, "png", temp.toFile())) {
                    throw new IOException("PNG writer unavailable.");
                }
                java.nio.file.Files.move(temp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } finally {
                java.nio.file.Files.deleteIfExists(temp);
                image.flush();
            }
        }

        private boolean isVegetation(String name) {
            return foliage.containsKey(name)
                || name.equals("minecraft:leaves") || name.equals("minecraft:leaves2")
                || name.equals("minecraft:log") || name.equals("minecraft:log2")
                || name.equals("minecraft:tallgrass") || name.equals("minecraft:double_plant")
                || name.equals("minecraft:vine");
        }

        private void scanChunk(int chunkX, int chunkZ, int firstX, int firstZ,
                               BufferedImage imgGround, BufferedImage imgFoliage, int[][] heights) {
            Chunk chunk = world.func_72964_e(chunkX, chunkZ);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int pixelX = (chunkX - firstX) * 16 + x;
                    int pixelZ = (chunkZ - firstZ) * 16 + z;
                    int airCount = 0;
                    int lastGap = 0;
                    String lastNonAir = "";
                    int y;
                    for (y = 0; y < Math.min(chunk.func_76611_b(x, z) + 5, 256); y++) {
                        String name = chunk.func_186032_a(x, y, z).func_177230_c().getRegistryName().toString();
                        if (emph.contains(name)) {
                            lastNonAir = name;
                            lastGap = 0;
                            break;
                        }
                        if (penetrate.contains(name)) {
                            airCount++;
                        } else {
                            lastNonAir = name;
                            if (airCount > 0) {
                                lastGap = airCount;
                            }
                            airCount = 0;
                        }
                        if (airCount >= maxAirGap) {
                            break;
                        }
                    }
                    final int localX = x;
                    final int localZ = z;
                    int highestBlock = Math.min(chunk.func_76611_b(x, z) + 4, 255);
                    heights[pixelZ][pixelX] = SurfaceHeight.find(highestBlock,
                        level -> chunk.func_186032_a(localX, level, localZ)
                            .func_177230_c().getRegistryName().toString(),
                        name -> !penetrate.contains(name) && !isVegetation(name));
                    Integer rgb = ground.get(lastNonAir);
                    if (rgb != null) {
                        if (displayingGap.contains(lastNonAir)) {
                            rgb = makeRGBWithDepth(rgb, lastGap);
                        }
                        imgGround.setRGB(pixelX, pixelZ, rgb);
                    }
                    String topName = "";
                    int topY;
                    for (topY = Math.min(chunk.func_76611_b(x, z) + 4, 255); topY >= 0; topY--) {
                        topName = chunk.func_186032_a(x, topY, z).func_177230_c().getRegistryName().toString();
                        if (!penetrate.contains(topName)) {
                            break;
                        }
                    }
                    Integer topColor = foliage.get(topName);
                    if (topColor != null && topY > heights[pixelZ][pixelX]) {
                        imgFoliage.setRGB(pixelX, pixelZ, topColor);
                    } else {
                        topColor = ground.get(topName);
                        if (topColor != null) {
                            imgFoliage.setRGB(pixelX, pixelZ, topColor);
                        }
                    }
                }
            }
        }

        private int makeRGBWithDepth(int rgb, int depth) {
            return 0xFF000000
                | Math.max((rgb >> 16 & 255) - decrease * depth, 0) << 16
                | Math.max((rgb >> 8 & 255) - decrease * depth, 0) << 8
                | Math.max((rgb & 255) - decrease * depth, 0);
        }
    }
}
