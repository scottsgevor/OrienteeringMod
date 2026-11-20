/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.math.BlockPos;
import net.respawner.orienteeringmod.Cartography;
import net.respawner.orienteeringmod.MatchGeneration;
import net.respawner.orienteeringmod.MatchLoading;
import net.respawner.orienteeringmod.MsgMatchClient;
import net.respawner.orienteeringmod.MsgTimePenalty;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTIngameException;
import net.respawner.orienteeringmod.ORTMatch;
import net.respawner.orienteeringmod.ORTPlayer;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.OrienteeringMod;
import net.respawner.orienteeringmod.StatOutput;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public class CommandORT
extends CommandBase {
    @Override
    public String func_71517_b() {
        return "ORT";
    }

    @Override
    public String func_71518_a(ICommandSender sender) {
        return "/ORT - Commands for the orienteering system.";
    }

    @Override
    public boolean func_184882_a(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public int func_82362_a() {
        return 0;
    }

    @Override
    public List<String> func_184883_a(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return func_71530_a(args, "generate", "loadMatch", "openMatch", "closeMatch",
                    "home", "clearPlayers", "getCourse", "changeCourse", "changeMode",
                    "stat", "sketch", "stuck");
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if ("loadMatch".equals(sub)) {
                return func_175762_a(args, matchNames(Util.STR_MATCH_FILE));
            }
            if ("generate".equals(sub)) {
                return func_175762_a(args, matchNames("matchseed.json"));
            }
            if ("changeCourse".equals(sub) && ORTWorld.hasMatchServer()) {
                List<String> courses = new ArrayList<String>(ORTWorld.getMatchInstanceServer().match.getCourseList());
                Collections.sort(courses);
                return func_175762_a(args, courses);
            }
        }
        return Collections.emptyList();
    }

    private static List<String> matchNames(String requiredFile) {
        List<String> names = new ArrayList<String>();
        File[] dirs = new File(Util.getRootPath()).listFiles();
        if (dirs == null) {
            return names;
        }
        for (File dir : dirs) {
            if (dir.isDirectory() && new File(dir, requiredFile).isFile()) {
                names.add(dir.getName());
            }
        }
        Collections.sort(names);
        return names;
    }

    @Override
    public void func_184881_a(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP)sender;
        if (args[0].equals("generate")) {
            String matchName = args.length >= 2 ? args[1] : player.func_130014_f_().func_72912_H().func_76065_j();
            boolean dedicated = false;
            try {
                dedicated = player.field_71133_b instanceof DedicatedServer;
            }
            catch (NoClassDefFoundError noClassDefFoundError) {
                // Client side does not have the dedicated server class.
            }
            try {
                MatchGeneration.generateAndSave(matchName, player.func_70005_c_());
                if (dedicated) {
                    MatchLoading.LoadMatchServer((DedicatedServer)server, matchName);
                } else {
                    MatchLoading.LoadMatchClient((IntegratedServer)server, matchName);
                    ORTWorld.getMatchInstanceServer().onPlayerJoin(player.func_70005_c_());
                    for (String p : server.func_184103_al().func_152606_n()) {
                        server.func_184103_al().func_152610_b(server.func_184103_al().func_152603_m().func_152700_a(p));
                    }
                    OrienteeringMod.NETHANDLER.sendTo(MsgMatchClient.CreateNewMsg(ORTWorld.getMatchInstanceServer(), player.func_70005_c_()), player);
                }
            }
            catch (ORTIngameException e) {
                Util.sendMessage(player, e.getMessage());
                return;
            }
            catch (ORTFileException e) {
                Util.sendMessage(player, e.getMessage());
                return;
            }
            catch (Throwable e) {
                LogManager.getLogger().info(e.toString());
                Util.sendMessage(player, "Error: action Failed. Unknown Reason.");
                return;
            }
            Util.sendMessage(player, "Success.");
            return;
        }
        if (args[0].equals("loadMatch")) {
            if (args.length < 2) {
                Util.sendMessage(player, "Command error: you need to specify the name of the match.");
                return;
            }
            try {
                if (player.field_71133_b instanceof IntegratedServer) {
                    Util.sendMessage(player, "This command is only allowed on dedicated servers.");
                    return;
                }
            }
            catch (NoClassDefFoundError e) {
                // empty catch block
            }
            try {
                MatchLoading.LoadMatchServer((DedicatedServer)server, args[1]);
            }
            catch (ORTFileException e) {
                Util.sendMessage(player, e.getMessage());
                return;
            }
            catch (Throwable e) {
                LogManager.getLogger().error(e.toString());
                Util.sendMessage(player, "Error: action Failed. Unknown Reason.");
                return;
            }
            Util.sendMessage(player, "Success.");
            return;
        }
        if (args[0].equals("openMatch")) {
            try {
                if (!ORTWorld.hasMatchServer()) {
                    Util.sendMessage(player, "The current world does not have a match.");
                    return;
                }
                if (!ORTWorld.getMatchInstanceServer().match.isAdmin(player.func_70005_c_())) {
                    Util.sendMessage(player, "Only admin can announce the opening of the match.");
                    return;
                }
                if (ORTWorld.getMatchInstanceServer().match.announceStart()) {
                    ORTWorld.getMatchInstanceServer().match.setMatchStart(true);
                    Util.sendMessage(player, "The match is now opened.");
                } else {
                    Util.sendMessage(player, "This match does not need announced start.");
                }
            }
            catch (Throwable e) {
                LogManager.getLogger().error(e.toString());
                Util.sendMessage(player, "Error: action Failed. Unknown Reason.");
            }
        }
        if (args[0].equals("closeMatch")) {
            try {
                if (!ORTWorld.hasMatchServer()) {
                    Util.sendMessage(player, "The current world does not have a match.");
                    return;
                }
                if (!ORTWorld.getMatchInstanceServer().match.isAdmin(player.func_70005_c_())) {
                    Util.sendMessage(player, "Only admin can announce the close of the match.");
                    return;
                }
                ORTWorld.saveAllDataToFile();
                if (ORTWorld.getMatchInstanceServer().match.announceStart()) {
                    ORTWorld.getMatchInstanceServer().match.setMatchStart(false);
                    Util.sendMessage(player, "The match is now closed.");
                } else {
                    Util.sendMessage(player, "This match does not need announced end.");
                }
            }
            catch (Throwable e) {
                LogManager.getLogger().error(e.toString());
                Util.sendMessage(player, "Error: action Failed. Unknown Reason.");
            }
        }
        if (args[0].equals("home")) {
            if (!ORTWorld.hasMatchServer()) {
                Util.sendMessage(player, "The current world does not have a match.");
                return;
            }
            ORTMatch match = ORTWorld.getMatchInstanceServer().match;
            ORTPlayer p = ORTWorld.getMatchInstanceServer().getPlayerByName(player.func_70005_c_());
            if (p != null) {
                if (p.getPlayerStatus() == ORTPlayer.PlayerStatus.InProgress) {
                    Util.sendMessage(player, "You cannot use this command during a match.");
                } else {
                    player.func_70634_a(match.spawn[0], match.spawn[1], match.spawn[2]);
                }
            }
        }
        if (args[0].equals("clearPlayers")) {
            try {
                if (!ORTWorld.hasMatchServer()) {
                    Util.sendMessage(player, "The current world does not have a match.");
                    return;
                }
                if (!ORTWorld.getMatchInstanceServer().match.isAdmin(player.func_70005_c_())) {
                    Util.sendMessage(player, "Only admin can use this command.");
                    return;
                }
                ORTWorld.getMatchInstanceServer().clearPlayer();
            }
            catch (ORTFileException e) {
                Util.sendMessage(player, e.getMessage());
            }
            catch (Throwable e) {
                LogManager.getLogger().error(e.toString());
                Util.sendMessage(player, "Error: action Failed. Unknown Reason.");
            }
        }
        if (args[0].equals("getCourse")) {
            try {
                if (!ORTWorld.hasMatchServer()) {
                    Util.sendMessage(player, "The current world does not have a match.");
                    return;
                }
                String course = ORTWorld.getMatchInstanceServer().match.getCourseByPlayer(player.func_70005_c_());
                if (course == null) {
                    Util.sendMessage(player, "No course matches the player's name.");
                }
                Util.sendMessage(player, course);
            }
            catch (Throwable e) {
                LogManager.getLogger().error(e.toString());
                Util.sendMessage(player, "Error: action Failed. Unknown Reason.");
            }
        }
        if (args[0].equals("changeCourse")) {
            try {
                if (!ORTWorld.hasMatchServer()) {
                    Util.sendMessage(player, "The current world does not have a match.");
                    return;
                }
                if (args.length < 2) {
                    Util.sendMessage(player, "The course name cannot be empty");
                    return;
                }
                if (ORTWorld.getMatchInstanceServer().match.getWorldMode() == ORTMatch.WorldMode.Match && !ORTWorld.getMatchInstanceServer().match.isAdmin(player.func_70005_c_())) {
                    return;
                }
                ORTPlayer p = ORTWorld.getMatchInstanceServer().getPlayerByName(player.func_70005_c_());
                if (p == null) {
                    Util.sendMessage(player, "Action failed. You are not registered in the player roster.");
                    return;
                }
                if (p.isInMatchProgress()) {
                    Util.sendMessage(player, "You are now in a match. You cannot change the course.");
                    return;
                }
                ORTWorld.getMatchInstanceServer().match.changeCourse(player.func_70005_c_(), args[1]);
                Util.sendMessage(player, "Success.");
            }
            catch (ORTIngameException e) {
                Util.sendMessage(player, e.getMessage());
            }
            catch (Throwable e) {
                LogManager.getLogger().error(e.toString());
                Util.sendMessage(player, "Error: action Failed. Unknown Reason.");
            }
            return;
        }
        if (args[0].equals("changeMode")) {
            try {
                if (!ORTWorld.hasMatchServer()) {
                    Util.sendMessage(player, "The current world does not have a match.");
                    return;
                }
                if (!ORTWorld.getMatchInstanceServer().match.isAdmin(player.func_70005_c_())) {
                    Util.sendMessage(player, "Only admin can use this command.");
                }
                Util.sendMessage(player, "Success: " + ORTWorld.getMatchInstanceServer().match.changeMode());
            }
            catch (Throwable e) {
                LogManager.getLogger().error(e.toString());
                Util.sendMessage(player, "Error: action Failed. Unknown Reason.");
            }
            return;
        }
        if (args[0].equals("stat")) {
            if (!ORTWorld.hasMatchServer()) {
                Util.sendMessage(player, "The current world does not have a match.");
                return;
            }
            try {
                StatOutput.output(ORTWorld.getMatchInstanceServer().matchName);
                Util.sendMessage(player, "Completed.");
            }
            catch (Throwable e) {
                Util.sendMessage(player, e.getMessage());
            }
            return;
        }
        if (args[0].equals("sketch")) {
            if (ORTWorld.hasMatchServer()) {
                Util.sendMessage(player, "This function is currently disabled.");
                return;
            }
            try {
                if (player.field_71133_b instanceof DedicatedServer) {
                    Util.sendMessage(player, "This command is not allowed on dedicated server.");
                    return;
                }
            }
            catch (NoClassDefFoundError e) {
                // empty catch block
            }
            try {
                Util.sendMessage(player, Cartography.sketch(player.func_130014_f_(), player));
            }
            catch (NoSuchMethodError e) {
                Util.sendMessage(player, "This function is only available in client side.");
            }
        }
        boolean modified = false;
        if (args[0].equals("stuck")) {
            if (!ORTWorld.hasMatchServer()) {
                Util.sendMessage(player, "The current world does not have a match.");
                return;
            }
            BlockPos pos = Util.randomRespawn(player.field_70170_p, player.field_70165_t, player.field_70163_u, player.field_70161_v, 30);
            player.func_70634_a(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p());
            OrienteeringMod.NETHANDLER.sendTo(new MsgTimePenalty(6000L), player);
            Util.sendMessage(player, "Teleporting... (this costs 5 min.)");
            ORTPlayer p = ORTWorld.getMatchInstanceServer().getPlayerByName(player.func_70005_c_());
            if (p == null) {
                Util.sendMessage(player, "Action failed. You are not registered in the player roster.");
                return;
            }
            p.addDeath();
        }
    }
}

