package com.bendude56.goldenapple.select;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.World;

import com.bendude56.goldenapple.User;

public class SimpleSelectionProvider implements ISelectionProvider {
    private HashMap<Long, Location> selections1 = new HashMap<Long, Location>();
    private HashMap<Long, Location> selections2 = new HashMap<Long, Location>();
    
    @Override
    public String getProviderName() {
        return "Builtin";
    }
    
    @Override
    public boolean isSelectionMade(User user) {
        return selections1.containsKey(user.getId()) && selections2.containsKey(user.getId()) && selections1.get(user.getId()).getWorld().equals(selections2.get(user.getId()).getWorld());
    }
    
    @Override
    public Location getSelectionMinimum(User user) {
        Location sel1 = selections1.get(user.getId());
        Location sel2 = selections2.get(user.getId());
        
        return new Location(sel1.getWorld(), Math.min(sel1.getX(), sel2.getX()), Math.min(sel1.getY(), sel2.getY()), Math.min(sel1.getZ(), sel2.getZ()));
    }
    
    @Override
    public Location getSelectionMaximum(User user) {
        Location sel1 = selections1.get(user.getId());
        Location sel2 = selections2.get(user.getId());
        
        return new Location(sel1.getWorld(), Math.max(sel1.getX(), sel2.getX()), Math.max(sel1.getY(), sel2.getY()), Math.max(sel1.getZ(), sel2.getZ()));
    }
    
    @Override
    public World getSelectionWorld(User user) {
        return isSelectionMade(user) ? selections1.get(user.getId()).getWorld() : null;
    }
    
    public int expandSelection(User user, int blocks, Direction direction) {
        Location sel1 = selections1.get(user.getId());
        Location sel2 = selections2.get(user.getId());
        if (blocks < 0) {
            blocks = -blocks;
            direction = direction.getOpposite();
        }
        
        switch (direction) {
            case NORTH:
                if (sel1.getZ() > sel2.getZ()) {
                    sel2.setZ(sel2.getZ() - blocks);
                } else {
                    sel1.setZ(sel1.getZ() - blocks);
                }
                break;
            case SOUTH:
                if (sel1.getZ() > sel2.getZ()) {
                    sel1.setZ(sel1.getZ() + blocks);
                } else {
                    sel2.setZ(sel2.getZ() + blocks);
                }
                break;
            case WEST:
                if (sel1.getX() > sel2.getX()) {
                    sel2.setX(sel2.getX() - blocks);
                } else {
                    sel1.setX(sel1.getX() - blocks);
                }
                break;
            case EAST:
                if (sel1.getX() > sel2.getX()) {
                    sel1.setX(sel1.getX() + blocks);
                } else {
                    sel2.setX(sel2.getX() + blocks);
                }
                break;
            case DOWN:
                if (sel1.getY() > sel2.getY()) {
                    sel2.setY(sel2.getY() - blocks);
                    
                    if (sel2.getY() < 0) {
                        blocks += sel2.getBlockY();
                        sel2.setY(0);
                    }
                } else {
                    sel1.setY(sel1.getY() - blocks);
                    
                    if (sel1.getY() < 0) {
                        blocks += sel1.getBlockY();
                        sel1.setY(0);
                    }
                }
                break;
            case UP:
                if (sel1.getY() > sel2.getY()) {
                    sel1.setY(sel1.getY() + blocks);
                    
                    if (sel1.getY() > sel1.getWorld().getMaxHeight()) {
                        blocks -= (sel1.getBlockY() - sel1.getWorld().getMaxHeight());
                        sel1.setY(sel1.getWorld().getMaxHeight());
                    }
                } else {
                    sel2.setY(sel2.getY() + blocks);
                    
                    if (sel2.getY() > sel2.getWorld().getMaxHeight()) {
                        blocks -= (sel2.getBlockY() - sel2.getWorld().getMaxHeight());
                        sel2.setY(sel2.getWorld().getMaxHeight());
                    }
                }
                break;
            default:
                return 0;
        }
        return blocks;
    }
    
    public int contractSelection(User user, int blocks, Direction direction) {
        Location sel1 = selections1.get(user.getId());
        Location sel2 = selections2.get(user.getId());
        if (blocks < 0) {
            blocks = -blocks;
            direction = direction.getOpposite();
        }
        
        switch (direction) {
            case NORTH:
                if (sel1.getZ() > sel2.getZ()) {
                    sel2.setZ(sel2.getZ() + blocks);
                    
                    if (sel2.getZ() > sel1.getZ()) {
                        blocks -= sel2.getZ() - sel1.getZ();
                        sel2.setZ(sel1.getZ());
                    }
                } else {
                    sel1.setZ(sel1.getZ() + blocks);
                    
                    if (sel1.getZ() > sel2.getZ()) {
                        blocks -= sel1.getZ() - sel2.getZ();
                        sel1.setZ(sel2.getZ());
                    }
                }
                break;
            case SOUTH:
                if (sel1.getZ() > sel2.getZ()) {
                    sel1.setZ(sel1.getZ() - blocks);
                    
                    if (sel2.getZ() > sel1.getZ()) {
                        blocks -= sel2.getZ() - sel1.getZ();
                        sel1.setZ(sel2.getZ());
                    }
                } else {
                    sel2.setZ(sel2.getZ() - blocks);
                    
                    if (sel1.getZ() > sel2.getZ()) {
                        blocks -= sel1.getZ() - sel2.getZ();
                        sel2.setZ(sel1.getZ());
                    }
                }
                break;
            case WEST:
                if (sel1.getX() > sel2.getX()) {
                    sel2.setX(sel2.getX() + blocks);
                    
                    if (sel2.getX() > sel1.getX()) {
                        blocks -= sel2.getX() - sel1.getX();
                        sel2.setX(sel1.getX());
                    }
                } else {
                    sel1.setX(sel1.getX() + blocks);
                    
                    if (sel1.getX() > sel2.getX()) {
                        blocks -= sel1.getX() - sel2.getX();
                        sel1.setX(sel2.getX());
                    }
                }
                break;
            case EAST:
                if (sel1.getX() > sel2.getX()) {
                    sel1.setX(sel1.getX() - blocks);
                    
                    if (sel2.getX() > sel1.getX()) {
                        blocks -= sel2.getX() - sel1.getX();
                        sel1.setX(sel2.getX());
                    }
                } else {
                    sel2.setX(sel2.getX() - blocks);
                    
                    if (sel1.getX() > sel2.getX()) {
                        blocks -= sel1.getX() - sel2.getX();
                        sel2.setX(sel1.getX());
                    }
                }
                break;
            case DOWN:
                if (sel1.getY() > sel2.getY()) {
                    sel2.setY(sel2.getY() + blocks);
                    
                    if (sel2.getY() > sel1.getY()) {
                        blocks -= sel2.getY() - sel1.getY();
                        sel2.setY(sel1.getY());
                    }
                } else {
                    sel1.setY(sel1.getY() + blocks);
                    
                    if (sel1.getY() > sel2.getY()) {
                        blocks -= sel1.getY() - sel2.getY();
                        sel1.setY(sel2.getY());
                    }
                }
                break;
            case UP:
                if (sel1.getY() > sel2.getY()) {
                    sel1.setY(sel1.getY() - blocks);
                    
                    if (sel2.getY() > sel1.getY()) {
                        blocks -= sel2.getY() - sel1.getY();
                        sel1.setY(sel2.getY());
                    }
                } else {
                    sel2.setY(sel2.getY() - blocks);
                    
                    if (sel1.getY() > sel2.getY()) {
                        blocks -= sel1.getY() - sel2.getY();
                        sel2.setY(sel1.getY());
                    }
                }
                break;
            default:
                return 0;
        }
        return blocks;
    }
    
    public int shiftSelection(User user, int blocks, Direction direction) {
        Location sel1 = selections1.get(user.getId());
        Location sel2 = selections2.get(user.getId());
        if (blocks < 0) {
            blocks = -blocks;
            direction = direction.getOpposite();
        }
        
        switch (direction) {
            case NORTH:
                sel1.setZ(sel1.getZ() - blocks);
                sel2.setZ(sel2.getZ() - blocks);
                break;
            case SOUTH:
                sel1.setZ(sel1.getZ() + blocks);
                sel2.setZ(sel2.getZ() + blocks);
                break;
            case WEST:
                sel1.setX(sel1.getX() - blocks);
                sel2.setX(sel2.getX() - blocks);
                break;
            case EAST:
                sel1.setX(sel1.getX() + blocks);
                sel2.setX(sel2.getX() + blocks);
                break;
            case DOWN:
                if (Math.min(sel1.getY(), sel2.getY()) < blocks) {
                    blocks = (int) Math.min(sel1.getY(), sel2.getY());
                }
                sel1.setY(sel1.getY() - blocks);
                sel2.setY(sel2.getY() - blocks);
                break;
            case UP:
                if (Math.max(sel1.getY(), sel2.getY()) > sel1.getWorld().getMaxHeight() - blocks) {
                    blocks = sel1.getWorld().getMaxHeight() - (int) Math.max(sel1.getY(), sel2.getY());
                }
                sel1.setY(sel1.getY() + blocks);
                sel2.setY(sel2.getY() + blocks);
                break;
            default:
                return 0;
        }
        return blocks;
    }
    
    public void clearSelection(User user) {
        clearSelection(user.getId());
    }
    
    public void clearSelection(Long id) {
        selections1.remove(id);
        selections2.remove(id);
    }
    
    public void setSelection1(User user, Location l) {
        selections1.put(user.getId(), l);
    }
    
    public void setSelection2(User user, Location l) {
        selections2.put(user.getId(), l);
    }
    
    public enum Direction {
        NORTH("shared.location.north", "north"),
        EAST("shared.location.east", "east"),
        SOUTH("shared.location.south", "south"),
        WEST("shared.location.west", "west"),
        UP("shared.location.up", "up"),
        DOWN("shared.location.down", "down");
        
        public final String friendlyName;
        public final String commandName;
        
        private Direction(String friendlyName, String commandName) {
            this.friendlyName = friendlyName;
            this.commandName = commandName;
        }
        
        public String getFriendlyName(User user) {
            return user.getLocalizedMessage(friendlyName);
        }
        
        public String getCommandName(User user) {
            return commandName;
        }
        
        public static Direction fromString(String input, User user) {
            for (Direction d : Direction.values()) {
                if (input.equalsIgnoreCase(d.getCommandName(user)) || input.equalsIgnoreCase(d.getCommandName(user).substring(0, 1))) {
                    return d;
                }
            }
            
            return null;
        }
        
        public static Direction fromYawPitch(float yaw, float pitch) {
            // Normalize the YAW
            yaw %= 360;
            if (yaw < -180) {
                yaw += 360;
            }
            if (yaw > 180) {
                yaw -= 360;
            }
            
            // Run comparisons
            if (pitch > 60.0) {
                return DOWN;
            }
            if (pitch < -60.0) {
                return UP;
            }
            if (yaw > 135.0 || yaw < -135.0) {
                return NORTH;
            }
            if (yaw <= 45.0 && yaw >= -45.0) {
                return SOUTH;
            }
            if (yaw > 45.0 && yaw <= 135.0) {
                return WEST;
            }
            if (yaw < -45.0 && yaw >= -135.0) {
                return EAST;
            }
            return null;
        }
        
        public Direction getOpposite() {
            switch (this) {
                case NORTH:
                    return SOUTH;
                case SOUTH:
                    return NORTH;
                case WEST:
                    return EAST;
                case EAST:
                    return WEST;
                case DOWN:
                    return UP;
                case UP:
                    return DOWN;
                default:
                    return null;
            }
        }
    }
}
