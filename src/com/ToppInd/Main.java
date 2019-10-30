package com.ToppInd;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final String PATH_BASE = "C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\";
    private static final Path COVER_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.cover.txt");
    static Path getCoverConfigPath() {
        return COVER_CONFIG_PATH;
    }
    private static final Path SQUARE_COVER_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.coverSquare.txt");
    static Path getSquareCoverConfigPath() {
        return SQUARE_COVER_CONFIG_PATH;
    }
    private static final Path COVER_SHAPE_ASSEMBLY_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.coverConfig.txt");
    static Path getCoverShapeAssemblyConfigPath() {
        return COVER_SHAPE_ASSEMBLY_CONFIG_PATH;
    }
    private static final Path REBUILD_DAEMON_APP_DATA_PATH = Paths.get("C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\app data\\rebuild.txt");
    static Path getRebuildDaemonAppDataPath() {
        return REBUILD_DAEMON_APP_DATA_PATH;
    }
    private static final String APP_DATA_BASE = "C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\app data\\";
    private static final Path HANDLE_OFFSET_LOOKUP_TABLE_PATH = Paths.get(APP_DATA_BASE + "assembly handle offset table.txt");
    static Path getHandleOffsetLookupTablePath() {
        return HANDLE_OFFSET_LOOKUP_TABLE_PATH;
    }
    private static final Path COVER_ASSEMBLY_CONFIG_PATH = Paths.get("C:\\Users\\bolinger\\Documents\\SolidWorks Projects\\Prefab Blob - Cover Blob\\blob - L2\\blob.L2_cover.txt");
    static Path getCoverAssemblyConfigPath() {
        return COVER_ASSEMBLY_CONFIG_PATH;
    }
    private static final Path ANGLE_FRAME_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.2inAngleFrame.txt");
    static Path getAngleFrameConfigPath() {
        return ANGLE_FRAME_CONFIG_PATH;
    }
    private static final Path ALUM_FLAT_BAR_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.alumFlatBar.txt");
    static Path getAlumFlatBarConfigPath() {
        return ALUM_FLAT_BAR_CONFIG_PATH;
    }
    private static final Path INSPECTION_PLATE_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.inspectionPlate.txt");
    static Path getInspectionPlateConfigPath() {
        return INSPECTION_PLATE_CONFIG_PATH;
    }
    private static final Path COVER_DRAWING_CONFIG_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.coverDrawing.txt");
    static Path getCoverDrawingConfigPath() {
        return COVER_DRAWING_CONFIG_PATH;
    }
    private static final Path COVER_ASSEMBLY_PATH = Paths.get(PATH_BASE + "blob - L2\\blob.L2_cover.SLDASM");
    static Path getCoverAssemblyPath() {
        return COVER_ASSEMBLY_PATH;
    }
    private static final Path COVER_DRAWING_PATH = Paths.get(PATH_BASE + "base blob - L1\\blob.cover.SLDDRW");
    static Path getCoverDrawingPath() {
        return COVER_DRAWING_PATH;
    }
    private static final HashMap<Integer, Path> HOLE_FEATURE_CONFIG_MAP = new HashMap<>(
            Map.of(
                    1, Paths.get(PATH_BASE + "blob - L2\\blob.holeFeature_1.txt"),
                    2, Paths.get(PATH_BASE + "blob - L2\\blob.holeFeature_2.txt"),
                    3, Paths.get(PATH_BASE + "blob - L2\\blob.holeFeature_3.txt"),
                    4, Paths.get(PATH_BASE + "blob - L2\\blob.holeFeature_4.txt"),
                    5, Paths.get(PATH_BASE + "blob - L2\\blob.holeFeature_5.txt"),
                    6, Paths.get(PATH_BASE + "blob - L2\\blob.holeFeature_6.txt")
            )
    );
    static HashMap<Integer, Path> getHoleFeatureConfigMap() {
        return HOLE_FEATURE_CONFIG_MAP;
    }
    private static HashMap<String, Integer> coverConfigVariableNameLineNumberTable = new HashMap<>();
    static HashMap<String, Integer> getCoverConfigVariableNameLineNumberTable() {
        return coverConfigVariableNameLineNumberTable;
    }
    private static HashMap<String, String> coverConfigVariableUserInputTable = new HashMap<>();
    static HashMap<String, String> getCoverConfigVariableUserInputTable() {
        return coverConfigVariableUserInputTable;
    }
    private static String coverShapeSelection = "Circular";
    static String getCoverShapeSelection() {
        return coverShapeSelection;
    }
    static void setCoverShapeSelection(String shapeSelection) {
        coverShapeSelection = shapeSelection;
    }
    private static final HashMap<String, String> MATERIAL_CONFIG_TABLE = new HashMap<>(
            Map.of(
                    "ASTM A36 Steel", "0",
                    "6061 Alloy", "1"
            )
    );
    static HashMap<String, String> getMaterialConfigTable() {
        return MATERIAL_CONFIG_TABLE;
    }
    private static final boolean REBUILDABLE = true;
    static boolean getBuildable() {
        return REBUILDABLE;
    }
    private static final boolean WRITEABLE = true;
    static boolean getWritable() {
        return WRITEABLE;
    }
    private static final boolean ASSEMBLY_MATE_CALIBRATION = false;
    static boolean getMateCalibration() {
        return ASSEMBLY_MATE_CALIBRATION;
    }
    // TODO - incorporate lookup tables
    //  - 1) write to a configuration selection app data file
    //      - the length and width of hatch
    //  - 2) from that config app data file set relative offsets
    //      - such as GT Box, Handle, Hatch Arm, etc. based on a
    //      - lookup table for that feature
    // TODO - automate blob.* based part descriptions
    //  - 1) do this based on a lookup table for the part - cover/2in angle frame/etc.
    //      - utilize <> variables that will substitute current user defined values for diameter etc.
    // TODO - define an appropriate algorithm and lookup table scheme for including drawing notes
    // TODO - in the auto-dimension daemon - have daemon search for 0" dimensions and unmark for drawing those - mark else
    // TODO - Pack And Go - define the algorithm and user interaction
    // TODO - BOM - auto-delete rows based on a lookup table - not sure if I'll go white/black list
    // TODO - on selection of a hole feature - ECG 2 Hole for example - automatically set the diameter and BC properties
    // TODO - (long-term/down time) - need to refactor the mess that is this monolithic class
    // TODO - (after previous/next phase) - begin general prefab - utilize lookup tables and wrapper assemblies
    //          - utilize separate *.SLDDRW files per sheet and auto-copy past into final drawing doc each sheet
    // TODO - eff it - prefab now - modeling initial success condition after - SO:324900
    //  - various thoughts in no particular order:
    //      - config prefab button
    //      - require 3 individual *.SLDDRW files for each sheet
    //      - copy paste into final
    //      - configure pump - selection
    //      - config all the various bits base XYZ placement on lookup tables
    //      - adapt drawing config to handle sheets - cover, wet well1, wet well2, prefab BOM etc
    //      - config basin window
    //      - maybe don't have individual sheets in different files - cover/prefab okay, any more then BOM difficult to transfer

    public static void main(String[] args) {
        // display main window
         displayAppWindow();
    }

    private static void displayAppWindow() {
        var window = new JFrame("AutoPrefab");
        window.setSize(400, 200);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new FlowLayout());

        // add "Configure Cover" button
        window.add(Cover.Button.configureCoverButton());

        // add "Configure Prefab" button
        window.add(Prefab.Button.getButton());

        // add "Build Drawing" button
        window.add(Drawing.Button.configureDrawingButton());

        window.setVisible(true);
    }
}
