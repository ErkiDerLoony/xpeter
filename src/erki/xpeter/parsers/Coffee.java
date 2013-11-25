package erki.xpeter.parsers;

import java.util.Random;

import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.util.BotApi;

/**
 * This implementation of a parser emphasizes one of the most urgent needs of
 * the human kind.
 * 
 * @author Martin Jänicke <m.jaenicke@gmail.com>
 */
public class Coffee implements Parser, Observer<TextMessage> {
    
    private Random random;
    
    private static final String[] MESSAGES = new String[] {
        "K to the A to the double F to the double E!", 
        "C to the O to the double F to the double E!",
        "Kay to the Ay to the Ef to the Ef to the double E?",
        "Doppelter Espresso in den Pott Kaffee, ihr Luschen!",
        "Nix Kaffee, T! Oder mal 'n L oder 'n M.",
        "Mh Kaffee...",
        "Was ist besser als ein Kaffee? Zwei Kaffee!" };
    
    @Override
    public void init(Bot bot) {
        bot.register(TextMessage.class, this);
        random = new Random(31337);
    }
    
    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
    }
    
    @Override
    public void inform(TextMessage msg) {
        boolean addresses = false;
        String text = msg.getText();
        
        if (text.matches(".*[cCkK][oa]ffee.*")) {
            
            double random = Math.random();
            
            if (random > .7d) {
                // respond with a 30% probability
                
                final int decider = this.random.nextInt(MESSAGES.length);
                
                DelayedMessage dm = new DelayedMessage(MESSAGES[decider], 1500);
                
                msg.respond(dm);
            }
        }
    }
}

