package net.respawner.orienteeringmod;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public final class CompassKeyBindings {
    public static final KeyBinding CYCLE_POSITION = new KeyBinding("key.orienteeringmod.compass_position",
            Keyboard.KEY_U, "key.categories.orienteeringmod");
    public static final KeyBinding TOGGLE_VISIBILITY = new KeyBinding("key.orienteeringmod.compass_toggle",
            Keyboard.KEY_I, "key.categories.orienteeringmod");

    private CompassKeyBindings() {
    }

    public static void register() {
        ClientRegistry.registerKeyBinding(CYCLE_POSITION);
        ClientRegistry.registerKeyBinding(TOGGLE_VISIBILITY);
    }
}