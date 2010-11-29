/*
 * © Copyright 2008–2010 by Edgar Kalkowski <eMail@edgar-kalkowski.de>
 * 
 * This file is part of the chatbot xpeter.
 * 
 * The chatbot xpeter is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package erki.xpeter;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import erki.api.storage.Storage;
import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.con.Connection;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.Message;
import erki.xpeter.parsers.Parser;
import erki.xpeter.util.Keys;

/**
 * This class connects the different connections the bot handles. If a new connection is added (via
 * {@link #add(Connection)}) the new connection is immediately started. This class also contains all
 * the parsers and {@link #process(Message)} is called by the connections to delegate incoming
 * messages to all available parsers.
 * 
 * @author Edgar Kalkowski
 */
public class Bot implements BotInterface {
    
    private Collection<Connection> cons = new LinkedList<Connection>();
    
    private Set<Parser> parsers = new TreeSet<Parser>(new Comparator<Parser>() {
        
        public int compare(Parser o1, Parser o2) {
            return (o1.getClass().getCanonicalName().compareTo(o2.getClass().getCanonicalName()));
        }
    });
    
    private TreeMap<String, LinkedList<Observer<? extends Message>>> parserMapping = new TreeMap<String, LinkedList<Observer<? extends Message>>>();
    
    private final Storage<Keys> storage;
    
    /**
     * Create a new Bot with an initial set of some parsers.
     * 
     * @param parsers
     *        The initially used parsers of this Bot.
     */
    public Bot(Iterable<Class<? extends Parser>> parsers, Storage<Keys> storage) {
        this.storage = storage;
        
        for (Class<? extends Parser> clazz : parsers) {
            add(clazz);
        }
    }
    
    @Override
    public void add(Class<? extends Parser> clazz) {
        Log.debug("Loading parser " + clazz.getSimpleName() + ".");
        
        /*
         * Try to instanicate all the parser classes and be sure to catch all exceptions if some
         * parser goes mad because we do not want to crash the whole bot.
         */
        try {
            Parser parser = clazz.newInstance();
            Parser[] pArray = parsers.toArray(new Parser[0]);
            
            for (Parser p : pArray) {
                
                if (p.getClass().getCanonicalName().equals(parser.getClass().getCanonicalName())) {
                    Log.debug("Parser " + clazz.getSimpleName() + " is already loaded. Reloading.");
                    remove(p.getClass());
                    break;
                }
            }
            
            parser.init(this);
            parsers.add(parser);
        } catch (InstantiationException e) {
            Log.warning("Parser " + clazz.getSimpleName() + " could not be loaded ("
                    + e.getClass().getSimpleName() + ")!");
            Log.info("Trying to continue without this parser.");
        } catch (IllegalAccessException e) {
            Log.warning("You are not allowed to instanciate the parser class "
                    + clazz.getCanonicalName() + ". Please check your security settings!");
            Log.info("Trying to continue without this parser.");
        } catch (Throwable e) {
            Log.warning("Could not initialize the parser " + clazz.getSimpleName() + " ("
                    + e.getClass().getSimpleName() + ").");
            Log.info("Trying to continue without this one.");
        }
    }
    
    @Override
    public TreeSet<Class<? extends Parser>> getParsers() {
        TreeSet<Class<? extends Parser>> parsers = new TreeSet<Class<? extends Parser>>(
                new Comparator<Class<? extends Parser>>() {
                    
                    @Override
                    public int compare(Class<? extends Parser> o1, Class<? extends Parser> o2) {
                        return o1.getCanonicalName().compareTo(o2.getCanonicalName());
                    }
                });
        
        for (Parser p : this.parsers) {
            parsers.add(p.getClass());
        }
        
        return parsers;
    }
    
    @Override
    public void remove(Class<? extends Parser> clazz) {
        Parser[] pArray = parsers.toArray(new Parser[0]);
        
        for (Parser p : pArray) {
            
            if (p.getClass().getCanonicalName().equals(clazz.getCanonicalName())) {
                Log.debug("Removing parser " + p.getClass().getSimpleName() + ".");
                p.destroy(this);
                parsers.remove(p);
            }
        }
    }
    
    @Override
    public Storage<Keys> getStorage() {
        return storage;
    }
    
    /**
     * Add a new connection to this bot. For each connection a separate {@link Thread} is started
     * immediately.
     * 
     * @param con
     *        The connection to add.
     */
    public void add(Connection con) {
        
        synchronized (cons) {
            cons.add(con);
            new Thread(con, con.toString()).start();
        }
    }
    
    @Override
    public Collection<Connection> getConnections() {
        return cons;
    }
    
    @Override
    public void broadcast(Message msg) {
        
        synchronized (cons) {
            
            for (Connection con : cons) {
                con.send(msg);
            }
        }
    }
    
    @Override
    public void broadcast(Message msg, String shortId) {
        
        synchronized (cons) {
            
            for (Connection con : cons) {
                
                if (!con.getShortId().equals(shortId)) {
                    con.send(msg);
                }
            }
        }
    }
    
    /**
     * Parsers can register themself via this method to be informed if a certain type of message was
     * received.
     * 
     * @param <MessageType>
     *        The type of message the parser wants to be informed about.
     * @param messageType
     *        The class of the message type the parser wants to be informed about (this is needed
     *        for implementation issues).
     * @param observer
     *        The observer instance that will be informed.
     */
    public <MessageType extends Message> void register(Class<MessageType> messageType,
            Observer<MessageType> observer) {
        
        synchronized (parserMapping) {
            Log.debug("Registered new listener for " + messageType.getSimpleName() + "s: "
                    + observer.getClass().getSimpleName());
            
            if (!parserMapping.containsKey(messageType.getCanonicalName())) {
                parserMapping.put(messageType.getCanonicalName(),
                        new LinkedList<Observer<? extends Message>>());
            }
            
            parserMapping.get(messageType.getCanonicalName()).add(observer);
        }
    }
    
    /**
     * Deregister a previously registered parser for a certain type of message. If the given parser
     * instance was not previously registered for that message type nothing happens.
     * 
     * @param <MessageType>
     *        The type of message the given parser was notified about.
     * @param messageType
     *        The class of the message type the given parser was notified about (needed for
     *        implementation issues).
     * @param observer
     *        The observer role of the parser that shall no longer be notified about messages of the
     *        given type.
     */
    public <MessageType extends Message> void deregister(Class<MessageType> messageType,
            Observer<MessageType> observer) {
        
        synchronized (parserMapping) {
            
            if (parserMapping.containsKey(messageType.getCanonicalName())) {
                parserMapping.get(messageType.getCanonicalName()).remove(observer);
            }
        }
    }
    
    /**
     * Processes a message that was received from some connection. This method just delegates the
     * message to all available parsers and lets them do the work.
     * 
     * @param msg
     *        The message to process.
     */
    /*
     * The unchecked casts here are safe because the types are actually forced to be correct when
     * registering observers (see #register).
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void process(Message msg) {
        
        synchronized (parserMapping) {
            Log.debug("Parsing a " + msg.getClass().getSimpleName() + ".");
            LinkedList<Observer<? extends Message>> parsers = parserMapping.get(msg.getClass()
                    .getCanonicalName());
            
            if (parsers == null) {
                Log.debug("There are no parsers registered.");
                return;
            } else {
                Log.debug("Registered parsers are " + parsers + ".");
            }
            
            for (Observer parser : parsers) {
                
                try {
                    parser.inform(msg);
                } catch (Throwable e) {
                    Log.error(e);
                    Log.warning("Parser " + parser.getClass().getSimpleName() + " crashed!");
                    Log.info("Continuing anyway.");
                    msg.respond(new DelayedMessage("Mumble mumble in "
                            + parser.getClass().getSimpleName() + ": "
                            + e.getClass().getSimpleName(), 2500));
                }
            }
            
            msg.conclude();
        }
    }
}
