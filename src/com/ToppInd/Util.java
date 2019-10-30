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
                                                String[] lines, String userInput, String units) {
            var original = "";

            for (int lineNumber : lineNumberVariableMap.keySet()) {
                original = lineNumberVariableMap.get(lineNumber);
            }
            var newLine = original.split("=")[0].trim() + "= " + userInput + units;

            for (int lineNumber : lineNumberVariableMap.keySet()) {
                lines[lineNumber] = newLine;
            }

            return lines;
        }

        static String getUserTextInput(ActionEvent event) {
            return event.getActionCommand().isEmpty() ? null : event.getActionCommand();
        }
    }

    static class SetMap {
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
    }

    static class Path {
        static String[] getLinesFromPath(java.nio.file.Path path) {
            return FilesUtil.read(path).split("\n");
        }

        static JLabel[] getLabelsFromLines(String lineContains, java.nio.file.Path path) {
            var lines = Util.Path.getLinesFromPath(path);
            var returnLabelTotal = 0;
            for (String line : lines) {
                if (line.contains(lineContains)) {
                    ++returnLabelTotal;
                }
            }
            var labels = new JLabel[returnLabelTotal];
            var labelIndex = 0;
            for (String line : lines) {
                if (line.contains(lineContains)) {
                    var text = line.split(":")[1].split("=")[0].replace("\"", "").trim() + ": ";
                    labels[labelIndex++] = new JLabel(text);
                }
            }
            return labels;
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
}
