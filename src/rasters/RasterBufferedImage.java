package rasters;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RasterBufferedImage implements Raster {

    private final BufferedImage img;
    private int color;

    public BufferedImage getImg() {
        return img;
    }

    public RasterBufferedImage(int width, int height) {
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void repaint(Graphics graphics) {
        graphics.drawImage(img, 0, 0, null);
    }

    public Graphics getGraphics(){
        return img.getGraphics();
    }

    @Override
    public int getPixel(int x, int y) {
        if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()) {
            return img.getRGB(x, y);
        }
        return 0;
    }

    @Override
    public void setPixel(int x, int y, int color) {
        if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()) {
            img.setRGB(x, y, color);
        } else {
            System.out.printf("Pixel out of bounds: (%d, %d)%n", x, y);
        }
    }

    @Override
    public void clear() {
        Graphics g = img.getGraphics();
        g.setColor(new Color(color));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
    }

    @Override
    public void copyFrom(Raster source) {
        BufferedImage sourceImg = ((RasterBufferedImage)source).getImg();
        Graphics g = img.getGraphics();
        g.drawImage(sourceImg, 0, 0, null);
        g.dispose();
    }

    @Override
    public void setClearColor(int color) {
        this.color = color;
    }

    @Override
    public int getWidth() {
        return img.getWidth();
    }

    @Override
    public int getHeight() {
        return img.getHeight();
    }

}
