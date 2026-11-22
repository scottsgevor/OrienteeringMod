package net.respawner.orienteeringmod;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class MapImageRef {
    public static final String TAG_MATCH = "ORTMapMatch";
    public static final String TAG_IMAGE = "ORTMapImage";

    private final String match;
    private final String image;

    public MapImageRef(String match, String image) {
        this.match = match;
        this.image = image;
    }

    public String getMatch() {
        return this.match;
    }

    public String getImage() {
        return this.image;
    }

    public String key() {
        return this.match + "\n" + this.image;
    }

    public static MapImageRef fromStack(ItemStack stack) {
        if (stack == null || stack.func_190926_b() || !(stack.func_77973_b() instanceof ItemMapImage)) {
            return null;
        }
        NBTTagCompound tag = stack.func_77978_p();
        if (tag == null || !tag.func_74764_b(TAG_MATCH) || !tag.func_74764_b(TAG_IMAGE)) {
            return null;
        }
        String match = tag.func_74779_i(TAG_MATCH);
        String image = tag.func_74779_i(TAG_IMAGE);
        if (match.isEmpty() || image.isEmpty()) {
            return null;
        }
        return new MapImageRef(match, image);
    }

    public static String displayName(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}