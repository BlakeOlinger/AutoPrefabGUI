package com.ToppInd;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

final class Drawing {
    static class Button {
        static JButton configureDrawingButton() {
            var button = new JButton("Configure Drawing");
            button.addActionListener(e -> Window.displayConfigureDrawingWindow());
            return button;
        }

        static JButton customPropertiesButton() {
            var button = new JButton("Document Info");
            button.addActionListener(e -> Window.displayCustomPropertiesConfigWindow());
            return button;
        }

        static JButton documentPropertiesSetAsDefaultsButton() {
            var button = new JButton("Default");
            button.addActionListener(e -> ActionHandler.handleDocumentPropertyAction(
                    "Property",
                    "<>",
                    "",
                    Main.getCoverDrawingConfigPath(),
                    Main.getRebuildDaemonAppDataPath(),
                    DaemonProgram.REBUILD));
            return button;
        }
    }

    static class ActionHandler {
        static void handleDocumentPropertyAction(String lineContains, String userInput, String units,
                                                 Path drawingConfigPath, Path appDataPath, DaemonProgram daemonProgram) {
            var configLines = Util.Path.getLinesFromPath(drawingConfigPath);
            var index = 0;
            for (String line : configLines) {
                if (line.contains(lineContains)) {
                    var newLine = Util.UserInput.getNewLineFromUserInput(line, userInput, units);
                    configLines[index] = newLine;
                }
                ++index;
            }

            var output = generateWriteOutput(configLines);
            writeToConfig(output, drawingConfigPath);

            writeToConfig(drawingConfigPath.toString(), appDataPath);

            rebuild(daemonProgram);
        }
    }

    static class Window {
        static void displayConfigureDrawingWindow() {
            var window = new JFrame("Drawing Configurer");
            window.setSize(300, 300);
            window.setLocationRelativeTo(null);
            window.setLayout(new FlowLayout());
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            window.add(Button.customPropertiesButton());
            window.add(drawingViewScaleButton());
            window.add(generateDimensionsButton());
            window.add(autoBalloonButton());
            window.add(autoCenterMarkButton());

            window.setVisible(true);
        }

        static void displayCustomPropertiesConfigWindow() {
            var window = new JFrame("Document Information Configurer");
            window.setSize(225, 500);
            window.setLayout(new FlowLayout());
            window.setLocationRelativeTo(null);
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            var propertyLabels = Util.Path.getLabelsFromLines("Property", Main.getCoverDrawingConfigPath());
            for (JLabel label : propertyLabels) {
                window.add(label);
                var textBox = new JTextField(10);
                textBox.addActionListener(e -> handleDocumentPropertyAction(e, label));
                window.add(textBox);
            }

            window.add(Button.documentPropertiesSetAsDefaultsButton());

            window.setVisible(true);
        }
    }
}
