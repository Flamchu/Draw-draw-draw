package rasters;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DoubleBufferedRaster implements Raster {
    private final Raster baseLayer;
    private final Raster previewLayer;

    // initialize with base and preview raster layers
    public DoubleBufferedRaster(Raster base, Raster preview) {
        this.baseLayer = base;
        this.previewLayer = preview;
    }

    // prepare preview by clearing and copying base layer
    public void startPreview() {
        previewLayer.clear();
        previewLayer.copyFrom(baseLayer);
    }

    // commit preview changes to base layer
    public void endPreview() {
        baseLayer.copyFrom(previewLayer);
    }

    // set pixel in preview layer
    @Override
    public void setPixel(int x, int y, int color) {
        previewLayer.setPixel(x, y, color);
    }

    // get pixel from preview layer
    @Override
    public int getPixel(int x, int y) {
        return previewLayer.getPixel(x, y);
    }

    // clear both raster layers
    @Override
    public void clear() {
        baseLayer.clear();
        previewLayer.clear();
    }

    // set clear color for both layers
    @Override
    public void setClearColor(int color) {
        baseLayer.setClearColor(color);
        previewLayer.setClearColor(color);
    }

    // get width from base layer
    @Override
    public int getWidth() {
        return baseLayer.getWidth();
    }

    // get height from base layer
    @Override
    public int getHeight() {
        return baseLayer.getHeight();
    }

    // copy source to both layers
    @Override
    public void copyFrom(Raster source) {
        baseLayer.copyFrom(source);
        previewLayer.copyFrom(source);
    }

    // get graphics from preview layer
    @Override
    public Graphics getGraphics() {
        return previewLayer.getGraphics();
    }

    // repaint preview layer
    @Override
    public void repaint(Graphics graphics) {
        previewLayer.repaint(graphics);
    }

    // get image from preview layer
    @Override
    public BufferedImage getImg() {
        return previewLayer.getImg();
    }
}