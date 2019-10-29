package com.ToppInd;

import bo.core.system.FilesUtil;

import javax.swing.*;
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
    }

    static class SetMap {
        static void setCoverConfigVariableNameLineNumberTable(java.nio.file.Path path,
                                                              HashMap<String, Integer> variableLineNumberMap) {
            var coverConfigLines = Path.getLinesFromPath(path);

            // create line to line number relationship for each variable
            var index = 0;
            // sort by line NOT contains @ or "IIF" - increment index each line - if NOT HashMap.put()
            for (String line : coverConfigLines) {
                if (!line.contains("@") && !line.contains("IIF") && !line.contains("Negative")){
                    // get variable name from line
                    var variableName = line.split("=")[0];
                    variableLineNumberMap.put(variableName, index);
                }
                ++index;
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
                if (line.contains("Property")) {
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
}
