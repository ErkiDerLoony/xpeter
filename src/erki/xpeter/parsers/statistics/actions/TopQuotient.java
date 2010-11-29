package erki.xpeter.parsers.statistics.actions;

import java.text.NumberFormat;
import java.util.TreeMap;
import java.util.TreeSet;

import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.statistics.User;

/**
 * This {@link Action} echoes the users who wrote the most words per line.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class TopQuotient extends Action<TextMessage> {
    
    private TreeMap<String, User> users;
    
    /**
     * Create a new TopQuotient.
     * 
     * @param users
     *        The users this bot has gathered statistical information about.
     */
    public TopQuotient(TreeMap<String, User> users) {
        super(TextMessage.class, true);
        this.users = users;
    }
    
    @Override
    public String getRegex() {
        return "[tT]op ?(1?\\d) ?([qQ]uotient|[wW](ö|oe)rter pro [zZ]eile|[pP]ro|"
                + "[wW][pP][Zz])[!\\.\\?]?";
    }
    
    @Override
    public String getDescription() {
        return "Gibt die Nutzer aus, welche die meisten Wörter pro Zeile geschrieben haben.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        int count;
        
        try {
            count = Integer.parseInt(args[0]);
            TreeMap<Double, TreeSet<User>> users = new TreeMap<Double, TreeSet<User>>();
            
            synchronized (this.users) {
                
                for (String user : this.users.keySet()) {
                    
                    if (this.users.get(user).getLineCount() > 0) {
                        
                        if (users.containsKey(this.users.get(user).getWordCount()
                                / (double) this.users.get(user).getLineCount())) {
                            TreeSet<User> list = users.get(this.users.get(user).getWordCount()
                                    / (double) this.users.get(user).getLineCount());
                            list.add(this.users.get(user));
                        } else {
                            TreeSet<User> list = new TreeSet<User>();
                            list.add(this.users.get(user));
                            users.put(this.users.get(user).getWordCount()
                                    / (double) this.users.get(user).getLineCount(), list);
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
                
                NumberFormat nf = NumberFormat.getNumberInstance();
                nf.setMinimumFractionDigits(1);
                nf.setMaximumFractionDigits(3);
                
                for (int j = 0; j < list.length; j++) {
                    
                    if (j < list.length - 2) {
                        line += list[j].getName() + ", ";
                    } else if (j < list.length - 1) {
                        line += list[j].getName() + " und ";
                    } else {
                        line += list[j].getName();
                    }
                }
                
                result += number + ". " + line + " mit " + nf.format(users.lastKey())
                        + " Wörtern pro Zeile.\n";
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
