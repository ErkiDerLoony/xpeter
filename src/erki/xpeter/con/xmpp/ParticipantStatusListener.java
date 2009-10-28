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

package erki.xpeter.con.xmpp;

import erki.api.util.Log;

/**
 * This listener ist used in {@link XmppConnection} and is called if some member of the chat room
 * was granted with extra permissions or permissions were removed from someone. It is not called for
 * the bot itself (that would be {@link UserStatusListener}).
 * 
 * @author Edgar Kalkowski
 */
public class ParticipantStatusListener implements
        org.jivesoftware.smackx.muc.ParticipantStatusListener {
    
    @Override
    public void adminGranted(String participant) {
        Log.debug(participant + " was granted admin permission.");
    }
    
    @Override
    public void adminRevoked(String participant) {
        Log.debug(participant + "’s admin permissions were removed.");
    }
    
    @Override
    public void banned(String participant, String actor, String reason) {
        Log.debug(participant + " was banned by " + actor + " (" + reason + ").");
    }
    
    @Override
    public void joined(String participant) {
        Log.debug(participant + " has joined the chat.");
    }
    
    @Override
    public void kicked(String participant, String actor, String reason) {
        Log.debug(participant + " was kicked by " + actor + " (" + reason + ").");
    }
    
    @Override
    public void left(String participant) {
        Log.debug(participant + " left the chat.");
    }
    
    @Override
    public void membershipGranted(String participant) {
        Log.debug(participant + " is now a member of the room.");
    }
    
    @Override
    public void membershipRevoked(String participant) {
        Log.debug(participant + "’s membership was revoked.");
    }
    
    @Override
    public void moderatorGranted(String participant) {
        Log.debug(participant + " is now a moderator of the chat.");
    }
    
    @Override
    public void moderatorRevoked(String participant) {
        Log.debug(participant + " is no longer a moderator of the chat.");
    }
    
    @Override
    public void nicknameChanged(String participant, String newNickname) {
        Log.debug(participant + " is now known as " + newNickname + ".");
    }
    
    @Override
    public void ownershipGranted(String participant) {
        Log.debug(participant + " is now an owner of the chat.");
    }
    
    @Override
    public void ownershipRevoked(String participant) {
        Log.debug(participant + " is no longer an owner of the chat.");
    }
    
    @Override
    public void voiceGranted(String participant) {
        Log.debug(participant + " got granted voice permissions.");
    }
    
    @Override
    public void voiceRevoked(String participant) {
        Log.debug(participant + "’s void permissions were removed.");
    }
}
