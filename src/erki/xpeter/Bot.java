/*
 * © Copyright 2008–2009 by Edgar Kalkowski (eMail@edgar-kalkowski.de)
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
import java.util.TreeMap;

import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.con.Connection;
import erki.xpeter.msg.Message;
import erki.xpeter.parsers.Parser;

/**
 * This class connects the different connections the bot handles. If a new connection is added (via
 * {@link #add(Connection)}) the new connection is immediately started. This class also contains all
 * the parsers and {@link #process(Message)} is called by the connections to delegate incoming
 * messages to all available parsers.
 * 
 * @author Edgar Kalkowski
 */
public class Bot {
    
    private Collection<Connection> cons = new LinkedList<Connection>();
    
    private TreeMap<Class<? extends Message>, LinkedList<Observer<? extends Message>>> parserMapping = new TreeMap<Class<? extends Message>, LinkedList<Observer<? extends Message>>>(
            new Comparator<Class<? extends Message>>() {
                
                @Override
                public int compare(Class<? extends Message> o1, Class<? extends Message> o2) {
                    return o1.getClass().getCanonicalName().compareTo(
                            o2.getClass().getCanonicalName());
                }
            });
    
    public Bot(Iterable<Class<? extends Parser>> parsers) {
        
        // Try to instanicate all the parser classes.
        for (Class<? extends Parser> clazz : parsers) {
            
            try {
                Parser parser = clazz.newInstance();
                parser.init(this);
            } catch (InstantiationException e) {
                Log.error(e);
                Log.warning("Parser " + clazz.getCanonicalName() + " could not be loaded!");
                Log.info("Trying to continue without this parser.");
            } catch (IllegalAccessException e) {
                Log.error(e);
                Log.warning("You are not allowed to instanciate the parser class "
                        + clazz.getCanonicalName() + ". Please check your security settings!");
                Log.info("Trying to continue without this parser.");
            } catch (Throwable e) {
                Log.error(e);
                Log.warning("Could not initialize the parser " + clazz.getCanonicalName() + ".");
                Log.info("Trying to continue without this one.");
            }
        }
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
    
    /**
     * Broadcast a message to all connections currently available to this bot.
     * 
     * @param msg
     *        The message to broadcast.
     */
    public void broadcast(Message msg) {
        
        synchronized (cons) {
            
            for (Connection con : cons) {
                con.send(msg);
            }
        }
    }
    
    /**
     * Broadcast a message to all connection currently available to this bot with the exception of
     * {@code con}.
     * 
     * @param msg
     *        The message to broadcast.
     * @param con
     *        The Connection instance that will not receive {@code msg}. The connections are
     *        compared using the “==” operator.
     */
    public void broadcast(Message msg, Connection con) {
        
        synchronized (cons) {
            
            for (Connection conn : cons) {
                
                if (conn != con) {
                    conn.send(msg);
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
        
        if (!parserMapping.containsKey(messageType)) {
            parserMapping.put(messageType, new LinkedList<Observer<? extends Message>>());
        }
        
        parserMapping.get(messageType).add(observer);
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
    @SuppressWarnings("unchecked")
    public <MessageType> void process(MessageType msg) {
        LinkedList<Observer<? extends Message>> parsers = parserMapping.get(msg.getClass());
        
        for (Observer parser : parsers) {
            parser.inform(msg);
        }
    }
}
