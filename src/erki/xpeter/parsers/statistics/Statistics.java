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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;

import erki.api.storage.Storage;
import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.NickChangeMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.msg.UserJoinedMessage;
import erki.xpeter.msg.UserLeftMessage;
import erki.xpeter.parsers.SuperParser;
import erki.xpeter.parsers.statistics.actions.History;
import erki.xpeter.parsers.statistics.actions.LastSaid;
import erki.xpeter.parsers.statistics.actions.LastSeen;
import erki.xpeter.parsers.statistics.actions.TopLines;
import erki.xpeter.parsers.statistics.actions.TopQuotient;
import erki.xpeter.parsers.statistics.actions.TopUptime;
import erki.xpeter.parsers.statistics.actions.TopWords;
import erki.xpeter.parsers.statistics.actions.UserLines;
import erki.xpeter.parsers.statistics.actions.UserQuotient;
import erki.xpeter.parsers.statistics.actions.UserUptime;
import erki.xpeter.parsers.statistics.actions.UserWords;
import erki.xpeter.parsers.statistics.actions.Who;
import erki.xpeter.util.Keys;
import erki.xpeter.util.StorageKey;

/**
 * This parser gathers statistical information about the participants of the chats the bot has
 * joined and provides a way for the users to query them.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class Statistics extends SuperParser implements Observer<TextMessage> {
    
    private static final StorageKey<TreeMap<String, User>> storageKey = new StorageKey<TreeMap<String, User>>(
            Keys.STATISTICS);
    
    private Storage<Keys> storage;
    
    private TreeMap<String, User> users;
    
    private Observer<UserJoinedMessage> userJoinedObserver;
    
    private Observer<UserLeftMessage> userLeftObserver;
    
    private Observer<NickChangeMessage> nickChangeObserver;
    
    @Override
    public void createActions() {
        
        if (users == null) {
            users = new TreeMap<String, User>();
        }
        
        actions.add(new History(users));
        actions.add(new LastSaid(users));
        actions.add(new LastSeen(users));
        actions.add(new TopLines(users));
        actions.add(new TopQuotient(users));
        actions.add(new TopUptime(users));
        actions.add(new TopWords(users));
        actions.add(new UserLines(users));
        actions.add(new UserQuotient(users));
        actions.add(new UserUptime(users));
        actions.add(new UserWords(users));
        actions.add(new Who(users));
    }
    
    @Override
    public String getDescription() {
        return "Dieser Parser sammelt statistische Informationen.";
    }
    
    @Override
    public void init(Bot bot) {
        storage = bot.getStorage();
        
        if (storage.contains(storageKey)) {
            users.putAll(storage.get(storageKey));
        }
        
        // First initialize all actions.
        super.init(bot);
        
        // Secondly add some parsers that are not exposed to the user.
        bot.register(TextMessage.class, this);
        
        // Start a new session for a joining user.
        userJoinedObserver = new Observer<UserJoinedMessage>() {
            
            @Override
            public void inform(UserJoinedMessage message) {
                
                if (message.getNick().equals(message.getBotNick())) {
                    return;
                }
                
                synchronized (users) {
                    
                    if (users.keySet().contains(message.getNick())) {
                        users.get(message.getNick()).startSession();
                    } else {
                        users.put(message.getNick(), new User(message.getNick()));
                    }
                }
                
                Log.debug("Started statistics session for " + message.getNick() + ".");
            }
        };
        
        bot.register(UserJoinedMessage.class, userJoinedObserver);
        
        // Close session for leaving user.
        userLeftObserver = new Observer<UserLeftMessage>() {
            
            @Override
            public void inform(UserLeftMessage message) {
                
                if (message.getNick().equals(message.getBotNick())) {
                    return;
                }
                
                synchronized (users) {
                    
                    if (users.keySet().contains(message.getNick())) {
                        users.get(message.getNick()).closeSession();
                        Log.debug("Closed statistics session for " + message.getNick() + ".");
                    } else {
                        Log.warning("User " + message.getNick()
                                + " has left, but had no active statistics session!");
                    }
                }
            }
        };
        
        bot.register(UserLeftMessage.class, userLeftObserver);
        
        // Close session for old nick and start session for new nick.
        nickChangeObserver = new Observer<NickChangeMessage>() {
            
            @Override
            public void inform(NickChangeMessage message) {
                
                synchronized (users) {
                    
                    if (users.keySet().contains(message.getOldNick())) {
                        users.get(message.getOldNick()).closeSession();
                        Log.debug("Closed statistics session for " + message.getOldNick() + ".");
                    } else {
                        Log.warning("User " + message.getOldNick() + " is now known as "
                                + message.getNewNick() + " but had no active statistics session!");
                    }
                    
                    if (users.keySet().contains(message.getNewNick())) {
                        users.get(message.getNewNick()).startSession();
                    } else {
                        users.put(message.getNewNick(), new User(message.getNewNick()));
                    }
                }
                
                Log.debug("Started new statistics session for " + message.getNewNick() + ".");
            }
        };
        
        bot.register(NickChangeMessage.class, nickChangeObserver);
    }
    
    @Override
    public void destroy(Bot bot) {
        super.destroy(bot);
        bot.deregister(TextMessage.class, this);
        bot.deregister(UserJoinedMessage.class, userJoinedObserver);
        bot.deregister(UserLeftMessage.class, userLeftObserver);
        bot.deregister(NickChangeMessage.class, nickChangeObserver);
    }
    
    @Override
    public void inform(TextMessage message) {
        
        synchronized (users) {
            
            if (!users.containsKey(message.getNick())) {
                users.put(message.getNick(), new User(message.getNick()));
                Log.warning(message.getNick() + " said something, but "
                        + "has no active statistics session!");
            }
            
            // Make sure a session is started for the user who said something.
            users.get(message.getNick()).startSession();
            
            // Count statistics for the user who said something.
            users.get(message.getNick()).addLine(message.getText());
            
            TreeMap<String, User> users = new TreeMap<String, User>();
            
            synchronized (this.users) {
                
                // Close the sessions of all users before saving.
                for (String user : this.users.keySet()) {
                    User clone = (User) this.users.get(user).clone();
                    clone.closeSession();
                    users.put(user, clone);
                }
            }
            
            storage.add(storageKey, users);
        }
    }
    
    /**
     * Format an instance of {@link Date} like “DD.MM.YYYY”.
     * 
     * @param date
     *        The Date instance to format.
     * @return The given date formatted like “DD.MM.YYYY”.
     */
    public static String formatDate(Date date) {
        String result = "am ";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        result += day < 10 ? "0" + day + "." : day + ".";
        result += month < 10 ? "0" + month + "." : month + ".";
        
        if (today(calendar)) {
            return "heute";
        }
        
        if (yesterday(calendar)) {
            return "gestern";
        }
        
        if (daybeforeyesterday(calendar)) {
            return "vorgestern";
        }
        
        if (weekday(calendar)) {
            return "letzten "
                    + calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.GERMAN);
        }
        
        return result + calendar.get(Calendar.YEAR);
    }
    
    private static boolean today(Calendar calendar) {
        Calendar today = Calendar.getInstance();
        
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return true;
        } else {
            return false;
        }
    }
    
    private static boolean yesterday(Calendar calendar) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        
        if (calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
                && calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR)) {
            return true;
        } else {
            return false;
        }
    }
    
    private static boolean daybeforeyesterday(Calendar calendar) {
        Calendar bound = Calendar.getInstance();
        bound.add(Calendar.DAY_OF_YEAR, -2);
        
        if (calendar.get(Calendar.DAY_OF_YEAR) == bound.get(Calendar.DAY_OF_YEAR)
                && calendar.get(Calendar.YEAR) == bound.get(Calendar.YEAR)) {
            return true;
        } else {
            return false;
        }
    }
    
    private static boolean weekday(Calendar calendar) {
        Calendar bound = Calendar.getInstance();
        bound.add(Calendar.DAY_OF_YEAR, -5);
        
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        
        if (calendar.after(bound) && calendar.before(now)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Format an instance of {@link Date} like “HH:MM:SS”.
     * 
     * @param date
     *        The Date instance to format.
     * @return The given date formatted like “HH:MM:SS”.
     */
    public static String formatTime(Date date) {
        String result = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        result += hour < 10 ? "0" + hour + ":" : hour + ":";
        result += min < 10 ? "0" + min + ":" : min + ":";
        result += sec < 10 ? "0" + sec : sec;
        return result;
    }
    
    /**
     * Format a time difference like “Dd HH:MM:SS”.
     * 
     * @param time
     *        The time difference to format (given in ms).
     * @return The given time difference formatted like “Dd HH:MM:SS”.
     */
    public static String formatTime(long time) {
        String result = "";
        long sec = time / 1000;
        long min = sec / 60;
        sec %= 60;
        long hour = min / 60;
        min %= 60;
        long day = hour / 24;
        hour %= 24;
        result += day + "d ";
        result += hour < 10 ? "0" + hour + ":" : hour + ":";
        result += min < 10 ? "0" + min + ":" : min + ":";
        result += sec < 10 ? "0" + sec : sec;
        return result;
    }
}
