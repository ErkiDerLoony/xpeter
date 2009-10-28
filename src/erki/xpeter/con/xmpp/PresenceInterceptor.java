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

import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.packet.Packet;

import erki.api.util.Log;

/**
 * This listener is used in {@link XmppConnection} and is called whenever some presence that shall
 * be sent to the server must be intercepted (why ever one would need this functionality?!).
 * 
 * @author Edgar Kalkowski
 */
public class PresenceInterceptor implements PacketInterceptor {
    
    @Override
    public void interceptPacket(Packet packet) {
        Log.debug("A presence was intercepted: " + packet);
    }
}
