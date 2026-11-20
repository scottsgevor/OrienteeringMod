/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

public class ORTException
extends Exception {
    private static final long serialVersionUID = 4L;

    public ORTException(String msg) {
        super(msg);
    }

    @Override
    @Deprecated
    public void printStackTrace() {
        throw new NoSuchMethodError();
    }
}

