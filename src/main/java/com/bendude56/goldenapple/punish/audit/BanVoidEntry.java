package com.bendude56.goldenapple.punish.audit;

public class BanVoidEntry extends PunishEntry {
    public BanVoidEntry() {
        super(302, AuditEntryLevel.WARNING);
    }
    
    public BanVoidEntry(String authorizingUser, String target) {
        super(302, AuditEntryLevel.WARNING, authorizingUser, target);
    }
    
    @Override
    public String formatMessage() {
        return this.authorizingUser + " has voided the ban on " + this.target;
    }
    
}
