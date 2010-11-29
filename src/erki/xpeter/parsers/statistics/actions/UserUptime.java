package erki.xpeter.parsers.statistics.actions;

import java.util.TreeMap;

import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.statistics.Statistics;
import erki.xpeter.parsers.statistics.User;

/**
 * This {@link Action} echoes how long some user was already online.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class UserUptime extends Action<TextMessage> {
    
    private TreeMap<String, User> users;
    
    /**
     * Create a new UserUptime.
     * 
     * @param users
     *        The users this bot has gathered statistical information about.
     */
    public UserUptime(TreeMap<String, User> users) {
        super(TextMessage.class, true);
        this.users = users;
    }
    
    @Override
    public String getRegex() {
        return "[wW]ie lange (war|ist) (.*?) (schon )?(online|hier|da)( gewesen)?\\?";
    }
    
    @Override
    public String getDescription() {
        return "Gibt an wie lange ein bestimmter Nutzer schon online war.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        
        synchronized (users) {
            
            if (users.containsKey(args[1])) {
                User user = users.get(args[1]);
                message.respond(new Message(args[1] + " war schon " + Statistics.formatTime(user.getUptime())
                        + " online."));
            } else {
                message.respond(new Message("Das weiß ich nicht."));
            }
        }
    }
}
