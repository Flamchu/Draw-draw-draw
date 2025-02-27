package models;

import java.util.ArrayList;

public class LineCanvas {

    private ArrayList<Line> lines;
    private ArrayList<Line> dottedLines;

    public LineCanvas(
            ArrayList<Line> lines,
            ArrayList<Line> dottedLines
    ) {
        this.lines = lines;
        this.dottedLines = dottedLines;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public ArrayList<Line> getDottedLines() {
        return dottedLines;
    }

    public void add(Line line) {
        lines.add(line);
    }

    public void addDottedLine(Line line) {
        dottedLines.add(line);
    }

    public void clear() {
        lines.clear();
        dottedLines.clear();
    }

}
