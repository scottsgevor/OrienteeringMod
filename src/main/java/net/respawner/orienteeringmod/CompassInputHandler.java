package net.respawner.orienteeringmod;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = OrienteeringMod.MODID)
public final class CompassInputHandler {
    private CompassInputHandler() {
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
        if (CompassKeyBindings.CYCLE_POSITION.isActiveAndMatches(key)) {
            CompassClientState.cyclePosition();
        }
        else if (CompassKeyBindings.TOGGLE_VISIBILITY.isActiveAndMatches(key)) {
            CompassClientState.toggleVisible();
        }
    }
}