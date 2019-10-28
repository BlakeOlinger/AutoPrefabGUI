package com.ToppInd;

import bo.core.system.FilesUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final String PATH_BASE = "C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\";
    private static final Path COVER_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.cover.txt");
    private static final Path SQUARE_COVER_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.coverSquare.txt");
    private static final Path COVER_SHAPE_ASSEMBLY_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.coverConfig.txt");
    private static final Path REBUILD_DAEMON_APP_DATA_PATH = Paths.get("C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\app data\\rebuild.txt");
    private static final String APP_DATA_BASE = "C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\app data\\";
    private static final Path APP_DATA_HANDLE_OFFSET_TABLE = Paths.get(APP_DATA_BASE + "assembly handle offset table.txt");
    private static final Path COVER_ASSEMBLY_CONFIG_PATH = Paths.get("C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\blob - L2\\blob.L2_cover.txt");
    private static final Path ANGLE_FRAME_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.2inAngleFrame.txt");
    private static final Path ALUM_FLAT_BAR_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.alumFlatBar.txt");
    private static final Path INSPECTION_PLATE_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.inspectionPlate.txt");
    private static final Path COVER_DRAWING_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.coverDrawing.txt");
    private static final Path COVER_ASSEMBLY_PATH = Paths.get(PATH_BASE + "blob - L2\\blob.L2_cover.SLDASM");
    private static final Path COVER_DRAWING_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.cover.SLDDRW");
    private static HashMap<String, Integer> coverConfigVariableNameLineNumberTable = new HashMap<>();
    private static HashMap<String, String> coverConfigVariableUserInputTable = new HashMap<>();
    private static String coverShapeSelection = "Circular";
    private static final HashMap<String, String> MATERIAL_CONFIG_TABLE = new HashMap<>(
            Map.of(
                    "ASTM A36 Steel", "0",
                    "6061 Alloy", "1"
            )
    );
    private static final boolean REBUILDABLE = true;
    private static final boolean WRITEABLE = true;
    private static final boolean ASSEMBLY_MATE_CALIBRATION = false;

    public static void main(String[] args) {
        // display main window
         displayAppWindow();
    }

    private static void setUserInputParametersTable() {
        for (String variable : coverConfigVariableNameLineNumberTable.keySet()) {
            coverConfigVariableUserInputTable.put(variable, "");
        }
    }

    private static void setCoverConfigVariableNameLineNumberTable(Path path) {
        // read cover config contents
        var coverConfigContent = FilesUtil.read(path);

        // split by new line
        var coverConfigLines = coverConfigContent.split("\n");

        // create line to line number relationship for each variable
        var index = 0;
        // sort by line NOT contains @ or "IIF" - increment index each line - if NOT HashMap.put()
        for (String line : coverConfigLines) {
            if (!line.contains("@") && !line.contains("IIF") && !line.contains("Negative")){
                // get variable name from line
                var variableName = line.split("=")[0];
                coverConfigVariableNameLineNumberTable.put(variableName, index);
            }
            ++index;
        }
    }

    private static void displayAppWindow() {
        var window = new JFrame("AutoPrefab");
        window.setSize(400, 200);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new FlowLayout());

        // add "Configure Cover" button
        window.add(configureCoverButton());

        // add "Build Drawing" button
        window.add(configureDrawingButton());

        window.setVisible(true);
    }

    private static JButton configureDrawingButton() {
        var button = new JButton("Configure Drawing");
        button.addActionListener(e -> displayConfigureDrawingWindow());
        return button;
    }
// drawing configurer
    private static void displayConfigureDrawingWindow() {
        var window = new JFrame("Drawing Configurer");
        window.setSize(300, 300);
        window.setLocationRelativeTo(null);
        window.setLayout(new FlowLayout());
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        window.add(customPropertiesButton());
        window.add(drawingViewScaleButton());
        window.add(generateDimensionsButton());
        window.add(autoBalloonButton());
        window.add(autoCenterMarkButton());

        window.setVisible(true);
    }

    private static JButton autoCenterMarkButton() {
        var button = new JButton("Center Mark");
        button.addActionListener(e -> rebuild(DaemonProgram.CENTER_MARK));
        return button;
    }

    private static JButton autoBalloonButton() {
        var button = new JButton("Feature Balloon");
        button.addActionListener(e -> rebuild(DaemonProgram.AUTO_BALLOON));
        return button;
    }

    private static JButton generateDimensionsButton() {
        var button = new JButton("Generate Dimensions");
        button.addActionListener(e -> handleDrawingGenerateDimensionsAction());
        return button;
    }

    private static void handleDrawingGenerateDimensionsAction() {
        // for now it is just a passive activator aside from writing the appropriate paths to app data
        var appData = COVER_ASSEMBLY_PATH + "\n";
        appData += COVER_ASSEMBLY_CONFIG_PATH + "\n";
        appData += COVER_DRAWING_PATH;

        writeToConfig(appData, REBUILD_DAEMON_APP_DATA_PATH);

        rebuild(DaemonProgram.DRAWING_AUTO_DIMENSION);
    }

    private static JButton customPropertiesButton() {
        var button = new JButton("Document Info");
        button.addActionListener(e -> displayCustomPropertiesConfigWindow());
        return button;
    }

    private static void displayCustomPropertiesConfigWindow() {
        var window = new JFrame("Document Information Configurer");
        window.setSize(225, 500);
        window.setLayout(new FlowLayout());
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        var propertyLabels = getPropertyLabels();
        for (JLabel label : propertyLabels) {
            window.add(label);
            var textBox = new JTextField(10);
            textBox.addActionListener(e -> handleDocumentPropertyAction(e, label));
            window.add(textBox);
        }

        window.add(documentPropertiesSetAsDefaultsButton());

        window.setVisible(true);
    }

    private static JButton documentPropertiesSetAsDefaultsButton() {
        var button = new JButton("Default");
        button.addActionListener(e -> handleDocumentPropertyAction());
        return button;
    }

    private static void handleDocumentPropertyAction(ActionEvent event, JLabel label) {
        var text = label.getText();
        var userInput = getUserTextInput(event);

        if (userInput != null) {
            var configLines = getLinesFromPath(COVER_DRAWING_CONFIG_PATH);

            var index = 0;
            for (String line : configLines) {
                if (line.contains("Property")) {
                    var identifier = text.replace(":", "").trim();
                    if (line.contains(identifier)) {
                        var newLine = getNewLineUserInput(line, userInput, "");
                        configLines[index] = newLine;
                    }
                }
                ++index;
            }

            var output = generateWriteOutput(configLines);
            writeToConfig(output, COVER_DRAWING_CONFIG_PATH);

            writeToConfig(COVER_DRAWING_CONFIG_PATH.toString(), REBUILD_DAEMON_APP_DATA_PATH);

            rebuild(DaemonProgram.DRAWING_PROPERTIES);
        }
    }

    // this resets the values to their defaults
    private static void handleDocumentPropertyAction() {
        var configLines = getLinesFromPath(COVER_DRAWING_CONFIG_PATH);
        var index = 0;
        for (String line : configLines) {
            if (line.contains("Property")) {
                var newLine = getNewLineUserInput(line, "<>", "");
                configLines[index] = newLine;
            }
            ++index;
        }

        var output = generateWriteOutput(configLines);
        writeToConfig(output, COVER_DRAWING_CONFIG_PATH);

        writeToConfig(COVER_DRAWING_CONFIG_PATH.toString(), REBUILD_DAEMON_APP_DATA_PATH);

        rebuild(DaemonProgram.DRAWING_PROPERTIES);
    }

    private static JLabel[] getPropertyLabels() {
        var configLines = getLinesFromPath(Main.COVER_DRAWING_CONFIG_PATH);
        var returnLabelTotal = 0;
        for (String line : configLines) {
            if (line.contains("Property")) {
                ++returnLabelTotal;
            }
        }
        var labels = new JLabel[returnLabelTotal];
        var labelIndex = 0;
        for (String line : configLines) {
            if (line.contains("Property")) {
                var text = line.split(":")[1].split("=")[0].replace("\"", "").trim() + ": ";
                labels[labelIndex++] = new JLabel(text);
            }
        }
        return labels;
    }

    private static JButton drawingViewScaleButton() {
        var button = new JButton("Drawing View Scale");
        button.addActionListener(e -> displayDrawingViewScaleConfigWindow());
        return button;
    }

    private static void displayDrawingViewScaleConfigWindow() {
        var window = new JFrame("Drawing Scale View Configurer");
        window.setSize(250, 300);
        window.setLocationRelativeTo(null);
        window.setLayout(new FlowLayout());
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        window.add(new JLabel("Drawing View 1 Scale: "));
        var drawView1Box = new JTextField(2);
        drawView1Box.addActionListener(Main::handleDrawingScaleViewAction);
        window.add(drawView1Box);

        window.setVisible(true);
    }

    private static void handleDrawingScaleViewAction(ActionEvent event) {
        var userInput = getUserTextInput(event);

        if (userInput != null) {
            var drawingViewOut = "\"Drawing View 1 Scale\"= " + userInput;

            // write to app data
            writeToConfig(COVER_DRAWING_CONFIG_PATH.toString(), REBUILD_DAEMON_APP_DATA_PATH);

            // write to drawing config
            writeToConfig(drawingViewOut, COVER_DRAWING_CONFIG_PATH);

            rebuild(DaemonProgram.DRAWING_VIEW_SCALE);
        }
    }

    private static JButton configureCoverButton() {
        var button = new JButton("Configure Cover");
        button.addActionListener(e -> displayCoverShapeSelector());
        return button;
    }

    private static void displayCoverShapeSelector() {
        var window = new JFrame("Select Shape");
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setSize(200, 150);
        window.setLocationRelativeTo(null);
        window.setLayout(new FlowLayout());

        var circleSelectButton = new JButton("Circular");
        circleSelectButton.addActionListener(e -> displayCoverConfigWindow("Circular"));
        var squareSelectButton = new JButton("Square");
        squareSelectButton.addActionListener(e -> displayCoverConfigWindow("Square"));

        window.add(circleSelectButton);
        window.add(squareSelectButton);

        window.setVisible(true);
    }
// cover configurer
    private static void displayCoverConfigWindow(String shapeSelection) {
        var window = new JFrame("Cover Configurer");
        window.setLayout(new FlowLayout());
        window.setSize(400, 300);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        coverShapeSelection = shapeSelection;

        // if square write to assembly config "Square Center Mark"= 1 else = 0
        setAssemblySquareCenterMark();

        // set cover selection assembly config
        setCoverSelectionAssemblyConfig();

        // read cover config contents and set cover config variable table
        setCoverConfigVariableNameLineNumberTable(shapeSelection.contains("Square") ? SQUARE_COVER_CONFIG_PATH : COVER_CONFIG_PATH);

        // define total number of hole features to know the number of hole feature buttons to produce
        var holeFeatures = 0;
        for (String variable : coverConfigVariableNameLineNumberTable.keySet()) {
            if (!variable.matches("[^0-9]*") && variable.contains("Hole"))
                holeFeatures = Math.max(Integer.parseInt(variable.split(" ")[1]), holeFeatures);
        }

        // set user input parameters table based on cover config variable table
        setUserInputParametersTable();

        window.add(coverParamsButton("Base Cover", e -> displayBaseCoverParamsConfigWindow(
                "Base Cover Parameters", "Cover")));
        for (var i = 1; i <= holeFeatures; ++i) {
            int finalI = i;
            window.add(coverParamsButton("Hole " + i, e -> displayBaseCoverParamsConfigWindow(
                    "Hole " + finalI + " Parameters", "Hole " + finalI)
            ));
        }
        window.add(selectMaterialButton());

        window.add(coverAssemblyConfigureButton());

        window.setVisible(true);
    }

    private static void setAssemblySquareCenterMark() {
        var configLines = getLinesFromPath(COVER_ASSEMBLY_CONFIG_PATH);
        var identifier = "Square Center Mark";
        var index = 0;
        var writeNeeded = false;

        // set config line square center mark based on current setting and user shape selection
        for (String line : configLines) {
            if (line.contains(identifier) && !line.contains("IIF")) {
                var currentStateIsOne = line.contains("1");
                var selectionIsSquare = coverShapeSelection.contains("Square");
                var newLine = "";
                if (!currentStateIsOne && selectionIsSquare) {
                    newLine = line.replace("0", "1");
                    configLines[index] = newLine;
                    writeNeeded = true;
                } else if (currentStateIsOne && !selectionIsSquare) {
                    newLine = line.replace("1", "0");
                    configLines[index] = newLine;
                    writeNeeded = true;
                }
            }
            ++index;
        }

        if (writeNeeded) {
            var newText = generateWriteOutput(configLines);

            writeToConfig(newText, COVER_ASSEMBLY_CONFIG_PATH);

            writeToConfig(COVER_ASSEMBLY_CONFIG_PATH.toString(), REBUILD_DAEMON_APP_DATA_PATH);

            rebuild(DaemonProgram.BASIC_REBUILD);
        }
    }

    private static JButton coverAssemblyConfigureButton() {
        var button = new JButton("Cover Assembly Configurer");
        button.addActionListener(e -> displayCoverAssemblyConfigWindow());
        return button;
    }
// cover assembly feature configurer window
    private static void displayCoverAssemblyConfigWindow() {
        var window = new JFrame("General Assembly Feature Configurer");
        window.setSize(400, 300);
        window.setLayout(new FlowLayout());
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        window.add(handleButton());
        window.add(angleFrameButton());
        window.add(alumFlatBarButton());
        window.add(lockPlateButton());
        window.add(hingesButton());

        window.setVisible(true);
    }

    private static JButton hingesButton() {
        var button = new JButton("Hinges");
        button.addActionListener(e -> displayHingesConfigWindow());
        return button;
    }

    private static void displayHingesConfigWindow() {
        var window = new JFrame("Hinge Configurer");
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setSize(300, 300);
        window.setLocationRelativeTo(null);
        window.setLayout(new FlowLayout());

        window.add(new JLabel("Bolt-In Hinges Bool: "));
        var boolBox = new JTextField(1);
        boolBox.addActionListener(e -> assemblyBoolActionHandler(e, "\"Bolt-In Hinge Bool\"="));
        window.add(boolBox);

        window.add(new JLabel("Bolt-In Hinges Z Offset: "));
        var zOffset = new JTextField(2);
        zOffset.addActionListener(e -> assemblyDimensionActionHandler(e, "\"Bolt-In Hinge Z Offset\"=", "in"));
        window.add(zOffset);

        window.add(new JLabel("Bolt-In Hinge 1 X Offset: "));
        var xOneOffset = new JTextField(2);
        xOneOffset.addActionListener(e -> assemblyDimensionActionHandler(e, "\"Bolt-In Hinge 1 X Offset\"=", "in"));
        window.add(xOneOffset);

        window.add(new JLabel("Bolt-In Hinge 2 X Offset: "));
        var xTwoOffset = new JTextField(2);
        xTwoOffset.addActionListener(e -> assemblyDimensionActionHandler(e, "\"Bolt-In Hinge 2 X Offset\"=", "in"));
        window.add(xTwoOffset);

        window.setVisible(true);
    }

    private static JButton lockPlateButton() {
        var button = new JButton("Lock Plate");
        button.addActionListener(Main::displayLockPlateConfigWindow);
        return button;
    }

    private static void displayLockPlateConfigWindow(ActionEvent event) {
        var window = new JFrame("Lock Plate Configurer");
        window.setSize(300, 300);
        window.setLocationRelativeTo(null);
        window.setLayout(new FlowLayout());
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        window.add(new JLabel("Lock Plate Bool: "));
        var textBox = new JTextField(1);
        textBox.addActionListener(e -> assemblyBoolActionHandler(e, "\"Hatch Lock Plate Bool\"="));
        window.add(textBox);

        window.add(new JLabel("Lock Plate Z Offset: "));
        var offsetBox = new JTextField(2);
        offsetBox.addActionListener(e -> assemblyDimensionActionHandler(e, "\"Hatch Lock Plate Z Offset\"=", "in"));
        window.add(offsetBox);

        window.setVisible(true);
    }

    private static JButton alumFlatBarButton() {
        var button = new JButton("Alum Flat Bar");
        button.addActionListener(e -> displayAlumFlatConfigWindow());
        return button;
    }

    private static void displayAlumFlatConfigWindow() {
        var window = new JFrame("Assembly Alum Flat Bar Config");
        window.setSize(300, 300);
        window.setLayout(new FlowLayout());
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        window.add(new JLabel("Alum Flat 0deg Bool: "));
        var alumFlatBoolBox = new JTextField(1);
        alumFlatBoolBox.addActionListener(e -> assemblyBoolActionHandler(e, "\"Aluminum Flat Bar 0deg Bool\"="));
        window.add(alumFlatBoolBox);

        window.add(new JLabel("Alum Flat 90deg Bool: "));
        var alumFlat90BoolBox = new JTextField(1);
        alumFlat90BoolBox.addActionListener(e -> assemblyBoolActionHandler(e, "\"Aluminum Flat Bar 90deg Bool\"="));
        window.add(alumFlat90BoolBox);

        window.add(new JLabel("Alum Flat Bar X Length: "));
        var alumFlatLengthX = new JTextField(2);
        alumFlatLengthX.addActionListener(e -> assemblyDimensionActionHandler(e, "\"Length X\"=", "in", ALUM_FLAT_BAR_CONFIG_PATH));
        window.add(alumFlatLengthX);

        window.add(new JLabel("Alum Flat Bar Z Length: "));
        var alumFlatLengthZ = new JTextField(2);
        alumFlatLengthZ.addActionListener(e -> assemblyDimensionActionHandler(e, "\"Length Z\"=", "in", ALUM_FLAT_BAR_CONFIG_PATH));
        window.add(alumFlatLengthZ);

        window.add(new JLabel("Alum Flat Bar Placement Z Offset: "));
        var alumFlatBarPlacementZOffset = new JTextField(2);
        alumFlatBarPlacementZOffset.addActionListener(e -> assemblyDimensionActionHandler(e, "\"Aluminum Flat Bar 90deg Placement Z Offset\"=", "in", COVER_ASSEMBLY_CONFIG_PATH));
        window.add(alumFlatBarPlacementZOffset);

        window.setVisible(true);
    }

    private static JButton angleFrameButton() {
        var button = new JButton("2in Angle Frame");
        button.addActionListener(e -> displayAngleFrameConfigWindow());
        return button;
    }

    private static void displayAngleFrameConfigWindow() {
        var window = new JFrame("2in Angle Frame Configurer");
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setSize(300, 300);
        window.setLocationRelativeTo(null);
        window.setLayout(new FlowLayout());

        window.add(new JLabel("2in Angle Frame Bool: "));
        var boolBox = new JTextField(1);
        boolBox.addActionListener(Main::handleAngleFrameBoolAction);
        window.add(boolBox);

        window.add(new JLabel("2in Angle Frame X Length: "));
        var xLengthBox = new JTextField(2);
        xLengthBox.addActionListener(e -> handleAngleFrameLength(e, "X"));
        window.add(xLengthBox);

        window.add(new JLabel("2in Angle Frame Z Length: "));
        var zLengthBox = new JTextField(2);
        zLengthBox.addActionListener(e -> handleAngleFrameLength(e, "Z"));
        window.add(zLengthBox);

        window.add(new JLabel("2in Angle Frame ID Cutaway Bool: "));
        var cutawayBoolInputBox = new JTextField(1);
        cutawayBoolInputBox.addActionListener(e -> assemblyBoolActionHandler(e, "Angle Frame ID Cutaway Bool"));
        window.add(cutawayBoolInputBox);

        window.add(new JLabel("Angle Frame ID Cutaway Diameter: "));
        var cutawayDiameterInputBox = new JTextField(2);
        cutawayDiameterInputBox.addActionListener(e -> assemblyDimensionActionHandler(e, "\"Angle Frame ID Cutaway Diameter\"=", "in"));
        window.add(cutawayDiameterInputBox);

        window.add(new JLabel("Angle Frame Placement Z Offset: "));
        var angleFramePlacementZOffset = new JTextField(2);
        angleFramePlacementZOffset.addActionListener(e -> assemblyDimensionActionHandler(e, "\"Angle Frame Placement Z Offset\"=", "in"));
        window.add(angleFramePlacementZOffset);

        window.setVisible(true);
    }

    // refactor to Assembly Util API - general assembly dimension handler
    private static void assemblyDimensionActionHandler(ActionEvent event, String line, String units, Path configPath) {
        // the two booleans are going to point to calls to external methods
        var userInput = getUserTextInput(event);
        if (userInput != null) {
            var assemblyConfigLines = getLinesFromPath(configPath);
            var dimensionLineNumberTable = getSingleLineNumberTable(assemblyConfigLines, line);

            for (int lineNumber : dimensionLineNumberTable.keySet()) {
                var newLine = getNewLineUserInput(assemblyConfigLines[lineNumber], userInput, units);

                assemblyConfigLines[lineNumber] = newLine;
            }

            // write app data
            writeToConfig(configPath.toString(), REBUILD_DAEMON_APP_DATA_PATH);

            // generate and write config.txt data
            var newText = generateWriteOutput(assemblyConfigLines);
            writeToConfig(newText, configPath);

            // call rebuild daemon
            rebuild(DaemonProgram.ASSEMBLY_GENERAL);
        }
    }

    // refactor to Assembly Util API - general assembly dimension handler
    private static void assemblyDimensionActionHandler(ActionEvent event, String line, String units) {
        // the two booleans are going to point to calls to external methods
        var userInput = getUserTextInput(event);
        if (userInput != null) {
            var assemblyConfigLines = getLinesFromPath(COVER_ASSEMBLY_CONFIG_PATH);
            var dimensionLineNumberTable = getSingleLineNumberTable(assemblyConfigLines, line);

            for (int lineNumber : dimensionLineNumberTable.keySet()) {
                var newLine = getNewLineUserInput(assemblyConfigLines[lineNumber], userInput, units);

                assemblyConfigLines[lineNumber] = newLine;
            }

            // write app data
            writeToConfig(COVER_ASSEMBLY_CONFIG_PATH.toString(), REBUILD_DAEMON_APP_DATA_PATH);

            // generate and write config.txt data
            var newText = generateWriteOutput(assemblyConfigLines);
            writeToConfig(newText, COVER_ASSEMBLY_CONFIG_PATH);

            // call rebuild daemon
            rebuild(DaemonProgram.ASSEMBLY_GENERAL);
        }
    }

    // refactor to Assembly Util API - general assembly bool handler
    private static void assemblyBoolActionHandler(ActionEvent event, String line) {
        var userInput = getUserTextInput(event);
        if (userInput != null) {
            var assemblyConfigLines = getLinesFromPath(COVER_ASSEMBLY_CONFIG_PATH);
            var boolLineNumberTable = getSingleLineNumberTable(assemblyConfigLines, line);

            for (int lineNumber : boolLineNumberTable.keySet()) {
                var newLine = getNewLineUserInput(assemblyConfigLines[lineNumber], userInput, "");

                assemblyConfigLines[lineNumber] = newLine;
            }

            // write app data
            writeToConfig(COVER_ASSEMBLY_CONFIG_PATH.toString(), REBUILD_DAEMON_APP_DATA_PATH);

            // generate and write new lines
            var newText = generateWriteOutput(assemblyConfigLines);
            writeToConfig(newText, COVER_ASSEMBLY_CONFIG_PATH);

            // call rebuild daemon
            rebuild(DaemonProgram.ASSEMBLY_GENERAL);
        }
    }

    // refactor to Assembly Util API - general assembly bool handler
    private static void assemblyBoolActionHandler(ActionEvent event, String line, Path configPath) {
        var userInput = getUserTextInput(event);
        if (userInput != null) {
            var assemblyConfigLines = getLinesFromPath(configPath);
            var boolLineNumberTable = getSingleLineNumberTable(assemblyConfigLines, line);

            for (int lineNumber : boolLineNumberTable.keySet()) {
                var newLine = getNewLineUserInput(assemblyConfigLines[lineNumber], userInput, "");

                assemblyConfigLines[lineNumber] = newLine;
            }

            // write app data
            writeToConfig(configPath.toString(), REBUILD_DAEMON_APP_DATA_PATH);

            // generate and write new lines
            var newText = generateWriteOutput(assemblyConfigLines);
            writeToConfig(newText, configPath);

            // call rebuild daemon
            rebuild(DaemonProgram.ASSEMBLY_GENERAL);
        }
    }

    private static void handleAngleFrameLength(ActionEvent event, String XZ) {
        var userInput = getUserTextInput(event);

        // if user input is not null
        if (userInput != null) {
            var partConfigLines = getLinesFromPath(ANGLE_FRAME_CONFIG_PATH);

            var lineIdentifier = "\"" + XZ + " Clear Access\"=";

            var lineNumberTable = getSingleLineNumberTable(partConfigLines, lineIdentifier);
            outputLines(lineNumberTable);

            for (int lineNumber : lineNumberTable.keySet()) {
                var newLine = lineNumberTable.get(lineNumber).split("=")[0].trim() +
                        "= " + userInput + "in";

                partConfigLines[lineNumber] = newLine;
            }

            // write app data
            writeToConfig(getCoverConfigPath().toString(), REBUILD_DAEMON_APP_DATA_PATH);

            // write to assembly config
            var newText = generateWriteOutput(partConfigLines);
            writeToConfig(newText, ANGLE_FRAME_CONFIG_PATH);

            // send rebuild command
            rebuild(DaemonProgram.ASSEMBLY_GENERAL);
        }
    }

    // refactor to Util API - general use
    private static String getNewLineUserInput(String original, String userInput, String units) {
        return original.split("=")[0].trim() + "= " + userInput + units;
    }

    // refactor to Util API - general use
    private static String generateWriteOutput(String... lines) {
        var builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line);
            builder.append("\n");
        }
        return builder.toString();
    }

    // refactor to Util API - general use
    private static HashMap<Integer, String> getSingleLineNumberTable(String[] lines, String identifier) {
        var map = new HashMap<Integer, String>();
        var index = 0;

        for (String line : lines) {
            if (line.contains(identifier) && !line.contains("IIF")) {
                map.put(index, line);
                break;
            }
            ++index;
        }

        return map;
    }

    // refactor to Util API - general use
    private static void outputLines(String... lines) {
        for (String line : lines) {
            System.out.println(line);
        }
    }

    // refactor to Util API - general use
    private static void outputLines(String line, int integer) {
        System.out.println(line);
        System.out.println(integer + "");
    }

    private static <T> void outputLines(T line) {
        System.out.println(line);
    }

    // refactor to Util API - general use
    private static void outputLines(HashMap<Integer, String> map) {
        for (String line : map.values()) {
            System.out.println(line);
        }
    }

    // refactor to Util API - general use
    private static void outputLines(HashMap<String, Boolean> map, boolean isStringInt) {
        for (String line : map.keySet()) {
            System.out.println(line);
        }
    }

    // refactor to Util API - general use
    private static String[] getLinesFromPath(Path path) {
        return FilesUtil.read(path).split("\n");
    }

    // refactor to Util API - general use
    private static String getUserTextInput(ActionEvent event) {
        return event.getActionCommand().isEmpty() ? null : event.getActionCommand();
    }

    private static void handleAngleFrameBoolAction(ActionEvent event) {
        var userInput = event.getActionCommand().isEmpty() ? null : event.getActionCommand();

        // check if user input is not null - if null do nothing
        if (userInput != null) {
            var coverAssemblyConfigLines = FilesUtil.read(COVER_ASSEMBLY_CONFIG_PATH).split("\n");

            // get angle frame bool line number and line
            // get current bool
            var isCurrentlyActive = false;
            var variableLineNumber = 0;
            for (String line : coverAssemblyConfigLines) {
                if (line.contains("2in Angle Frame Bool") &&
                        !line.contains("IIF")) {
                    isCurrentlyActive = coverAssemblyConfigLines[variableLineNumber].split("=")[1].contains("1");
                    break;
                }
                ++variableLineNumber;
            }

            // get userInput bool
            var userInputIsTrue = userInput.contains("1");

            // if opposing signs write updated bool to assembly config.txt and rebuild
            if (userInputIsTrue != isCurrentlyActive) {
                var line = coverAssemblyConfigLines[variableLineNumber];
                var newLine = line.split("=")[0].trim() + "= " + (isCurrentlyActive ? "0" : "1");

                coverAssemblyConfigLines[variableLineNumber] = newLine;

                // generate builder
                var builder = new StringBuilder();
                for (String assemblyLine : coverAssemblyConfigLines) {
                    builder.append(assemblyLine);
                    builder.append("\n");
                }

                // write to assembly config
                writeToConfig(builder.toString(), COVER_ASSEMBLY_CONFIG_PATH);

                // rebuild
                rebuild(DaemonProgram.ASSEMBLY_GENERAL);
            }
        }
    }

    private static JButton handleButton() {
        var button = new JButton("Cover Assembly Handle Config");
        button.addActionListener(e -> displayAssemblyHandleConfigWindow());
        return button;
    }

    private static void displayAssemblyHandleConfigWindow() {
        var window = new JFrame("Cover Assembly Handle Configurer");
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setSize(275, 250);
        window.setLocationRelativeTo(null);
        window.setLayout(new FlowLayout());

        // disable config assembly handle if no part config.txt handle is active
        var coverHasHandle = false;
        var partConfigLines = FilesUtil.read(getCoverConfigPath()).split("\n");
        for (String line : partConfigLines) {
            if (line.contains("Handle") && line.contains("Bool") &&
            !line.contains("IIF")) {
                if (!coverHasHandle) {
                    coverHasHandle = line.split("=")[1].contains("1");
                }
            }
        }

        var handleGTBoxLabel = coverHasHandle ? "GT Box 0deg Bool: " : "No Active Handle Found ";
        var label = new JLabel(handleGTBoxLabel);
        window.add(label);

        if (coverHasHandle) {
            var textBox = new JTextField(1);
            textBox.addActionListener(e -> handleBoolAction(e, "GT Box 0deg"));
            window.add(textBox);

            var GTBox90degLabel = new JLabel("GT Box 90deg Bool: ");
            window.add(GTBox90degLabel);
            var GTBox90degBox = new JTextField(1);
            GTBox90degBox.addActionListener(e -> handleBoolAction(e, "GT Box 90deg"));
            window.add(GTBox90degBox);

            var handleBoolLabel = new JLabel("Handle 0deg Bool: ");
            window.add(handleBoolLabel);
            var handleBoolTextBox = new JTextField(1);
            handleBoolTextBox.addActionListener(e -> handleBoolAction(e, "Handle 0deg"));
            window.add(handleBoolTextBox);

            var handle90degBoolLabel = new JLabel("Handle 90deg Bool: ");
            window.add(handle90degBoolLabel);
            var handle90debBox = new JTextField(1);
            handle90debBox.addActionListener(e -> handleBoolAction(e, "Handle 90deg"));
            window.add(handle90debBox);
        }

        window.setVisible(true);
    }

    // This handles assembly features that are bool only input and derive X/Z location from
    // part config and app data offset table
    private static void handleBoolAction(ActionEvent event, String assemblyFeature){
        var userInput = getUserTextInput(event);
        if (userInput != null) {
            var partConfigLines = FilesUtil.read(getCoverConfigPath()).split("\n");
            var assemblyConfigLines = FilesUtil.read(COVER_ASSEMBLY_CONFIG_PATH).split("\n");

            // populate a part config.txt line number - variable list table
            var partConfigLineNumberVariableListTable = new HashMap<Integer, String>();
            var index = 0;
            for (String line : partConfigLines) {
                if (line.contains("Handle") && !line.contains("IIF") &&
                        !line.contains("@")) {
                    partConfigLineNumberVariableListTable.put(index, line);
                }
                ++index;
            }

            // populate an assembly config.txt line number - variable table list
            var assemblyConfigLineNumberVariableListTable = new HashMap<Integer, String>();
            index = 0;
            for (String line : assemblyConfigLines) {
                if (line.contains(assemblyFeature) && !line.contains("@") &&
                        !line.contains("IIF")) {
                    assemblyConfigLineNumberVariableListTable.put(index, line);
                }
                ++index;
            }


            // makes sure part config and assembly config handle booleans match - default if user input is null
            for (int partConfigLineNumber : partConfigLineNumberVariableListTable.keySet()) {
                var partConfigLine = partConfigLineNumberVariableListTable.get(partConfigLineNumber);

                if (partConfigLine.contains("Bool")) {
                    var partIs90deg = partConfigLine.contains("9");
                    var partTypeIndex = partConfigLine.split("=")[0].indexOf(partConfigLine.contains("9") ? '9' : '0');
                    var partType = partConfigLine.substring(partTypeIndex, partIs90deg ? partTypeIndex + 5 : partTypeIndex + 4);
                    var partHandleIsActive = partConfigLines[partConfigLineNumber].split("=")[1].contains("1");

                    for (int assemblyConfigLineNumber : assemblyConfigLineNumberVariableListTable.keySet()) {
                        var assemblyConfigLine = assemblyConfigLineNumberVariableListTable.get(assemblyConfigLineNumber);

                        if (assemblyConfigLine.contains("Bool")) {
                            var assemblyType = getDimensionDegreeType(assemblyConfigLine);
                            var partAssemblyTypeIsSame = assemblyType.compareTo(partType) == 0;
                            if (partAssemblyTypeIsSame) {
                                var assemblyHandleIsActive = assemblyConfigLines[assemblyConfigLineNumber].split("=")[1].contains("1");

                                // check if handle booleans match
                                var partAssemblyBoolMismatch = !(partHandleIsActive && assemblyHandleIsActive ||
                                        !assemblyHandleIsActive && !partHandleIsActive);

                                // if mismatched set assembly config line to match part config line
                                if (partAssemblyBoolMismatch) {
                                    var newLine = getNewLineUserInput(assemblyConfigLine, userInput, "");

                                    assemblyConfigLines[assemblyConfigLineNumber] = newLine;
                                }
                            }
                        }
                    }
                }
            }

            // make sure X/Z offsets between part/assembly config match
            // populate part config handle offsets
            var partHandleVariableBuilder = new StringBuilder();
            for (String line : partConfigLines) {
                if (line.contains("Handle") && line.contains("Offset") &&
                        !line.contains("@")) {
                    partHandleVariableBuilder.append(line);
                    partHandleVariableBuilder.append("!");
                }
            }
            var partHandleVariableArray = partHandleVariableBuilder.toString().split("!");

            // populate assembly config handle offsets
            var assemblyLineNumberOffsetTable = new HashMap<Integer, String>();
            index = 0;
            for (String line : assemblyConfigLines) {
                if (line.contains(assemblyFeature) && line.contains("Offset") &&
                        !line.contains("IIF") && !line.contains("@")) {
                    assemblyLineNumberOffsetTable.put(index, line);
                }
                ++index;
            }

            // populate relevant offset table list
            var offsetTableStringList = new StringBuilder();
            var offsetTableLines = FilesUtil.read(APP_DATA_HANDLE_OFFSET_TABLE).split("\n");
            for (String line : offsetTableLines) {
                if (line.contains(assemblyFeature)) {
                    offsetTableStringList.append(line);
                    offsetTableStringList.append("!");
                }
            }
            var offsetTableArray = offsetTableStringList.toString().split("!");

            // compare offsets and generate corresponding assembly config lines
            for (int lineNumber : assemblyLineNumberOffsetTable.keySet()) {
                var assemblyLine = assemblyConfigLines[lineNumber];
                var assemblyType = getDimensionDegreeType(assemblyLine);
                var assemblyIsX = isDimensionX(assemblyLine);

                for (String partLine : partHandleVariableArray) {

                    var partType = getDimensionDegreeType(partLine);
                    var partIsX = isDimensionX(partLine);
                    var assemblyPartTypeIsSame = assemblyType.compareTo(partType) == 0;

                    if (assemblyPartTypeIsSame &&
                            (assemblyIsX && partIsX || !assemblyIsX && !partIsX)) {

                        for (String offset : offsetTableArray) {
                            var offsetIsX = isDimensionX(offset);
                            var offsetType = getDimensionDegreeType(offset);

                            if ((offsetIsX && assemblyIsX || !offsetIsX && !assemblyIsX) &&
                                    offsetType.compareTo(partType) == 0) {
                                var partValue = Double.parseDouble(partLine.split("=")[1].replace("in", "").trim());
                                var offsetValueAsInt = Double.parseDouble(offset.split("=")[1].trim());
                                partValue -= offsetValueAsInt;
                                var newLine = assemblyLine.split("=")[0].trim() + "= " + partValue + "in";

                                assemblyConfigLines[lineNumber] = newLine;
                            }
                        }
                    }
                }
            }

            // if user input is not null - check if input is "0" or "1"
            // if "0" set all assembly config lines handle bool to zero
            // if "1" do nothing
            if (userInput.contains("0")) {
                // set all GT Box Bool to 0
                for (int assemblyConfigLineNumber : assemblyConfigLineNumberVariableListTable.keySet()) {
                    var assemblyConfigLine = assemblyConfigLineNumberVariableListTable.get(assemblyConfigLineNumber);
                    if (assemblyConfigLine.contains("Bool")) {
                        var newLine = getNewLineUserInput(assemblyConfigLine, userInput, "");
                        assemblyConfigLines[assemblyConfigLineNumber] = newLine;
                    }
                }
            }

            // generate assembly config output
            var builder = new StringBuilder();
            for (String line : assemblyConfigLines) {
                builder.append(line);
                builder.append("\n");
            }

            // write to assembly config
            writeToConfig(builder.toString(), COVER_ASSEMBLY_CONFIG_PATH);

            // generate app data
            // look for handle part config negation state and compare to
            // assembly handle negation state - if diff write handle offset
            // Distance<#> to app data - USER CONFIRM - if app data contains flip request

            // generate part config line number negation table
            var partConfigLineNumberNegationTable = new HashMap<Integer, String>();
            index = 0;
            for (String line : partConfigLines) {
                if (line.contains("Negative") && line.contains("Handle")) {
                    partConfigLineNumberNegationTable.put(index, line);
                }
                ++index;
            }

            // generate assembly config line number negation table
            var assemblyConfigLineNumberNegationTable = new HashMap<Integer, String>();
            index = 0;
            for (String line : assemblyConfigLines) {
                if (line.contains("Negative") && line.contains(assemblyFeature)) {
                    assemblyConfigLineNumberNegationTable.put(index, line);
                }
                ++index;
            }

            // compare negation states and populate app data with appropriate
            // Distance<#> on dif
            // populate assembly config negation line string list for mates to flip
            var assemblyConfigNegationLineToFlipStringList = new StringBuilder();
            for (int partConfigLineNumber : partConfigLineNumberNegationTable.keySet()) {
                var partConfigLine = partConfigLineNumberNegationTable.get(partConfigLineNumber);
                var partLineIsX = isDimensionX(partConfigLine);
                var partLineType = getDimensionDegreeType(partConfigLine);

                for (int assemblyLineNumber : assemblyConfigLineNumberNegationTable.keySet()) {
                    var assemblyConfigLine = assemblyConfigLineNumberNegationTable.get(assemblyLineNumber);
                    var assemblyLineIsX = isDimensionX(assemblyConfigLine);
                    var assemblyLineType = getDimensionDegreeType(assemblyConfigLine);

                    // for matching X/Z and matching part type (0deg or 90deg)
                    if (partLineIsX && assemblyLineIsX ||
                            !partLineIsX && !assemblyLineIsX) {
                        if (assemblyLineType.compareTo(partLineType) == 0) {
                            var partLineIsNegative = partConfigLine.split("=")[1].contains("1");
                            var assemblyLineIsNegative = assemblyConfigLine.split("=")[1].contains("1");
                            var negationIsDif = !(partLineIsNegative && assemblyLineIsNegative ||
                                    !partLineIsNegative && !assemblyLineIsNegative);
                            if (negationIsDif) {
                                assemblyConfigNegationLineToFlipStringList.append(assemblyConfigLine);
                                assemblyConfigNegationLineToFlipStringList.append("!");
                            }
                        }
                    }
                }
            }

            // generate app data initial line
            var appData = new StringBuilder();
            appData.append(COVER_ASSEMBLY_CONFIG_PATH);
            appData.append("\n");

            // generate and write app data
            // if mates will be flipped confirm changes okay first
            if (assemblyConfigNegationLineToFlipStringList.length() > 0) {
                var assemblyConfigNegationFlipArray = assemblyConfigNegationLineToFlipStringList.toString().split("!");

                // first ask for user confirmation and display the list of variables that will be flipped
                var window = new JFrame("Confirm");
                window.setSize(225, 300);
                window.setLayout(new FlowLayout());
                window.setLocationRelativeTo(null);
                window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                var label = new JLabel("The Following Mates Will Be Flipped:");
                window.add(label);
                var labels = new JLabel[assemblyConfigNegationFlipArray.length];

                var labelIndex = 0;
                for (String dimension : assemblyConfigNegationFlipArray) {
                    labels[labelIndex] = new JLabel(dimension);
                    window.add(labels[labelIndex++]);
                }

                // get user confirm/cancel
                var confirmButton = new JButton("Confirm");
                confirmButton.addActionListener(e1 -> {
                    // generate dimensions to add to app data
                    for (String dimension : assemblyConfigNegationFlipArray) {
                        for (String line : assemblyConfigLines) {
                            var assemblyVariable = dimension.split("=")[0].split("Negative")[0]
                                    .replace("\"", "").trim();
                            if (line.contains(assemblyVariable) &&
                                    line.contains("@")) {
                                var distance = line.split("=")[0].split("@")[1].replace("\"", "").trim();
                                appData.append(distance);
                                appData.append("\n");
                            }
                        }
                    }

                    // write app data
                    writeToConfig(appData.toString(), REBUILD_DAEMON_APP_DATA_PATH);
                    rebuild(DaemonProgram.ASSEMBLY_GENERAL);
                    window.dispose();
                });
                window.add(confirmButton);

                var cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(e1 -> {
                    writeToConfig(appData.toString(), REBUILD_DAEMON_APP_DATA_PATH);
                    rebuild(DaemonProgram.ASSEMBLY_GENERAL);
                    window.dispose();
                });
                window.add(cancelButton);

                window.setVisible(true);
            } else {
                writeToConfig(appData.toString(), REBUILD_DAEMON_APP_DATA_PATH);

                rebuild(DaemonProgram.ASSEMBLY_GENERAL);
            }
        }
    }

    // refactor to Util API - general use - gets 0deg/90deg from arbitrary line containing '='
    private static String getDimensionDegreeType(String line) {
        var startIndex = 0;
        var lineSplit = line.split("=")[0];
        var type = "";
        var lineContainsZero = lineSplit.contains("0");
        if (lineSplit.contains("9")) {
            startIndex = firstIndex(line, '9');
        } else if (lineContainsZero){
            startIndex = firstIndex(line, '0');
        }

        var endIndex = line.contains("9") ? startIndex + 5 : startIndex + 4;

        try {
            type = line.substring(startIndex, endIndex);
        } catch (StringIndexOutOfBoundsException exception) {
            System.out.println(line);
        }

        return type.trim();
    }

    private static int firstIndex(String line, char indexCharacter) {
        var index = 0;
        var charArray = line.toCharArray();
        var stop = false;
        while (!stop) {
            if (charArray[index] == indexCharacter){
                stop = true;
            } else {
                ++index;
            }
        }
        return index;
    }

    // refactor to Util API - general use - gets X/Z dimension type
    private static boolean isDimensionX(String line) {
        return line.contains("X");
    }

    // sets cover shape selection
    // references: blob.coverConfig.txt; L1 - blob.cover.SLDASM
    private static void setCoverSelectionAssemblyConfig() {
        // get config lines
        var configLines = FilesUtil.read(COVER_SHAPE_ASSEMBLY_CONFIG_PATH).split("\n");

        // set new value from user cover shape selection
        for (var i = 0; i < configLines.length; ++i) {
            if (configLines[i].contains("Configuration") &&
            !configLines[i].contains("IIF")) {
                configLines[i] = "Configuration = " + (coverShapeSelection.contains("Square") ? "2" : "1");
            }
        }

        // write new value to cover shape assembly config
        var builder = new StringBuilder();
        for (String line : configLines) {
            builder.append(line);
            builder.append("\n");
        }
        writeToConfig(builder.toString(), COVER_SHAPE_ASSEMBLY_CONFIG_PATH);

        // set rebuild.txt app data to cover shape assembly config path
        writeToConfig(COVER_SHAPE_ASSEMBLY_CONFIG_PATH.toString(), REBUILD_DAEMON_APP_DATA_PATH);

        // call assembly rebuild daemon
        rebuild(DaemonProgram.ASSEMBLY_REBUILD);
    }

    private static JButton selectMaterialButton() {
        var button = new JButton("Material");
        button.addActionListener(e -> displaySelectMaterialWindow());
        return button;
    }

    private static void displaySelectMaterialWindow() {
        var window = new JFrame("Select Material");
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setSize(300, 300);
        window.setLayout(new FlowLayout());
        window.setLocationRelativeTo(null);

        window.add(materialConfigButton("ASTM A36 Steel"));
        window.add(materialConfigButton("6061 Alloy"));

        window.setVisible(true);
    }

    private static JButton materialConfigButton(String material) {
        var button = new JButton(material);
        button.addActionListener(e -> writeMaterialConfig(material));
        return button;
    }

    private static void writeMaterialConfig(String material) {
        var configLines = FilesUtil.read(coverShapeSelection.contains("Square") ? SQUARE_COVER_CONFIG_PATH : COVER_CONFIG_PATH).split("\n");
        for (var i = 0; i < configLines.length; ++i) {
            if (configLines[i].split("=")[0].contains("Material")) {
                configLines[i] = configLines[i].replace(configLines[i].split("=")[1], " " + MATERIAL_CONFIG_TABLE.get(material));
            }
        }

        var builder = new StringBuilder();
        for (String line : configLines) {
            builder.append(line);
            builder.append("\n");
        }

        // write material config to selected config.txt file
        writeToConfig(builder.toString(), getCoverConfigPath());

        // write selected config.txt path to rebuild.txt app data
        writeToConfig(getCoverConfigPath().toString(), REBUILD_DAEMON_APP_DATA_PATH);

        // call rebuild for AutoMaterialConfig.appref-ms
        rebuild(DaemonProgram.MATERIAL_CONFIG);
    }

    private static JButton coverParamsButton(String label, ActionListener actionListener) {
        var button = new JButton(label);
        button.addActionListener(actionListener);
        return button;
    }

    private static void displayBaseCoverParamsConfigWindow(String windowTitle, String variableName) {
        var window = new JFrame(windowTitle);
        window.setSize(300, 900);
        window.setLocationRelativeTo(null);
        window.setLayout(new FlowLayout());
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        window.add(new JLabel("Instructions:"));
        window.add(new JLabel(" After each input press Enter to 'set' the value."));
        window.add(new JLabel(" Click the Build button to generate the model."));

        for (String variable : coverConfigVariableNameLineNumberTable.keySet()) {
            if (variable.contains(variableName)) {
                window.add(new JLabel(variable + ": "));
                var textInput = new JTextField( 4);
                textInput.addActionListener(e -> coverConfigVariableUserInputTable.put(variable, e.getActionCommand()));
                window.add(textInput);
            }
        }

        if (!windowTitle.contains("Base Cover"))
            window.add(holeAssemblyConfigButton(variableName));

        window.add(baseCoverParamsBuildButton(variableName));

        window.setVisible(true);
    }

    private static JButton holeAssemblyConfigButton(String variableName) {
        var button = new JButton("Assembly Config");
        button.addActionListener(e -> holeAssemblyConfigWindow(variableName));
        return button;
    }

    private static void holeAssemblyConfigWindow(String variableName) {
        var window = new JFrame(variableName + " Assembly Configuration");
        window.setSize(300, 400);
        window.setLocationRelativeTo(null);
        window.setLayout(new FlowLayout());
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        var radioButtons = holeAssemblyConfigRadios(variableName);
        var buttonGroup = new ButtonGroup();

        for (JRadioButton radioButton : radioButtons) {
            buttonGroup.add(radioButton);
        }

        for (JRadioButton radioButton : radioButtons) {
            window.add(radioButton);
        }

        window.add(inspectionPlateConfigButton(variableName));

        window.add(confirmHoleAssemblyConfigButton(variableName, buttonGroup));

        window.setVisible(true);
    }

    private static JButton inspectionPlateConfigButton(String variableName) {
        var button = new JButton("Inspection Plate Config");
        button.addActionListener(e -> displayInspectionPlateConfigWindow(variableName));
        return button;
    }

    private static void displayInspectionPlateConfigWindow(String variableName) {
        var window = new JFrame("Inspection Plate Configurer");
        window.setSize(300, 300);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setLayout(new FlowLayout());
// gets X/Z offsets from current hole - does this at user clicking 'confirm' in the prior assembly config screen
        window.add(new JLabel("IN04A-25 4 Grips 45deg Bool: "));
        var in0445degBoolBox = new JTextField(1);
        in0445degBoolBox.addActionListener(e -> assemblyBoolActionHandler(e, "\"IN04A-25 4 Grips 45deg Bool\"=", INSPECTION_PLATE_CONFIG_PATH));
        window.add(in0445degBoolBox);

        window.setVisible(true);
    }

    private static JButton confirmHoleAssemblyConfigButton(String variableName, ButtonGroup buttonGroup) {
        var button = new JButton("Confirm");
        button.addActionListener(e -> {

            // read blob.L2_cover.txt file and split into lines
            var coverAssemblyConfigLines = FilesUtil.read(COVER_ASSEMBLY_CONFIG_PATH).split("\n");

            // get user selection - forces default "none" if nothing is selected on confirm
            var userSelection = "";
            try {
                userSelection = buttonGroup.getSelection().getActionCommand();
            } catch (NullPointerException exception) {
                userSelection = "none";
            }

            // populate a line number - feature bool map
            var lineNumberFeatureBoolTable = new HashMap<Integer, String>();
            var index = 0;
            for (String line : coverAssemblyConfigLines) {
                if (line.contains("Bool") && !line.contains("IIF")) {
                    lineNumberFeatureBoolTable.put(index, line);
                }
                ++index;
            }

            // set the bool state of the hole (variable name) and feature selected
            // for the assembly config.txt file - writes later
            for (int lineNumber : lineNumberFeatureBoolTable.keySet()) {
                var line = lineNumberFeatureBoolTable.get(lineNumber);
                // check if line in assembly config.txt contains user selected hole number
                if (line.contains(variableName) && line.contains("Bool") &&
                !line.contains("IIF")) {
                    if (line.contains(userSelection)) {
                        var featureSelectedVariable = line.split("=")[1].trim();
                        var isFeatureCurrentlySelected = featureSelectedVariable.contains("1");

                        // if feature isn't selected set it to selected
                        if (!isFeatureCurrentlySelected) {
                            var newLine = coverAssemblyConfigLines[lineNumber].split("=")[0].trim() + "= 1";
                            coverAssemblyConfigLines[lineNumber] = newLine;
                        }
                        // set rest to 0
                    } else {
                        var newLine = coverAssemblyConfigLines[lineNumber].split("=")[0].trim() + "= 0";
                        coverAssemblyConfigLines[lineNumber] = newLine;
                    }
                }
            }

            // read part config.txt for the variable name (hole number) and X/Z negative state - read only
            // only care about the current variable name (hole number) X/Z
            var partConfigXZNegationStateTable = new HashMap<String, Boolean>();
            var partConfigLines = FilesUtil.read(getCoverConfigPath()).split("\n");
            for (String line : partConfigLines) {
                if (line.contains("Negative") && line.contains(variableName)) {
                    var isNegative = line.split("=")[1].contains("1");

                    partConfigXZNegationStateTable.put(line, isNegative);
                }
            }

            // populate assembly config X/Z negation information for variable name (hole number) - read only
            var assemblyConfigXZNegationTable = new HashMap<String, Boolean>();
            for (String line : coverAssemblyConfigLines) {
                if (line.contains("Negative") && line.contains("Hole")) {
                    var isNegative = line.split("=")[1].contains("1");

                    assemblyConfigXZNegationTable.put(line, isNegative);
                }
            }

            // if part/assembly negation do not match make that hole and all its dimensions
            // added to app data to flip
            var rebuildAppData = new StringBuilder();
            rebuildAppData.append(COVER_ASSEMBLY_CONFIG_PATH);
            rebuildAppData.append("\n");
            for (String assemblyDimension : assemblyConfigXZNegationTable.keySet()) {
                for (String partDimension : partConfigXZNegationStateTable.keySet()) {
                    var assemblyHoleNumber = getHoleNumber(assemblyDimension);
                    var partHoleNumber = getHoleNumber(partDimension);
                    if (assemblyHoleNumber == partHoleNumber && (assemblyDimension.contains("X") && partDimension.contains("X") ||
                            assemblyDimension.contains("Z") && partDimension.contains("Z"))) {
                        var assemblyIsNegative = assemblyDimension.split("=")[1].contains("1");
                        var partIsNegative = partDimension.split("=")[1].contains("1");
                        if (!(assemblyIsNegative && partIsNegative ||
                                !assemblyIsNegative && !partIsNegative)) {
                            var XorZ = assemblyDimension.contains("X") ? "X" : "Z";
                            for (String line : coverAssemblyConfigLines) {
                                if (line.contains("@") && line.contains(XorZ) &&
                                        line.contains(variableName)) {
                                    var dimension = line.split("@")[1].split("=")[0].replace("\"", "").trim();
                                    rebuildAppData.append(dimension);
                                    rebuildAppData.append("\n");
                                }
                            }
                        }
                    }
                }
            }

            // get part config X Z offsets for the hole number (variable name)
            var partConfigOffsetTable = new HashMap<String, String>();
            for (String line : partConfigLines) {
                if (line.contains(variableName) && line.contains("CA")
                && line.contains("Offset") && line.contains("in")) {
                    if (line.contains("X")) {
                        var dimension = line.split("=")[1];
                        partConfigOffsetTable.put("X Offset", dimension);
                    } else if (line.contains("Z")) {
                        var dimension = line.split("=")[1];
                        partConfigOffsetTable.put("Z Offset", dimension);
                    }
                }
            }

            // get line numbers for the hole number X and Z offset
            var assemblyConfigXZOffsetLineNumberStringList = new StringBuilder();
            index = 0;
            for (String line : coverAssemblyConfigLines) {
                if (line.contains(variableName) && line.contains("Offset") &&
                line.contains("in")) {
                    assemblyConfigXZOffsetLineNumberStringList.append(index);
                    assemblyConfigXZOffsetLineNumberStringList.append(" ");
                }
                ++index;
            }
            var assemblyConfigOffsetLineNumberArray = assemblyConfigXZOffsetLineNumberStringList.toString().split(" ");

            // set cover assembly lines for hole number offset to part config dimension
            for (String lineNumber : assemblyConfigOffsetLineNumberArray) {
                var asInt = Integer.parseInt(lineNumber);
                for (String offset : partConfigOffsetTable.keySet()) {
                    var line = coverAssemblyConfigLines[asInt];
                    if (line.contains(offset)) {
                        var newOffset = partConfigOffsetTable.get(offset);
                        var lineSegments = line.split("=");
                        var newLine = lineSegments[0].trim() + "=" + newOffset;
                        coverAssemblyConfigLines[asInt] = newLine;
                    }
                }
            }
            // display rebuild app data and all hole part assembly negations
            if (ASSEMBLY_MATE_CALIBRATION) {
                System.out.println("\n\n\n");
                for (String line : partConfigLines) {
                    if (line.contains("Negative")) {
                        System.out.println(line);
                    }
                }
                for (String line : coverAssemblyConfigLines) {
                    if (line.contains("Negative")) {
                        System.out.println(line);
                    }
                }

                System.out.println(rebuildAppData);
            }

            var builder = new StringBuilder();
            for (String line : coverAssemblyConfigLines) {
                builder.append(line);
                builder.append("\n");
            }
            // write app data
            // ask to do so first if damn thing is going to flip mates
            if (rebuildAppData.toString().contains("Distance")) {
                var window = new JFrame("Confirm");
                window.setSize(300, 300);
                window.setLayout(new FlowLayout());
                window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                window.setLocationRelativeTo(null);
                var yesButton = new JButton("Confirm");
                yesButton.addActionListener(e1 -> {
                    writeToConfig(rebuildAppData.toString(), REBUILD_DAEMON_APP_DATA_PATH);
                    // generate new config.txt content

                    // write new config
                    writeToConfig(builder.toString(), COVER_ASSEMBLY_CONFIG_PATH);

                    rebuild(DaemonProgram.ASSEMBLY_REBUILD);
                    window.dispose();
                });
                window.add(yesButton);
                var noButton = new JButton("Cancel");
                noButton.addActionListener(e1 -> window.dispose());
                window.add(noButton);
                window.setVisible(true);
            } else {
                writeToConfig(rebuildAppData.toString(), REBUILD_DAEMON_APP_DATA_PATH);
                // generate new config.txt content

                // write new config
                writeToConfig(builder.toString(), COVER_ASSEMBLY_CONFIG_PATH);

                rebuild(DaemonProgram.ASSEMBLY_REBUILD);
            }
        });
        return button;
    }

    private static int getHoleNumber(String line) {
        var holeNumber = 0;
        if (line.contains("Hole")) {
            holeNumber = Integer.parseInt(line.split(" ")[1].trim());
        }
        return holeNumber;
    }

    private static void writeToConfig(String content, Path path) {
        if (WRITEABLE)
            FilesUtil.write(content, path);
    }

    private static Path getCoverConfigPath() {
        return coverShapeSelection.contains("Square") ? SQUARE_COVER_CONFIG_PATH : COVER_CONFIG_PATH;
    }

    private static JRadioButton[] holeAssemblyConfigRadios(String variableName) {
        var featureStringList = new StringBuilder();
        featureStringList.append("none");
        featureStringList.append("!");
        var assemblyConfigLines = FilesUtil.read(COVER_ASSEMBLY_CONFIG_PATH).split("\n");

        for (String line : assemblyConfigLines) {
            if (line.contains(variableName) && line.contains("Bool") &&
            !line.contains("IIF")) {
                var feature = line.split(variableName)[1].split("=")[0]
                        .replace("\"", "")
                        .replace("Bool", "")
                        .trim();
                featureStringList.append(feature);
                featureStringList.append("!");
            }
        }

        var featureArray = featureStringList.toString().split("!");

        var radioButtonArray = new JRadioButton[featureArray.length];
        var index = 0;
        for (String feature : featureArray) {
            radioButtonArray[index] = new JRadioButton(feature);
            radioButtonArray[index].setActionCommand(feature);
            ++index;
        }

        return radioButtonArray;
    }

    private static JButton baseCoverParamsBuildButton(String variableName) {
        var button = new JButton("Build");
        button.addActionListener(e -> writeBaseCoverChanges(variableName));
        return button;
    }

    private static void writeBaseCoverChanges(String variableName) {
        var coverConfigPath = coverShapeSelection.contains("Square") ? SQUARE_COVER_CONFIG_PATH : COVER_CONFIG_PATH;
        var coverConfigContentLines = FilesUtil.read(coverConfigPath).split("\n");

        // gets cover variables - user input and appends the line with the changed value to the lines array
        for (String userInputVariable : coverConfigVariableUserInputTable.keySet()) {
            if (userInputVariable.contains(variableName) &&
            !coverConfigVariableUserInputTable.get(userInputVariable).isEmpty()) {
                var variableLineNumber = coverConfigVariableNameLineNumberTable.get(userInputVariable);
                var lineSuffix = "";
                if (coverConfigContentLines[variableLineNumber].contains("in")) {
                    lineSuffix = "in";
                } else if (coverConfigContentLines[variableLineNumber].contains("deg") &&
                !coverConfigContentLines[variableLineNumber].contains("Bool")) {
                    lineSuffix = "deg";
                }

                var userVariable = coverConfigVariableUserInputTable.get(userInputVariable);

                var newLine = userInputVariable + "= " + userVariable + lineSuffix;
                coverConfigContentLines[variableLineNumber] = newLine;
            }
        }


        // check for variableName matching user input and if null - generate user input table for this variableName
        var offsetUserInputTable = new HashMap<String, String>();
        for (String userInput : coverConfigVariableUserInputTable.keySet()) {
            if (userInput.contains(variableName) && userInput.contains("Offset") &&
            !userInput.contains("Degree")) {
                var userOffset = coverConfigVariableUserInputTable.get(userInput).isEmpty() ?
                        "null" : coverConfigVariableUserInputTable.get(userInput);
                offsetUserInputTable.put(userInput, userOffset);
            }
        }

        // generate table of hole/X/Z - line numbers
        var holeZXLineNumberTable = new HashMap<Integer, String>();
        // generate table of line number - negative hole/x/z
        var negativeXZStringList = new StringBuilder();
        var index = 0;
        for (String line : coverConfigContentLines) {
            if (line.contains("in") && line.contains("Offset")) {
                holeZXLineNumberTable.put(index, line);
            } else if (line.contains("Negative")) {
                negativeXZStringList.append(line);
                negativeXZStringList.append("!");
            }
            ++index;
        }
        var negativeXZArray = negativeXZStringList.toString().split("!");

        for (int lineNumber : holeZXLineNumberTable.keySet()) {
            var line = coverConfigContentLines[lineNumber];
            for (String negation : negativeXZArray) {
                var holeNumber = "";
                var isNegative = false;
                if (negation.contains("Handle")) {
                    holeNumber = negation.split(" ")[2].trim() + " " + negation.split(" ")[3].trim();
                    isNegative = negation.split("=")[1].contains("1");
                } else {
                    holeNumber = negation.split("CA")[0].replace("\"", "").trim();
                    isNegative = negation.split("=")[1].contains("1");
                }
                try {
                    var XorZ = "";
                    if (negation.contains("CA")) {
                        XorZ = negation.split("CA")[1].split("Negative")[0];
                    } else if (negation.contains("Handle")) {
                        XorZ = negation.split("deg")[1].split("Negative")[0].trim();
                    }
                if (line.contains(holeNumber) && isNegative && line.contains(XorZ)) {
                    if (line.contains(variableName)) {
                        var lineVariable = line.split("=")[0];
                        var userInput = offsetUserInputTable.get(lineVariable);
                        var isUserInputNull = userInput.contains("null");
                        if (isUserInputNull) {
                            var newLine = coverConfigContentLines[lineNumber].split("=")[0] + "= -" +
                                    coverConfigContentLines[lineNumber].split("=")[1].trim();
                            coverConfigContentLines[lineNumber] = newLine;
                        } else {
                            var isUserInputNegative = userInput.contains("-");
                            if (isUserInputNegative) {
                                var newLine = coverConfigContentLines[lineNumber].split("=")[0] + "= " +
                                        coverConfigContentLines[lineNumber].split("=")[1].trim();
                                coverConfigContentLines[lineNumber] = newLine;

                            }
                        }
                    } else {
                        var newLine = coverConfigContentLines[lineNumber].split("=")[0] + "= -" +
                                coverConfigContentLines[lineNumber].split("=")[1].trim();
                        coverConfigContentLines[lineNumber] = newLine;
                    }
                }
                } catch (ArrayIndexOutOfBoundsException exception) {
                    System.out.println(negation);
                }
            }
        }

        // write updated content to file
        var builder = new StringBuilder();
        for (String line : coverConfigContentLines) {
            builder.append(line);
            builder.append("\n");
        }
        writeToConfig(builder.toString(), coverConfigPath);

        // write path app data to rebuild.txt
        writeToConfig(coverConfigPath.toString(), REBUILD_DAEMON_APP_DATA_PATH);

        // call auto-rebuild daemon
        rebuild(DaemonProgram.REBUILD);
    }

    // refactor to Util API - general use
    private static void rebuild(DaemonProgram program) {
        if (REBUILDABLE) {
            // write to rebuild.txt the path to the assembly config.txt file
            try {
                var rebuildDaemonProcess = new ProcessBuilder("cmd.exe", "/c", program.getProgram()).start();
                rebuildDaemonProcess.waitFor();
                rebuildDaemonProcess.destroy();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

}
