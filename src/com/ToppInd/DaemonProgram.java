package com.ToppInd;

enum DaemonProgram {
    REBUILD("AutoRebuildPart.appref-ms"),
    MATERIAL_CONFIG("AutoMaterialConfig.appref-ms");

    private String program;

    DaemonProgram(String s) {

    }

    String getProgram() {
        return program;
    }
}
