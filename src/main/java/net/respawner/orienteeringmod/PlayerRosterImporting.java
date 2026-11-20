/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import net.respawner.orienteeringmod.IPlayerGroup;
import net.respawner.orienteeringmod.IllegalSourceException;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTMatch;
import net.respawner.orienteeringmod.PlayerGroupRelay;
import net.respawner.orienteeringmod.PlayerGroupSingle;
import net.respawner.orienteeringmod.PlayerGroupTeam;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public class PlayerRosterImporting {
    public static Set<IPlayerGroup> importRoster(String worldName, ORTMatch.MatchType type) throws Throwable {
        HashSet<IPlayerGroup> out = new HashSet<IPlayerGroup>();
        String path = Util.getBasicDataPath(worldName) + "roster.json";
        try {
            JsonObject seed = (JsonObject)new JsonParser().parse(new FileReader(path));
            JsonArray array = seed.get("groups").getAsJsonArray();
            for (int i = 0; i < array.size(); ++i) {
                IPlayerGroup group;
                JsonObject jsonGroup = array.get(i).getAsJsonObject();
                JsonArray jsonCourses = jsonGroup.get("courses").getAsJsonArray();
                ArrayList<String> courses = new ArrayList<String>();
                for (int j = 0; j < jsonCourses.size(); ++j) {
                    courses.add(jsonCourses.get(j).getAsString());
                }
                switch (type) {
                    case Single: {
                        group = new PlayerGroupSingle();
                        if (jsonCourses.size() <= 1) break;
                        throw new IllegalSourceException(ORTFileException.FileType.Roster, "single-player match cannot have more than one people in a group.");
                    }
                    case Relay: {
                        group = new PlayerGroupRelay();
                        if (jsonCourses.size() != 1) break;
                        throw new IllegalSourceException(ORTFileException.FileType.Roster, "relay match must have at least two people in a group.");
                    }
                    case Team: {
                        if (jsonCourses.size() > 1) {
                            throw new IllegalSourceException(ORTFileException.FileType.Roster, "there can only be one course under relay match setting.");
                        }
                        HashSet<String> players = new HashSet<String>();
                        JsonArray jsonPlayers = jsonGroup.get("players").getAsJsonArray();
                        for (int j = 0; j < jsonPlayers.size(); ++j) {
                            players.add(jsonPlayers.get(j).getAsString());
                        }
                        group = new PlayerGroupTeam(players.toArray(new String[players.size()]));
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException();
                    }
                }
                group.init(jsonGroup.get("name").getAsString(), courses);
                out.add(group);
            }
            LogManager.getLogger().info("Player roster exported. Group number: " + array.size());
        }
        catch (Throwable e) {
            if (e instanceof ORTFileException) {
                throw e;
            }
            e.printStackTrace();
            throw new ORTFileException(ORTFileException.FileType.Roster, ORTFileException.IOType.Read, null);
        }
        return out;
    }
}

