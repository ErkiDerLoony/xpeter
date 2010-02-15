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

package erki.xpeter.msg;

import java.util.LinkedList;

import erki.api.util.Log;
import erki.xpeter.Bot;
import erki.xpeter.con.Connection;

/**
 * Superclass for all messages that can be processed by xpeter. It assumes that every message at
 * least contains some text. The text that this class contains is immutable. Just create a new
 * instance if needed. This class also provides an easy way to respond to messages.
 * <p>
 * Known subclasses: {@link TextMessage}, {@link DelayedMessage}, {@link RawMessage},
 * {@link NickChangeMessage}, {@link UserJoinedMessage}, {@link UserLeftMessage}
 * 
 * @author Edgar Kalkowski
 */
public class Message {
    
    protected Connection connection;
    
    protected String text;
    
    protected Message defaultResponse = null;
    
    protected LinkedList<Message> responses = new LinkedList<Message>();
    
    /**
     * Create a new Message object.
     * 
     * @param text
     *        The text of the new message.
     * @param connection
     *        The connection over which the text was received.
     */
    public Message(String text, Connection connection) {
        this.connection = connection;
        this.text = text;
    }
    
    /**
     * Create a new Message without an associated {@link Connection}. This can be useful if creating
     * response messages in some parser but it may also be harmful if a connection accidentally
     * creates messages using this constructor that are later processed by parsers that rely on the
     * connection information.
     * 
     * @param text
     *        The text of the new message.
     */
    public Message(String text) {
        this(text, null);
    }
    
    /**
     * Access the wrapped text of the message.
     * 
     * @return The text of the message.
     */
    public String getText() {
        return text;
    }
    
    /**
     * Access the nickname that the bot itself uses for the connection this message belongs to. This
     * call is directly forwarded to the {@link Connection} that is associated to this message.
     * 
     * @return The bot’s nickname or {@code null} if it is unknown to which connection this message
     *         belongs.
     */
    public String getBotNick() {
        return connection != null ? connection.getNick() : null;
    }
    
    /**
     * Access a short identifier for the connection that is associated to this message. See
     * {@link Connection#getShortId()} for details.
     * 
     * @return A short identifier for this message’s connection or {@code null} if there is no
     *         connection associated to this message.
     */
    public String getShortId() {
        return connection != null ? connection.getShortId() : null;
    }
    
    /**
     * Set a default response that shall be sent if no parser finds a suitable response.
     * 
     * @param defaultResponse
     *        The default response to this message or {@code null} if no such message shall be sent.
     */
    public void setDefaultResponse(Message defaultResponse) {
        this.defaultResponse = defaultResponse;
    }
    
    /**
     * Add a response to this message to the list of responses. All response messages are
     * accumulated and sent after all parsers have finished processing this message. If no parser
     * finds a suitable response the default response is sent if one was set (see
     * {@link #setDefaultResponse(String)}).
     * 
     * @param msg
     *        The message that will be sent as a response to this message.
     */
    public void respond(Message msg) {
        responses.add(msg);
    }
    
    /**
     * This method is called by {@link Bot} after all parsers had a chance to compute their
     * responses and add them to this messages send queue. <i>It must not be called by any
     * parser!</i> After this method was called subsequent calls to {@link #respond(Message)} will
     * have no effect!
     */
    public final void conclude() {
        
        if (!responses.isEmpty()) {
            
            for (Message msg : responses) {
                
                if (connection != null) {
                    connection.send(msg);
                } else {
                    Log.warning("Someone delivered a foul message! Refusing to answer to it!");
                }
            }
            
        } else {
            
            if (defaultResponse != null) {
                
                if (connection != null) {
                    connection.send(defaultResponse);
                } else {
                    Log.warning("Someone delivered a foul message! Refusing to answer to it!");
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(“" + text + "”)";
    }
}
