/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
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
    public static String sketch(World world, EntityPlayer player) {
        Cartography instance = new Cartography();
        try {
            instance.importFromFile(world.func_72912_H().func_76065_j());
            LogManager.getLogger().info("Mapping info successfully imported from file.");
            if (thread != null) {
                return "Another sketching mission is running.";
            }
            Cartography cartography = instance;
            cartography.getClass();
            thread = new Thread(cartography.new Draw(world, player));
            thread.start();
        }
        catch (Throwable e) {
            LogManager.getLogger().error(e.toString());
            return "Error: action Failed. Unknown Reason.";
        }
        return "Success. See \"ground.png\" and \"foliage.png\".";
    }

    private void importFromFile(String worldName) throws ORTFileException {
        JsonParser parser = new JsonParser();
        try {
            JsonObject json = (JsonObject)parser.parse(new FileReader(Util.getBasicDataPath(worldName) + "cartography.json"));
            JsonArray jsonNW = json.get("regionNW").getAsJsonArray();
            this.NW_X = jsonNW.get(0).getAsInt();
            this.NW_Z = jsonNW.get(1).getAsInt();
            JsonArray jsonSE = json.get("regionSE").getAsJsonArray();
            this.SE_X = jsonSE.get(0).getAsInt();
            this.SE_Z = jsonSE.get(1).getAsInt();
            this.maxAirGap = json.get("maxairgap").getAsInt();
            this.decrease = json.get("decrease").getAsInt();
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
                this.foliage.put(item.get("material").getAsString(), this.makeRGB(item.get("R").getAsInt(), item.get("G").getAsInt(), item.get("B").getAsInt()));
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

    private class Draw
    implements Runnable {
        World world;
        EntityPlayer player;

        public Draw(World world, EntityPlayer player) {
            this.world = world;
            this.player = player;
        }

        @Override
        public void run() {
            try {
                int x1 = Cartography.this.NW_X;
                int z1 = Cartography.this.NW_Z;
                int x2 = Cartography.this.SE_X;
                int z2 = Cartography.this.SE_Z;
                BufferedImage imgGround = new BufferedImage(x2 / 16 * 16 - x1 / 16 * 16 + 16, z2 / 16 * 16 - z1 / 16 * 16 + 16, 2);
                BufferedImage imgFoliage = new BufferedImage(x2 / 16 * 16 - x1 / 16 * 16 + 16, z2 / 16 * 16 - z1 / 16 * 16 + 16, 2);
                int totalChunks = (x2 / 16 - x1 / 16 + 1) * (z2 / 16 - z1 / 16 + 1);
                int turn = 0;
                for (int i = x1 / 16; i <= x2 / 16; ++i) {
                    for (int j = z1 / 16; j <= z2 / 16; ++j) {
                        int r = i * 16;
                        int s = j * 16;
                        Chunk chunk = this.world.func_72964_e(i, j);
                        for (int x = 0; x < 16; ++x) {
                            for (int z = 0; z < 16; ++z) {
                                Integer rgb2;
                                int y2;
                                Integer rgb;
                                int y;
                                int airCount = 0;
                                int lastGap = 0;
                                String lastNonAir = "";
                                for (y = 0; y < Math.min(chunk.func_76611_b(x, z) + 5, 256); ++y) {
                                    String name = chunk.func_186032_a(x, y, z).func_177230_c().getRegistryName().toString();
                                    if (Cartography.this.emph.contains(name)) {
                                        lastNonAir = name;
                                        lastGap = 0;
                                        break;
                                    }
                                    if (Cartography.this.penetrate.contains(name)) {
                                        ++airCount;
                                    } else {
                                        lastNonAir = name;
                                        if (airCount > 0) {
                                            lastGap = airCount;
                                        }
                                        airCount = 0;
                                    }
                                    if (airCount >= Cartography.this.maxAirGap) break;
                                }
                                if ((rgb = (Integer)Cartography.this.ground.get(lastNonAir)) != null) {
                                    if (Cartography.this.displayingGap.contains(lastNonAir)) {
                                        rgb = this.makeRGBWithDepth(rgb, lastGap);
                                    }
                                    imgGround.setRGB(r + x - x1 / 16 * 16, s + z - z1 / 16 * 16, rgb);
                                }
                                String name2 = "";
                                for (y2 = Math.min(chunk.func_76611_b(x, z) + 4, 255); y2 >= 0; --y2) {
                                    name2 = chunk.func_186032_a(x, y2, z).func_177230_c().getRegistryName().toString();
                                    if (!Cartography.this.penetrate.contains(name2)) break;
                                }
                                if ((rgb2 = (Integer)Cartography.this.foliage.get(name2)) != null && y2 > y) {
                                    imgFoliage.setRGB(r + x - x1 / 16 * 16, s + z - z1 / 16 * 16, rgb2);
                                    continue;
                                }
                                Integer rgb3 = (Integer)Cartography.this.ground.get(name2);
                                if (rgb3 == null) continue;
                                imgFoliage.setRGB(r + x - x1 / 16 * 16, s + z - z1 / 16 * 16, rgb3);
                            }
                        }
                        if (++turn % 1000 != 0) continue;
                        int percent = (int)((float)turn * 100.0f / (float)totalChunks);
                        Util.sendMessage(this.player, "Export progress: 0." + percent + " (" + turn + "/" + totalChunks + ")");
                    }
                }
                File fileGround = new File(Util.getBasicDataPath(this.world.func_72912_H().func_76065_j()) + "ground.png");
                if (fileGround.exists()) {
                    fileGround.delete();
                }
                fileGround.createNewFile();
                FileOutputStream outGround = new FileOutputStream(fileGround);
                ImageIO.write((RenderedImage)imgGround, "png", outGround);
                imgGround.flush();
                outGround.flush();
                outGround.close();
                File fileFoliage = new File(Util.getBasicDataPath(this.world.func_72912_H().func_76065_j()) + "foliage.png");
                if (fileFoliage.exists()) {
                    fileFoliage.delete();
                }
                fileFoliage.createNewFile();
                FileOutputStream outFoliage = new FileOutputStream(fileFoliage);
                ImageIO.write((RenderedImage)imgFoliage, "png", outFoliage);
                imgFoliage.flush();
                outFoliage.flush();
                outFoliage.close();
            }
            catch (IOException e) {
                LogManager.getLogger().error("Failed to export the graphs due to file writting.");
            }
            LogManager.getLogger().info("Graphs have been exported.");
            Util.sendMessage(this.player, "The graphs have been successfully exported.");
            thread = null;
        }

        private int makeRGBWithDepth(int rgb, int depth) {
            if (depth == 0) {
                return rgb;
            }
            return 0xFF000000 | Math.max((rgb >> 16 & 0xFF) - Cartography.this.decrease * depth, 0) << 16 | Math.max((rgb >> 8 & 0xFF) - Cartography.this.decrease * depth, 0) << 8 | Math.max((rgb & 0xFF) - Cartography.this.decrease * depth, 0);
        }
    }
}

