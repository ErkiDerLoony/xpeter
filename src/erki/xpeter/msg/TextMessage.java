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

import erki.xpeter.con.Connection;

/**
 * Extends {@link Message} to represent some text that someone said on a chat. It contains the
 * actual text said and the nickname of the person who said it.
 * 
 * @author Edgar Kalkowski
 */
public class TextMessage extends Message {
    
    private String nick;
    
    /**
     * Create a new TextMessage that wraps the text someone said in a chat.
     * 
     * @param nick
     *        The nickname of the person that said something.
     * @param text
     *        The text that was said in the chat.
     * @param connection
     *        The connection over which this message was received.
     */
    public TextMessage(String nick, String text, Connection connection) {
        super(text, connection);
        this.nick = nick;
    }
    
    /**
     * Access the nickname of the person who wrote the text in a chat.
     * 
     * @return The nickname of the chatter.
     */
    public String getNick() {
        return nick;
    }
    
    @Override
    public String toString() {
        return "TextMessage(" + getNick() + ": “" + getText() + "”)";
    }
}
