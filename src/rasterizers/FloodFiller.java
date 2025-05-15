package rasterizers;

import rasters.Raster;
import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;
import models.Point;

public class FloodFiller {
    private final Raster raster;

    // initialize with raster to modify
    public FloodFiller(Raster raster) {
        this.raster = raster;
    }

    // fill connected area starting from (x,y) with new color
    public void floodFill(int x, int y, Color newColor) {
        int targetColor = raster.getPixel(x, y);
        int replacementColor = newColor.getRGB();

        // skip if already the target color
        if (targetColor == replacementColor) {
            return;
        }

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x, y));

        // process all connected pixels
        while (!queue.isEmpty()) {
            Point p = queue.remove();
            int px = p.getX();
            int py = p.getY();

            // skip if out of bounds
            if (px < 0 || py < 0 || px >= raster.getWidth() || py >= raster.getHeight()) {
                continue;
            }

            // fill and add neighbors if matching target color
            if (raster.getPixel(px, py) == targetColor) {
                raster.setPixel(px, py, replacementColor);

                queue.add(new Point(px + 1, py));
                queue.add(new Point(px - 1, py));
                queue.add(new Point(px, py + 1));
                queue.add(new Point(px, py - 1));
            }
        }
    }
}