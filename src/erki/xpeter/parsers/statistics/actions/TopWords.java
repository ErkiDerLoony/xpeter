package erki.xpeter.parsers.statistics.actions;

import java.util.TreeMap;
import java.util.TreeSet;

import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.statistics.User;

/**
 * This {@link Action} echoes the users that wrote the most words.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class TopWords extends Action<TextMessage> {
    
    private TreeMap<String, User> users;
    
    /**
     * Create a new TopWords.
     * 
     * @param users
     *        All users the bot has statistical information about.
     */
    public TopWords(TreeMap<String, User> users) {
        super(TextMessage.class, true);
        this.users = users;
    }
    
    @Override
    public String getRegex() {
        return "[tT]op ?(1?\\d) ?([wW]ords|[wW](ö|oe)rter)[!\\.\\?]?";
    }
    
    @Override
    public String getDescription() {
        return "Gibt die Nutzer aus, welche die meisten Wörter geschrieben haben.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        int count;
        
        try {
            count = Integer.parseInt(args[0]);
            TreeMap<Long, TreeSet<User>> users = new TreeMap<Long, TreeSet<User>>();
            
            synchronized (this.users) {
                
                for (String user : this.users.keySet()) {
                    
                    if (this.users.get(user).getWordCount() > 0) {
                        
                        if (users.containsKey(this.users.get(user).getWordCount())) {
                            TreeSet<User> list = users.get(this.users.get(user).getWordCount());
                            list.add(this.users.get(user));
                        } else {
                            TreeSet<User> list = new TreeSet<User>();
                            list.add(this.users.get(user));
                            users.put(this.users.get(user).getWordCount(), list);
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
                
                if (list[0].getWordCount() == 1) {
                    result += number + ". " + line + " mit einem Wort.\n";
                    
                } else {
                    result += number + ". " + line + " mit " + users.lastKey() + " Wörtern.\n";
                }
                
                users.remove(users.lastKey());
            }
            
            // Be failsafe here.
            if (result.length() > 0 && result.charAt(result.length() - 1) == '\n') {
                result = result.substring(0, result.length() - 1);
            }
            
            message.respond(new Message(result));
        } catch (NumberFormatException e) {
            message.respond(new Message("Ich habe deine Zahl für die Wörter (" + args[0]
                    + ") nicht " + "verstanden."));
        }
    }
}
