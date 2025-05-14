package models;

import java.awt.Color;
import java.util.List;
import models.Point;
import rasters.Raster;

public interface Selectable {
    boolean contains(int x, int y);
    void translate(int dx, int dy);
    void drawSelection(Raster raster);
    List<Point> getResizePoints();
    Color getColor();
    void setColor(Color color);
}