package net.respawner.orienteeringmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MapImageItemRenderer extends TileEntityItemStackRenderer {
    private static final ResourceLocation PLACEHOLDER = new ResourceLocation("textures/items/map_filled.png");
    private static EnumHand renderingHand;

    public static void setRenderingHand(EnumHand hand) {
        renderingHand = hand;
    }

    @Override
    public void func_192838_a(ItemStack stack, float partialTicks) {
        MapImageRef ref = MapImageRef.fromStack(stack);
        if (ref == null) {
            return;
        }
        Minecraft mc = Minecraft.func_71410_x();
        ResourceLocation texture = MapImageClientCache.texture(ref);
        if (texture == null) {
            texture = PLACEHOLDER;
        }
        int imageWidth = MapImageClientCache.width(ref);
        int imageHeight = MapImageClientCache.height(ref);
        float width = 1.0f;
        float height = 1.0f;
        if (imageWidth > 0 && imageHeight > 0) {
            if (imageWidth >= imageHeight) {
                height = (float) imageHeight / (float) imageWidth;
            } else {
                width = (float) imageWidth / (float) imageHeight;
            }
        }
        int rotation = MapImageClientCache.rotation(ref);
        if (rotation % 180 != 0) {
            float swap = width;
            width = height;
            height = swap;
        }

        mc.func_110434_K().func_110577_a(texture);
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b(0.5f, 0.5f, 0.5f);
        GlStateManager.func_179114_b((float) (-rotation), 0.0f, 0.0f, 1.0f);
        GlStateManager.func_179140_f();
        GlStateManager.func_179129_p();
        GlStateManager.func_179147_l();
        GlStateManager.func_179112_b(770, 771);
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);

        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        float[] window = MapImageClientCache.window(ref);
        boolean offhand = renderingHand == EnumHand.OFF_HAND;
        float uLeft = offhand ? window[2] : window[0];
        float uRight = offhand ? window[0] : window[2];
        float vTop = window[1];
        float vBottom = window[3];
        Tessellator tessellator = Tessellator.func_178181_a();
        BufferBuilder buffer = tessellator.func_178180_c();
        buffer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        buffer.func_181662_b(-halfWidth, halfHeight, 0.0).func_187315_a(uLeft, vTop).func_181675_d();
        buffer.func_181662_b(halfWidth, halfHeight, 0.0).func_187315_a(uRight, vTop).func_181675_d();
        buffer.func_181662_b(halfWidth, -halfHeight, 0.0).func_187315_a(uRight, vBottom).func_181675_d();
        buffer.func_181662_b(-halfWidth, -halfHeight, 0.0).func_187315_a(uLeft, vBottom).func_181675_d();
        tessellator.func_78381_a();

        GlStateManager.func_179084_k();
        GlStateManager.func_179089_o();
        GlStateManager.func_179145_e();
        GlStateManager.func_179121_F();
    }
}