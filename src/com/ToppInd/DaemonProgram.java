package com.ToppInd;

enum DaemonProgram {
    REBUILD("AutoRebuildPart.appref-ms"),
    MATERIAL_CONFIG("AutoMaterialConfig.appref-ms"),
    ASSEMBLY_REBUILD("AutoAssemblyRebuild.appref-ms"),
    ASSEMBLY_GENERAL("AutoGeneralAssembly.appref-ms"),
    DRAWING_AUTO_BALLOON("sw-test.appref-ms"),
    DRAWING_AUTO_CENTER_MARK("AutoCenterMark.appref-ms"),
    DRAWING_AUTO_DIMENSION("AutoDimension.appref-ms");

    private String program;

    DaemonProgram(String s) {
        program = s;
    }

    String getProgram() {
        return program;
    }
}
