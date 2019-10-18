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
    private static final boolean WRITEABLE = REBUILDABLE;
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

    private static JButton coverAssemblyConfigureButton() {
        var button = new JButton("Cover Assembly Configurer");
        button.addActionListener(e -> displayCoverAssemblyConfigWindow());
        return button;
    }

    private static void displayCoverAssemblyConfigWindow() {
        var window = new JFrame("General Assembly Feature Configurer");
        window.setSize(400, 300);
        window.setLayout(new FlowLayout());
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        window.add(handleButton());

        window.setVisible(true);
    }

    private static JButton handleButton() {
        var button = new JButton("Cover Assembly Handle Config");
        button.addActionListener(e -> displayAssemblyHandleConfigWindow());
        return button;
    }

    private static void displayAssemblyHandleConfigWindow() {
        var window = new JFrame("Cover Assembly Handle Configurer");
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setSize(300, 150);
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

        var handleGTBoxLabel = coverHasHandle ? "Handle GT Box Bool: " : "No Active Handle Found ";
        var label = new JLabel(handleGTBoxLabel);
        window.add(label);

        if (coverHasHandle) {
            var textBox = new JTextField(1);
            textBox.addActionListener(Main::handleBoolAction);
            window.add(textBox);
        }

        window.setVisible(true);
    }

    // TODO - refactor this into a class that can be used
    //  - with other general assembly features
    private static void handleBoolAction(ActionEvent e){
        var userInput = e.getActionCommand().isEmpty() ? null : e.getActionCommand();
        var partConfigLines = FilesUtil.read(getCoverConfigPath()).split("\n");
        var assemblyConfigLines = FilesUtil.read(COVER_ASSEMBLY_CONFIG_PATH).split("\n");

        // populate a part config.txt line number - variable list table
        var partConfigLineNumberVariableListTable = new HashMap<Integer, String>();
        var index = 0;
        for (String line : partConfigLines) {
            if (line.contains("Handle") && !line.contains("IIF") &&
            !line.contains("@")) {
                var handleVariable = line.split("=")[0].trim();

                partConfigLineNumberVariableListTable.put(index, handleVariable);
            }
            ++index;
        }

        // populate an assembly config.txt line number - variable table list
        var assemblyConfigLineNumberVariableListTable = new HashMap<Integer, String>();
        index = 0;
        for (String line : assemblyConfigLines) {
            if (line.contains("GT Box") && !line.contains("@") &&
            !line.contains("IIF")) {
                var handleVariable = line.split("=")[0].trim();

                assemblyConfigLineNumberVariableListTable.put(index, handleVariable);
            }
            ++index;
        }

        // makes sure part config and assembly config handle booleans match - default if user input is null
        for (int partConfigLineNumber : partConfigLineNumberVariableListTable.keySet()) {
            var partConfigVariable = partConfigLineNumberVariableListTable.get(partConfigLineNumber);
            if (partConfigVariable.contains("Bool")) {
                var partHandleIsActive = partConfigLines[partConfigLineNumber].split("=")[1].contains("1");
                var partHandleType = partConfigVariable.split("deg")[0].trim().split("Handle")[1]
                        .trim() + "deg";
                for (int assemblyConfigLineNumber : assemblyConfigLineNumberVariableListTable.keySet()) {
                    var assemblyConfigVariable = assemblyConfigLineNumberVariableListTable.get(assemblyConfigLineNumber);
                    if (assemblyConfigVariable.contains("Bool")) {
                        // line assumed to be: "GT Box 0deg Bool"
                        var assemblyHandleType = assemblyConfigVariable.split(" ")[2].trim();

                        if (assemblyHandleType.compareTo(partHandleType) == 0) {
                            var assemblyHandleIsActive = assemblyConfigLines[assemblyConfigLineNumber].split("=")[1].contains("1");

                            // check if handle booleans match
                            var partAssemblyBoolMismatch = !(partHandleIsActive && assemblyHandleIsActive ||
                                    !assemblyHandleIsActive && !partHandleIsActive);

                            // if mismatched set assembly config line to match part config line
                            if (partAssemblyBoolMismatch) {
                                var newLine = assemblyConfigVariable + "= " + (partHandleIsActive ? "1" : "0");

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
        var assemblyLineNumberGTBoxVariableTable = new HashMap<Integer, String>();
        index = 0;
        for (String line : assemblyConfigLines) {
            if (line.contains("GT Box") && line.contains("Offset") &&
            !line.contains("IIF") && !line.contains("@")) {
                assemblyLineNumberGTBoxVariableTable.put(index, line);
            }
            ++index;
        }

        // populate relevant offset table list
        var offsetTableStringList = new StringBuilder();
        var offsetTableLines = FilesUtil.read(APP_DATA_HANDLE_OFFSET_TABLE).split("\n");
        for (String line : offsetTableLines) {
            if (line.contains("GT Box")) {
                offsetTableStringList.append(line);
                offsetTableStringList.append("!");
            }
        }
        var offsetTableArray = offsetTableStringList.toString().split("!");

        // compare offsets and generate corresponding assembly config lines
        for (int lineNumber : assemblyLineNumberGTBoxVariableTable.keySet()) {
            var assemblyLine = assemblyConfigLines[lineNumber];
            var assemblyType = assemblyLine.split(" ")[2].trim();
            var assemblyIsX = assemblyLine.split(" ")[3].contains("X");

            for (String partVariable : partHandleVariableArray) {
                var partType = partVariable.split("Handle")[1].split(" ")[1].trim();
                var partIsX = partVariable.split(" ")[4].contains("X");

                if (assemblyType.compareTo(partType) == 0 &&
                        (assemblyIsX && partIsX || !assemblyIsX && !partIsX)){
                    for (String offset : offsetTableArray) {
                        var offsetIsX = offset.split("=")[0].contains("X");
                        var offsetType = offset.split(" ")[2].trim();

                        if ((offsetIsX && assemblyIsX || !offsetIsX && !assemblyIsX) &&
                        offsetType.compareTo(partType) == 0) {
                            var partValue = Double.parseDouble(partVariable.split("=")[1].replace("in", "").trim());
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
        if (userInput != null && userInput.contains("0")) {
            // set all GT Box Bool to 0
            for (int assemblyConfigLineNumber : assemblyConfigLineNumberVariableListTable.keySet()) {
                var assemblyConfigVariable = assemblyConfigLineNumberVariableListTable.get(assemblyConfigLineNumber);
                if (assemblyConfigVariable.contains("Bool")) {
                    var newLine = assemblyConfigVariable.trim() + "= 0";
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
            if (line.contains("Negative") && line.contains("GT Box")) {
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
            var partLineIsX = partConfigLine.contains("X");
            // assumes: "Cover Hatch Handle 90deg Z Negative"= 0
            var partLineType = partConfigLine.split(" ")[3].trim();

            for (int assemblyLineNumber : assemblyConfigLineNumberNegationTable.keySet()) {
                var assemblyConfigLine = assemblyConfigLineNumberNegationTable.get(assemblyLineNumber);
                var assemblyLineIsX = assemblyConfigLine.contains("X");
                // assumes: "GT Box 0deg X Negative"= 0
                var assemblyLineType = assemblyConfigLine.split(" ")[2].trim();

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
        window.setSize(300, 500);
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

        window.add(confirmHoleAssemblyConfigButton(variableName, buttonGroup));

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
                if (line.contains("Negative")) {
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
                    if (assemblyDimension.contains("X") && partDimension.contains("X") ||
                            assemblyDimension.contains("Z") && partDimension.contains("Z")) {
                        var assemblyStringSegments = assemblyDimension.split(" ");
                        var partStringSegments = partDimension.split(" ");
                        var assemblyHoleNumber = Integer.parseInt(assemblyStringSegments[1].trim());
                        var partHoleNumber =Integer.parseInt(partStringSegments[1].trim());
                        if (assemblyHoleNumber == partHoleNumber) {
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
