package erki.xpeter.parsers;

import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.NickChangeMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.msg.UserJoinedMessage;
import erki.xpeter.msg.UserLeftMessage;

/**
 * This parser forwards everything that is said in one chat to all other chats.
 * 
 * @author Edgar Kalkowski
 */
public class Interconnector implements Parser, Observer<TextMessage> {
    
    private Bot bot;
    
    private Observer<UserJoinedMessage> userJoinedObserver;
    
    private Observer<UserLeftMessage> userLeftObserver;
    
    private Observer<NickChangeMessage> nickChangeObserver;
    
    @Override
    public void init(final Bot bot) {
        this.bot = bot;
        bot.register(TextMessage.class, this);
        
        userJoinedObserver = new Observer<UserJoinedMessage>() {
            
            @Override
            public void inform(UserJoinedMessage msg) {
                bot.broadcast(new Message(msg.getConnection().getShortId() + ": " + msg.getNick()
                        + " hat den Chat betreten."), msg.getConnection());
            }
        };
        
        userLeftObserver = new Observer<UserLeftMessage>() {
            
            @Override
            public void inform(UserLeftMessage msg) {
                bot.broadcast(new Message(msg.getConnection().getShortId() + ": " + msg.getNick()
                        + " hat den Chat verlassen."), msg.getConnection());
            }
        };
        
        nickChangeObserver = new Observer<NickChangeMessage>() {
            
            @Override
            public void inform(NickChangeMessage msg) {
                bot.broadcast(new Message(msg.getConnection().getShortId() + ": "
                        + msg.getOldNick() + " heißt jetzt " + msg.getNewNick() + "."), msg
                        .getConnection());
            }
        };
        
        bot.register(UserJoinedMessage.class, userJoinedObserver);
        bot.register(UserLeftMessage.class, userLeftObserver);
        bot.register(NickChangeMessage.class, nickChangeObserver);
    }
    
    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
        bot.deregister(UserJoinedMessage.class, userJoinedObserver);
        bot.deregister(UserLeftMessage.class, userLeftObserver);
        bot.deregister(NickChangeMessage.class, nickChangeObserver);
    }
    
    @Override
    public void inform(TextMessage msg) {
        
        if (!msg.getNick().equals(msg.getConnection().getNick())) {
            bot.broadcast(new Message(msg.getNick() + "@" + msg.getConnection().getShortId()
                    + ": »" + msg.getText() + "«"), msg.getConnection());
        }
    }
}
