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

        if (!windowTitle.contains("Base Cover")) {
            var radioButtons = holeAssemblyConfigRadios();
            var buttonGroup = new ButtonGroup();

            for (JRadioButton radioButton : radioButtons) {
                buttonGroup.add(radioButton);
            }

            for (JRadioButton radioButton : radioButtons) {
                window.add(radioButton);
            }

            window.add(baseCoverParamsBuildButton(variableName));
        }
        window.setVisible(true);
    }

    private static JRadioButton[] holeAssemblyConfigRadios() {
        return new JRadioButton[]{
                new JRadioButton("PF150S 0deg"),
                new JRadioButton("PF200T 90deg"),
                new JRadioButton("ECG 2 Hole"),
                new JRadioButton("10in Inspection Plate 45deg BC"),
                new JRadioButton("none")
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
