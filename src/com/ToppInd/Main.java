package com.ToppInd;

import bo.core.system.FilesUtil;

import javax.swing.*;
import java.awt.*;
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

    // TODO - add to "Build Drawing" button the call to the
    //  - C# daemon that turns off marked for drawing on dimensions equal to zero (still have to make that)
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

        window.add(coverParamsButton());

        window.setVisible(true);
    }

    private static JButton coverParamsButton() {
        var button = new JButton("Base Cover");
        button.addActionListener(e -> displayBaseCoverParamsConfigWindow());
        return button;
    }

    private static void displayBaseCoverParamsConfigWindow() {
        var window = new JFrame("Base Cover Parameters");
        window.setSize(300, 400);
        window.setLocationRelativeTo(null);
        window.setLayout(new FlowLayout());
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        window.add(new JLabel("Instructions:"));
        window.add(new JLabel(" After each input press Enter to 'set' the value."));
        window.add(new JLabel(" Click the Build button to generate the model."));

        for (String variable : coverConfigVariableNameLineNumberTable.keySet()) {
            if (variable.contains("Cover")) {
                window.add(new JLabel(variable + ": "));
                var textInput = new JTextField( 4);
                textInput.addActionListener(e -> coverConfigVariableUserInputTable.put(variable, e.getActionCommand()));
                window.add(textInput);
            }
        }

        window.add(baseCoverParamsBuildButton());

        window.setVisible(true);
    }

    private static JButton baseCoverParamsBuildButton() {
        var button = new JButton("Build");
        button.addActionListener(e -> writeBaseCoverChanges());
        return button;
    }

    private static void writeBaseCoverChanges() {
        var coverConfigContentLines = FilesUtil.read(COVER_CONFIG_PATH).split("\n");

        // gets cover variables - user input and appends the line with the changed value to the lines array
        for (String userInputVariable : coverConfigVariableUserInputTable.keySet()) {
            if (userInputVariable.contains("Cover") &&
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
