package com.bendude56.goldenapple.area.command;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.area.Area;
import com.bendude56.goldenapple.area.AreaAccessLevel;
import com.bendude56.goldenapple.area.AreaFlag;
import com.bendude56.goldenapple.area.AreaManager;
import com.bendude56.goldenapple.area.RegionShape;
import com.bendude56.goldenapple.command.DualSyntaxCommand;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.select.SelectManager;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;

public class AreaCommand extends DualSyntaxCommand {
    
    @Override
    public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(user, commandLabel, true);
            return;
        }
        
        ComplexArgumentParser arg = new ComplexArgumentParser(getArguments());
        if (!arg.parse(user, args)) {
            return;
        }
        user.sendLocalizedMessage("module.area.header");
        
        if (arg.isDefined("help")) {
            sendHelp(user, commandLabel, true);
            return;
        }
        
        if (arg.isDefined("override")) {
            String overrideMode = arg.getKeyValuePair("override").getKey();
            
            if (overrideMode.equalsIgnoreCase("alwayson")) {
                if (!AreaManager.canOverride(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else {
                    AreaManager.getInstance().setOverrideOn(user, true);
                    user.setVariable("goldenapple.area.alwaysOverride", true);
                    user.sendLocalizedMessage("module.area.override.on");
                }
            } else if (overrideMode.equalsIgnoreCase("on")) {
                if (!AreaManager.canOverride(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else {
                    AreaManager.getInstance().setOverrideOn(user, true);
                    user.deleteVariable("goldenapple.area.alwaysOverride");
                    user.sendLocalizedMessage("module.area.override.on");
                }
            } else if (overrideMode.equalsIgnoreCase("off")) {
                if (!AreaManager.canOverride(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else {
                    AreaManager.getInstance().setOverrideOn(user, false);
                    user.deleteVariable("goldenapple.area.alwaysOverride");
                    user.sendLocalizedMessage("module.area.override.off");
                }
            }
            
            return;
        } else if (arg.isDefined("list")) {
            onExecuteComplexList(instance, user, commandLabel, arg, args);
        }
        
        // Check for area creation/deletion. If both are defined, only execute
        // creation and ignore deletion.
        if (arg.isDefined("create")) {
            if (!onExecuteComplexCreate(instance, user, commandLabel, arg, args)) {
                return;
            }
        } else if (arg.isDefined("delete")) {
            if (!onExecuteComplexDelete(instance, user, commandLabel, arg, args)) {
                return;
            }
        } else {
            
            // Check for area modification. If multiple are defined, execute
            // them in order defined below.
            if (arg.isDefined("owner")) {
                onExecuteComplexOwner(instance, user, commandLabel, arg, args);
            }
            if (arg.isDefined("group-owner")) {
                onExecuteComplexGroupOwner(instance, user, commandLabel, arg, args);
            }
            if (arg.isDefined("invite")) {
                onExecuteComplexGuest(instance, user, commandLabel, arg, args);
            }
            if (arg.isDefined("group-invite")) {
                onExecuteComplexGroupGuest(instance, user, commandLabel, arg, args);
            }
            if (arg.isDefined("priority")) {
                onExecuteComplexSetPriority(instance, user, commandLabel, arg, args);
            }
            if (arg.isDefined("label")) {
                onExecuteComplexSetLabel(instance, user, commandLabel, arg, args);
            }
            if (arg.isDefined("info")) {
                onExecuteComplexInfo(instance, user, commandLabel, arg, args);
            }
            
            // TODO Add commands for adding/removing flags/regions
        }
    }
    
    @Override
    public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
        
        // Check if user is requesting help
        if (args.length == 0 || args[0].equals("-?") || args[0].equalsIgnoreCase("help")) {
            sendHelp(user, commandLabel, false);
            return;
        }
        
        user.sendLocalizedMessage("module.area.header");
        
        switch (args[0].toLowerCase()) {
            case "create":
                onExecuteSimpleCreate(instance, user, commandLabel, args);
                break;
            case "delete":
                onExecuteSimpleDelete(instance, user, commandLabel, args);
                break;
            case "list":
            case "ls":
                onExecuteSimpleList(instance, user, commandLabel, args);
                break;
            case "info":
            case "i":
                onExecuteSimpleInfo(instance, user, commandLabel, args);
                break;
            case "override":
                onExecuteSimpleOverride(instance, user, commandLabel, args);
                break;
            case "addowner":
                onExecuteSimpleAddOwner(instance, user, commandLabel, args);
                break;
            case "removeowner":
                onExecuteSimpleRemoveOwner(instance, user, commandLabel, args);
                break;
            case "addgroupowner":
                onExecuteSimpleAddGroupOwner(instance, user, commandLabel, args);
                break;
            case "removegroupowner":
                onExecuteSimpleRemoveGroupOwner(instance, user, commandLabel, args);
                break;
            case "invite":
                onExecuteSimpleAddGuest(instance, user, commandLabel, args);
                break;
            case "uninvite":
                onExecuteSimpleRemoveGuest(instance, user, commandLabel, args);
                break;
            case "invitegroup":
                onExecuteSimpleAddGroupGuest(instance, user, commandLabel, args);
                break;
            case "uninvitegroup":
                onExecuteSimpleRemoveGroupGuest(instance, user, commandLabel, args);
                break;
            
            // TODO case "setpriority":
            // TODO case "setlabel":
            // TODO Create more cases for each action
            
            default:
                user.sendLocalizedMessage("shared.parser.unknownOption", args[0]);
                
        }
    }
    
    private boolean onExecuteSimpleCreate(GoldenApple instance, User user, String commandLabel, String[] args) {
        
        // Make sure the user is a player. Required for technical reasons. (i.e.
        // console can't select a region in the world.)
        if (!(user.getHandle() instanceof Player)) {
            user.sendLocalizedMessage("shared.consoleNotAllowed");
            return false;
        }
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.addPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Handle optional arguments
        // Extract owner if argument is included
        List<IPermissionUser> owner = new ArrayList<IPermissionUser>();
        if (args.length > 1) {
            owner.add(PermissionManager.getInstance().findUser(args[1], true));
            if (owner.get(0) == null) {
                user.sendLocalizedMessage("shared.parser.userNotFound.error", args[1]);
                return false;
            }
        } else {
            owner.add(user);
        }
        
        // Get selected area
        Location c1, c2;
        if (SelectManager.getInstance().isSelectionMade(user) && SelectManager.getInstance().getSelectionWorld(user) == user.getPlayerHandle().getWorld()) {
            c1 = SelectManager.getInstance().getSelectionMinimum(user);
            c2 = SelectManager.getInstance().getSelectionMaximum(user);
        } else {
            user.sendLocalizedMessage("module.area.error.noSelection");
            return false;
        }
        
        return createArea(user, null, 0, owner, RegionShape.CUBOID, c1, c2, false) != null;
    }
    
    private boolean onExecuteSimpleDelete(GoldenApple instance, User user, String commandLabel, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.removePermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Get the selected area
        Area area = findArea(user, args[1]);
        if (area == null) {
            return false;
        }
        
        return deleteArea(user, area.getAreaId());
    }
    
    private boolean onExecuteSimpleList(GoldenApple instance, User user, String commandLabel, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.listLocationPermission) && !user.hasPermission(AreaManager.listAllPermission) && !user.hasPermission(AreaManager.listOwnPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        boolean all = false;
        boolean location = false;
        boolean mine = false;
        boolean owner = false;
        IPermissionUser o = null;
        int page = 1;
        
        // Determine mode
        if (args.length > 1) {
            switch (args[1].toLowerCase()) {
                case "all":
                case "a":
                    
                    // List-all-areas-on-server mode
                    if (!user.hasPermission(AreaManager.listAllPermission)) {
                        GoldenApple.logPermissionFail(user, commandLabel, args, true);
                        return false;
                    }
                    all = true;
                    break;
                
                case "location":
                case "here":
                case "l":
                    
                    // List-all-areas-at-current-location mode
                    if (!(user.getHandle() instanceof Player)) {
                        user.sendLocalizedMessage("shared.consoleNotAllowed");
                        return false;
                    }
                    if (!user.hasPermission(AreaManager.listLocationPermission)) {
                        GoldenApple.logPermissionFail(user, commandLabel, args, true);
                        return false;
                    }
                    location = true;
                    break;
                
                case "mine":
                case "me":
                    
                    // List-areas-owned-by-user mode
                    if (!user.hasPermission(AreaManager.listOwnPermission)) {
                        GoldenApple.logPermissionFail(user, commandLabel, args, true);
                        return false;
                    }
                    mine = true;
                    o = user;
                    break;
                
                case "owner":
                case "user":
                case "u":
                    
                    // List-all-areas-owned-by-certain-user mode
                    if (!user.hasPermission(AreaManager.listAllPermission)) {
                        GoldenApple.logPermissionFail(user, commandLabel, args, true);
                        return false;
                    }
                    owner = true;
                    if (args.length > 2) {
                        
                        // Get the owner
                        o = PermissionManager.getInstance().findUser(args[2], true);
                        if (o == null) {
                            user.sendLocalizedMessage("shared.parser.userNotFound.error", args[2]);
                            return false;
                        }
                    }
                    break;
                
                default:
                    
                    // No special mode
                    try {
                        page = Integer.parseInt(args[1]);
                        if (page < 1) {
                            page = 1;
                        }
                    } catch (NumberFormatException e) {
                        user.sendLocalizedMessage("shared.parser.unknownOption", args[1]);
                        return false;
                    }
                    break;
            }
        } else {
            page = 1;
        }
        
        // Figure out the page, or he mode if no mode is given
        if (all || location || mine || owner) {
            if (args.length > (owner ? 3 : 2)) {
                try {
                    page = Integer.parseInt(args[owner ? 3 : 2]);
                    if (page < 1) {
                        page = 1;
                    }
                } catch (NumberFormatException e) {
                    user.sendLocalizedMessage("shared.convertError.number", args[owner ? 3 : 2]);
                    return false;
                }
            } else {
                page = 1;
            }
        } else {
            
            // No mode selected, default to the one the user has permission for
            if (user.hasPermission(AreaManager.listLocationPermission) && !user.isServer()) {
                location = true;
            } else if (user.hasPermission(AreaManager.listAllPermission)) {
                all = true;
            } else if (user.hasPermission(AreaManager.listOwnPermission)) {
                mine = true;
                o = user;
            } else {
                user.sendLocalizedMessage("shared.consoleNotAllowed");
                return false;
            }
        }
        
        // Well, that sucked. Now to actually send the list.
        if (all) {
            sendAreaListAll(user, page);
        } else if (location) {
            sendAreaListLocation(user, page, ((Player) user.getHandle()).getLocation());
        } else if (mine || owner) {
            sendAreaListOwner(user, page, o);
        }
        return true;
    }
    
    private boolean onExecuteSimpleOverride(GoldenApple instance, User user, String commandLabel, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.overridePermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Verify argument length
        if (args.length < 2) {
            user.sendLocalizedMessage("shared.parser.parameterMissing", args[0]);
            return false;
        } else if (args.length > 2) {
            user.sendLocalizedMessage("shared.parser.unknownOption", args[2]);
            return false;
        }
        
        // Determine how they want it and make the switch
        switch (args[1].toLowerCase()) {
            case "on": // Turn overide on
                AreaManager.getInstance().setOverrideOn(user, true);
                user.sendLocalizedMessage("module.area.override.on");
                break;
            
            case "off": // Turn override off
                AreaManager.getInstance().setOverrideOn(user, false);
                user.sendLocalizedMessage("module.area.override.off");
                break;
            
            default: // Unknown option
                user.sendLocalizedMessage("shared.parser.unknownOption", args[1]);
                return false;
        }
        return true;
    }
    
    private boolean onExecuteSimpleAddOwner(GoldenApple instance, User user, String commandLabel, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.editAllOwnersPermission) && !user.hasPermission(AreaManager.editOwnOwnersPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Verify number of arguments
        if (args.length < 2) {
            user.sendLocalizedMessage("shared.parser.parameterMissing", args[0]);
            return false;
        }
        
        // Generate Query
        String query = null;
        if (args.length > 2) {
            query = "";
            for (int i = 2; i < args.length; i++) {
                query += " " + args[i];
            }
        }
        
        // Get the selected area
        Area area = findAreaWithPermissionSimple(user, commandLabel, args, query, AreaManager.editAllOwnersPermission, AreaManager.editOwnOwnersPermission);
        if (area == null) {
            return false;
        }
        
        // Get the user
        IPermissionUser u = PermissionManager.getInstance().findUser(args[1], true);
        if (u == null) {
            user.sendLocalizedMessage("shared.parser.userNotFound.error", args[1]);
            return false;
        }
        
        addAreaOwner(user, u, area);
        return true;
    }
    
    private boolean onExecuteSimpleRemoveOwner(GoldenApple instance, User user, String commandLabel, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.editAllOwnersPermission) && !user.hasPermission(AreaManager.editOwnOwnersPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Verify number of arguments
        if (args.length < 2) {
            user.sendLocalizedMessage("shared.parser.parameterMissing", args[0]);
            return false;
        }
        
        // Generate Query
        String query = null;
        if (args.length > 2) {
            query = "";
            for (int i = 2; i < args.length; i++) {
                query += " " + args[i];
            }
        }
        
        // Get the selected area
        Area area = findAreaWithPermissionSimple(user, commandLabel, args, query, AreaManager.editAllOwnersPermission, AreaManager.editOwnOwnersPermission);
        if (area == null) {
            return false;
        }
        
        // Get the user
        IPermissionUser u = PermissionManager.getInstance().findUser(args[1], true);
        if (u == null) {
            user.sendLocalizedMessage("shared.parser.userNotFound.error", args[1]);
            return false;
        }
        
        // Make sure user doesn't remove themselves from the area without any
        // way of adding themselves back in
        if (u.getId() == user.getId() && area.getUserAccessLevel(u.getId()) == AreaAccessLevel.OWNER && !user.hasPermission(AreaManager.editAllOwnersPermission)) {
            
            // See how many groups own this area that the user belongs to
            int count = 0;
            for (IPermissionGroup group : area.getGroups(AreaAccessLevel.OWNER)) {
                if (group.isMember(user, true)) {
                    count++;
                }
            }
            
            // If there are no groups, stop the user
            if (count == 0) {
                user.sendLocalizedMessage("module.area.owner.removeSelfFail");
                return false;
            }
        }
        
        removeAreaOwner(user, u, area);
        return true;
    }
    
    private boolean onExecuteSimpleAddGroupOwner(GoldenApple instance, User user, String commandLabel, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.editAllOwnersPermission) && !user.hasPermission(AreaManager.editOwnOwnersPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Verify number of arguments
        if (args.length < 2) {
            user.sendLocalizedMessage("shared.parser.parameterMissing", args[0]);
            return false;
        }
        
        // Generate Query
        String query = null;
        if (args.length > 2) {
            query = "";
            for (int i = 2; i < args.length; i++) {
                query += " " + args[i];
            }
        }
        
        // Get the selected area
        Area area = findAreaWithPermissionSimple(user, commandLabel, args, query, AreaManager.editAllOwnersPermission, AreaManager.editOwnOwnersPermission);
        if (area == null) {
            return false;
        }
        
        // Get the user
        IPermissionGroup g = PermissionManager.getInstance().getGroup(args[1]);
        if (g == null) {
            user.sendLocalizedMessage("shared.parser.groupNotFound.error", args[1]);
            return false;
        }
        
        addAreaGroupOwner(user, g, area);
        return true;
    }
    
    private boolean onExecuteSimpleRemoveGroupOwner(GoldenApple instance, User user, String commandLabel, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.editAllOwnersPermission) && !user.hasPermission(AreaManager.editOwnOwnersPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Verify number of arguments
        if (args.length < 2) {
            user.sendLocalizedMessage("shared.parser.parameterMissing", args[0]);
            return false;
        }
        
        // Generate Query
        String query = null;
        if (args.length > 2) {
            query = "";
            for (int i = 2; i < args.length; i++) {
                query += " " + args[i];
            }
        }
        
        // Get the selected area
        Area area = findAreaWithPermissionSimple(user, commandLabel, args, query, AreaManager.editAllOwnersPermission, AreaManager.editOwnOwnersPermission);
        if (area == null) {
            return false;
        }
        
        // Get the user
        IPermissionGroup g = PermissionManager.getInstance().getGroup(args[1]);
        if (g == null) {
            user.sendLocalizedMessage("shared.parser.groupNotFound.error", args[1]);
            return false;
        }
        
        // Make sure user doesn't remove themselves from the area without any
        // way of adding themselves back in
        if (g.isMember(user, false) && area.getGroupAccessLevel(g.getId()) == AreaAccessLevel.OWNER && !user.hasPermission(AreaManager.editAllOwnersPermission) && area.getUserAccessLevel(user.getId()) != AreaAccessLevel.OWNER) {
            
            // See how many groups own this area that the user belongs to
            int count = 0;
            for (IPermissionGroup group : area.getGroups(AreaAccessLevel.OWNER)) {
                if (group.isMember(user, true)) {
                    count++;
                }
            }
            
            // If this is the only group, stop the user
            if (count == 1) {
                user.sendLocalizedMessage("module.area.owner.removeSelfFail");
                return false;
            }
        }
        
        removeAreaGroupOwner(user, g, area);
        return true;
    }
    
    private boolean onExecuteSimpleAddGuest(GoldenApple instance, User user, String commandLabel, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.editAllGuestsPermission) && !user.hasPermission(AreaManager.editOwnGuestsPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Verify number of arguments
        if (args.length < 2) {
            user.sendLocalizedMessage("shared.parser.parameterMissing", args[0]);
            return false;
        }
        
        // Generate Query
        String query = null;
        if (args.length > 2) {
            query = "";
            for (int i = 2; i < args.length; i++) {
                query += " " + args[i];
            }
        }
        
        // Get the selected area
        Area area = findAreaWithPermissionSimple(user, commandLabel, args, query, AreaManager.editAllGuestsPermission, AreaManager.editOwnGuestsPermission);
        if (area == null) {
            return false;
        }
        
        // Get the user
        IPermissionUser u = PermissionManager.getInstance().findUser(args[1], true);
        if (u == null) {
            user.sendLocalizedMessage("shared.parser.userNotFound.error", args[1]);
            return false;
        }
        
        addAreaGuest(user, u, area);
        return true;
    }
    
    private boolean onExecuteSimpleRemoveGuest(GoldenApple instance, User user, String commandLabel, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.editAllGuestsPermission) && !user.hasPermission(AreaManager.editOwnGuestsPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Verify number of arguments
        if (args.length < 2) {
            user.sendLocalizedMessage("shared.parser.parameterMissing", args[0]);
            return false;
        }
        
        // Generate Query
        String query = null;
        if (args.length > 2) {
            query = "";
            for (int i = 2; i < args.length; i++) {
                query += " " + args[i];
            }
        }
        
        // Get the selected area
        Area area = findAreaWithPermissionSimple(user, commandLabel, args, query, AreaManager.editAllGuestsPermission, AreaManager.editOwnGuestsPermission);
        if (area == null) {
            return false;
        }
        
        // Get the user
        IPermissionUser u = PermissionManager.getInstance().findUser(args[1], true);
        if (u == null) {
            user.sendLocalizedMessage("shared.parser.userNotFound.error", args[1]);
            return false;
        }
        
        removeAreaGuest(user, u, area);
        return true;
    }
    
    private boolean onExecuteSimpleAddGroupGuest(GoldenApple instance, User user, String commandLabel, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.editAllGuestsPermission) && !user.hasPermission(AreaManager.editOwnGuestsPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Verify number of arguments
        if (args.length < 2) {
            user.sendLocalizedMessage("shared.parser.parameterMissing", args[0]);
            return false;
        }
        
        // Generate Query
        String query = null;
        if (args.length > 2) {
            query = "";
            for (int i = 2; i < args.length; i++) {
                query += " " + args[i];
            }
        }
        
        // Get the selected area
        Area area = findAreaWithPermissionSimple(user, commandLabel, args, query, AreaManager.editAllGuestsPermission, AreaManager.editOwnGuestsPermission);
        if (area == null) {
            return false;
        }
        
        // Get the user
        IPermissionGroup g = PermissionManager.getInstance().getGroup(args[1]);
        if (g == null) {
            user.sendLocalizedMessage("shared.parser.groupNotFound.error", args[1]);
            return false;
        }
        
        addAreaGroupGuest(user, g, area);
        return true;
    }
    
    private boolean onExecuteSimpleRemoveGroupGuest(GoldenApple instance, User user, String commandLabel, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.editAllGuestsPermission) && !user.hasPermission(AreaManager.editOwnGuestsPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Verify number of arguments
        if (args.length < 2) {
            user.sendLocalizedMessage("shared.parser.parameterMissing", args[0]);
            return false;
        }
        
        // Generate Query
        String query = null;
        if (args.length > 2) {
            query = "";
            for (int i = 2; i < args.length; i++) {
                query += " " + args[i];
            }
        }
        
        // Get the selected area
        Area area = findAreaWithPermissionSimple(user, commandLabel, args, query, AreaManager.editAllGuestsPermission, AreaManager.editOwnGuestsPermission);
        if (area == null) {
            return false;
        }
        
        // Get the user
        IPermissionGroup g = PermissionManager.getInstance().getGroup(args[1]);
        if (g == null) {
            user.sendLocalizedMessage("shared.parser.groupNotFound.error", args[1]);
            return false;
        }
        
        removeAreaGroupGuest(user, g, area);
        return true;
    }
    
    private boolean onExecuteSimpleInfo(GoldenApple instnace, User user, String commandLabel, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.infoAllPermission) && !user.hasPermission(AreaManager.infoOwnPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        String query;
        Area area;
        
        // Check if they're selecting a specific area
        if (args.length > 1) {
            query = "";
            for (int i = 1; i < args.length; i++) {
                query += " " + args[i];
            }
            query.trim();
        } else {
            query = null;
        }
        
        // Get the selected area
        if (query == null) {
            
            // Make sure user isn't a console
            if (!(user.getHandle() instanceof Player)) {
                user.sendLocalizedMessage("shared.consoleNotAllowed");
                return false;
            }
            
            // Get area at user's current location
            area = this.findArea(user, !user.hasPermission(AreaManager.infoAllPermission));
        } else {
            
            // Search for area based on query
            area = this.findArea(user, query);
        }
        
        // Make sure area was actually found
        if (area == null) {
            return false;
        }
        
        sendAreaInfo(user, area);
        return true;
    }
    
    private boolean onExecuteComplexCreate(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg, String[] args) {
        
        // Make sure the user is a player. Required for technical reasons. (i.e.
        // console can't select a region in the world.)
        if (!(user.getHandle() instanceof Player)) {
            user.sendLocalizedMessage("shared.consoleNotAllowed");
            return false;
        }
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.addPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Extract all needed properties
        String label = extractLabel(arg);
        RegionShape shape = extractShape(arg);
        Integer priority = extractPriority(arg);
        Boolean ignoreY = extractIgnoreY(arg);
        List<IPermissionUser> owners = extractOwners(arg, user);
        Location c1, c2;
        Area area;
        
        // Get selected area
        if (SelectManager.getInstance().isSelectionMade(user) && SelectManager.getInstance().getSelectionWorld(user) == user.getPlayerHandle().getWorld()) {
            c1 = SelectManager.getInstance().getSelectionMinimum(user);
            c2 = SelectManager.getInstance().getSelectionMaximum(user);
        } else {
            user.sendLocalizedMessage("module.area.error.noSelection");
            return false;
        }
        
        // Validate shape type or set to default value
        if (shape == null) {
            if (arg.isDefined("shape")) {
                user.sendLocalizedMessage("module.area.error.invalidShape", arg.getString("shape"));
                return false;
            } else {
                shape = RegionShape.CUBOID; // Default value
            }
        }
        
        // Validate priority or set to default value
        if (priority == null) {
            if (arg.isDefined("priority")) {
                return false;
            } else {
                priority = 0; // Default value
            }
        }
        
        // Validate owner or set to default value
        if (owners == null || owners.isEmpty()) {
            if (arg.isDefined("owner")) {
                return false;
            } else {
                owners = new ArrayList<IPermissionUser>(); // Default value
                owners.add(user);
            }
        }
        
        area = createArea(user, label, priority, owners, shape, c1, c2, ignoreY);
        return (area != null && (!arg.isDefined("flags") || onExecuteComplexFlags(instance, user, commandLabel, arg, args)));
    }
    
    private boolean onExecuteComplexDelete(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.removePermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Get the selected area
        Area area = extractSelectedArea(arg, user, false, false);
        if (area == null) {
            return false;
        }
        
        return deleteArea(user, area.getAreaId());
    }
    
    private boolean onExecuteComplexList(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.listLocationPermission) && !user.hasPermission(AreaManager.listAllPermission) && !user.hasPermission(AreaManager.listOwnPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        boolean all = false;
        boolean location = false;
        boolean mine = false;
        boolean owner = false;
        int page;
        IPermissionUser o = null;
        
        // Determine which areas to list
        if (arg.isDefined("all")) {
            if (!user.hasPermission(AreaManager.listAllPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return false;
            }
            all = true;
        } else if (arg.isDefined("location")) {
            if (!(user.getHandle() instanceof Player)) {
                user.sendLocalizedMessage("shared.consoleNotAllowed");
                return false;
            }
            if (!user.hasPermission(AreaManager.listLocationPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return false;
            }
            location = true;
        } else if (arg.isDefined("mine")) {
            if (!user.hasPermission(AreaManager.listOwnPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return false;
            }
            mine = true;
            o = user;
        } else if (arg.isDefined("owner")) {
            if (!user.hasPermission(AreaManager.listAllPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return false;
            }
            owner = true;
            o = arg.getUser("owner");
        } else {
            
            // No mode was explicitly specified, so automatically decide based
            // on the user's permissions.
            if (user.hasPermission(AreaManager.listLocationPermission) && user.getHandle() instanceof Player) {
                location = true;
            } else if (user.hasPermission(AreaManager.listAllPermission)) {
                all = true;
            } else if (user.hasPermission(AreaManager.listOwnPermission) && user.getHandle() instanceof Player) {
                mine = true;
                o = user;
            }
        }
        
        // Get the page
        page = (arg.isDefined("page") ? arg.getInt("page") : 1);
        if (page < 1) {
            page = 1;
        }
        
        if (all) {
            sendAreaListAll(user, page);
        } else if (location) {
            sendAreaListLocation(user, page, ((Player) user.getHandle()).getLocation());
        } else if (mine || owner) {
            sendAreaListOwner(user, page, o);
        }
        return true;
    }
    
    private boolean onExecuteComplexOwner(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.editAllOwnersPermission) && !user.hasPermission(AreaManager.editOwnOwnersPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        int count = 0;
        
        // Find selected area while taking given permissions into account
        Area area = findAreaWithPermissionComplex(user, commandLabel, args, arg, AreaManager.editAllOwnersPermission, AreaManager.editOwnOwnersPermission);
        if (area == null) {
            return false;
        }
        
        // Add/remove all of the owners specified
        for (Entry<String, Object> entry : arg.getKeyValuePairList("owner")) {
            switch (entry.getKey().toLowerCase()) {
                case "add":
                case "a":
                    if (addAreaOwner(user, (IPermissionUser) entry.getValue(), area)) {
                        count++;
                    }
                    break;
                
                case "remove":
                case "r":
                    if (removeAreaOwner(user, (IPermissionUser) entry.getValue(), area)) {
                        count++;
                    }
                    break;
                
                default:
                    user.sendLocalizedMessage("shared.parser.unknownOption", entry.getKey());
            }
        }
        
        // check if no changes were made
        if (count == 0) {
            user.sendLocalizedMessage("module.area.owner.unmodified", area.getAreaId());
        }
        
        return true;
    }
    
    private boolean onExecuteComplexGroupOwner(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.editAllOwnersPermission) && !user.hasPermission(AreaManager.editOwnOwnersPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        int count = 0;
        
        // Find selected area while taking given permissions into account
        Area area = findAreaWithPermissionComplex(user, commandLabel, args, arg, AreaManager.editAllOwnersPermission, AreaManager.editOwnOwnersPermission);
        if (area == null) {
            return false;
        }
        
        for (Entry<String, Object> entry : arg.getKeyValuePairList("group-owner")) {
            switch (entry.getKey()) {
                case "add":
                case "a":
                    if (addAreaGroupOwner(user, (IPermissionGroup) entry.getValue(), area)) {
                        count++;
                    }
                    break;
                
                case "remove":
                case "r":
                    if (removeAreaGroupOwner(user, (IPermissionGroup) entry.getValue(), area)) {
                        count++;
                    }
                    break;
                
                default:
                    user.sendLocalizedMessage("shared.parser.unknownOption", entry.getKey());
            }
        }
        
        // Check if no changes were made
        if (count == 0) {
            user.sendLocalizedMessage("module.area.groupOwner.unmodified", area.getAreaId());
        }
        
        return true;
    }
    
    private boolean onExecuteComplexGuest(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.editAllGuestsPermission) && !user.hasPermission(AreaManager.editOwnGuestsPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        int count = 0;
        
        // Find selected area while taking given permissions into account
        Area area = findAreaWithPermissionComplex(user, commandLabel, args, arg, AreaManager.editAllGuestsPermission, AreaManager.editOwnGuestsPermission);
        if (area == null) {
            return false;
        }
        
        for (Entry<String, Object> entry : arg.getKeyValuePairList("invite")) {
            switch (entry.getKey().toLowerCase()) {
                case "full":
                case "f":
                    if (addAreaGuest(user, (IPermissionUser) entry.getValue(), area)) {
                        count++;
                    }
                    break;
                
                case "none":
                case "n":
                    if (removeAreaGuest(user, (IPermissionUser) entry.getValue(), area)) {
                        count++;
                    }
                    break;
                
                default:
                    user.sendLocalizedMessage("shared.parser.unknownOption", entry.getKey());
            }
        }
        
        // Check if no changes were made
        if (count == 0) {
            user.sendLocalizedMessage("module.area.guest.unmodified", area.getAreaId());
        }
        
        return true;
    }
    
    private boolean onExecuteComplexGroupGuest(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.editAllGuestsPermission) && !user.hasPermission(AreaManager.editOwnGuestsPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        int count = 0;
        
        // Find selected area while taking given permissions into account
        Area area = findAreaWithPermissionComplex(user, commandLabel, args, arg, AreaManager.editAllGuestsPermission, AreaManager.editOwnGuestsPermission);
        if (area == null) {
            return false;
        }
        
        for (Entry<String, Object> entry : arg.getKeyValuePairList("group-invite")) {
            switch (entry.getKey().toLowerCase()) {
                case "full":
                case "f":
                    if (addAreaGroupGuest(user, (IPermissionGroup) entry.getValue(), area)) {
                        count++;
                    }
                    break;
                
                case "none":
                case "n":
                    if (removeAreaGroupGuest(user, (IPermissionGroup) entry.getValue(), area)) {
                        count++;
                    }
                    break;
                
                default:
                    user.sendLocalizedMessage("shared.parser.unknownOption", entry.getKey());
            }
        }
        
        // Check if no changes were made
        if (count == 0) {
            user.sendLocalizedMessage("module.area.groupGuest.unmodified", area.getAreaId());
        }
        
        return true;
    }
    
    private boolean onExecuteComplexFlags(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg, String[] args) {
        
        // Make sure use has adequate permissions
        if (!user.hasPermission(AreaManager.editAllFlagsPermission) && !user.hasPermission(AreaManager.editOwnFlagsPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        int count = 0;
        
        // Find selected area while taking given permissions into account
        Area area = findAreaWithPermissionComplex(user, commandLabel, args, arg, AreaManager.editAllFlagsPermission, AreaManager.editOwnFlagsPermission);
        if (area == null) {
            return false;
        }
        
        AreaFlag f;
        for (Entry<String, Object> entry : arg.getKeyValuePairList("flags")) {
            switch (entry.getKey().toLowerCase()) {
                case "add":
                case "a":
                case "set":
                case "s":
                    f = AreaFlag.fromString((String) entry.getValue());
                    if (f != null) {
                        addAreaFlag(user, area, f);
                        count++;
                    } else {
                        user.sendLocalizedMessage("module.area.flag.unknown", (String) entry.getValue());
                    }
                    break;
                
                case "remove":
                case "reset":
                case "r":
                    f = AreaFlag.fromString((String) entry.getValue());
                    if (f != null) {
                        removeAreaFlag(user, area, f);
                        count++;
                    } else {
                        user.sendLocalizedMessage("module.area.flag.unknown", (String) entry.getValue());
                    }
                    break;
                
                default:
                    user.sendLocalizedMessage("shared.parser.unknownOption", entry.getKey());
            }
        }
        
        if (count == 0) {
            user.sendLocalizedMessage("module.area.flag.unmodified", area.getAreaId() + "");
        }
        
        return true;
    }
    
    private boolean onExecuteComplexSetPriority(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg, String[] args) {
        Integer priority;
        
        // Find selected area while taking given permissions into account
        Area area = findAreaWithPermissionComplex(user, commandLabel, args, arg, AreaManager.editAllPriorityPermission, AreaManager.editOwnPriorityPermission);
        if (area == null) {
            return false;
        }
        
        // Extract priority from arguments
        priority = extractPriority(arg);
        if (priority == null) {
            return false;
        }
        
        return setAreaPriority(user, area, priority);
    }
    
    private boolean onExecuteComplexSetLabel(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg, String[] args) {
        String label;
        
        // Find selected area while taking given permissions into account
        Area area = findAreaWithPermissionComplex(user, commandLabel, args, arg, AreaManager.editAllLabelPermission, AreaManager.editOwnLabelPermission);
        if (area == null) {
            return false;
        }
        
        // Extract label from arguments
        label = extractLabel(arg);
        
        return setAreaLabel(user, area, label);
    }
    
    private boolean onExecuteComplexInfo(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg, String[] args) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(AreaManager.infoAllPermission) && !user.hasPermission(AreaManager.infoOwnPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        }
        
        // Get the selected area
        Area area = extractSelectedArea(arg, user, true, (user.hasPermission(AreaManager.infoOwnPermission) && !user.hasPermission(AreaManager.infoAllPermission)));
        if (area == null) {
            return false;
        }
        
        sendAreaInfo(user, area);
        return true;
        
    }
    
    private Area extractSelectedArea(ComplexArgumentParser arg, User user, boolean useLocation, boolean own) {
        if (arg.isDefined("select")) {
            return findArea(user, arg.getString("select"));
        } else if (useLocation && user != null) {
            return findArea(user, own);
        } else {
            if (user != null) {
                user.sendLocalizedMessage("module.area.error.selectMissing");
            }
            return null;
        }
    }
    
    private List<IPermissionUser> extractOwners(ComplexArgumentParser arg, User user) {
        List<IPermissionUser> list = new ArrayList<IPermissionUser>();
        if (arg.isDefined("owner")) {
            for (Entry<String, Object> entry : arg.getKeyValuePairList("owner")) {
                switch (entry.getKey().toLowerCase()) {
                    case "add":
                    case "set":
                    case "a":
                    case "s":
                    case "":
                        list.add((IPermissionUser) entry.getValue());
                        break;
                    
                    default:
                        user.sendLocalizedMessage("shared.parser.unknownOption", entry.getKey());
                }
            }
        }
        return list;
    }
    
    private String extractLabel(ComplexArgumentParser arg) {
        if (arg.isDefined("label")) {
            return arg.getString("label");
        } else {
            return null;
        }
    }
    
    private RegionShape extractShape(ComplexArgumentParser arg) {
        if (arg.isDefined("shape")) {
            switch (arg.getString("shape").toLowerCase()) {
                case "rectangle":
                case "square":
                case "cuboid":
                case "box":
                case "block":
                    return RegionShape.CUBOID;
                case "cylinder":
                case "pipe":
                case "pole":
                case "column":
                    return RegionShape.CYLINDER;
                case "sphere":
                case "ellipsoid":
                case "circle":
                case "ball":
                    return RegionShape.ELLIPSOID;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }
    
    private Integer extractPriority(ComplexArgumentParser arg) {
        if (arg.isDefined("priority")) {
            try {
                return arg.getInt("priority");
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
    }
    
    private Boolean extractIgnoreY(ComplexArgumentParser arg) {
        return (arg.isDefined("ignore-y"));
    }
    
    private Area findArea(User user, String query) {
        Area area;
        
        // User is explicitly selecting an area
        try {
            
            // If query is a number, search for by ID
            area = AreaManager.getInstance().getArea(Long.parseLong(query));
            if (area == null) {
                user.sendLocalizedMessage("module.area.error.selectId", query);
            }
        } catch (NumberFormatException e) {
            
            // If query is not a number, search by label
            area = AreaManager.getInstance().getArea(query);
            if (area == null) {
                user.sendLocalizedMessage("module.area.error.selectLabel", query);
            }
        }
        
        return area;
    }
    
    private Area findArea(User user, boolean own) {
        Location location;
        
        // Verify that command sender is a location-bearing object
        if (user.getHandle() instanceof Player) {
            location = ((Player) user.getHandle()).getLocation();
        } else if (user.getHandle() instanceof BlockCommandSender) {
            location = ((BlockCommandSender) user.getHandle()).getBlock().getLocation();
        } else if (user.getHandle() instanceof CommandMinecart) {
            location = ((CommandMinecart) user.getHandle()).getLocation();
        } else {
            user.sendLocalizedMessage("shared.consoleNotAllowed");
            return null;
        }
        
        // Return a list of areas at current location
        List<Area> areas = AreaManager.getInstance().getAreas(location);
        
        if (areas.isEmpty()) {
            user.sendLocalizedMessage("module.area.error.selectLocation");
            return null; // No areas at current location
        }
        if (own) {
            for (Area area : areas) {
                if (area.getUserAccessLevel(user.getId()) == AreaAccessLevel.OWNER) {
                    return area;
                }
            }
            user.sendLocalizedMessage("module.area.error.noOwn");
            return null;
        } else {
            return areas.get(0);
        }
    }
    
    private Area findAreaWithPermissionComplex(User user, String commandLabel, String[] args, ComplexArgumentParser arg, Permission global, Permission own) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(global) && !user.hasPermission(own)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return null;
        }
        
        // Get the selected area
        Area area = extractSelectedArea(arg, user, true, !user.hasPermission(global));
        if (area == null) {
            return null;
        }
        
        // Make sure user has adequate permissions again
        if (!user.hasPermission(global) && (!user.hasPermission(own) || area.getUserAccessLevel(user.getId()).getComparableValue() < AreaAccessLevel.OWNER.getComparableValue())) {
            user.sendLocalizedMessage("module.area.error.selectOwn");
            return null;
        }
        
        return area;
    }
    
    private Area findAreaWithPermissionSimple(User user, String commandLabel, String[] args, String query, Permission global, Permission own) {
        
        // Make sure user has adequate permissions
        if (!user.hasPermission(global) && !user.hasPermission(own)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return null;
        }
        
        // Get the selected area
        // Area area = extractSelectedArea(arg, user, true,
        // !user.hasPermission(global));
        Area area = query != null ? findArea(user, query) : findArea(user, !user.hasPermission(global));
        if (area == null) {
            return null;
        }
        
        // Make sure user has adequate permissions again
        if (!user.hasPermission(global) && (!user.hasPermission(own) || area.getUserAccessLevel(user.getId()).getComparableValue() < AreaAccessLevel.OWNER.getComparableValue())) {
            user.sendLocalizedMessage("module.area.error.selectOwn");
            return null;
        }
        
        return area;
    }
    
    private Area createArea(User user, String label, int priority, List<IPermissionUser> owners, RegionShape shape, Location c1, Location c2, boolean ignoreY) {
        Area area;
        String ownerList;
        
        // Generate list of owners for user feedback
        if (owners.size() == 0) {
            ownerList = user.getLocalizedMessage("shared.values.none");
        } else {
            ownerList = "";
            for (IPermissionUser o : owners) {
                if (!ownerList.isEmpty()) {
                    ownerList += ", ";
                }
                ownerList += o.getName();
            }
        }
        
        try {
            area = AreaManager.getInstance().createArea(owners, label, priority, shape, c1, c2, ignoreY);
            if (area == null) {
                user.sendLocalizedMessage("module.area.create.fail");
                return null;
            }
            user.sendLocalizedMessage("module.area.create.success", area.getAreaId(), (label == null ? user.getLocalizedMessage("module.area.label.none") : label), priority, ownerList);
            return area;
        } catch (Exception e) {
            
            // An error has occurred. Notify the user and log the error.
            user.sendLocalizedMessage("module.area.create.fail");
            GoldenApple.log(Level.SEVERE, "An error occured while attempting to create a new area.");
            GoldenApple.log(Level.SEVERE, "Please send the following information to the GoldenApple developers:");
            GoldenApple.log(Level.SEVERE, "User:" + user.getName() + " ID:" + user.getId() + ")");
            GoldenApple.log(Level.SEVERE, e);
            return null;
        }
    }
    
    private boolean deleteArea(User user, long areaId) {
        
        try {
            // Attempt to delete the area
            AreaManager.getInstance().deleteArea(areaId);
            user.sendLocalizedMessage("module.area.delete.success", areaId);
            return true;
        } catch (SQLException e) {
            
            // An error has occurred. Notify the user and log the error.
            user.sendLocalizedMessage("module.area.delete.fail");
            GoldenApple.log(Level.SEVERE, "An error occured while attempting to delete area " + areaId);
            GoldenApple.log(Level.SEVERE, e);
            return false;
        }
    }
    
    private boolean addAreaOwner(User user, IPermissionUser u, Area area) {
        if (area.getUserAccessLevel(u.getId()).getComparableValue() < AreaAccessLevel.OWNER.getComparableValue()) {
            area.setUserAccessLevel(u.getId(), AreaAccessLevel.OWNER);
            user.sendLocalizedMessage("module.area.owner.add", area.getAreaId(), u.getName());
            return true;
        } else {
            user.sendLocalizedMessage("module.area.owner.addFail", area.getAreaId(), u.getName(), area.getUserAccessLevel(u.getId()).toString());
            return false;
        }
    }
    
    private boolean removeAreaOwner(User user, IPermissionUser u, Area area) {
        if (area.getUserAccessLevel(u.getId()) == AreaAccessLevel.OWNER) {
            area.setUserAccessLevel(u.getId(), AreaAccessLevel.NONE);
            user.sendLocalizedMessage("module.area.owner.remove", area.getAreaId(), u.getName());
            return true;
        } else {
            user.sendLocalizedMessage("module.area.owner.removeFail", area.getAreaId(), u.getName());
            return false;
        }
    }
    
    private boolean addAreaGroupOwner(User user, IPermissionGroup g, Area area) {
        if (area.getGroupAccessLevel(g.getId()).getComparableValue() < AreaAccessLevel.OWNER.getComparableValue()) {
            area.setGroupAccessLevel(g.getId(), AreaAccessLevel.OWNER);
            user.sendLocalizedMessage("module.area.groupOwner.add", area.getAreaId(), g.getName());
            return true;
        } else {
            user.sendLocalizedMessage("module.area.groupOwner.addFail", area.getAreaId(), g.getName(), area.getGroupAccessLevel(g.getId()).toString());
            return false;
        }
    }
    
    private boolean removeAreaGroupOwner(User user, IPermissionGroup g, Area area) {
        if (area.getGroupAccessLevel(g.getId()) == AreaAccessLevel.OWNER) {
            area.setGroupAccessLevel(g.getId(), AreaAccessLevel.NONE);
            user.sendLocalizedMessage("module.area.groupOwner.remove", area.getAreaId(), g.getName());
            return true;
        } else {
            user.sendLocalizedMessage("module.area.groupOwner.removeFail", area.getAreaId(), g.getName());
            return false;
        }
    }
    
    private boolean addAreaGuest(User user, IPermissionUser u, Area area) {
        if (area.getUserAccessLevel(u.getId()).getComparableValue() < AreaAccessLevel.GUEST.getComparableValue()) {
            area.setUserAccessLevel(u.getId(), AreaAccessLevel.GUEST);
            user.sendLocalizedMessage("module.area.guest.add", area.getAreaId(), u.getName());
            return true;
        } else {
            user.sendLocalizedMessage("module.area.guest.addFail", area.getAreaId(), u.getName(), area.getUserAccessLevel(u.getId()).toString());
            return false;
        }
    }
    
    private boolean removeAreaGuest(User user, IPermissionUser u, Area area) {
        if (area.getUserAccessLevel(u.getId()) == AreaAccessLevel.GUEST) {
            area.setUserAccessLevel(u.getId(), AreaAccessLevel.NONE);
            user.sendLocalizedMessage("module.area.guest.remove", area.getAreaId(), u.getName());
            return true;
        } else {
            user.sendLocalizedMessage("module.area.guest.removeFail", area.getAreaId(), u.getName());
            return false;
        }
    }
    
    private boolean addAreaGroupGuest(User user, IPermissionGroup g, Area area) {
        if (area.getGroupAccessLevel(g.getId()).getComparableValue() < AreaAccessLevel.GUEST.getComparableValue()) {
            area.setGroupAccessLevel(g.getId(), AreaAccessLevel.GUEST);
            user.sendLocalizedMessage("module.area.groupGuest.add", area.getAreaId(), g.getName());
            return true;
        } else {
            user.sendLocalizedMessage("module.area.groupGuest.addFail", area.getAreaId(), g.getName(), area.getGroupAccessLevel(g.getId()).toString());
            return false;
        }
    }
    
    private boolean removeAreaGroupGuest(User user, IPermissionGroup g, Area area) {
        if (area.getGroupAccessLevel(g.getId()) == AreaAccessLevel.GUEST) {
            area.setGroupAccessLevel(g.getId(), AreaAccessLevel.NONE);
            user.sendLocalizedMessage("module.area.groupGuest.remove", area.getAreaId(), g.getName());
            return true;
        } else {
            user.sendLocalizedMessage("module.area.groupGuest.removeFail", area.getAreaId(), g.getName());
            return false;
        }
    }
    
    private boolean setAreaLabel(User user, Area area, String label) {
        area.setLabel(label);
        user.sendLocalizedMessage("module.area.label.change", area.getAreaId(), (label == null ? user.getLocalizedMessage("module.area.label.none") : label));
        return true;
    }
    
    private boolean setAreaPriority(User user, Area area, int priority) {
        area.setPriority(priority);
        user.sendLocalizedMessage("module.area.priority", area.getAreaId(), priority);
        return true;
    }
    
    private boolean addAreaFlag(User user, Area area, AreaFlag flag) {
        area.setFlag(flag, true);
        user.sendLocalizedMessage("module.area.flag.add", area.getAreaId(), flag.toString());
        
        return true;
    }
    
    private boolean removeAreaFlag(User user, Area area, AreaFlag flag) {
        area.setFlag(flag, false);
        user.sendLocalizedMessage("module.area.flag.remove", area.getAreaId(), flag.toString());
        
        return true;
    }
    
    private void sendAreaListAll(User user, int page) {
        int per = (user.getHandle() instanceof ConsoleCommandSender ? 10 : 6);
        int total;
        
        // Retrieve list of areas
        List<Area> areas = AreaManager.getInstance().getAreas(page, per);
        total = AreaManager.getInstance().getTotalAreas();
        
        // Adjust for pagination
        if (page > (total + per - 1) / per) {
            page = (total + per - 1) / per;
        }
        if (page < 1) {
            page = 1;
        }
        
        // Check if no areas are there
        if (areas.isEmpty()) {
            user.sendLocalizedMessage("module.area.list.none");
            return;
        }
        
        // Print listing header
        user.sendLocalizedMessage("module.area.list.header", page, (total + per - 1) / 6);
        
        sendAreaList(user, areas);
    }
    
    private void sendAreaListLocation(User user, int page, Location location) {
        
        int per = (user.getHandle() instanceof ConsoleCommandSender ? 10 : 6);
        int total;
        
        // Retrieve list of areas
        List<Area> areas = AreaManager.getInstance().getAreas(((Player) user.getHandle()).getLocation());
        total = areas.size();
        
        // Adjust for pagination
        if (page > (total + per - 1) / per) {
            page = (total + per - 1) / per;
        }
        if (page < 1) {
            page = 1;
        }
        areas = areas.subList((page - 1) * per, ((page * per) > total ? total : (page * per)));
        
        // Check if no areas are there
        if (areas.isEmpty()) {
            user.sendLocalizedMessage("module.area.list.noneLocation");
            return;
        }
        
        // Print listing header
        user.sendLocalizedMessage("module.area.list.headerLocation", page, (total + per - 1) / 6);
        
        sendAreaList(user, areas);
    }
    
    private void sendAreaListOwner(User user, int page, IPermissionUser owner) {
        
        int per = (user.getHandle() instanceof ConsoleCommandSender ? 10 : 6);
        int total;
        boolean same = user.getId() == owner.getId();
        
        // Retrieve list of areas
        List<Area> areas = AreaManager.getInstance().getAreasByOwner(owner.getId());
        total = areas.size();
        
        // Adjust for pagination
        if (page > (total + per - 1) / per) {
            page = (total + per - 1) / per;
        }
        if (page < 1) {
            page = 1;
        }
        areas = areas.subList((page - 1) * per, ((page * per) > total ? total : (page * per)));
        
        // Check if no areas are there
        if (areas.isEmpty()) {
            if (same) {
                user.sendLocalizedMessage("module.area.list.noneOwn");
            } else {
                user.sendLocalizedMessage("module.area.list.noneUser", owner.getName());
            }
            return;
        }
        
        // Print listing header
        if (same) {
            user.sendLocalizedMessage("module.area.list.headerOwn", page, (total + per - 1) / per);
        } else {
            user.sendLocalizedMessage("module.area.list.headerUser", page, (total + per - 1) / per, owner.getName());
        }
        
        sendAreaList(user, areas);
        
    }
    
    private void sendAreaList(User user, List<Area> areas) {
        for (Area area : areas) {
            user.sendLocalizedMessage("module.area.list.item", area.getAreaId(), (area.getLabel() == null || area.getLabel().isEmpty()) ? user.getLocalizedMessage("module.area.label.none") : area.getLabel(), area.getPriority());
        }
    }
    
    private void sendAreaInfo(User user, Area area) {
        
        // Strings used for compiling lists and info
        String flags;
        String owners;
        String guests;
        String gowners;
        String gguests;
        String world;
        
        // Generate flag list
        List<AreaFlag> flaglist = area.getFlags();
        if (flaglist.isEmpty()) {
            flags = user.getLocalizedMessage("shared.values.none");
        } else {
            flags = "";
            for (AreaFlag f : flaglist) {
                if (!flags.isEmpty()) {
                    flags += ", ";
                }
                flags += f.toString();
            }
        }
        
        // Generate owner list
        List<IPermissionUser> users = area.getUsers(AreaAccessLevel.OWNER);
        if (users.isEmpty()) {
            owners = user.getLocalizedMessage("shared.values.none");
        } else {
            owners = "";
            for (IPermissionUser u : users) {
                if (!owners.isEmpty()) {
                    owners += ", ";
                }
                owners += u.getName();
            }
        }
        
        // Generate guest list
        users = area.getUsers(AreaAccessLevel.GUEST);
        if (users.isEmpty()) {
            guests = user.getLocalizedMessage("shared.values.none");
        } else {
            guests = "";
            for (IPermissionUser u : users) {
                if (!guests.isEmpty()) {
                    guests += ", ";
                }
                guests += u.getName();
            }
        }
        
        // Generate group owner list
        List<IPermissionGroup> groups = area.getGroups(AreaAccessLevel.OWNER);
        if (groups.isEmpty()) {
            gowners = user.getLocalizedMessage("shared.values.none");
        } else {
            gowners = "";
            for (IPermissionGroup g : groups) {
                if (!gowners.isEmpty()) {
                    gowners += ", ";
                }
                gowners += g.getName();
            }
        }
        
        // Generate group guest list
        groups = area.getGroups(AreaAccessLevel.GUEST);
        if (groups.isEmpty()) {
            gguests = user.getLocalizedMessage("shared.values.none");
        } else {
            gguests = "";
            for (IPermissionGroup g : groups) {
                if (!gguests.isEmpty()) {
                    gguests += ", ";
                }
                gguests += g.getName();
            }
        }
        
        if (area.getRegionIds().size() > 0) {
            world = AreaManager.getInstance().getRegion(area.getRegionIds().get(0)).getWorld().getName();
        } else {
            world = user.getLocalizedMessage("shared.values.none");
        }
        
        user.sendLocalizedMessage("module.area.info", area.getAreaId(), (area.getLabel() == null ? user.getLocalizedMessage("module.area.label.none") : area.getLabel()), area.getPriority(), owners, guests, gowners, gguests, area.getRegionIds().size(), world);
    }
    
    private void sendHelp(User user, String commandLabel, boolean complex) {
        user.sendLocalizedMessage("module.area.header");
        user.sendLocalizedMessage((complex) ? "module.area.help.complex" : "module.area.help.simple", commandLabel);
    }
    
    private ArgumentInfo[] getArguments() {
        return new ArgumentInfo[] {
            ArgumentInfo.newString("select", "s", "select", true),
            ArgumentInfo.newSwitch("help", "?", "help"),
            
            ArgumentInfo.newKeyValuePair(ArgumentInfo.newSwitch("override", "over", "override")),
            
            ArgumentInfo.newSwitch("create", "c", "create"),
            ArgumentInfo.newSwitch("delete", "d", "delete"),
            ArgumentInfo.newSwitch("list", "ls", "list"),
            ArgumentInfo.newSwitch("all", "a", "all"),
            ArgumentInfo.newSwitch("location", "loc", "location"),
            ArgumentInfo.newSwitch("mine", "me", "mine"),
            ArgumentInfo.newInt("page", "pg", "page"),
            
            ArgumentInfo.newSwitch("info", "i", "info"),
            
            // For new areas/regions
            ArgumentInfo.newString("label", "l", "label", true),
            ArgumentInfo.newInt("priority", "p", "priority"),
            ArgumentInfo.newString("shape", "sh", "shape", false),
            ArgumentInfo.newSwitch("ignore-y", "y", "ignorey"),
            
            // For existing areas
            ArgumentInfo.newKeyValuePair(ArgumentInfo.newUserList("owner", "o", "owner", false, false)),
            ArgumentInfo.newKeyValuePair(ArgumentInfo.newGroupList("group-owner", "go", "groupowner", false)),
            ArgumentInfo.newKeyValuePair(ArgumentInfo.newUserList("invite", "in", "invite", false, false)),
            ArgumentInfo.newKeyValuePair(ArgumentInfo.newGroupList("group-invite", "gin", "groupinvite", false)),
            ArgumentInfo.newKeyValuePair(ArgumentInfo.newLong("region", "r", "region")),
        };
    }
}
