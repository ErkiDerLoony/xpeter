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
import java.util.Date;
import java.util.LinkedList;

/**
 * A user for who statistical information is gathered.
 * 
 * @author Edgar Kalkowski
 */
public class User implements Serializable, Cloneable, Comparable<User> {
    
    private static final long serialVersionUID = -137888309386492171L;
    
    private final String name;
    
    private LinkedList<Session> sessions;
    
    private LinkedList<String> history = new LinkedList<String>();
    
    /**
     * Create a new {@code User}. Automatically creates and starts a first {@link Session} for this
     * user as a new user is only created if that user is online.
     * 
     * @param name
     *        The nickname of the user.
     */
    public User(String name) {
        this.name = name;
        sessions = new LinkedList<Session>();
        sessions.add(new Session());
    }
    
    private Session getSession() {
        
        if (sessions.getLast().isClosed()) {
            sessions.addLast(new Session());
        }
        
        return sessions.getLast();
    }
    
    /** @return The nickname of this user. */
    public String getName() {
        return name;
    }
    
    /**
     * Add a line of text this user said to the statistics.
     * 
     * @param line
     *        The line of text this user recently said.
     */
    public void addLine(String line) {
        getSession().addLine(line);
    }
    
    /** @return The number of words this user said during all recorded sessions. */
    public long getWordCount() {
        long wordCount = 0;
        
        for (Session session : sessions) {
            wordCount += session.getWordCount();
        }
        
        return wordCount;
    }
    
    /** @return The number of lines this user said during all recorded sessinos. */
    public long getLineCount() {
        long lineCount = 0;
        
        for (Session session : sessions) {
            lineCount += session.getLineCount();
        }
        
        return lineCount;
    }
    
    /**
     * @return The {@link Date} this user was last online or {@code null} if this user is currently
     *         online.
     */
    public Date getLastOnline() {
        return sessions.getLast().getEnd();
    }
    
    /** @return The time this user has already been recorded online in ms. */
    public long getUptime() {
        long uptime = 0;
        
        for (Session session : sessions) {
            
            if (session.getEnd() == null) {
                uptime += new Date().getTime() - session.getStart().getTime();
            } else {
                uptime += session.getEnd().getTime() - session.getStart().getTime();
            }
        }
        
        return uptime;
    }
    
    /** Starts a new session for this user if no session is already active. */
    public void startSession() {
        
        if (sessions.getLast().isClosed()) {
            sessions.add(new Session());
        }
    }
    
    /** Closes the current session of this user if one is active. */
    public void closeSession() {
        
        if (!sessions.getLast().isClosed()) {
            sessions.getLast().close();
        }
    }
    
    @Override
    public int compareTo(User o) {
        return name.compareTo(o.name);
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof User) {
            User other = (User) obj;
            
            if (other.name.equals(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        return "[" + name + ": " + getLineCount() + " Zeilen, " + getWordCount() + " Wörter]";
    }
    
    /**
     * @return A deep copy of this {@code User}. All member variables are also deep copied. Thus no
     *         change to the clone will affect this object.
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        User clone = null;
        
        try {
            clone = (User) super.clone();
            clone.sessions = new LinkedList<Session>();
            
            for (Session s : sessions) {
                clone.sessions.add((Session) s.clone());
            }
            
            clone.history = (LinkedList<String>) history.clone();
            
        } catch (CloneNotSupportedException e) {
            // This is not possible as this class implements the Cloneable
            // interface.
            throw new Error(e);
        }
        
        return clone;
    }
}
