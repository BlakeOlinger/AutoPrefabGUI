package com.ToppInd;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.HashMap;

final class Drawing {
    static class Button {
        // top level button
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
                    DaemonProgram.DRAWING_PROPERTIES));
            return button;
        }

        static JButton drawingViewScaleButton() {
            var button = new JButton("Drawing View Scale");
            button.addActionListener(e -> Window.displayDrawingViewScaleConfigWindow());
            return button;
        }

        static JButton generateDimensionsButton() {
            var button = new JButton("Generate Dimensions");
            button.addActionListener(e -> ActionHandler.handleDrawingGenerateDimensionsAction(
                    Main.getCoverAssemblyPath(),
                    Main.getCoverShapeAssemblyConfigPath(),
                    Main.getCoverDrawingPath(),
                    Main.getRebuildDaemonAppDataPath(),
                    DaemonProgram.DRAWING_AUTO_DIMENSION
            ));
            return button;
        }

        static JButton autoBalloonButton() {
            var button = new JButton("Feature Balloon");
            button.addActionListener(e -> Util.Build.rebuild(DaemonProgram.AUTO_BALLOON, Main.getBuildable()));
            return button;
        }

        static JButton autoCenterMarkButton() {
            var button = new JButton("Center Mark");
            button.addActionListener(e -> Util.Build.rebuild(DaemonProgram.CENTER_MARK, Main.getBuildable()));
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

            var output = Util.Output.generateWriteOutput(configLines);
            Util.Output.writeToConfig(output, drawingConfigPath, Main.getWritable());

            Util.Output.writeToConfig(drawingConfigPath.toString(), appDataPath, Main.getWritable());

            Util.Build.rebuild(daemonProgram, Main.getBuildable());
        }

        static void handleDocumentPropertyAction(String lineContains, String units,
                                                 ActionEvent event, JLabel label, Path drawingConfigPath,
                                                 Path appDataPath, DaemonProgram daemonProgram) {
            var text = label.getText();
            var userInput = Util.UserInput.getUserTextInput(event);

            if (userInput != null) {
                var configLines = Util.Path.getLinesFromPath(drawingConfigPath);

                var index = 0;
                for (String line : configLines) {
                    if (line.contains(lineContains)) {
                        var identifier = text.replace(":", "").trim();
                        if (line.contains(identifier)) {
                            var newLine = Util.UserInput.getNewLineFromUserInput(line, userInput, units);
                            configLines[index] = newLine;
                        }
                    }
                    ++index;
                }

                var output = Util.Output.generateWriteOutput(configLines);
                Util.Output.writeToConfig(output, drawingConfigPath, Main.getWritable());

                Util.Output.writeToConfig(drawingConfigPath.toString(),
                        appDataPath, Main.getWritable());

                Util.Build.rebuild(daemonProgram, Main.getBuildable());
            }
        }

        static void handleDrawingScaleViewAction(ActionEvent event, Path drawingConfigPath, Path appDataPath,
                                                 DaemonProgram daemonProgram) {
            var userInput = Util.UserInput.getUserTextInput(event);

            if (userInput != null) {
                var lines = Util.Path.getLinesFromPath(drawingConfigPath);
                var lineNumberVariableMap = new HashMap<Integer, String>();
                Util.Map.setVariableLineNumberMap(
                        lines,
                        lineNumberVariableMap,
                        "\"Drawing View1 Scale\"= ");
                var newLines = Util.UserInput.getNewLineFromUserInput(
                        lineNumberVariableMap,
                        lines,
                        userInput,
                        ""
                );

                var drawingViewOut = Util.Output.generateWriteOutput(newLines);

                // write to app data
                Util.Output.writeToConfig(drawingConfigPath.toString(), appDataPath, Main.getWritable());

                // write to drawing config
                Util.Output.writeToConfig(drawingViewOut, drawingConfigPath, Main.getWritable());

                Util.Build.rebuild(daemonProgram, Main.getBuildable());
            }
        }

        static void handleDrawingGenerateDimensionsAction(Path coverAssemblyPath, Path coverAsmConfigPath,
                                                          Path coverDrawingPath, Path appDataPath,
                                                          DaemonProgram daemonProgram) {
            // for now it is just a passive activator aside from writing the appropriate paths to app data
            var appData = coverAssemblyPath + "\n";
            appData += coverAsmConfigPath + "\n";
            appData += coverDrawingPath;

            Util.Output.writeToConfig(appData, appDataPath, Main.getWritable());

            Util.Build.rebuild(daemonProgram, Main.getBuildable());
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
            window.add(Button.drawingViewScaleButton());
            window.add(Button.generateDimensionsButton());
            window.add(Button.autoBalloonButton());
            window.add(Button.autoCenterMarkButton());

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
                textBox.addActionListener(e -> ActionHandler.handleDocumentPropertyAction(
                        "Property",
                        "",
                        e,
                        label,
                        Main.getCoverDrawingConfigPath(),
                        Main.getRebuildDaemonAppDataPath(),
                        DaemonProgram.DRAWING_PROPERTIES));
                window.add(textBox);
            }

            window.add(Button.documentPropertiesSetAsDefaultsButton());

            window.setVisible(true);
        }

        static void displayDrawingViewScaleConfigWindow() {
            var window = new JFrame("Drawing Scale View Configurer");
            window.setSize(250, 300);
            window.setLocationRelativeTo(null);
            window.setLayout(new FlowLayout());
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            window.add(new JLabel("Drawing View 1 Scale: "));
            var drawView1Box = new JTextField(2);
            drawView1Box.addActionListener(e -> ActionHandler.handleDrawingScaleViewAction(
                    e,
                    Main.getCoverDrawingConfigPath(),
                    Main.getRebuildDaemonAppDataPath(),
                    DaemonProgram.DRAWING_VIEW_SCALE));
            window.add(drawView1Box);

            window.setVisible(true);
        }
    }
}
