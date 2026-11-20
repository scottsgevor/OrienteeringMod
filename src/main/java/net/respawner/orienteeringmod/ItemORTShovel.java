/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.respawner.orienteeringmod.CreativeTabORT;
import net.respawner.orienteeringmod.ORTPlayer;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public class ItemORTShovel
extends ItemSpade {
    public ItemORTShovel() {
        super(Item.ToolMaterial.DIAMOND);
        this.func_77637_a(CreativeTabORT.tabOrienteering);
        this.setNoRepair();
        this.func_77656_e(65536);
        this.func_77625_d(1);
        this.func_77655_b("orienteeringmod.itemortshovel");
    }

    @Override
    public void func_77624_a(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("In a match, right click on a block will destroy it.");
    }

    @Override
    public EnumActionResult func_180614_a(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.field_72995_K) {
            return EnumActionResult.PASS;
        }
        if (Block.func_176210_f(worldIn.func_180495_p(pos)) == 0) {
            return EnumActionResult.PASS;
        }
        if (!ORTWorld.hasMatchServer()) {
            return EnumActionResult.PASS;
        }
        try {
            ORTPlayer p = ORTWorld.getMatchInstanceServer().getPlayerByName(player.func_70005_c_());
            if (p == null) {
                return EnumActionResult.PASS;
            }
            if (worldIn.func_82737_E() >= p.lastShovelUsedTime + 2400L) {
                if (Util.isORTBLock(worldIn.func_180495_p(pos))) {
                    return EnumActionResult.PASS;
                }
                if (!p.isInMatchProgress()) {
                    return EnumActionResult.PASS;
                }
                worldIn.func_175655_b(pos, false);
                Util.sendMessage(player, "The shovel will need 2 minutes' cooling down time.");
                p.lastShovelUsedTime = worldIn.func_82737_E();
            }
        }
        catch (Throwable e) {
            LogManager.getLogger().error(e.toString());
            Util.sendMessage(player, "Error: action Failed. Unknown Reason.");
        }
        return EnumActionResult.SUCCESS;
    }
}

