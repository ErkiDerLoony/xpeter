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

package erki.xpeter.con;

import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;

/**
 * Interface implemented by all connection supported by xpeter. Every instance of Connection shall
 * connect to a single chat (so multiple connections are required if xpeter shall join multiple
 * channels on one server) and see to it that the connection is re-established if it breaks. In case
 * the connection is not ready to send outgoing messages at one point those messages shall be
 * buffered until the connection becomes ready again.
 * 
 * @author Edgar Kalkowski
 */
public interface Connection extends Runnable {
    
    /**
     * Send some message over this connection. If the connection is not ready to send messages at
     * one point the messages will be buffered and sent when the connection becomes ready again.
     * 
     * @param msg
     *        The message to send.
     */
    public void send(Message msg);
    
    /**
     * Send a simple text to the chat. This is a convenience method that saves the wrapping of the
     * string in a {@link Message} or {@link TextMessage} object.
     * 
     * @param msg
     *        The text to send to the chat.
     */
    public void send(String msg);
    
    /**
     * Access the nickname the bot uses with this connection.
     * 
     * @return The nickname of the bot.
     */
    public String getNick();
}
