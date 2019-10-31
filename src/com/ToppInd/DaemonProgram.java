package com.ToppInd;

enum DaemonProgram {
    PART_REBUILD("AutoRebuildPart.appref-ms"),
    MATERIAL_CONFIG("AutoMaterialConfig.appref-ms"),
    ASSEMBLY_REBUILD("AutoAssemblyRebuild.appref-ms"),
    ASSEMBLY_GENERAL("AutoGeneralAssembly.appref-ms"),
    AUTO_BALLOON("AutoBalloon.appref-ms"),
    CENTER_MARK("AutoCenterMark.appref-ms"),
    DRAWING_AUTO_DIMENSION("AutoDimension.appref-ms"),
    DRAWING_VIEW_SCALE("DrawingViewScale.appref-ms"),
    DRAWING_PROPERTIES("DrawingProperties.appref-ms"),
    BASIC_REBUILD("BasicRebuild.appref-ms");

    private String program;

    DaemonProgram(String s) {
        program = s;
    }

    String getProgram() {
        return program;
    }
}
