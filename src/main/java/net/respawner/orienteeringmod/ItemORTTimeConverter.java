/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.respawner.orienteeringmod.CreativeTabORT;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.Util;

public class ItemORTTimeConverter
extends Item {
    public ItemORTTimeConverter() {
        this.func_77637_a(CreativeTabORT.tabOrienteering);
        this.func_77625_d(1);
        this.func_77655_b("orienteeringmod.itemorttimeconverter");
    }

    @Override
    public void func_77624_a(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("Right click will set the time to sunrise.");
    }

    @Override
    public ActionResult<ItemStack> func_77659_a(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (worldIn.field_72995_K) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.func_184586_b(handIn));
        }
        if (ORTWorld.getMatchInstanceServer() != null) {
            playerIn.func_130014_f_().func_72877_b(23250L);
            Util.sendMessage(playerIn, "Time has been set to sunrise.");
        }
        return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.func_184586_b(handIn));
    }
}

