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
 * This class wraps a raw line of text that shall be sent to the server as is.
 * 
 * @author Edgar Kalkowski
 */
public class RawMessage extends Message {
    
    /**
     * Create a new RawMessage as a response that shall be sent to the server.
     * 
     * @param text
     *        The text to send to the server (unchainged).
     */
    public RawMessage(String text) {
        super(text);
    }
    
    /**
     * Create a new RawMessage that contains raw text received from the server.
     * 
     * @param text
     *        The text that was received.
     * @param connection
     *        The connection the text was received over (necessary for any responses of the bot to
     *        this message).
     */
    public RawMessage(String text, Connection connection) {
        super(text, connection);
    }
}
