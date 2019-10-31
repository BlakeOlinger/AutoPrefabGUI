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

        static JButton circleSelectButton() {
            var button = new JButton("Circular");
            button.addActionListener(e -> Window.displayCoverConfigWindow("Circular"));
            return button;
        }

        static JButton squareSelectButton() {
            var button = new JButton("Square");
            button.addActionListener(e -> Window.displayCoverConfigWindow("Square"));
            return button;
        }

        static JButton hatch90degButton() {
            var button = new JButton("Hatch 90deg");
            button.addActionListener(e -> ActionHandler.setHatchOrientation(true));
            return button;
        }

        static JButton hatch0degButton() {
            var button = new JButton("Hatch 0deg");
            button.addActionListener(e -> ActionHandler.setHatchOrientation(false));
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
            button.addActionListener(e -> ActionHandler.writeBaseCoverChanges(variableName));
            return button;
        }

        static JButton selectMaterialButton() {
            var button = new JButton("Material");
            button.addActionListener(e -> Window.displaySelectMaterialWindow());
            return button;
        }

        static JButton materialConfigButton(String material) {
            var button = new JButton(material);
            button.addActionListener(e -> ActionHandler.writeMaterialConfig(material));
            return button;
        }

        static JButton coverAssemblyConfigureButton() {
            var button = new JButton("Cover Assembly Configurer");
            button.addActionListener(e -> Window.displayCoverAssemblyConfigWindow());
            return button;
        }

        static JButton inspectionPlateConfigButton() {
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

                // for user selection and variable name automatically configure *.SLDPRT dimensions
                // diameter first - write only
                SLDPRT.writeHoleDiameterOnRadioSelect(variableName, userSelection);

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
                var partConfigLines = FilesUtil.read(Util.Path.getCoverShapeConfigPath()).split("\n");
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
            var button = new JButton("Handle/Box Config");
            button.addActionListener(e -> Window.displayHandleBoxConfigWindow());
            return button;
        }

        static JButton handleConfig() {
            var button = new JButton("Handle Config");
            button.addActionListener(e -> Window.displayHandleConfigWindow());
            return button;
        }

        static JButton angleFrameButton() {
            var button = new JButton("2in Angle Frame");
            button.addActionListener(e -> Window.displayAngleFrameConfigWindow());
            return button;
        }

        static JButton alumFlatBarButton() {
            var button = new JButton("Alum Flat Bar");
            button.addActionListener(e -> Window.displayAlumFlatConfigWindow());
            return button;
        }

        static JButton lockPlateButton() {
            var button = new JButton("Lock Plate");
            button.addActionListener(e -> Window.displayLockPlateConfigWindow());
            return button;
        }

        static JButton hingesButton() {
            var button = new JButton("Hinges");
            button.addActionListener(e -> Window.displayHingesConfigWindow());
            return button;
        }

        static JButton armButton() {
            var button = new JButton("Hatch Arm");
            button.addActionListener(e -> Window.displayHatchArmConfigWindow());
            return button;
        }

        static JButton flipAssemblyMateButton(String mate, Path path) {
            var button = new JButton("Mate Flip");
            button.addActionListener(e -> ActionHandler.handleSingleMateFlip(mate, path));
            return button;
        }
    }

    static class ActionHandler {
        static void assemblyBoolActionHandler(ActionEvent event, String line, Path configPath) {
            var userInput = Util.UserInput.getUserTextInput(event);
            if (userInput != null) {
                var assemblyConfigLines = Util.Path.getLinesFromPath(configPath);
                var boolLineNumberTable = Util.Map.getLineNumberTable(assemblyConfigLines, line);

                for (int lineNumber : boolLineNumberTable.keySet()) {
                    var newLine = Util.UserInput.getNewLinesFromUserInput(assemblyConfigLines[lineNumber],
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
                var boolLineNumberTable = Util.Map.getLineNumberTable(assemblyConfigLines, line);

                for (int lineNumber : boolLineNumberTable.keySet()) {
                    var newLine = Util.UserInput.getNewLinesFromUserInput(assemblyConfigLines[lineNumber],
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

        static void assemblyDimensionActionHandler(ActionEvent event, String line) {
            // the two booleans are going to point to calls to external methods
            var userInput = Util.UserInput.getUserTextInput(event);
            if (userInput != null) {
                var assemblyConfigLines = Util.Path.getLinesFromPath(Main.getCoverAssemblyConfigPath());
                var dimensionLineNumberTable = Util.Map.getLineNumberTable(assemblyConfigLines, line);

                for (int lineNumber : dimensionLineNumberTable.keySet()) {
                    var newLine = Util.UserInput.getNewLinesFromUserInput(assemblyConfigLines[lineNumber],
                            userInput, "in");

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

        static void assemblyDimensionActionHandler(ActionEvent event, String line, Path configPath) {
            // the two booleans are going to point to calls to external methods
            var userInput = Util.UserInput.getUserTextInput(event);
            if (userInput != null) {
                var assemblyConfigLines = Util.Path.getLinesFromPath(configPath);
                var dimensionLineNumberTable = Util.Map.getLineNumberTable(assemblyConfigLines, line);

                for (int lineNumber : dimensionLineNumberTable.keySet()) {
                    var newLine = Util.UserInput.getNewLinesFromUserInput(assemblyConfigLines[lineNumber],
                            userInput, "in");

                    assemblyConfigLines[lineNumber] = newLine;
                }

                // write app data
                Util.Output.writeToConfig(configPath.toString(), Main.getRebuildDaemonAppDataPath(),
                        Main.getWritable());

                // generate and write config.txt data
                var newText = Util.Output.generateWriteOutput(assemblyConfigLines);
                Util.Output.writeToConfig(newText, configPath, Main.getWritable());

                // call rebuild daemon
                Util.Build.rebuild(DaemonProgram.ASSEMBLY_GENERAL, Main.getBuildable());
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

                var lineNumberTable = Util.Map.getLineNumberTable(partConfigLines, lineIdentifier);

                for (int lineNumber : lineNumberTable.keySet()) {
                    var newLine = lineNumberTable.get(lineNumber).split("=")[0].trim() +
                            "= " + userInput + "in";

                    partConfigLines[lineNumber] = newLine;
                }

                // write app data
                Util.Output.writeToConfig(Util.Path.getCoverShapeConfigPath().toString(), Main.getRebuildDaemonAppDataPath(),
                        Main.getWritable());

                // write to assembly config
                var newText = Util.Output.generateWriteOutput(partConfigLines);
                Util.Output.writeToConfig(newText, Main.getAngleFrameConfigPath(), Main.getWritable());

                // send rebuild command
                Util.Build.rebuild(DaemonProgram.ASSEMBLY_GENERAL, Main.getBuildable());
            }
        }

        static void writeMaterialConfig(String material) {
            // get path for selected cover shape
            var coverConfigPath = Util.Path.getCoverShapeConfigPath();
            var configLines = Util.Path.getLinesFromPath(coverConfigPath);

            // line number map for "Material"
            var materialLineNumberMap = Util.Map.getLineNumberTable(
                    configLines,
                    new HashMap<>(),
                    0,
                    "Material"
            );

            var materialCode = Main.getMaterialConfigTable().get(material);

            var newLines = Util.UserInput.getNewLinesFromUserInput(materialLineNumberMap, configLines, materialCode);

            var writeOutput = Util.Output.generateWriteOutput(newLines);

            // write material config to selected config.txt file
            Util.Output.writeToConfig(writeOutput, Util.Path.getCoverShapeConfigPath(),
                    Main.getWritable());

            // call aluminum handle macro - tell macro if material is aluminum - material code "1"
            SLDPRT.writeHandleOrientationBool(materialCode.contains("1"));

            // write selected config.txt path to rebuild.txt app data
            Util.Output.writeToConfig(Util.Path.getCoverShapeConfigPath().toString(),
                    Main.getRebuildDaemonAppDataPath(), Main.getWritable());

            // call rebuild for AutoMaterialConfig.appref-ms
            Util.Build.rebuild(DaemonProgram.MATERIAL_CONFIG, Main.getBuildable());
        }

        static void writeBaseCoverChanges(String variableName) {
            var coverConfigPath = Main.getCoverShapeSelection().contains("Square") ?
                    Main.getSquareCoverConfigPath() : Main.getCoverConfigPath();
            var coverConfigContentLines = FilesUtil.read(coverConfigPath).split("\n");

            // gets cover variables - user input and appends the line with the changed value to the lines array
            for (String userInputVariable : Main.getCoverConfigVariableUserInputTable().keySet()) {
                if (userInputVariable.contains(variableName) &&
                        !Main.getCoverConfigVariableUserInputTable().get(userInputVariable).isEmpty()) {
                    var variableLineNumber = Main.getCoverConfigVariableNameLineNumberTable().get(userInputVariable);
                    var lineSuffix = "";
                    if (coverConfigContentLines[variableLineNumber].contains("in")) {
                        lineSuffix = "in";
                    } else if (coverConfigContentLines[variableLineNumber].contains("deg") &&
                            !coverConfigContentLines[variableLineNumber].contains("Bool")) {
                        lineSuffix = "deg";
                    }

                    var userVariable = Main.getCoverConfigVariableUserInputTable().get(userInputVariable);

                    var newLine = userInputVariable + "= " + userVariable + lineSuffix;
                    coverConfigContentLines[variableLineNumber] = newLine;
                }
            }


            // check for variableName matching user input and if null - generate user input table for this variableName
            var offsetUserInputTable = new HashMap<String, String>();
            for (String userInput : Main.getCoverConfigVariableUserInputTable().keySet()) {
                if (userInput.contains(variableName) && userInput.contains("Offset") &&
                        !userInput.contains("Degree")) {
                    var userOffset = Main.getCoverConfigVariableUserInputTable().get(userInput).isEmpty() ?
                            "null" : Main.getCoverConfigVariableUserInputTable().get(userInput);
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
            Util.Output.writeToConfig(builder.toString(), coverConfigPath, Main.getWritable());

            // write path app data to rebuild.txt
            Util.Output.writeToConfig(coverConfigPath.toString(), Main.getRebuildDaemonAppDataPath(),
                    Main.getWritable());

            // call auto-rebuild daemon
            Util.Build.rebuild(DaemonProgram.PART_REBUILD, Main.getBuildable());
        }

        static void handleSingleMateFlip(String mate, Path path) {
            var configLines = Util.Path.getLinesFromPath(path);
            var distanceLine = Util.Dimension.getMateDistanceLine(configLines, mate);

            var appData = path + "\n" + distanceLine;

            Util.Output.writeToConfig(appData, Main.getRebuildDaemonAppDataPath(),
                    Main.getWritable());

            Util.Build.rebuild(DaemonProgram.ASSEMBLY_REBUILD, Main.getBuildable());
        }

        static void handleRadioGroup(ButtonGroup buttonGroup) {
            var configLines = Util.Path.getLinesFromPath(Main.getHandleConfigPath());
            var userSelection = buttonGroup.getSelection().getActionCommand();

            for (var i = 0; i < configLines.length; ++i) {
                var line = configLines[i];
                if (line.contains(userSelection) && !line.contains("IIF")) {
                    configLines[i] = Util.UserInput.getNewLinesFromUserInput(line, "1", "");
                } else if (!line.contains(userSelection) && !line.contains("IIF")){
                    configLines[i] = Util.UserInput.getNewLinesFromUserInput(line, "0", "");
                }
            }

            var newLines = Util.Output.generateWriteOutput(configLines);

            Util.Output.writeToConfig(newLines, Main.getHandleConfigPath(), Main.getWritable());

            Util.Build.rebuild(DaemonProgram.ASSEMBLY_REBUILD, Main.getBuildable());
        }

        static void setHatchOrientation(boolean is90deg) {
            // set skeleton config hatch orientation to 1 for is90deg true else 0

            // get skeleton config path and lines
            var skeletonConfigPath = Main.getSkeletonConfigPath();
            var skeletonConfigLines = Util.Path.getLinesFromPath(skeletonConfigPath);

            // get line number map for "Hatch Orientation"
            var orientationLineNumberMap = Util.Map.getLineNumberTable(
                    skeletonConfigLines,
                    "Hatch Orientation"
            );

            // set user selected output
            var orientation = is90deg ? "1" : "0";

            var newLines = Util.UserInput.getNewLinesFromUserInput(
                    orientationLineNumberMap, skeletonConfigLines, orientation);

            var writeOutput = Util.Output.generateWriteOutput(newLines);

            Util.Output.writeToConfig(writeOutput, skeletonConfigPath, Main.getWritable());
        }
    }

    static class Window {
        static void displayCoverShapeSelector() {
            var window = new JFrame("Select Shape");
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            window.setSize(200, 150);
            window.setLocationRelativeTo(null);
            window.setLayout(new FlowLayout());

            window.add(Button.circleSelectButton());
            window.add(Button.squareSelectButton());

            window.add(Button.hatch90degButton());
            window.add(Button.hatch0degButton());

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
                    Main.getRebuildDaemonAppDataPath()
            );

            // set cover selection assembly config
            Assembly.setCoverSelectionAssemblyConfig(
                    Main.getCoverShapeAssemblyConfigPath(),
                    Main.getRebuildDaemonAppDataPath()
            );

            // read cover config contents and set cover config variable table
            Util.Map.setVariableLineNumberMap(Util.Path.getCoverShapeConfigPath(),
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

            var radioButtons = Assembly.getRadioButtons(variableName);
            var buttonGroup = new ButtonGroup();

            for (JRadioButton radioButton : radioButtons) {
                buttonGroup.add(radioButton);
            }

            for (JRadioButton radioButton : radioButtons) {
                window.add(radioButton);
            }

            window.add(Button.inspectionPlateConfigButton());

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
            window.add(Button.alumFlatBarButton());
            window.add(Button.lockPlateButton());
            window.add(Button.hingesButton());
            window.add(Button.armButton());

            window.setVisible(true);
        }

        static void displayHandleBoxConfigWindow() {
            var window = new JFrame("Cover Assembly Handle Configurer");
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            window.setSize(275, 250);
            window.setLocationRelativeTo(null);
            window.setLayout(new FlowLayout());

            window.add(new JLabel("GT Box 0deg Bool: "));
            var textBox = new JTextField(1);
            textBox.addActionListener(e -> ActionHandler.assemblyBoolActionHandler(e,
                    "\"GT Box 0deg Bool\"=",
                    Main.getCoverAssemblyConfigPath()));
            window.add(textBox);

            window.add(new JLabel("GT Box 0deg X Offset: "));
            var textBox0degXOffset = new JTextField(2);
            textBox0degXOffset.addActionListener(e -> ActionHandler.assemblyDimensionActionHandler(e,
                    "\"GT Box 0deg X Offset\"="));
            window.add(textBox0degXOffset);
            window.add(Button.flipAssemblyMateButton("GT Box 0deg X Offset",
                    Main.getCoverAssemblyConfigPath()));

            window.add(new JLabel("GT Box 0deg Z Offset: "));
            var textBox0degZOffset = new JTextField(2);
            textBox0degZOffset.addActionListener(e -> ActionHandler.assemblyDimensionActionHandler(e,
                    "\"GT Box 0deg Z Offset\"="));
            window.add(textBox0degZOffset);
            window.add(Button.flipAssemblyMateButton("GT Box 0deg Z Offset",
                    Main.getCoverAssemblyConfigPath()));

            var GTBox90degLabel = new JLabel("GT Box 90deg Bool: ");
            window.add(GTBox90degLabel);
            var GTBox90degBox = new JTextField(1);
            GTBox90degBox.addActionListener(e -> ActionHandler.assemblyBoolActionHandler(e,
                    "\"GT Box 90deg Bool\"=",
                    Main.getCoverAssemblyConfigPath()));
            window.add(GTBox90degBox);

            window.add(new JLabel("GT Box 90deg X Offset: "));
            var textBox90degXOffset = new JTextField(2);
            textBox90degXOffset.addActionListener(e -> ActionHandler.assemblyDimensionActionHandler(e,
                    "\"GT Box 90deg X Offset\"="));
            window.add(textBox90degXOffset);
            window.add(Button.flipAssemblyMateButton("GT Box 90deg X Offset",
                    Main.getCoverAssemblyConfigPath()));

            window.add(new JLabel("GT Box 90deg Z Offset: "));
            var textBox90degZOffset = new JTextField(2);
            textBox90degZOffset.addActionListener(e -> ActionHandler.assemblyDimensionActionHandler(e,
                    "\"GT Box 90deg Z Offset\"="));
            window.add(textBox90degZOffset);
            window.add(Button.flipAssemblyMateButton("GT Box 90deg Z Offset",
                    Main.getCoverAssemblyConfigPath()));

            window.add(Button.handleConfig());

            window.setVisible(true);
        }

        static void displayHandleConfigWindow() {
            var window = new JFrame("Handle Config");
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            window.setSize(300, 300);
            window.setLocationRelativeTo(null);
            window.setLayout(new FlowLayout());

            // generate radio buttons using hole radio button generator for handle config
            var radioButtons = Assembly.getRadioButtons(Main.getHandleConfigPath());
            var buttonGroup = new ButtonGroup();

            for (JRadioButton radioButton : radioButtons) {
                buttonGroup.add(radioButton);
            }

            for (JRadioButton radioButton : radioButtons) {
                radioButton.addActionListener(e -> ActionHandler.handleRadioGroup(buttonGroup));
                window.add(radioButton);
            }

            window.add(new JLabel("Handle X Offset: "));
            var textBox0degXOffset = new JTextField(2);
            textBox0degXOffset.addActionListener(e -> ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Cover Hatch Handle X Offset\"="));
            window.add(textBox0degXOffset);
            window.add(Button.flipAssemblyMateButton("Cover Hatch Handle X Offset",
                    Main.getCoverAssemblyConfigPath()));

            window.add(new JLabel("Handle Z Offset: "));
            var textBox0degZOffset = new JTextField(2);
            textBox0degZOffset.addActionListener(e -> ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Cover Hatch Handle Z Offset\"="));
            window.add(textBox0degZOffset);
            window.add(Button.flipAssemblyMateButton("Cover Hatch Handle Z Offset",
                    Main.getCoverAssemblyConfigPath()));

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
                    "\"Angle Frame ID Cutaway Diameter\"="));
            window.add(cutawayDiameterInputBox);

            window.add(new JLabel("Angle Frame Placement Z Offset: "));
            var angleFramePlacementZOffset = new JTextField(2);
            angleFramePlacementZOffset.addActionListener(e -> ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Angle Frame Placement Z Offset\"="));
            window.add(angleFramePlacementZOffset);
            window.add(Button.flipAssemblyMateButton("Angle Frame Placement Z Offset",
                    Main.getCoverAssemblyConfigPath()));

            window.setVisible(true);
        }

        static void displayAlumFlatConfigWindow() {
            var window = new JFrame("Assembly Alum Flat Bar Config");
            window.setSize(300, 300);
            window.setLayout(new FlowLayout());
            window.setLocationRelativeTo(null);
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            window.add(new JLabel("Alum Flat 0deg Bool: "));
            var alumFlatBoolBox = new JTextField(1);
            alumFlatBoolBox.addActionListener(e -> Cover.ActionHandler.assemblyBoolActionHandler(e,
                    "\"Aluminum Flat Bar 0deg Bool\"="));
            window.add(alumFlatBoolBox);

            window.add(new JLabel("Alum Flat 90deg Bool: "));
            var alumFlat90BoolBox = new JTextField(1);
            alumFlat90BoolBox.addActionListener(e -> Cover.ActionHandler.assemblyBoolActionHandler(e,
                    "\"Aluminum Flat Bar 90deg Bool\"="));
            window.add(alumFlat90BoolBox);

            window.add(new JLabel("Alum Flat Bar X Length: "));
            var alumFlatLengthX = new JTextField(2);
            alumFlatLengthX.addActionListener(e -> ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Length X\"=", Main.getAlumFlatBarConfigPath()));
            window.add(alumFlatLengthX);

            window.add(new JLabel("Alum Flat Bar Z Length: "));
            var alumFlatLengthZ = new JTextField(2);
            alumFlatLengthZ.addActionListener(e -> ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Length Z\"=", Main.getAlumFlatBarConfigPath()));
            window.add(alumFlatLengthZ);

            window.add(new JLabel("Alum Flat Bar Placement Z Offset: "));
            var alumFlatBarPlacementZOffset = new JTextField(2);
            alumFlatBarPlacementZOffset.addActionListener(e -> ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Aluminum Flat Bar 90deg Placement Z Offset\"=",
                    Main.getCoverAssemblyConfigPath()));
            window.add(alumFlatBarPlacementZOffset);

            window.setVisible(true);
        }

        static void displayLockPlateConfigWindow() {
            var window = new JFrame("Lock Plate Configurer");
            window.setSize(300, 300);
            window.setLocationRelativeTo(null);
            window.setLayout(new FlowLayout());
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            window.add(new JLabel("Lock Plate Bool: "));
            var textBox = new JTextField(1);
            textBox.addActionListener(e -> Cover.ActionHandler.assemblyBoolActionHandler(e,
                    "\"Hatch Lock Plate Bool\"="));
            window.add(textBox);

            window.add(new JLabel("Lock Plate Z Offset: "));
            var offsetBox = new JTextField(2);
            offsetBox.addActionListener(e -> Cover.ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Hatch Lock Plate Z Offset\"="));
            window.add(offsetBox);

            window.setVisible(true);
        }

        static void displayHingesConfigWindow() {
            var window = new JFrame("Hinge Configurer");
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            window.setSize(300, 300);
            window.setLocationRelativeTo(null);
            window.setLayout(new FlowLayout());

            window.add(new JLabel("Bolt-In Hinges Bool: "));
            var boolBox = new JTextField(1);
            boolBox.addActionListener(e -> Cover.ActionHandler.assemblyBoolActionHandler(e,
                    "\"Bolt-In Hinge Bool\"="));
            window.add(boolBox);

            window.add(new JLabel("Bolt-In Hinges Z Offset: "));
            var zOffset = new JTextField(2);
            zOffset.addActionListener(e -> Cover.ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Bolt-In Hinge Z Offset\"="));
            window.add(zOffset);

            window.add(new JLabel("Bolt-In Hinge 1 X Offset: "));
            var xOneOffset = new JTextField(2);
            xOneOffset.addActionListener(e -> Cover.ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Bolt-In Hinge 1 X Offset\"="));
            window.add(xOneOffset);

            window.add(new JLabel("Bolt-In Hinge 2 X Offset: "));
            var xTwoOffset = new JTextField(2);
            xTwoOffset.addActionListener(e -> Cover.ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Bolt-In Hinge 2 X Offset\"="));
            window.add(xTwoOffset);

            window.setVisible(true);
        }

        static void displayHatchArmConfigWindow() {
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
            xOffset.addActionListener(e -> Cover.ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Arm X Offset\"="));
            window.add(xOffset);

            window.add(new JLabel("Hatch Arm Z Offset: "));
            var zOffset = new JTextField(2);
            zOffset.addActionListener(e -> Cover.ActionHandler.assemblyDimensionActionHandler(e,
                    "\"Arm Z Offset\"="));
            window.add(zOffset);

            window.setVisible(true);
        }

        static void displaySelectMaterialWindow() {
            var window = new JFrame("Select Material");
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            window.setSize(300, 300);
            window.setLayout(new FlowLayout());
            window.setLocationRelativeTo(null);

            window.add(Button.materialConfigButton("ASTM A36 Steel"));
            window.add(Button.materialConfigButton("6061 Alloy"));

            window.setVisible(true);
        }
    }

    static class Assembly {
        static void setAssemblySquareCenterMark(Path assemblyConfigPath,
                                                Path appDataPath) {
            var configLines = Util.Path.getLinesFromPath(assemblyConfigPath);
            var index = 0;
            var writeNeeded = false;

            // set config line square center mark based on current setting and user shape selection
            for (String line : configLines) {
                if (line.contains("Square Center Mark") && !line.contains("IIF")) {
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

                Util.Build.rebuild(DaemonProgram.BASIC_REBUILD, Main.getBuildable());
            }
        }

        static void setCoverSelectionAssemblyConfig(Path coverShapeConfigPath,
                                                    Path appDataPath) {
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
            Util.Build.rebuild(DaemonProgram.ASSEMBLY_REBUILD, Main.getBuildable());
        }

        static JRadioButton[] getRadioButtons(String variableName) {
            var featureStringList = new StringBuilder();
            featureStringList.append("none");
            featureStringList.append("!");
            var holePath = Util.Path.getHolePath(variableName);

            var assemblyConfigLines = FilesUtil.read(holePath).split("\n");

            return getJRadioButtons(featureStringList, assemblyConfigLines);
        }

        static JRadioButton[] getRadioButtons(Path path) {
            var featureStringList = new StringBuilder();
            featureStringList.append("none");
            featureStringList.append("!");

            var configLines = Util.Path.getLinesFromPath(path);

            return getJRadioButtons(featureStringList, configLines);
        }

        private static JRadioButton[] getJRadioButtons(StringBuilder featureStringList, String[] configLines) {
            for (String line : configLines) {
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
                var formattedFeature = Util.Output.removeNoneASCIIChars(feature);

                radioButtonArray[index] = new JRadioButton(formattedFeature);
                radioButtonArray[index].setActionCommand(formattedFeature);
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
// TODO - FINISH THIS
    static class SLDPRT {
        static void writeHoleDiameterOnRadioSelect(String holeNumber, String userSelection) {
            // TODO - for hole number and user selection define diameter based on lookup table file
            //  - if user selection is none write hole bool = 0 - else make sure hole bool = 1

            // get path for selected cover shape
            var coverConfigPath = Util.Path.getCoverShapeConfigPath();
            var coverConfigLines = Util.Path.getLinesFromPath(coverConfigPath);

            // for the hole number get line number map for hole booleans
            var boolLineNumberMap = Util.Map.getLineNumberTable(
                    coverConfigLines,
                    new HashMap<>(),
                    0,
                    holeNumber, "Bool");

            // write only - set config lines bool values
            var boolConfigLines = Util.Output.setBoolLines(
                    coverConfigLines,
                    boolLineNumberMap,
                    userSelection.contains("none"));
        }

        static void writeHandleOrientationBool(boolean isAluminum) {
            // get path for selected cover shape
            var coverConfigPath = Util.Path.getCoverShapeConfigPath();
            var coverConfigLines = Util.Path.getLinesFromPath(coverConfigPath);

            // get bool line map from cover part config for hatch handle
            var handleBoolLineNumberMap = Util.Map.getLineNumberTable(
                    coverConfigLines,
                    new HashMap<>(),
                    0,
                    "Handle", "Bool"
            );

            // get hatch orientation - 90deg versus 0deg
            var hatchIs90deg = Util.Configuration.hatchIs90deg();

            // if hatch is 90 write 0 to 0deg 1 to 90deg and vice versa
            for (int lineNumber : handleBoolLineNumberMap.keySet()) {
                var line = handleBoolLineNumberMap.get(lineNumber);
                var newLine = "";

                if (isAluminum) {

                    if (line.contains("9")) {
                        if (hatchIs90deg) {
                            newLine = Util.UserInput.getNewLinesFromUserInput(line, "1");
                        } else {
                            newLine = Util.UserInput.getNewLinesFromUserInput(line, "0");
                        }
                    } else {
                        if (hatchIs90deg) {
                            newLine = Util.UserInput.getNewLinesFromUserInput(line, "0");
                        } else {
                            newLine = Util.UserInput.getNewLinesFromUserInput(line, "1");
                        }
                    }
                } else {
                    newLine = Util.UserInput.getNewLinesFromUserInput(line, "0");
                }

                handleBoolLineNumberMap.put(lineNumber, newLine);
            }

            var boolConfigLines = Util.Output.getLinesMapSwap(handleBoolLineNumberMap, coverConfigLines);

            var writeOutput = Util.Output.generateWriteOutput(boolConfigLines);

            Util.Output.writeToConfig(writeOutput, coverConfigPath, Main.getWritable());

            Util.Build.rebuild(DaemonProgram.BASIC_REBUILD, Main.getBuildable());
        }
    }
}
