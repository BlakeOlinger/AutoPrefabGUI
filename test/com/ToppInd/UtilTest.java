package com.ToppInd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {
    private static final String[] LINES = new String[] {
            "How Now!",
            "Brown Cow!",
            "\"cow\"= <>"
    };
    private static final HashMap<Integer, String> LINE_NUMBER_VARIABLE_MAP = new HashMap<>();

    @AfterEach
    void tearDown() {
        LINE_NUMBER_VARIABLE_MAP.clear();
    }

    @Test
    void true_for_line_number_equal_1() {
        Util.Map.setVariableLineNumberMap(LINES, LINE_NUMBER_VARIABLE_MAP, "Brown Cow!");

        var expected = 1;

        var result = 0;

        for (int lineNumber : LINE_NUMBER_VARIABLE_MAP.keySet()) {
            result = lineNumber;
        }

        assertEquals(expected, result);
    }

    @Test
    void true_for_line_exchanged_with_user_input() {
        var expected = new String[] {
                "How Now!",
                "Brown Cow!",
                "\"cow\"= foo"
        };
        Util.Map.setVariableLineNumberMap(LINES, LINE_NUMBER_VARIABLE_MAP, "\"cow\"=");
        var result = Util.UserInput.getNewLineFromUserInput(LINE_NUMBER_VARIABLE_MAP, LINES, "foo");

        assertArrayEquals(expected, result);
    }
}