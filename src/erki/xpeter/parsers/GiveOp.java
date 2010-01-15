package erki.xpeter.parsers;

import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.RawMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.util.BotApi;

/**
 * This enables the bot to give users operator status on irc.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class GiveOp implements Parser, Observer<TextMessage> {
    
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
        String nick = msg.getNick();
        
        if (BotApi.addresses(text, botNick)) {
            addresses = true;
            text = BotApi.trimNick(text, botNick);
        }
        
        if (addresses && text.matches("([gG]i(b|ve|pp?) ?[oO]pp?!?!?!?\\.?|[oO]pp?!?!?!?\\.?)")) {
            
            if (nick.matches("DZoom") || nick.matches("ErkiDerLoony")) {
                msg.respond(new RawMessage("MODE " + msg.getShortId() + " +o " + nick));
            } else {
                msg.respond(new DelayedMessage("Nope.", 1500));
            }
        }
    }
}
