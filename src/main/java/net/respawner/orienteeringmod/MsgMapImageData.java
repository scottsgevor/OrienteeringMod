package net.respawner.orienteeringmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MsgMapImageData implements IMessage {
    private static final int MAX_CHUNK_SIZE = 256 * 1024;
    private String matchName = "";
    private String imageName = "";
    private int totalSize;
    private int offset;
    private byte[] data = new byte[0];

    public MsgMapImageData() {
    }

    public MsgMapImageData(String matchName, String imageName, int totalSize, int offset, byte[] data) {
        this.matchName = matchName;
        this.imageName = imageName;
        this.totalSize = totalSize;
        this.offset = offset;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        try {
            this.matchName = buffer.func_150789_c(256);
            this.imageName = buffer.func_150789_c(256);
            this.totalSize = buffer.readInt();
            this.offset = buffer.readInt();
            int length = buffer.readInt();
            if (length < 0 || length > MAX_CHUNK_SIZE) {
                this.data = new byte[0];
                return;
            }
            this.data = new byte[length];
            buffer.readBytes(this.data);
        }
        catch (Throwable e) {
            this.data = new byte[0];
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.func_180714_a(this.matchName);
        buffer.func_180714_a(this.imageName);
        buffer.writeInt(this.totalSize);
        buffer.writeInt(this.offset);
        buffer.writeInt(this.data == null ? 0 : this.data.length);
        if (this.data != null) {
            buffer.writeBytes(this.data);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class MsgMIDHandler implements IMessageHandler<MsgMapImageData, IMessage> {
        @Override
        public IMessage onMessage(MsgMapImageData message, MessageContext ctx) {
            MapImageClientCache.receiveChunk(message.matchName, message.imageName, message.totalSize, message.offset, message.data);
            return null;
        }
    }

    public static class ServerNoopHandler implements IMessageHandler<MsgMapImageData, IMessage> {
        @Override
        public IMessage onMessage(MsgMapImageData message, MessageContext ctx) {
            return null;
        }
    }
}