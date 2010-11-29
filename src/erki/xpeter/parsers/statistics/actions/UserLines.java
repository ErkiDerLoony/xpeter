package erki.xpeter.parsers.statistics.actions;

import java.util.TreeMap;

import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.statistics.User;

/**
 * This {@link Action} echoes the number of lines some user has already written in the chats.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class UserLines extends Action<TextMessage> {
    
    private TreeMap<String, User> users;
    
    /**
     * Create a new UserLines.
     * 
     * @param users
     *        The users this bot has gathered statistical information about.
     */
    public UserLines(TreeMap<String, User> users) {
        super(TextMessage.class, true);
        this.users = users;
    }
    
    @Override
    public String getRegex() {
        return "[wW]ie ?viele [zZ]eilen hat (.*?) (bisher|schon|bisher schon)? "
                + "(gesagt|geschrieben)\\?";
    }
    
    @Override
    public String getDescription() {
        return "Gibt aus wie viele Zeilen ein bestimmter Nutzer schon geschrieben hat.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        
        synchronized (users) {
            
            if (users.containsKey(args[0])) {
                User user = users.get(args[0]);
                
                if (user.getLineCount() == 1) {
                    message.respond(new Message(args[0] + " hat schon eine Zeile geschrieben."));
                } else {
                    message.respond(new Message(args[0] + " hat schon " + user.getLineCount()
                            + " Zeilen geschrieben."));
                }
                
            } else {
                message.respond(new Message("Das weiß ich nicht."));
            }
        }
    }
}
