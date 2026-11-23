package net.respawner.orienteeringmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        if (mc == null || mc.field_71439_g == null || mc.field_71462_r != null) {
            return;
        }
        ScaledResolution resolution = new ScaledResolution(mc);
        GlStateManager.func_179097_i();
        try {
            drawHelp(mc, resolution.func_78326_a());
        }
        finally {
            GlStateManager.func_179126_j();
        }
    }

    private static void drawHelp(Minecraft mc, int screenWidth) {
        String help = MapImageKeyBindings.display(MapImageKeyBindings.ROTATE_LEFT) + ": rotate left   "
                + MapImageKeyBindings.display(MapImageKeyBindings.ROTATE_RIGHT) + ": rotate right   "
                + "Scroll: zoom held map   "
                + MapImageKeyBindings.display(MapImageKeyBindings.INSPECT) + ": restore cursor";
        String zoom = "Zoom: " + Math.round(MapInspectionController.getZoom() * 100.0f) + "%";
        int textWidth = mc.field_71466_p.func_78256_a(help);
        int x = Math.max(4, (screenWidth - textWidth) / 2);
        Gui.func_73734_a(x - 4, 6, x + textWidth + 4, 32, 0x80000000);
        mc.field_71466_p.func_78276_b(help, x, 9, 0xFFFFFF);
        mc.field_71466_p.func_78276_b(zoom, x, 21, 0xA0A0A0);
    }
}