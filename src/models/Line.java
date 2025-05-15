package models;

import java.awt.*;

public class Line {
    private Point point1;
    private Point point2;
    private Color color;
    private LineStyle style;

    // initialize line with points, color and style
    public Line(Point point1, Point point2, Color color, LineStyle style) {
        this.point1 = point1;
        this.point2 = point2;
        this.color = color;
        this.style = style;
    }

    // get line color
    public Color getColor() {
        return color;
    }

    // get first point
    public Point getPoint1() {
        return point1;
    }

    // get second point
    public Point getPoint2() {
        return point2;
    }

    // get line style
    public LineStyle getStyle() {
        return style;
    }
}