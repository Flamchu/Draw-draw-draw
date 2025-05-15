package rasterizers;

import models.LineStyle;
import models.Point;
import models.Line;
import models.Polygon;
import rasters.Raster;

import java.awt.*;
import java.util.List;

public class PolygonRasterizer {
    private final LineRasterizerTrivial lineRasterizer;
    private final LineCanvasRasterizer lineCanvasRasterizer;

    // Initialize with both rasterizers
    public PolygonRasterizer(Raster raster, LineCanvasRasterizer lineCanvasRasterizer) {
        this.lineRasterizer = new LineRasterizerTrivial(raster);
        this.lineCanvasRasterizer = lineCanvasRasterizer;
    }

    // Draw polygon outline using LineCanvasRasterizer with given color, style, and width
    public void rasterize(Polygon polygon, Color color, LineStyle style, int lineWidth) {
        if (lineCanvasRasterizer == null) {
            throw new IllegalStateException("LineCanvasRasterizer is not initialized.");
        }

        List<Point> points = polygon.getPoints();
        if (points.size() < 2) return;

        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % points.size());
            Line line = new Line(a, b, color, style);
            lineCanvasRasterizer.setLineWidth(lineWidth);
            lineCanvasRasterizer.rasterizeLine(line);
        }
    }

    // Point-in-polygon test using ray casting algorithm
    public boolean isPointInsidePolygon(Polygon polygon, Point point) {
        List<Point> points = polygon.getPoints();
        int size = points.size();
        int x = point.getX();
        int y = point.getY();
        boolean inside = false;

        for (int i = 0, j = size - 1; i < size; j = i++) {
            int xi = points.get(i).getX();
            int yi = points.get(i).getY();
            int xj = points.get(j).getX();
            int yj = points.get(j).getY();

            // Point lies exactly on a vertex
            if ((xi == x && yi == y) || (xj == x && yj == y)) {
                return true;
            }

            // Ray-casting logic
            boolean intersect = ((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (double)(yj - yi) + xi);
            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }
}
