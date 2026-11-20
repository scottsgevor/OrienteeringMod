/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.respawner.orienteeringmod.OrienteeringMod;

public class MsgTimePenalty
implements IMessage {
    private long penalty;

    public MsgTimePenalty() {
    }

    public long getPenalty() {
        return this.penalty;
    }

    public MsgTimePenalty(long penalty) {
        this.penalty = penalty;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            this.penalty = buf.readLong();
        }
        catch (Throwable e) {
            e.printStackTrace();
            this.penalty = 0L;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            buf.writeLong(this.penalty);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static class MsgTPHandler
    implements IMessageHandler<MsgTimePenalty, IMessage> {
        @Override
        public IMessage onMessage(MsgTimePenalty message, MessageContext ctx) {
            OrienteeringMod.IngameResponseClient.addDeath(message.penalty);
            return null;
        }
    }
}

