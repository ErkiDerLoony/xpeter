package erki.xpeter.parsers.statistics.actions;

import java.util.Date;
import java.util.LinkedList;
import java.util.TreeMap;

import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.statistics.Statistics;
import erki.xpeter.parsers.statistics.User;

/**
 * This {@link Action} echoes the last things some user has said.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class History extends Action<TextMessage> {
    
    private TreeMap<String, User> users;
    
    /**
     * Create a new History.
     * 
     * @param users
     *        The users this bot has gathered statistical information about.
     */
    public History(TreeMap<String, User> users) {
        super(TextMessage.class, true);
        this.users = users;
    }
    
    @Override
    public String getRegex() {
        return "[wW]as waren die( letzten)? ([23456789]|10)( letzten)? "
                + "([dD]inge|[sS]achen|[zZ]eilen),? die (.*?) (zuletzt )?gesagt hat\\??";
    }
    
    @Override
    public String getDescription() {
        return "Gibt aus, was die letzten Worte waren, die jemand im Chat gesagt hat.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        String number = args[0];
        String nick = args[3];
        
        synchronized (users) {
            
            if (users.containsKey(nick)) {
                TreeMap<Long, String> history = users.get(nick).getHistory();
                String response = nick + " hat zu letzt gesagt:\n";
                
                try {
                    int n = Integer.parseInt(number);
                    int counter = 0;
                    LinkedList<String> lines = new LinkedList<String>();
                    
                    for (long key : history.descendingKeySet()) {
                        lines.addFirst("[" + Statistics.formatDate(new Date(key)) + ", "
                                + Statistics.formatTime(new Date(key)) + "] " + history.get(key));
                        counter++;
                        
                        if (counter >= n) {
                            break;
                        }
                    }
                    
                    for (String line : lines) {
                        response += line + "\n";
                    }
                    
                    response = response.substring(0, response.length() - 1);
                    message.respond(new DelayedMessage(response, 2222));
                } catch (NumberFormatException e) {
                    message.respond(new DelayedMessage("Ich habe leider deine Zahl (" + number
                            + ") nicht verstanden.", 2222));
                }
                
            } else {
                message.respond(new DelayedMessage("Das weiß ich leider nicht.", 2222));
            }
        }
    }
}
