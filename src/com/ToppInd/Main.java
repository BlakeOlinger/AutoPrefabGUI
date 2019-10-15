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
        button.addActionListener(e -> {
            try {
                var buildDrawingDaemonProcess = new ProcessBuilder("cmd.exe", "/c", "AutoPrefabDaemon.bat").start();
                buildDrawingDaemonProcess.waitFor();
                buildDrawingDaemonProcess.destroy();
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });
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

        // populate coverParamsButton() arguments and number by reading the selected shape's config.txt file and extracting relevant variables

        coverShapeSelection = shapeSelection;

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
// TODO - configure rebuild() to take ENUM argument representing the specific daemon to call
    // TODO - write to rebuild.txt on material config selection
    // TODO - finish having material config selection write to the appropriate config.txt file
    // TODO - have C# daemon read the "Material"= # line and configure the *.SLDPRT file material accordingly
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
        FilesUtil.write(builder.toString(), coverShapeSelection.contains("Square") ? SQUARE_COVER_CONFIG_PATH : COVER_CONFIG_PATH);

        // write selected config.txt path to rebuild.txt app data
        var path = coverShapeSelection.contains("Square") ? SQUARE_COVER_CONFIG_PATH : COVER_CONFIG_PATH;
        FilesUtil.write(path.toString(), REBUILD_DAEMON_APP_DATA_PATH);

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

    private static JButton confirmHoleAssemblyConfigButton(String variableName, ButtonGroup buttonGroup) {
        var button = new JButton("Confirm");
        button.addActionListener(e -> {
            // TODO - include variableName in the section that updates lines so that only the correct hole # is updated
            // read blob.L2_cover.txt file and split into lines
            var configContentLines = FilesUtil.read(COVER_ASSEMBLY_CONFIG_PATH).split("\n");
// FIXME - for any write to *.txt config file - write 1 of three states; ! - rebuild as negate - write negate; "" - rebuild write positive/don't overwrite; (-) - rebuild positive write negative
//  - this way it's possible to determine if the negation is versus a prior negative state or not -
            // get line - line number for variables pairs
            var variableLineNumberTable = new HashMap<String, Integer>();

            // check if line doesn't contain an '@' character or "IIF"
            // if not *.put(that line split("=")[0], index)
            var xOffset = "";
            var zOffset = "";
            var index = 0;
            for (String line : configContentLines) {
                if (!line.contains("@") && !line.contains("IIF") &&
                line.contains("Bool")) {
                    var boolVariable = line.split("=")[0];
                    variableLineNumberTable.put(boolVariable, index);
                } else if (!line.contains("@") && !line.contains("IIF") &&
                line.contains("Offset")) {
                    var offset = line.split("=")[0].trim();
                    variableLineNumberTable.put(offset, index);
                    if (offset.contains("X")) {
                        xOffset = offset;
                    } else {
                        zOffset = offset;
                    }
                }
                ++index;
            }
            var userSelection = buttonGroup.getSelection().getActionCommand();

            // define set of user input variables
            var readCount = 0;
            var newVariableTable = new HashMap<String, String>();
            for (String variable : variableLineNumberTable.keySet()) {
                if (!variable.contains("Offset")) {
                    newVariableTable.put(variable, variable.contains(userSelection) ? "1" : "0");
                } else {
                    // if variable contains Offset - read from blob.cover.txt for the variableName hole number and get the hole # X/Z CA Offset values
                    // put that in the value of the variable
                    if (readCount == 0) {
                        var baseCoverConfigLines = FilesUtil.read(COVER_CONFIG_PATH).split("\n");
                        for (String line : baseCoverConfigLines) {
                            var segments = line.split("=");
                            var firstSegment = segments[0];
                            var secondSegment = segments[1];
                            if (firstSegment.contains(variableName) && firstSegment.contains("Offset") &&
                                    firstSegment.contains("CA")) {
                                var putVariable = "";
                                if (line.contains("X")) {
                                    putVariable = xOffset;
                                } else {
                                    putVariable = zOffset;
                                }
                                newVariableTable.put(putVariable, secondSegment.trim());
                            }
                        }
                        ++readCount;
                    }
                }
            }

            // write config to config.txt file
            var builder = new StringBuilder();
            for (String line : configContentLines) {
                builder.append(line);
                builder.append("\n");
            }

            // write new config
            FilesUtil.write(builder.toString(), COVER_ASSEMBLY_CONFIG_PATH);

            // TODO - extract assembly rebuild daemon from part rebuild daemon
            // call rebuild daemon
            writeAssemblyMatesToFlip(newVariableTable, configContentLines, variableLineNumberTable);
            rebuild(DaemonProgram.REBUILD);
        });
        return button;
    }

    private static JRadioButton[] holeAssemblyConfigRadios() {
        var pf150s0deg = new JRadioButton("PF150S 0deg");
        pf150s0deg.setActionCommand("PF150S 0deg");
        var pf200t90deg = new JRadioButton("PF200T 90deg");
        pf200t90deg.setActionCommand("PF200T 90deg");
        var ecg2 = new JRadioButton("ECG 2 Hole");
        ecg2.setActionCommand("ECG 2 Hole");
        var inspection10in45degBC = new JRadioButton("10in Inspection Plate 45deg BC");
        inspection10in45degBC.setActionCommand("10in Inspection Plate 45deg BC");
        var none = new JRadioButton("none");
        none.setActionCommand("none");

        return new JRadioButton[]{
                pf150s0deg,
                pf200t90deg,
                ecg2,
                inspection10in45degBC,
                none
        };
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
                var newLine = userInputVariable + "= " + coverConfigVariableUserInputTable.get(userInputVariable) +
                        lineSuffix;
                coverConfigContentLines[variableLineNumber] = newLine;
            }
        }

        // write updated content to file
        var builder = new StringBuilder();
        for (String line : coverConfigContentLines) {
            builder.append(line);
            builder.append("\n");
        }
        FilesUtil.write(builder.toString(), coverConfigPath);

        // write to rebuild.txt which file to look for negative values in
        FilesUtil.write(coverConfigPath.toString(), REBUILD_DAEMON_APP_DATA_PATH);

        // call C# auto-rebuild daemon
        rebuild(DaemonProgram.REBUILD);
    }

    private static void writeAssemblyMatesToFlip(HashMap<String, String> newVariableTable,
                                        String[] configContentLines,
                                        HashMap<String, Integer> variableLineNumberTable) {
        var rebuildText = new StringBuilder();
        rebuildText.append(COVER_ASSEMBLY_CONFIG_PATH.toString());
        rebuildText.append("\n");
        for (String newVariable : newVariableTable.keySet()) {
            if (newVariableTable.get(newVariable).contains("-")) {
                for (String configContentLine : configContentLines) {
                    if (configContentLine.contains(newVariable) && configContentLine.contains("@")) {
                        rebuildText.append(configContentLine.split("=")[0].split("@")[1].replace("\"", ""));
                        rebuildText.append("\n");
                    }
                }
                configContentLines[variableLineNumberTable.get(newVariable)] = newVariable + "= " + newVariableTable.get(newVariable).replace("-", "");
            } else
                configContentLines[variableLineNumberTable.get(newVariable)] = newVariable + "= " + newVariableTable.get(newVariable);
        }

        FilesUtil.write(rebuildText.toString(), REBUILD_DAEMON_APP_DATA_PATH);
    }

    private static void rebuild(DaemonProgram program) {
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
