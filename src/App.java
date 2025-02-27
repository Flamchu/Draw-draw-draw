import models.Line;
import models.LineCanvas;
import models.Point;
import rasterizers.LineCanvasRasterizer;
import rasterizers.LineRasterizerTrivial;
import rasterizers.Rasterizer;
import rasters.Raster;
import rasters.RasterBufferedImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.util.ArrayList;

public class App {

    private final JPanel panel;
    private final Raster raster;
    private MouseAdapter mouseAdapter;
    private KeyAdapter keyAdapter;
    private Point point;
    private LineCanvasRasterizer rasterizer;
    private LineCanvas canvas;
    private boolean controlMode = false;
    private boolean shiftMode = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App(800, 600).start());
    }

    public void clear(int color) {
        raster.setClearColor(color);
        raster.clear();
    }

    public void present(Graphics graphics) {
        raster.repaint(graphics);
    }

    public void start() {
        clear(0xaaaaaa);
        panel.repaint();
    }

    public App(int width, int height) {
        JFrame frame = new JFrame();

        frame.setLayout(new BorderLayout());

        frame.setTitle("Los grafikos - DeltaStyle : " + this.getClass().getName());
        frame.setResizable(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        raster = new RasterBufferedImage(width, height);

        panel = new JPanel() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                present(g);
            }
        };
        panel.setPreferredSize(new Dimension(width, height));

        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        rasterizer = new LineCanvasRasterizer(
                raster
        );
        canvas = new LineCanvas(new ArrayList<>(), new ArrayList<>());

        createAdapters();
        panel.addMouseMotionListener(mouseAdapter);
        panel.addMouseListener(mouseAdapter);
        panel.addKeyListener(keyAdapter);

        panel.requestFocus();
        panel.requestFocusInWindow();
    }

    private void createAdapters() {
        mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                point = new Point(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Point point2 = alignPoint(new Point(e.getX(), e.getY()));
                Line line = new Line(point, point2, Color.red);

                raster.clear();

                if (controlMode) {
                    canvas.addDottedLine(line);
                } else {
                    canvas.add(line);
                }

                rasterizer.rasterizeCanvas(canvas);

                panel.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point point2 = alignPoint(new Point(e.getX(), e.getY()));
                Line line = new Line(point, point2, Color.red);

                raster.clear();

                rasterizer.rasterizeCanvas(canvas);

                if (controlMode) {
                    rasterizer.rasterizeDottedLine(line);
                } else {
                    rasterizer.rasterizeLine(line);
                }

                panel.repaint();
            }
        };

        keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    controlMode = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftMode = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    controlMode = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftMode = false;
                }
            }
        };
    }

    private Point alignPoint(Point target) {
        if (!shiftMode) return target;

        int dx = target.getX() - point.getX();
        int dy = target.getY() - point.getY();

        if (Math.abs(dx) > Math.abs(dy)) {
            return new Point(target.getX(), point.getY());
        } else if (Math.abs(dy) > Math.abs(dx)) {
            return new Point(point.getX(), target.getY());
        } else {
            int d = Math.min(Math.abs(dx), Math.abs(dy));
            return new Point(point.getX() + (dx < 0 ? -d : d), point.getY() + (dy < 0 ? -d : d));
        }
    }
}
