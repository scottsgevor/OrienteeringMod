/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.OrienteeringMod;

public class MatchLoading {
    @SideOnly(value=Side.CLIENT)
    public static void LoadMatchClient(IntegratedServer server) throws ORTFileException {
        LoadMatchClient(server, server.func_71221_J());
    }

    @SideOnly(value=Side.CLIENT)
    public static void LoadMatchClient(IntegratedServer server, String matchName) throws ORTFileException {
        try {
            ORTWorld.loadMatch(matchName, true);
        }
        catch (ORTFileException e) {
            ORTWorld.setNull();
            throw e;
        }
        MatchLoading.setWorldRules(server.func_71218_a(0));
    }

    @SideOnly(value=Side.SERVER)
    public static void LoadMatchServer(DedicatedServer server, String matchName) throws ORTFileException {
        try {
            ORTWorld.loadMatch(matchName, true);
        }
        catch (ORTFileException e) {
            ORTWorld.setNull();
            throw e;
        }
        MatchLoading.setWorldRules(server.func_71218_a(0));
        for (EntityPlayerMP player : server.func_184103_al().func_181057_v()) {
            OrienteeringMod.InGameResponseServer.onPlayerJoins(new EntityJoinWorldEvent(player, server.func_71218_a(0)));
        }
    }

    private static void setWorldRules(World world) {
        GameRules rule = world.func_82736_K();
        if (!rule.func_82766_b("keepInventory")) {
            rule.func_82764_b("keepInventory", "true");
        }
        if (rule.func_82766_b("doMobSpawning")) {
            rule.func_82764_b("doMobSpawning", "false");
        }
        world.func_72912_H().func_176144_a(EnumDifficulty.NORMAL);
        world.func_72912_H().func_176121_c(false);
    }
}

