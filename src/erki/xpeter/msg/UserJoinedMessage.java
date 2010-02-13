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
 * This message indicates that some user joined the chat.
 * 
 * @author Edgar Kalkowski
 */
public class UserJoinedMessage extends Message {
    
    /**
     * Create a new UserJoinedMessage.
     * 
     * @param nick
     *        The nickname of the user who joined the chat.
     * @param con
     *        The connection this message belongs to.
     */
    public UserJoinedMessage(String nick, Connection con) {
        super(nick, con);
    }
    
    /**
     * Access the nickname of the user who joined the chat.
     * 
     * @return The nickname of the user who joined the chat.
     */
    public String getNick() {
        return getText();
    }
}
