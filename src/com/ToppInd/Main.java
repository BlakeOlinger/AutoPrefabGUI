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
    private static final Path HANDLE_CONFIG_PATH = Paths.get(PATH_BASE + "blob - L2\\blob.handle_1.txt");
    static Path getHandleConfigPath() {
        return HANDLE_CONFIG_PATH;
    }
    private static final Path SKELETON_CONFIG_PATH = Paths.get(PATH_BASE + "app data\\skeleton.txt");
    static Path getSkeletonConfigPath() {
        return SKELETON_CONFIG_PATH;
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
// TODO - make a hole assembly feature selection define the part based on lookup tables:
//  - 1) hole diameter
//  - 2) BC radius
//  - 3) BC hole count
//  - 4) BC degree offset
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
