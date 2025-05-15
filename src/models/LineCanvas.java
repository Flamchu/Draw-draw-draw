package models;

import java.util.ArrayList;

public class LineCanvas {

    private ArrayList<Line> lines;
    private ArrayList<Line> dottedLines;

    // initialize canvas with regular and dotted lines
    public LineCanvas(
            ArrayList<Line> lines,
            ArrayList<Line> dottedLines
    ) {
        this.lines = lines;
        this.dottedLines = dottedLines;
    }

    // get all regular lines
    public ArrayList<Line> getLines() {
        return lines;
    }

    // get all dotted lines
    public ArrayList<Line> getDottedLines() {
        return dottedLines;
    }

    // add a regular line to canvas
    public void add(Line line) {
        lines.add(line);
    }

    // add a dotted line to canvas
    public void addDottedLine(Line line) {
        dottedLines.add(line);
    }

    // clear all lines from canvas
    public void clear() {
        lines.clear();
        dottedLines.clear();
    }
}