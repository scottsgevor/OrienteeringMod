/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.io.File;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.respawner.orienteeringmod.BlockCPEnd;
import net.respawner.orienteeringmod.BlockCPStart;
import net.respawner.orienteeringmod.BlockCheckpoint;
import net.respawner.orienteeringmod.BlockMainStation;
import net.respawner.orienteeringmod.CommandORT;
import net.respawner.orienteeringmod.Drawing;
import net.respawner.orienteeringmod.ItemORTShovel;
import net.respawner.orienteeringmod.ItemORTTimeConverter;
import net.respawner.orienteeringmod.MatchLoading;
import net.respawner.orienteeringmod.MsgGradeRequest;
import net.respawner.orienteeringmod.MsgMatchClient;
import net.respawner.orienteeringmod.MsgPlayerClicksPoint;
import net.respawner.orienteeringmod.MsgSetCP;
import net.respawner.orienteeringmod.MsgTimePenalty;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTIngameException;
import net.respawner.orienteeringmod.ORTMatchDNEException;
import net.respawner.orienteeringmod.ORTPlayer;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.TileEntityCheckpoint;
import net.respawner.orienteeringmod.Track;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

@Mod(modid="orienteeringmod", name="Orienteering Mod", version="4.4", acceptedMinecraftVersions="[1.12.2]")
public class OrienteeringMod {
    public static final String MODID = "orienteeringmod";
    public static final String NAME = "Orienteering Mod";
    public static final String VERSION = "4.4";
    public static final long SerialVersion = 4L;
    public static final int MatchseedVersion = 4;
    public static final SimpleNetworkWrapper NETHANDLER = NetworkRegistry.INSTANCE.newSimpleChannel("orienteeringmod");
    public static long lastSavingTime = 0L;
    static OrienteeringMod instance = new OrienteeringMod();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NETHANDLER.registerMessage(MsgMatchClient.MsgPJHandler.class, MsgMatchClient.class, 1, Side.CLIENT);
        NETHANDLER.registerMessage(MsgPlayerClicksPoint.MsgPCPHandler.class, MsgPlayerClicksPoint.class, 2, Side.SERVER);
        NETHANDLER.registerMessage(MsgPlayerClicksPoint.MsgResponse.MsgResponseHandler.class, MsgPlayerClicksPoint.MsgResponse.class, 3, Side.CLIENT);
        NETHANDLER.registerMessage(MsgGradeRequest.MsgGRHandler.class, MsgGradeRequest.class, 4, Side.SERVER);
        NETHANDLER.registerMessage(MsgGradeRequest.MsgResponse.MsgResponseHandler.class, MsgGradeRequest.MsgResponse.class, 5, Side.CLIENT);
        NETHANDLER.registerMessage(MsgSetCP.MsgSetCPHandler.class, MsgSetCP.class, 6, Side.SERVER);
        NETHANDLER.registerMessage(MsgTimePenalty.MsgTPHandler.class, MsgTimePenalty.class, 7, Side.CLIENT);
        File rootPath = new File(Util.getRootPath());
        try {
            if (!rootPath.isDirectory()) {
                rootPath.delete();
            }
            if (!rootPath.exists()) {
                rootPath.mkdir();
            }
        }
        catch (SecurityException e) {
            return;
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandORT());
        try {
            if (event.getServer() instanceof IntegratedServer) {
                MatchLoading.LoadMatchClient((IntegratedServer)event.getServer());
            }
        }
        catch (ORTFileException e) {
            if (e instanceof ORTMatchDNEException) {
                LogManager.getLogger().info("No orienteering match detected for this world.");
            }
        }
        catch (NoClassDefFoundError e) {
            LogManager.getLogger().info("The current server is a dedicated server. The orienteering match must be manually loaded.");
        }
        catch (Throwable e) {
            LogManager.getLogger().error(e.toString());
        }
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        try {
            ORTWorld.saveAllDataToFile();
        }
        catch (ORTFileException e) {
            LogManager.getLogger().error(e.toString());
        }
        ORTWorld.setNull();
        ORTWorld.setMatchClient(null);
    }

    @Mod.EventBusSubscriber(value={Side.CLIENT}, modid="orienteeringmod")
    public static class IngameResponseClient {
        private static long lastUpdateTime = 0L;
        private static long startTime = 0L;
        private static long timePenalty = 0L;

        public static void matchStartUpdate(long startTime2) {
            lastUpdateTime = 0L;
            startTime = startTime2;
            timePenalty = 0L;
        }

        public static void addDeath(long penalty) {
            timePenalty += penalty;
        }

        @SubscribeEvent
        @SideOnly(value=Side.CLIENT)
        public static void clientUpdate(RenderGameOverlayEvent.Post event) {
            MsgMatchClient instance = ORTWorld.getMatchInstanceClient();
            if (instance == null) {
                return;
            }
            if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
                return;
            }
            Minecraft mc = Minecraft.func_71410_x();
            GuiIngame gui = mc.field_71456_v;
            if (instance.decodeStatus() == ORTPlayer.PlayerStatus.InProgress) {
                if (instance.needDrawCompass()) {
                    Drawing.drawCompass(mc, gui);
                }
                if (instance.displayTime()) {
                    Drawing.drawTimeString(mc, gui, startTime, timePenalty);
                }
                if (mc.field_71441_e.func_82737_E() >= lastUpdateTime + 10L) {
                    Track.getTrack().update(mc.field_71439_g.field_70165_t, mc.field_71439_g.field_70161_v, mc.field_71441_e.func_82737_E());
                    lastUpdateTime = mc.field_71441_e.func_82737_E();
                }
            }
            if (!instance.isAdmin()) {
                mc.field_71474_y.field_74330_P = false;
            }
        }
    }

    @Mod.EventBusSubscriber(modid="orienteeringmod")
    public static final class InGameResponseServer {
        @SubscribeEvent
        public static boolean onPlayerJoins(EntityJoinWorldEvent event) {
            if (!ORTWorld.hasMatchServer()) {
                return true;
            }
            World world = event.getWorld();
            if (world.field_72995_K) {
                return false;
            }
            if (!(event.getEntity() instanceof EntityPlayerMP)) {
                return false;
            }
            EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
            ORTWorld instance = ORTWorld.getMatchInstanceServer();
            try {
                if (instance.unloadedPlayers.contains(player.func_70005_c_())) {
                    Util.sendMessage(player, "Detected that your data is not correctly loaded.We are generating new data in place of the broken one.");
                    instance.unloadedPlayers.remove(player.func_70005_c_());
                }
                instance.onPlayerJoin(player.func_70005_c_());
            }
            catch (ORTIngameException e) {
                LogManager.getLogger().info("" + player.func_70005_c_() + " is denied access to the world.");
                try {
                    if (player.field_71133_b instanceof DedicatedServer) {
                        player.field_71135_a.func_194028_b(new TextComponentTranslation("Your name " + player.func_70005_c_() + " is not found on the player roster.", new Object[0]));
                    }
                }
                catch (NoClassDefFoundError e2) {
                    LogManager.getLogger().info("The game is running in single-player mode. We cannot kick the player.");
                }
                event.setCanceled(true);
            }
            catch (Throwable e) {
                LogManager.getLogger().info(e.getMessage());
                e.printStackTrace();
                event.setCanceled(true);
                return false;
            }
            try {
                ORTPlayer p = ORTWorld.getMatchInstanceServer().players.get(player.func_70005_c_());
                if (p == null) {
                    Util.sendMessage(player, "Warning: you are not registered in the match.");
                    LogManager.getLogger().info("Unregistered player: " + player.func_70005_c_());
                    return false;
                }
                String course = instance.match.getCourseByPlayer(player.func_70005_c_());
                if (course == null) {
                    throw new ORTIngameException("No course is assigned to you.");
                }
                NETHANDLER.sendTo(MsgMatchClient.CreateNewMsg(instance, player.func_70005_c_()), player);
                LogManager.getLogger().info("Player entered: " + player.func_70005_c_() + " (course: " + course + ", status: " + (Object)((Object)p.getPlayerStatus()) + ")");
                Util.sendMessage(player, "Detected presence of orienteering match.");
                Util.sendMessage(player, "Your assigned course is: " + course + ". Your status is: " + (Object)((Object)p.getPlayerStatus()));
                if (p.isInMatchProgress()) {
                    Util.sendMessage(player, "Your last checkpoint is: " + p.getLastCP());
                }
            }
            catch (Throwable e) {
                LogManager.getLogger().info(e.getMessage());
                if (!(e instanceof ORTIngameException)) {
                    e.printStackTrace();
                }
                return false;
            }
            return true;
        }

        @SubscribeEvent
        public static boolean onPlayerDeath(LivingDeathEvent event) {
            if (!(event.getEntityLiving() instanceof EntityPlayerMP)) {
                return true;
            }
            EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
            World world = player.func_130014_f_();
            if (world.field_72995_K) {
                return true;
            }
            if (ORTWorld.getMatchInstanceServer() == null) {
                return true;
            }
            try {
                ORTPlayer p = ORTWorld.getMatchInstanceServer().getPlayerByName(player.func_70005_c_());
                if (p == null) {
                    return true;
                }
                if (!p.isInMatchProgress()) {
                    return true;
                }
                switch (ORTWorld.getMatchInstanceServer().match.getDeathPolicy()) {
                    case Abort: {
                        p.addDeath();
                        p.abort();
                        int[] spawn = ORTWorld.getMatchInstanceServer().match.getSpawnPos();
                        player.func_180473_a(new BlockPos(spawn[0], spawn[1], spawn[2]), true);
                        break;
                    }
                    case Penalty: {
                        p.addDeath();
                        NETHANDLER.sendTo(new MsgTimePenalty(ORTWorld.getMatchInstanceServer().match.getDeathPenalty() * 1200), player);
                        BlockPos pos = Util.randomRespawn(world, player.field_70165_t, player.field_70163_u, player.field_70161_v, ORTWorld.getMatchInstanceServer().match.getSpawnRadius());
                        boolean a = false;
                        LogManager.getLogger().debug("#ORT Debug: " + player.func_70005_c_() + " died at (" + player.field_70165_t + ", " + player.field_70163_u + ", " + player.field_70161_v + ").");
                        player.func_180473_a(pos, true);
                    }
                }
                LogManager.getLogger().info("Player " + player.func_70005_c_() + " dies. Death policy: " + (Object)((Object)ORTWorld.getMatchInstanceServer().match.getDeathPolicy()));
            }
            catch (Throwable e) {
                LogManager.getLogger().info(e.getMessage());
                if (!(e instanceof ORTIngameException)) {
                    e.printStackTrace();
                }
                return true;
            }
            return true;
        }

        @SubscribeEvent
        public static void OnFalling(LivingAttackEvent event) {
            if (!(event.getEntityLiving() instanceof EntityPlayerMP)) {
                return;
            }
            if (event.getSource() != DamageSource.field_76379_h) {
                return;
            }
            if (!ORTWorld.hasMatchServer()) {
                return;
            }
            if (!ORTWorld.getMatchInstanceServer().match.useCustomFallDamage()) {
                return;
            }
            EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
            player.func_70097_a(DamageSource.field_76380_i, event.getAmount() > player.func_110138_aP() / 2.0f ? 100.0f : event.getAmount() / 2.0f);
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void ORTPlayerUpdate(LivingEvent.LivingUpdateEvent event) {
            if (!(event.getEntityLiving() instanceof EntityPlayerMP)) {
                return;
            }
            EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
            World world = player.func_130014_f_();
            if (world.field_72995_K) {
                return;
            }
            if (!ORTWorld.hasMatchServer()) {
                return;
            }
            if (player.field_70170_p.func_82737_E() >= lastSavingTime + 2400L) {
                try {
                    ORTWorld.saveAllDataToFile();
                }
                catch (ORTFileException e) {
                    LogManager.getLogger().info(e.getMessage());
                }
                lastSavingTime = player.field_70170_p.func_82737_E();
            }
            try {
                ORTPlayer p;
                player.func_184589_d(Potion.func_188412_a(19));
                if (ORTWorld.getMatchInstanceServer().match.isSpectator(player.func_70005_c_())) {
                    player.func_71033_a(GameType.SPECTATOR);
                }
                if (!ORTWorld.getMatchInstanceServer().match.isAdmin(player.func_70005_c_())) {
                    if (player.field_71075_bZ.field_75099_e || player.field_71075_bZ.field_75101_c) {
                        player.func_71033_a(GameType.ADVENTURE);
                    }
                    if (player.field_71075_bZ.field_75100_b) {
                        Util.sendMessage(player, "The system detected that you are flying. You are dead now.");
                        player.func_70097_a(DamageSource.field_76380_i, Float.MAX_VALUE);
                    }
                }
                if ((p = ORTWorld.getMatchInstanceServer().players.get(player.func_70005_c_())) == null) {
                    return;
                }
                if (p.isInMatchProgress()) {
                    OrienteeringFoodHooks.setFoodLevelIfEnabled(player.func_71024_bL(), 15);
                    if (ORTWorld.getMatchInstanceServer().match.needNightVision()) {
                        player.func_70690_d(new PotionEffect(Potion.func_188412_a(16), 310));
                    }
                } else {
                    OrienteeringFoodHooks.setFoodLevelIfEnabled(player.func_71024_bL(), 20);
                    player.func_184589_d(Potion.func_188412_a(16));
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    @Mod.EventBusSubscriber(modid="orienteeringmod")
    public static final class ModInit {
        static BlockCheckpoint checkpoint = new BlockCheckpoint();
        static BlockCPStart CPStart = new BlockCPStart();
        static BlockCPEnd CPEnd = new BlockCPEnd();
        static BlockMainStation mainStation = new BlockMainStation();
        static ItemORTShovel ORTShovel = new ItemORTShovel();
        static ItemORTTimeConverter ORTTimeConverter = new ItemORTTimeConverter();

        @SubscribeEvent
        public static void registerBlock(RegistryEvent.Register<Block> event) {
            event.getRegistry().register((Block)checkpoint.setRegistryName(OrienteeringMod.MODID, "checkpoint"));
            event.getRegistry().register((Block)CPStart.setRegistryName(OrienteeringMod.MODID, "cpstart"));
            event.getRegistry().register((Block)CPEnd.setRegistryName(OrienteeringMod.MODID, "cpend"));
            event.getRegistry().register((Block)mainStation.setRegistryName(OrienteeringMod.MODID, "mainstation"));
            GameRegistry.registerTileEntity(TileEntityCheckpoint.class, new ResourceLocation(OrienteeringMod.MODID, "checkpoint"));
        }

        @SubscribeEvent
        public static void registerItem(RegistryEvent.Register<Item> event) {
            event.getRegistry().register((Item)new ItemBlock(checkpoint).setRegistryName(OrienteeringMod.MODID, "checkpoint"));
            event.getRegistry().register((Item)new ItemBlock(CPStart).setRegistryName(OrienteeringMod.MODID, "cpstart"));
            event.getRegistry().register((Item)new ItemBlock(CPEnd).setRegistryName(OrienteeringMod.MODID, "cpend"));
            event.getRegistry().register((Item)new ItemBlock(mainStation).setRegistryName(OrienteeringMod.MODID, "mainstation"));
            event.getRegistry().register((Item)ORTShovel.setRegistryName(OrienteeringMod.MODID, "ortshovel"));
            event.getRegistry().register((Item)ORTTimeConverter.setRegistryName(OrienteeringMod.MODID, "orttimeconverter"));
        }

        @SubscribeEvent
        public static void registryModel(ModelRegistryEvent event) {
            ModelLoader.setCustomModelResourceLocation(ORTShovel, 0, new ModelResourceLocation(new ResourceLocation("minecraft", "diamond_shovel"), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ORTTimeConverter, 0, new ModelResourceLocation(new ResourceLocation("minecraft", "clock"), "inventory"));
            ModelLoader.setCustomModelResourceLocation(Item.func_150898_a(checkpoint), 0, new ModelResourceLocation(new ResourceLocation(OrienteeringMod.MODID, "checkpoint"), "inventory"));
            ModelLoader.setCustomModelResourceLocation(Item.func_150898_a(CPStart), 0, new ModelResourceLocation(new ResourceLocation(OrienteeringMod.MODID, "cpstart"), "inventory"));
            ModelLoader.setCustomModelResourceLocation(Item.func_150898_a(CPEnd), 0, new ModelResourceLocation(new ResourceLocation(OrienteeringMod.MODID, "cpend"), "inventory"));
            ModelLoader.setCustomModelResourceLocation(Item.func_150898_a(mainStation), 0, new ModelResourceLocation(new ResourceLocation(OrienteeringMod.MODID, "mainstation"), "inventory"));
        }
    }
}

