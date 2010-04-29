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
 * A message that indicates that a user left the chat. The text of this message is the leave message
 * of the user that left if he gave any.
 * 
 * @author Edgar Kalkowski
 */
public class UserLeftMessage extends Message {
    
    private String nick;
    
    /**
     * Create a new LeaveMessage.
     * 
     * @param nick
     *        The nickname of the user that left the chat. Must not be {@code null}!
     * @param text
     *        The leave message left by the leaving user. This may either be the empty string or
     *        {@code null} if the user did not leave a quit message.
     * @param con
     *        The connection instance this message is associated to. If this is {@code null} and
     *        some parser tries to answer to this message a {@link NullPointerException} may occurr.
     */
    public UserLeftMessage(String nick, String text, Connection con) {
        super(text, con);
        
        if (nick == null) {
            throw new NullPointerException();
        }
        
        this.nick = nick;
    }
    
    /**
     * Access the nick of the user that left the chat.
     * 
     * @return The nickname of the user that left the chat.
     */
    public String getNick() {
        return nick;
    }
}
