package erki.xpeter.parsers.statistics.actions;

import java.util.Date;
import java.util.TreeMap;

import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.statistics.Statistics;
import erki.xpeter.parsers.statistics.User;

/**
 * This {@link Action} echoes when some user was last seen online in the chat.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class LastSeen extends Action<TextMessage> {
    
    private TreeMap<String, User> users;
    
    /**
     * Create a new LastSee.
     * 
     * @param users
     *        The users this bot has gathered statistical information about.
     */
    public LastSeen(TreeMap<String, User> users) {
        super(TextMessage.class, true);
        this.users = users;
    }
    
    @Override
    public String getRegex() {
        return "[wW]ann (war|ist) (.*?) (zuletzt|"
                + "zum letzten [mM]al) (online|da|hier)( gewesen)?\\?";
    }
    
    @Override
    public String getDescription() {
        return "Gibt an wann ein Nutzer zuletzt im Chat online war.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        
        synchronized (users) {
            
            if (users.containsKey(args[1])) {
                Date lastOnline = users.get(args[1]).getLastOnline();
                
                if (lastOnline == null) {
                    message.respond(new Message(args[1] + " ist gerade online!"));
                } else {
                    message.respond(new Message(args[1] + " war zuletzt "
                            + Statistics.formatDate(lastOnline) + " um "
                            + Statistics.formatTime(lastOnline) + " online."));
                }
                
            } else {
                message.respond(new Message("Das weiß ich nicht."));
            }
        }
    }
}
