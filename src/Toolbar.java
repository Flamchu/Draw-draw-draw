import models.LineStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;

public class Toolbar {
    private final JMenuBar menuBar;
    private final JCheckBoxMenuItem lineItem;
    private final JCheckBoxMenuItem polygonItem;
    private final JCheckBoxMenuItem fillItem;
    private final JCheckBoxMenuItem rectangleItem;
    private final JCheckBoxMenuItem triangleItem;
    private final JCheckBoxMenuItem circleItem;
    private final JCheckBoxMenuItem brushItem;
    private final JCheckBoxMenuItem eraserItem;
    private final JSlider widthSlider;
    private JComboBox<String> styleComboBox;

    private Color selectedColor = Color.WHITE;
    private int lineWidth = 1;
    private boolean dottedLine = false;
    private ActionListener settingsChangeListener;
    private LineStyle lineStyle = LineStyle.SOLID;

    private final Color[] colorPalette = {
            Color.WHITE,
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.MAGENTA
    };

    public Toolbar(ActionListener actionListener) {
        menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        addMenuItem(fileMenu, "New", "NEW", actionListener);
        addMenuItem(fileMenu, "Open", "OPEN", actionListener);
        addMenuItem(fileMenu, "Save", "SAVE", actionListener);
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Exit", "EXIT", actionListener);

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        addMenuItem(editMenu, "Undo", "UNDO", actionListener);
        editMenu.addSeparator();
        addMenuItem(editMenu, "Clear Canvas", "CLEAR", actionListener);

        // Tools menu
        JMenu toolsMenu = new JMenu("Tools");
        lineItem = new JCheckBoxMenuItem("Line Tool", true);
        polygonItem = new JCheckBoxMenuItem("Polygon Tool");
        fillItem = new JCheckBoxMenuItem("Fill Tool");
        rectangleItem = new JCheckBoxMenuItem("Rectangle Tool");
        triangleItem = new JCheckBoxMenuItem("Triangle Tool");
        circleItem = new JCheckBoxMenuItem("Circle Tool");
        brushItem = new JCheckBoxMenuItem("Brush Tool");

        JMenu lineStyleMenu = new JMenu("Line Style");

        ButtonGroup lineStyleGroup = new ButtonGroup();

        ButtonGroup toolsGroup = new ButtonGroup();
        toolsGroup.add(lineItem);
        toolsGroup.add(polygonItem);
        toolsGroup.add(fillItem);
        toolsGroup.add(rectangleItem);
        toolsGroup.add(triangleItem);
        toolsGroup.add(circleItem);
        toolsGroup.add(brushItem);

        lineItem.setActionCommand("TOOL_LINE");
        polygonItem.setActionCommand("TOOL_POLYGON");
        fillItem.setActionCommand("TOOL_FILL");
        rectangleItem.setActionCommand("TOOL_RECTANGLE");
        triangleItem.setActionCommand("TOOL_TRIANGLE");
        circleItem.setActionCommand("TOOL_CIRCLE");
        brushItem.setActionCommand("TOOL_BRUSH");

        lineItem.addActionListener(actionListener);
        polygonItem.addActionListener(actionListener);
        fillItem.addActionListener(actionListener);
        rectangleItem.addActionListener(actionListener);
        triangleItem.addActionListener(actionListener);
        circleItem.addActionListener(actionListener);
        brushItem.addActionListener(actionListener);

        toolsMenu.add(lineItem);
        toolsMenu.add(polygonItem);
        toolsMenu.add(fillItem);
        toolsMenu.add(rectangleItem);
        toolsMenu.add(triangleItem);
        toolsMenu.add(circleItem);
        toolsMenu.add(brushItem);

        // Color menu
        JMenu colorMenu = new JMenu("Colors");
        for (int i = 0; i < colorPalette.length; i++) {
            Color color = colorPalette[i];
            JMenuItem colorItem = new JMenuItem("Color " + (i + 1));
            colorItem.setActionCommand("COLOR_" + i);
            colorItem.addActionListener(e -> {
                selectedColor = color;
                updateColorSelection();
            });
            colorItem.setIcon(createColorIcon(color));
            colorMenu.add(colorItem);
        }

        // Settings menu
        JMenu settingsMenu = new JMenu("Settings");

        // Line Width Slider
        JPanel widthPanel = new JPanel();
        widthPanel.setLayout(new BoxLayout(widthPanel, BoxLayout.Y_AXIS));
        widthPanel.add(new JLabel("Line Width:"));

        widthSlider = new JSlider(1, 20, lineWidth);
        widthSlider.setMajorTickSpacing(5);
        widthSlider.setMinorTickSpacing(1);
        widthSlider.setPaintTicks(true);
        widthSlider.setPaintLabels(true);
        widthSlider.addChangeListener(e -> fireSettingsChanged());
        widthPanel.add(widthSlider);

        // Line Style ComboBox
        JPanel stylePanel = new JPanel();
        stylePanel.setLayout(new BoxLayout(stylePanel, BoxLayout.Y_AXIS));
        stylePanel.add(new JLabel("Line Style:"));

        String[] lineStyles = {"Solid", "Dotted", "Dashed"};
        styleComboBox = new JComboBox<>(lineStyles);
        styleComboBox.addActionListener(e -> fireSettingsChanged());
        stylePanel.add(styleComboBox);

        settingsMenu.add(widthPanel);
        settingsMenu.add(stylePanel);

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(toolsMenu);
        menuBar.add(colorMenu);
        menuBar.add(settingsMenu);

        eraserItem = new JCheckBoxMenuItem("Eraser Tool");
        toolsGroup.add(eraserItem);
        eraserItem.setActionCommand("TOOL_ERASER");
        eraserItem.addActionListener(actionListener);
        toolsMenu.add(eraserItem);
    }

    private void createStyleComboBox() {
        String[] lineStyles = {"Solid", "Dotted", "Dashed"};
        styleComboBox = new JComboBox<>(lineStyles);
        styleComboBox.addActionListener(e -> fireSettingsChanged());
    }


    private Icon createColorIcon(Color color) {
        int width = 16;
        int height = 16;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(color);
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, width-1, height-1);
        g2.dispose();
        return new ImageIcon(image);
    }

    private void updateColorSelection() {
        System.out.println("Selected color: " + selectedColor);
    }

    public void setSettingsChangeListener(ActionListener listener) {
        this.settingsChangeListener = listener;
    }

    private void fireSettingsChanged() {

        String selectedStyle = (String) styleComboBox.getSelectedItem();
        switch (selectedStyle) {
            case "Solid": lineStyle = LineStyle.SOLID; break;
            case "Dotted": lineStyle = LineStyle.DOTTED; break;
            case "Dashed": lineStyle = LineStyle.DASHED; break;
        }

        lineWidth = widthSlider.getValue();
        dottedLine = styleComboBox.getSelectedIndex() == 1;
        if (settingsChangeListener != null) {
            settingsChangeListener.actionPerformed(
                    new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "SETTINGS_CHANGED"));
        }
    }

    public Color getSelectedColor() {
        return selectedColor;
    }

    public LineStyle getLineStyle() {
        return lineStyle;
    }
    public int getLineWidth() {
        return lineWidth;
    }

    public boolean isDottedLine() {
        return dottedLine;
    }

    private void addMenuItem(JMenu menu, String text, String actionCommand, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.setActionCommand(actionCommand);
        item.addActionListener(listener);
        menu.add(item);
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public void setActiveTool(String tool) {
        lineItem.setSelected(false);
        polygonItem.setSelected(false);
        fillItem.setSelected(false);
        rectangleItem.setSelected(false);
        triangleItem.setSelected(false);
        circleItem.setSelected(false);
        brushItem.setSelected(false);

        switch (tool) {
            case "LINE":
                lineItem.setSelected(true);
                break;
            case "POLYGON":
                polygonItem.setSelected(true);
                break;
            case "FILL":
                fillItem.setSelected(true);
                break;
            case "RECTANGLE":
                rectangleItem.setSelected(true);
                break;
            case "TRIANGLE":
                triangleItem.setSelected(true);
                break;
            case "CIRCLE":
                circleItem.setSelected(true);
                break;
            case "BRUSH":
                brushItem.setSelected(true);
                break;
        }
    }
}