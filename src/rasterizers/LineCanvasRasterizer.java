package rasterizers;

import models.Line;
import models.LineCanvas;
import rasters.Raster;

import java.util.ArrayList;

public class LineCanvasRasterizer {
    private final Raster raster;
    private final LineRasterizerTrivial lineRasterizer;

    public LineCanvasRasterizer(Raster raster) {
        this.raster = raster;
        this.lineRasterizer = new LineRasterizerTrivial(raster);
    }

    public void setLineWidth(int width) {
        lineRasterizer.setLineWidth(width);
    }

    public void rasterizeCanvas(LineCanvas canvas) {
        // Draw all lines
        for (Line line : canvas.getLines()) {
            if (line.getPoint1() != null && line.getPoint2() != null) {
                lineRasterizer.rasterize(line);
            }
        }
    }

    public void rasterizeLine(Line line) {
        if (line != null && line.getPoint1() != null && line.getPoint2() != null) {
            lineRasterizer.rasterize(line);
        }
    }
}