package erki.xpeter.parsers.statistics.actions;

import java.util.TreeMap;

import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.statistics.User;

/**
 * This {@link Action} echoes how many words some user has written so far.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class UserWords extends Action<TextMessage> {
    
    private TreeMap<String, User> users;
    
    public UserWords(TreeMap<String, User> users) {
        super(TextMessage.class, true);
        this.users = users;
    }
    
    @Override
    public String getRegex() {
        return "[wW]ie ?viele [wW](ö|oe)rter hat (.*?) (bisher|schon|bisher schon)? "
                + "(gesagt|geschrieben)\\?";
    }
    
    @Override
    public String getDescription() {
        return "Gibt an wie viele Wörter ein bestimmter Nutzer schon geschrieben hat.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        
        synchronized (users) {
            
            if (users.containsKey(args[1])) {
                User user = users.get(args[1]);
                
                if (user.getWordCount() == 1) {
                    message.respond(new Message(args[1] + " hat schon ein Wort geschrieben."));
                } else {
                    message.respond(new Message(args[1] + " hat schon " + user.getWordCount()
                            + " Wörter geschrieben."));
                }
                
            } else {
                message.respond(new Message("Das weiß ich nicht."));
            }
        }
    }
}
