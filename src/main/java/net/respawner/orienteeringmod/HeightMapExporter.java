package net.respawner.orienteeringmod;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Exports the same integer ground elevations used for the contour overlay. */
final class HeightMapExporter {
    static final int NO_DATA_COLOR = 0xFFD9D9D9;
    private static final int LEGEND_WIDTH = 228;
    private static final int LEGEND_TOP = 124;
    private static final int ROW_HEIGHT = 16;
    private static final int[] PALETTE = {
        0x215C35, 0x61A94A, 0xD6D964, 0xF49433, 0xA86A46, 0xF4EEE5
    };

    static BufferedImage render(int[][] heights, int originX, int originZ) {
        int rows = heights.length;
        int columns = heights[0].length;
        int minimum = Integer.MAX_VALUE;
        int maximum = Integer.MIN_VALUE;
        for (int[] row : heights) {
            for (int height : row) {
                if (height >= 0) {
                    minimum = Math.min(minimum, height);
                    maximum = Math.max(maximum, height);
                }
            }
        }
        boolean hasData = minimum != Integer.MAX_VALUE;
        if (!hasData) {
            minimum = 0;
            maximum = 0;
        }
        int levels = maximum - minimum + 1;
        int canvasHeight = Math.max(rows, LEGEND_TOP + levels * ROW_HEIGHT + 52);
        BufferedImage image = new BufferedImage(columns + LEGEND_WIDTH, canvasHeight,
                                               BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            for (int z = 0; z < rows; z++) {
                for (int x = 0; x < columns; x++) {
                    image.setRGB(x, z, color(heights[z][x], minimum, maximum));
                }
            }
            graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            graphics.setColor(Color.DARK_GRAY);
            int left = columns + 16;
            graphics.drawString("GROUND HEIGHT / Y", left, 20);
            graphics.drawString("Color interval: 1 block", left, 40);
            graphics.drawString("Map: 1 pixel = 1 block", left, 58);
            graphics.drawString("X: " + originX + " to " + (originX + columns - 1), left, 76);
            graphics.drawString("Z: " + originZ + " to " + (originZ + rows - 1), left, 94);
            graphics.drawString("X right, Z down", left, 112);
            if (hasData) {
                for (int height = maximum; height >= minimum; height--) {
                    int top = LEGEND_TOP + (maximum - height) * ROW_HEIGHT;
                    graphics.setColor(new Color(color(height, minimum, maximum)));
                    graphics.fillRect(left, top, 42, ROW_HEIGHT);
                    graphics.setColor(Color.DARK_GRAY);
                    graphics.drawString(Integer.toString(height), left + 52, top + 12);
                }
            } else {
                graphics.drawString("No valid ground samples", left, LEGEND_TOP + 12);
            }
            int noDataTop = LEGEND_TOP + levels * ROW_HEIGHT + 16;
            graphics.setColor(new Color(NO_DATA_COLOR));
            graphics.fillRect(left, noDataTop, 42, ROW_HEIGHT);
            graphics.setColor(Color.DARK_GRAY);
            graphics.drawString("No data", left + 52, noDataTop + 12);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    static int color(int height, int minimum, int maximum) {
        if (height < 0) {
            return NO_DATA_COLOR;
        }
        if (minimum == maximum) {
            return 0xFF000000 | PALETTE[0];
        }
        double position = (height - minimum) * (PALETTE.length - 1.0) / (maximum - minimum);
        int segment = Math.min((int) position, PALETTE.length - 2);
        double fraction = position - segment;
        int first = PALETTE[segment];
        int second = PALETTE[segment + 1];
        int red = interpolate(first >> 16 & 255, second >> 16 & 255, fraction);
        int green = interpolate(first >> 8 & 255, second >> 8 & 255, fraction);
        int blue = interpolate(first & 255, second & 255, fraction);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private static int interpolate(int first, int second, double fraction) {
        return (int) Math.round(first + (second - first) * fraction);
    }

    static void writeCsv(Path target, int[][] heights, int originX, int originZ) throws IOException {
        Path temp = Files.createTempFile(target.toAbsolutePath().getParent(), "ground-height-", ".csv");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                writer.write("Z/X");
                for (int x = 0; x < heights[0].length; x++) {
                    writer.write(",");
                    writer.write(Integer.toString(originX + x));
                }
                writer.newLine();
                for (int z = 0; z < heights.length; z++) {
                    writer.write(Integer.toString(originZ + z));
                    for (int height : heights[z]) {
                        writer.write(",");
                        if (height >= 0) {
                            writer.write(Integer.toString(height));
                        }
                    }
                    writer.newLine();
                }
            }
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
