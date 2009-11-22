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

import java.io.Serializable;

import erki.xpeter.util.BotApi;

/**
 * A container for a short message and its sender.
 * 
 * @author Edgar Kalkowski
 */
public class ShortMessage implements Serializable {
    
    private static final long serialVersionUID = 1974654312661296133L;
    
    private String msg;
    
    private String sender;
    
    private long time;
    
    /**
     * Create a new ShortMessage.
     * 
     * @param msg
     *        The actual text of the message.
     * @param sender
     *        The nickname of the sender of the message.
     */
    public ShortMessage(String msg, String sender) {
        this.msg = msg;
        this.sender = sender;
        time = System.currentTimeMillis();
    }
    
    /**
     * Access the text of this message.
     * 
     * @return The text of this message.
     */
    public String getMessage() {
        return msg;
    }
    
    /**
     * Access the sender of this message.
     * 
     * @return The nickname of the sender of this message.
     */
    public String getSender() {
        return sender;
    }
    
    private static String day(long days) {
        
        if (days == 1) {
            return "einem Tag";
        } else {
            return BotApi.number(days) + " Tagen";
        }
    }
    
    private static String hour(long hours) {
        
        if (hours == 1) {
            return "einer Stunde";
        } else {
            return BotApi.number(hours) + " Stunden";
        }
    }
    
    private static String min(long mins) {
        
        if (mins == 1) {
            return "einer Minute";
        } else {
            return BotApi.number(mins) + " Minuten";
        }
    }
    
    private static String sec(long secs) {
        
        if (secs == 1) {
            return "einer Sekunde";
        } else {
            return BotApi.number(secs) + " Sekunden";
        }
    }
    
    /**
     * Describes the time that elapsed since this message was created.
     * 
     * @return A string representation of the time that elapsed since this message was created.
     */
    public String getDate() {
        long diff = System.currentTimeMillis() - time;
        long secs = diff / 1000;
        long mins = secs / 60;
        secs %= 60;
        long hours = mins / 60;
        mins %= 60;
        long days = hours / 24;
        hours %= 24;
        
        if (days == 0 && mins == 0 && hours == 0) {
            return sec(secs);
        } else if (days == 0 && hours == 0) {
            return min(mins) + " und " + sec(secs);
        } else if (days == 0) {
            return hour(hours) + ", " + min(mins) + " und " + sec(secs);
        } else {
            return day(days) + ", " + hour(hours) + " und " + min(mins);
        }
    }
}
