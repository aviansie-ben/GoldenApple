package com.bendude56.goldenapple.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
		return (Serializable) i.readObject();
	}
}
