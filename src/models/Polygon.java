package models;

import java.util.ArrayList;
import java.util.List;

public class Polygon {
    private List<Point> points;

    public Polygon() {
        this.points = new ArrayList<>();
    }

    public void addPoint(Point point) {
        points.add(point);
        System.out.printf("Added point: (%d, %d). Total points: %d%n", point.getX(), point.getY(), points.size());
    }

    public List<Point> getPoints() {
        System.out.printf("Retrieving points. Total points: %d%n", points.size());
        return points;
    }
}