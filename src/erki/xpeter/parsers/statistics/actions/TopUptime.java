package erki.xpeter.parsers.statistics.actions;

import java.util.TreeMap;
import java.util.TreeSet;

import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.statistics.Statistics;
import erki.xpeter.parsers.statistics.User;

/**
 * This {@link Action} echoes the users with the most uptime.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class TopUptime extends Action<TextMessage> {
    
    private TreeMap<String, User> users;
    
    /**
     * Create a new TopUptime.
     * 
     * @param users
     *        The users this bot has gathered statistical information about.
     */
    public TopUptime(TreeMap<String, User> users) {
        super(TextMessage.class, true);
        this.users = users;
    }
    
    @Override
    public String getRegex() {
        return "[tT]op ?(1?\\d) ?([Uu]ptime|[zZ]eit online|[oO]nline ?[Zz]eit)[!\\.\\?]?";
    }
    
    @Override
    public String getDescription() {
        return "Gibt die Nutzer mit der längsten Uptime aus.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        int count;
        
        try {
            count = Integer.parseInt(args[0]);
            TreeMap<Long, TreeSet<User>> users = new TreeMap<Long, TreeSet<User>>();
            
            synchronized (this.users) {
                
                for (String user : this.users.keySet()) {
                    
                    if (users.containsKey(this.users.get(user).getUptime())) {
                        TreeSet<User> list = users.get(this.users.get(user).getUptime());
                        list.add(this.users.get(user));
                    } else {
                        TreeSet<User> list = new TreeSet<User>();
                        list.add(this.users.get(user));
                        users.put(this.users.get(user).getUptime(), list);
                    }
                }
            }
            
            int size = users.size();
            String result = "";
            
            for (int i = 0; i < count && !users.isEmpty(); i++) {
                User[] list = users.get(users.lastKey()).toArray(new User[0]);
                String number = "", line = "";
                
                if ((i + 1) < 10 && count > 9 && size > 9) {
                    number += "0" + (i + 1);
                } else {
                    number += (i + 1);
                }
                
                for (int j = 0; j < list.length; j++) {
                    
                    if (j < list.length - 2) {
                        line += list[j].getName() + ", ";
                    } else if (j < list.length - 1) {
                        line += list[j].getName() + " und ";
                    } else {
                        line += list[j].getName();
                    }
                }
                
                result += number + ". " + line + " mit " + Statistics.formatTime(users.lastKey())
                        + ".\n";
                users.remove(users.lastKey());
            }
            
            if (result.length() > 0 && result.charAt(result.length() - 1) == '\n') {
                result = result.substring(0, result.length() - 1);
            }
            
            message.respond(new Message(result));
        } catch (NumberFormatException e) {
            message.respond(new Message("Ich habe deine Zahl (" + args[0] + ") nicht "
                    + "verstanden."));
        }
    }
}
