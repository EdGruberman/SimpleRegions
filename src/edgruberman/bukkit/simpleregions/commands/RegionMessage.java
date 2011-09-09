package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;
import edgruberman.java.CaseInsensitiveString;
import edgruberman.java.FormattedString;

public class RegionMessage extends Action {
    
    public static final String NAME = "message";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("enter", "exit", "=enter", "=exit"));
    
    RegionMessage(final Command owner) {
        super(owner, RegionMessage.NAME, Permission.REGION_ACTIVE);
        this.aliases.addAll(RegionMessage.ALIASES);
    }
    
    @Override
    void execute(final Context context) {
        edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null) {
            Main.messageManager.respond(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }
        
        CaseInsensitiveString operation = new CaseInsensitiveString(context.arguments.get(context.actionIndex));
        if (operation.equals("=enter") || operation.equals("message")) operation = new CaseInsensitiveString("enter");
        if (operation.equals("=exit")) operation = new CaseInsensitiveString("exit");
        
        FormattedString message = region.enter;
        if (operation.equals("exit")) message = region.exit;
        
        // Set format if parameters supplied past action
        if (context.arguments.size() > context.actionIndex + 1) {
            String format = Command.join(context.arguments.subList(context.actionIndex + 1, context.arguments.size()), " ");
            message.setFormat(format);
            
            Main.saveRegion(region, false);
            Main.messageManager.respond(context.sender, "Region " + operation + " format set.", MessageLevel.STATUS, false);
        }
        
        Main.messageManager.respond(context.sender, "Region " + operation + " format: " + message.getFormat()
                + "\nRegion " + operation + " message: " + message.formatted, MessageLevel.CONFIG, false);
    }
}