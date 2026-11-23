/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.respawner.orienteeringmod.ICourse;
import net.respawner.orienteeringmod.MsgMatchClient;
import net.respawner.orienteeringmod.ORTIngameException;
import net.respawner.orienteeringmod.ORTPlayer;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.OrienteeringMod;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public class MsgPlayerClicksPoint
implements IMessage {
    public static final int NULL = -1;
    public static final int START = 0;
    public static final int CHECKPOINT = 1;
    public static final int END = 2;
    private int playerNameLength;
    private char[] playerName;
    private int type;
    private int CPCode;

    public MsgPlayerClicksPoint() {
    }

    public MsgPlayerClicksPoint(String name, int type, int CPCode) {
        this.playerNameLength = name.length();
        this.playerName = name.toCharArray();
        this.type = type;
        this.CPCode = CPCode;
    }

    public String getName() {
        return String.copyValueOf(this.playerName);
    }

    public int getActionType() {
        return this.type;
    }

    public int getCPCode() {
        return this.CPCode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            this.playerNameLength = buf.readInt();
            this.playerName = new char[this.playerNameLength];
            for (int i = 0; i < this.playerNameLength; ++i) {
                this.playerName[i] = buf.readChar();
            }
            this.type = buf.readInt();
            this.CPCode = buf.readInt();
        }
        catch (Throwable e) {
            e.printStackTrace();
            this.type = -1;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            buf.writeInt(this.playerNameLength);
            for (int i = 0; i < this.playerNameLength; ++i) {
                buf.writeChar(this.playerName[i]);
            }
            buf.writeInt(this.type);
            buf.writeInt(this.CPCode);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static class MsgResponse
    implements IMessage {
        public static final String BLANK = " ";
        private boolean isCancelled;
        private boolean isStart;
        private boolean isEnd;
        private int msgLength;
        private char[] msg;

        public MsgResponse() {
        }

        public MsgResponse(EntityPlayer player, boolean isCancelled, boolean isStart, boolean isEnd, String msg) {
            if (!msg.equals(BLANK)) {
                Util.sendMessage(player, msg);
            }
            this.isCancelled = isCancelled;
            this.isStart = isStart;
            this.isEnd = isEnd;
            this.msgLength = msg.length();
            this.msg = msg.toCharArray();
        }

        public boolean isCancelled() {
            return this.isCancelled;
        }

        public boolean isStart() {
            return this.isStart;
        }

        public boolean isEnd() {
            return this.isEnd;
        }

        public String getMessage() {
            return String.copyValueOf(this.msg);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            try {
                this.isCancelled = buf.readBoolean();
                this.isStart = buf.readBoolean();
                this.isEnd = buf.readBoolean();
                this.msgLength = buf.readInt();
                this.msg = new char[this.msgLength];
                for (int i = 0; i < this.msgLength; ++i) {
                    this.msg[i] = buf.readChar();
                }
            }
            catch (Throwable e) {
                LogManager.getLogger().error(e.toString());
                this.msg = "Error: action Failed. Unknown Reason.".toCharArray();
            }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            try {
                buf.writeBoolean(this.isCancelled);
                buf.writeBoolean(this.isStart);
                buf.writeBoolean(this.isEnd);
                buf.writeInt(this.msgLength);
                for (int i = 0; i < this.msgLength; ++i) {
                    buf.writeChar(this.msg[i]);
                }
            }
            catch (Throwable e) {
                LogManager.getLogger().error(e.toString());
            }
        }

        public static class MsgResponseHandler
        implements IMessageHandler<MsgResponse, IMessage> {
            @Override
            public IMessage onMessage(MsgResponse message, MessageContext ctx) {
                return null;
            }
        }
    }

    public static class MsgPCPHandler
    implements IMessageHandler<MsgPlayerClicksPoint, MsgResponse> {
        @Override
        public MsgResponse onMessage(MsgPlayerClicksPoint message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().field_147369_b;
            ORTWorld instance = ORTWorld.getMatchInstanceServer();
            if (instance == null) {
                return new MsgResponse(player, true, false, false, "The current world does not have a match.");
            }
            ORTPlayer p = instance.players.get(player.func_70005_c_());
            switch (message.getActionType()) {
                case 0: {
                    try {
                        if (p == null) {
                            return new MsgResponse(player, true, true, false, "Action failed. You are not registered in the player roster.");
                        }
                        instance.match.checkRuleBeforeStarting(p, ORTWorld.getMatchInstanceServer());
                        WorldServer world = player.func_71121_q();
                        ICourse course = instance.courses.get(instance.match.getCourseByPlayer(player.func_70005_c_()));
                        if (course.needTeleport()) {
                            player.func_70634_a((double)course.getStartPos()[0] + 0.5, (double)course.getStartPos()[1] + 0.5, (double)course.getStartPos()[2] + 0.5);
                        }
                        player.func_180473_a(new BlockPos(instance.match.spawn[0], instance.match.spawn[1], instance.match.spawn[2]), true);
                        p.start(world.func_82737_E());
                        p.recordStartPosition(player.field_70165_t, player.field_70163_u, player.field_70161_v);
                        try {
                            MapImageService.giveMaps(player, instance.matchName, course.getName());
                        }
                        catch (Throwable e) {
                            LogManager.getLogger().warn("Failed to give map images to " + player.func_70005_c_() + ": " + e.getMessage());
                        }
                        LogManager.getLogger().info("" + player.func_70005_c_() + " starts at time " + world.func_82737_E() + ". Course: " + course.getName() + ".");
                        OrienteeringMod.NETHANDLER.sendTo(MsgMatchClient.CreateNewMsg(instance, player.func_70005_c_()), player);
                        Util.sendMessage(player, "# Player: " + player.func_70005_c_() + ". Course: " + course.getName() + ". Type: " + course.getCourseType().toString());
                        if (course.getWelcomeString() != null) {
                            Util.sendMessage(player, course.getWelcomeString());
                        }
                        return new MsgResponse(player, false, true, false, "Match begins now. Run fast!");
                    }
                    catch (ORTIngameException e) {
                        return new MsgResponse(player, true, true, false, e.getMessage());
                    }
                    catch (Throwable e) {
                        return new MsgResponse(player, true, true, false, "Error: action Failed. Unknown Reason.");
                    }
                }
                case 1: {
                    try {
                        if (p == null) {
                            return new MsgResponse(player, true, false, false, "(No match data found.)");
                        }
                        if (!p.isInMatchProgress()) {
                            return new MsgResponse(player, true, false, false, "(You are not in a match.)");
                        }
                        p.checkpoint(message.getCPCode(), player.func_71121_q().func_82737_E(),
                                player.field_70165_t, player.field_70163_u, player.field_70161_v);
                        LogManager.getLogger().info("" + player.func_70005_c_() + " clicks checkpoint " + message.getCPCode() + " at time " + player.func_71121_q().func_82737_E());
                        return new MsgResponse(player, false, false, false, " ");
                    }
                    catch (Throwable e) {
                        return new MsgResponse(player, true, false, false, "Error: action Failed. Unknown Reason.");
                    }
                }
                case 2: {
                    try {
                        if (p == null) {
                            return new MsgResponse(player, true, false, true, "Action failed. You are not registered in the player roster.");
                        }
                        if (!p.isInMatchProgress()) {
                            return new MsgResponse(player, true, false, true, "# Checkpoint End (You are not in a match.)");
                        }
                        player.func_70634_a(instance.match.spawn[0], instance.match.spawn[1], instance.match.spawn[2]);
                        player.func_180473_a(new BlockPos(instance.match.spawn[0], instance.match.spawn[1], instance.match.spawn[2]), true);
                        p.end(player.func_71121_q().func_82737_E());
                        LogManager.getLogger().info("" + player.func_70005_c_() + " ends at time " + player.func_71121_q().func_82737_E() + ".");
                        OrienteeringMod.NETHANDLER.sendTo(MsgMatchClient.CreateNewMsg(instance, player.func_70005_c_()), player);
                        return new MsgResponse(player, false, false, true, "Finally!");
                    }
                    catch (Throwable e) {
                        return new MsgResponse(player, true, false, true, "Error: action Failed. Unknown Reason.");
                    }
                }
            }
            return new MsgResponse(player, true, false, true, "Action failed due to network problems.");
        }
    }
}

