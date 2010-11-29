package erki.xpeter.parsers.statistics.actions;

import java.util.TreeMap;
import java.util.TreeSet;

import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.statistics.User;

/**
 * This {@link Action} echoes the top users with respect to the number of lines they wrote.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class TopLines extends Action<TextMessage> {
    
    private TreeMap<String, User> users;
    
    /**
     * Create a new TopLines.
     * 
     * @param users
     *        The users this bot has gathered statistical information about.
     */
    public TopLines(TreeMap<String, User> users) {
        super(TextMessage.class, false);
        this.users = users;
    }
    
    @Override
    public String getRegex() {
        return "[tT]op ?(1?\\d) ?([lL]ines|[zZ]eilen)?[!\\.\\?]?";
    }
    
    @Override
    public String getDescription() {
        return "Gibt die Nutzer aus, welche die meisten Zeilen geschrieben haben.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        int count;
        
        try {
            count = Integer.parseInt(args[0]);
            TreeMap<Long, TreeSet<User>> users = new TreeMap<Long, TreeSet<User>>();
            
            synchronized (users) {
                
                for (String user : this.users.keySet()) {
                    
                    if (this.users.get(user).getLineCount() > 0) {
                        
                        if (users.containsKey(this.users.get(user).getLineCount())) {
                            users.get(this.users.get(user).getLineCount())
                                    .add(this.users.get(user));
                        } else {
                            TreeSet<User> list = new TreeSet<User>();
                            list.add(this.users.get(user));
                            users.put(this.users.get(user).getLineCount(), list);
                        }
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
                
                if (list[0].getLineCount() == 1) {
                    result += number + ". " + line + " mit einer Zeile.\n";
                } else {
                    result += number + ". " + line + " mit " + users.lastKey() + " Zeilen.\n";
                }
                
                users.remove(users.lastKey());
            }
            
            // Be failsafe here.
            if (result.length() > 0 && result.charAt(result.length() - 1) == '\n') {
                result = result.substring(0, result.length() - 1);
            }
            
            message.respond(new Message(result));
        } catch (NumberFormatException e) {
            message.respond(new Message("Ich habe deine Zahl für die Zeilen (" + args[0]
                    + ") nicht " + "verstanden."));
        }
    }
}
