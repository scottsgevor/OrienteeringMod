package net.respawner.orienteeringmod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;

public final class MapImageService {
    public static final int CHUNK_SIZE = 128 * 1024;
    private static final int MAX_IMAGE_BYTES = 64 * 1024 * 1024;
    private static final Map<String, CachedImage> CACHE = new ConcurrentHashMap<String, CachedImage>();

    private MapImageService() {
    }

    public static boolean isSupportedImageName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    public static List<File> listImageFiles(String matchName) {
        List<File> result = new ArrayList<File>();
        if (matchName == null || matchName.isEmpty()) {
            return result;
        }
        File dir = new File(Util.getBasicDataPath(matchName), "maps");
        File[] files = dir.listFiles();
        if (files == null) {
            return result;
        }
        for (File file : files) {
            if (file.isFile() && file.length() > 0L && file.length() <= (long) MAX_IMAGE_BYTES
                    && isSupportedImageName(file.getName())) {
                result.add(file);
            }
        }
        result.sort(new Comparator<File>() {
            @Override
            public int compare(File left, File right) {
                return left.getName().compareToIgnoreCase(right.getName());
            }
        });
        return result;
    }

    public static int giveMaps(EntityPlayerMP player, String matchName, String courseName) {
        if (player == null || matchName == null) {
            return 0;
        }
        int removed = clearMapImages(player);
        if (courseName == null || courseName.isEmpty()) {
            syncInventory(player, removed > 0);
            return 0;
        }
        String prefix = courseName.toLowerCase(Locale.ROOT);
        int given = 0;
        for (File file : listImageFiles(matchName)) {
            if (!file.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                continue;
            }
            MapImageRef ref = new MapImageRef(matchName, file.getName());
            ItemStack stack = new ItemStack(OrienteeringMod.ModInit.mapImage);
            NBTTagCompound tag = new NBTTagCompound();
            tag.func_74778_a(MapImageRef.TAG_MATCH, ref.getMatch());
            tag.func_74778_a(MapImageRef.TAG_IMAGE, ref.getImage());
            stack.func_77982_d(tag);
            stack.func_151001_c(MapImageRef.displayName(file.getName()));
            player.field_71071_by.func_70441_a(stack);
            if (!stack.func_190926_b()) {
                player.func_71019_a(stack, false);
            }
            ++given;
        }
        syncInventory(player, removed > 0 || given > 0);
        if (given > 0) {
            Util.sendMessage(player, "You received " + given + " map image(s).");
        }
        return given;
    }

    private static int clearMapImages(EntityPlayerMP player) {
        int removed = 0;
        for (int i = 0; i < player.field_71071_by.func_70302_i_(); ++i) {
            ItemStack stack = player.field_71071_by.func_70301_a(i);
            if (!stack.func_190926_b() && stack.func_77973_b() == OrienteeringMod.ModInit.mapImage) {
                player.field_71071_by.func_70299_a(i, ItemStack.field_190927_a);
                ++removed;
            }
        }
        return removed;
    }

    private static void syncInventory(EntityPlayerMP player, boolean changed) {
        if (changed) {
            player.field_71070_bA.func_75142_b();
        }
    }

    public static void handleRequest(final EntityPlayerMP player, final String matchName, final String imageName) {
        if (player == null) {
            return;
        }
        player.field_71133_b.func_152344_a(new Runnable() {
            @Override
            public void run() {
                sendImage(player, matchName, imageName);
            }
        });
    }

    private static void sendImage(EntityPlayerMP player, String matchName, String imageName) {
        byte[] data = readImage(matchName, imageName);
        try {
            if (data == null || data.length == 0) {
                OrienteeringMod.NETHANDLER.sendTo(new MsgMapImageData(matchName, imageName, 0, 0, new byte[0]), player);
                return;
            }
            for (int offset = 0; offset < data.length; offset += CHUNK_SIZE) {
                int length = Math.min(CHUNK_SIZE, data.length - offset);
                byte[] chunk = Arrays.copyOfRange(data, offset, offset + length);
                OrienteeringMod.NETHANDLER.sendTo(new MsgMapImageData(matchName, imageName, data.length, offset, chunk), player);
            }
        }
        catch (Throwable e) {
            LogManager.getLogger().warn("Failed to send map image " + matchName + "/" + imageName + ": " + e.getMessage());
        }
    }

    private static byte[] readImage(String matchName, String imageName) {
        File file = resolveImage(matchName, imageName);
        if (file == null) {
            return null;
        }
        String key = matchName + "\n" + imageName;
        CachedImage cached = CACHE.get(key);
        if (cached != null && cached.lastModified == file.lastModified() && cached.length == file.length()) {
            return cached.data;
        }
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            if (data.length == 0 || data.length > MAX_IMAGE_BYTES) {
                return null;
            }
            CACHE.put(key, new CachedImage(data, file.lastModified(), file.length()));
            return data;
        }
        catch (IOException e) {
            LogManager.getLogger().warn("Failed to read map image " + file.getAbsolutePath() + ": " + e.getMessage());
            return null;
        }
    }

    private static File resolveImage(String matchName, String imageName) {
        if (matchName == null || imageName == null || matchName.isEmpty() || imageName.isEmpty()) {
            return null;
        }
        ORTWorld world = ORTWorld.getMatchInstanceServer();
        if (world == null || !matchName.equals(world.matchName)) {
            return null;
        }
        if (!imageName.equals(new File(imageName).getName()) || !isSupportedImageName(imageName)) {
            return null;
        }
        File dir = new File(Util.getBasicDataPath(matchName), "maps");
        File file = new File(dir, imageName);
        try {
            File canonicalDir = dir.getCanonicalFile();
            File canonicalFile = file.getCanonicalFile();
            if (!canonicalFile.getParentFile().equals(canonicalDir) || !canonicalFile.isFile()) {
                return null;
            }
            if (canonicalFile.length() <= 0L || canonicalFile.length() > (long) MAX_IMAGE_BYTES) {
                return null;
            }
            return canonicalFile;
        }
        catch (IOException e) {
            return null;
        }
    }

    private static final class CachedImage {
        final byte[] data;
        final long lastModified;
        final long length;

        CachedImage(byte[] data, long lastModified, long length) {
            this.data = data;
            this.lastModified = lastModified;
            this.length = length;
        }
    }
}