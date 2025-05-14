import models.*;
import models.Point;
import models.Polygon;
import rasterizers.FloodFiller;
import rasterizers.LineCanvasRasterizer;
import rasterizers.PolygonRasterizer;
import rasters.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Serial;
import java.util.ArrayList;

public class App {
    private final JPanel panel;
    private final Raster baseRaster;
    private final Raster previewRaster;
    private final DoubleBufferedRaster raster;
    private MouseAdapter mouseAdapter;
    private KeyAdapter keyAdapter;
    private Point point;
    private LineCanvasRasterizer rasterizer;
    private LineCanvas canvas;
    private Polygon polygon;
    private PolygonRasterizer polygonRasterizer;
    private Color backgroundColor;
    private SelectionTracker selectionTracker = new SelectionTracker();
    private boolean isSelecting = false;
    private boolean shiftMode = false;
    private boolean polygonMode = false;
    private boolean fillMode = false;
    private boolean rectangleMode = false;
    private boolean triangleMode = false;
    private boolean circleMode = false;
    private boolean brushMode = false;
    private boolean eraserMode = false;
    private Point shapeStartPoint;
    private Toolbar toolbar;
    private FloodFiller floodFiller;
    private Point lastBrushPoint;
    private ArrayList<Polygon> polygons = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App(800, 600).start());
    }

    public void clear(Color color) {
        raster.setClearColor(color.getRGB());
        raster.clear();
    }

    public void present(Graphics graphics) {
        raster.repaint(graphics);
        if (isSelecting) {
            graphics.setColor(Color.RED);
            graphics.drawString("Selection Tool Active", 10, 20);
        }
    }

    public void start() {
        backgroundColor = new Color(0xaaaaaa);
        clear(backgroundColor);
        panel.repaint();
    }

    public App(int width, int height) {
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setTitle("Delta draw");
        frame.setResizable(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        toolbar = new Toolbar(this::handleMenuAction);
        frame.setJMenuBar(toolbar.getMenuBar());

        toolbar.setSettingsChangeListener(e -> {
            rasterizer.setLineWidth(toolbar.getLineWidth());
        });

        baseRaster = new RasterBufferedImage(width, height);
        previewRaster = new RasterBufferedImage(width, height);
        raster = new DoubleBufferedRaster(baseRaster, previewRaster);

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

        rasterizer = new LineCanvasRasterizer(raster);
        polygonRasterizer = new PolygonRasterizer(raster);
        canvas = new LineCanvas(new ArrayList<>(), new ArrayList<>());
        polygon = new Polygon();
        floodFiller = new FloodFiller(raster);

        createAdapters();
        panel.addMouseMotionListener(mouseAdapter);
        panel.addMouseListener(mouseAdapter);
        panel.addKeyListener(keyAdapter);

        panel.requestFocus();
        panel.requestFocusInWindow();
    }

    private void handleMenuAction(ActionEvent e) {

        switch (e.getActionCommand()) {
            case "NEW":
                raster.clear();
                canvas = new LineCanvas(new ArrayList<>(), new ArrayList<>());
                polygon = new Polygon();
                panel.repaint();
                break;

            case "CLEAR":
                raster.clear();
                panel.repaint();
                break;

            case "EXIT":
                System.exit(0);
                break;

            case "TOOL_LINE":
                resetModes();
                toolbar.setActiveTool("LINE");
                break;

            case "TOOL_POLYGON":
                resetModes();
                polygonMode = true;
                polygon = new Polygon();
                toolbar.setActiveTool("POLYGON");
                break;

            case "TOOL_FILL":
                resetModes();
                fillMode = true;
                toolbar.setActiveTool("FILL");
                break;

            case "TOOL_ERASER":
                resetModes();
                eraserMode = true;
                toolbar.setActiveTool("ERASER");
                break;

            case "UNDO":
                if (!canvas.getLines().isEmpty()) {
                    canvas.getLines().remove(canvas.getLines().size() - 1);
                    raster.clear();
                    rasterizer.rasterizeCanvas(canvas);
                    panel.repaint();
                }
                break;

            case "OPEN":
                JOptionPane.showMessageDialog(panel, "No, thanks.");
                break;

            case "SAVE":
                JOptionPane.showMessageDialog(panel, "Deleting System32...");
                break;

            case "TOOL_RECTANGLE":
                resetModes();
                rectangleMode = true;
                toolbar.setActiveTool("RECTANGLE");
                break;

            case "TOOL_TRIANGLE":
                resetModes();
                triangleMode = true;
                toolbar.setActiveTool("TRIANGLE");
                break;

            case "TOOL_CIRCLE":
                resetModes();
                circleMode = true;
                toolbar.setActiveTool("CIRCLE");
                break;

            case "TOOL_SELECT":
                resetModes();
                isSelecting = true;
                toolbar.setActiveTool("SELECT");
                break;

            case "TOOL_BRUSH":
                resetModes();
                brushMode = true;
                toolbar.setActiveTool("BRUSH");
                break;

            default:
                if (e.getActionCommand().startsWith("COLOR_")) {
                    System.out.println("Selected color: " + toolbar.getSelectedColor());
                }
                break;
        }
    }

    private void resetModes() {
        polygonMode = false;
        fillMode = false;
        rectangleMode = false;
        triangleMode = false;
        circleMode = false;
        brushMode = false;
        eraserMode = false;
        isSelecting = false;
        selectionTracker.clearSelection();
        lastBrushPoint = null;
    }

    private void createAdapters() {
        mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isSelecting) {
                    // Clear previous selection if not holding shift
                    if (!shiftMode) {
                        selectionTracker.clearSelection();
                    }

                    boolean found = trySelectShape(e.getX(), e.getY());
                    if (found) {
                        raster.startPreview();
                        redrawBaseToPreview();
                        selectionTracker.startDrag(e.getX(), e.getY());
                    }
                }
                else if (fillMode) {
                    floodFiller.floodFill(e.getX(), e.getY(), toolbar.getSelectedColor());
                    panel.repaint();
                } else if (polygonMode) {
                    point = new Point(e.getX(), e.getY());
                    polygon.addPoint(point);
                    if (polygon.getPoints().size() >= 3) {
                        raster.startPreview();
                        previewRaster.copyFrom(baseRaster);
                        polygonRasterizer.rasterize(polygon, toolbar.getSelectedColor(), toolbar.getLineStyle());
                        panel.repaint();
                    }
                } else if (brushMode || eraserMode) {
                    lastBrushPoint = new Point(e.getX(), e.getY());
                    raster.startPreview();
                    previewRaster.copyFrom(baseRaster);
                    Color color = eraserMode ? backgroundColor : toolbar.getSelectedColor();
                    raster.setPixel(e.getX(), e.getY(), color.getRGB());
                    panel.repaint();
                } else {
                    point = new Point(e.getX(), e.getY());
                    shapeStartPoint = point;
                    raster.startPreview();
                    previewRaster.copyFrom(baseRaster);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectionTracker.hasSelection()) {
                    commitSelectionChanges();
                }
                else if (brushMode || eraserMode) {
                    raster.endPreview();
                    panel.repaint();
                }  else if (rectangleMode || triangleMode || circleMode) {
                    Point endPoint = alignPoint(new Point(e.getX(), e.getY()));
                    Polygon shape = null;

                    if (rectangleMode) {
                        shape = createRectangle(shapeStartPoint, endPoint);
                    } else if (triangleMode) {
                        shape = createTriangle(shapeStartPoint, endPoint);
                    } else if (circleMode) {
                        shape = createCircle(shapeStartPoint, endPoint);
                    }

                    if (shape != null) {
                        polygons.add(shape); // Store the shape
                        polygonRasterizer.rasterize(shape, toolbar.getSelectedColor(), toolbar.getLineStyle());
                    }

                    raster.endPreview();
                    panel.repaint();
                } else if (!polygonMode) {
                    Point point2 = alignPoint(new Point(e.getX(), e.getY()));
                    Line line = new Line(point, point2, toolbar.getSelectedColor(), toolbar.getLineStyle());
                    canvas.add(line);

                    raster.endPreview();
                    rasterizer.rasterizeCanvas(canvas);
                    panel.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectionTracker.hasSelection()) {
                    selectionTracker.updateDrag(e.getX(), e.getY());
                    raster.clear();
                    redrawEverything();
                }
                else if (brushMode || eraserMode) {
                    Point currentPoint = new Point(e.getX(), e.getY());
                    if (lastBrushPoint != null) {
                        Color color = eraserMode ? backgroundColor : toolbar.getSelectedColor();
                        Line line = new Line(lastBrushPoint, currentPoint, color, LineStyle.SOLID);
                        rasterizer.setLineWidth(toolbar.getLineWidth());
                        rasterizer.rasterizeLine(line);
                        panel.repaint();
                    }
                    lastBrushPoint = currentPoint;
                } else if (rectangleMode || triangleMode || circleMode) {
                    Point endPoint = alignPoint(new Point(e.getX(), e.getY()));
                    raster.startPreview();
                    previewRaster.copyFrom(baseRaster);

                    if (rectangleMode) {
                        Polygon rectangle = createRectangle(shapeStartPoint, endPoint);
                        polygonRasterizer.rasterize(rectangle, toolbar.getSelectedColor(), toolbar.getLineStyle());
                    } else if (triangleMode) {
                        Polygon triangle = createTriangle(shapeStartPoint, endPoint);
                        polygonRasterizer.rasterize(triangle, toolbar.getSelectedColor(), toolbar.getLineStyle());
                    } else if (circleMode) {
                        Polygon circle = createCircle(shapeStartPoint, endPoint);
                        polygonRasterizer.rasterize(circle, toolbar.getSelectedColor(), toolbar.getLineStyle());
                    }
                    panel.repaint();
                } else if (!polygonMode) {
                    Point point2 = alignPoint(new Point(e.getX(), e.getY()));
                    if (point != null && point2 != null) {
                        Line line = new Line(point, point2, toolbar.getSelectedColor(), toolbar.getLineStyle());

                        raster.startPreview();
                        previewRaster.copyFrom(baseRaster);
                        rasterizer.rasterizeCanvas(canvas);

                        rasterizer.setLineWidth(toolbar.getLineWidth());
                        rasterizer.rasterizeLine(line);

                        panel.repaint();
                    }
                }
            }
        };

        keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftMode = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftMode = false;
                }
            }
        };
    }

    private Polygon createRectangle(Point start, Point end) {
        Polygon rectangle = new Polygon();
        rectangle.addPoint(new Point(start.getX(), start.getY()));
        rectangle.addPoint(new Point(end.getX(), start.getY()));
        rectangle.addPoint(new Point(end.getX(), end.getY()));
        rectangle.addPoint(new Point(start.getX(), end.getY()));
        return rectangle;
    }

    private Polygon createTriangle(Point start, Point end) {
        Polygon triangle = new Polygon();
        int width = end.getX() - start.getX();
        triangle.addPoint(new Point(start.getX() + width/2, start.getY()));
        triangle.addPoint(new Point(end.getX(), end.getY()));
        triangle.addPoint(new Point(start.getX(), end.getY()));
        return triangle;
    }

    private Polygon createCircle(Point center, Point edge) {
        Polygon circle = new Polygon();
        int radius = (int) Math.sqrt(Math.pow(edge.getX() - center.getX(), 2) +
                Math.pow(edge.getY() - center.getY(), 2));
        int points = 36;

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            int x = (int) (center.getX() + radius * Math.cos(angle));
            int y = (int) (center.getY() + radius * Math.sin(angle));
            circle.addPoint(new Point(x, y));
        }
        return circle;
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

    private boolean trySelectShape(int x, int y) {
        // First check polygons (newest first)
        for (int i = polygons.size() - 1; i >= 0; i--) {
            Polygon polygon = polygons.get(i);
            if (polygonRasterizer.isPointInsidePolygon(polygon, new Point(x, y))) {
                selectionTracker.selectPolygon(polygon);
                return true;
            }
        }

        // Then check lines (newest first)
        for (int i = canvas.getLines().size() - 1; i >= 0; i--) {
            Line line = canvas.getLines().get(i);
            if (isPointNearLine(x, y, line)) {
                selectionTracker.selectLine(line);
                return true;
            }
        }

        return false;
    }

    private boolean isPointNearLine(int x, int y, Line line) {
        Point p1 = line.getPoint1();
        Point p2 = line.getPoint2();

        // Simple distance-to-line check
        double lineLength = p1.distanceTo(p2);
        if (lineLength == 0) return p1.distanceTo(new Point(x, y)) < 5;

        double t = ((x - p1.getX()) * (p2.getX() - p1.getX()) +
                (y - p1.getY()) * (p2.getY() - p1.getY())) / (lineLength * lineLength);

        t = Math.max(0, Math.min(1, t));

        double projX = p1.getX() + t * (p2.getX() - p1.getX());
        double projY = p1.getY() + t * (p2.getY() - p1.getY());

        return new Point(x, y).distanceTo(new Point((int)projX, (int)projY)) < 5;
    }

    private void redrawEverything() {
        if (isSelecting && selectionTracker.hasSelection()) {
            // Selection mode - use preview layer
            raster.startPreview();
            redrawBaseToPreview();

            // Draw modified versions
            for (Line line : selectionTracker.getSelectedLines()) {
                rasterizer.rasterizeLine(line);
            }
            for (Polygon polygon : selectionTracker.getSelectedPolygons()) {
                polygonRasterizer.rasterize(polygon, toolbar.getSelectedColor(), toolbar.getLineStyle());
            }

        } else {
            // Normal mode - draw to base layer
            commitAllChanges();
        }
        panel.repaint();
    }

    private void redrawBaseToPreview() {
        previewRaster.clear();
        previewRaster.copyFrom(baseRaster);
    }

    private void commitAllChanges() {
        // 1. Clear both layers
        baseRaster.clear();
        previewRaster.clear();

        // 2. Redraw all permanent elements to base layer
        rasterizer.rasterizeCanvas(canvas);
        for (Polygon polygon : polygons) {
            polygonRasterizer.rasterize(polygon, toolbar.getSelectedColor(), toolbar.getLineStyle());
        }

        // 3. Synchronize layers
        previewRaster.copyFrom(baseRaster);
        selectionTracker.resetChangeFlag();

        panel.repaint();
    }

    private void commitSelectionChanges() {
        raster.endPreview();  // Copies preview to base
        redrawEverything();   // Ensures consistent state
    }

    private void handlePolygonCreation() {
        if (polygon.getPoints().size() >= 3) {
            polygons.add(polygon);
            polygon = new Polygon(); // Reset for next polygon
        }
    }
}