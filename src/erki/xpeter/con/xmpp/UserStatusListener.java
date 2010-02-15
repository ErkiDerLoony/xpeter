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
 * This listener is used in {@link XmppConnection} and called every time when someone grants the bot
 * extra permissions or removes them. Most importantly it is used to detect if the bot was kicked or
 * banned from the chat and trigger a reconnect.
 * 
 * @author Edgar Kalkowski
 */
public class UserStatusListener implements org.jivesoftware.smackx.muc.UserStatusListener {
    
    private XmppConnection con;
    
    public UserStatusListener(XmppConnection con) {
        this.con = con;
    }
    
    @Override
    public void adminGranted() {
        Log.debug("I was given admin permissions.");
    }
    
    @Override
    public void adminRevoked() {
        Log.debug("My admin permissions were removed.");
    }
    
    @Override
    public void banned(String actor, String reason) {
        Log.debug("I was banned from the chat by " + actor + " (" + reason + ").");
        con.reconnect();
    }
    
    @Override
    public void kicked(String actor, String reason) {
        Log.debug("I was kicked from the chat by " + actor + " (" + reason + "). Reconnecting.");
        con.reconnect();
    }
    
    @Override
    public void membershipGranted() {
        Log.debug("I became a member of the chat.");
    }
    
    @Override
    public void membershipRevoked() {
        Log.debug("My membership of the chat was revoked.");
    }
    
    @Override
    public void moderatorGranted() {
        Log.debug("I am now a moderator of the chat.");
    }
    
    @Override
    public void moderatorRevoked() {
        Log.debug("I am no longer a moderator of the chat.");
    }
    
    @Override
    public void ownershipGranted() {
        Log.debug("I am now an owner of the chat.");
    }
    
    @Override
    public void ownershipRevoked() {
        Log.debug("I am no longer an owner of the chat.");
    }
    
    @Override
    public void voiceGranted() {
        Log.debug("I was granted voice permissions.");
    }
    
    @Override
    public void voiceRevoked() {
        Log.debug("My voice permissions were revoked.");
    }
}
