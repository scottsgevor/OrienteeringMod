/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public abstract class CreativeTabORT {
    public static final CreativeTabs tabOrienteering = new CreativeTabs("orienteering"){

        @Override
        public ItemStack func_78016_d() {
            return new ItemStack(Items.field_151111_aL);
        }
    };
}

