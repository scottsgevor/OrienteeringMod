/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import net.respawner.orienteeringmod.MatchGeneration;
import net.respawner.orienteeringmod.ORTPlayer;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.StatOutput;

public class Debugging {
    public static void main(String[] args) throws Throwable {
        MatchGeneration.generateAndSave("Debugging", "AAAAA");
        ORTWorld.loadMatch("Debugging", true);
        ORTWorld instance = ORTWorld.getMatchInstanceServer();
        instance.onPlayerJoin("B-1");
        ORTPlayer b1 = instance.players.get("B-1");
        instance.onPlayerJoin("B-2");
        ORTPlayer b2 = instance.players.get("B-2");
        instance.onPlayerJoin("B-3");
        ORTPlayer b3 = instance.players.get("B-3");
        instance.onPlayerJoin("A-1");
        ORTPlayer a1 = instance.players.get("A-1");
        instance.onPlayerJoin("A-3");
        ORTPlayer a3 = instance.players.get("A-3");
        instance.onPlayerJoin("AAAAA");
        instance.match.checkRuleBeforeStarting(b1, instance);
        b1.start(0L);
        b1.checkpoint(36, 20L);
        b1.checkpoint(37, 40L);
        b1.checkpoint(38, 60L);
        b1.addDeath();
        b1.checkpoint(39, 80L);
        b1.checkpoint(40, 100L);
        b1.checkpoint(41, 110L);
        b1.end(120L);
        try {
            instance.match.checkRuleBeforeStarting(b3, instance);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        instance.match.checkRuleBeforeStarting(b2, instance);
        b2.start(140L);
        b2.checkpoint(36, 160L);
        try {
            instance.match.checkRuleBeforeStarting(b3, instance);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        b2.addDeath();
        b2.abort();
        instance.match.checkRuleBeforeStarting(b3, instance);
        b3.start(180L);
        b3.checkpoint(45, 200L);
        b3.checkpoint(46, 220L);
        b3.end(240L);
        instance.match.checkRuleBeforeStarting(a1, instance);
        a1.start(0L);
        a1.checkpoint(31, 20L);
        a1.checkpoint(32, 40L);
        a1.abort();
        instance.match.checkRuleBeforeStarting(a3, instance);
        a3.start(60L);
        a3.checkpoint(42, 80L);
        a3.checkpoint(43, 100L);
        a3.checkpoint(44, 120L);
        a3.end(140L);
        instance.onPlayerJoin("A-2");
        ORTPlayer a2 = instance.players.get("A-2");
        try {
            instance.match.checkRuleBeforeStarting(a2, instance);
            a2.start(160L);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        ORTWorld.saveAllDataToFile();
        StatOutput.output(instance.matchName);
        System.out.println("" + instance.match.isAdmin("Respawner__"));
    }
}

