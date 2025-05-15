package rasterizers;

import models.Line;
import models.LineCanvas;
import rasters.Raster;

import java.util.ArrayList;

public class LineCanvasRasterizer {
    private final Raster raster;
    private final LineRasterizerTrivial lineRasterizer;

    // initialize with raster and create line rasterizer
    public LineCanvasRasterizer(Raster raster) {
        this.raster = raster;
        this.lineRasterizer = new LineRasterizerTrivial(raster);
    }

    // set width for all drawn lines
    public void setLineWidth(int width) {
        lineRasterizer.setLineWidth(width);
    }

    // draw all lines from canvas to raster
    public void rasterizeCanvas(LineCanvas canvas) {
        for (Line line : canvas.getLines()) {
            if (line.getPoint1() != null && line.getPoint2() != null) {
                lineRasterizer.rasterize(line);
            }
        }
    }

    // draw single line to raster
    public void rasterizeLine(Line line) {
        if (line != null && line.getPoint1() != null && line.getPoint2() != null) {
            lineRasterizer.rasterize(line);
        }
    }
}