/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import net.minecraft.entity.player.EntityPlayer;
import net.respawner.orienteeringmod.ORTException;
import net.respawner.orienteeringmod.Util;

class ORTIngameException
extends ORTException {
    private static final long serialVersionUID = 1L;

    public ORTIngameException(String msg) {
        super(msg);
    }

    public ORTIngameException(EntityPlayer player, String msg) {
        super(msg);
        Util.sendMessage(player, msg);
    }
}

