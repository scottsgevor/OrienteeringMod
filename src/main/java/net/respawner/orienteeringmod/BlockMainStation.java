/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.respawner.orienteeringmod.CreativeTabORT;
import net.respawner.orienteeringmod.MsgGradeRequest;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.OrienteeringMod;
import net.respawner.orienteeringmod.Track;
import net.respawner.orienteeringmod.Util;

public class BlockMainStation
extends Block {
    public BlockMainStation() {
        super(Material.field_151592_s);
        this.func_149647_a(CreativeTabORT.tabOrienteering);
        this.func_149722_s();
        this.func_149715_a(1.0f);
        this.func_149663_c("orienteeringmod.blockmainstation");
    }

    @Override
    @SideOnly(value=Side.CLIENT)
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean func_149662_c(IBlockState state) {
        return false;
    }

    @Override
    public boolean func_149686_d(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(value=Side.CLIENT)
    public boolean func_180639_a(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.field_72995_K) {
            return false;
        }
        if (hand == EnumHand.OFF_HAND) {
            return false;
        }
        if (!ORTWorld.hasMatchClient()) {
            Util.sendMessage(player, "No match data found.");
            return false;
        }
        Track.getTrack().export();
        OrienteeringMod.NETHANDLER.sendToServer(new MsgGradeRequest());
        return false;
    }
}

