/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityCheckpoint
extends TileEntity {
    private int code;

    public TileEntityCheckpoint() {
        this.code = 0;
    }

    public TileEntityCheckpoint(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public void setCodeAndUpdate(int code) {
        this.code = code;
        this.func_145831_w().func_184138_a(this.func_174877_v(), this.func_145838_q().func_176223_P(), this.func_145838_q().func_176223_P(), 3);
        this.func_70296_d();
    }

    @Override
    public void func_145839_a(NBTTagCompound tagCompound) {
        super.func_145839_a(tagCompound);
        this.code = tagCompound.func_74762_e("CP_Code");
    }

    @Override
    public NBTTagCompound func_189515_b(NBTTagCompound tagCompound) {
        super.func_189515_b(tagCompound);
        tagCompound.func_74768_a("CP_Code", this.code);
        return tagCompound;
    }

    @Override
    public NBTTagCompound func_189517_E_() {
        return this.func_189515_b(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.func_145839_a(tag);
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity func_189518_D_() {
        return new SPacketUpdateTileEntity(this.field_174879_c, 1, this.func_189517_E_());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.func_145839_a(packet.func_148857_g());
    }
}

