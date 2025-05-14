package models;

import java.util.ArrayList;
import java.util.List;

public class SelectionTracker {
    private List<Line> selectedLines = new ArrayList<>();
    private List<Polygon> selectedPolygons = new ArrayList<>();
    private Point dragStart;
    private Point resizePoint;
    private boolean isResizing = false;
    private int handleSize = 6; // Size of selection handles in pixels
    private int selectionTolerance = 8; // Pixel tolerance for selection
    private boolean hasUncommittedChanges = false;

    // Selection methods
    public void selectLine(Line line) {
        clearSelection();
        selectedLines.add(line);
    }

    public void selectPolygon(Polygon polygon) {
        clearSelection();
        selectedPolygons.add(polygon);
    }

    public void clearSelection() {
        selectedLines.clear();
        selectedPolygons.clear();
        dragStart = null;
        resizePoint = null;
        isResizing = false;
    }

    public boolean hasSelection() {
        return !selectedLines.isEmpty() || !selectedPolygons.isEmpty();
    }

    public void resetChangeFlag() {
        hasUncommittedChanges = false;
    }

    // Drag operations
    public void startDrag(int x, int y) {
        dragStart = new Point(x, y);
        resizePoint = findResizePoint(x, y);
        isResizing = (resizePoint != null);
    }

    public void updateDrag(int x, int y) {
        if (dragStart == null) return;

        int dx = x - dragStart.getX();
        int dy = y - dragStart.getY();

        if (isResizing) {
            handleResize(dx, dy);
        } else {
            handleMove(dx, dy);
        }

        dragStart = new Point(x, y);
        hasUncommittedChanges = true;
    }

    private void handleMove(int dx, int dy) {
        // Move all selected lines
        for (Line line : selectedLines) {
            line.getPoint1().translate(dx, dy);
            line.getPoint2().translate(dx, dy);
        }

        // Move all selected polygons
        for (Polygon polygon : selectedPolygons) {
            for (Point p : polygon.getPoints()) {
                p.translate(dx, dy);
            }
        }
    }

    private void handleResize(int dx, int dy) {
        if (resizePoint != null) {
            resizePoint.translate(dx, dy);

            // For lines, ensure the other point stays connected if needed
            if (selectedLines.size() == 1 && selectedPolygons.isEmpty()) {
                Line line = selectedLines.get(0);
                if (resizePoint == line.getPoint1()) {
                    // Optional: Add constraints here if needed
                } else if (resizePoint == line.getPoint2()) {
                    // Optional: Add constraints here if needed
                }
            }
        }
    }

    // Hit testing methods
    public Point findResizePoint(int x, int y) {
        // Check lines first (priority to lines if they overlap)
        for (Line line : selectedLines) {
            if (line.getPoint1().distanceTo(new Point(x, y)) < selectionTolerance) {
                return line.getPoint1();
            }
            if (line.getPoint2().distanceTo(new Point(x, y)) < selectionTolerance) {
                return line.getPoint2();
            }
        }

        // Check polygons
        for (Polygon polygon : selectedPolygons) {
            for (Point p : polygon.getPoints()) {
                if (p.distanceTo(new Point(x, y)) < selectionTolerance) {
                    return p;
                }
            }
        }

        return null;
    }
    // Getters for selected items
    public List<Line> getSelectedLines() {
        return new ArrayList<>(selectedLines);
    }

    public List<Polygon> getSelectedPolygons() {
        return new ArrayList<>(selectedPolygons);
    }
}