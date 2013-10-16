package erki.xpeter.parsers.tobi;

import java.util.Arrays;

import erki.api.storage.Key;
import erki.api.storage.Storage;
import erki.api.util.Log;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.util.Keys;

/**
 * Action that allows to change Tobi’s nickname.
 * 
 * @author Edgar Kalkowski <edgar.kalkowski@uni-kassel.de>
 */
public class NewNick extends Action<TextMessage> {
    
    private static final Key<String, Keys> KEY = new Key<>(Keys.TOBIS_NICK);
    private final Storage<Keys> storage;
    private String nick;
    
    /**
     * Create a new action.
     * 
     * @param storage
     *        Persistent configuration storage.
     */
    public NewNick(final Storage<Keys> storage) {
        super(TextMessage.class, true);
        this.storage = storage;
        
        if (storage.contains(KEY)) {
            this.nick = storage.get(KEY);
        } else {
            this.nick = "tobias";
            storage.add(KEY, this.nick);
        }
    }
    
    @Override
    public String getRegex() {
        return "[Tt]obi(as)? hei(ss|ß)t (ab )?jetzt (.*?)[!\\.]?";
    }
    
    @Override
    public String getDescription() {
        return "Ändere Tobis Nick.";
    }
    
    @Override
    public void execute(final String[] args, final TextMessage message) {
        Log.debug("Arguments are " + Arrays.toString(args) + ".");
        this.nick = args[args.length - 1];
        Log.debug("Tobi’s new nick is »" + this.nick + "«.");
        this.storage.add(KEY, this.nick);
        
        final long delay = (long) (Math.random() * 2000.0 + 1000.0);
        final int rnd = (int) (3 * Math.random());
        final String answer;
        
        switch (rnd) {
            case 0:
                answer = "Alles klar.";
                break;
            case 1:
                answer = "Weißsch B’scheid.";
                break;
            default:
                answer = "Ok.";
        }
        
        message.respond(new DelayedMessage(answer, delay));
    }
}
