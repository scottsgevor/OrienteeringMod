package net.respawner.orienteeringmod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;

@SideOnly(Side.CLIENT)
public final class CompassClientState {
    private static final File CONFIG_FILE = new File("config", "orienteering-client.cfg");
    private static final String POSITION_KEY = "compassPosition";
    private static final String VISIBLE_KEY = "compassVisible";

    private static Position position = Position.BOTTOM_RIGHT;
    private static boolean visible = true;

    static {
        load();
    }

    private CompassClientState() {
    }

    public static void cyclePosition() {
        position = position.next();
        save();
    }

    public static void toggleVisible() {
        visible = !visible;
        save();
    }

    public static int positionIndex() {
        return position.ordinal();
    }

    public static boolean isVisible() {
        return visible;
    }

    private static void load() {
        if (!CONFIG_FILE.isFile()) {
            return;
        }
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            properties.load(in);
        }
        catch (IOException e) {
            LogManager.getLogger().error("Failed to read client config " + CONFIG_FILE.getAbsolutePath(), e);
            return;
        }
        try {
            position = Position.valueOf(properties.getProperty(POSITION_KEY, Position.BOTTOM_RIGHT.name()));
        }
        catch (IllegalArgumentException e) {
            position = Position.BOTTOM_RIGHT;
        }
        visible = Boolean.parseBoolean(properties.getProperty(VISIBLE_KEY, "true"));
    }

    private static void save() {
        File parent = CONFIG_FILE.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs() && !parent.isDirectory()) {
            LogManager.getLogger().error("Could not create config directory: " + parent.getAbsolutePath());
            return;
        }
        Properties properties = new Properties();
        properties.setProperty(POSITION_KEY, position.name());
        properties.setProperty(VISIBLE_KEY, Boolean.toString(visible));
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            properties.store(out, "OrienteeringMod client HUD settings.");
        }
        catch (IOException e) {
            LogManager.getLogger().error("Failed to write client config " + CONFIG_FILE.getAbsolutePath(), e);
        }
    }

    public enum Position {
        TOP_RIGHT,
        TOP_LEFT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT;

        Position next() {
            Position[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
}