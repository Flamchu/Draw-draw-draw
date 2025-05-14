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

    public PolygonRasterizer(Raster raster) {
        this.lineRasterizer = new LineRasterizerTrivial(raster);
    }

    public void rasterize(Polygon polygon, Color color, LineStyle lineStyle) {
        List<Point> points = polygon.getPoints();
        int size = points.size();

        if (size < 2) return;

        for (int i = 0; i < size; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % size);
            Line line = new Line(p1, p2, color, lineStyle);
            lineRasterizer.rasterize(line);
        }
    }

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

            // Check if the point is on an edge
            if ((xi == x && yi == y) || (xj == x && yj == y)) {
                return true;
            }

            // Check if the edge intersects the horizontal ray
            boolean intersect = ((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }
}