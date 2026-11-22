/*
 * Decompiled with CFR 0.152.
 */
package net.respawner.orienteeringmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.respawner.orienteeringmod.IPlayerGroup;
import net.respawner.orienteeringmod.IllegalSourceException;
import net.respawner.orienteeringmod.ORTFileException;
import net.respawner.orienteeringmod.ORTIngameException;
import net.respawner.orienteeringmod.ORTPlayer;
import net.respawner.orienteeringmod.ORTWorld;
import net.respawner.orienteeringmod.PlayerGroupRelay;
import net.respawner.orienteeringmod.PlayerGroupSingle;
import net.respawner.orienteeringmod.PlayerGroupTeam;
import net.respawner.orienteeringmod.PlayerRosterImporting;
import net.respawner.orienteeringmod.Util;
import org.apache.logging.log4j.LogManager;

public class ORTMatch
implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Set<String> courseList;
    private final MatchType matchType;
    private String defaultCourse;
    private final boolean useRoster;
    private final Set<IPlayerGroup> playerRoster;
    private final boolean announceStart;
    private boolean isMatchStarted;
    private final DeathPolicy deathPolicy;
    private final int deathPenalty;
    private Integer backPenalty;
    private final int spawnRadius;
    private final boolean drawCompass;
    private final boolean nightVision;
    private Boolean customFallDamage;
    protected final int[] spawn;
    private final Set<String> admins;
    private Set<String> spectators;
    private WorldMode wMode;

    public Set<String> getCourseList() {
        return this.courseList;
    }

    public int getCourseNumber() {
        return this.courseList.size();
    }

    public MatchType getMatchType() {
        return this.matchType;
    }

    public DeathPolicy getDeathPolicy() {
        return this.deathPolicy;
    }

    public int getDeathPenalty() {
        return this.deathPenalty;
    }

    public int getBackPenalty() {
        return this.backPenalty == null ? 5 : this.backPenalty.intValue();
    }

    public int getSpawnRadius() {
        return this.spawnRadius;
    }

    public WorldMode getWorldMode() {
        return this.wMode;
    }

    public int[] getSpawnPos() {
        return this.spawn;
    }

    public boolean needDrawCompass() {
        return this.drawCompass;
    }

    public boolean needNightVision() {
        return this.nightVision;
    }

    public boolean useCustomFallDamage() {
        return this.customFallDamage == null || this.customFallDamage.booleanValue();
    }

    public void addCourse(String name) {
        this.courseList.add(name);
    }

    public String getDefaultCourse() {
        return this.defaultCourse;
    }

    public boolean useRoster() {
        return this.useRoster;
    }

    public Set<IPlayerGroup> getPlayerRoster() {
        return this.playerRoster;
    }

    public void addGroupToRoster(IPlayerGroup group) {
        this.playerRoster.add(group);
    }

    public boolean announceStart() {
        return this.announceStart;
    }

    public boolean isMatchStarted() {
        return this.isMatchStarted;
    }

    public void setMatchStart(boolean status) {
        this.isMatchStarted = status;
    }

    @Nullable
    public IPlayerGroup registerNewPlayer(String playerName) throws ORTIngameException {
        if (this.isSpectator(playerName)) {
            return null;
        }
        if (this.useRoster && !this.isAdmin(playerName)) {
            throw new ORTIngameException("In the presence of player roster, the new player cannot be registered.");
        }
        PlayerGroupSingle group = new PlayerGroupSingle();
        group.init(playerName, Arrays.asList(this.getDefaultCourse()));
        this.playerRoster.add(group);
        return group;
    }

    @Nullable
    public IPlayerGroup getGroupByPlayer(String playerName, boolean doCreate) throws ORTIngameException {
        for (IPlayerGroup p : this.playerRoster) {
            if (!p.containsPlayer(playerName)) continue;
            return p;
        }
        if (doCreate) {
            return this.registerNewPlayer(playerName);
        }
        return null;
    }

    public boolean isRosterContainsPlayer(String playerName) {
        for (IPlayerGroup p : this.playerRoster) {
            if (!p.containsPlayer(playerName)) continue;
            return true;
        }
        return false;
    }

    @Nullable
    public String getCourseByPlayer(String playerName) {
        IPlayerGroup group = null;
        try {
            group = this.getGroupByPlayer(playerName, false);
        }
        catch (Throwable e) {
            return null;
        }
        if (group == null) {
            return null;
        }
        String course = group.getCourse(playerName);
        return course;
    }

    public void changeCourse(String playerName, String newCourse) throws ORTIngameException {
        if (this.useRoster() && !this.isAdmin(playerName)) {
            throw new ORTIngameException("Error: in the presence of player roster, you cannot change your course.");
        }
        if (!this.getCourseList().contains(newCourse)) {
            throw new ORTIngameException("Error: cannot find the course in the course list.");
        }
        try {
            IPlayerGroup group = this.getGroupByPlayer(playerName, false);
            if (group == null) {
                throw new ORTIngameException("Error: you are not registered.");
            }
            if (!(group instanceof PlayerGroupSingle)) {
                throw new ORTIngameException("Error: relay match does not allow changing course.");
            }
            PlayerGroupSingle group2 = (PlayerGroupSingle)group;
            group2.setCourse(newCourse);
        }
        catch (Throwable e) {
            if (e instanceof ORTIngameException) {
                throw e;
            }
            LogManager.getLogger().error(e.toString());
            throw new ORTIngameException("Error: action Failed. Unknown Reason.");
        }
    }

    @Nonnull
    public String changeMode() {
        WorldMode previous = this.wMode;
        switch (this.wMode) {
            case Training: {
                this.wMode = WorldMode.Match;
                break;
            }
            case Match: {
                this.wMode = WorldMode.Admin;
                break;
            }
            default: {
                this.wMode = WorldMode.Training;
                break;
            }
        }
        return previous.toString() + "->" + this.wMode.toString();
    }

    public void checkRuleBeforeStarting(ORTPlayer player, ORTWorld instance) throws ORTIngameException {
        if (this.announceStart && !this.isMatchStarted) {
            throw new ORTIngameException("The match is currently not opened.");
        }
        if (this.wMode == WorldMode.Match && player.isStarted()) {
            throw new ORTIngameException("You have already run a match. You cannot start another one.");
        }
        if (this.getCourseByPlayer(player.playerName) == null) {
            throw new ORTIngameException("No course is assigned to you. You cannot start a match.");
        }
        try {
            switch (this.matchType) {
                case Single: {
                    PlayerGroupSingle p = (PlayerGroupSingle)this.getGroupByPlayer(player.playerName, false);
                    p.containsPlayer(player.playerName);
                    break;
                }
                case Relay: {
                    PlayerGroupRelay group = (PlayerGroupRelay)this.getGroupByPlayer(player.playerName, false);
                    if (group == null) {
                        throw new ORTIngameException("You are not registered. You cannot start a match.");
                    }
                    ORTPlayer first = instance.getPlayerByName(group.getPlayerByTurn(0));
                    if (first == null || first != player && !first.isEnded()) {
                        throw new ORTIngameException("The first player has not finished yet. You cannot start.");
                    }
                    ORTPlayer previous = instance.getPlayerByName(group.getPlayerByTurn(group.getTurn(player.playerName) - 1));
                    if (previous != null && !previous.isEnded()) {
                        throw new ORTIngameException("The previous player has not finished yet. You cannot start.");
                    }
                    for (int i = group.getTurn(player.playerName) + 1; i < group.size(); ++i) {
                        ORTPlayer next = instance.getPlayerByName(group.getPlayerByTurn(i));
                        if (next == null || !next.isStarted()) continue;
                        throw new ORTIngameException("People behind you have started. You have no chance to start now.");
                    }
                    break;
                }
                case Team: {
                    PlayerGroupTeam team = (PlayerGroupTeam)this.getGroupByPlayer(player.playerName, false);
                    team.containsPlayer(player.playerName);
                    break;
                }
                default: {
                    throw new IllegalArgumentException();
                }
            }
        }
        catch (Throwable e) {
            if (e instanceof ORTIngameException) {
                throw e;
            }
            LogManager.getLogger().error(e.toString());
            throw new ORTIngameException("Error: action Failed. Unknown Reason.");
        }
    }

    public ORTMatch(JsonObject seed, String worldName) throws ORTFileException {
        try {
            this.courseList = new HashSet<String>();
            this.matchType = Objects.requireNonNull(MatchType.fromString(seed.get("matchtype").getAsString()));
            this.useRoster = seed.get("useplayerroster").getAsBoolean();
            this.announceStart = seed.get("announcestart").getAsBoolean();
            this.isMatchStarted = false;
            this.playerRoster = this.useRoster ? PlayerRosterImporting.importRoster(worldName, this.matchType) : new HashSet<IPlayerGroup>();
            this.defaultCourse = seed.get("defaultcourse").getAsString();
            if (this.matchType != MatchType.Single && !this.useRoster) {
                throw new IllegalSourceException(ORTFileException.FileType.Matchseed, "relay/team match must use player roster.");
            }
            this.deathPolicy = Objects.requireNonNull(DeathPolicy.fromString(seed.get("deathpolicy").getAsString()));
            if (this.deathPolicy == DeathPolicy.Penalty) {
                this.deathPenalty = seed.get("deathpenalty").getAsInt();
                this.spawnRadius = seed.get("spawnradius").getAsInt();
            } else {
                this.deathPenalty = 0;
                this.spawnRadius = 0;
            }
            this.backPenalty = seed.has("backpenalty")
                    ? Integer.valueOf(Math.max(0, seed.get("backpenalty").getAsInt()))
                    : Integer.valueOf(5);
            this.drawCompass = seed.get("drawcompass").getAsBoolean();
            this.nightVision = seed.get("nightvision").getAsBoolean();
            this.customFallDamage = seed.has("customfalldamage")
                    ? Boolean.valueOf(seed.get("customfalldamage").getAsBoolean())
                    : Boolean.TRUE;
            this.spawn = new int[3];
            JsonArray jsonSpawn = seed.get("spawn").getAsJsonArray();
            for (int i = 0; i < 3; ++i) {
                this.spawn[i] = jsonSpawn.get(i).getAsInt();
            }
            this.admins = new HashSet<String>();
            JsonArray jsonAdmins = seed.get("admin").getAsJsonArray();
            for (int i = 0; i < jsonAdmins.size(); ++i) {
                this.admins.add(jsonAdmins.get(i).getAsString());
            }
            this.spectators = new HashSet<String>();
            JsonArray jsonSpectators = seed.get("spectator").getAsJsonArray();
            for (int i = 0; i < jsonSpectators.size(); ++i) {
                this.spectators.add(jsonSpectators.get(i).getAsString());
            }
            this.wMode = Objects.requireNonNull(WorldMode.fromString(seed.get("worldmode").getAsString()));
        }
        catch (Throwable e) {
            if (e instanceof ORTFileException) {
                throw (ORTFileException)e;
            }
            if (e instanceof NullPointerException) {
                throw new ORTFileException(ORTFileException.FileType.Matchseed, ORTFileException.IOType.Read, "please check spelling of json syntax.");
            }
            LogManager.getLogger().error(e.toString());
            throw new ORTFileException(ORTFileException.FileType.Matchseed, ORTFileException.IOType.Read, e.getMessage());
        }
    }

    @Nonnull
    private DeathPolicy getDeathPolicyFromString(String str) {
        return str.equals("penalty") ? DeathPolicy.Penalty : DeathPolicy.Abort;
    }

    @Nonnull
    private WorldMode getWModeFromString(String str) {
        return str.equals("training") ? WorldMode.Training : (str.equals("match") ? WorldMode.Match : WorldMode.Admin);
    }

    public boolean isAdmin(String playerName) {
        if (this.wMode == WorldMode.Admin) {
            return true;
        }
        if (this.admins.contains(playerName)) {
            return true;
        }
        return ORTMatch.isServerOp(playerName);
    }

    private static boolean isServerOp(String playerName) {
        try {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server == null) {
                return false;
            }
            EntityPlayerMP player = server.func_184103_al().func_152612_a(playerName);
            return player != null && server.func_184103_al().func_152596_g(player.func_146103_bH());
        }
        catch (Throwable e) {
            return false;
        }
    }

    public boolean isSpectator(String playerName) {
        return this.spectators.contains(playerName);
    }

    @Nonnull
    public static ORTMatch readFromFile(String matchName) throws ORTFileException {
        ORTMatch match = null;
        String fileName = Util.getMatchFilePath(matchName);
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
            match = (ORTMatch)in.readObject();
            in.close();
        }
        catch (Throwable e) {
            LogManager.getLogger().error(e.toString());
            throw new ORTFileException(ORTFileException.FileType.Match, ORTFileException.IOType.Read, e.getMessage());
        }
        return match;
    }

    public static void writeToFile(String matchName, ORTMatch match) throws ORTFileException {
        String fileName = Util.getMatchFilePath(matchName);
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(match);
            out.flush();
            out.close();
        }
        catch (Throwable e) {
            LogManager.getLogger().error(e.toString());
            throw new ORTFileException(ORTFileException.FileType.Match, ORTFileException.IOType.Write, e.getMessage());
        }
    }

    public void writeToFile(String matchName) throws ORTFileException {
        ORTMatch.writeToFile(matchName, this);
    }

    public static enum WorldMode {
        Training,
        Match,
        Admin;


        @Nullable
        public static WorldMode fromString(String str) {
            return str.equals("training") ? Training : (str.equals("match") ? Match : (str.equals("admin") ? Admin : null));
        }
    }

    public static enum DeathPolicy {
        Abort,
        Penalty;


        @Nullable
        public static DeathPolicy fromString(String str) {
            return str.equals("abort") ? Abort : (str.equals("penalty") ? Penalty : null);
        }
    }

    public static enum MatchType {
        Single,
        Relay,
        Team;


        @Nullable
        public static MatchType fromString(String str) {
            return str.equals("single") ? Single : (str.equals("relay") ? Relay : (str.equals("team") ? Team : null));
        }

        public boolean isSingle() {
            return this == Single;
        }
    }
}

