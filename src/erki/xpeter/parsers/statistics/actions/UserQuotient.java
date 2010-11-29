package erki.xpeter.parsers.statistics.actions;

import java.text.NumberFormat;
import java.util.TreeMap;

import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.statistics.User;

/**
 * This {@link Action} echoes how many words per line some user has written.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class UserQuotient extends Action<TextMessage> {
    
    private TreeMap<String, User> users;
    
    /**
     * Create a new UserQuotient.
     * 
     * @param users
     *        The users this bot has gathered statistical information about.
     */
    public UserQuotient(TreeMap<String, User> users) {
        super(TextMessage.class, true);
        this.users = users;
    }
    
    @Override
    public String getRegex() {
        return "[Ww]ie ?viele [wW](ö|oe)rter pro [zZ]eile hat "
                + "(.*?) (bisher|schon|bisher schon| im ([Dd]urch)?"
                + "[sS]chnitt)? (gesagt|geschrieben)\\?";
    }
    
    @Override
    public String getDescription() {
        return "Gibt an wie viele Wörter pro Zeile ein Nutzer geschrieben hat.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        
        synchronized (users) {
            
            if (users.containsKey(args[1])) {
                User user = users.get(args[1]);
                NumberFormat nf = NumberFormat.getNumberInstance();
                nf.setMinimumFractionDigits(3);
                nf.setMaximumFractionDigits(3);
                message.respond(new Message(args[1] + " hat im Schnitt "
                        + nf.format(user.getWordCount() / (double) user.getLineCount())
                        + " Wörter pro Zeile geschrieben."));
            } else {
                message.respond(new Message("Das weiß ich nicht."));
            }
        }
    }
}
