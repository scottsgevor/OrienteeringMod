/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.respawner.orienteeringmod.CreativeTabORT;
import net.respawner.orienteeringmod.GuiCheckpoint;
import net.respawner.orienteeringmod.MsgPlayerClicksPoint;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.OrienteeringMod;
import net.respawner.orienteeringmod.TileEntityCheckpoint;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public class BlockCheckpoint
extends Block
implements ITileEntityProvider {
    public AxisAlignedBB AABB = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

    public BlockCheckpoint() {
        super(Material.field_151592_s);
        this.func_149647_a(CreativeTabORT.tabOrienteering);
        this.func_149722_s();
        this.func_149663_c("orienteeringmod.blockcheckpoint");
        this.field_149758_A = true;
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
    public boolean hasTileEntity(IBlockState state) {
        return true;
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
        TileEntity entity = world.func_175625_s(pos);
        if (!(entity instanceof TileEntityCheckpoint)) {
            return false;
        }
        TileEntityCheckpoint entityCP = (TileEntityCheckpoint)entity;
        Util.sendMessage(player, "# Checkpoint " + entityCP.getCode());
        if (!ORTWorld.hasMatchClient()) {
            Util.sendMessage(player, "(The current world does not have a match.)");
            return false;
        }
        OrienteeringMod.NETHANDLER.sendToServer(new MsgPlayerClicksPoint(player.func_70005_c_(), 1, entityCP.getCode()));
        return false;
    }

    @Override
    @SideOnly(value=Side.CLIENT)
    public void func_180633_a(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.func_180633_a(world, pos, state, placer, stack);
        if (!world.field_72995_K) {
            return;
        }
        if (!(placer instanceof EntityPlayer)) {
            return;
        }
        TileEntity Entity2 = world.func_175625_s(pos);
        if (!(Entity2 instanceof TileEntityCheckpoint)) {
            return;
        }
        TileEntityCheckpoint entityCP = (TileEntityCheckpoint)Entity2;
        try {
            Minecraft.func_71410_x().func_147108_a(new GuiCheckpoint(entityCP, (EntityPlayer)placer, Minecraft.func_71410_x()));
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
        }
        catch (Throwable e) {
            LogManager.getLogger().error(e.toString());
            Util.sendMessage((EntityPlayer)placer, "Error: action Failed. Unknown Reason.");
        }
    }

    @Override
    public TileEntity func_149915_a(World worldIn, int meta) {
        return new TileEntityCheckpoint(0);
    }

    @Override
    public void func_176213_c(World worldIn, BlockPos pos, IBlockState state) {
        super.func_176213_c(worldIn, pos, state);
    }

    @Override
    public void func_180663_b(World worldIn, BlockPos pos, IBlockState state) {
        super.func_180663_b(worldIn, pos, state);
        worldIn.func_175713_t(pos);
    }
}

