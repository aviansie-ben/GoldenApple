package com.bendude56.goldenapple.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

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
	
	public static String serializeItemStack(ItemStack[] stack)
	{
		String s = "";
		
		for (int slot = 0; slot < stack.length; slot++)
		{
			s += slot + "=";
			s += stack[slot].getAmount();
			s += ";";
			s += stack[slot].getTypeId();
			s += ";";
			s += stack[slot].getDurability();
			s += ";";
			for (Enchantment enchantment : stack[slot].getEnchantments().keySet())
			{
				s += enchantment.getId();
				s += ",";
				s += stack[slot].getEnchantments().get(enchantment);
				s += ":";
			}
			s += "/";
		}
		
		return s;
	}
	
	public static ItemStack[] deserializeItemStack(String s) //TODO Finish deserializeItemStack
	{
		ItemStack[] stack = new ItemStack[s.split("/").length];

		String[] slots = s.split("/");
		
		/*	String property = getProperty(slot);
			
			String[] parts = property.split(";");
			
			int amount = Integer.parseInt(parts[0]);
			int type = Integer.parseInt(parts[1]);
			short durability = Short.parseShort(parts[2]);
			
			Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
			
			if (parts.length > 3)
			{
				for (String enchantment : parts[3].split(":"))
				{
					enchantments.put(Enchantment.getById(Integer.parseInt(enchantment.split(",")[0])), Integer.parseInt(enchantment.split(",")[1]));
				}
			}
			
			ItemStack item = new ItemStack(Material.getMaterial(type));
			item.setAmount(amount);
			item.setDurability(durability);
			item.addEnchantments(enchantments);
			*/
		
		
		return stack;
	}
	
}
