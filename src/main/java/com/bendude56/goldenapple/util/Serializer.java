package com.bendude56.goldenapple.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Serializer {
	private Serializer() {}

	/**
	 * Takes a serializable object and serializes it, then turns it into a
	 * base64 encoded string
	 * 
	 * @param s The object to be serialized
	 * @return A base64 encoded string representing the object
	 */
	public static String serialize(Serializable s) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(bos);
		o.writeObject(s);
		return Base64.encodeBytes(bos.toByteArray());
	}

	/**
	 * Takes a base64 encoded string and uses it to deserialize the contained
	 * bytes back into an object
	 * 
	 * @param s The base64 encoded string representing the serialized object
	 * @return The object represented by the serialized string
	 */
	public static Serializable deserialize(String s) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(s));
		ObjectInputStream i = new ObjectInputStream(bis);
		return (Serializable)i.readObject();
	}

	public static String serializeLocation(Location l) {
		return l.getX() + "|" + l.getY() + "|" + l.getZ() + "|" + l.getWorld().getName();
	}

	public static Location deserializeLocation(String s) {
		double x = Double.parseDouble(s.split("|")[0]);
		double y = Double.parseDouble(s.split("|")[1]);
		double z = Double.parseDouble(s.split("|")[2]);
		World w = Bukkit.getWorld(s.split("|", 4)[3]);
		return new Location(w, x, y, z);
	}
}
