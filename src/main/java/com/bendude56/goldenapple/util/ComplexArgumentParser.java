package com.bendude56.goldenapple.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo.ArgumentType;

public class ComplexArgumentParser {
    private ArgumentInfo[] parserInfo;
    private HashMap<String, List<Object>> values;
    
    public ComplexArgumentParser(ArgumentInfo[] parserInfo) {
        this.parserInfo = parserInfo;
        this.values = new HashMap<String, List<Object>>();
    }
    
    public boolean parse(User u, String[] args) {
        return parse(u, args, 0, args.length);
    }
    
    public boolean parse(User u, String[] args, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            ArgumentInfo arg = null;
            String kvpKey = null;
            
            if (args[i].startsWith("--")) {
                // Indicates a long-form switch name
                for (ArgumentInfo a : parserInfo) {
                    if (args[i].substring(2).equalsIgnoreCase(a.longSwitch)) {
                        arg = a;
                        break;
                    } else if (args[i].substring(2).startsWith(a.longSwitch + ":") && args[i].length() > 3 + a.longSwitch.length()) {
                        arg = a.valueArgument;
                        kvpKey = args[i].substring(3 + a.longSwitch.length());
                        break;
                    }
                }
            } else if (args[i].startsWith("-")) {
                // Indicates a short-form switch name
                for (ArgumentInfo a : parserInfo) {
                    if (args[i].substring(1).equalsIgnoreCase(a.shortSwitch)) {
                        if (a.type == ArgumentType.KEY_VALUE_PAIR) {
                            arg = a.valueArgument;
                            kvpKey = "";
                        } else {
                            arg = a;
                        }
                        
                        break;
                    } else if (a.type == ArgumentType.KEY_VALUE_PAIR && args[i].substring(1).startsWith(a.shortSwitch + ":") && args[i].length() > 2 + a.shortSwitch.length()) {
                        arg = a.valueArgument;
                        kvpKey = args[i].substring(2 + a.shortSwitch.length());
                        break;
                    }
                }
            }
            
            if (arg == null) {
                // Not a valid switch
                if (u != null) {
                    u.sendLocalizedMessage("shared.parser.unknownOption", args[i]);
                }
                return false;
            } else if (arg.type == ArgumentType.SWITCH) {
                if (kvpKey != null) {
                    values.put(arg.argumentName, new ArrayList<Object>());
                    values.get(arg.argumentName).add(new AbstractMap.SimpleImmutableEntry<String, Object>(kvpKey, null));
                } else {
                    values.put(arg.argumentName, null);
                }
            } else if (++i >= offset + length) {
                // No value was defined
                if (u != null) {
                    u.sendLocalizedMessage("shared.parser.parameterMissing", args[i - 1]);
                }
                return false;
            } else {
                Object value = args[i];
                
                if (arg.type == ArgumentType.STRING) {
                    // Normal string. Leave it alone.
                } else if (arg.type == ArgumentType.STRING_WITH_SPACES) {
                    // Allow spaces in the string
                    while (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        value += " " + args[++i];
                    }
                } else if (arg.type == ArgumentType.INT) {
                    // Convert to an integer
                    try {
                        value = Integer.parseInt((String) value);
                    } catch (NumberFormatException e) {
                        if (u != null) {
                            u.sendLocalizedMessage("shared.convertError.number", (String) value);
                        }
                        return false;
                    }
                } else if (arg.type == ArgumentType.LONG) {
                    // Convert to a long
                    try {
                        value = Long.parseLong((String) value);
                    } catch (NumberFormatException e) {
                        if (u != null) {
                            u.sendLocalizedMessage("shared.convertError.number", (String) value);
                        }
                        return false;
                    }
                } else if (arg.type == ArgumentType.USER) {
                    // Offline user
                    String name = (String) value;
                    
                    value = PermissionManager.getInstance().findUser(name, false);
                    
                    if (value == null) {
                        if (u != null) {
                            u.sendLocalizedMessage((arg.errorIfNotFound) ? "shared.parser.userNotFound.error" : "shared.parser.userNotFound.warning", name);
                        }
                        if (arg.errorIfNotFound) {
                            return false;
                        } else {
                            continue;
                        }
                    }
                } else if (arg.type == ArgumentType.USER_ONLINE) {
                    String name = (String) value;
                    
                    value = User.findUser(name);
                    
                    if (value == null) {
                        if (u != null) {
                            u.sendLocalizedMessage((arg.errorIfNotFound) ? "shared.parser.userNotFound.error" : "shared.parser.userNotFound.warning", name);
                        }
                        if (arg.errorIfNotFound) {
                            return false;
                        } else {
                            continue;
                        }
                    }
                } else if (arg.type == ArgumentType.GROUP) {
                    String name = (String) value;
                    
                    value = PermissionManager.getInstance().getGroup(name);
                    
                    if (value == null) {
                        if (u != null) {
                            u.sendLocalizedMessage((arg.errorIfNotFound) ? "shared.parser.groupNotFound.error" : "shared.parser.groupNotFound.warning", name);
                        }
                        if (arg.errorIfNotFound) {
                            return false;
                        } else {
                            continue;
                        }
                    }
                } else if (arg.type == ArgumentType.WORLD) {
                    String name = (String) value;
                    
                    value = Bukkit.getWorld(name);
                    
                    if (value == null) {
                        if (u != null) {
                            u.sendLocalizedMessage((arg.errorIfNotFound) ? "shared.parser.worldNotFound.error" : "shared.parser.worldNotFound.warning", name);
                        }
                        if (arg.errorIfNotFound) {
                            return false;
                        } else {
                            continue;
                        }
                    }
                }
                
                if (arg.isList && values.containsKey(arg.argumentName)) {
                    if (kvpKey != null) {
                        values.get(arg.argumentName).add(new AbstractMap.SimpleImmutableEntry<String, Object>(kvpKey, value));
                    } else {
                        values.get(arg.argumentName).add(value);
                    }
                } else {
                    if (kvpKey != null) {
                        values.put(arg.argumentName, new ArrayList<Object>());
                        values.get(arg.argumentName).add(new AbstractMap.SimpleImmutableEntry<String, Object>(kvpKey, value));
                    } else {
                        values.put(arg.argumentName, new ArrayList<Object>());
                        values.get(arg.argumentName).add(value);
                    }
                }
            }
        }
        
        return true;
    }
    
    public boolean isDefined(String argument) {
        return values.containsKey(argument);
    }
    
    public Object getArgument(String argument) {
        if (!values.containsKey(argument)) {
            return null;
        }
        
        return values.get(argument).get(0);
    }
    
    public String getString(String argument) {
        return (String) getArgument(argument);
    }
    
    public int getInt(String argument) {
        return (int) getArgument(argument);
    }
    
    public long getLong(String argument) {
        return (long) getArgument(argument);
    }
    
    public IPermissionUser getUser(String argument) {
        return (IPermissionUser) getArgument(argument);
    }
    
    public User getOnlineUser(String argument) {
        return (User) getArgument(argument);
    }
    
    public IPermissionGroup getGroup(String argument) {
        return (IPermissionGroup) getArgument(argument);
    }
    
    public World getWorld(String argument) {
        return (World) getArgument(argument);
    }
    
    @SuppressWarnings("unchecked")
    public Map.Entry<String, Object> getKeyValuePair(String argument) {
        return (Map.Entry<String, Object>) getArgument(argument);
    }
    
    public List<Object> getArgumentList(String argument) {
        if (!values.containsKey(argument)) {
            throw new IllegalArgumentException("Argument with name " + argument + " is undefined!");
        }
        
        return values.get(argument);
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String argument) {
        return (List<String>) (List<?>) getArgumentList(argument);
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> getIntList(String argument) {
        return (List<Integer>) (List<?>) getArgumentList(argument);
    }
    
    @SuppressWarnings("unchecked")
    public List<Long> getLongList(String argument) {
        return (List<Long>) (List<?>) getArgumentList(argument);
    }
    
    @SuppressWarnings("unchecked")
    public List<IPermissionUser> getUserList(String argument) {
        return (List<IPermissionUser>) (List<?>) getArgumentList(argument);
    }
    
    @SuppressWarnings("unchecked")
    public List<User> getOnlineUserList(String argument) {
        return (List<User>) (List<?>) getArgumentList(argument);
    }
    
    @SuppressWarnings("unchecked")
    public List<IPermissionGroup> getGroupList(String argument) {
        return (List<IPermissionGroup>) (List<?>) getArgumentList(argument);
    }
    
    @SuppressWarnings("unchecked")
    public List<World> getWorldList(String argument) {
        return (List<World>) (List<?>) getArgumentList(argument);
    }
    
    @SuppressWarnings("unchecked")
    public List<Map.Entry<String, Object>> getKeyValuePairList(String argument) {
        return (List<Map.Entry<String, Object>>) (List<?>) getArgumentList(argument);
    }
    
    public static class ArgumentInfo {
        public String argumentName;
        public String shortSwitch;
        public String longSwitch;
        
        public ArgumentType type;
        public boolean isList;
        public boolean errorIfNotFound;
        public boolean mustBeOnline;
        public ArgumentInfo valueArgument;
        
        public ArgumentInfo(String argumentName, String shortSwitch, String longSwitch, ArgumentType type, boolean isList, boolean errorIfNotFound, ArgumentInfo valueArgument) {
            this.argumentName = argumentName;
            this.shortSwitch = shortSwitch;
            this.longSwitch = longSwitch;
            this.type = type;
            this.isList = isList;
            this.errorIfNotFound = errorIfNotFound;
            this.valueArgument = valueArgument;
        }
        
        public static ArgumentInfo newSwitch(String name, String shortSwitch, String longSwitch) {
            return new ArgumentInfo(name, shortSwitch, longSwitch, ArgumentType.SWITCH, false, false, null);
        }
        
        public static ArgumentInfo newString(String name, String shortSwitch, String longSwitch, boolean allowSpaces) {
            return new ArgumentInfo(name, shortSwitch, longSwitch, (allowSpaces) ? ArgumentType.STRING_WITH_SPACES : ArgumentType.STRING, false, false, null);
        }
        
        public static ArgumentInfo newInt(String name, String shortSwitch, String longSwitch) {
            return new ArgumentInfo(name, shortSwitch, longSwitch, ArgumentType.INT, false, false, null);
        }
        
        public static ArgumentInfo newLong(String name, String shortSwitch, String longSwitch) {
            return new ArgumentInfo(name, shortSwitch, longSwitch, ArgumentType.LONG, false, false, null);
        }
        
        public static ArgumentInfo newUser(String name, String shortSwitch, String longSwitch, boolean errorIfNotFound, boolean mustBeOnline) {
            return new ArgumentInfo(name, shortSwitch, longSwitch, (mustBeOnline) ? ArgumentType.USER_ONLINE : ArgumentType.USER, false, errorIfNotFound, null);
        }
        
        public static ArgumentInfo newGroup(String name, String shortSwitch, String longSwitch, boolean errorIfNotFound) {
            return new ArgumentInfo(name, shortSwitch, longSwitch, ArgumentType.GROUP, false, errorIfNotFound, null);
        }
        
        public static ArgumentInfo newWorld(String name, String shortSwitch, String longSwitch, boolean errorIfNotFound) {
            return new ArgumentInfo(name, shortSwitch, longSwitch, ArgumentType.WORLD, false, errorIfNotFound, null);
        }
        
        public static ArgumentInfo newStringList(String name, String shortSwitch, String longSwitch, boolean allowSpaces) {
            return new ArgumentInfo(name, shortSwitch, longSwitch, (allowSpaces) ? ArgumentType.STRING_WITH_SPACES : ArgumentType.STRING, true, false, null);
        }
        
        public static ArgumentInfo newIntList(String name, String shortSwitch, String longSwitch) {
            return new ArgumentInfo(name, shortSwitch, longSwitch, ArgumentType.INT, true, false, null);
        }
        
        public static ArgumentInfo newLongList(String name, String shortSwitch, String longSwitch) {
            return new ArgumentInfo(name, shortSwitch, longSwitch, ArgumentType.LONG, true, false, null);
        }
        
        public static ArgumentInfo newUserList(String name, String shortSwitch, String longSwitch, boolean errorIfNotFound, boolean mustBeOnline) {
            return new ArgumentInfo(name, shortSwitch, longSwitch, (mustBeOnline) ? ArgumentType.USER_ONLINE : ArgumentType.USER, true, errorIfNotFound, null);
        }
        
        public static ArgumentInfo newGroupList(String name, String shortSwitch, String longSwitch, boolean errorIfNotFound) {
            return new ArgumentInfo(name, shortSwitch, longSwitch, ArgumentType.GROUP, true, errorIfNotFound, null);
        }
        
        public static ArgumentInfo newWorldList(String name, String shortSwitch, String longSwitch, boolean errorIfNotFound) {
            return new ArgumentInfo(name, shortSwitch, longSwitch, ArgumentType.WORLD, true, errorIfNotFound, null);
        }
        
        public static ArgumentInfo newKeyValuePair(ArgumentInfo valueArgument) {
            return new ArgumentInfo(valueArgument.argumentName, valueArgument.shortSwitch, valueArgument.longSwitch, ArgumentType.KEY_VALUE_PAIR, valueArgument.isList, false, valueArgument);
        }
        
        public enum ArgumentType {
            SWITCH, STRING, STRING_WITH_SPACES, INT, LONG, USER, USER_ONLINE, GROUP, WORLD, KEY_VALUE_PAIR
        }
    }
}
