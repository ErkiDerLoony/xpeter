package erki.xpeter.parsers.statistics.actions;

import java.util.TreeMap;

import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.statistics.User;
import erki.xpeter.util.BotApi;

/**
 * This {@link Action} echoes the list of nicknames of all the users about who the bot has
 * statistical information.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class Who extends Action<TextMessage> {
    
    private TreeMap<String, User> users;
    
    /**
     * Create a new Who.
     * 
     * @param users
     *        The users this bot has gathered statistical information about.
     */
    public Who(TreeMap<String, User> users) {
        super(TextMessage.class, true);
        this.users = users;
    }
    
    @Override
    public String getRegex() {
        return "((Ü|ü|ue|Ue)ber wen|[vV]on wem) (hast|f(ü|ue)hrst) [dD]u (alles )?"
                + "(eine [sS]tatistik|statistische ([iI]nfo(rmationen|s)|[dD]aten))\\?";
    }
    
    @Override
    public String getDescription() {
        return "Gibt die Nicks aller Nutzer aus über die statistische Informationen vorliegen.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        
        synchronized (users) {
            
            if (users.keySet().isEmpty()) {
                message.respond(new Message("Momentan habe ich zu "
                        + "niemandem statistische Informationen."));
            } else if (users.keySet().size() == 1) {
                message.respond(new Message("Momentan habe ich nur zu " + users.firstKey()
                        + " statistische Informationen."));
            } else {
                message.respond(new Message("Ich sammle momentan statistische Daten über "
                        + BotApi.enumerate(users.keySet()) + "."));
            }
        }
    }
}
