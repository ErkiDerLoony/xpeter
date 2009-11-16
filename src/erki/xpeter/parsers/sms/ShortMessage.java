/*
 * © Copyright 2008–2009 by Edgar Kalkowski (eMail@edgar-kalkowski.de)
 * 
 * This file is part of the chatbot ABCPeter.
 * 
 * The chatbot ABCPeter is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package erki.xpeter.parsers.sms;

/**
 * A container for a short message and its sender.
 * 
 * @author Edgar Kalkowski
 */
public class ShortMessage {
    
    private String msg;
    
    private String sender;
    
    public ShortMessage(String msg, String sender) {
        this.msg = msg;
        this.sender = sender;
    }
    
    public String getMessage() {
        return msg;
    }
    
    public String getSender() {
        return sender;
    }
}
