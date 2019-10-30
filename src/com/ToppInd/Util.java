package com.ToppInd;

import bo.core.system.FilesUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;

final class Util {
    static class UserInput {
        static void setVariableUserInputMap(HashMap<String, Integer> variableLineNumberMap,
                                                    HashMap<String, String> variableUserInputMap) {
            for (String variable : variableLineNumberMap.keySet()) {
                variableUserInputMap.put(variable, "");
            }
        }

        static String getNewLineFromUserInput(String original, String userInput, String units) {
            return original.split("=")[0].trim() + "= " + userInput + units;
        }

        static String[] getNewLineFromUserInput(HashMap<Integer, String> lineNumberVariableMap,
                                                String[] lines, String userInput) {
            var original = "";

            for (int lineNumber : lineNumberVariableMap.keySet()) {
                original = lineNumberVariableMap.get(lineNumber);
            }
            var newLine = original.split("=")[0].trim() + "= " + userInput + "";

            for (int lineNumber : lineNumberVariableMap.keySet()) {
                lines[lineNumber] = newLine;
            }

            return lines;
        }

        static String getUserTextInput(ActionEvent event) {
            return event.getActionCommand().isEmpty() ? null : event.getActionCommand();
        }
    }

    static class Map {
        static void setVariableLineNumberMap(java.nio.file.Path path,
                                             HashMap<String, Integer> variableLineNumberMap) {
            var configLines = Path.getLinesFromPath(path);

            // create line to line number relationship for each variable
            var index = 0;
            // sort by line NOT contains @ or "IIF" - increment index each line - if NOT HashMap.put()
            for (String line : configLines) {
                if (!line.contains("@") && !line.contains("IIF") && !line.contains("Negative")){
                    // get variable name from line
                    var variableName = line.split("=")[0];
                    variableLineNumberMap.put(variableName, index);
                }
                ++index;
            }
        }

        static void setVariableLineNumberMap(String[] lines,
                                             HashMap<Integer, String> lineNumberVariableMap,
                                             String lineContains) {
            var lineIndex = 0;
            for (String line : lines) {
                if (line.contains(lineContains)) {
                    lineNumberVariableMap.put(lineIndex, line);
                }
                ++lineIndex;
            }
        }

        static HashMap<Integer, String> getSingleLineNumberTable(String[] lines, String identifier) {
            var map = new HashMap<Integer, String>();
            var index = 0;

            for (String line : lines) {
                if (line.contains(identifier) && !line.contains("IIF")) {
                    map.put(index, line);
                    break;
                }
                ++index;
            }

            return map;
        }
    }

    static class Path {
        static String[] getLinesFromPath(java.nio.file.Path path) {
            return FilesUtil.read(path).split("\n");
        }

        static JLabel[] getLabelsFromLines(java.nio.file.Path path) {
            var lines = Util.Path.getLinesFromPath(path);
            var returnLabelTotal = 0;
            for (String line : lines) {
                if (line.contains("Property")) {
                    ++returnLabelTotal;
                }
            }
            var labels = new JLabel[returnLabelTotal];
            var labelIndex = 0;
            for (String line : lines) {
                if (line.contains("Property")) {
                    var text = line.split(":")[1].split("=")[0].replace("\"", "").trim() + ": ";
                    labels[labelIndex++] = new JLabel(text);
                }
            }
            return labels;
        }

        static java.nio.file.Path getCoverConfigPath() {
            return Main.getCoverShapeSelection().contains("Square") ?
                    Main.getSquareCoverConfigPath() :
                    Main.getCoverConfigPath();
        }

        static java.nio.file.Path getHolePath(String hole) {
            var holeNumber = Integer.parseInt(hole.split(" ")[1].trim());
            return Main.getHoleFeatureConfigMap().get(holeNumber);
        }
    }

    static class Output {
        static String generateWriteOutput(String... lines) {
            var builder = new StringBuilder();
            for (String line : lines) {
                builder.append(line);
                builder.append("\n");
            }
            return builder.toString();
        }

        static void writeToConfig(String content, java.nio.file.Path path, boolean isWriteable) {
            if (isWriteable)
                FilesUtil.write(content, path);
        }

        static String removeNoneASCIIChars(String string) {
            var formattedFeature = new StringBuilder();

            for (char character : string.toCharArray()){
                if ((int) character < 250) {
                    formattedFeature.append(character);
                }
            }

            return formattedFeature.toString();
        }
    }

    static class Build {
        static void rebuild(DaemonProgram program, Boolean isBuildable) {
            if (isBuildable) {
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
    }

    static class Dimension {
        static String getMateDistanceLine(String[] lines, String identifier) {
            var distanceLine = "";
            for (String line : lines) {
                if (line.contains(identifier) && line.contains("@")) {
                    distanceLine = line;
                }
            }

            distanceLine = distanceLine.split("=")[0].split("@")[1]
                    .replace("\"", "").trim();

            return distanceLine;
        }
    }
}
