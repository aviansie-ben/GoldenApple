package com.bendude56.goldenapple.permissions;

public interface IPermissionStored {
    /**
     * Gets an ID number representing this permission object. This ID number is
     * guaranteed to be unique among objects <strong>of the same type</strong>.
     * It is possible for two objects of different types (e.g. a group and a
     * user) to have the same ID.
     * 
     * @return An ID number uniquely representing this permission object.
     */
    public long getId();
}
