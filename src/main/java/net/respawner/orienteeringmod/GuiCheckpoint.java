/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.respawner.orienteeringmod.MsgSetCP;
import net.respawner.orienteeringmod.OrienteeringMod;
import net.respawner.orienteeringmod.TileEntityCheckpoint;

@SideOnly(value=Side.CLIENT)
public class GuiCheckpoint
extends GuiScreen {
    private GuiTextField codeInput;
    private GuiButton done;
    private GuiButton cancel;
    TileEntityCheckpoint entityCP;
    EntityPlayer player;
    World world;
    BlockPos pos;

    public GuiCheckpoint(TileEntityCheckpoint entityCP, EntityPlayer player, Minecraft mc) {
        this.entityCP = entityCP;
        this.player = player;
        this.field_146297_k = mc;
        this.field_146289_q = this.field_146297_k.field_71466_p;
    }

    @Override
    public void func_73876_c() {
        this.codeInput.func_146178_a();
    }

    @Override
    public void func_73866_w_() {
        this.field_146292_n.clear();
        this.codeInput = new GuiTextField(1, this.field_146289_q, this.field_146294_l / 2 - 62, this.field_146295_m / 2 - 10, 124, 20);
        this.codeInput.func_146195_b(true);
        this.codeInput.func_146203_f(3);
        this.done = this.func_189646_b(new GuiButton(2, this.field_146294_l / 2 - 62, this.field_146295_m / 2 + 14, 60, 20, "Done"));
        this.cancel = this.func_189646_b(new GuiButton(3, this.field_146294_l / 2 + 2, this.field_146295_m / 2 + 14, 60, 20, "Cancel"));
        this.done.field_146124_l = false;
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int X, int Y) {
        int rectWidth = 132;
        int rectHeight = 80;
        this.field_146297_k.func_110434_K().func_110577_a(new ResourceLocation("orienteeringmod", "textures/gui/checkpoint.png"));
        this.func_73729_b((this.field_146294_l - rectWidth) / 2, (this.field_146295_m - rectHeight) / 2, 0, 0, rectWidth, rectHeight);
    }

    @Override
    protected void func_146284_a(GuiButton button) throws IOException {
        if (!button.field_146124_l) {
            return;
        }
        if (button.field_146127_k == 2) {
            OrienteeringMod.NETHANDLER.sendToServer(new MsgSetCP(this.entityCP.func_174877_v(), Integer.parseInt(this.codeInput.func_146179_b())));
            this.field_146297_k.func_147108_a(null);
        }
        if (button.field_146127_k == 3) {
            this.field_146297_k.func_147108_a(null);
        }
    }

    @Override
    protected void func_73869_a(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            this.func_146284_a(this.cancel);
        }
        if (keyCode == 28 || keyCode == 156) {
            this.func_146284_a(this.done);
        }
        if ('0' <= typedChar && typedChar <= '9' || keyCode == 14 || keyCode == 211) {
            this.codeInput.func_146201_a(typedChar, keyCode);
        }
    }

    @Override
    protected void func_73864_a(int x, int y, int button) throws IOException {
        super.func_73864_a(x, y, button);
        this.codeInput.func_146192_a(x, y, button);
    }

    @Override
    public void func_146281_b() {
    }

    @Override
    public void func_73863_a(int X, int Y, float partialTicks) {
        this.func_146276_q_();
        this.codeInput.func_146194_f();
        this.func_73732_a(this.field_146289_q, "Input the checkpoint code:", this.field_146294_l / 2, this.field_146295_m / 2 - 24, 0xFFFFFF);
        if (!this.codeInput.func_146179_b().equals("")) {
            this.done.field_146124_l = true;
        }
        super.func_73863_a(X, Y, partialTicks);
    }
}

