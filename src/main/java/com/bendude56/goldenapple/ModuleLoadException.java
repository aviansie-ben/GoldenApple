package com.bendude56.goldenapple;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class ModuleLoadException extends RuntimeException {
	private static final long		serialVersionUID	= 1202984010081752893L;

	private final Serializable[]	dumpInfo;

	public ModuleLoadException(String module) {
		this(module, null, new Serializable[] { 0x0000, module, new Exception("Unknown error") });
	}

	public ModuleLoadException(String module, Throwable cause) {
		this(module, cause, new Serializable[] { 0x0000, module, cause });
	}

	public ModuleLoadException(String module, Serializable[] dumpInfo) {
		this(module, null, dumpInfo);
	}

	public ModuleLoadException(String module, Throwable cause, Serializable[] dumpInfo) {
		super("Unrecoverable error while loading module '" + module + "'", cause);
		this.dumpInfo = dumpInfo;
	}

	public void dump(OutputStream s) throws IOException {
		ObjectOutputStream o = new ObjectOutputStream(s);
		o.writeObject(super.getStackTrace());
		for (Serializable i : dumpInfo) {
			o.writeObject(i);
		}
		o.close();
	}

	public enum LoadExceptionType {
		Sql, CommandRegister, EventRegister, PermissionRegister
	}
}
