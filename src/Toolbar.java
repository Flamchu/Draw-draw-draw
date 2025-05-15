import models.LineStyle;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;

public class Toolbar {
    // main menu bar component
    private final JMenuBar menuBar;

    // tool selection menu items
    private final JCheckBoxMenuItem lineItem = new JCheckBoxMenuItem("Line Tool", true);
    private final JCheckBoxMenuItem polygonItem = new JCheckBoxMenuItem("Polygon Tool");
    private final JCheckBoxMenuItem fillItem = new JCheckBoxMenuItem("Fill Tool");
    private final JCheckBoxMenuItem rectangleItem = new JCheckBoxMenuItem("Rectangle Tool");
    private final JCheckBoxMenuItem triangleItem = new JCheckBoxMenuItem("Triangle Tool");
    private final JCheckBoxMenuItem circleItem = new JCheckBoxMenuItem("Circle Tool");
    private final JCheckBoxMenuItem brushItem = new JCheckBoxMenuItem("Brush Tool");
    private final JCheckBoxMenuItem eraserItem = new JCheckBoxMenuItem("Eraser Tool");
    private final JCheckBoxMenuItem selectItem = new JCheckBoxMenuItem("Selection Tool");

    // drawing settings components
    private final JSlider widthSlider;
    private final JComboBox<String> styleComboBox;

    // current drawing settings
    private Color selectedColor = Color.WHITE;
    private int lineWidth = 1;
    private ActionListener settingsChangeListener;

    // available color palette
    private final Color[] colorPalette = {
            Color.WHITE,
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.MAGENTA
    };

    // initialize toolbar with action listener
    public Toolbar(ActionListener actionListener) {
        menuBar = new JMenuBar();

        // setup settings components
        widthSlider = new JSlider(1, 20, lineWidth);
        styleComboBox = new JComboBox<>(new String[]{"Solid", "Dotted", "Dashed"});

        // build all menu sections
        buildFileMenu(actionListener);
        buildEditMenu(actionListener);
        buildToolsMenu(actionListener);
        buildColorMenu();
        buildSettingsMenu();
    }

    // build file menu with basic operations
    private void buildFileMenu(ActionListener actionListener) {
        JMenu fileMenu = new JMenu("File");
        addMenuItem(fileMenu, "New", "NEW", actionListener);
        addMenuItem(fileMenu, "Open", "OPEN", actionListener);
        addMenuItem(fileMenu, "Save", "SAVE", actionListener);
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Exit", "EXIT", actionListener);
        menuBar.add(fileMenu);
    }

    // build edit menu with undo/clear
    private void buildEditMenu(ActionListener actionListener) {
        JMenu editMenu = new JMenu("Edit");
        addMenuItem(editMenu, "Undo", "UNDO", actionListener);
        editMenu.addSeparator();
        addMenuItem(editMenu, "Clear Canvas", "CLEAR", actionListener);
        menuBar.add(editMenu);
    }

    // build tools menu with all drawing tools
    private void buildToolsMenu(ActionListener actionListener) {
        JMenu toolsMenu = new JMenu("Tools");

        // configure tool items
        lineItem.setActionCommand("TOOL_LINE");
        polygonItem.setActionCommand("TOOL_POLYGON");
        fillItem.setActionCommand("TOOL_FILL");
        rectangleItem.setActionCommand("TOOL_RECTANGLE");
        triangleItem.setActionCommand("TOOL_TRIANGLE");
        circleItem.setActionCommand("TOOL_CIRCLE");
        brushItem.setActionCommand("TOOL_BRUSH");
        eraserItem.setActionCommand("TOOL_ERASER");
        selectItem.setActionCommand("TOOL_SELECT");

        // add action listeners
        lineItem.addActionListener(actionListener);
        polygonItem.addActionListener(actionListener);
        fillItem.addActionListener(actionListener);
        rectangleItem.addActionListener(actionListener);
        triangleItem.addActionListener(actionListener);
        circleItem.addActionListener(actionListener);
        brushItem.addActionListener(actionListener);
        eraserItem.addActionListener(actionListener);
        selectItem.addActionListener(actionListener);

        // group tools as radio buttons
        ButtonGroup toolsGroup = new ButtonGroup();
        toolsGroup.add(lineItem);
        toolsGroup.add(polygonItem);
        toolsGroup.add(fillItem);
        toolsGroup.add(rectangleItem);
        toolsGroup.add(triangleItem);
        toolsGroup.add(circleItem);
        toolsGroup.add(brushItem);
        toolsGroup.add(eraserItem);
        toolsGroup.add(selectItem);

        // add tools to menu
        toolsMenu.add(lineItem);
        toolsMenu.add(polygonItem);
        toolsMenu.add(fillItem);
        toolsMenu.add(rectangleItem);
        toolsMenu.add(triangleItem);
        toolsMenu.add(circleItem);
        toolsMenu.add(brushItem);
        toolsMenu.add(eraserItem);
        toolsMenu.add(selectItem);

        menuBar.add(toolsMenu);
    }

    // build color selection menu
    private void buildColorMenu() {
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
        menuBar.add(colorMenu);
    }

    // build settings menu with line options
    private void buildSettingsMenu() {
        JMenu settingsMenu = new JMenu("Settings");

        // line width slider panel
        JPanel widthPanel = new JPanel();
        widthPanel.setLayout(new BoxLayout(widthPanel, BoxLayout.Y_AXIS));
        widthPanel.add(new JLabel("Line Width:"));

        widthSlider.setMajorTickSpacing(5);
        widthSlider.setMinorTickSpacing(1);
        widthSlider.setPaintTicks(true);
        widthSlider.setPaintLabels(true);
        widthSlider.addChangeListener(e -> fireSettingsChanged());
        widthPanel.add(widthSlider);

        // line style combo box panel
        settingsMenu.add(widthPanel);
        menuBar.add(settingsMenu);
    }

    // create color icon for menu items
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

    // handle color selection change
    private void updateColorSelection() {
        System.out.println("Selected color: " + selectedColor);
    }

    // notify listeners when settings change
    private void fireSettingsChanged() {
        lineWidth = widthSlider.getValue();
        if (settingsChangeListener != null) {
            settingsChangeListener.actionPerformed(
                    new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "SETTINGS_CHANGED"));
        }
    }

    // helper to add menu items
    private void addMenuItem(JMenu menu, String text, String actionCommand, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.setActionCommand(actionCommand);
        item.addActionListener(listener);
        menu.add(item);
    }

    // get the menu bar component
    public JMenuBar getMenuBar() {
        return menuBar;
    }

    // get currently selected color
    public Color getSelectedColor() {
        return selectedColor;
    }

    // get current line width
    public int getLineWidth() {
        return lineWidth;
    }

    // set listener for settings changes
    public void setSettingsChangeListener(ActionListener listener) {
        this.settingsChangeListener = listener;
    }

    // set active tool in menu
    public void setActiveTool(String tool) {
        selectItem.setSelected(false);
        lineItem.setSelected(false);
        polygonItem.setSelected(false);
        fillItem.setSelected(false);
        rectangleItem.setSelected(false);
        triangleItem.setSelected(false);
        circleItem.setSelected(false);
        brushItem.setSelected(false);

        switch (tool) {
            case "SELECT":
                selectItem.setSelected(true);
                break;
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