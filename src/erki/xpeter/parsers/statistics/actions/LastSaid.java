package erki.xpeter.parsers.statistics.actions;

import java.util.Date;
import java.util.TreeMap;

import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.statistics.Statistics;
import erki.xpeter.parsers.statistics.User;

/**
 * This {@link Action} echoes the last line some user said on the chat.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class LastSaid extends Action<TextMessage> {
    
    private TreeMap<String, User> users;
    
    /**
     * Create a new LastSaid.
     * 
     * @param users
     *        The users this bot has gathered statistical information about.
     */
    public LastSaid(TreeMap<String, User> users) {
        super(TextMessage.class, true);
        this.users = users;
    }
    
    @Override
    public String getRegex() {
        return "[wW]as hat (.*?) zuletzt gesagt\\??";
    }
    
    @Override
    public String getDescription() {
        return "Gibt wieder, was ein bestimmter Nutzer zuletzt gesagt hat.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        
        synchronized (users) {
            
            if (users.containsKey(args[0])) {
                User user = users.get(args[0]);
                TreeMap<Long, String> history = user.getHistory();
                
                if (!history.isEmpty()) {
                    Date date = new Date(history.lastKey());
                    String text = history.get(history.lastKey());
                    String day = Statistics.formatDate(date);
                    String time = Statistics.formatTime(date);
                    message.respond(new DelayedMessage(args[0] + " hat am " + day + " um " + time
                            + " gesagt: " + text, 3000));
                } else {
                    message.respond(new DelayedMessage(args[0] + " hat bisher noch nichts gesagt.",
                            3000));
                }
                
            } else {
                message.respond(new DelayedMessage("Ich weiß leider nicht, was " + args[0]
                        + " zuletzt gesagt hat.", 3000));
            }
        }
    }
}
