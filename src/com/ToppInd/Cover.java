package com.ToppInd;

import bo.core.system.FilesUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.util.HashMap;

final class Cover {
    static class Button {
        static JButton configureCoverButton() {
            var button = new JButton("Configure Cover");
            button.addActionListener(e -> Window.displayCoverShapeSelector());
            return button;
        }

        static JButton coverParamsButton(String label, ActionListener actionListener) {
            var button = new JButton(label);
            button.addActionListener(actionListener);
            return button;
        }

        static JButton holeAssemblyConfigButton(String variableName) {
            var button = new JButton("Assembly Config");
            button.addActionListener(e -> Window.holeAssemblyConfigWindow(variableName));
            return button;
        }

        static JButton baseCoverParamsBuildButton(String variableName) {
            var button = new JButton("Build");
            button.addActionListener(e -> writeBaseCoverChanges(variableName));
            return button;
        }

        static JButton selectMaterialButton() {
            var button = new JButton("Material");
            button.addActionListener(e -> displaySelectMaterialWindow());
            return button;
        }

        static JButton coverAssemblyConfigureButton() {
            var button = new JButton("Cover Assembly Configurer");
            button.addActionListener(e -> Window.displayCoverAssemblyConfigWindow());
            return button;
        }

        static JButton inspectionPlateConfigButton(String variableName) {
            var button = new JButton("Inspection Plate Config");
            button.addActionListener(e -> Window.displayInspectionPlateConfigWindow());
            return button;
        }

        // at some point this will need to be broken up
        static JButton confirmHoleAssemblyConfigButton(String variableName, ButtonGroup buttonGroup) {
            var button = new JButton("Confirm");
            button.addActionListener(e -> {

                // read blob.L2_cover.txt file and split into lines
                var coverAssemblyConfigLines = FilesUtil.read(Main.getCoverAssemblyConfigPath()).split("\n");
                var holePath = Util.Path.getHolePath(variableName);

                var holeConfigLines = Util.Path.getLinesFromPath(holePath);

                // get user selection - forces default "none" if nothing is selected on confirm
                var userSelection = "";
                try {
                    userSelection = buttonGroup.getSelection().getActionCommand().trim();
                } catch (NullPointerException exception) {
                    userSelection = "none";
                }

                // populate a line number - feature bool map
                var lineNumberFeatureBoolTable = new HashMap<Integer, String>();
                var index = 0;
                for (String line : holeConfigLines) {
                    if (line.contains("Bool") && !line.contains("IIF")) {
                        lineNumberFeatureBoolTable.put(index, line);
                    }
                    ++index;
                }

                // set the bool state of the hole (variable name) and feature selected
                // for the hole config.txt file - writes now
                for (int lineNumber : lineNumberFeatureBoolTable.keySet()) {
                    var line = lineNumberFeatureBoolTable.get(lineNumber);
                    // check if line in assembly config.txt contains user selected hole number
                    if (line.contains("Bool") &&
                            !line.contains("IIF")) {
                        if (line.replace("\"", "").contains(userSelection)) {
                            var featureSelectedVariable = line.split("=")[1].trim();
                            var isFeatureCurrentlySelected = featureSelectedVariable.contains("1");
                            // if feature isn't selected set it to selected
                            if (!isFeatureCurrentlySelected) {
                                var newLine = holeConfigLines[lineNumber].split("=")[0].trim() + "= 1";
                                holeConfigLines[lineNumber] = newLine;
                            }
                            // set rest to 0
                        } else {
                            var newLine = holeConfigLines[lineNumber].split("=")[0].trim() + "= 0";
                            holeConfigLines[lineNumber] = newLine;
                        }
                    }
                }
                var writeOutput = Util.Output.generateWriteOutput(holeConfigLines);
                Util.Output.writeToConfig(writeOutput, holePath, Main.getWritable());
                Util.Build.rebuild(DaemonProgram.BASIC_REBUILD, Main.getBuildable());

                // read part config.txt for the variable name (hole number) and X/Z negative state - read only
                // only care about the current variable name (hole number) X/Z
                var partConfigXZNegationStateTable = new HashMap<String, Boolean>();
                var partConfigLines = FilesUtil.read(Util.Path.getCoverConfigPath()).split("\n");
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
                rebuildAppData.append(Main.getCoverAssemblyConfigPath());
                rebuildAppData.append("\n");
                for (String assemblyDimension : assemblyConfigXZNegationTable.keySet()) {
                    for (String partDimension : partConfigXZNegationStateTable.keySet()) {
                        var assemblyHoleNumber = Assembly.getHoleNumber(assemblyDimension);
                        var partHoleNumber = Assembly.getHoleNumber(partDimension);
                        if (assemblyHoleNumber == partHoleNumber && (assemblyDimension.contains("X") &&
                                partDimension.contains("X") || assemblyDimension.contains("Z")
                                && partDimension.contains("Z"))) {
                            var assemblyIsNegative = assemblyDimension.split("=")[1].contains("1");
                            var partIsNegative = partDimension.split("=")[1].contains("1");
                            if (!(assemblyIsNegative && partIsNegative ||
                                    !assemblyIsNegative && !partIsNegative)) {
                                var XorZ = assemblyDimension.contains("X") ? "X" : "Z";
                                for (String line : coverAssemblyConfigLines) {
                                    if (line.contains("@") && line.contains(XorZ) &&
                                            line.contains(variableName)) {
                                        var dimension = line.split("@")[1].split("=")[0]
                                                .replace("\"", "").trim();
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
                    if (line.contains(variableName)) {
                        if (line.contains("Offset")) {
                            if (line.contains("in")) {
                                assemblyConfigXZOffsetLineNumberStringList.append(index);
                                assemblyConfigXZOffsetLineNumberStringList.append(" ");
                            }
                        }
                    }
                    ++index;
                }
                var assemblyConfigOffsetLineNumberArray = assemblyConfigXZOffsetLineNumberStringList.toString()
                        .split(" ");

                // set cover assembly lines for hole number offset to part config dimension
                for (String lineNumber : assemblyConfigOffsetLineNumberArray) {
//                System.out.println(lineNumber);
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
                if (Main.getMateCalibration()) {
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
                    window.setSize(400, 600);
                    window.setLayout(new FlowLayout());
                    window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    window.setLocationRelativeTo(null);
                    var yesButton = new JButton("Confirm");
                    yesButton.addActionListener(e1 -> {
                        Util.Output.writeToConfig(rebuildAppData.toString(), Main.getRebuildDaemonAppDataPath(),
                                Main.getWritable());
                        // generate new config.txt content

                        // write new config
                        Util.Output.writeToConfig(builder.toString(), Main.getCoverAssemblyConfigPath(),
                                Main.getWritable());

                        Util.Build.rebuild(DaemonProgram.ASSEMBLY_REBUILD, Main.getBuildable());
                        window.dispose();
                    });
                    window.add(yesButton);
                    var noButton = new JButton("Cancel");

                    // show mates about to flip
                    window.add(new JLabel("Mates to flip:"));
                    var mateLines = rebuildAppData.toString().split("\n");
                    for (var i = 1; i < mateLines.length; ++i) {
                        window.add(new JLabel(mateLines[i]));
                    }
                    noButton.addActionListener(e1 -> window.dispose());
                    window.add(noButton);
                    window.setVisible(true);
                } else {
                    Util.Output.writeToConfig(rebuildAppData.toString(), Main.getRebuildDaemonAppDataPath(),
                            Main.getWritable());
                    // generate new config.txt content

                    // write new config
                    Util.Output.writeToConfig(builder.toString(), Main.getCoverAssemblyConfigPath(),
                            Main.getWritable());

                    Util.Build.rebuild(DaemonProgram.ASSEMBLY_REBUILD, Main.getBuildable());
                }
            });
            return button;
        }

        static JButton handleButton() {
            var button = new JButton("Cover Assembly Handle Config");
            button.addActionListener(e -> Window.displayAssemblyHandleConfigWindow());
            return button;
        }

        static JButton angleFrameButton() {
            var button = new JButton("2in Angle Frame");
            button.addActionListener(e -> Window.displayAngleFrameConfigWindow());
            return button;
        }
    }

    static class ActionHandler {
        static void assemblyBoolActionHandler(ActionEvent event, String line, Path configPath) {
            var userInput = Util.UserInput.getUserTextInput(event);
            if (userInput != null) {
                var assemblyConfigLines = Util.Path.getLinesFromPath(configPath);
                var boolLineNumberTable = Util.Map.getSingleLineNumberTable(assemblyConfigLines, line);

                for (int lineNumber : boolLineNumberTable.keySet()) {
                    var newLine = Util.UserInput.getNewLineFromUserInput(assemblyConfigLines[lineNumber],
                            userInput, "");

                    assemblyConfigLines[lineNumber] = newLine;
                }

                // write app data
                Util.Output.writeToConfig(configPath.toString(), Main.getRebuildDaemonAppDataPath(),
                        Main.getWritable());

                // generate and write new lines
                var newText = Util.Output.generateWriteOutput(assemblyConfigLines);
                Util.Output.writeToConfig(newText, configPath, Main.getWritable());

                // call rebuild daemon
                Util.Build.rebuild(DaemonProgram.ASSEMBLY_GENERAL, Main.getBuildable());
            }
        }

        static void assemblyBoolActionHandler(ActionEvent event, String line) {
            var userInput = Util.UserInput.getUserTextInput(event);
            if (userInput != null) {
                var assemblyConfigLines = Util.Path.getLinesFromPath(Main.getCoverAssemblyConfigPath());
                var boolLineNumberTable = Util.Map.getSingleLineNumberTable(assemblyConfigLines, line);

                for (int lineNumber : boolLineNumberTable.keySet()) {
                    var newLine = Util.UserInput.getNewLineFromUserInput(assemblyConfigLines[lineNumber],
                            userInput, "");

                    assemblyConfigLines[lineNumber] = newLine;
                }

                // write app data
                Util.Output.writeToConfig(Main.getCoverAssemblyConfigPath().toString(),
                        Main.getRebuildDaemonAppDataPath(), Main.getWritable());

                // generate and write new lines
                var newText = Util.Output.generateWriteOutput(assemblyConfigLines);
                Util.Output.writeToConfig(newText, Main.getCoverAssemblyConfigPath(),
                        Main.getWritable());

                // call rebuild daemon
                Util.Build.rebuild(DaemonProgram.ASSEMBLY_GENERAL, Main.getBuildable());
            }
        }

        static void assemblyDimensionActionHandler(ActionEvent event, String line, String units) {
            // the two booleans are going to point to calls to external methods
            var userInput = Util.UserInput.getUserTextInput(event);
            if (userInput != null) {
                var assemblyConfigLines = Util.Path.getLinesFromPath(Main.getCoverAssemblyConfigPath());
                var dimensionLineNumberTable = Util.Map.getSingleLineNumberTable(assemblyConfigLines, line);

                for (int lineNumber : dimensionLineNumberTable.keySet()) {
                    var newLine = Util.UserInput.getNewLineFromUserInput(assemblyConfigLines[lineNumber],
                            userInput, units);

                    assemblyConfigLines[lineNumber] = newLine;
                }

                // write app data
                Util.Output.writeToConfig(Main.getCoverAssemblyConfigPath().toString(),
                        Main.getRebuildDaemonAppDataPath(), Main.getWritable());

                // generate and write config.txt data
                var newText = Util.Output.generateWriteOutput(assemblyConfigLines);
                Util.Output.writeToConfig(newText, Main.getCoverAssemblyConfigPath(),
                        Main.getWritable());

                // call rebuild daemon
                Util.Build.rebuild(DaemonProgram.ASSEMBLY_GENERAL, Main.getBuildable());
            }
        }

        // at some point this will need to be broken up
        static void handleBoolAction(ActionEvent event, String assemblyFeature){
            var userInput = Util.UserInput.getUserTextInput(event);
            if (userInput != null) {
                var partConfigLines = FilesUtil.read(Util.Path.getCoverConfigPath()).split("\n");
                var assemblyConfigLines = FilesUtil.read(Main.getCoverAssemblyConfigPath()).split("\n");

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
                        var partTypeIndex = partConfigLine.split("=")[0].indexOf(partConfigLine.contains("9") ?
                                '9' : '0');
                        var partType = partConfigLine.substring(partTypeIndex, partIs90deg ? partTypeIndex + 5 :
                                partTypeIndex + 4);
                        var partHandleIsActive = partConfigLines[partConfigLineNumber].split("=")[1]
                                .contains("1");

                        for (int assemblyConfigLineNumber : assemblyConfigLineNumberVariableListTable.keySet()) {
                            var assemblyConfigLine = assemblyConfigLineNumberVariableListTable
                                    .get(assemblyConfigLineNumber);

                            if (assemblyConfigLine.contains("Bool")) {
                                var assemblyType = Util.Dimension.getDimensionDegreeType(assemblyConfigLine);
                                var partAssemblyTypeIsSame = assemblyType.compareTo(partType) == 0;
                                if (partAssemblyTypeIsSame) {
                                    var assemblyHandleIsActive = assemblyConfigLines[assemblyConfigLineNumber]
                                            .split("=")[1].contains("1");

                                    // check if handle booleans match
                                    var partAssemblyBoolMismatch = !(partHandleIsActive && assemblyHandleIsActive ||
                                            !assemblyHandleIsActive && !partHandleIsActive);

                                    // if mismatched set assembly config line to match part config line
                                    if (partAssemblyBoolMismatch) {
                                        var newLine = Util.UserInput.getNewLineFromUserInput(assemblyConfigLine,
                                                userInput, "");

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
                var offsetTableLines = FilesUtil.read(Main.getHandleOffsetLookupTablePath()).split("\n");
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
                    var assemblyType = Util.Dimension.getDimensionDegreeType(assemblyLine);
                    var assemblyIsX = Util.Dimension.isDimensionX(assemblyLine);

                    for (String partLine : partHandleVariableArray) {

                        var partType = Util.Dimension.getDimensionDegreeType(partLine);
                        var partIsX = Util.Dimension.isDimensionX(partLine);
                        var assemblyPartTypeIsSame = assemblyType.compareTo(partType) == 0;

                        if (assemblyPartTypeIsSame &&
                                (assemblyIsX && partIsX || !assemblyIsX && !partIsX)) {

                            for (String offset : offsetTableArray) {
                                var offsetIsX = Util.Dimension.isDimensionX(offset);
                                var offsetType = Util.Dimension.getDimensionDegreeType(offset);

                                if ((offsetIsX && assemblyIsX || !offsetIsX && !assemblyIsX) &&
                                        offsetType.compareTo(partType) == 0) {
                                    var partValue = Double.parseDouble(partLine.split("=")[1]
                                            .replace("in", "").trim());
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
                            var newLine = Util.UserInput.getNewLineFromUserInput(assemblyConfigLine, userInput, "");
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
                Util.Output.writeToConfig(builder.toString(), Main.getCoverAssemblyConfigPath(),
                        Main.getWritable());

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
                    var partLineIsX = Util.Dimension.isDimensionX(partConfigLine);
                    var partLineType = Util.Dimension.getDimensionDegreeType(partConfigLine);

                    for (int assemblyLineNumber : assemblyConfigLineNumberNegationTable.keySet()) {
                        var assemblyConfigLine = assemblyConfigLineNumberNegationTable.get(assemblyLineNumber);
                        var assemblyLineIsX = Util.Dimension.isDimensionX(assemblyConfigLine);
                        var assemblyLineType = Util.Dimension.getDimensionDegreeType(assemblyConfigLine);

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
                appData.append(Main.getCoverAssemblyConfigPath());
                appData.append("\n");

                // generate and write app data
                // if mates will be flipped confirm changes okay first
                if (assemblyConfigNegationLineToFlipStringList.length() > 0) {
                    var assemblyConfigNegationFlipArray = assemblyConfigNegationLineToFlipStringList.toString()
                            .split("!");

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
                                    var distance = line.split("=")[0].split("@")[1]
                                            .replace("\"", "").trim();
                                    appData.append(distance);
                                    appData.append("\n");
                                }
                            }
                        }

                        // write app data
                        Util.Output.writeToConfig(appData.toString(), Main.getRebuildDaemonAppDataPath(),
                                Main.getWritable());
                        Util.Build.rebuild(DaemonProgram.ASSEMBLY_GENERAL, Main.getBuildable());
                        window.dispose();
                    });
                    window.add(confirmButton);

                    var cancelButton = new JButton("Cancel");
                    cancelButton.addActionListener(e1 -> {
                        Util.Output.writeToConfig(appData.toString(), Main.getRebuildDaemonAppDataPath(),
                                Main.getWritable());
                        Util.Build.rebuild(DaemonProgram.ASSEMBLY_GENERAL, Main.getBuildable());
                        window.dispose();
                    });
                    window.add(cancelButton);

                    window.setVisible(true);
                } else {
                    Util.Output.writeToConfig(appData.toString(), Main.getRebuildDaemonAppDataPath(),
                            Main.getWritable());

                    Util.Build.rebuild(DaemonProgram.ASSEMBLY_GENERAL, Main.getBuildable());
                }
            }
        }

        static void handleAngleFrameBoolAction(ActionEvent event) {
            var userInput = event.getActionCommand().isEmpty() ? null : event.getActionCommand();

            // check if user input is not null - if null do nothing
            if (userInput != null) {
                var coverAssemblyConfigLines = FilesUtil.read(Main.getCoverAssemblyConfigPath())
                        .split("\n");

                // get angle frame bool line number and line
                // get current bool
                var isCurrentlyActive = false;
                var variableLineNumber = 0;
                for (String line : coverAssemblyConfigLines) {
                    if (line.contains("2in Angle Frame Bool") &&
                            !line.contains("IIF")) {
                        isCurrentlyActive = coverAssemblyConfigLines[variableLineNumber].split("=")[1]
                                .contains("1");
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
                    Util.Output.writeToConfig(builder.toString(), Main.getCoverAssemblyConfigPath(),
                            Main.getWritable());

                    // rebuild
                    Util.Build.rebuild(DaemonProgram.ASSEMBLY_GENERAL, Main.getBuildable());
                }
            }
        }

        static void handleAngleFrameLength(ActionEvent event, String XZ) {
            var userInput = Util.UserInput.getUserTextInput(event);

            // if user input is not null
            if (userInput != null) {
                var partConfigLines = Util.Path.getLinesFromPath(Main.getAngleFrameConfigPath());

                var lineIdentifier = "\"" + XZ + " Clear Access\"=";

                var lineNumberTable = Util.Map.getSingleLineNumberTable(partConfigLines, lineIdentifier);

                for (int lineNumber : lineNumberTable.keySet()) {
                    var newLine = lineNumberTable.get(lineNumber).split("=")[0].trim() +
                            "= " + userInput + "in";

                    partConfigLines[lineNumber] = newLine;
                }

                // write app data
                Util.Output.writeToConfig(Util.Path.getCoverConfigPath().toString(), Main.getRebuildDaemonAppDataPath(),
                        Main.getWritable());

                // write to assembly config
                var newText = Util.Output.generateWriteOutput(partConfigLines);
                Util.Output.writeToConfig(newText, Main.getAngleFrameConfigPath(), Main.getWritable());

                // send rebuild command
                Util.Build.rebuild(DaemonProgram.ASSEMBLY_GENERAL, Main.getBuildable());
            }
        }
    }

    static class Window {
        static void displayCoverShapeSelector() {
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

        static void displayCoverConfigWindow(String shapeSelection) {
            var window = new JFrame("Cover Configurer");
            window.setLayout(new FlowLayout());
            window.setSize(400, 300);
            window.setLocationRelativeTo(null);
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            Main.setCoverShapeSelection(shapeSelection);

            // if square write to assembly config "Square Center Mark"= 1 else = 0
            Assembly.setAssemblySquareCenterMark(
                    Main.getCoverAssemblyConfigPath(),
                    "Square Center Mark",
                    Main.getRebuildDaemonAppDataPath(),
                    DaemonProgram.BASIC_REBUILD);

            // set cover selection assembly config
            Assembly.setCoverSelectionAssemblyConfig(
                    Main.getCoverShapeAssemblyConfigPath(),
                    Main.getRebuildDaemonAppDataPath(),
                    DaemonProgram.ASSEMBLY_REBUILD
            );

            // read cover config contents and set cover config variable table
            Util.Map.setVariableLineNumberMap(Util.Path.getCoverConfigPath(),
                    Main.getCoverConfigVariableNameLineNumberTable());

            // define total number of hole features to know the number of hole feature buttons to produce
            var holeFeatures = 0;
            for (String variable : Main.getCoverConfigVariableNameLineNumberTable().keySet()) {
                if (!variable.matches("[^0-9]*") && variable.contains("Hole"))
                    holeFeatures = Math.max(Integer.parseInt(variable.split(" ")[1]), holeFeatures);
            }

            // set user input parameters table based on cover config variable table
            Util.UserInput.setVariableUserInputMap(Main.getCoverConfigVariableNameLineNumberTable(),
                    Main.getCoverConfigVariableUserInputTable());

            window.add(Button.coverParamsButton("Base Cover", e -> displayBaseCoverParamsConfigWindow(
                    "Base Cover Parameters", "Cover")));
            for (var i = 1; i <= holeFeatures; ++i) {
                int finalI = i;
                window.add(Button.coverParamsButton("Hole " + i, e -> displayBaseCoverParamsConfigWindow(
                        "Hole " + finalI + " Parameters", "Hole " + finalI)
                ));
            }
            window.add(Button.selectMaterialButton());

            window.add(Button.coverAssemblyConfigureButton());

            window.setVisible(true);
        }

        static void displayBaseCoverParamsConfigWindow(String windowTitle, String variableName) {
            var window = new JFrame(windowTitle);
            window.setSize(300, 900);
            window.setLocationRelativeTo(null);
            window.setLayout(new FlowLayout());
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            window.add(new JLabel("Instructions:"));
            window.add(new JLabel(" After each input press Enter to 'set' the value."));
            window.add(new JLabel(" Click the Build button to generate the model."));

            for (String variable : Main.getCoverConfigVariableNameLineNumberTable().keySet()) {
                if (variable.contains(variableName)) {
                    window.add(new JLabel(variable + ": "));
                    var textInput = new JTextField( 4);
                    textInput.addActionListener(e -> Main.getCoverConfigVariableUserInputTable().put(variable, e.getActionCommand()));
                    window.add(textInput);
                }
            }

            if (!windowTitle.contains("Base Cover"))
                window.add(Button.holeAssemblyConfigButton(variableName));

            window.add(Button.baseCoverParamsBuildButton(variableName));

            window.setVisible(true);
        }

        static void holeAssemblyConfigWindow(String variableName) {
            var window = new JFrame(variableName + " Assembly Configuration");
            window.setSize(300, 400);
            window.setLocationRelativeTo(null);
            window.setLayout(new FlowLayout());
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            var radioButtons = Assembly.holeAssemblyConfigRadios(variableName);
            var buttonGroup = new ButtonGroup();

            for (JRadioButton radioButton : radioButtons) {
                buttonGroup.add(radioButton);
            }

            for (JRadioButton radioButton : radioButtons) {
                window.add(radioButton);
            }

            window.add(Button.inspectionPlateConfigButton(variableName));

            window.add(Button.confirmHoleAssemblyConfigButton(variableName, buttonGroup));

            window.setVisible(true);
        }

        static void displayInspectionPlateConfigWindow() {
            var window = new JFrame("Inspection Plate Configurer");
            window.setSize(300, 300);
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            window.setLocationRelativeTo(null);
            window.setLayout(new FlowLayout());
// gets X/Z offsets from current hole - does this at user clicking 'confirm' in the prior assembly config screen
            window.add(new JLabel("IN04A-25 4 Grips 45deg Bool: "));
            var in0445degBoolBox = new JTextField(1);
            in0445degBoolBox.addActionListener(e -> ActionHandler.assemblyBoolActionHandler(
                    e,
                    "\"IN04A-25 4 Grips 45deg Bool\"=",
                    Main.getInspectionPlateConfigPath()));
            window.add(in0445degBoolBox);

            window.setVisible(true);
        }

        static void displayCoverAssemblyConfigWindow() {
            var window = new JFrame("General Assembly Feature Configurer");
            window.setSize(400, 300);
            window.setLayout(new FlowLayout());
            window.setLocationRelativeTo(null);
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            window.add(Button.handleButton());
            window.add(Button.angleFrameButton());
            window.add(alumFlatBarButton());
            window.add(lockPlateButton());
            window.add(hingesButton());
            window.add(armButton());

            window.setVisible(true);
        }

        static void displayAssemblyHandleConfigWindow() {
            var window = new JFrame("Cover Assembly Handle Configurer");
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            window.setSize(275, 250);
            window.setLocationRelativeTo(null);
            window.setLayout(new FlowLayout());

            // disable config assembly handle if no part config.txt handle is active
            var coverHasHandle = false;
            var partConfigLines = FilesUtil.read(Util.Path.getCoverConfigPath()).split("\n");
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
                textBox.addActionListener(e -> ActionHandler.handleBoolAction(e, "GT Box 0deg"));
                window.add(textBox);

                var GTBox90degLabel = new JLabel("GT Box 90deg Bool: ");
                window.add(GTBox90degLabel);
                var GTBox90degBox = new JTextField(1);
                GTBox90degBox.addActionListener(e -> ActionHandler.handleBoolAction(e, "GT Box 90deg"));
                window.add(GTBox90degBox);

                var handleBoolLabel = new JLabel("Handle 0deg Bool: ");
                window.add(handleBoolLabel);
                var handleBoolTextBox = new JTextField(1);
                handleBoolTextBox.addActionListener(e -> ActionHandler.handleBoolAction(e, "Handle 0deg"));
                window.add(handleBoolTextBox);

                var handle90degBoolLabel = new JLabel("Handle 90deg Bool: ");
                window.add(handle90degBoolLabel);
                var handle90debBox = new JTextField(1);
                handle90debBox.addActionListener(e -> ActionHandler.handleBoolAction(e, "Handle 90deg"));
                window.add(handle90debBox);
            }

            window.setVisible(true);
        }

        static void displayAngleFrameConfigWindow() {
            var window = new JFrame("2in Angle Frame Configurer");
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            window.setSize(300, 300);
            window.setLocationRelativeTo(null);
            window.setLayout(new FlowLayout());

            window.add(new JLabel("2in Angle Frame Bool: "));
            var boolBox = new JTextField(1);
            boolBox.addActionListener(ActionHandler::handleAngleFrameBoolAction);
            window.add(boolBox);

            window.add(new JLabel("2in Angle Frame X Length: "));
            var xLengthBox = new JTextField(2);
            xLengthBox.addActionListener(e -> ActionHandler.handleAngleFrameLength(e, "X"));
            window.add(xLengthBox);

            window.add(new JLabel("2in Angle Frame Z Length: "));
            var zLengthBox = new JTextField(2);
            zLengthBox.addActionListener(e -> ActionHandler.handleAngleFrameLength(e, "Z"));
            window.add(zLengthBox);

            window.add(new JLabel("2in Angle Frame ID Cutaway Bool: "));
            var cutawayBoolInputBox = new JTextField(1);
            cutawayBoolInputBox.addActionListener(e -> ActionHandler.assemblyBoolActionHandler(
                    e,"Angle Frame ID Cutaway Bool"));
            window.add(cutawayBoolInputBox);

            window.add(new JLabel("Angle Frame ID Cutaway Diameter: "));
            var cutawayDiameterInputBox = new JTextField(2);
            cutawayDiameterInputBox.addActionListener(e -> ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Angle Frame ID Cutaway Diameter\"=", "in"));
            window.add(cutawayDiameterInputBox);

            window.add(new JLabel("Angle Frame Placement Z Offset: "));
            var angleFramePlacementZOffset = new JTextField(2);
            angleFramePlacementZOffset.addActionListener(e -> ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Angle Frame Placement Z Offset\"=", "in"));
            window.add(angleFramePlacementZOffset);

            window.setVisible(true);
        }
    }

    static class Assembly {
        static void setAssemblySquareCenterMark(Path assemblyConfigPath,
                                                String identifier,
                                                Path appDataPath,
                                                DaemonProgram daemonProgram) {
            var configLines = Util.Path.getLinesFromPath(assemblyConfigPath);
            var index = 0;
            var writeNeeded = false;

            // set config line square center mark based on current setting and user shape selection
            for (String line : configLines) {
                if (line.contains(identifier) && !line.contains("IIF")) {
                    var currentStateIsOne = line.contains("1");
                    var selectionIsSquare = Main.getCoverShapeSelection().contains("Square");
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
                var newText = Util.Output.generateWriteOutput(configLines);

                Util.Output.writeToConfig(newText, assemblyConfigPath, Main.getWritable());

                Util.Output.writeToConfig(assemblyConfigPath.toString(),
                        appDataPath,
                        Main.getWritable());

                Util.Build.rebuild(daemonProgram, Main.getBuildable());
            }
        }

        static void setCoverSelectionAssemblyConfig(Path coverShapeConfigPath,
                                                    Path appDataPath,
                                                    DaemonProgram daemonProgram) {
            // get config lines
            var configLines = FilesUtil.read(coverShapeConfigPath).split("\n");

            // set new value from user cover shape selection
            for (var i = 0; i < configLines.length; ++i) {
                if (configLines[i].contains("Configuration") &&
                        !configLines[i].contains("IIF")) {
                    configLines[i] = "Configuration = " + (Main.getCoverShapeSelection().contains("Square") ? "2" : "1");
                }
            }

            // write new value to cover shape assembly config
            var builder = new StringBuilder();
            for (String line : configLines) {
                builder.append(line);
                builder.append("\n");
            }
            Util.Output.writeToConfig(builder.toString(), coverShapeConfigPath, Main.getWritable());

            // set rebuild.txt app data to cover shape assembly config path
            Util.Output.writeToConfig(coverShapeConfigPath.toString(),
                    appDataPath,
                    Main.getWritable());

            // call assembly rebuild daemon
            Util.Build.rebuild(daemonProgram, Main.getBuildable());
        }

        static JRadioButton[] holeAssemblyConfigRadios(String variableName) {
            var featureStringList = new StringBuilder();
            featureStringList.append("none");
            featureStringList.append("!");
            var holePath = Util.Path.getHolePath(variableName);

            var assemblyConfigLines = FilesUtil.read(holePath).split("\n");

            for (String line : assemblyConfigLines) {
                if (line.contains("Bool") && !line.contains("IIF")) {
                    var feature = line.split("=")[0].replace("Bool", "")
                            .replace("\"", "");
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

        static int getHoleNumber(String line) {
            var holeNumber = 0;
            if (line.contains("Hole")) {
                holeNumber = Integer.parseInt(line.split(" ")[1].trim());
            }
            return holeNumber;
        }
    }
}
