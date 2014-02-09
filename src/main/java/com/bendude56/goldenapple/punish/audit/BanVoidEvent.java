package com.bendude56.goldenapple.punish.audit;


public class BanVoidEvent extends PunishEvent {
	public BanVoidEvent(String authorizingUser, String target) {
		super(302, AuditEventLevel.WARNING, authorizingUser, target);
	}

	@Override
	public String formatMessage() {
		return this.authorizingUser + " has voided the ban on " + this.target;
	}

}
