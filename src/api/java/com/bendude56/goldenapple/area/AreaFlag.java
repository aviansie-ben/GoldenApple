package com.bendude56.goldenapple.area;

public enum AreaFlag {
    PVP(0), REGEN(1), NOMOBS(2);
    
    private final int id;
    
    private AreaFlag(int id) {
        this.id = id;
    }
    
    public static AreaFlag fromId(int id) {
        for (AreaFlag f : AreaFlag.values()) {
            if (f.id == id) {
                return f;
            }
        }
        return null;
    }
    
    public int getId() {
        return this.id;
    }
    
    public static AreaFlag fromString(String s) {
        switch (s.toLowerCase()) {
            case "pvp":
                return PVP;
            case "regen":
            case "healing":
                return REGEN;
            case "nomobs":
            case "safety":
                return NOMOBS;
            default:
                return null;
        }
    }
    
    @Override
    public String toString() {
        switch (this) {
            case PVP:
                return "pvp";
            case REGEN:
                return "regen";
            case NOMOBS:
                return "nomobs";
            default:
                return "";
        }
    }
}
