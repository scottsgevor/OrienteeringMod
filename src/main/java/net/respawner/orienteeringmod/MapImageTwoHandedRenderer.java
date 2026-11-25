package net.respawner.orienteeringmod;

import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = OrienteeringMod.MODID)
public final class MapImageTwoHandedRenderer {
    private static final ResourceLocation PLACEHOLDER = new ResourceLocation("textures/items/map_filled.png");
    private static final ResourceLocation MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    private static final Method RENDER_ARMS = findRenderArms();

    private MapImageTwoHandedRenderer() {
    }

    @SubscribeEvent
    public static void onOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            MapImageItemRenderer.setRenderingHand(null);
        }
    }

    @SubscribeEvent
    public static void onRenderSpecificHand(RenderSpecificHandEvent event) {
        MapImageItemRenderer.setRenderingHand(event.getHand());
        if (event.getHand() != EnumHand.MAIN_HAND) {
            return;
        }
        MapImageRef ref = MapImageRef.fromStack(event.getItemStack());
        if (ref == null) {
            return;
        }
        Minecraft mc = Minecraft.func_71410_x();
        EntityPlayerSP player = mc.field_71439_g;
        if (player == null || !player.func_184586_b(EnumHand.OFF_HAND).func_190926_b()) {
            return;
        }
        if (RENDER_ARMS == null) {
            return;
        }
        event.setCanceled(true);
        GlStateManager.func_179094_E();
        try {
            render(mc, ref, event);
        }
        catch (Throwable e) {
            LogManager.getLogger().warn("Failed to render two-handed map image: " + e.getMessage());
        }
        finally {
            GlStateManager.func_179121_F();
        }
    }

    private static void render(Minecraft mc, MapImageRef ref, RenderSpecificHandEvent event) throws Exception {
        float swing = event.getSwingProgress();
        float f = MathHelper.func_76129_c(swing);
        float f1 = -0.2f * MathHelper.func_76126_a(swing * (float) Math.PI);
        float f2 = -0.4f * MathHelper.func_76126_a(f * (float) Math.PI);
        GlStateManager.func_179109_b(0.0f, -f1 / 2.0f, f2);
        float equip = equipFactor(event.getInterpolatedPitch());
        GlStateManager.func_179109_b(0.0f, 0.04f + event.getEquipProgress() * -1.2f + equip * -0.5f, -0.72f);
        GlStateManager.func_179114_b(equip * -85.0f, 1.0f, 0.0f, 0.0f);
        RENDER_ARMS.invoke(mc.func_175597_ag());
        float f8 = MathHelper.func_76126_a(f * (float) Math.PI);
        GlStateManager.func_179114_b(f8 * 20.0f, 1.0f, 0.0f, 0.0f);
        GlStateManager.func_179152_a(2.0f, 2.0f, 2.0f);
        renderMap(mc, ref);
    }

    private static void renderMap(Minecraft mc, MapImageRef ref) {
        ResourceLocation texture = MapImageClientCache.texture(ref);
        if (texture == null) {
            texture = PLACEHOLDER;
        }
        float[] quad = MapImageClientCache.displayQuad(ref, 128.0f);
        float width = quad[0];
        float height = quad[1];
        float left = 64.0f - width / 2.0f;
        float right = 64.0f + width / 2.0f;
        float bottom = 64.0f - height / 2.0f;
        float top = 64.0f + height / 2.0f;

        GlStateManager.func_179114_b(180.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.func_179114_b(180.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.func_179152_a(0.38f, 0.38f, 0.38f);
        GlStateManager.func_179140_f();
        GlStateManager.func_179098_w();
        GlStateManager.func_179147_l();
        GlStateManager.func_179112_b(770, 771);
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.func_179129_p();

        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b(-0.5f, -0.5f, 0.0f);
        GlStateManager.func_179152_a(0.0078125f, 0.0078125f, 0.0078125f);
        mc.func_110434_K().func_110577_a(MAP_BACKGROUND);
        Tessellator tessellator = Tessellator.func_178181_a();
        BufferBuilder buffer = tessellator.func_178180_c();
        buffer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        buffer.func_181662_b(-7.0, 135.0, 0.0).func_187315_a(0.0, 1.0).func_181675_d();
        buffer.func_181662_b(135.0, 135.0, 0.0).func_187315_a(1.0, 1.0).func_181675_d();
        buffer.func_181662_b(135.0, -7.0, 0.0).func_187315_a(1.0, 0.0).func_181675_d();
        buffer.func_181662_b(-7.0, -7.0, 0.0).func_187315_a(0.0, 0.0).func_181675_d();
        tessellator.func_78381_a();

        mc.func_110434_K().func_110577_a(texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        float[] window = MapImageClientCache.window(ref);
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b(64.0f, 64.0f, 0.0f);
        GlStateManager.func_179114_b((float) MapImageClientCache.rotation(ref), 0.0f, 0.0f, 1.0f);
        GlStateManager.func_179109_b(-64.0f, -64.0f, 0.0f);
        MapImageProjection.update(ref, left, top, right, bottom, -0.01f);
        buffer = tessellator.func_178180_c();
        buffer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        buffer.func_181662_b(left, top, -0.01).func_187315_a(window[0], window[3]).func_181675_d();
        buffer.func_181662_b(right, top, -0.01).func_187315_a(window[2], window[3]).func_181675_d();
        buffer.func_181662_b(right, bottom, -0.01).func_187315_a(window[2], window[1]).func_181675_d();
        buffer.func_181662_b(left, bottom, -0.01).func_187315_a(window[0], window[1]).func_181675_d();
        tessellator.func_78381_a();
        GlStateManager.func_179121_F();
        GlStateManager.func_179121_F();

        GlStateManager.func_179089_o();
        GlStateManager.func_179145_e();
    }

    private static float equipFactor(float pitch) {
        float value = 1.0f - pitch / 45.0f + 0.1f;
        value = MathHelper.func_76131_a(value, 0.0f, 1.0f);
        return -MathHelper.func_76134_b(value * (float) Math.PI) * 0.5f + 0.5f;
    }

    private static Method findRenderArms() {
        try {
            Method method = ObfuscationReflectionHelper.findMethod(ItemRenderer.class, "func_187466_c", Void.TYPE);
            method.setAccessible(true);
            return method;
        }
        catch (Throwable e) {
            try {
                Method method = ItemRenderer.class.getDeclaredMethod("func_187466_c");
                method.setAccessible(true);
                return method;
            }
            catch (Throwable ignored) {
                LogManager.getLogger().warn("Could not find vanilla two-handed arm renderer; map images use one-handed rendering.");
                return null;
            }
        }
    }
}