package erki.xpeter.parsers;

import java.util.Calendar;
import java.util.Random;

import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.TextMessage;

/**
 * Parser reminding us of the next Dagstuhl-event.
 * 
 * <p>
 * It's gonna be legen -- wait for it
 * </p>
 *
 * @author Martin Jänicke <m.jaenicke@gmail.com>
 */
public final class DagstuhlParser implements Parser, Observer<TextMessage> {
    
    private static final String[] MSG_STARTS = new String[] {
        "So, noch ", 
        "OK, ihr habt noch ",
        "Freudige Mitteilung an alle: nur noch "
        };
    
    private static final String DAYS = " Tage bis zum nächsten mal Dagstuhl! Also ";
    
    private static final String[] COMMENTS = new String[] {
        "Käseplatten leerfuttern und Wein trinken.", 
        "im Tischtennis gegen Tobi verlieren.", 
        "Bier verköstigen. Viel Bier. Sehr viel Bier.", 
        "Koffein intravenös aufnehmen.",
        "Vorträge halten und hören.",
        "neue Forschungsergebnisse, Erkenntnisse und (Achtung Geek-Witz) Blötzinn vorstellen."
        };
    
    private Random random;
    
    private long lastReminded;
    
    private long nextDagstuhl;
    
    @Override
    public void init(Bot bot) {
        bot.register(TextMessage.class, this);
        this.random = new Random(31337);
        this.lastReminded = System.currentTimeMillis();
        
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 9);
        c.set(Calendar.MONTH, Calendar.JUNE);
        c.set(Calendar.YEAR, 2014);
        this.nextDagstuhl = c.getTimeInMillis();
    }
    
    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
    }

    @Override
    public void inform(TextMessage msg) {
        
        long now = System.currentTimeMillis();
        
        if (now - this.lastReminded >= 24 * 60 * 60 * 1000) {
            
            if (this.random.nextDouble() < .6) {
                
                final long days = (this.nextDagstuhl - now) / 1000 / 60 / 60 / 24;
                StringBuilder message = new StringBuilder(MSG_STARTS[this.random.nextInt(MSG_STARTS.length)]);
                message.append(days);
                message.append(DAYS);
                message.append(COMMENTS[this.random.nextInt(COMMENTS.length)]);
                
                msg.respond(new DelayedMessage(message.toString(), 1000));
            }
        }
    }
}
