package edgruberman.bukkit.simpleregions;

import java.util.List;

/**
 * Group management abstraction.
 * TODO: a lot.
 * 
 * @author EdGruberman (ed@rjump.com)
 */
public class GroupManager {
    
    private Main plugin;

    public GroupManager(Main plugin) {
        this.plugin = plugin;
    }
    
    public List<String> getMembers(String group) {
        return this.getMembers(group, false);
    }
    
    public List<String> getMembers(String group, boolean expand) {
        List<String> members = this.getMembersSource(group);
        if (!expand) return members;

        //TODO Abstract expansion for wrapping external plugin;
        //TODO Expand recursively more than one level, but check for circular references along the way.
        //TODO Use a constant that describes group format for regex matching.
        for (String member : members) {
            if (member.startsWith("[") && member.endsWith("]")) {
                // Expand group name
                members.addAll(this.getMembers(member.substring(1, member.length() - 1)));
            } else {
                // Direct player name
                members.add(member);
            }
        }
        return members;
    }
    
    private List<String> getMembersSource(String group) {
        return this.plugin.groupsGetMembers(group);
    }
    
}