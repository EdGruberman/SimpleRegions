package edgruberman.bukkit.simpleregions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.messagemanager.MessageLevel;

// TODO Unfuglify
final class CommandManager implements CommandExecutor {
    
    private Main main;

    CommandManager (Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        Main.messageManager.log(
                ((sender instanceof Player) ? ((Player) sender).getName() : "[CONSOLE]")
                    + " issued command: " + label + " " + this.join(split)
                , MessageLevel.FINE
        );

        // Re-parse the command line to account for double quotes delineating a single argument that contains spaces.
        List<String> args = this.getArguments(split);
        Main.messageManager.log("split = " + Arrays.asList(split) + "    args = " + args, MessageLevel.FINEST);
        
        // Format the command line and interpret implicit parameters as necessary.
        List<String> formatted = this.formatArguments(args, sender);
        Main.messageManager.log("formatted = " + formatted, MessageLevel.FINEST);

        // Parse action argument and it's parameters.
        String action = formatted.get(2);
        List<String> parameters = new ArrayList<String>();
        if (formatted.size() >= 4) parameters = formatted.subList(3, formatted.size());
        Main.messageManager.log("parameters = " + parameters, MessageLevel.FINEST);
        
        if (action.equals("load")) {
            if (!sender.isOp()) {
                Main.messageManager.respond(sender, "You must be a server operator to issue that command.", MessageLevel.RIGHTS, false);
                return true;
            }
            Main.configurationFile.load();
            int count = this.main.loadRegions();
            Main.messageManager.respond(sender, "Loaded " + count + " regions.", MessageLevel.STATUS, false);
            return true;
        }
        
        if (action.equals("current")) {
            this.actionCurrent(sender, (formatted.size() >= 4 ? formatted.get(3) : null), null);
            return true;
        }
        
        if (action.equals("target")) {
            this.actionCurrent(sender, null, ((Player) sender).getTargetBlock(null, 50));
            return true;
        }
        
        String playerName = (sender instanceof Player ? ((Player) sender).getName() : "[CONSOLE]");
        String worldName = formatted.get(0);
        if (worldName != null) {
            if (worldName.equals("*") || worldName.equals("SERVER")) worldName = null;
        }
        String regionName = formatted.get(1);
        if (regionName != null) {
            if (regionName.equals("DEFAULT")) regionName = null;
        }
        
        // Determine region.
        // If a region creation is in progress, return the uncommitted region if no region specified or the name matches.
        // Otherwise return the matching region by name, or assume the current region if player is in only one.
        Region region = null;
        region = this.main.uncommittedRegions.get(worldName + ":" + playerName);
        if (!action.equals("create")) {
            if (region != null && regionName != null && !region.getName().equals(regionName)) region = null;
        }
        if (region == null) region = this.main.getRegion(worldName, regionName);
        Main.messageManager.log(
                "action = " + action
                    + "    region.worldName = " + (region == null ? null : region.getWorldName())
                    + "    region.name = "    + (region == null ? null : region.getName())
                , MessageLevel.FINEST
        );
        if (region == null && !action.equals("create")) {
            // TODO show different error for correct syntax, but region not defined
            this.showUsage(sender, "region", "Unable to determine region.");
            return true;
        }

        if (action.equals("detail")) {
            this.actionDetail(sender, region, parameters);
            return true;
        }
        
        if (action.equals("size")) {
            Main.messageManager.respond(sender, "\"" + region.getName() + "\" Size: " + region.describeVolume(), MessageLevel.STATUS, false);
            return true;
        }
        
        if (action.equals("area")) {
            Main.messageManager.respond(sender, "\"" + region.getName() + "\" Area: " + region.describeArea(), MessageLevel.STATUS, false);
            return true;
        }
        
        // ---- Only region owners or server operators can use commands past this point.
        if (!sender.isOp()) {
            if (!region.isOwner(((Player) sender).getName())) {
                Main.messageManager.respond(sender
                        , "You must be one of the region owners to issue that command."
                        , MessageLevel.RIGHTS
                        , false
                );
                return true;
            }
        }
        
        if (action.equals("+active")) {
            region.setActive(true);
            if (region.isCommitted()) this.main.saveRegions(false);
            this.actionDetail(sender, region, parameters);
            Main.messageManager.respond(sender, "\"" + region.getName() + "\" activated.", MessageLevel.STATUS, false);
            return true;
        }
        
        if (action.equals("-active")) {
            region.setActive(false);
            if (region.isCommitted()) this.main.saveRegions(false);
            this.actionDetail(sender, region, parameters);
            Main.messageManager.respond(sender
                    , "\"" + region.getName() + "\" deactivated; Region will no longer prevent access."
                    , MessageLevel.STATUS
                    , false
            );
            return true;
        }
        
        if (action.equals("+owner") || action.equals("-owner") || action.equals("+helper") || action.equals("-helper")) {
            if (parameters.size() == 0) {
                this.showUsage(sender, "region", "No members specified.");
                return true;
            }
            
            for (String member : parameters) {
                if (action.equals("+owner")) {
                    if (region.isDirectOwner(member)) {
                        Main.messageManager.respond(sender, "\"" + member + "\" already exists in Owners.", MessageLevel.WARNING, false);
                        continue;
                    }
                    
                    if (region.addOwner(member)) {
                        Main.messageManager.respond(sender, "\"" + member + "\" added to Owners.", MessageLevel.STATUS, false);
                        if (region.isCommitted()) {
                            this.sendIfOnline(member, MessageLevel.EVENT, "You have been added as an Owner to the \"" + region.getName() + "\" region by " + playerName + ".");
                            this.main.saveRegions(false);
                        }
                    } else {
                        Main.messageManager.respond(sender, "\"" + member + "\" is unabled to be added to Owners.", MessageLevel.WARNING, false);
                    }
                    
                    continue;
                } // End if; +owner
                
                if (action.equals("-owner")) {
                    if (!region.isDirectOwner(member)) {
                        Main.messageManager.respond(sender, "\"" + member + "\" is not currently in Owners.", MessageLevel.WARNING, false);
                        continue;
                    }
                    
                    if (region.removeOwner(member)) {
                        if (!sender.isOp()) {
                            if (!region.isOwner(playerName)) {
                                region.addOwner(member);
                                Main.messageManager.respond(sender, "You can not remove yourself as an owner.", MessageLevel.SEVERE, false);
                                continue;
                            }
                        }
                        
                        Main.messageManager.respond(sender, "\"" + member + "\" removed from Owners.", MessageLevel.STATUS, false);
                        if (region.isCommitted()) {
                            this.sendIfOnline(member, MessageLevel.EVENT, "You have been removed as an Owner from the \"" + region.getName() + "\" region by " + playerName + ".");
                            this.main.saveRegions(false);
                        }
                    } else {
                        Main.messageManager.respond(sender, "\"" + member + "\" is unable to be removed from Owners.", MessageLevel.WARNING, false);
                    }
                    
                    continue;
                } // End if; -owner
                
                if (action.equals("+helper")) {
                    if (region.isDirectAccess(member)) {
                        Main.messageManager.respond(sender, "\"" + member + "\" already exists in Helpers.", MessageLevel.WARNING, false);
                        continue;
                    }
                    
                    if (region.addHelper(member)) {
                        Main.messageManager.respond(sender, "\"" + member + "\" added to Helpers.", MessageLevel.STATUS, false);
                        if (region.isCommitted()) {
                            this.sendIfOnline(member, MessageLevel.EVENT, "You have been added as a Helper to the \"" + region.getName() + "\" region by " + playerName + ".");
                            this.main.saveRegions(false);
                        }
                    } else {
                        Main.messageManager.respond(sender, "\"" + member + "\" is unabled to be added to Helpers.", MessageLevel.WARNING, false);
                    }
                    
                    continue;
                } // End if; +helper
                
                if (action.equals("-helper")) {
                    if (!region.isDirectAccess(member)) {
                        Main.messageManager.respond(sender, "\"" + member + "\" is not currently in Helpers.", MessageLevel.WARNING, false);
                        continue;
                    }
                    
                    if (region.removeHelper(member)) {
                        Main.messageManager.respond(sender, "\"" + member + "\" removed from Helpers.", MessageLevel.STATUS, false);
                        if (region.isCommitted()) {
                            this.sendIfOnline(member, MessageLevel.EVENT, "You have been removed as a Helper from the \"" + region.getName() + "\" region by " + playerName + ".");
                            this.main.saveRegions(false);
                        }
                    } else {
                        Main.messageManager.respond(sender, "\"" + member + "\" is unable to be removed from Helpers.", MessageLevel.WARNING, false);
                    }
                    
                    continue;
                } // End if; -helper
            } // End for; member
            
            this.actionDetail(sender, region, parameters);
            Main.messageManager.respond(sender, "Members adjusted.", MessageLevel.STATUS, false);
            return true;
        } // End if; (+|-)(owner|helper)
        
        if ((action.equals("enter") || action.equals("exit")) && (parameters.size() == 0)) {
            this.actionDetail(sender, region, parameters);
            Main.messageManager.respond(sender, "Current enter message: " + region.formatEnterMessage(), MessageLevel.CONFIG, false);
            Main.messageManager.respond(sender, "Current exit message: " + region.getExitFormatted(), MessageLevel.CONFIG, false);
            return true;
        }

        // ---- Only server operators can use commands past this point.
        if (!sender.isOp()) {
            Main.messageManager.respond(sender, "You must be a server operator to issue that command.", MessageLevel.RIGHTS, false);
            return true;
        }
        
        if (action.equals("enter") || action.equals("exit")) {
            String message = null;
            if (!(parameters.size() == 1 && parameters.get(0).equals("null")))
                message = this.trimDoubleQuotes(this.join(parameters, " "));
            
            if (action.equals("enter")) {
                region.setEnterMessage(message);
                if (region.isCommitted()) this.main.saveRegions(false);
                this.actionDetail(sender, region, parameters);
                Main.messageManager.respond(sender, "Enter message set to: " + region.formatEnterMessage(), MessageLevel.STATUS, false);
                Main.messageManager.respond(sender, "Current exit message: " + region.getExitFormatted(), MessageLevel.CONFIG, false);
                return true;
            }
            
            if (action.equals("exit")) {
                region.setExitMessage(message);
                if (region.isCommitted()) this.main.saveRegions(false);
                this.actionDetail(sender, region, parameters);
                Main.messageManager.respond(sender, "Current enter message: " + region.formatEnterMessage(), MessageLevel.CONFIG, false);
                Main.messageManager.respond(sender, "Exit message set to: " + region.getExitFormatted(), MessageLevel.STATUS, false);
                return true;
            }
        }
        
        if (action.equals("name")) {
            if (parameters.size() == 0) {
                this.showUsage(sender, "region", "Name not specified.");
                return true;
            }
            
            String newName = this.trimDoubleQuotes(parameters.get(0));
            if (!this.main.isRegionUnique(region.getWorldName(), newName, null)) {
                Main.messageManager.respond(sender, "Region name \"" + newName + "\" is not unique.", MessageLevel.SEVERE, false);
                return true;
            }
            
            String original = region.getName();
            if (region.isCommitted()) {
                this.main.renameRegion(region, newName);
            } else {
                region.setName(newName);
            }
            this.actionDetail(sender, region, parameters);
            Main.messageManager.respond(sender
                    , "Region renamed from \"" + original + "\" to \"" + region.getName() + "\"."
                    , MessageLevel.STATUS
                    , false
            );
            return true;
        }
        
        if (action.equals("define")) {
            this.actionDefine(sender, region, parameters);
            return true;
        }
        
        if (action.equals("create")) {
            if (region != null && !region.isCommitted()) {
                this.actionDetail(sender, region, parameters);
                Main.messageManager.respond(sender, "You already have a definition in progress.", MessageLevel.SEVERE, false);
                return true;
            }
            
            if (parameters.size() == 0) {
                this.showUsage(sender, "region", "You must specify the new region's name.");
                return true;
            }
            
            String newName = this.trimDoubleQuotes(parameters.get(0));
            if (!this.main.isRegionUnique(worldName, newName, null)) {
                Main.messageManager.respond(sender, "Region name \"" + newName + "\" is not unique.", MessageLevel.SEVERE, false);
                return true;
            }
            
            region = new Region(worldName, newName);
            this.main.uncommittedRegions.put(worldName + ":" + playerName, region);
            this.actionDetail(sender, region, parameters);
            Main.messageManager.respond(sender, "Region created. Use: /region define", MessageLevel.STATUS, false);
            return true;
        }
        
        if (action.equals("clear")) {
            if (region == null || (region != null && region.isCommitted())) {
                Main.messageManager.respond(sender, "You do not currently have a definition in progress.", MessageLevel.SEVERE, false);
                return true;
            }
            
            this.actionDetail(sender, region, parameters);
            this.main.uncommittedRegions.remove(region.getWorldName() + ":" + playerName);
            Main.messageManager.respond(sender, "Uncommitted region cleared.", MessageLevel.STATUS, false);
            return true;
        }
        
        if (action.equals("commit")) {
            if (region == null || (region != null && region.isCommitted())) {
                Main.messageManager.respond(sender, "You do not currently have a definition in progress.", MessageLevel.SEVERE, false);
                return true;
            }
            
            if (region.getX1() == null || region.getX2() == null
                    || region.getY1() == null || region.getY2() == null
                    || region.getZ1() == null || region.getZ2() == null) {
                
                this.actionDetail(sender, region, parameters);
                Main.messageManager.respond(sender, "You have not finished defining the coordinates.", MessageLevel.SEVERE, false);
                return true;
            }
 
            region.setCommitted(true);
            this.main.addRegion(region);
            this.actionDetail(sender, region, parameters);
            this.main.uncommittedRegions.remove(region.getWorldName() + ":" + playerName);
            Main.messageManager.respond(sender, "Region committed.", MessageLevel.STATUS, false);
            this.main.saveRegions(true);
            return true;
        }
        
        if (action.equals("remove")) {
            this.actionDetail(sender, region, parameters);
            
            boolean confirmed = false;
            if ((parameters.size() != 0) && (parameters.get(0).equals("yes"))) confirmed = true;
            if (!confirmed) {
                Main.messageManager.respond(sender
                    , "Are you sure you wish to remove this region?\n"
                        + "To confirm: /region \"" + region.getName() + "\" remove yes"
                    , MessageLevel.WARNING
                    , false
                );
                return true;
            }            

            this.main.removeRegion(region);
            Main.messageManager.respond(sender, "Region removed.", MessageLevel.STATUS, false);
            return true;
        }
        
        return true;
    }
    
    // TODO Split actions into separate Action classes with aliases
    private List<String> formatArguments(List<String> original, CommandSender sender) {
        List<String> actions = Arrays.asList(
              "current", "access", "detail", "info", "target", "size", "area"
            , "+active", "-active"
            , "+owner", "+owners", "-owner", "-owners", "+helper", "+helpers", "-helper", "-helpers", "+", "-"
            , "enter", "exit"
            , "load", "define", "create", "clear", "commit", "remove", "name"
        );
        
        // Standardized Arguments: /<command> <World> <Region> <Action>[ <Parameters>]
        List<String> standard = new ArrayList<String>(Arrays.asList((String) null, null, null));
        
        // Determine <Action> argument.
        if (original.size() == 0) {
            // No arguments; Assuming current regions requested.
            standard.set(2, "current");
        } else {
            // Search for an explicit action in the first three arguments only, use first action found.
            for (String arg : original.subList(0, (original.size() <= 2 ? original.size() : 3))) {
                arg = arg.toLowerCase();
                if (actions.contains(arg)) {
                    standard.set(2, arg);
                    break;
                }
            }
            
            // Arguments exist, but no explicit action; Assuming region specified and detail requested.
            if (standard.get(2) == null) standard.set(2, "detail");
        }
        
        // Determine if <World> or <Region> arguments exist based on position of <Action> argument.
        switch (original.indexOf(standard.get(2))) {
            case -1: // /<command>[ <Parameters>]
                if (standard.get(2).equals("detail")) {
                    // /<command> <World> <Region>[ detail]
                    if (original.size() >= 2) {
                        standard.set(0, this.trimDoubleQuotes(original.get(0)));
                        standard.set(1, this.trimDoubleQuotes(original.get(1)));
                        break;
                    }
                    
                    // /<command> <Region>[ detail]
                    if (original.size() == 1) standard.set(1, this.trimDoubleQuotes(original.get(0)));
                }
            case  0: // /<command> <Action>[ <Parameters>]
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    String worldName = player.getWorld().getName();
                    
                    // Assume current world of requesting player.
                    standard.set(0, worldName);
                    
                    if (standard.get(1) == null) {
                        Region uncommitted = this.main.uncommittedRegions.get(worldName + ":" + (player.getName()));
                        if (uncommitted != null) {
                            // If a region creation is in progress assume that region first.
                            standard.set(1, uncommitted.getName());
                        } else {
                            // Otherwise, assume current region of requesting player if they are in only one region.
                            List<Region> regions = this.main.getRegions(player, false);
                            if (regions.size() == 1) standard.set(1, regions.get(0).getName());
                        }
                    }
                }
                break;
            case  1: // /<command> <Region> <Action>[ <Parameters>]
                if (sender instanceof Player) standard.set(0, ((Player) sender).getWorld().getName());
                standard.set(1, this.trimDoubleQuotes(original.get(0)));
                break;
            case  2: // /<command> <World> <Region> <Action>[ <Parameters>]
                standard.set(0, this.trimDoubleQuotes(original.get(0)));
                standard.set(1, this.trimDoubleQuotes(original.get(1)));
                break;
        }
        
        // Affix any remaining parameters after <Action> on to the end.
        int posAction = original.indexOf(standard.get(2)) + 1;
        if ((posAction == 0) && standard.get(2).equals("detail")) posAction = 2;
        if (original.size() > posAction)
            standard.addAll(3, original.subList(posAction, original.size()));
        
             if (standard.get(2).equals("+owners"))  { standard.set(2, "+owner"); }
        else if (standard.get(2).equals("-owners"))  { standard.set(2, "-owner"); }
        else if (standard.get(2).equals("+helpers")) { standard.set(2, "+helper"); }
        else if (standard.get(2).equals("-helpers")) { standard.set(2, "-helper"); }
        else if (standard.get(2).equals("+"))        { standard.set(2, "+helper"); }
        else if (standard.get(2).equals("-"))        { standard.set(2, "-helper"); }
        else if (standard.get(2).equals("access"))   { standard.set(2, "current"); }
        else if (standard.get(2).equals("info"))     { standard.set(2, "detail"); }
        
        return standard;
    }
    
    private void actionCurrent(CommandSender sender, String targetName, Block block) {
        // /<command>[ current[ <Player>]]- Show the current region(s) for specified player, defaulting to sending player.
        
        Block target;
        Player player;
        if (targetName == null) {
            if (!(sender instanceof Player)) {
                this.showUsage(sender, "region", "Target <Player> parameter required from console.");
                return;
            }
            player = (Player) sender;
            if (block != null) {
                target = block;
            } else {
                target = player.getLocation().getBlock();
            }
        } else {
            player = this.main.getServer().getPlayer(targetName);
            if (player == null) {
                this.showUsage(sender, "region", "Unable to find \"" + targetName + "\" player.");
                return;
            }
            target = player.getLocation().getBlock();
        }
        
        List<Region> regions = this.main.getRegions(target.getWorld().getName(), target.getX(), target.getY(), target.getZ(), false);
        
        String regionsMessage = null;
        for (Region r : regions) {
            if (regionsMessage == null) {
                regionsMessage = "\"" + r.getName() + "\"";
            } else {
                regionsMessage += ", \"" + r.getName() + "\"";
            }
        }
        
        String message;
        if (block != null) {
            message = "Target block (x:" + target.getX() + " y:" + target.getY() + " z:" + target.getZ() + ") ";
        } else if (sender == target) {
            message = "You are ";
        } else {
            message = player.getName() + " is ";
        }
        
        if (regions.size() == 0 ) {
            message += "currently not in any regions.";
        } else {
            message += "currently in: " + regionsMessage;
        }
        
        if (sender instanceof Player) {
            Main.messageManager.send((Player) sender, message, MessageLevel.STATUS, false);
        } else {
            message += " (World \"" + target.getWorld().getName() + "\")";
            Main.messageManager.log(message);
        }
        
        boolean access = this.main.isAllowed(player.getName(), target.getWorld().getName()
            , target.getLocation().getBlockX(), target.getLocation().getBlockY(), target.getLocation().getBlockZ());
        if (access == true) {
            if (sender == target) { Main.messageManager.send((Player) sender, "You have access here.", MessageLevel.STATUS, false);
            } else {Main.messageManager.log(player.getName() + " has access there."); }
        } else {
            if (sender == target) { Main.messageManager.send((Player) sender, "You do not have access here.", MessageLevel.WARNING, false);
            } else { Main.messageManager.log(player.getName() + " does not have access there."); }
        }
    }
    
    private void actionDetail(CommandSender sender, Region region, List<String> parameters) {
        int referenceType = 3;
        if ((parameters.size() >= 1) && this.isInteger(parameters.get(0))) {
            referenceType = Integer.parseInt(parameters.get(0));
        }
        Main.messageManager.respond(sender, region.describe(referenceType), MessageLevel.CONFIG, false);
        if (!region.isCommitted()
                && region.getX1() != null && region.getX2() != null
                && region.getY1() != null && region.getY2() != null
                && region.getZ1() != null && region.getZ2() != null) {
            Main.messageManager.respond(sender, "To finalize: /region commit", MessageLevel.NOTICE, false);
        }
    }
    
//  /region[ <Region>]define x1:-100 y1:64 z1:-2000 x2:100 y2:66 z2:2000
//  /region[ <Region>]define[ 1|2|N|E|S|W|U|D]
    private void actionDefine(CommandSender sender, Region region, List<String> parameters) {
        Block block = null;
        if (sender instanceof Player) block = ((Player) sender).getTargetBlock(null, 100);

        if (parameters.size() == 0) {
            if (block == null) {        
                Main.messageManager.respond(sender, "Unable to determine target block.", MessageLevel.WARNING, false);
                return;
            }

            if (region.getX1() == null) {
                region.setX1(block.getX());
                region.setY1(block.getY());
                region.setZ1(block.getZ());
            } else if (region.getX2() == null) {
                region.setX2(block.getX());
                region.setY2(block.getY());
                region.setZ2(block.getZ());
            } else {
                region.setX1(region.getX2());
                region.setY1(region.getY2());
                region.setZ1(region.getZ2());
                region.setX2(block.getX());
                region.setY2(block.getY());
                region.setZ2(block.getZ());
            }
            
        } else {
            String type = parameters.get(0).toUpperCase();
            if (Arrays.asList("1", "2", "N", "E", "S", "W", "U", "D").contains(type)) {
                if (type.equals("1")) {
                    region.setX1(block.getX());
                    region.setY1(block.getY());
                    region.setZ1(block.getZ());
                } else if (type.equals("2")) {
                    region.setX2(block.getX());
                    region.setY2(block.getY());
                    region.setZ2(block.getZ());
                } else if (type.equals("N")) { region.setN(block.getX());
                } else if (type.equals("E")) { region.setE(block.getZ());
                } else if (type.equals("S")) { region.setS(block.getX());
                } else if (type.equals("W")) { region.setW(block.getZ());
                } else if (type.equals("U")) { region.setU(block.getY());
                } else if (type.equals("D")) { region.setD(block.getY());
                }
                
            } else {
                if (!parameters.get(0).contains(":")) {
                    this.showUsage(sender, "region", "Parameters must be recognizable key:value pairs.");
                    return;
                }
                
                // First parameter contains a colon, so look through the rest for more coordinates also.
                String key; int value;
                for (String coord : parameters) {
                    if (!coord.contains(":")) continue;
                    
                    if (!this.isInteger(coord.split(":")[1])) continue;
                    
                    value = Integer.parseInt(coord.split(":")[1]);
                    key = coord.split(":")[0].toLowerCase();
                         if (key.equals("x1")) { region.setX1(value); }
                    else if (key.equals("x2")) { region.setX2(value); }
                    else if (key.equals("y1")) { region.setY1(value); }
                    else if (key.equals("y2")) { region.setY2(value); }
                    else if (key.equals("z1")) { region.setZ1(value); }
                    else if (key.equals("z2")) { region.setZ2(value); }
                    else if (key.equals("n")) { region.setN(value); }
                    else if (key.equals("e")) { region.setE(value); }
                    else if (key.equals("s")) { region.setS(value); }
                    else if (key.equals("w")) { region.setW(value); }
                    else if (key.equals("u")) { region.setU(value); }
                    else if (key.equals("d")) { region.setD(value); }
                }
            }
        }
        
        if (region.isCommitted()) this.main.saveRegions(false);
        
        // Show configuration of region after update.
        this.actionDetail(sender, region, parameters);
        Main.messageManager.respond(sender, "Size: " + region.describeVolume(), MessageLevel.CONFIG, false);
        Main.messageManager.respond(sender, "Coordinate definition updated.", MessageLevel.STATUS, false);
        
        return;
    }
        
    private void showUsage(CommandSender sender, String label, String error) {
        Main.messageManager.respond(sender, "Syntax Error: " + error, MessageLevel.SEVERE, false);
        this.showUsage(sender, label);
    }
    
    private void showUsage(CommandSender sender, String label) {
      //TODO show only action usage and help usage if not help
        Main.messageManager.respond(sender, this.main.getCommand(label).getUsage().replaceAll("<command>", label), MessageLevel.NOTICE, false);
    }
    
    /**
     * Concatenate arguments to compensate for double quotes indicating single argument.
     * 
     * @return String array with double quoted arguments combined into a single argument.
     */
    private List<String> getArguments(String[] split) {
        List<String> args = new ArrayList<String>();
        
        String previousArg = null;
        for (String arg : split) {
            if (previousArg != null) {
                if (arg.endsWith("\"")) {
                    args.add(previousArg + " " + arg);
                    previousArg = null;
                } else {
                    previousArg += " " + arg;
                }
                continue;
            }

            if (arg.startsWith("\"") && !arg.endsWith("\"")) {
                previousArg = arg;
            } else {
                args.add(arg);
            }
        }
        if (previousArg != null) args.add(previousArg);
        
        return args;
    }
    
    private void sendIfOnline(String name, MessageLevel level, String message) {
        if (this.getExactPlayer(name) == null) return;
        
        Main.messageManager.send(this.getExactPlayer(name), message, level);
    }
    
    /**
     * Returns player only if it is a full and case insensitive name match.
     * 
     * @param name Name of player.
     * @return Player that matches name.
     */
    private Player getExactPlayer(String name) {
        Player player = this.main.getServer().getPlayer(name);
        if (player == null) return null;
        
        if (!player.getName().equalsIgnoreCase(name)) return null;
        
        return player;
    }
    
    private boolean isInteger(String s) {   
        try {   
            Integer.parseInt(s);   
            return true;   
        }   
        catch(Exception e) {   
            return false;   
        }   
    }
    
    private String join(String[] s) {
        return this.join(Arrays.asList(s), " ");
    }
    
    private String join(List<String> list, String delim) {
        if (list == null || list.isEmpty()) return "";
     
        StringBuilder sb = new StringBuilder();
        for (String s : list) sb.append(s + delim);
        sb.delete(sb.length() - delim.length(), sb.length());
        
        return sb.toString();
    }
    
    private String trimDoubleQuotes(String s) {
        return s.replaceAll("^\"|\"$", "");
    }
}