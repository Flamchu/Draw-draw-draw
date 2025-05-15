package models;

import java.util.ArrayList;
import java.util.List;

public class SelectionTracker {
    private List<Line> selectedLines = new ArrayList<>();
    private List<Polygon> selectedPolygons = new ArrayList<>();
    private Point dragStart;
    private Point resizePoint;
    private boolean isResizing = false;
    private int handleSize = 6;
    private int selectionTolerance = 8;
    private boolean hasUncommittedChanges = false;

    // select a single line and clear other selections
    public void selectLine(Line line) {
        clearSelection();
        selectedLines.add(line);
    }

    // select a single polygon and clear other selections
    public void selectPolygon(Polygon polygon) {
        clearSelection();
        selectedPolygons.add(polygon);
    }

    // clear all current selections and reset drag state
    public void clearSelection() {
        selectedLines.clear();
        selectedPolygons.clear();
        dragStart = null;
        resizePoint = null;
        isResizing = false;
    }

    // check if any items are currently selected
    public boolean hasSelection() {
        return !selectedLines.isEmpty() || !selectedPolygons.isEmpty();
    }

    // reset the uncommitted changes flag
    public void resetChangeFlag() {
        hasUncommittedChanges = false;
    }

    // start dragging from given coordinates
    public void startDrag(int x, int y) {
        dragStart = new Point(x, y);
        resizePoint = findResizePoint(x, y);
        isResizing = (resizePoint != null);
    }

    // update drag position and handle movement or resizing
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

    // move all selected items by given delta
    private void handleMove(int dx, int dy) {
        for (Line line : selectedLines) {
            line.getPoint1().translate(dx, dy);
            line.getPoint2().translate(dx, dy);
        }

        for (Polygon polygon : selectedPolygons) {
            for (Point p : polygon.getPoints()) {
                p.translate(dx, dy);
            }
        }
    }

    // resize selected items from the resize point
    private void handleResize(int dx, int dy) {
        if (resizePoint != null) {
            resizePoint.translate(dx, dy);

            if (selectedLines.size() == 1 && selectedPolygons.isEmpty()) {
                Line line = selectedLines.get(0);
                if (resizePoint == line.getPoint1()) {
                    // handle line point1 resize
                } else if (resizePoint == line.getPoint2()) {
                    // handle line point2 resize
                }
            }
        }
    }

    // find resize handle near given coordinates
    public Point findResizePoint(int x, int y) {
        for (Line line : selectedLines) {
            if (line.getPoint1().distanceTo(new Point(x, y)) < selectionTolerance) {
                return line.getPoint1();
            }
            if (line.getPoint2().distanceTo(new Point(x, y)) < selectionTolerance) {
                return line.getPoint2();
            }
        }

        for (Polygon polygon : selectedPolygons) {
            for (Point p : polygon.getPoints()) {
                if (p.distanceTo(new Point(x, y)) < selectionTolerance) {
                    return p;
                }
            }
        }

        return null;
    }

    // get copy of selected lines list
    public List<Line> getSelectedLines() {
        return new ArrayList<>(selectedLines);
    }

    // get copy of selected polygons list
    public List<Polygon> getSelectedPolygons() {
        return new ArrayList<>(selectedPolygons);
    }
}