/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.respawner.orienteeringmod.CreativeTabORT;
import net.respawner.orienteeringmod.MsgPlayerClicksPoint;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.OrienteeringMod;
import net.respawner.orienteeringmod.Track;
import net.respawner.orienteeringmod.Util;

public class BlockCPStart
extends Block {
    public AxisAlignedBB AABB = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

    public BlockCPStart() {
        super(Material.field_151592_s);
        this.func_149647_a(CreativeTabORT.tabOrienteering);
        this.func_149722_s();
        this.func_149715_a(1.0f);
        this.func_149663_c("orienteeringmod.blockcpstart");
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
    public AxisAlignedBB func_185496_a(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return this.AABB;
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
            Util.sendMessage(player, "# Checkpoint Start (no match data found.)");
            return false;
        }
        Track.getTrack().setMatchName(ORTWorld.getMatchInstanceClient().getMatchName());
        Track.getTrack().reset();
        OrienteeringMod.IngameResponseClient.matchStartUpdate(world.func_82737_E());
        OrienteeringMod.NETHANDLER.sendToServer(new MsgPlayerClicksPoint(player.func_70005_c_(), 0, 0));
        return false;
    }
}

