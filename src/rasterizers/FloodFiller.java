package rasterizers;

import rasters.Raster;
import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;
import models.Point;

public class FloodFiller {
    private final Raster raster;

    public FloodFiller(Raster raster) {
        this.raster = raster;
    }

    public void floodFill(int x, int y, Color newColor) {
        int targetColor = raster.getPixel(x, y);
        int replacementColor = newColor.getRGB();

        if (targetColor == replacementColor) {
            return;
        }

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x, y));

        while (!queue.isEmpty()) {
            Point p = queue.remove();
            int px = p.getX();
            int py = p.getY();

            if (px < 0 || py < 0 || px >= raster.getWidth() || py >= raster.getHeight()) {
                continue;
            }

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