package org.twightlight.talents.talents.enums;

public enum TalentsCategory {
    Melee("melee"),
    Ranged("ranged"),
    Protective("protective"),
    Supportive("supportive"),
    Miscellaneous("miscellaneous"),
    Special("special");

    private final String columnRelative;

    TalentsCategory(String columnRelative) {
        this.columnRelative = columnRelative;
    }

    public String getColumn() {
        return columnRelative;
    }

}
