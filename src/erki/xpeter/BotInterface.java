package erki.xpeter;

import java.util.Collection;
import java.util.Set;

import erki.api.storage.Storage;
import erki.xpeter.con.Connection;
import erki.xpeter.msg.Message;
import erki.xpeter.parsers.Parser;
import erki.xpeter.parsers.SuperParser;
import erki.xpeter.util.Keys;

/**
 * This interface is only implemented by {@link Bot}. It hides all methods from parsers deriving
 * {@link SuperParser} that should not be used by them (of course you could cast an instance of
 * BotInterface to Bot and break this restriction, but then you should know what you are doing!).
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public interface BotInterface {
    
    /**
     * Add new parsers to this bot. There can only be one instance of every parser class active at
     * one time.
     * 
     * @param clazz
     *        The class object describing the new parser.
     */
    public void add(Class<? extends Parser> clazz);
    
    /**
     * Access the set of parsers currently loaded by this bot.
     * 
     * @return The set of currently loaded parsers.
     */
    public Set<Parser> getParsers();
    
    /**
     * Remove parsers from this bot. The corresponding {@link Parser#destroy(Bot)} method is called
     * in which the parser itself must deregister all its listeners and finish all threads it may
     * have started.
     * 
     * @param clazz
     *        A class object describing the parser to remove.
     */
    public void remove(Class<? extends Parser> clazz);
    
    /**
     * Access the storage facility of this bot. Parsers can use it to persistently store
     * information.
     * 
     * @return A persistent storage facility.
     */
    public Storage<Keys> getStorage();
    
    /**
     * Access all connections of this bot. The returned instances of Connection are no copies! So
     * don’t mess with them! ;)
     * 
     * @return A Collection of all Connections of this bot.
     */
    public Collection<Connection> getConnections();
    
    /**
     * Broadcast a message to all connections currently available to this bot.
     * 
     * @param msg
     *        The message to broadcast.
     */
    public void broadcast(Message msg);
    
    /**
     * Broadcast a message to all connection currently available to this bot with the exception of
     * one connection.
     * 
     * @param msg
     *        The message to broadcast.
     * @param shortId
     *        The short identifier of the connection that will not receive {@code msg}.
     */
    public void broadcast(Message msg, String shortId);
    
}
