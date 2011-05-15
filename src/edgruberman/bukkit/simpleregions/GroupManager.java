package edgruberman.bukkit.simpleregions;

import java.util.ArrayList;
import java.util.List;

import edgruberman.bukkit.simplegroups.Group;
import edgruberman.bukkit.simplegroups.Member;

/**
 * Group management abstraction.
 * TODO: a lot.
 */
public class GroupManager {
    
    public static List<String> getMembers(String name) {
        return GroupManager.getMembers(name, false);
    }
    
    public static List<String> getMembers(String name, boolean expand) {
        List<String> members = GroupManager.getMembersSource(name);
        if (!expand) return members;

        //TODO Abstract expansion for wrapping external plugin;
        //TODO Expand recursively more than one level, but check for circular references along the way.
        //TODO Use a constant that describes group format for regex matching.
        for (String member : members) {
            if (member.startsWith("[") && member.endsWith("]")) {
                // Expand group name
                members.addAll(GroupManager.getMembers(member.substring(1, member.length() - 1)));
            } else {
                // Direct player name
                members.add(member);
            }
        }
        return members;
    }
    
    private static List<String> getMembersSource(String name) {
        List<String> members = new ArrayList<String>();

        Group group = edgruberman.bukkit.simplegroups.GroupManager.getGroup(name);
        if (group == null) return null;
        
        for (Member member : group.getMembers()) {
            members.add(member.getName());
        }
        
        return members;
    }
    
}