package com.bendude56.goldenapple.lock;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.util.Serializer;

/**
 * Represents a GoldenApple lock on a block. This class can represent
 * <em>any</em> block, including blocks that have been added by a third party
 * plugin.
 * 
 * @author ben_dude56
 */
public abstract class LockedBlock {
	private static List<RegisteredBlock>	registeredBlocks	= new ArrayList<RegisteredBlock>();
	private static List<ILocationCorrector>	locationCorrectors	= new ArrayList<ILocationCorrector>();

	public static void registerBlock(String identifier, Plugin plugin, Material blockType, Class<? extends LockedBlock> blockClass) {
		for (RegisteredBlock b : registeredBlocks) {
			if (b.identifier == identifier) {
				throw new IllegalArgumentException("Identifier " + identifier + " already registered to another class!");
			}
		}
		registeredBlocks.add(new RegisteredBlock(identifier, plugin, blockType, blockClass));
	}

	public static void unregisterBlock(String identifier) {
		for (int i = 0; i < registeredBlocks.size(); i++) {
			if (registeredBlocks.get(i).identifier == identifier) {
				registeredBlocks.remove(i);
			}
		}
	}

	public static void registerCorrector(Class<? extends ILocationCorrector> corrector) {
		for (ILocationCorrector c : locationCorrectors) {
			if (corrector.isInstance(c)) {
				throw new IllegalArgumentException("Location corrector already registered!");
			}
		}
		try {
			locationCorrectors.add(corrector.newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void unregisterCorrector(Class<? extends ILocationCorrector> corrector) {
		for (int i = 0; i < locationCorrectors.size(); i++) {

		}
	}

	static {
		registerBlock("GA_CHEST", GoldenApple.getInstance(), Material.CHEST, LockedChest.class);
		registerBlock("GA_FURNACE", GoldenApple.getInstance(), Material.FURNACE, null);
	}

	private final Location	l;
	private long			ownerId;
	private ArrayList<Long>	guests;
	private LockLevel		level;

	@SuppressWarnings("unchecked")
	protected LockedBlock(ResultSet r) throws SQLException, ClassNotFoundException, IOException {
		this.l = Serializer.deserializeLocation(r.getString("Location"));
		this.ownerId = r.getLong("Owner");
		this.guests = (ArrayList<Long>)Serializer.deserialize(r.getString("Guests"));
		this.level = LockLevel.getLevel(r.getInt("Level"));
	}

	protected LockedBlock(Location l, long ownerId, LockLevel level) {
		this.l = l;
		this.ownerId = ownerId;
		this.guests = new ArrayList<Long>();
		this.level = level;
	}

	/**
	 * Gets the location of this locked block
	 */
	public final Location getLocation() {
		return l;
	}

	/**
	 * Gets the ID of the user that owns this block. Returns -1 if the block has
	 * no owner.
	 */
	public final long getOwner() {
		return ownerId;
	}

	/**
	 * Changes the user that owns this block. Pass -1 to make this block owned
	 * by no one player.
	 * <p>
	 * <em><strong>Note:</strong> When overriding this method, <strong>never
	 * </strong> cancel the event! Cancelling this call may have unintended
	 * consequences!</em>
	 * 
	 * @param ownerId The ID of the user that should be the new owner of this
	 *            block.
	 */
	public void setOwner(long ownerId) {
		this.ownerId = ownerId;
	}

	/**
	 * Gets a <em>read-only</em> list of guests that have limited access to this
	 * block.
	 */
	public final List<Long> getGuests() {
		return Collections.unmodifiableList(guests);
	}

	/**
	 * Adds a user to the list of guests that can have limited access. A user
	 * appointed as a guest will be able to use the block and change the
	 * contents of the block (if applicable).
	 * 
	 * @param guestId The ID of the user that should be added to the guest list
	 *            of this block.
	 */
	public void addGuest(long guestId) {
		if (!guests.contains(guestId))
			guests.add(guestId);
	}

	/**
	 * Removes a user from the guest list of this block.
	 * 
	 * <em><strong>Note:</strong> When overriding this method, <strong>never
	 * </strong> cancel the event! Cancelling this call may have unintended
	 * consequences!</em>
	 * 
	 * @param guestId The ID of the user that should be removed from the guest
	 *            list of this block.
	 */
	public void remGuest(long guestId) {
		if (guests.contains(guestId))
			guests.remove(guestId);
	}

	/**
	 * Gets the level of protection on this block.
	 */
	public final LockLevel getLevel() {
		return level;
	}

	/**
	 * Sets the level of protection on this block.
	 * 
	 * @param level The level to set the protection on this block to.
	 */
	public void setLevel(LockLevel level) {
		this.level = level;
	}

	/**
	 * Checks whether or not the user may use this block. Use in this context is
	 * defined as being able to use right-click actions and, if applicable,
	 * modify the contents of the block represented by this protection.
	 * 
	 * @param user The user that is being checked for access to this block.
	 * @return True if the user is allowed to use this block, false otherwise.
	 */
	public boolean canUse(User user) {
		return (user.getId() == ownerId || guests.contains(user.getId()));
	}

	/**
	 * Checks whether or not the user may edit this block. Edit in this context
	 * is defined as being able to destroy the block, or change the properties
	 * of the lock on this block.
	 * 
	 * @param user The user that is being checked for access to this block.
	 * @return True if the user is allowed to edit this block, false otherwise.
	 */
	public boolean canEdit(User user) {
		return (user.getId() == ownerId);
	}

	public static enum LockLevel {
		UNKNOWN(-1), PRIVATE(0), PUBLIC(1);

		public int	levelId;

		LockLevel(int levelId) {
			this.levelId = levelId;
		}

		public static LockLevel getLevel(int levelId) {
			for (LockLevel l : LockLevel.values()) {
				if (l.levelId == levelId)
					return l;
			}
			return LockLevel.UNKNOWN;
		}
	}

	public static class RegisteredBlock {
		public final String							identifier;
		public final Plugin							plugin;
		public final Material						blockType;
		public final Class<? extends LockedBlock>	blockClass;

		private RegisteredBlock(String identifier, Plugin plugin, Material blockType, Class<? extends LockedBlock> blockClass) {
			this.identifier = identifier;
			this.plugin = plugin;
			this.blockType = blockType;
			this.blockClass = blockClass;
		}
	}
}
