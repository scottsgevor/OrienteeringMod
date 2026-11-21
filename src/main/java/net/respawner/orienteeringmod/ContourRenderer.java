package net.respawner.orienteeringmod;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

/** Draws 5-block elevation contours using piecewise-linear triangles. */
final class ContourRenderer {
    static final int COLOR = 0xFF965F32;
    static final int INTERVAL = 5;

    static void overlay(BufferedImage image, int[][] heights) {
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(COLOR, true));
            graphics.setStroke(new BasicStroke(1.0f));
            double[] px = new double[3];
            double[] pz = new double[3];
            int[] h = new int[3];
            double[] intersections = new double[4];
            for (int z = 0; z < image.getHeight() - 1; z++) {
                for (int x = 0; x < image.getWidth() - 1; x++) {
                    int a = heights[z][x];
                    int b = heights[z][x + 1];
                    int c = heights[z + 1][x + 1];
                    int d = heights[z + 1][x];
                    if (Math.min(Math.min(a, b), Math.min(c, d)) < 0) {
                        continue;
                    }
                    px[0] = x;
                    pz[0] = z;
                    h[0] = a;
                    px[1] = x + 1;
                    pz[1] = z;
                    h[1] = b;
                    px[2] = x + 1;
                    pz[2] = z + 1;
                    h[2] = c;
                    drawTriangle(graphics, px, pz, h, intersections);
                    px[1] = x + 1;
                    pz[1] = z + 1;
                    h[1] = c;
                    px[2] = x;
                    pz[2] = z + 1;
                    h[2] = d;
                    drawTriangle(graphics, px, pz, h, intersections);
                }
            }
        } finally {
            graphics.dispose();
        }
    }

    private static void drawTriangle(Graphics2D graphics, double[] x, double[] z,
                                     int[] h, double[] points) {
        int min = Math.min(h[0], Math.min(h[1], h[2]));
        int max = Math.max(h[0], Math.max(h[1], h[2]));
        for (int level = Math.floorDiv(min, INTERVAL) * INTERVAL + INTERVAL;
             level <= max; level += INTERVAL) {
            int count = 0;
            for (int edge = 0; edge < 3; edge++) {
                int next = (edge + 1) % 3;
                if ((h[edge] < level) == (h[next] < level)) {
                    continue;
                }
                double fraction = (level - h[edge]) / (double) (h[next] - h[edge]);
                points[count++] = x[edge] + fraction * (x[next] - x[edge]);
                points[count++] = z[edge] + fraction * (z[next] - z[edge]);
            }
            if (count == 4 && (points[0] != points[2] || points[1] != points[3])) {
                graphics.draw(new Line2D.Double(points[0], points[1], points[2], points[3]));
            }
        }
    }
}
