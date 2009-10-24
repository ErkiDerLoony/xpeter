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

import java.util.LinkedList;
import java.util.Queue;

import erki.xpeter.Bot;
import erki.xpeter.Connection;
import erki.xpeter.msg.Message;

/**
 * Establishes a connection to an XMPP server. If the connection is lost it tries to reconnect from
 * time to time. Messages that are to be sent to the server are buffered in case the connection is
 * lost and sent if the connection is re-established.
 * 
 * @author Edgar Kalkowski
 */
public class XmppConnection implements Connection {
    
    private Object sendLock = new Object();
    
    private Queue<Message> sendQueue = new LinkedList<Message>();
    
    private String host, channel;
    
    private int port;

    private Bot bot;
    
    public XmppConnection(String host, int port, String channel, String nickname) {
        this.host = host;
        this.port = port;
        this.channel = channel;
    }
    
    @Override
    public void run() {
    }

    @Override
    public void setBot(Bot bot) {
        this.bot = bot;
    }
}
