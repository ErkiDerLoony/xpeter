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
 * A message that indicates that some user changed his/her nickname.
 * 
 * @author Edgar Kalkowski
 */
public class NickChangeMessage extends Message {
    
    private String newNick;
    
    /**
     * Create a new NickChangeMessage.
     * 
     * @param oldNick
     *        The old nickname of the user.
     * @param newNick
     *        The new nickname of the user.
     * @param con
     *        The connection associated with this message.
     */
    public NickChangeMessage(String oldNick, String newNick, Connection con) {
        super(oldNick, con);
        this.newNick = newNick;
    }
    
    /** @return The old nickname of the user. */
    public String getOldNick() {
        return getText();
    }
    
    /** @return The new nickname of the user. */
    public String getNewNick() {
        return newNick;
    }
}
