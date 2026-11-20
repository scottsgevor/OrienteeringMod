/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import net.minecraft.entity.player.EntityPlayer;
import net.respawner.orienteeringmod.ORTFileException;

class ORTMatchDNEException
extends ORTFileException {
    private static final long serialVersionUID = 1L;

    public ORTMatchDNEException() {
        super("File Error: Match data does not exist or cannot be loaded.");
    }

    public ORTMatchDNEException(EntityPlayer player) {
        super(player, "File Error: Match data does not exist or cannot be loaded.");
    }
}

