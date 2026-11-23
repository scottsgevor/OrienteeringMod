package net.respawner.orienteeringmod;

import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemMapImage extends Item {
    public ItemMapImage() {
        this.func_77625_d(1);
        this.func_77655_b("orienteeringmod.mapimage");
        this.setNoRepair();
    }

    @Override
    public void func_77624_a(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("J/K: rotate 90 degrees");
        tooltip.add("H: release mouse to zoom the held map");
    }
}