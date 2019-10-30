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

    static class Index {
        static int firstIndex(String line, char indexCharacter) {
            var index = 0;
            var charArray = line.toCharArray();
            var stop = false;
            while (!stop) {
                if (charArray[index] == indexCharacter){
                    stop = true;
                } else {
                    ++index;
                }
            }
            return index;
        }
    }

    static class Dimension {
        static String getDimensionDegreeType(String line) {
            var startIndex = 0;
            var lineSplit = line.split("=")[0];
            var type = "";
            var lineContainsZero = lineSplit.contains("0");
            if (lineSplit.contains("9")) {
                startIndex = Index.firstIndex(line, '9');
            } else if (lineContainsZero){
                startIndex = Index.firstIndex(line, '0');
            }

            var endIndex = line.contains("9") ? startIndex + 5 : startIndex + 4;

            try {
                type = line.substring(startIndex, endIndex);
            } catch (StringIndexOutOfBoundsException exception) {
                System.out.println(line);
            }

            return type.trim();
        }

        static boolean isDimensionX(String line) {
            return line.contains("X");
        }
    }

    static class Debug {
        static void outputLines(String... lines) {
            for (String line : lines) {
                System.out.println(line);
            }
        }

        static void outputLines(String line, int integer) {
            System.out.println(line);
            System.out.println(integer + "");
        }

        static <T> void outputLines(T line) {
            System.out.println(line);
        }

        static void outputLines(HashMap<Integer, String> map) {
            for (String line : map.values()) {
                System.out.println(line);
            }
        }

        static void outputLines(HashMap<String, Boolean> map, boolean isStringInt) {
            for (String line : map.keySet()) {
                System.out.println(line);
            }
        }
    }
}
