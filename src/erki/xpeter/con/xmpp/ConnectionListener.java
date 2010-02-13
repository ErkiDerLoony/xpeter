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

package erki.xpeter.con.xmpp;

import erki.api.util.Log;

/**
 * The main purpose of this class so far is to detect if the connection is terminated because of
 * some error and trigger a reconnect if that happens.
 * 
 * @author Edgar Kalkowski
 */
public class ConnectionListener implements org.jivesoftware.smack.ConnectionListener {
    
    private XmppConnection con;
    
    public ConnectionListener(XmppConnection con) {
        this.con = con;
    }
    
    @Override
    public void connectionClosed() {
        Log.debug("The connection was closed.");
        con.reconnect();
    }
    
    @Override
    public void connectionClosedOnError(Exception e) {
        Log.debug("The connection was closed due to some error (" + e.getClass().getSimpleName()
                + ").");
        con.reconnect();
    }
    
    @Override
    public void reconnectingIn(int seconds) {
        Log.debug("Reconnecting in " + seconds + " s.");
    }
    
    @Override
    public void reconnectionFailed(Exception e) {
        Log.debug("Reconnection failed (" + e.getClass().getSimpleName() + ").");
        con.reconnect();
    }
    
    @Override
    public void reconnectionSuccessful() {
        Log.debug("Reconnection was successful.");
    }
}
