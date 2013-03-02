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

package erki.xpeter.con.erkitalk;

import erki.api.util.Log;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.RawMessage;

/**
 * Encoded instances of {@link Message} (or of subclasses) to a raw string that can be sent to an
 * ErkiTalk server.
 * 
 * @author Edgar Kalkowski
 */
public class MessageEncoder {
    
    private String encoded;
    
    /**
     * Create a new MessageEncoder for a specific message.
     * 
     * @param message
     *        The message to encode.
     */
    public MessageEncoder(Message message) {
        
        if (message instanceof RawMessage) {
            encoded = message.getText();
        } else {
            
            if (message.getText().contains("\n")) {
                
                for (String line : message.getText().split("\n")) {
                    encoded = "TEXT " + line + "\n";
                }
                
                encoded = encoded.substring(0, encoded.length() - 1);
            } else {
                encoded = "TEXT " + message.getText();
            }
        }
    }
    
    /**
     * Access the encoded message.
     * 
     * @return The encoded message ready to be sent to an ErkiTalk server.
     */
    public String get() {
        Log.debug("Sending “" + encoded + "” to the server.");
        return encoded;
    }
}
