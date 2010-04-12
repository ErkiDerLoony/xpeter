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

package erki.xpeter.parsers.statistics;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * The statistics of a session of a user.
 * 
 * @author Edgar Kalkowski
 */
public class Session implements Serializable, Cloneable {
    
    private static final long serialVersionUID = -1198846769486289585L;
    
    private int wordCount = 0;
    private int lineCount = 0;
    
    private Date start, end = null;
    
    private boolean closed = false;
    
    /**
     * Create a new {@code Session} for a user. Initializes the start date of this session with the
     * current date and time.
     */
    public Session() {
        start = new Date();
    }
    
    /**
     * Add a line of text the user this session belongs to has said. The method increases
     * {@link #getLineCount()} by one and increases {@link #getWordCount()} according to {@code
     * line.split(" ").length()}.
     * 
     * @param line
     *        The line of text the user said.
     */
    public void addLine(String line) {
        lineCount++;
        wordCount += line.split(" ").length;
    }
    
    /**
     * @return The number of words the user this session belongs to has said during this session.
     */
    public int getWordCount() {
        return wordCount;
    }
    
    /**
     * @return The number of lines the user this session belongs to has said during this session.
     */
    public int getLineCount() {
        return lineCount;
    }
    
    /** Close this session. */
    public void close() {
        closed = true;
        end = new Date();
    }
    
    /**
     * @return {@code true} if this session has been closed via {@link #close()} .<br />{@code false} if
     *         this session is still active.
     */
    public boolean isClosed() {
        return closed;
    }
    
    /**
     * @return The {@link Date} when this session was initialized. The {@code Date} is a copy of the
     *         original one to prevent manipulating.
     */
    public Date getStart() {
        return new Date(start.getTime());
    }
    
    /**
     * @return The {@link Date} this session was closed via {@link #close()} or {@code null} if this
     *         session is still active. The returned {@code Date} is a copy of the original one to
     *         prevent manipulating.
     */
    public Date getEnd() {
        
        if (end == null) {
            return null;
        } else {
            return new Date(end.getTime());
        }
    }
    
    private String toString(Date d) {
        
        if (d == null) {
            return "null";
        }
        
        String result = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        
        result += day < 10 ? "0" + day + "." : day + ".";
        result += month < 10 ? "0" + month + "." : month + ".";
        result += year + " ";
        result += hour < 10 ? "0" + hour + ":" : hour + ":";
        result += minute < 10 ? "0" + minute + ":" : minute + ":";
        result += second < 10 ? "0" + second : second;
        
        return result;
    }
    
    @Override
    public String toString() {
        
        if (end == null) {
            return "[Session: since " + toString(start) + ", " + lineCount + " lines with "
                    + wordCount + " words]";
        } else {
            return "[Session: from " + toString(start) + " to " + toString(end) + ", " + lineCount
                    + " lines with " + wordCount + " words]";
        }
    }
    
    /**
     * @return A deep copy of this {@code Session}. All member variables are also deep copied. Thus
     *         no change to the clone will affect this object.
     */
    public Object clone() {
        Session clone = null;
        
        try {
            clone = (Session) super.clone();
            clone.wordCount = wordCount;
            clone.lineCount = lineCount;
            clone.start = getStart();
            clone.end = getEnd();
            clone.closed = closed;
        } catch (CloneNotSupportedException e) {
            // This is impossible as this class implements the Cloneable
            // interface.
            throw new Error(e);
        }
        
        return clone;
    }
}
