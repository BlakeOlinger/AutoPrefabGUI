package com.ToppInd;

import bo.core.system.FilesUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Main {
    private static final String PATH_BASE = "C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\";
    private static final Path COVER_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.cover.txt");
    public static void main(String[] args) {
        // read cover config contents
        var coverConfigContent = FilesUtil.read(COVER_CONFIG_PATH);

        // split by new line
        var coverConfigLines = coverConfigContent.split("\n");

        // create line to line number relationship for each variable
        var variableNameLineNumberTable = new HashMap<String, Integer>();
        var index = 0;
            // sort by line NOT contains @ or "IIF" - increment index each line - if NOT HashMap.put()
        for (String line : coverConfigLines) {
            if (!line.contains("@") && !line.contains("IIF")){
                // get variable name from line
                var variableName = line.split("=")[0];
                variableNameLineNumberTable.put(variableName, index);
            }
            ++index;
        }

        // display main window
         displayAppWindow();
    }

    private static void displayAppWindow() {
        var window = new JFrame("AutoPrefab");
        window.setSize(600, 500);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new BorderLayout());

        // add "Configure Cover" button
        window.add(configureCoverButton(), BorderLayout.NORTH);

        window.setVisible(true);
    }

    private static JButton configureCoverButton() {
        var button = new JButton("Configure Cover");

        button.addActionListener(configureButtonAction());

        return button;
    }

    private static ActionListener configureButtonAction() {
        return e -> System.out.println("Action!");
    }
}
