package net.respawner.orienteeringmod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class MapImageHands {
    private MapImageHands() {
    }

    public static List<MapImageRef> held(EntityPlayer player) {
        List<MapImageRef> result = new ArrayList<MapImageRef>();
        if (player == null) {
            return result;
        }
        Map<String, MapImageRef> unique = new LinkedHashMap<String, MapImageRef>();
        add(unique, player.func_184586_b(EnumHand.MAIN_HAND));
        add(unique, player.func_184586_b(EnumHand.OFF_HAND));
        result.addAll(unique.values());
        return result;
    }

    public static MapImageRef primary(EntityPlayer player) {
        if (player == null) {
            return null;
        }
        MapImageRef ref = MapImageRef.fromStack(player.func_184586_b(EnumHand.MAIN_HAND));
        if (ref != null) {
            return ref;
        }
        return MapImageRef.fromStack(player.func_184586_b(EnumHand.OFF_HAND));
    }

    public static MapImageRef primaryTwoHanded(EntityPlayer player) {
        if (player == null) {
            return null;
        }
        MapImageRef main = MapImageRef.fromStack(player.func_184586_b(EnumHand.MAIN_HAND));
        if (main == null || !player.func_184586_b(EnumHand.OFF_HAND).func_190926_b()) {
            return null;
        }
        return main;
    }

    public static boolean isTwoHanded(EntityPlayer player, MapImageRef wanted) {
        MapImageRef main = primaryTwoHanded(player);
        return main != null && wanted != null
                && main.getMatch().equals(wanted.getMatch())
                && main.getImage().equals(wanted.getImage());
    }

    public static boolean holds(EntityPlayer player, MapImageRef wanted) {
        if (wanted == null) {
            return false;
        }
        for (MapImageRef ref : held(player)) {
            if (ref.getMatch().equals(wanted.getMatch()) && ref.getImage().equals(wanted.getImage())) {
                return true;
            }
        }
        return false;
    }

    private static void add(Map<String, MapImageRef> unique, ItemStack stack) {
        MapImageRef ref = MapImageRef.fromStack(stack);
        if (ref != null) {
            unique.put(ref.key(), ref);
        }
    }
}