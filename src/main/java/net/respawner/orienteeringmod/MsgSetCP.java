/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.respawner.orienteeringmod.TileEntityCheckpoint;
import org.apache.logging.log4j.LogManager;

public class MsgSetCP
implements IMessage {
    private int x;
    private int y;
    private int z;
    private int code;

    public MsgSetCP() {
    }

    public MsgSetCP(BlockPos pos, int code) {
        this.x = pos.func_177958_n();
        this.y = pos.func_177956_o();
        this.z = pos.func_177952_p();
        this.code = code;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            this.x = buf.readInt();
            this.y = buf.readInt();
            this.z = buf.readInt();
            this.code = buf.readInt();
        }
        catch (Throwable e) {
            LogManager.getLogger().error(e.toString());
            this.code = 0;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeInt(this.code);
    }

    public static class MsgSetCPHandler
    implements IMessageHandler<MsgSetCP, IMessage> {
        @Override
        public IMessage onMessage(MsgSetCP message, MessageContext ctx) {
            WorldServer world = ctx.getServerHandler().field_147369_b.func_71121_q();
            TileEntity cp = world.func_175625_s(new BlockPos(message.x, message.y, message.z));
            if (cp instanceof TileEntityCheckpoint) {
                ((TileEntityCheckpoint)cp).setCodeAndUpdate(message.code);
            }
            return null;
        }
    }
}

