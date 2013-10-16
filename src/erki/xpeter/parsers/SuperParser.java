package erki.xpeter.parsers;

import java.util.LinkedList;
import java.util.List;

import erki.api.storage.Storage;
import erki.xpeter.Bot;
import erki.xpeter.BotInterface;
import erki.xpeter.msg.Message;

/**
 * Parsers of this type consist of one or more instances of {@link Action}. The actions are
 * automatically registered and deregistered with the bot.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public abstract class SuperParser implements Parser {
    
    protected List<Action<? extends Message>> actions = new LinkedList<>();
    
    /**
     * This method is called by the constructor to create all the actions this parser shall consist
     * of. Here you can add the actions you want this parser to have to {@link #actions}.
     * 
     * @param bot
     *        The bot instance this parser belongs to and which may be useful to some parsers (e.g.
     *        to obtain the bot’s {@link Storage}).
     */
    public abstract void createActions(BotInterface bot);
    
    /**
     * Get a human readable description of what this parser does.
     * 
     * @return A description of this parser’s purpose.
     */
    public abstract String getDescription();
    
    /**
     * Access the collection of actions that belong to this parser.
     * 
     * @return This parser’s actions. The returned list is not a copy so don’t mess around with it!
     *         ;)
     */
    public List<Action<? extends Message>> getActions() {
        return this.actions;
    }
    
    /**
     * Registeres all actions of this parser with the bot. If you override this method make sure to
     * call {@code super.init(Bot)} or register all actions yourself.
     */
    @Override
    public void init(Bot bot) {
        createActions(bot);
        
        for (Action<? extends Message> action : this.actions) {
            action.register(bot);
        }
    }
    
    /**
     * Deregisters all actions of this parser from the bot. If you override this method make sure to
     * call {@code super.deregister(Bot)} or deregister all the actions yourself.
     */
    @Override
    public void destroy(Bot bot) {
        
        for (Action<? extends Message> action : this.actions) {
            action.deregister(bot);
        }
    }
}
