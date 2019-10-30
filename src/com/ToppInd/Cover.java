package com.ToppInd;

import bo.core.system.FilesUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.nio.file.Path;

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
            button.addActionListener(e -> displayCoverAssemblyConfigWindow());
            return button;
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
            Util.SetMap.setVariableLineNumberMap(Util.Path.getCoverConfigPath(),
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

            window.add(inspectionPlateConfigButton(variableName));

            window.add(confirmHoleAssemblyConfigButton(variableName, buttonGroup));

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
            var holePath = getHolePath(variableName);

            var assemblyConfigLines = FilesUtil.read(holePath).split("\n");

            for (String line : assemblyConfigLines) {
                if (line.contains("Bool") && !line.contains("IIF")) {
                    var feature = line.split("=")[0].replace("Bool", "").replace("\"", "");
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
    }
}
