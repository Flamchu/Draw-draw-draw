package models;

public class Point {
    private int x;
    private int y;

    // create point with x and y coordinates
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // get x coordinate
    public int getX() { return x; }

    // get y coordinate
    public int getY() { return y; }

    // set x coordinate
    public void setX(int x) { this.x = x; }

    // set y coordinate
    public void setY(int y) { this.y = y; }

    // move point by dx and dy
    public void translate(int dx, int dy) {
        x += dx;
        y += dy;
    }

    // calculate distance to another point
    public double distanceTo(Point other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }
}