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
    // constants for window dimensions
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final int V_KEY = KeyEvent.VK_V;
    private static final int B_KEY = KeyEvent.VK_B;


    // main ui components
    private final JPanel panel;
    private final JFrame frame;
    private final Toolbar toolbar;

    // raster components for double buffering
    private final Raster baseRaster;
    private final Raster previewRaster;
    private final DoubleBufferedRaster raster;

    // drawing tools and state
    private LineCanvasRasterizer rasterizer = null;
    private final PolygonRasterizer polygonRasterizer;
    private final FloodFiller floodFiller;
    private LineCanvas canvas;
    private final ArrayList<Polygon> polygons = new ArrayList<>();

    // input handlers
    private MouseAdapter mouseAdapter;
    private KeyAdapter keyAdapter;

    // current drawing state
    private Point point;
    private Polygon polygon;
    private Point shapeStartPoint;
    private Point lastBrushPoint;
    private final SelectionTracker selectionTracker = new SelectionTracker();

    // mode flags for different tools
    private boolean isSelecting = false;
    private boolean shiftMode = false;
    private boolean polygonMode = false;
    private boolean fillMode = false;
    private boolean rectangleMode = false;
    private boolean triangleMode = false;
    private boolean circleMode = false;
    private boolean brushMode = false;
    private boolean eraserMode = false;

    private LineStyle currentLineStyle = LineStyle.SOLID;

    // default background color
    private Color backgroundColor = new Color(0xaaaaaa);

    // main entry point
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App(DEFAULT_WIDTH, DEFAULT_HEIGHT).start());
    }

    // initialize application with given dimensions
    public App(int width, int height) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SHIFT -> shiftMode = true;
                    case KeyEvent.VK_V -> currentLineStyle = LineStyle.DOTTED;
                    case KeyEvent.VK_B -> currentLineStyle = LineStyle.DASHED;
                }
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SHIFT -> shiftMode = false;
                    case KeyEvent.VK_V, KeyEvent.VK_B -> currentLineStyle = LineStyle.SOLID;
                }
            }
            return false;
        });

        // setup main window
        frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setTitle("Draw-draw-draw");
        frame.setResizable(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // initialize toolbar with menu handlers
        toolbar = new Toolbar(this::handleMenuAction);
        frame.setJMenuBar(toolbar.getMenuBar());
        toolbar.setSettingsChangeListener(e -> rasterizer.setLineWidth(toolbar.getLineWidth()));

        // setup raster buffers
        baseRaster = new RasterBufferedImage(width, height);
        previewRaster = new RasterBufferedImage(width, height);
        raster = new DoubleBufferedRaster(baseRaster, previewRaster);

        // create drawing panel
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

        // initialize drawing tools
        rasterizer = new LineCanvasRasterizer(raster);
        polygonRasterizer = new PolygonRasterizer(raster, rasterizer);
        canvas = new LineCanvas(new ArrayList<>(), new ArrayList<>());
        polygon = new Polygon();
        floodFiller = new FloodFiller(raster);

        // setup input handlers and show window
        createInputAdapters();
        frame.pack();
        frame.setVisible(true);
    }

    // start application with cleared canvas
    public void start() {
        clear(backgroundColor);
        panel.repaint();
    }

    // clear canvas with specified color
    public void clear(Color color) {
        raster.setClearColor(color.getRGB());
        raster.clear();
    }

    // render current raster to graphics context
    public void present(Graphics graphics) {
        raster.repaint(graphics);
        if (isSelecting) {
            graphics.setColor(Color.RED);
            graphics.drawString("Selection Tool Active", 10, 20);
        }
    }

    // handle menu actions from toolbar
    private void handleMenuAction(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "NEW": handleNew(); break;
            case "CLEAR": handleClear(); break;
            case "EXIT": handleExit(); break;
            case "TOOL_LINE": handleToolLine(); break;
            case "TOOL_POLYGON": handleToolPolygon(); break;
            case "TOOL_FILL": handleToolFill(); break;
            case "TOOL_ERASER": handleToolEraser(); break;
            case "UNDO": handleUndo(); break;
            case "OPEN": handleOpen(); break;
            case "SAVE": handleSave(); break;
            case "TOOL_RECTANGLE": handleToolRectangle(); break;
            case "TOOL_TRIANGLE": handleToolTriangle(); break;
            case "TOOL_CIRCLE": handleToolCircle(); break;
            case "TOOL_SELECT": handleToolSelect(); break;
            case "TOOL_BRUSH": handleToolBrush(); break;
            default: handleColorSelection(e); break;
        }
    }

    // reset canvas to initial state
    private void handleNew() {
        raster.clear();
        canvas = new LineCanvas(new ArrayList<>(), new ArrayList<>());
        polygon = new Polygon();
        panel.repaint();
    }

    // clear current drawing
    private void handleClear() {
        raster.clear();
        panel.repaint();
    }

    // exit application
    private void handleExit() {
        System.exit(0);
    }

    // activate line drawing mode
    private void handleToolLine() {
        resetModes();
        toolbar.setActiveTool("LINE");
    }

    // activate polygon drawing mode
    private void handleToolPolygon() {
        resetModes();
        polygonMode = true;
        polygon = new Polygon();
        toolbar.setActiveTool("POLYGON");
    }

    // activate fill tool mode
    private void handleToolFill() {
        resetModes();
        fillMode = true;
        toolbar.setActiveTool("FILL");
    }

    // activate eraser tool mode
    private void handleToolEraser() {
        resetModes();
        eraserMode = true;
        toolbar.setActiveTool("ERASER");
    }

    // undo last drawing action
    private void handleUndo() {
        if (!canvas.getLines().isEmpty()) {
            canvas.getLines().remove(canvas.getLines().size() - 1);
            raster.clear();
            rasterizer.rasterizeCanvas(canvas);
            panel.repaint();
        }
    }

    // placeholder for open functionality
    private void handleOpen() {
        JOptionPane.showMessageDialog(panel, "No, thanks.");
    }

    // placeholder for save functionality
    private void handleSave() {
        JOptionPane.showMessageDialog(panel, "Deleting System32...");
    }

    // activate rectangle drawing mode
    private void handleToolRectangle() {
        resetModes();
        rectangleMode = true;
        toolbar.setActiveTool("RECTANGLE");
    }

    // activate triangle drawing mode
    private void handleToolTriangle() {
        resetModes();
        triangleMode = true;
        toolbar.setActiveTool("TRIANGLE");
    }

    // activate circle drawing mode
    private void handleToolCircle() {
        resetModes();
        circleMode = true;
        toolbar.setActiveTool("CIRCLE");
    }

    // activate selection tool mode
    private void handleToolSelect() {
        resetModes();
        isSelecting = true;
        toolbar.setActiveTool("SELECT");
    }

    // activate brush tool mode
    private void handleToolBrush() {
        resetModes();
        brushMode = true;
        toolbar.setActiveTool("BRUSH");
    }

    // handle color selection from toolbar
    private void handleColorSelection(ActionEvent e) {
        if (e.getActionCommand().startsWith("COLOR_")) {
            System.out.println("Selected color: " + toolbar.getSelectedColor());
        }
    }

    // reset all tool modes to default state
    private void resetModes() {
        raster.endPreview();
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

    // create mouse input handlers
    private void createInputAdapters() {
        createMouseAdapter();

        panel.addMouseMotionListener(mouseAdapter);
        panel.addMouseListener(mouseAdapter);
        panel.addKeyListener(keyAdapter);
        panel.requestFocus();
        panel.requestFocusInWindow();
    }

    // setup mouse event handlers
    private void createMouseAdapter() {
        mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isSelecting) handleSelectPress(e);
                else if (fillMode) handleFillPress(e);
                else if (polygonMode) handlePolygonPress(e);
                else if (brushMode || eraserMode) handleBrushPress(e);
                else handleDefaultPress(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectionTracker.hasSelection()) handleSelectRelease();
                else if (brushMode || eraserMode) handleBrushRelease();
                else if (rectangleMode || triangleMode || circleMode) handleShapeRelease(e);
                else if (!polygonMode) handleLineRelease(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectionTracker.hasSelection()) handleSelectDrag(e);
                else if (brushMode || eraserMode) handleBrushDrag(e);
                else if (rectangleMode || triangleMode || circleMode) handleShapeDrag(e);
                else if (!polygonMode) handleLineDrag(e);
            }
        };
    }

    // handle selection tool press
    private void handleSelectPress(MouseEvent e) {
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

    // handle fill tool press
    private void handleFillPress(MouseEvent e) {
        floodFiller.floodFill(e.getX(), e.getY(), toolbar.getSelectedColor());
        panel.repaint();
    }

    // handle polygon tool press
    private void handlePolygonPress(MouseEvent e) {
        point = new Point(e.getX(), e.getY());
        polygon.addPoint(point);
        if (polygon.getPoints().size() >= 3) {
            raster.startPreview();
            previewRaster.copyFrom(baseRaster);
            polygonRasterizer.rasterize(polygon, toolbar.getSelectedColor(), currentLineStyle, toolbar.getLineWidth());
            panel.repaint();
        }
    }

    // handle brush/eraser press
    private void handleBrushPress(MouseEvent e) {
        lastBrushPoint = new Point(e.getX(), e.getY());
        raster.startPreview();
        previewRaster.copyFrom(baseRaster);
        Color color = eraserMode ? backgroundColor : toolbar.getSelectedColor();
        raster.setPixel(e.getX(), e.getY(), color.getRGB());
        panel.repaint();
    }

    // handle default tool press
    private void handleDefaultPress(MouseEvent e) {
        point = new Point(e.getX(), e.getY());
        shapeStartPoint = point;
        raster.startPreview();
        previewRaster.copyFrom(baseRaster);
    }

    // handle selection tool release
    private void handleSelectRelease() {
        commitSelectionChanges();
    }

    // handle brush/eraser release
    private void handleBrushRelease() {
        raster.endPreview();
        panel.repaint();
    }

    // handle shape tool release
    private void handleShapeRelease(MouseEvent e) {
        Point endPoint = alignPoint(new Point(e.getX(), e.getY()));
        Polygon shape = createCurrentShape(shapeStartPoint, endPoint);

        if (shape != null) {
            polygons.add(shape);
            polygonRasterizer.rasterize(shape, toolbar.getSelectedColor(), currentLineStyle, toolbar.getLineWidth());
        }

        raster.endPreview();
        panel.repaint();
    }

    // handle line tool release
    private void handleLineRelease(MouseEvent e) {
        Point point2 = alignPoint(new Point(e.getX(), e.getY()));
        Line line = new Line(point, point2, toolbar.getSelectedColor(), currentLineStyle);
        canvas.add(line);

        raster.endPreview();
        rasterizer.rasterizeCanvas(canvas);
        panel.repaint();
    }

    // handle selection tool drag
    private void handleSelectDrag(MouseEvent e) {
        selectionTracker.updateDrag(e.getX(), e.getY());
        raster.clear();
        redrawEverything();
    }

    // handle brush/eraser drag
    private void handleBrushDrag(MouseEvent e) {
        Point currentPoint = new Point(e.getX(), e.getY());
        if (lastBrushPoint != null) {
            Color color = eraserMode ? backgroundColor : toolbar.getSelectedColor();
            Line line = new Line(lastBrushPoint, currentPoint, color, LineStyle.SOLID);
            rasterizer.setLineWidth(toolbar.getLineWidth());
            rasterizer.rasterizeLine(line);
            panel.repaint();
        }
        lastBrushPoint = currentPoint;
    }

    // handle shape tool drag
    private void handleShapeDrag(MouseEvent e) {
        Point endPoint = alignPoint(new Point(e.getX(), e.getY()));
        raster.startPreview();
        previewRaster.copyFrom(baseRaster);

        Polygon shape = createCurrentShape(shapeStartPoint, endPoint);
        if (shape != null) {
            polygonRasterizer.rasterize(shape, toolbar.getSelectedColor(), currentLineStyle, toolbar.getLineWidth());
        }
        panel.repaint();
    }

    // handle line tool drag
    private void handleLineDrag(MouseEvent e) {
        Point point2 = alignPoint(new Point(e.getX(), e.getY()));
        if (point != null && point2 != null) {
            Line line = new Line(point, point2, toolbar.getSelectedColor(), currentLineStyle);

            raster.startPreview();
            previewRaster.copyFrom(baseRaster);
            rasterizer.rasterizeCanvas(canvas);

            rasterizer.setLineWidth(toolbar.getLineWidth());
            rasterizer.rasterizeLine(line);

            panel.repaint();
        }
    }

    // create shape based on current mode
    private Polygon createCurrentShape(Point start, Point end) {
        if (rectangleMode) return createRectangle(start, end);
        if (triangleMode) return createTriangle(start, end);
        if (circleMode) return createCircle(start, end);
        return null;
    }

    // create rectangle polygon
    private Polygon createRectangle(Point start, Point end) {
        Polygon rectangle = new Polygon();
        rectangle.addPoint(new Point(start.getX(), start.getY()));
        rectangle.addPoint(new Point(end.getX(), start.getY()));
        rectangle.addPoint(new Point(end.getX(), end.getY()));
        rectangle.addPoint(new Point(start.getX(), end.getY()));
        return rectangle;
    }

    // create triangle polygon
    private Polygon createTriangle(Point start, Point end) {
        Polygon triangle = new Polygon();
        int width = end.getX() - start.getX();
        triangle.addPoint(new Point(start.getX() + width/2, start.getY()));
        triangle.addPoint(new Point(end.getX(), end.getY()));
        triangle.addPoint(new Point(start.getX(), end.getY()));
        return triangle;
    }

    // create circle polygon
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

    // align point for straight lines when shift is pressed
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

    // try to select shape at given coordinates
    private boolean trySelectShape(int x, int y) {
        // check polygons from newest to oldest
        for (int i = polygons.size() - 1; i >= 0; i--) {
            Polygon polygon = polygons.get(i);
            if (polygonRasterizer.isPointInsidePolygon(polygon, new Point(x, y))) {
                selectionTracker.selectPolygon(polygon);
                return true;
            }
        }

        // check lines from newest to oldest
        for (int i = canvas.getLines().size() - 1; i >= 0; i--) {
            Line line = canvas.getLines().get(i);
            if (isPointNearLine(x, y, line)) {
                selectionTracker.selectLine(line);
                return true;
            }
        }

        return false;
    }

    // check if point is near line
    private boolean isPointNearLine(int x, int y, Line line) {
        Point p1 = line.getPoint1();
        Point p2 = line.getPoint2();

        double lineLength = p1.distanceTo(p2);
        if (lineLength == 0) return p1.distanceTo(new Point(x, y)) < 5;

        double t = ((x - p1.getX()) * (p2.getX() - p1.getX()) +
                (y - p1.getY()) * (p2.getY() - p1.getY())) / (lineLength * lineLength);

        t = Math.max(0, Math.min(1, t));

        double projX = p1.getX() + t * (p2.getX() - p1.getX());
        double projY = p1.getY() + t * (p2.getY() - p1.getY());

        return new Point(x, y).distanceTo(new Point((int)projX, (int)projY)) < 5;
    }

    // redraw all elements on canvas
    private void redrawEverything() {
        if (isSelecting && selectionTracker.hasSelection()) {
            raster.startPreview();
            redrawBaseToPreview();

            for (Line line : selectionTracker.getSelectedLines()) {
                rasterizer.rasterizeLine(line);
            }
            for (Polygon polygon : selectionTracker.getSelectedPolygons()) {
                polygonRasterizer.rasterize(polygon, toolbar.getSelectedColor(), currentLineStyle, toolbar.getLineWidth());
            }
        } else {
            commitAllChanges();
        }
        panel.repaint();
    }

    // copy base raster to preview
    private void redrawBaseToPreview() {
        previewRaster.clear();
        previewRaster.copyFrom(baseRaster);
    }

    // commit all changes to base raster
    private void commitAllChanges() {
        baseRaster.clear();
        previewRaster.clear();

        rasterizer.rasterizeCanvas(canvas);
        for (Polygon polygon : polygons) {
            polygonRasterizer.rasterize(polygon, toolbar.getSelectedColor(), currentLineStyle, toolbar.getLineWidth());
        }

        previewRaster.copyFrom(baseRaster);
        selectionTracker.resetChangeFlag();
        panel.repaint();
    }

    // commit selection changes to base raster
    private void commitSelectionChanges() {
        raster.endPreview();
        redrawEverything();
    }
}