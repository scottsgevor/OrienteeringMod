package net.respawner.orienteeringmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MsgMapImageRequest implements IMessage {
    private String matchName = "";
    private String imageName = "";

    public MsgMapImageRequest() {
    }

    public MsgMapImageRequest(String matchName, String imageName) {
        this.matchName = matchName;
        this.imageName = imageName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        try {
            this.matchName = buffer.func_150789_c(256);
            this.imageName = buffer.func_150789_c(256);
        }
        catch (Throwable e) {
            this.matchName = "";
            this.imageName = "";
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.func_180714_a(this.matchName);
        buffer.func_180714_a(this.imageName);
    }

    public static class MsgMIRHandler implements IMessageHandler<MsgMapImageRequest, IMessage> {
        @Override
        public IMessage onMessage(MsgMapImageRequest message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().field_147369_b;
            MapImageService.handleRequest(player, message.matchName, message.imageName);
            return null;
        }
    }
}