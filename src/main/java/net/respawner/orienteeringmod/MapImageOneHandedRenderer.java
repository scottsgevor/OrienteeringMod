package net.respawner.orienteeringmod;

import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
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
public final class MapImageOneHandedRenderer {
    private static final ResourceLocation PLACEHOLDER = new ResourceLocation("textures/items/map_filled.png");
    private static final ResourceLocation MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    private static final Method RENDER_ARM = findRenderArm();

    private MapImageOneHandedRenderer() {
    }

    @SubscribeEvent
    public static void onRenderSpecificHand(RenderSpecificHandEvent event) {
        MapImageItemRenderer.setRenderingHand(event.getHand());
        MapImageRef ref = MapImageRef.fromStack(event.getItemStack());
        if (ref == null) {
            return;
        }
        Minecraft mc = Minecraft.func_71410_x();
        EntityPlayerSP player = mc.field_71439_g;
        if (player == null) {
            return;
        }
        if (event.getHand() == EnumHand.MAIN_HAND && player.func_184586_b(EnumHand.OFF_HAND).func_190926_b()) {
            return;
        }
        event.setCanceled(true);
        GlStateManager.func_179094_E();
        try {
            render(mc, player, ref, event);
        }
        catch (Throwable e) {
            LogManager.getLogger().warn("Failed to render one-handed map image: " + e.getMessage());
        }
        finally {
            GlStateManager.func_179121_F();
        }
    }

    private static void render(Minecraft mc, EntityPlayerSP player, MapImageRef ref, RenderSpecificHandEvent event) {
        EnumHandSide side = event.getHand() == EnumHand.MAIN_HAND
                ? player.func_184591_cq()
                : player.func_184591_cq().func_188468_a();
        float equip = event.getEquipProgress();
        float swing = event.getSwingProgress();
        float sideSign = side == EnumHandSide.RIGHT ? 1.0f : -1.0f;

        GlStateManager.func_179109_b(sideSign * 0.125f, -0.125f, 0.0f);
        if (!player.func_82150_aj() && RENDER_ARM != null) {
            GlStateManager.func_179094_E();
            try {
                GlStateManager.func_179114_b(sideSign * 10.0f, 0.0f, 0.0f, 1.0f);
                RENDER_ARM.invoke(mc.func_175597_ag(), equip, swing, side);
            }
            catch (Throwable e) {
                LogManager.getLogger().warn("Failed to render one-handed map arm: " + e.getMessage());
            }
            finally {
                GlStateManager.func_179121_F();
            }
        }

        GlStateManager.func_179094_E();
        try {
            GlStateManager.func_179109_b(sideSign * 0.51f, -0.08f - equip * 1.2f, -0.75f);
            float f = MathHelper.func_76129_c(swing);
            float f1 = MathHelper.func_76126_a(f * (float)Math.PI);
            float f2 = 0.4f * MathHelper.func_76126_a(f * (float)(Math.PI * 2.0));
            float f3 = -0.3f * MathHelper.func_76126_a(swing * (float)Math.PI);
            GlStateManager.func_179109_b(sideSign * (-0.5f * f1), f2 + 0.3f * f1, f3);
            GlStateManager.func_179114_b(f1 * -45.0f, 1.0f, 0.0f, 0.0f);
            GlStateManager.func_179114_b(sideSign * f1 * -30.0f, 0.0f, 1.0f, 0.0f);
            renderMap(mc, ref, event.getHand());
        }
        finally {
            GlStateManager.func_179121_F();
        }
    }

    private static void renderMap(Minecraft mc, MapImageRef ref, EnumHand hand) {
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
        GlStateManager.func_179114_b((float)MapImageClientCache.rotation(ref), 0.0f, 0.0f, 1.0f);
        GlStateManager.func_179109_b(-64.0f, -64.0f, 0.0f);
        if (MapInspectionController.isActive()) {
            MapImageProjection.update(ref, left, top, right, bottom, -0.01f, hand);
        }
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

    private static Method findRenderArm() {
        try {
            Method method = ObfuscationReflectionHelper.findMethod(ItemRenderer.class, "func_187456_a",
                    Void.TYPE, float.class, float.class, EnumHandSide.class);
            method.setAccessible(true);
            return method;
        }
        catch (Throwable e) {
            try {
                Method method = ItemRenderer.class.getDeclaredMethod("func_187456_a",
                        float.class, float.class, EnumHandSide.class);
                method.setAccessible(true);
                return method;
            }
            catch (Throwable ignored) {
                LogManager.getLogger().warn("Could not find vanilla one-handed arm renderer; map images use map-only rendering.");
                return null;
            }
        }
    }
}