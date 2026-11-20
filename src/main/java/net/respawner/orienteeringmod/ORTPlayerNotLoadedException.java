/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import java.util.List;
import net.respawner.orienteeringmod.ORTFileException;

class ORTPlayerNotLoadedException
extends ORTFileException {
    private static final long serialVersionUID = 1L;

    public ORTPlayerNotLoadedException(ORTFileException.FileType file, ORTFileException.IOType type, List<String> names) {
        super(file, type, "", names);
    }
}

