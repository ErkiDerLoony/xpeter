package erki.xpeter.parsers;

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
    
    @Override
    public void init(Bot bot) {
        bot.register(TextMessage.class, this);
    }
    
    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
    }
    
    @Override
    public void inform(TextMessage msg) {
        boolean addresses = false;
        String text = msg.getText();
        String botNick = msg.getBotNick();
        
        if (BotApi.addresses(text, botNick)) {
            addresses = true;
            text = BotApi.trimNick(text, botNick);
        }
        
        if (addresses && text.matches("([cCkK][oa]ffee")) {
            
            double random = Math.random();
            
            DelayedMessage dm = null;
            
            if (random < 0.25d) {
                
                dm = new DelayedMessage(
                        "K to the A to the double F to the double E!", 1500);
                
            } else if (random >= .25d && random < .5d) {
                
                dm = new DelayedMessage(
                        "C to the O to the double F to the double E!", 1500);
                
            } else if (random >= .5d && random < .75d) {
                
                dm = new DelayedMessage(
                        "Kay to the Ay to the Ef to the Ef to the double E?",
                        1500);
                
            } else if (random >= .75d) {
                
                dm = new DelayedMessage(
                        "Doppelter Espresso in den Pott Kaffee, ihr luschen!",
                        1500);
            }
            
            msg.respond(dm);
        }
    }
}
