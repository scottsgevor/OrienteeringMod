package net.respawner.orienteeringmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = OrienteeringMod.MODID)
public final class MapImageOverlayRenderer {
    private MapImageOverlayRenderer() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || !MapInspectionController.isActive()) {
            return;
        }
        Minecraft mc = Minecraft.func_71410_x();
        MapImageRef ref = MapInspectionController.getRef();
        if (mc == null || mc.field_71439_g == null || mc.field_71462_r != null || ref == null) {
            return;
        }
        ScaledResolution resolution = new ScaledResolution(mc);
        int screenWidth = resolution.func_78326_a();
        int screenHeight = resolution.func_78328_b();
        ResourceLocation texture = MapImageClientCache.texture(ref);
        if (texture == null) {
            String message = MapImageClientCache.failed(ref) ? "Failed to load map image." : "Loading map image...";
            drawPanel(mc, screenWidth, screenHeight, message);
            drawHelp(mc, screenWidth);
            return;
        }
        int imageWidth = MapImageClientCache.width(ref);
        int imageHeight = MapImageClientCache.height(ref);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }
        float fit = MapInspectionController.fitScale(screenWidth, screenHeight);
        float scale = fit * MapInspectionController.getZoom();
        MapInspectionController.clampPan(screenWidth, screenHeight, fit);
        float centerX = (float) screenWidth / 2.0f + MapInspectionController.getPanX();
        float centerY = (float) screenHeight / 2.0f + MapInspectionController.getPanY();
        int rotation = MapImageClientCache.rotation(ref);

        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b(centerX, centerY, 0.0f);
        GlStateManager.func_179114_b((float) rotation, 0.0f, 0.0f, 1.0f);
        GlStateManager.func_179152_a(scale, scale, 1.0f);
        GlStateManager.func_179098_w();
        GlStateManager.func_179147_l();
        GlStateManager.func_179112_b(770, 771);
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        mc.func_110434_K().func_110577_a(texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        float halfWidth = (float) imageWidth / 2.0f;
        float halfHeight = (float) imageHeight / 2.0f;
        Tessellator tessellator = Tessellator.func_178181_a();
        BufferBuilder buffer = tessellator.func_178180_c();
        buffer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        buffer.func_181662_b(-halfWidth, -halfHeight, 0.0).func_187315_a(0.0, 0.0).func_181675_d();
        buffer.func_181662_b(halfWidth, -halfHeight, 0.0).func_187315_a(1.0, 0.0).func_181675_d();
        buffer.func_181662_b(halfWidth, halfHeight, 0.0).func_187315_a(1.0, 1.0).func_181675_d();
        buffer.func_181662_b(-halfWidth, halfHeight, 0.0).func_187315_a(0.0, 1.0).func_181675_d();
        tessellator.func_78381_a();

        GlStateManager.func_179121_F();
        drawHelp(mc, screenWidth);
    }

    private static void drawPanel(Minecraft mc, int screenWidth, int screenHeight, String message) {
        int textWidth = mc.field_71466_p.func_78256_a(message);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 2;
        Gui.func_73734_a(x - 6, y - 6, x + textWidth + 6, y + 14, 0xA0000000);
        mc.field_71466_p.func_78276_b(message, x, y, 0xFFFFFF);
    }

    private static void drawHelp(Minecraft mc, int screenWidth) {
        String help = "J: rotate left   K: rotate right   Scroll: zoom   H: restore cursor";
        String zoom = "Zoom: " + Math.round(MapInspectionController.getZoom() * 100.0f) + "%";
        int textWidth = mc.field_71466_p.func_78256_a(help);
        int x = Math.max(4, (screenWidth - textWidth) / 2);
        Gui.func_73734_a(x - 4, 6, x + textWidth + 4, 32, 0x80000000);
        mc.field_71466_p.func_78276_b(help, x, 9, 0xFFFFFF);
        mc.field_71466_p.func_78276_b(zoom, x, 21, 0xA0A0A0);
    }
}