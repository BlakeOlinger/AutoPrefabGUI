package com.ToppInd;

import bo.core.system.FilesUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Main {
    private static final String PATH_BASE = "C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\";
    private static final Path COVER_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.cover.txt");
    private static final Path REBUILD_DAEMON_APP_DATA_PATH = Paths.get("C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\app data\\rebuild.txt");
    private static final Path COVER_ASSEMBLY_CONFIG_PATH = Paths.get("C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\blob - L2\\blob.L2_cover.txt");
    private static HashMap<String, Integer> coverConfigVariableNameLineNumberTable = new HashMap<>();
    private static HashMap<String, String> coverConfigVariableUserInputTable = new HashMap<>();

    public static void main(String[] args) {
        // read cover config contents and set cover config variable table
        setCoverConfigVariableNameLineNumberTable();

        // set user input parameters table based on cover config variable table
        setUserInputParametersTable();

        // display main window
         displayAppWindow();
    }

    private static void setUserInputParametersTable() {
        for (String variable : coverConfigVariableNameLineNumberTable.keySet()) {
            coverConfigVariableUserInputTable.put(variable, "");
        }
    }

    private static void setCoverConfigVariableNameLineNumberTable() {
        // read cover config contents
        var coverConfigContent = FilesUtil.read(COVER_CONFIG_PATH);

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

        button.addActionListener(e -> displayCoverConfigWindow());

        return button;
    }

    private static void displayCoverConfigWindow() {
        var window = new JFrame("Cover Configurer");
        window.setLayout(new FlowLayout());
        window.setSize(400, 700);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        window.add(coverParamsButton("Base Cover", e -> displayBaseCoverParamsConfigWindow(
                "Base Cover Parameters", "Cover")));
        window.add(coverParamsButton("Hole 1", e -> displayBaseCoverParamsConfigWindow(
                "Hole 1 Parameters", "Hole 1")));
        window.add(coverParamsButton("Hole 2", e -> displayBaseCoverParamsConfigWindow(
                "Hole 2 Parameters", "Hole 2")));
        window.add(coverParamsButton("Hole 3", e -> displayBaseCoverParamsConfigWindow(
                "Hole 3 Parameters", "Hole 3")));
        window.add(coverParamsButton("Hole 4", e -> displayBaseCoverParamsConfigWindow(
                "Hole 4 Parameters", "Hole 4")));
        window.add(coverParamsButton("Hole 5", e -> displayBaseCoverParamsConfigWindow(
                "Hole 5 Parameters", "Hole 5")));

        window.setVisible(true);
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
                        } // TODO - fix rebuild Daemon to not remove '-' character from non-numbers
                        ++readCount;
                    }
                }
            }

            // replace previous config lines with current
            for (String newVariable : newVariableTable.keySet()) {
                configContentLines[variableLineNumberTable.get(newVariable)] = newVariable + "= " + newVariableTable.get(newVariable);
            }

            // write config to config.txt file
            var builder = new StringBuilder();
            for (String line : configContentLines) {
                builder.append(line);
                builder.append("\n");
            }
            System.out.println(builder.toString());
//            FilesUtil.write(builder.toString(), COVER_ASSEMBLY_CONFIG_PATH);

            // write to rebuild.txt the path to the assembly config.txt file
//            FilesUtil.write(COVER_ASSEMBLY_CONFIG_PATH.toString(), REBUILD_DAEMON_APP_DATA_PATH);

            // call rebuild daemon
//            rebuild();
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
        var coverConfigContentLines = FilesUtil.read(COVER_CONFIG_PATH).split("\n");

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
        FilesUtil.write(builder.toString(), COVER_CONFIG_PATH);

        // write to rebuild.txt which file to look for negative values in
        FilesUtil.write(COVER_CONFIG_PATH.toString(), REBUILD_DAEMON_APP_DATA_PATH);

        // call C# auto-rebuild daemon
        rebuild();
    }

    private static void rebuild() {
        try {
            var rebuildDaemonProcess = new ProcessBuilder("cmd.exe", "/c", "AutoRebuildPart.appref-ms").start();
            rebuildDaemonProcess.waitFor();
            rebuildDaemonProcess.destroy();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
