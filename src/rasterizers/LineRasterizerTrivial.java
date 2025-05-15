package rasterizers;

import models.Line;
import models.LineStyle;
import models.Point;
import rasters.Raster;

import java.awt.*;
import java.util.ArrayList;

public class LineRasterizerTrivial implements Rasterizer {
    private final Raster raster;
    private int lineWidth;

    // initialize with raster and default line width
    public LineRasterizerTrivial(Raster raster) {
        this.raster = raster;
        this.lineWidth = 1;
    }

    // set minimum line width to 1 pixel
    public void setLineWidth(int width) {
        this.lineWidth = Math.max(1, width);
    }

    // draw line with given style and width
    @Override
    public void rasterize(Line line) {
        if (line == null || line.getPoint1() == null || line.getPoint2() == null) {
            return;
        }

        int x1 = line.getPoint1().getX();
        int y1 = line.getPoint1().getY();
        int x2 = line.getPoint2().getX();
        int y2 = line.getPoint2().getY();

        if (lineWidth == 1) {
            drawThinLine(x1, y1, x2, y2, line.getColor(), line.getStyle());
        } else {
            drawThickLine(x1, y1, x2, y2, line.getColor(), lineWidth, line.getStyle());
        }
    }

    // draw single pixel width line with style pattern
    private void drawThinLine(int x1, int y1, int x2, int y2, Color color, LineStyle style) {
        int width = raster.getWidth();
        int height = raster.getHeight();

        if (x1 == x2) { // vertical line case
            if (y1 > y2) {
                int temp = y1;
                y1 = y2;
                y2 = temp;
            }

            for (int y = y1; y <= y2; y++) {
                if (shouldDraw(y - y1, style) && isValid(x1, y)) {
                    raster.setPixel(x1, y, color.getRGB());
                }
            }
            return;
        }

        float k = (float) (y2 - y1) / (x2 - x1);
        float q = y1 - (k * x1);

        if (Math.abs(k) < 1) { // shallow slope (x-major)
            if (x1 > x2) {
                int temp = x1;
                x1 = x2;
                x2 = temp;
            }

            for (int x = x1; x <= x2; x++) {
                int y = Math.round(k * x + q);
                if (shouldDraw(x - x1, style) && isValid(x, y)) {
                    raster.setPixel(x, y, color.getRGB());
                }
            }
        } else { // steep slope (y-major)
            if (y1 > y2) {
                int temp = y1;
                y1 = y2;
                y2 = temp;
            }

            for (int y = y1; y <= y2; y++) {
                int x = Math.round((y - q) / k);
                if (shouldDraw(y - y1, style) && isValid(x, y)) {
                    raster.setPixel(x, y, color.getRGB());
                }
            }
        }
    }

    // determine if pixel should be drawn based on line style
    private boolean shouldDraw(int position, LineStyle style) {
        if (style == null) return true;

        switch (style) {
            case DOTTED:
                return position % 4 < 2;  // 2 on, 2 off
            case DASHED:
                return position % 12 < 8;  // 8 on, 4 off
            default:  // SOLID
                return true;
        }
    }

    // rasterize polygon edges
    public void rasterizePolygonEdge(Point p1, Point p2, Color color, LineStyle style) {
        if (p1 == null || p2 == null) return;

        int x1 = p1.getX();
        int y1 = p1.getY();
        int x2 = p2.getX();
        int y2 = p2.getY();

        if (lineWidth == 1) {
            drawThinLine(x1, y1, x2, y2, color, style);
        } else {
            drawThickLine(x1, y1, x2, y2, color, lineWidth, style);
        }
    }

    // check if coordinates are within raster bounds
    private boolean isValid(int x, int y) {
        return x >= 0 && x < raster.getWidth() && y >= 0 && y < raster.getHeight();
    }

    // draw thick line using multiple thin lines
    private void drawThickLine(int x1, int y1, int x2, int y2, Color color, int thickness, LineStyle style) {
        int halfThickness = thickness / 2;
        if (Math.abs(x2 - x1) > Math.abs(y2 - y1)) {
            for (int i = -halfThickness; i <= halfThickness; i++) {
                drawThinLine(x1, y1 + i, x2, y2 + i, color, style);
            }
        } else {
            for (int i = -halfThickness; i <= halfThickness; i++) {
                drawThinLine(x1 + i, y1, x2 + i, y2, color, style);
            }
        }
        drawCircle(x1, y1, halfThickness, color);
        drawCircle(x2, y2, halfThickness, color);
    }

    // draw filled circle for line endpoints
    private void drawCircle(int centerX, int centerY, int radius, Color color) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x*x + y*y <= radius*radius) {
                    int px = centerX + x;
                    int py = centerY + y;
                    if (isValid(px, py)) {
                        raster.setPixel(px, py, color.getRGB());
                    }
                }
            }
        }
    }

    // draw multiple lines from array
    @Override
    public void rasterizeArray(ArrayList<Line> lines) {
        for (Line line : lines) {
            rasterize(line);
        }
    }
}