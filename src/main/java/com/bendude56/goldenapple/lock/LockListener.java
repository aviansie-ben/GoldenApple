package com.bendude56.goldenapple.lock;

import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.PerformanceMonitor.PerformanceEvent;
import com.bendude56.goldenapple.lock.LockManager;
import com.bendude56.goldenapple.lock.LockedBlock;
import com.bendude56.goldenapple.lock.LockedBlock.GuestLevel;
import com.bendude56.goldenapple.lock.LockedBlock.LockLevel;
import com.bendude56.goldenapple.permissions.PermissionManager;

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
		BlockExpEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		BlockPlaceEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		InventoryMoveItemEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		BlockRedstoneEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		BlockDispenseEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
	}

	private void unregisterEvents() {
		PlayerInteractEvent.getHandlerList().unregister(this);
		BlockExpEvent.getHandlerList().unregister(this);
		BlockPlaceEvent.getHandlerList().unregister(this);
		InventoryMoveItemEvent.getHandlerList().unregister(this);
		BlockRedstoneEvent.getHandlerList().unregister(this);
		BlockDispenseEvent.getHandlerList().unregister(this);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		PerformanceEvent e = GoldenApple.getInstancePerformanceMonitor().createForEvent("Lock", event.getClass().getName());
		e.start();
		
		try {
			if (event instanceof PlayerInteractEvent) {
				playerInteract((PlayerInteractEvent)event);
			} else if (event instanceof BlockBreakEvent) {
				blockBreak((BlockBreakEvent)event);
			} else if (event instanceof BlockPlaceEvent) {
				chestMove((BlockPlaceEvent)event);
				autoLock((BlockPlaceEvent)event);
			} else if (event instanceof InventoryMoveItemEvent) {
				itemMove((InventoryMoveItemEvent)event);
			} else if (event instanceof BlockRedstoneEvent) {
				lockRedstone((BlockRedstoneEvent)event);
			} else if (event instanceof BlockDispenseEvent) {
				lockDispense((BlockDispenseEvent)event);
			} else if (event instanceof BlockExpEvent) {
			} else {
				GoldenApple.log(Level.WARNING, "Unrecognized event in LockListener: " + event.getClass().getName());
			}
		} finally {
			e.stop();
		}
	}

	private void playerInteract(PlayerInteractEvent event) {
		if (!LockManager.getInstance().isLockable(event.getClickedBlock().getType())) return;
		
		LockedBlock lock = LockManager.getInstance().getLock(event.getClickedBlock().getLocation());
		User u = User.getUser(event.getPlayer());
		if (lock == null)
			return;

		if (!lock.canUse(u)) {
			u.sendLocalizedMessage("error.lock.noUse", PermissionManager.getInstance().getUser(lock.getOwner()).getName());
			if (lock.getOverrideLevel(u).levelId >= GuestLevel.USE.levelId) {
				u.sendLocalizedMessage((u.isUsingComplexCommands()) ? "general.lock.overrideAvailable.complex" : "general.lock.overrideAvailable.simple");
			}
			event.setCancelled(true);
			return;
		}
	}

	private void blockBreak(BlockBreakEvent event) {
		if (!LockManager.getInstance().isLockable(event.getBlock().getType())) return;
		
		LockedBlock lock = LockManager.getInstance().getLock(event.getBlock().getLocation());
		User u = User.getUser(event.getPlayer());
		if (lock == null)
			return;

		if (!lock.canModifyBlock(u)) {
			u.sendLocalizedMessage("error.lock.noEdit");
			if (lock.getOverrideLevel(u).levelId >= GuestLevel.ALLOW_BLOCK_MODIFY.levelId) {
				u.sendLocalizedMessage((u.isUsingComplexCommands()) ? "general.lock.overrideAvailable.complex" : "general.lock.overrideAvailable.simple");
			}
			event.setCancelled(true);
			return;
		} else {
			if (event.getBlock().getType() == Material.CHEST && chestDeleteCheck(lock, event.getBlock().getLocation()))
				return;
			try {
				LockManager.getInstance().deleteLock(lock.getLockId());
				u.sendLocalizedMessage("general.lock.delete.success");
			} catch (SQLException e) {
				event.setCancelled(true);
				u.sendLocalizedMessage("error.lock.delete.ioError");
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
		LockedBlock lock = LockManager.getInstance().getLockSpecific(l);

		if (lock != null) {
			LockedBlock.correctLocation(l);
			lock.moveLock(l);
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	private void autoLock(BlockPlaceEvent event) {
		User user = User.getUser(event.getPlayer());

		// TODO getTypeId() is deprecated. Look at alternatives.
		if (user.isAutoLockEnabled() && user.hasPermission(LockManager.addPermission) && GoldenApple.getInstanceMainConfig().getIntegerList("modules.lock.autoLockBlocks").contains(event.getBlock().getTypeId()) && LockManager.getInstance().getLock(event.getBlock().getLocation()) == null) {
			try {
				LockManager.getInstance().createLock(event.getBlock().getLocation(), LockLevel.PRIVATE, user);
				user.sendLocalizedMessage("general.lock.auto");
			} catch (Exception e) {
			}
		}
	}
	
	private void itemMove(InventoryMoveItemEvent event) {
		LockedBlock lockSource, lockDestination;
		
		if (event.getDestination().getHolder() instanceof BlockState && event.getSource().getHolder() instanceof BlockState) {
			lockDestination = LockManager.getInstance().getLock(((BlockState) event.getDestination().getHolder()).getLocation());
			lockSource = LockManager.getInstance().getLock(((BlockState) event.getSource().getHolder()).getLocation());
			
			if (lockDestination != null && !lockDestination.getAllowExternal() && (lockSource == null || lockSource.getOwner() != lockDestination.getOwner())) {
				event.setCancelled(true);
			} else if (lockSource != null && !lockSource.getAllowExternal() && (lockDestination == null || lockSource.getOwner() != lockDestination.getOwner())) {
				event.setCancelled(true);
			}
		}
	}
	
	private void lockRedstone(BlockRedstoneEvent event) {
		if (!LockManager.getInstance().isLockable(event.getBlock().getType())) return;
		
		LockedBlock lock = LockManager.getInstance().getLock(event.getBlock().getLocation());
		
		if (lock != null && !lock.getAllowExternal() && lock.isRedstoneAccessApplicable())
			event.setNewCurrent(event.getOldCurrent());
	}
	
	private void lockDispense(BlockDispenseEvent event) {
		LockedBlock lock = LockManager.getInstance().getLock(event.getBlock().getLocation());
		
		if (lock != null && !lock.getAllowExternal() && lock.isRedstoneAccessApplicable())
			event.setCancelled(true);
	}
}
