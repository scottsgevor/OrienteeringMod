package net.respawner.orienteeringmod;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = OrienteeringMod.MODID)
public final class MapImageClientSetup {
    private MapImageClientSetup() {
    }

    public static void registerKeyBindings() {
        MapImageKeyBindings.register();
    }

    @SubscribeEvent
    public static void registerModel(ModelRegistryEvent event) {
        MapImageClientCache.ensureNetworkRegistered();
        OrienteeringMod.ModInit.mapImage.setTileEntityItemStackRenderer(new MapImageItemRenderer());
        ModelLoader.setCustomModelResourceLocation(OrienteeringMod.ModInit.mapImage, 0,
                new ModelResourceLocation(new ResourceLocation(OrienteeringMod.MODID, "mapimage"), "inventory"));
    }
}