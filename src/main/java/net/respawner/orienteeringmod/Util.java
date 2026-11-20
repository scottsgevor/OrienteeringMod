/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.io.File;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.respawner.orienteeringmod.BlockCPEnd;
import net.respawner.orienteeringmod.BlockCPStart;
import net.respawner.orienteeringmod.BlockCheckpoint;
import net.respawner.orienteeringmod.BlockMainStation;
import net.respawner.orienteeringmod.NonOriginal;
import org.apache.logging.log4j.LogManager;

public final class Util {
    public static final int CODE_END = -1;
    public static final String STR_ORIENTEERING = "orienteering";
    public static final String STR_MATCH_FILE = "match";
    public static final String MSG_OUT_OF_MATCH = "The current world does not have a match.";
    public static final String MSG_ACTION_FAILED = "Error: action Failed. Unknown Reason.";
    public static final String MSG_NOT_ADMIN = "Only admin can use this command.";
    public static final String MSG_NOT_REGISTERED = "Action failed. You are not registered in the player roster.";

    private Util() {
    }

    public static void sendMessage(EntityPlayer player, String str) {
        player.func_145747_a(new TextComponentTranslation(str, new Object[0]));
    }

    public static boolean isORTBLock(IBlockState state) {
        Block block = state.func_177230_c();
        if (block instanceof BlockCheckpoint) {
            return true;
        }
        if (block instanceof BlockCPStart) {
            return true;
        }
        if (block instanceof BlockCPEnd) {
            return true;
        }
        return block instanceof BlockMainStation;
    }

    public static String timeTranslation(long time) {
        String str = "";
        if (time == 0L) {
            str = "00";
            return str;
        }
        int time2 = (int)time;
        int msec = time2 % 10;
        time2 = (time2 - msec) / 10;
        int sec = time2 % 60;
        time2 = (time2 - sec) / 60;
        int min = time2 % 60;
        time2 = (time2 - min) / 60;
        str = str + Util.timeDigitsTranslation(time2);
        str = str + ":";
        str = str + Util.timeDigitsTranslation(min);
        str = str + ":";
        str = str + Util.timeDigitsTranslation(sec);
        str = str + ".";
        str = str + msec;
        return str;
    }

    private static String timeDigitsTranslation(int a) {
        String str = "";
        if (a < 10) {
            str = str + "0";
        }
        str = str + a;
        return str;
    }

    public static BlockPos randomRespawn(World world, double x, double y, double z, int radius) {
        int i;
        int x1 = 0;
        int y1 = 0;
        int z1 = 0;
        Random r = new Random(1L);
        for (i = 0; i < 5000 && (y1 = Util.tryRespawn(world, (double)(x1 = (int)x - radius + r.nextInt(radius * 2)) + 0.5, (double)(z1 = (int)z - radius + r.nextInt(radius * 2)) + 0.5)) <= 0; ++i) {
        }
        boolean a = false;
        LogManager.getLogger().debug("#ORT Debug: respawn point (" + x1 + ", " + y1 + ", " + z1 + "). Tried " + i + " times.");
        return new BlockPos((double)x1 + 0.5, (double)y1, (double)z1 + 0.5);
    }

    private static int tryRespawn(World world, double x, double z) {
        int y = 0;
        while (!(Util.isAir(world, x, y, z) && Util.isAir(world, x, y + 1, z) && Util.isPermitted(world, x, y - 1, z))) {
            if (y >= 255) {
                return 0;
            }
            ++y;
        }
        int faceCount = 0;
        if (!Util.isAir(world, x + 1.0, y + 1, z)) {
            ++faceCount;
        }
        if (!Util.isAir(world, x - 1.0, y + 1, z)) {
            ++faceCount;
        }
        if (!Util.isAir(world, x, y + 1, z + 1.0)) {
            ++faceCount;
        }
        if (!Util.isAir(world, x, y + 1, z - 1.0)) {
            ++faceCount;
        }
        if (Util.isAir(world, x + 1.0, y - 2, z)) {
            ++faceCount;
        }
        if (Util.isAir(world, x - 1.0, y - 2, z)) {
            ++faceCount;
        }
        if (Util.isAir(world, x, y - 2, z + 1.0)) {
            ++faceCount;
        }
        if (Util.isAir(world, x, y - 2, z - 1.0)) {
            ++faceCount;
        }
        if (faceCount > 1) {
            return 0;
        }
        return y;
    }

    private static boolean isAir(World world, double x, double y, double z) {
        if (y >= 255.0) {
            return true;
        }
        return Block.func_149682_b(world.func_180495_p(new BlockPos(x, y, z)).func_177230_c()) == 0;
    }

    @NonOriginal
    private static boolean isPermitted(World world, double x, double y, double z) {
        if (y < 1.0) {
            return false;
        }
        IBlockState state = world.func_180495_p(new BlockPos(x, y, z));
        Block block = state.func_177230_c();
        if (state.func_185904_a().func_76224_d()) {
            return false;
        }
        if (block instanceof BlockStone) {
            return false;
        }
        if (block instanceof BlockWeb) {
            return false;
        }
        if (block instanceof BlockOre) {
            return false;
        }
        if (block instanceof BlockGravel) {
            return false;
        }
        return !(block instanceof BlockAir);
    }

    public static String getRootPath() {
        String path = ".orienteering" + File.separator;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static String getBasicDataPath(String matchName) {
        String path = Util.getRootPath() + matchName + File.separator;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static String getPlayerDataPath(String matchName) {
        String path = Util.getBasicDataPath(matchName) + "players" + File.separator;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static String getCourseDataPath(String matchName) {
        String path = Util.getBasicDataPath(matchName) + "courses" + File.separator;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static String getGradeDataPath(String matchName) {
        String path = Util.getBasicDataPath(matchName) + "grades" + File.separator;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static String getMatchFilePath(String matchName) {
        return Util.getBasicDataPath(matchName) + STR_MATCH_FILE;
    }
}

