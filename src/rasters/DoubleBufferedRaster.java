package rasters;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DoubleBufferedRaster implements Raster {
    private final Raster baseLayer;
    private final Raster previewLayer;

    public DoubleBufferedRaster(Raster base, Raster preview) {
        this.baseLayer = base;
        this.previewLayer = preview;
    }

    public void startPreview() {
        previewLayer.clear();
        previewLayer.copyFrom(baseLayer);
    }

    public void endPreview() {
        baseLayer.copyFrom(previewLayer);
    }

    @Override
    public void setPixel(int x, int y, int color) {
        previewLayer.setPixel(x, y, color);
    }

    @Override
    public int getPixel(int x, int y) {
        return previewLayer.getPixel(x, y);
    }

    @Override
    public void clear() {
        baseLayer.clear();
        previewLayer.clear();
    }

    @Override
    public void setClearColor(int color) {
        baseLayer.setClearColor(color);
        previewLayer.setClearColor(color);
    }

    @Override
    public int getWidth() {
        return baseLayer.getWidth();
    }

    @Override
    public int getHeight() {
        return baseLayer.getHeight();
    }

    @Override
    public void copyFrom(Raster source) {
        baseLayer.copyFrom(source);
        previewLayer.copyFrom(source);
    }

    @Override
    public Graphics getGraphics() {
        return previewLayer.getGraphics();
    }

    @Override
    public void repaint(Graphics graphics) {
        previewLayer.repaint(graphics);
    }

    @Override
    public BufferedImage getImg() {
        return previewLayer.getImg();
    }
}