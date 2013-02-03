package com.bendude56.goldenapple.listener;

import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.lock.LockManager;
import com.bendude56.goldenapple.lock.LockedBlock;
import com.bendude56.goldenapple.lock.LockedBlock.LockLevel;

public class LockListener implements Listener, EventExecutor {

	private static LockListener	listener;

	public static void startListening() {
		listener = new LockListener();
		listener.registerEvents();
	}

	public static void stopListening() {
		if (listener != null) {
			listener.unregisterEvents();
			listener = null;
		}
	}

	private void registerEvents() {
		PlayerInteractEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		BlockBreakEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		BlockPlaceEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.MONITOR, GoldenApple.getInstance(), true));
	}

	private void unregisterEvents() {
		PlayerInteractEvent.getHandlerList().unregister(this);
		BlockBreakEvent.getHandlerList().unregister(this);
		BlockPlaceEvent.getHandlerList().unregister(this);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if (event instanceof PlayerInteractEvent) {
			playerInteract((PlayerInteractEvent)event);
		} else if (event instanceof BlockBreakEvent) {
			blockBreak((BlockBreakEvent)event);
		} else if (event instanceof BlockPlaceEvent) {
			chestMove((BlockPlaceEvent)event);
			autoLock((BlockPlaceEvent)event);
		} else {
			GoldenApple.log(Level.WARNING, "Unrecognized event in LockListener: " + event.getClass().getName());
		}
	}

	private void playerInteract(PlayerInteractEvent event) {
		LockedBlock lock = GoldenApple.getInstance().locks.getLock(event.getClickedBlock().getLocation());
		User u = User.getUser(event.getPlayer());
		if (lock == null)
			return;

		if (!lock.canUse(u)) {
			GoldenApple.getInstance().locale.sendMessage(u, "error.lock.noUse", false, GoldenApple.getInstance().permissions.getUser(lock.getOwner()).getName());
			event.setCancelled(true);
			return;
		}
	}

	private void blockBreak(BlockBreakEvent event) {
		LockedBlock lock = GoldenApple.getInstance().locks.getLock(event.getBlock().getLocation());
		User u = User.getUser(event.getPlayer());
		if (lock == null)
			return;

		if (!lock.canModifyBlock(u)) {
			GoldenApple.getInstance().locale.sendMessage(u, "error.lock.noEdit", false);
			event.setCancelled(true);
			return;
		} else {
			if (event.getBlock().getType() == Material.CHEST && chestDeleteCheck(lock, event.getBlock().getLocation()))
				return;
			try {
				GoldenApple.getInstance().locks.deleteLock(lock.getLockId());
				GoldenApple.getInstance().locale.sendMessage(u, "general.lock.delete.success", false);
			} catch (SQLException e) {
				event.setCancelled(true);
				GoldenApple.getInstance().locale.sendMessage(u, "error.lock.delete.ioError", false);
			}
		}
	}

	private boolean chestDeleteCheck(LockedBlock lock, Location l) {
		if (!l.equals(lock.getLocation()))
			return true;

		try {
			l.setX(l.getX() - 1);
			if (l.getBlock().getType() == Material.CHEST) {
				lock.moveLock(l);
				return true;
			}

			l.setX(l.getX() + 1);
			l.setZ(l.getZ() - 1);
			if (l.getBlock().getType() == Material.CHEST) {
				lock.moveLock(l);
				return true;
			}

			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private void chestMove(BlockPlaceEvent event) {
		GoldenApple instance = GoldenApple.getInstance();

		if (event.getBlock().getType() != Material.CHEST)
			return;

		Location l = event.getBlock().getLocation();

		try {
			l.setX(l.getX() - 1);
			if (adjustChestLock(instance, l))
				return;

			l.setX(l.getX() + 1);
			l.setZ(l.getZ() - 1);
			if (adjustChestLock(instance, l))
				return;
		} catch (Exception e) {}
	}

	private boolean adjustChestLock(GoldenApple instance, Location l) throws SQLException {
		LockedBlock lock = instance.locks.getLockSpecific(l);

		if (lock != null) {
			LockedBlock.correctLocation(l);
			lock.moveLock(l);
			return true;
		} else {
			return false;
		}
	}

	private void autoLock(BlockPlaceEvent event) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(event.getPlayer());

		if (user.isAutoLockEnabled() && user.hasPermission(LockManager.addPermission) && instance.mainConfig.getIntegerList("modules.lock.autoLockBlocks").contains(event.getBlock().getTypeId()) && instance.locks.getLock(event.getBlock().getLocation()) == null) {
			try {
				instance.locks.createLock(event.getBlock().getLocation(), LockLevel.PRIVATE, user);
				instance.locale.sendMessage(user, "general.lock.auto", false);
			} catch (Exception e) {
			}
		}
	}
}
