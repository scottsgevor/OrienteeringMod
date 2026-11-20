/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.respawner.orienteeringmod.ORTIngameException;
import net.respawner.orienteeringmod.ORTPlayer;
import net.respawner.orienteeringmod.ORTWorld;

public class MsgMatchClient
implements IMessage {
    private int matchNameLength;
    private char[] matchName;
    private int courseNameLength;
    private char[] courseName;
    private boolean drawCompass;
    private boolean displayTime;
    private boolean isAdmin;
    private boolean isSpectator;
    private int playerStatus;

    public MsgMatchClient() {
    }

    public boolean isAdmin() {
        return this.isAdmin;
    }

    public MsgMatchClient(String match, String course, boolean drawCompass, boolean nightVision, boolean displayTime, boolean isAdmin, boolean isSpectator, ORTPlayer.PlayerStatus status) {
        this.matchNameLength = match.length();
        this.matchName = match.toCharArray();
        this.courseNameLength = course.length();
        this.courseName = course.toCharArray();
        this.drawCompass = drawCompass;
        this.displayTime = displayTime;
        this.isAdmin = isAdmin;
        this.isSpectator = isSpectator;
        this.playerStatus = MsgMatchClient.encodeStatus(status);
    }

    public static MsgMatchClient CreateNewMsg(ORTWorld instance, String playerName) throws ORTIngameException {
        String course = instance.match.getCourseByPlayer(playerName);
        if (course == null) {
            throw new ORTIngameException("no course found matching the player's name.");
        }
        return new MsgMatchClient(instance.matchName, course, instance.match.needDrawCompass(), instance.match.needNightVision(), instance.courses.get(course).displayTime(), instance.match.isAdmin(playerName), instance.match.isSpectator(playerName), instance.players.get(playerName).getPlayerStatus());
    }

    public String getMatchName() {
        return String.copyValueOf(this.matchName);
    }

    public String getCourseName() {
        return String.copyValueOf(this.courseName);
    }

    public boolean needDrawCompass() {
        return this.drawCompass;
    }

    public boolean displayTime() {
        return this.displayTime;
    }

    public static ORTPlayer.PlayerStatus decodeStatus(int code) {
        return code == 0 ? ORTPlayer.PlayerStatus.Unstarted : (code == 1 ? ORTPlayer.PlayerStatus.InProgress : (code == 2 ? ORTPlayer.PlayerStatus.Finished : ORTPlayer.PlayerStatus.Aborted));
    }

    public ORTPlayer.PlayerStatus decodeStatus() {
        return MsgMatchClient.decodeStatus(this.playerStatus);
    }

    public static int encodeStatus(ORTPlayer.PlayerStatus status) {
        switch (status) {
            case Unstarted: {
                return 0;
            }
            case InProgress: {
                return 1;
            }
            case Finished: {
                return 2;
            }
        }
        return 3;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            int i;
            this.matchNameLength = buf.readInt();
            this.matchName = new char[this.matchNameLength];
            for (i = 0; i < this.matchNameLength; ++i) {
                this.matchName[i] = buf.readChar();
            }
            this.courseNameLength = buf.readInt();
            this.courseName = new char[this.courseNameLength];
            for (i = 0; i < this.courseNameLength; ++i) {
                this.courseName[i] = buf.readChar();
            }
            this.drawCompass = buf.readBoolean();
            this.displayTime = buf.readBoolean();
            this.isAdmin = buf.readBoolean();
            this.isSpectator = buf.readBoolean();
            this.playerStatus = buf.readInt();
        }
        catch (Throwable e) {
            e.printStackTrace();
            this.matchName = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            int i;
            buf.writeInt(this.matchNameLength);
            for (i = 0; i < this.matchNameLength; ++i) {
                buf.writeChar(this.matchName[i]);
            }
            buf.writeInt(this.courseNameLength);
            for (i = 0; i < this.courseNameLength; ++i) {
                buf.writeChar(this.courseName[i]);
            }
            buf.writeBoolean(this.drawCompass);
            buf.writeBoolean(this.displayTime);
            buf.writeBoolean(this.isAdmin);
            buf.writeBoolean(this.isSpectator);
            buf.writeInt(this.playerStatus);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static class MsgPJHandler
    implements IMessageHandler<MsgMatchClient, IMessage> {
        @Override
        public IMessage onMessage(MsgMatchClient message, MessageContext ctx) {
            if (message.getMatchName() != null) {
                ORTWorld.setMatchClient(message);
            }
            return null;
        }
    }
}

