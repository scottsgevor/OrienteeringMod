/*
 * Client-side crosshair info for orienteering start/end/checkpoint blocks.
 */
package net.respawner.orienteeringmod;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = "orienteeringmod", value = {Side.CLIENT})
public class TargetInfoOverlay {
    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        Minecraft mc = Minecraft.func_71410_x();
        if (mc == null || mc.field_71439_g == null || mc.field_71441_e == null || mc.field_71462_r != null) {
            return;
        }
        if (mc.field_71474_y != null && mc.field_71474_y.field_74330_P) {
            return;
        }
        RayTraceResult target = mc.field_71476_x;
        if (target == null) {
            return;
        }
        BlockPos pos = target.func_178782_a();
        if (pos == null) {
            return;
        }
        IBlockState state = mc.field_71441_e.func_180495_p(pos);
        Block block = state.func_177230_c();
        String text;
        if (block instanceof BlockCPStart) {
            text = TextFormatting.LIGHT_PURPLE + "起点";
        } else if (block instanceof BlockCPEnd) {
            text = TextFormatting.BLUE + "终点";
        } else if (block instanceof BlockCheckpoint) {
            TileEntity tile = mc.field_71441_e.func_175625_s(pos);
            if (tile instanceof TileEntityCheckpoint) {
                text = TextFormatting.YELLOW + "检查点 " + ((TileEntityCheckpoint) tile).getCode();
            } else {
                text = TextFormatting.YELLOW + "检查点";
            }
        } else {
            return;
        }

        ScaledResolution resolution = new ScaledResolution(mc);
        int centerX = resolution.func_78326_a() / 2;
        int y = resolution.func_78328_b() / 2 + 12;
        FontRenderer font = mc.field_71466_p;
        int width = font.func_78256_a(text);
        Gui.func_73734_a(centerX - width / 2 - 3, y - 3, centerX + width / 2 + 3, y + 10, 0x90000000);
        mc.field_71456_v.func_73732_a(font, text, centerX, y, 0xFFFFFF);
    }
}