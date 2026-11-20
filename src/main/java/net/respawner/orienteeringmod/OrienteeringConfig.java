package net.respawner.orienteeringmod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;

public final class OrienteeringConfig {
    public static final boolean DEFAULT_FORCE_FOOD_LEVEL = false;

    private static final File CONFIG_FILE = new File("config", "orienteering.cfg");
    private static boolean forceFoodLevel = DEFAULT_FORCE_FOOD_LEVEL;

    static {
        Properties properties = new Properties();
        boolean exists = CONFIG_FILE.isFile();
        if (exists) {
            try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                properties.load(in);
            } catch (IOException e) {
                LogManager.getLogger().error("Failed to read Orienteering config " + CONFIG_FILE.getAbsolutePath(), e);
            }
        }
        String value = properties.getProperty("forceFoodLevel");
        if (value != null) {
            forceFoodLevel = Boolean.parseBoolean(value.trim());
        }
        if (!exists || value == null) {
            save();
        }
        LogManager.getLogger().info("Orienteering forceFoodLevel=" + forceFoodLevel);
    }

    private OrienteeringConfig() {
    }

    public static boolean isForceFoodLevel() {
        return forceFoodLevel;
    }

    private static void save() {
        File parent = CONFIG_FILE.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs() && !parent.isDirectory()) {
            LogManager.getLogger().error("Could not create config directory: " + parent.getAbsolutePath());
            return;
        }
        Properties properties = new Properties();
        properties.setProperty("forceFoodLevel", Boolean.toString(forceFoodLevel));
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            properties.store(out, "OrienteeringMod: set forceFoodLevel=true to restore per-tick 15/20 food locking; false lets vanilla/VitalSpeed hunger work.");
        } catch (IOException e) {
            LogManager.getLogger().error("Failed to write Orienteering config " + CONFIG_FILE.getAbsolutePath(), e);
        }
    }
}