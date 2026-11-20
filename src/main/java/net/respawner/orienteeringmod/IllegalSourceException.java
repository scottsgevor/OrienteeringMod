/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import net.respawner.orienteeringmod.ORTFileException;

class IllegalSourceException
extends ORTFileException {
    private static final long serialVersionUID = 1L;

    public IllegalSourceException(ORTFileException.FileType type, String msg) {
        super(type.toString() + " error: " + msg);
    }
}

