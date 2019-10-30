package com.ToppInd;

import bo.core.system.FilesUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final String PATH_BASE = "C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\";
    private static final Path COVER_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.cover.txt");
    static Path getCoverConfigPath() {
        return COVER_CONFIG_PATH;
    }
    private static final Path SQUARE_COVER_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.coverSquare.txt");
    static Path getSquareCoverConfigPath() {
        return SQUARE_COVER_CONFIG_PATH;
    }
    private static final Path COVER_SHAPE_ASSEMBLY_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.coverConfig.txt");
    static Path getCoverShapeAssemblyConfigPath() {
        return COVER_ASSEMBLY_CONFIG_PATH;
    }
    private static final Path REBUILD_DAEMON_APP_DATA_PATH = Paths.get("C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\app data\\rebuild.txt");
    static Path getRebuildDaemonAppDataPath() {
        return REBUILD_DAEMON_APP_DATA_PATH;
    }
    private static final String APP_DATA_BASE = "C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\app data\\";
    private static final Path HANDLE_OFFSET_LOOKUP_TABLE_PATH = Paths.get(APP_DATA_BASE + "assembly handle offset table.txt");
    static Path getHandleOffsetLookupTablePath() {
        return HANDLE_OFFSET_LOOKUP_TABLE_PATH;
    }
    private static final Path COVER_ASSEMBLY_CONFIG_PATH = Paths.get("C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\blob - L2\\blob.L2_cover.txt");
    static Path getCoverAssemblyConfigPath() {
        return COVER_ASSEMBLY_CONFIG_PATH;
    }
    private static final Path ANGLE_FRAME_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.2inAngleFrame.txt");
    static Path getAngleFrameConfigPath() {
        return ANGLE_FRAME_CONFIG_PATH;
    }
    private static final Path ALUM_FLAT_BAR_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.alumFlatBar.txt");
    private static final Path INSPECTION_PLATE_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.inspectionPlate.txt");
    static Path getInspectionPlateConfigPath() {
        return INSPECTION_PLATE_CONFIG_PATH;
    }
    private static final Path COVER_DRAWING_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.coverDrawing.txt");
    static Path getCoverDrawingConfigPath() {
        return COVER_DRAWING_CONFIG_PATH;
    }
    private static final Path COVER_ASSEMBLY_PATH = Paths.get(PATH_BASE + "blob - L2\\blob.L2_cover.SLDASM");
    static Path getCoverAssemblyPath() {
        return COVER_ASSEMBLY_PATH;
    }
    private static final Path COVER_DRAWING_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.cover.SLDDRW");
    static Path getCoverDrawingPath() {
        return COVER_DRAWING_PATH;
    }
    private static final HashMap<Integer, Path> HOLE_FEATURE_CONFIG_MAP = new HashMap<>(
            Map.of(
                    1, Paths.get(PATH_BASE + "blob - L2\\blob.holeFeature_1.txt"),
                    2, Paths.get(PATH_BASE + "blob - L2\\blob.holeFeature_2.txt"),
                    3, Paths.get(PATH_BASE + "blob - L2\\blob.holeFeature_3.txt"),
                    4, Paths.get(PATH_BASE + "blob - L2\\blob.holeFeature_4.txt"),
                    5, Paths.get(PATH_BASE + "blob - L2\\blob.holeFeature_5.txt"),
                    6, Paths.get(PATH_BASE + "blob - L2\\blob.holeFeature_6.txt")
            )
    );
    static HashMap<Integer, Path> getHoleFeatureConfigMap() {
        return HOLE_FEATURE_CONFIG_MAP;
    }
    private static HashMap<String, Integer> coverConfigVariableNameLineNumberTable = new HashMap<>();
    static HashMap<String, Integer> getCoverConfigVariableNameLineNumberTable() {
        return coverConfigVariableNameLineNumberTable;
    }
    private static HashMap<String, String> coverConfigVariableUserInputTable = new HashMap<>();
    static HashMap<String, String> getCoverConfigVariableUserInputTable() {
        return coverConfigVariableUserInputTable;
    }
    private static String coverShapeSelection = "Circular";
    static String getCoverShapeSelection() {
        return coverShapeSelection;
    }
    static void setCoverShapeSelection(String shapeSelection) {
        coverShapeSelection = shapeSelection;
    }
    private static final HashMap<String, String> MATERIAL_CONFIG_TABLE = new HashMap<>(
            Map.of(
                    "ASTM A36 Steel", "0",
                    "6061 Alloy", "1"
            )
    );
    private static final boolean REBUILDABLE = true;
    static boolean getBuildable() {
        return REBUILDABLE;
    }
    private static final boolean WRITEABLE = true;
    static boolean getWritable() {
        return WRITEABLE;
    }
    private static final boolean ASSEMBLY_MATE_CALIBRATION = false;
    static boolean getMateCalibration() {
        return ASSEMBLY_MATE_CALIBRATION;
    }
    // TODO - incorporate lookup tables
    //  - 1) write to a configuration selection app data file
    //      - the length and width of hatch
    //  - 2) from that config app data file set relative offsets
    //      - such as GT Box, Handle, Hatch Arm, etc. based on a
    //      - lookup table for that feature
    // TODO - automate blob.* based part descriptions
    //  - 1) do this based on a lookup table for the part - cover/2in angle frame/etc.
    //      - utilize <> variables that will substitute current user defined values for diameter etc.
    // TODO - define an appropriate algorithm and lookup table scheme for including drawing notes
    // TODO - in the auto-dimension daemon - have daemon search for 0" dimensions and unmark for drawing those - mark else
    // TODO - Pack And Go - define the algorithm and user interaction
    // TODO - BOM - auto-delete rows based on a lookup table - not sure if I'll go white/black list
    // TODO - on selection of a hole feature - ECG 2 Hole for example - automatically set the diameter and BC properties
    // TODO - (long-term/down time) - need to refactor the mess that is this monolithic class
    // TODO - (after previous/next phase) - begin general prefab - utilize lookup tables and wrapper assemblies
    //          - utilize separate *.SLDDRW files per sheet and auto-copy past into final drawing doc each sheet
    // TODO - eff it - prefab now - modeling initial success condition after - SO:324900
    //  - various thoughts in no particular order:
    //      - config prefab button
    //      - require 3 individual *.SLDDRW files for each sheet
    //      - copy paste into final
    //      - configure pump - selection
    //      - config all the various bits base XYZ placement on lookup tables
    //      - adapt drawing config to handle sheets - cover, wet well1, wet well2, prefab BOM etc
    //      - config basin window
    //      - maybe don't have individual sheets in different files - cover/prefab okay, any more then BOM difficult to transfer

    public static void main(String[] args) {
        // display main window
         displayAppWindow();
    }

    private static void displayAppWindow() {
        var window = new JFrame("AutoPrefab");
        window.setSize(400, 200);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new FlowLayout());

        // add "Configure Cover" button
        window.add(Cover.Button.configureCoverButton());

        // add "Configure Prefab" button
        window.add(Prefab.Button.getButton());

        // add "Build Drawing" button
        window.add(Drawing.Button.configureDrawingButton());

        window.setVisible(true);
    }

    private static JButton armButton() {
        var button = new JButton("Hatch Arm");
        button.addActionListener(e -> displayHatchArmConfigWindow());
        return button;
    }

    private static void displayHatchArmConfigWindow() {
        var window = new JFrame("Hatch Arm Configurer");
        window.setSize(300, 300);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setLayout(new FlowLayout());

        window.add(new JLabel("Hatch Arm Bool: "));
        var boolBox = new JTextField(1);
        boolBox.addActionListener(e -> Cover.ActionHandler.assemblyBoolActionHandler(e, "\"Arm Bool\"="));
        window.add(boolBox);

        window.add(new JLabel("Hatch Arm X Offset: "));
        var xOffset = new JTextField(2);
        xOffset.addActionListener(e -> Cover.ActionHandler.assemblyDimensionActionHandler(e, "\"Arm X Offset\"=", "in"));
        window.add(xOffset);

        window.add(new JLabel("Hatch Arm Z Offset: "));
        var zOffset = new JTextField(2);
        zOffset.addActionListener(e -> Cover.ActionHandler.assemblyDimensionActionHandler(e, "\"Arm Z Offset\"=", "in"));
        window.add(zOffset);

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
        boolBox.addActionListener(e -> Cover.ActionHandler.assemblyBoolActionHandler(e, "\"Bolt-In Hinge Bool\"="));
        window.add(boolBox);

        window.add(new JLabel("Bolt-In Hinges Z Offset: "));
        var zOffset = new JTextField(2);
        zOffset.addActionListener(e -> Cover.ActionHandler.assemblyDimensionActionHandler(e, "\"Bolt-In Hinge Z Offset\"=", "in"));
        window.add(zOffset);

        window.add(new JLabel("Bolt-In Hinge 1 X Offset: "));
        var xOneOffset = new JTextField(2);
        xOneOffset.addActionListener(e -> Cover.ActionHandler.assemblyDimensionActionHandler(e, "\"Bolt-In Hinge 1 X Offset\"=", "in"));
        window.add(xOneOffset);

        window.add(new JLabel("Bolt-In Hinge 2 X Offset: "));
        var xTwoOffset = new JTextField(2);
        xTwoOffset.addActionListener(e -> Cover.ActionHandler.assemblyDimensionActionHandler(e, "\"Bolt-In Hinge 2 X Offset\"=", "in"));
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
        textBox.addActionListener(e -> Cover.ActionHandler.assemblyBoolActionHandler(e, "\"Hatch Lock Plate Bool\"="));
        window.add(textBox);

        window.add(new JLabel("Lock Plate Z Offset: "));
        var offsetBox = new JTextField(2);
        offsetBox.addActionListener(e -> Cover.ActionHandler.assemblyDimensionActionHandler(e, "\"Hatch Lock Plate Z Offset\"=", "in"));
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
        alumFlatBoolBox.addActionListener(e -> Cover.ActionHandler.assemblyBoolActionHandler(e, "\"Aluminum Flat Bar 0deg Bool\"="));
        window.add(alumFlatBoolBox);

        window.add(new JLabel("Alum Flat 90deg Bool: "));
        var alumFlat90BoolBox = new JTextField(1);
        alumFlat90BoolBox.addActionListener(e -> Cover.ActionHandler.assemblyBoolActionHandler(e, "\"Aluminum Flat Bar 90deg Bool\"="));
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

    // refactor to Assembly Util API - general assembly dimension handler
    private static void assemblyDimensionActionHandler(ActionEvent event, String line, String units, Path configPath) {
        // the two booleans are going to point to calls to external methods
        var userInput = Util.UserInput.getUserTextInput(event);
        if (userInput != null) {
            var assemblyConfigLines = Util.Path.getLinesFromPath(configPath);
            var dimensionLineNumberTable = Util.Map.getSingleLineNumberTable(assemblyConfigLines, line);

            for (int lineNumber : dimensionLineNumberTable.keySet()) {
                var newLine = Util.UserInput.getNewLineFromUserInput(assemblyConfigLines[lineNumber], userInput, units);

                assemblyConfigLines[lineNumber] = newLine;
            }

            // write app data
            Util.Output.writeToConfig(configPath.toString(), REBUILD_DAEMON_APP_DATA_PATH, WRITEABLE);

            // generate and write config.txt data
            var newText = Util.Output.generateWriteOutput(assemblyConfigLines);
            Util.Output.writeToConfig(newText, configPath, WRITEABLE);

            // call rebuild daemon
            Util.Build.rebuild(DaemonProgram.ASSEMBLY_GENERAL, REBUILDABLE);
        }
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
        Util.Output.writeToConfig(builder.toString(), Util.Path.getCoverConfigPath(), WRITEABLE);

        // write selected config.txt path to rebuild.txt app data
        Util.Output.writeToConfig(Util.Path.getCoverConfigPath().toString(), REBUILD_DAEMON_APP_DATA_PATH, WRITEABLE);

        // call rebuild for AutoMaterialConfig.appref-ms
        Util.Build.rebuild(DaemonProgram.MATERIAL_CONFIG, REBUILDABLE);
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
        Util.Output.writeToConfig(builder.toString(), coverConfigPath, WRITEABLE);

        // write path app data to rebuild.txt
        Util.Output.writeToConfig(coverConfigPath.toString(), REBUILD_DAEMON_APP_DATA_PATH, WRITEABLE);

        // call auto-rebuild daemon
        Util.Build.rebuild(DaemonProgram.REBUILD, REBUILDABLE);
    }

}
