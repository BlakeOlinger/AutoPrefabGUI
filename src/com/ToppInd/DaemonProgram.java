package com.ToppInd;

enum DaemonProgram {
    REBUILD("AutoRebuildPart.appref-ms"),
    MATERIAL_CONFIG("AutoMaterialConfig.appref-ms"),
    ASSEMBLY_REBUILD("AutoAssemblyRebuild.appref-ms"),
    BUILD_DRAWING("AutoPrefabDaemon.bat");

    private String program;

    DaemonProgram(String s) {
        program = s;
    }

    String getProgram() {
        return program;
    }
}
