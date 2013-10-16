package erki.xpeter.parsers.tobi;

import java.util.Calendar;

import erki.api.storage.Key;
import erki.api.storage.Storage;
import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.BotInterface;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.NickChangeMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.msg.UserJoinedMessage;
import erki.xpeter.parsers.SuperParser;
import erki.xpeter.util.Keys;

/**
 * Remind Tobi how many days are left.
 * 
 * @author Edgar Kalkowski <edgar.kalkowski@uni-kassel.de>
 */
public class Tobi extends SuperParser {
    
    private static final Key<String, Keys> KEY = new Key<>(Keys.TOBIS_NICK);
    private static final long destination;
    
    static {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 16);
        c.set(Calendar.MONTH, Calendar.NOVEMBER);
        c.set(Calendar.YEAR, 2014);
        destination = c.getTimeInMillis();
    }
    
    private Storage<Keys> storage = null;
    private Long lastNotified = null;
    private final Observer<TextMessage> textMessageObserver;
    private final Observer<NickChangeMessage> nickChangeMessageObserver;
    private final Observer<UserJoinedMessage> userJoinedMessageObserver;
    
    /** Create a new parser. */
    public Tobi() {
        
        this.textMessageObserver = new Observer<TextMessage>() {
            
            @Override
            public void inform(final TextMessage message) {
                
                if (Tobi.this.storage == null || doneForToday()) {
                    Log.debug("Tobi was already reminded.");
                    return;
                }
                
                final String tobi = Tobi.this.storage.get(KEY);
                
                if (message.getNick().equals(tobi)) {
                    Log.debug("Reminding Tobi.");
                    message.respond(new DelayedMessage(getResponse(tobi),
                            (long) (Math.random() * 300000.0 + 300000.0)));
                    Tobi.this.lastNotified = System.currentTimeMillis();
                }
            }
        };
        
        this.nickChangeMessageObserver = new Observer<NickChangeMessage>() {
            
            @Override
            public void inform(final NickChangeMessage message) {
                
                if (Tobi.this.storage == null) {
                    return;
                }
                
                final String tobi = Tobi.this.storage.get(KEY);
                
                if (message.getNewNick().equals(tobi)) {
                    Log.debug("Reminding Tobi.");
                    message.respond(new DelayedMessage(getResponse(tobi),
                            (long) (Math.random() * 300000.0 + 300000.0)));
                    Tobi.this.lastNotified = System.currentTimeMillis();
                }
            }
        };
        
        this.userJoinedMessageObserver = new Observer<UserJoinedMessage>() {
            
            @Override
            public void inform(final UserJoinedMessage message) {
                
                if (Tobi.this.storage == null) {
                    return;
                }
                
                final String tobi = Tobi.this.storage.get(KEY);
                
                if (message.getNick().equals(tobi)) {
                    Log.debug("Reminding Tobi.");
                    message.respond(new DelayedMessage(getResponse(tobi), (long) (300000.0 + Math
                            .random() * 300000.0)));
                    Tobi.this.lastNotified = System.currentTimeMillis();
                }
            }
        };
    }
    
    private final boolean doneForToday() {
        
        if (this.lastNotified == null) {
            return false;
        }
        
        return System.currentTimeMillis() - this.lastNotified < 24 * 60 * 60 * 1000;
    }
    
    private static final String getResponse(final String tobi) {
        final String response;
        final int rnd = (int) (Math.random() * 3.0);
        final long now = Calendar.getInstance().getTimeInMillis();
        final long days = (destination - now) / 1000 / 60 / 60 / 24;
        
        switch (rnd) {
            case 0:
                response = tobi + ": Denk dran: Nur noch " + days + " Tag" + (days == 1 ? "" : "e")
                        + "!";
                break;
            case 1:
                response = tobi + ": Waaaaahh, nur noch " + days + " Tag" + (days == 1 ? "" : "e")
                        + " bis zur Abgabe!";
                break;
            default:
                response = tobi + ": FYI: (Nur!) noch " + days + " Tag" + (days == 1 ? "" : "e")
                        + "!";
        }
        
        return response;
    }
    
    @Override
    public void createActions(final BotInterface bot) {
        this.actions.add(new NewNick(bot.getStorage()));
    }
    
    @Override
    public String getDescription() {
        return "Erinnere Tobi wie viele Tage ihm noch bleiben.";
    }
    
    @Override
    public void init(final Bot bot) {
        super.init(bot);
        this.storage = bot.getStorage();
        bot.register(TextMessage.class, this.textMessageObserver);
        bot.register(NickChangeMessage.class, this.nickChangeMessageObserver);
        bot.register(UserJoinedMessage.class, this.userJoinedMessageObserver);
    }
    
    @Override
    public void destroy(final Bot bot) {
        super.destroy(bot);
        bot.deregister(TextMessage.class, this.textMessageObserver);
        bot.deregister(NickChangeMessage.class, this.nickChangeMessageObserver);
        bot.deregister(UserJoinedMessage.class, this.userJoinedMessageObserver);
    }
}
