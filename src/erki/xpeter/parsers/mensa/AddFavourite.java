package erki.xpeter.parsers.mensa;

import java.util.Arrays;

import erki.api.storage.Storage;
import erki.api.util.Log;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.util.Keys;

/**
 * This action allows users to add their favourite meals to the {@link Mensa} parser.
 * 
 * @author Edgar Kalkowski <edgar.kalkowski@uni-kassel.de>
 */
public class AddFavourite extends Action<TextMessage> {
    
    private final Storage<Keys> storage;
    
    /** Create a new action. */
    public AddFavourite(final Storage<Keys> storage) {
        super(TextMessage.class, true);
        this.storage = storage;
    }
    
    @Override
    public String getRegex() {
        return "[Ii]ch mag (besonders )?gerne (.*)\\.?";
    }
    
    @Override
    public String getDescription() {
        return "Specify personal favourite meals.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        final String nick = message.getNick();
        Log.info("I found out: " + nick + " likes to eat " + Arrays.toString(args) + ".");
    }
}
