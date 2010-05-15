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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.Timer;

import erki.api.util.Log;
import erki.xpeter.Bot;
import erki.xpeter.msg.NickChangeMessage;
import erki.xpeter.msg.UserJoinedMessage;
import erki.xpeter.msg.UserLeftMessage;

/**
 * This listener ist used in {@link XmppConnection} and is called if some member of the chat room
 * was granted with extra permissions or permissions were removed from someone. It is not called for
 * the bot itself (that would be {@link UserStatusListener}).
 * 
 * @author Edgar Kalkowski
 */
public class ParticipantStatusListener implements
        org.jivesoftware.smackx.muc.ParticipantStatusListener {
    
    private XmppConnection con;
    
    private Bot bot;
    
    private String lastNickChangeNewNick = "";
    
    private Collection<String> userList = new LinkedList<String>();

    private WatchDog dog;
    
    public ParticipantStatusListener(XmppConnection con, Bot bot, WatchDog dog) {
        this.con = con;
        this.bot = bot;
        this.dog = dog;
    }
    
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
        Log.debug(getNick(participant) + " was banned by " + actor + " (" + reason + ").");
        
        synchronized (userList) {
            userList.remove(getNick(participant));
        }
        
        bot.process(new UserLeftMessage(getNick(participant), "Banned: " + reason, con));
    }
    
    @Override
    public void joined(String participant) {
        dog.reset();
        
        if (getNick(participant).equals(lastNickChangeNewNick)) {
            Log.debug("Though it looks like " + getNick(participant)
                    + " joined the chat, it really was only a nick change.");
        } else {
            Log.debug(getNick(participant) + " has joined the chat.");
            
            synchronized (userList) {
                userList.add(getNick(participant));
            }
            
            bot.process(new UserJoinedMessage(getNick(participant), con));
        }
    }
    
    @Override
    public void kicked(String participant, String actor, String reason) {
        Log.debug(getNick(participant) + " was kicked by " + actor + " (" + reason + ").");
        
        synchronized (userList) {
            userList.remove(getNick(participant));
        }
        
        bot.process(new UserLeftMessage(getNick(participant), "Kicked: " + reason, con));
    }
    
    @Override
    public void left(String participant) {
        dog.reset();
        Log.debug(getNick(participant) + " left the chat.");
        
        synchronized (userList) {
            userList.remove(getNick(participant));
        }
        
        bot.process(new UserLeftMessage(getNick(participant), "", con));
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
        dog.reset();
        Log.debug(getNick(participant) + " is now known as " + newNickname + ".");
        lastNickChangeNewNick = newNickname;
        
        synchronized (userList) {
            userList.remove(getNick(participant));
            userList.add(newNickname);
        }
        
        bot.process(new NickChangeMessage(getNick(participant), newNickname, con));
        
        // wait 5 seconds for the join message the protocol sends out and discard it
        new Timer(5000, new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                lastNickChangeNewNick = "";
            }
            
        }).start();
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
    
    private String getNick(String participant) {
        String nick = participant;
        nick = nick.substring(nick.indexOf('@'));
        nick = nick.substring(nick.indexOf('/') + 1);
        return nick;
    }
    
    /**
     * Access the current list of online users of this chat. The returned Collection is copied so no
     * harm can be done editing it.
     * 
     * @return A Collection of currently online users.
     */
    public Collection<String> getUserList() {
        LinkedList<String> users = new LinkedList<String>();
        
        synchronized (userList) {
            users.addAll(userList);
        }
        
        return users;
    }
}
