package net.respawner.orienteeringmod;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = OrienteeringMod.MODID)
public final class MapImageInputHandler {
    private MapImageInputHandler() {
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.func_71410_x();
        if (mc == null || mc.field_71439_g == null || mc.field_71462_r != null) {
            return;
        }
        if (!Keyboard.getEventKeyState()) {
            return;
        }
        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_H) {
            MapInspectionController.toggle(mc);
        } else if (key == Keyboard.KEY_J) {
            MapInspectionController.rotateHeld(mc, -90);
        } else if (key == Keyboard.KEY_K) {
            MapInspectionController.rotateHeld(mc, 90);
        }
    }

    @SubscribeEvent
    public static void onMouseEvent(MouseEvent event) {
        if (!MapInspectionController.isActive()) {
            return;
        }
        Minecraft mc = Minecraft.func_71410_x();
        if (mc == null) {
            return;
        }
        int wheel = event.getDwheel();
        if (wheel != 0) {
            MapInspectionController.scroll(mc, wheel);
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MapInspectionController.tick(Minecraft.func_71410_x());
    }
}