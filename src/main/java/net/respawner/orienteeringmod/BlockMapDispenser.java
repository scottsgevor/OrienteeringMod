/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.respawner.orienteeringmod.CreativeTabORT;
import net.respawner.orienteeringmod.MapImageService;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.Util;

public class BlockMapDispenser
extends Block {
    public BlockMapDispenser() {
        super(Material.field_151575_d);
        this.func_149647_a(CreativeTabORT.tabOrienteering);
        this.func_149711_c(2.5f);
        this.func_149752_b(2.5f);
        this.func_149663_c("orienteeringmod.mapdispenser");
    }

    @Override
    public boolean func_180639_a(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                 EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.field_72995_K) {
            return true;
        }
        if (hand == EnumHand.OFF_HAND) {
            return true;
        }
        if (!(player instanceof EntityPlayerMP)) {
            return true;
        }
        if (!ORTWorld.hasMatchServer()) {
            Util.sendMessage(player, "The current world does not have a match.");
            return true;
        }
        ORTWorld instance = ORTWorld.getMatchInstanceServer();
        String course = instance.match.getCourseByPlayer(player.func_70005_c_());
        if (course == null) {
            Util.sendMessage(player, "No course matches your name.");
            return true;
        }
        int given;
        try {
            given = MapImageService.giveMaps((EntityPlayerMP)player, instance.matchName, course);
        }
        catch (Throwable e) {
            Util.sendMessage(player, "Failed to get map images.");
            return true;
        }
        if (given == 0) {
            Util.sendMessage(player, "No map images for course " + course + ".");
        }
        return true;
    }
}