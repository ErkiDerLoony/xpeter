package erki.xpeter.parsers;

import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;

/**
 * This parser forwards everything that is said in one chat to all other chats.
 * 
 * @author Edgar Kalkowski
 */
public class Interconnector implements Parser, Observer<TextMessage> {
    
    private Bot bot;
    
    @Override
    public void init(Bot bot) {
        Log.debug("Initializing.");
        this.bot = bot;
        bot.register(TextMessage.class, this);
    }
    
    @Override
    public void inform(TextMessage msg) {
        Log.debug("Parsing " + msg + ".");
        
        if (!msg.getNick().equals(msg.getConnection().getNick())) {
            bot.broadcast(new Message(msg.getNick() + "@" + msg.getConnection().getShortId()
                    + ": »" + msg.getText() + "«"), msg.getConnection());
        }
    }
}
