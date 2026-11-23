/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.respawner.orienteeringmod.Util;

public class Drawing {
    @SideOnly(value=Side.CLIENT)
    public static void drawTimeString(Minecraft mc, GuiIngame gui, long beginningTime, long deathTimePenalty) {
        ScaledResolution res = new ScaledResolution(mc);
        String str = "Your time: " + Util.timeTranslation((mc.field_71441_e.func_82737_E() - beginningTime + deathTimePenalty) / 2L);
        gui.func_73732_a(mc.field_71466_p, str, res.func_78326_a() / 2, 14, 0xFFFFFF);
    }

    @SideOnly(value=Side.CLIENT)
    public static void drawCompass(Minecraft mc, GuiIngame gui, int position) {
        ScaledResolution resolution = new ScaledResolution(mc);
        mc.field_71446_o.func_110577_a(new ResourceLocation("orienteeringmod", "textures/gui/compass_hd.png"));
        int w = resolution.func_78326_a();
        int h = resolution.func_78328_b();
        int L = h / 16;
        int D = h * 3 / 8;
        int left;
        int top;
        switch (position) {
            case 0:
                left = w - L - D;
                top = L;
                break;
            case 1:
                left = L;
                top = L;
                break;
            case 2:
                left = L;
                top = h - L - D;
                break;
            default:
                left = w - L - D;
                top = h - L - D;
                break;
        }
        int cx = left + D / 2;
        int cy = top + D / 2;
        Gui.func_146110_a(left, top, 0.0f, 0.0f, D, D, D, D);
        int r = h / 100;
        int l = D * 2 / 7;
        int R2 = D / 2 - 9;
        float angle = -mc.field_71439_g.field_70759_as * 2.0f * (float)Math.PI / 360.0f;
        Tessellator tessellator = Tessellator.func_178181_a();
        BufferBuilder bufferbuilder = tessellator.func_178180_c();
        mc.field_71446_o.func_110577_a(new ResourceLocation("orienteeringmod", "textures/gui/red.png"));
        bufferbuilder.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        bufferbuilder.func_181662_b((double)cx - (double)r * Math.cos(angle), (double)cy - (double)r * Math.sin(angle), 0.0).func_187315_a(0.0, 0.0).func_181675_d();
        bufferbuilder.func_181662_b((double)cx - (double)r * Math.cos(angle) - (double)l * Math.sin(angle), (double)cy - (double)r * Math.sin(angle) + (double)l * Math.cos(angle), 0.0).func_187315_a(0.0, 1.0).func_181675_d();
        bufferbuilder.func_181662_b((double)cx + (double)r * Math.cos(angle) - (double)l * Math.sin(angle), (double)cy + (double)r * Math.sin(angle) + (double)l * Math.cos(angle), 0.0).func_187315_a(1.0, 1.0).func_181675_d();
        bufferbuilder.func_181662_b((double)cx + (double)r * Math.cos(angle), (double)cy + (double)r * Math.sin(angle), 0.0).func_187315_a(1.0, 0.0).func_181675_d();
        tessellator.func_78381_a();
        mc.field_71446_o.func_110577_a(new ResourceLocation("orienteeringmod", "textures/gui/white.png"));
        bufferbuilder.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        bufferbuilder.func_181662_b((double)cx + (double)r * Math.cos(angle), (double)cy + (double)r * Math.sin(angle), 0.0).func_187315_a(0.0, 0.0).func_181675_d();
        bufferbuilder.func_181662_b((double)cx + (double)r * Math.cos(angle) + (double)l * Math.sin(angle), (double)cy + (double)r * Math.sin(angle) - (double)l * Math.cos(angle), 0.0).func_187315_a(0.0, 1.0).func_181675_d();
        bufferbuilder.func_181662_b((double)cx - (double)r * Math.cos(angle) + (double)l * Math.sin(angle), (double)cy - (double)r * Math.sin(angle) - (double)l * Math.cos(angle), 0.0).func_187315_a(1.0, 1.0).func_181675_d();
        bufferbuilder.func_181662_b((double)cx - (double)r * Math.cos(angle), (double)cy - (double)r * Math.sin(angle), 0.0).func_187315_a(1.0, 0.0).func_181675_d();
        tessellator.func_78381_a();
        mc.field_71446_o.func_110577_a(Gui.field_110324_m);
        FontRenderer font = mc.field_71466_p;
        gui.func_73732_a(font, "N", cx - (int)((double)R2 * Math.sin(angle)), cy + (int)((double)R2 * Math.cos(angle)) - 4, 0xFFFFFF);
        gui.func_73732_a(font, "E", cx - (int)((double)R2 * Math.cos(angle)), cy - (int)((double)R2 * Math.sin(angle)) - 4, 0xFFFFFF);
        gui.func_73732_a(font, "S", cx + (int)((double)R2 * Math.sin(angle)), cy - (int)((double)R2 * Math.cos(angle)) - 4, 0xFFFFFF);
        gui.func_73732_a(font, "W", cx + (int)((double)R2 * Math.cos(angle)), cy + (int)((double)R2 * Math.sin(angle)) - 4, 0xFFFFFF);
    }
}

