package com.bendude56.goldenapple.area;

import java.util.logging.Level;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.projectiles.ProjectileSource;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.PerformanceMonitor.PerformanceEvent;
import com.bendude56.goldenapple.User;

public class AreaListener implements Listener, EventExecutor {
    
    private static AreaListener listener;
    
    public static void startListening() {
        listener = new AreaListener();
        listener.registerEvents();
    }
    
    public static void stopListening() {
        if (listener != null) {
            listener.unregisterEvents();
            listener = null;
        }
    }
    
    private void registerEvents() {
        BlockBreakEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        BlockPlaceEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        PlayerBucketFillEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        PlayerBucketEmptyEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        HangingBreakByEntityEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        HangingPlaceEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        VehicleDamageEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        PlayerInteractEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        PlayerInteractEntityEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        EntityDamageByEntityEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
    }
    
    private void unregisterEvents() {
        BlockBreakEvent.getHandlerList().unregister(this);
        BlockPlaceEvent.getHandlerList().unregister(this);
        PlayerBucketFillEvent.getHandlerList().unregister(this);
        PlayerBucketEmptyEvent.getHandlerList().unregister(this);
        HangingBreakByEntityEvent.getHandlerList().unregister(this);
        HangingPlaceEvent.getHandlerList().unregister(this);
        VehicleDamageEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerInteractEntityEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }
    
    @Override
    public void execute(Listener listener, Event event) throws EventException {
        PerformanceEvent e = GoldenApple.getInstancePerformanceMonitor().createForEvent("Area", event.getClass().getName());
        e.start();
        
        try {
            if (event instanceof BlockBreakEvent) {
                blockBreak((BlockBreakEvent) event);
            } else if (event instanceof BlockPlaceEvent) {
                blockPlace((BlockPlaceEvent) event);
            } else if (event instanceof PlayerBucketFillEvent) {
                bucketFill((PlayerBucketFillEvent) event);
            } else if (event instanceof PlayerBucketEmptyEvent) {
                bucketEmpty((PlayerBucketEmptyEvent) event);
            } else if (event instanceof HangingBreakByEntityEvent) {
                hangingBreak((HangingBreakByEntityEvent) event);
            } else if (event instanceof HangingPlaceEvent) {
                hangingPlace((HangingPlaceEvent) event);
            } else if (event instanceof VehicleDamageEvent) {
                vehicleDamage((VehicleDamageEvent) event);
            } else if (event instanceof PlayerInteractEvent) {
                blockInteract((PlayerInteractEvent) event);
            } else if (event instanceof PlayerInteractEntityEvent) {
                entityInteract((PlayerInteractEntityEvent) event);
            } else if (event instanceof EntityDamageByEntityEvent) {
                attackEntity((EntityDamageByEntityEvent) event);
            } else if (event instanceof EntityDamageEvent) {
                // Do nothing
            } else if (event instanceof BlockExpEvent) {
                // Do nothing
            } else {
                GoldenApple.log(Level.WARNING, "Unrecognized event in AreaListener: " + event.getClass().getName());
            }
        } finally {
            e.stop();
        }
        
    }
    
    private void blockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        User u = User.getUser(event.getPlayer());
        
        if (AreaManager.getInstance().canEditAtLocation(u, event.getBlock().getLocation())) {
            return;
        }
        
        event.setCancelled(true);
        u.sendLocalizedMessage("module.area.error.noEdit");
    }
    
    private void blockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        User u = User.getUser(event.getPlayer());
        
        if (AreaManager.getInstance().canEditAtLocation(u, event.getBlock().getLocation())) {
            return;
        }
        
        event.setCancelled(true);
        u.sendLocalizedMessage("module.area.error.noEdit");
    }
    
    private void bucketFill(PlayerBucketFillEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        User u = User.getUser(event.getPlayer());
        
        if (AreaManager.getInstance().canEditAtLocation(u, event.getBlockClicked().getLocation())) {
            return;
        }
        
        event.setCancelled(true);
        u.sendLocalizedMessage("module.area.error.noEdit");
    }
    
    private void bucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        User u = User.getUser(event.getPlayer());
        
        if (AreaManager.getInstance().canEditAtLocation(u, event.getBlockClicked().getLocation())) {
            return;
        }
        
        event.setCancelled(true);
        u.sendLocalizedMessage("module.area.error.noEdit");
    }
    
    private void hangingBreak(HangingBreakByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        User u = null;
        
        if (event.getRemover() instanceof Player) {
            u = User.getUser((Player) event.getRemover());
        } else if (event.getRemover() instanceof Projectile) {
            if (((Projectile) event.getRemover()).getShooter() instanceof Player) {
                u = User.getUser((Player) ((Projectile) event.getRemover()).getShooter());
            }
        }
        
        if (u == null) {
            return;
        }
        
        if (AreaManager.getInstance().canEditAtLocation(u, event.getEntity().getLocation())) {
            return;
        }
        
        event.setCancelled(true);
        u.sendLocalizedMessage("module.area.error.noEdit");
    }
    
    private void hangingPlace(HangingPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        User u = User.getUser(event.getPlayer());
        
        if (u == null) {
            return;
        }
        
        if (AreaManager.getInstance().canEditAtLocation(u, event.getEntity().getLocation())) {
            return;
        }
        
        event.setCancelled(true);
        u.sendLocalizedMessage("module.area.error.noEdit");
    }
    
    private void vehicleDamage(VehicleDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        if (event.getAttacker() == null) {
            return;
        }
        
        User u = null;
        
        if (event.getAttacker() instanceof Player) {
            u = User.getUser((Player) event.getAttacker());
        } else if (event.getAttacker() instanceof Projectile) {
            if (((Projectile) event.getAttacker()).getShooter() instanceof Player) {
                u = User.getUser((Player) ((Projectile) event.getAttacker()).getShooter());
            }
        }
        
        if (u == null) {
            return;
        }
        
        if (AreaManager.getInstance().canEditAtLocation(u, event.getVehicle().getLocation())) {
            return;
        }
        
        event.setCancelled(true);
        u.sendLocalizedMessage("module.area.error.noEdit");
    }
    
    private void blockInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        User u = User.getUser(event.getPlayer());
        
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                break;
            default:
                return;
        }
        
        switch (event.getClickedBlock().getType()) {
            case RAILS:
            case POWERED_RAIL:
            case DETECTOR_RAIL:
            case ACTIVATOR_RAIL:
                switch (event.getPlayer().getItemInHand().getType()) {
                    case MINECART:
                    case POWERED_MINECART:
                    case STORAGE_MINECART:
                    case EXPLOSIVE_MINECART:
                    case HOPPER_MINECART:
                    case COMMAND_MINECART:
                        break;
                    default:
                        return;
                }
                break;
            case CAKE_BLOCK:
                break;
            default:
                return;
        }
        
        if (AreaManager.getInstance().canEditAtLocation(u, event.getClickedBlock().getLocation())) {
            return;
        }
        
        event.setCancelled(true);
        u.sendLocalizedMessage("module.area.error.noEdit");
    }
    
    private void entityInteract(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        User u = User.getUser(event.getPlayer());
        if (u == null) {
            return;
        }
        
        switch (event.getRightClicked().getType()) {
            case ITEM_FRAME:
            case ENDER_CRYSTAL:
            case LEASH_HITCH:
            case HORSE:
            case SHEEP:
            case PIG:
            case WOLF:
            case OCELOT:
            case COW:
            case MINECART_TNT:
                break;
            default:
                switch (event.getPlayer().getItemInHand().getType()) {
                    case NAME_TAG:
                    case LEASH:
                    case SADDLE:
                        break;
                    default:
                        return;
                }
                break;
        }
        
        if (AreaManager.getInstance().canEditAtLocation(u, event.getRightClicked().getLocation())) {
            return;
        }
        
        event.setCancelled(true);
        u.sendLocalizedMessage("module.area.error.noEdit");
    }
    
    private void attackEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        boolean projectile; // User is attacking via projectile
        boolean victim; // User is victim here
        User u; // The user to check permissions for
        Entity e; // Non-user entity. (Could be player, but that is not
                  // relevant)
        Entity damagee; // Shorthand for damagee
        Entity damager; // Shorthand for damager
        ProjectileSource source;
        
        damagee = event.getEntity();
        damager = event.getDamager();
        
        // If the damage is caused by a projectile, figure out what shot it
        if (damager instanceof Projectile) {
            source = ((Projectile) damager).getShooter();
            if (source instanceof Entity) {
                damager = (Entity) source;
            } else {
                return;
            }
            projectile = true;
        } else {
            projectile = false;
        }
        
        // Determine whether the user is attacking or being attacked
        if (damager instanceof Player) {
            u = User.getUser((Player) damager);
            e = damagee;
            victim = false;
        } else if (damagee instanceof Player) {
            u = User.getUser((Player) damagee);
            e = damager;
            victim = true;
        } else {
            return; // A user is not involved. Short-circuit to avoid errors
        }
        
        switch (e.getType()) {
            case PAINTING:
            case ITEM_FRAME:
            case COW:
            case MUSHROOM_COW:
            case PIG:
            case SHEEP:
            case CHICKEN:
            case WOLF:
            case OCELOT:
            case HORSE:
            case ENDER_CRYSTAL:
            case SNOWMAN:
            case BOAT:
            case MINECART:
            case MINECART_FURNACE:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_HOPPER:
            case MINECART_TNT:
            case MINECART_MOB_SPAWNER:
                break;
            default:
                if (e instanceof LivingEntity && ((LivingEntity) e).getCustomName() != null) {
                    break;
                }
                return;
        }
        
        if (AreaManager.getInstance().canEditAtLocation(u, damagee.getLocation())) {
            return;
        }
        
        event.setCancelled(true);
        if (!projectile && !victim) {
            u.sendLocalizedMessage((e.getType() == EntityType.ITEM_FRAME ? "module.area.error.noEdit" : "module.area.error.noAttack"));
        }
    }
    
}
