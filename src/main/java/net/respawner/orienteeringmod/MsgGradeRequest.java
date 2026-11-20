/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import io.netty.buffer.ByteBuf;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.respawner.orienteeringmod.IGrade;
import net.respawner.orienteeringmod.IPlayerGroup;
import net.respawner.orienteeringmod.ORTPlayer;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public class MsgGradeRequest
implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class MsgResponse
    implements IMessage {
        private int playerNameLength;
        private char[] playerName;
        private IGrade grade;

        public MsgResponse() {
        }

        public MsgResponse(IGrade grade) {
            this.grade = grade;
            this.playerName = grade.getGroupName().toCharArray();
            this.playerNameLength = this.playerName.length;
        }

        public IGrade getGrade() {
            return this.grade;
        }

        public String getPlayerName() {
            return String.copyValueOf(this.playerName);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            try {
                this.playerNameLength = buf.readInt();
                this.playerName = new char[this.playerNameLength];
                for (int i = 0; i < this.playerNameLength; ++i) {
                    this.playerName[i] = buf.readChar();
                }
                byte[] array = new byte[buf.capacity() - 4 - 2 * this.playerNameLength];
                buf.getBytes(4 + 2 * this.playerNameLength, array);
                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(array));
                this.grade = (IGrade)in.readObject();
            }
            catch (Throwable e) {
                e.printStackTrace();
                this.grade = null;
            }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            try {
                buf.writeInt(this.playerNameLength);
                for (int i = 0; i < this.playerNameLength; ++i) {
                    buf.writeChar(this.playerName[i]);
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream out2 = new ObjectOutputStream(out);
                out2.writeObject(this.grade);
                buf.writeBytes(out.toByteArray());
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
        }

        public static class MsgResponseHandler
        implements IMessageHandler<MsgResponse, IMessage> {
            @Override
            public IMessage onMessage(MsgResponse message, MessageContext ctx) {
                if (message == null) {
                    return null;
                }
                try {
                    IGrade grade = message.getGrade();
                    if (grade == null) {
                        LogManager.getLogger().info("Received empty grade data.");
                        return null;
                    }
                    grade.exportGradeStripToFile(message.getPlayerName());
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }

    public static class MsgGRHandler
    implements IMessageHandler<MsgGradeRequest, MsgResponse> {
        @Override
        public MsgResponse onMessage(MsgGradeRequest message, MessageContext ctx) {
            if (!ORTWorld.hasMatchServer()) {
                return null;
            }
            ORTWorld instance = ORTWorld.getMatchInstanceServer();
            try {
                EntityPlayerMP player = ctx.getServerHandler().field_147369_b;
                ORTPlayer p = ORTWorld.getMatchInstanceServer().players.get(player.func_70005_c_());
                if (p == null) {
                    Util.sendMessage(player, "Action failed. You are not registered in the player roster.");
                    return null;
                }
                IPlayerGroup group = ORTWorld.getMatchInstanceServer().match.getGroupByPlayer(player.func_70005_c_(), false);
                if (group == null) {
                    return null;
                }
                IGrade g = group.computeGroupGrade(instance.players, instance.courses);
                if (g == null) {
                    Util.sendMessage(player, "Your grade is not available/visible now.");
                    return null;
                }
                if (!instance.grades.containsKey(group.getGroupName())) {
                    instance.grades.remove(group.getGroupName());
                }
                instance.grades.put(group.getGroupName(), g);
                g.printGradeStripToScreen(player);
                return new MsgResponse(g);
            }
            catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}

