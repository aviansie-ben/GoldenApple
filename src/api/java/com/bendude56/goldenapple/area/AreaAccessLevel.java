package com.bendude56.goldenapple.area;

import java.util.HashMap;

/**
 * Enum representing the access level that users and groups have over areas.
 * 
 * @author Deaboy
 * 
 */
public enum AreaAccessLevel {
    
    // IMPORTANT: Never change the first number, NO MATTER WHAT!
    // The second number may change, as long as there are no conflicts.
    NONE(0, 0), GUEST(1, 1), OWNER(2, 2);
    
    /**
     * The official, serializeable ID of a specific instance of this enum NEVER
     * CHANGE! Used in database queries! Must remain constant throughout each
     * version.
     */
    private final int id;
    /**
     * The comparable value of this level. May change without warning; do not
     * save!
     */
    private final int level;
    
    /**
     * A table for instantaneous id lookups.
     */
    private static final HashMap<Integer, AreaAccessLevel> lookupTable = new HashMap<Integer, AreaAccessLevel>();
    
    // Automatically insert all enum values into the lookup table.
    static {
        for (AreaAccessLevel l : values()) {
            lookupTable.put(l.id, l);
        }
    }
    
    /**
     * NEVER CHANGE THE VALUE OF THE ID FROM VERSION TO VERSION OF THIS PROGRAM.
     * 
     * @param id
     * @param level
     */
    private AreaAccessLevel(int id, int level) {
        this.id = id;
        this.level = level;
    }
    
    /**
     * Returns the numeric representation of the access level. This number will
     * never change from each version of GoldenApple.
     * 
     * @return The constant, unchanging ID of the AreaAccessLevel.
     */
    public int getId() {
        return this.id;
    }
    
    /**
     * Returns comparable numeric value. This value could change from version
     * version, so do not store this value. Only meant for comparison.
     * 
     * @return Returns a numeric value that can be used to gauge which access
     * levels are more important.
     */
    public int getComparableValue() {
        return this.level;
    }
    
    /**
     * Returns the AreaAccess enumeration with an access code that matches the
     * given code.
     * 
     * @param code The code to match.
     * @return The AreaAccess enum that matches the code. Null if none exist.
     */
    public static AreaAccessLevel fromId(Integer code) {
        if (lookupTable.containsKey(code)) {
            return lookupTable.get(code);
        }
        return null;
    }
    
    @Override
    public String toString() {
        switch (this) {
            case NONE:
                return "stranger";
            case GUEST:
                return "guest";
            case OWNER:
                return "owner";
            default:
                return "";
        }
    }
    
}
