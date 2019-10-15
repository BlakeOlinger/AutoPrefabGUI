package com.ToppInd;

import bo.core.system.FilesUtil;

import javax.swing.*;
import java.awt.*;
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
    private static final Path COVER_ASSEMBLY_CONFIG_PATH = Paths.get("C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\blob - L2\\blob.L2_cover.txt");
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
            if (!line.contains("@") && !line.contains("IIF")){
                // get variable name from line
                var variableName = line.split("=")[0];
                coverConfigVariableNameLineNumberTable.put(variableName, index);
            }
            ++index;
        }
    }

    private static void displayAppWindow() {
        var window = new JFrame("AutoPrefab");
        window.setSize(600, 500);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new BorderLayout());

        // add "Configure Cover" button
        window.add(configureCoverButton(), BorderLayout.NORTH);

        // add "Build Drawing" button
        window.add(buildDrawingButton(), BorderLayout.SOUTH);

        window.setVisible(true);
    }

    private static JButton buildDrawingButton() {
        var button = new JButton("Build Drawing");
        button.addActionListener(e -> rebuild(DaemonProgram.BUILD_DRAWING));
        return button;
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

    private static void displayCoverConfigWindow(String shapeSelection) {
        var window = new JFrame("Cover Configurer");
        window.setLayout(new FlowLayout());
        window.setSize(400, 300);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        coverShapeSelection = shapeSelection;

        // set cover selection assembly config
        setCoverSelectionAssemblyConfig();

        // read cover config contents and set cover config variable table
        setCoverConfigVariableNameLineNumberTable(shapeSelection.contains("Square") ? SQUARE_COVER_CONFIG_PATH : COVER_CONFIG_PATH);

        // define total number of hole features to know the number of hole feature buttons to produce
        var holeFeatures = 0;
        for (String variable : coverConfigVariableNameLineNumberTable.keySet()) {
            if (!variable.matches("[^0-9]*"))
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

        window.setVisible(true);
    }

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
        window.setSize(300, 400);
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

        var radioButtons = holeAssemblyConfigRadios();
        var buttonGroup = new ButtonGroup();

        for (JRadioButton radioButton : radioButtons) {
            buttonGroup.add(radioButton);
        }

        for (JRadioButton radioButton : radioButtons) {
            window.add(radioButton);
        }

        window.add(confirmHoleAssemblyConfigButton(variableName, buttonGroup));

        window.setVisible(true);
    }

    // FIXME - BIG ONE - any dimension can never be negative as a stable state - it can only be negative for a single rebuild
    //  - then it must be overwritten to a positive value - this means there has to be an external variable for each
    //  - dimension that has to be able to be negative with every negation being based on those variables
    private static JButton confirmHoleAssemblyConfigButton(String variableName, ButtonGroup buttonGroup) {
        var button = new JButton("Confirm");
        button.addActionListener(e -> {

            // read blob.L2_cover.txt file and split into lines
            var coverAssemblyConfigLines = FilesUtil.read(COVER_ASSEMBLY_CONFIG_PATH).split("\n");

            // get line - line number for variables pairs
            var coverAssemblyConfigVariableLineNumberTable = new HashMap<String, Integer>();

            // <update this comment>
            var xOffset = "";
            var zOffset = "";
            var index = 0;
            for (String line : coverAssemblyConfigLines) {
                if (!line.contains("@") && !line.contains("IIF") &&
                line.contains("Bool")) {
                    var boolVariable = line.split("=")[0];
                    coverAssemblyConfigVariableLineNumberTable.put(boolVariable, index);
                } else if (!line.contains("@") && !line.contains("IIF") &&
                line.contains("Offset")) {
                    var offset = line.split("=")[0].trim();
                    coverAssemblyConfigVariableLineNumberTable.put(offset, index);
                    if (offset.contains("X")) {
                        xOffset = offset;
                    } else {
                        zOffset = offset;
                    }
                }
                ++index;
            }
            var userSelection = buttonGroup.getSelection().getActionCommand();

            // define user feature selection table
            var coverAssemblyFeatureUserSelectionTable = new HashMap<String, String>();
            for (String variable : coverAssemblyConfigVariableLineNumberTable.keySet()) {
                if (!variable.contains("Offset")) {
                    var isSelected = variable.contains(userSelection) ? "1" : "0";
                    coverAssemblyFeatureUserSelectionTable.put(variable, isSelected);
                }
            }

            // used only to help determine if a mate needs to be flipped
            var coverAssemblyOffsetVariablesTable = new HashMap<String, Boolean>();
            for (String variable : coverAssemblyConfigVariableLineNumberTable.keySet()) {
                if (variable.contains("Offset")) {
                    var currentAssemblyConfigLine = coverAssemblyConfigLines[coverAssemblyConfigVariableLineNumberTable.get(variable)];
                    var currentLineIsNegative = currentAssemblyConfigLine.split("=")[1].contains("-");
                    coverAssemblyOffsetVariablesTable.put(variable, currentLineIsNegative);
                }
            }

            // if variable contains Offset - read from blob.cover.txt for the variableName hole number and get the hole # X/Z CA Offset values
            // put that in the value of the variable
            var baseCoverConfigLines = FilesUtil.read(getCoverConfigPath()).split("\n");
            for (String line : baseCoverConfigLines) {
                if (line.contains("CA") && !line.contains("IIF") &&
                !line.contains("@")) {
                    var lineSplitCA = line.split("CA");
                    var xYFeature = lineSplitCA[1].split("=")[0].replace("\"", "").trim();
                    var holeNumber = lineSplitCA[0].replace("\"", "").trim();
                    var partConfigFeatureIsNegative = lineSplitCA[1].split("=")[1].contains("-");

                    for (String offsetVariable : coverAssemblyOffsetVariablesTable.keySet()) {
                        if (offsetVariable.contains(holeNumber) && offsetVariable.contains(xYFeature)) {
                            var assemblyOffsetIsNegative = coverAssemblyOffsetVariablesTable.get(offsetVariable);
                            var lineSegments = line.split("=");
                            var variableDeclaration = lineSegments[0];
                            var variableValue = lineSegments[1].trim();
                            if (!(partConfigFeatureIsNegative && assemblyOffsetIsNegative ||
                            !partConfigFeatureIsNegative && !assemblyOffsetIsNegative)) {
                                variableValue = "!" + variableValue;
                            }
                            if (variableDeclaration.contains(variableName) && variableDeclaration.contains("Offset") &&
                                    variableDeclaration.contains("CA")) {
                                var putVariable = "";
                                if (line.contains("X")) {
                                    putVariable = xOffset;
                                } else {
                                    putVariable = zOffset;
                                }
                                coverAssemblyFeatureUserSelectionTable.put(putVariable, variableValue);
                            }
                        }
                    }
                }
            }

            // FIXME - need an overall negative state watcher to determine if the assembly config offset is negative despite its value always being positive
            //  - otherwise it will write the dimensions to app data erroneously
            // generate mates to write to rebuild.txt app data - do this looking for any ! appended to offset features
            // add all dimensions @Distance4 for example but just the Distance<#> and write to app data
            // replace all occurrences of ! to '' - all values written to the assembly config must be positive
            var rebuildAppData = new StringBuilder();
            rebuildAppData.append(COVER_ASSEMBLY_CONFIG_PATH.toString());
            rebuildAppData.append("\n");
            for (String variable : coverAssemblyFeatureUserSelectionTable.keySet()) {
                if (coverAssemblyFeatureUserSelectionTable.get(variable).contains("!")) {
                    var variableSegments = variable.split(" ");
                    var variableHoleNumber = variableSegments[1].trim();
                    var variableXZ = variableSegments[2].trim();

                    for (String line : coverAssemblyConfigLines) {
                        if (line.contains("@") && line.contains(variableHoleNumber) &&
                                line.contains(variableXZ)) {
                            var configLineDistance = line.split("=")[0].split("@")[1].replace("\"", "");
                            rebuildAppData.append(configLineDistance);
                            rebuildAppData.append("\n");
                        }
                    }
                }
            }
            writeToConfig(rebuildAppData.toString(), REBUILD_DAEMON_APP_DATA_PATH);

            // update blob.L2_config.txt lines
            for (String variable : coverAssemblyFeatureUserSelectionTable.keySet()) {
                var lineNumber = coverAssemblyConfigVariableLineNumberTable.get(variable);
                var newLine = variable + "= " + coverAssemblyFeatureUserSelectionTable.get(variable);
                coverAssemblyConfigLines[lineNumber] = newLine;
            }

            // write config to config.txt file
            var builder = new StringBuilder();
            for (String line : coverAssemblyConfigLines) {
                builder.append(line);
                builder.append("\n");
            }

            // write new config
            writeToConfig(builder.toString(), COVER_ASSEMBLY_CONFIG_PATH);

            rebuild(DaemonProgram.ASSEMBLY_REBUILD);
        });
        return button;
    }

    private static void writeToConfig(String content, Path path) {
        if (WRITEABLE)
            FilesUtil.write(content, path);
    }

    private static Path getCoverConfigPath() {
        return coverShapeSelection.contains("Square") ? SQUARE_COVER_CONFIG_PATH : COVER_CONFIG_PATH;
    }

    private static JRadioButton[] holeAssemblyConfigRadios() {
        var featureArray = new String[]{
                "PF150S 0deg",
                "PF200T 90deg",
                "ECG 2 Hole",
                "10in Inspection Plate 45deg BC",
                "PF200S 0deg",
                "PF200S 90deg",
                "none"

        };
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
                } else if (coverConfigContentLines[variableLineNumber].contains("deg")) {
                    lineSuffix = "deg";
                }
                // set variable dimension prefix based on if user input positive/negative sign opposes the current negation
                var negationPrefix = "";
                var currentLineDimensionIsNegative = coverConfigContentLines[variableLineNumber].split("=")[1].contains("-");
                var userInputDimensionIsNegative = coverConfigVariableUserInputTable.get(userInputVariable).contains("-");
                if (currentLineDimensionIsNegative && userInputDimensionIsNegative ||
                        !currentLineDimensionIsNegative && !userInputDimensionIsNegative) {
                    negationPrefix = currentLineDimensionIsNegative ? "-" : "";
                } else {
                    negationPrefix = "!" + (currentLineDimensionIsNegative ? "-" : "");
                }

                var userVariable = coverConfigVariableUserInputTable.get(userInputVariable);
                if (userVariable.contains("-")) {
                    userVariable = userVariable.replace("-", "");
                }

                var newLine = userInputVariable + "= " + negationPrefix + userVariable + lineSuffix;
                coverConfigContentLines[variableLineNumber] = newLine;
            }
        }

        // write updated content to file
        var builder = new StringBuilder();
        for (String line : coverConfigContentLines) {
            builder.append(line);
            builder.append("\n");
        }
        writeToConfig(builder.toString(), coverConfigPath);

        // write to rebuild.txt which file to look for negative values in
        writeToConfig(coverConfigPath.toString(), REBUILD_DAEMON_APP_DATA_PATH);

        // call auto-rebuild daemon
        rebuild(DaemonProgram.REBUILD);
    }

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
