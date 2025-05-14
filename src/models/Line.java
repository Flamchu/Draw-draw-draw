package models;

import java.awt.*;

public class Line {
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

    public Color getColor() {
        return color;
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