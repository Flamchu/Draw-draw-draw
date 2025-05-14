package models;

import rasters.Raster;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Line implements Selectable {
    private Point point1;
    private Point point2;
    private Color color;
    private LineStyle style;

    public Line(Point point1, Point point2, Color color, LineStyle style) {
        this.point1 = point1;
        this.point2 = point2;
        this.color = color;
        this.style = style;
    }

    @Override
    public boolean contains(int x, int y) {
        return pointToLineDistance(x, y) < 5; // 5 pixel tolerance
    }

    @Override
    public void translate(int dx, int dy) {
        point1 = new Point(point1.getX() + dx, point1.getY() + dy);
        point2 = new Point(point2.getX() + dx, point2.getY() + dy);
    }

    @Override
    public java.util.List<Point> getResizePoints() {
        List<Point> points = new ArrayList<>();
        points.add(point1);
        points.add(point2);
        return points;
    }

    @Override
    public void drawSelection(Raster raster) {
        // Draw selection handles at endpoints
        drawHandle(raster, point1.getX(), point1.getY());
        drawHandle(raster, point2.getX(), point2.getY());
    }

    private void drawHandle(Raster raster, int x, int y) {
        int handleSize = 8;
        for (int i = -handleSize/2; i <= handleSize/2; i++) {
            for (int j = -handleSize/2; j <= handleSize/2; j++) {
                if (Math.abs(i) + Math.abs(j) <= handleSize/2) {
                    raster.setPixel(x + i, y + j, Color.RED.getRGB());
                }
            }
        }
    }

    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color color) {
        
    }

    public Point getPoint1() {
        return point1;
    }

    public Point getPoint2() {
        return point2;
    }

    public LineStyle getStyle() {
        return style;
    }
}