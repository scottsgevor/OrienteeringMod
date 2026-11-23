package net.respawner.orienteeringmod;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public final class MapImageKeyBindings {
    public static final KeyBinding ROTATE_LEFT = new KeyBinding("key.orienteeringmod.rotate_left",
            Keyboard.KEY_J, "key.categories.orienteeringmod");
    public static final KeyBinding ROTATE_RIGHT = new KeyBinding("key.orienteeringmod.rotate_right",
            Keyboard.KEY_K, "key.categories.orienteeringmod");
    public static final KeyBinding INSPECT = new KeyBinding("key.orienteeringmod.inspect",
            Keyboard.KEY_H, "key.categories.orienteeringmod");

    private MapImageKeyBindings() {
    }

    public static void register() {
        ClientRegistry.registerKeyBinding(ROTATE_LEFT);
        ClientRegistry.registerKeyBinding(ROTATE_RIGHT);
        ClientRegistry.registerKeyBinding(INSPECT);
    }

    public static String display(KeyBinding binding) {
        if (binding == null) {
            return "NONE";
        }
        String name = binding.getKeyModifier().getLocalizedComboName(binding.func_151463_i());
        if (name == null || name.isEmpty()) {
            name = Keyboard.getKeyName(binding.func_151463_i());
        }
        return name == null || name.isEmpty() ? "NONE" : name;
    }
}