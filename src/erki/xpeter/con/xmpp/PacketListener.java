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

import org.jivesoftware.smack.packet.Packet;

import erki.api.util.Log;
import erki.xpeter.Bot;
import erki.xpeter.msg.TextMessage;

/**
 * This listener is used in {@link XmppConnection} and is called if some packet was received. If the
 * packet was in fact a message to the chat room it is parsed into the bot’s internal message format
 * and handed over to the parsers.
 */
public class PacketListener implements org.jivesoftware.smack.PacketListener {
    
    private XmppConnection con;
    
    private Bot bot;
    
    public PacketListener(XmppConnection con, Bot bot) {
        this.con = con;
        this.bot = bot;
    }
    
    @Override
    public void processPacket(Packet packet) {
        
        if (packet instanceof org.jivesoftware.smack.packet.Message) {
            org.jivesoftware.smack.packet.Message message = (org.jivesoftware.smack.packet.Message) packet;
            String from = message.getFrom().substring(message.getFrom().lastIndexOf('/') + 1);
            TextMessage msg = new TextMessage(from, message.getBody(), con);
            Log.info("Received " + msg + ".");
            bot.process(msg);
        } else {
            Log.info("Unknown packet received: " + packet);
        }
    }
}
