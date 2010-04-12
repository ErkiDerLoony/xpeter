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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;

import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.NickChangeMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.msg.UserJoinedMessage;
import erki.xpeter.msg.UserLeftMessage;
import erki.xpeter.parsers.Parser;
import erki.xpeter.util.BotApi;

public class Statistics implements Parser, Observer<TextMessage> {
    
    private static final String STAT_FILE = "config" + System.getProperty("file.separator")
            + "stats";
    
    private TreeMap<String, User> users;
    
    private Observer<UserJoinedMessage> userJoinedObserver;
    
    private Observer<UserLeftMessage> userLeftObserver;
    
    private Observer<NickChangeMessage> nickChangeObserver;
    
    private boolean killed = false;
    
    private Thread saveThread;
    
    @Override
    public void init(Bot bot) {
        loadStats();
        bot.register(TextMessage.class, this);
        
        // Start a new session for a joining user.
        userJoinedObserver = new Observer<UserJoinedMessage>() {
            
            @Override
            public void inform(UserJoinedMessage message) {
                
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
        
        // Start thread that saves the statistical information to the STAT_FILE
        // from time to time in case the bot crashes or is terminated.
        saveThread = new Thread() {
            
            @Override
            public void run() {
                super.run();
                
                while (!killed) {
                    
                    try {
                        Thread.sleep(300000);
                    } catch (InterruptedException e) {
                    }
                    
                    // checkUserList();
                    saveStats();
                }
            }
            
        };
    }
    
    @Override
    public void destroy(Bot bot) {
        saveThread.interrupt();
        bot.deregister(TextMessage.class, this);
        bot.deregister(UserJoinedMessage.class, userJoinedObserver);
        bot.deregister(UserLeftMessage.class, userLeftObserver);
        bot.deregister(NickChangeMessage.class, nickChangeObserver);
    }
    
    // TODO: Perhaps this can be done some other way with the new framework, too?
    
    /*
     * private void checkUserList() { Collection<String> userList = bot.getUserList();
     * 
     * synchronized (users) {
     * 
     * for (String user : userList) {
     * 
     * if (users.containsKey(user)) { users.get(user).startSession(); } else { users.put(user, new
     * User(user)); } }
     * 
     * for (String user : users.keySet()) {
     * 
     * if (!userList.contains(user)) { users.get(user).closeSession(); } } } }
     */

    @SuppressWarnings("unchecked")
    private void loadStats() {
        
        try {
            ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(STAT_FILE));
            users = (TreeMap<String, User>) objectIn.readObject();
            objectIn.close();
            Log.info("Loaded statistical information from " + STAT_FILE + ".");
            Log.debug("Statistical information: " + users);
        } catch (FileNotFoundException e) {
            Log.info("Could not find any stored statistical information.");
            users = new TreeMap<String, User>();
        } catch (IOException e) {
            Log.error(e);
            Log.warning("Could not load file containing statistical information!");
            users = new TreeMap<String, User>();
        } catch (ClassNotFoundException e) {
            Log.error(e);
            Log.warning("Could not load statistical information!");
            users = new TreeMap<String, User>();
        }
    }
    
    private void saveStats() {
        TreeMap<String, User> users = new TreeMap<String, User>();
        
        synchronized (this.users) {
            
            // Close the sessions of all users before saving.
            for (String user : this.users.keySet()) {
                User clone = (User) this.users.get(user).clone();
                clone.closeSession();
                users.put(user, clone);
            }
        }
        
        try {
            ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(STAT_FILE));
            objectOut.writeObject(users);
            objectOut.close();
            // Only use debug here as otherwise the log is flooded by this line
            // because this method is called every some minutes in case the bot
            // crashes or is terminated.
            Log.debug("Statistical information stored to " + STAT_FILE + ".");
        } catch (FileNotFoundException e) {
            Log.error(e);
            Log.warning("Could not store statistical information to " + STAT_FILE + ".");
        } catch (IOException e) {
            Log.error(e);
            Log.warning("Could not store statistical information to " + STAT_FILE + ".");
        }
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
            
            String text = message.getText();
            
            if (BotApi.addresses(text, message.getBotNick())) {
                text = BotApi.trimNick(text, message.getBotNick());
            } else {
                return;
            }
            
            String topLines = "[tT]op ?(1?\\d) ?([lL]ines|[zZ]eilen)?[!\\.\\?]?";
            
            if (text.matches(topLines)) {
                int count;
                
                try {
                    count = Integer.parseInt(text.replaceAll(topLines, "$1"));
                    TreeMap<Long, TreeSet<User>> users = new TreeMap<Long, TreeSet<User>>();
                    
                    for (String user : this.users.keySet()) {
                        
                        if (this.users.get(user).getLineCount() > 0) {
                            
                            if (users.containsKey(this.users.get(user).getLineCount())) {
                                users.get(this.users.get(user).getLineCount()).add(
                                        this.users.get(user));
                            } else {
                                TreeSet<User> list = new TreeSet<User>();
                                list.add(this.users.get(user));
                                users.put(this.users.get(user).getLineCount(), list);
                            }
                        }
                    }
                    
                    int size = users.size();
                    String result = "";
                    
                    for (int i = 0; i < count && !users.isEmpty(); i++) {
                        User[] list = users.get(users.lastKey()).toArray(new User[0]);
                        String number = "", line = "";
                        
                        if ((i + 1) < 10 && count > 9 && size > 9) {
                            number += "0" + (i + 1);
                        } else {
                            number += (i + 1);
                        }
                        
                        for (int j = 0; j < list.length; j++) {
                            
                            if (j < list.length - 2) {
                                line += list[j].getName() + ", ";
                            } else if (j < list.length - 1) {
                                line += list[j].getName() + " und ";
                            } else {
                                line += list[j].getName();
                            }
                        }
                        
                        if (list[0].getLineCount() == 1) {
                            result += number + ". " + line + " mit einer Zeile.\n";
                        } else {
                            result += number + ". " + line + " mit " + users.lastKey()
                                    + " Zeilen.\n";
                        }
                        
                        users.remove(users.lastKey());
                    }
                    
                    // Be failsafe here.
                    if (result.length() > 0 && result.charAt(result.length() - 1) == '\n') {
                        result = result.substring(0, result.length() - 1);
                    }
                    
                    message.respond(new Message(result));
                } catch (NumberFormatException e) {
                    message.respond(new Message("Ich habe deine Zahl für die Zeilen ("
                            + text.replaceAll(topLines, "$1") + ") nicht " + "verstanden."));
                }
            }
            
            String topWords = "[tT]op ?(1?\\d) ?([wW]ords|[wW](ö|oe)rter)[!\\.\\?]?";
            
            if (text.matches(topWords)) {
                int count;
                
                try {
                    count = Integer.parseInt(text.replaceAll(topWords, "$1"));
                    TreeMap<Long, TreeSet<User>> users = new TreeMap<Long, TreeSet<User>>();
                    
                    for (String user : this.users.keySet()) {
                        
                        if (this.users.get(user).getWordCount() > 0) {
                            
                            if (users.containsKey(this.users.get(user).getWordCount())) {
                                TreeSet<User> list = users.get(this.users.get(user).getWordCount());
                                list.add(this.users.get(user));
                            } else {
                                TreeSet<User> list = new TreeSet<User>();
                                list.add(this.users.get(user));
                                users.put(this.users.get(user).getWordCount(), list);
                            }
                        }
                    }
                    
                    int size = users.size();
                    String result = "";
                    
                    for (int i = 0; i < count && !users.isEmpty(); i++) {
                        User[] list = users.get(users.lastKey()).toArray(new User[0]);
                        String number = "", line = "";
                        
                        if ((i + 1) < 10 && count > 9 && size > 9) {
                            number += "0" + (i + 1);
                        } else {
                            number += (i + 1);
                        }
                        
                        for (int j = 0; j < list.length; j++) {
                            
                            if (j < list.length - 2) {
                                line += list[j].getName() + ", ";
                            } else if (j < list.length - 1) {
                                line += list[j].getName() + " und ";
                            } else {
                                line += list[j].getName();
                            }
                        }
                        
                        if (list[0].getWordCount() == 1) {
                            result += number + ". " + line + " mit einem Wort.\n";
                            
                        } else {
                            result += number + ". " + line + " mit " + users.lastKey()
                                    + " Wörtern.\n";
                        }
                        
                        users.remove(users.lastKey());
                    }
                    
                    // Be failsafe here.
                    if (result.length() > 0 && result.charAt(result.length() - 1) == '\n') {
                        result = result.substring(0, result.length() - 1);
                    }
                    
                    message.respond(new Message(result));
                } catch (NumberFormatException e) {
                    message.respond(new Message("Ich habe deine Zahl für die Wörter ("
                            + text.replaceAll(topLines, "$1") + ") nicht " + "verstanden."));
                }
            }
            
            String topQuotient = "[tT]op ?(1?\\d) ?"
                    + "([qQ]uotient|[wW](ö|oe)rter pro [zZ]eile|[pP]ro|"
                    + "[wW][pP][Zz])[!\\.\\?]?";
            
            if (text.matches(topQuotient)) {
                int count;
                
                try {
                    count = Integer.parseInt(text.replaceAll(topQuotient, "$1"));
                    TreeMap<Double, TreeSet<User>> users = new TreeMap<Double, TreeSet<User>>();
                    
                    for (String user : this.users.keySet()) {
                        
                        if (this.users.get(user).getLineCount() > 0) {
                            
                            if (users.containsKey(this.users.get(user).getWordCount()
                                    / (double) this.users.get(user).getLineCount())) {
                                TreeSet<User> list = users.get(this.users.get(user).getWordCount()
                                        / (double) this.users.get(user).getLineCount());
                                list.add(this.users.get(user));
                            } else {
                                TreeSet<User> list = new TreeSet<User>();
                                list.add(this.users.get(user));
                                users.put(this.users.get(user).getWordCount()
                                        / (double) this.users.get(user).getLineCount(), list);
                            }
                        }
                    }
                    
                    int size = users.size();
                    String result = "";
                    
                    for (int i = 0; i < count && !users.isEmpty(); i++) {
                        User[] list = users.get(users.lastKey()).toArray(new User[0]);
                        String number = "", line = "";
                        
                        if ((i + 1) < 10 && count > 9 && size > 9) {
                            number += "0" + (i + 1);
                        } else {
                            number += (i + 1);
                        }
                        
                        NumberFormat nf = NumberFormat.getNumberInstance();
                        nf.setMinimumFractionDigits(1);
                        nf.setMaximumFractionDigits(3);
                        
                        for (int j = 0; j < list.length; j++) {
                            
                            if (j < list.length - 2) {
                                line += list[j].getName() + ", ";
                            } else if (j < list.length - 1) {
                                line += list[j].getName() + " und ";
                            } else {
                                line += list[j].getName();
                            }
                        }
                        
                        result += number + ". " + line + " mit " + nf.format(users.lastKey())
                                + " Wörtern pro Zeile.\n";
                        users.remove(users.lastKey());
                    }
                    
                    if (result.length() > 0 && result.charAt(result.length() - 1) == '\n') {
                        result = result.substring(0, result.length() - 1);
                    }
                    
                    message.respond(new Message(result));
                } catch (NumberFormatException e) {
                    message.respond(new Message("Ich habe deine Zahl ("
                            + text.replaceAll(topLines, "$1") + ") nicht " + "verstanden."));
                }
            }
            
            String topUptime = "[tT]op ?(1?\\d) ?([Uu]ptime|[zZ]eit online|[oO]nline ?[Zz]eit)"
                    + "[!\\.\\?]?";
            
            if (text.matches(topUptime)) {
                int count;
                
                try {
                    count = Integer.parseInt(text.replaceAll(topUptime, "$1"));
                    TreeMap<Long, TreeSet<User>> users = new TreeMap<Long, TreeSet<User>>();
                    
                    for (String user : this.users.keySet()) {
                        
                        if (users.containsKey(this.users.get(user).getUptime())) {
                            TreeSet<User> list = users.get(this.users.get(user).getUptime());
                            list.add(this.users.get(user));
                        } else {
                            TreeSet<User> list = new TreeSet<User>();
                            list.add(this.users.get(user));
                            users.put(this.users.get(user).getUptime(), list);
                        }
                    }
                    
                    int size = users.size();
                    String result = "";
                    
                    for (int i = 0; i < count && !users.isEmpty(); i++) {
                        User[] list = users.get(users.lastKey()).toArray(new User[0]);
                        String number = "", line = "";
                        
                        if ((i + 1) < 10 && count > 9 && size > 9) {
                            number += "0" + (i + 1);
                        } else {
                            number += (i + 1);
                        }
                        
                        for (int j = 0; j < list.length; j++) {
                            
                            if (j < list.length - 2) {
                                line += list[j].getName() + ", ";
                            } else if (j < list.length - 1) {
                                line += list[j].getName() + " und ";
                            } else {
                                line += list[j].getName();
                            }
                        }
                        
                        result += number + ". " + line + " mit " + getTime(users.lastKey()) + ".\n";
                        users.remove(users.lastKey());
                    }
                    
                    if (result.length() > 0 && result.charAt(result.length() - 1) == '\n') {
                        result = result.substring(0, result.length() - 1);
                    }
                    
                    message.respond(new Message(result));
                } catch (NumberFormatException e) {
                    message.respond(new Message("Ich habe deine Zahl ("
                            + text.replaceAll(topLines, "$1") + ") nicht " + "verstanden."));
                }
            }
            
            String userLines = "[wW]ie ?viele [zZ]eilen hat (.*?) (bisher|schon|bisher schon)? "
                    + "(gesagt|geschrieben)\\?";
            
            if (text.matches(userLines)) {
                String name = text.replaceAll(userLines, "$1");
                
                if (users.containsKey(name)) {
                    User user = users.get(name);
                    
                    if (user.getLineCount() == 1) {
                        message.respond(new Message(name + " hat schon eine Zeile geschrieben."));
                    } else {
                        message.respond(new Message(name + " hat schon " + user.getLineCount()
                                + " Zeilen geschrieben."));
                    }
                    
                } else {
                    message.respond(new Message("Das weiß ich nicht."));
                }
            }
            
            String userWords = "[wW]ie ?viele "
                    + "[wW](ö|oe)rter hat (.*?) (bisher|schon|bisher schon)? "
                    + "(gesagt|geschrieben)\\?";
            
            if (text.matches(userWords)) {
                String name = text.replaceAll(userWords, "$2");
                
                if (users.containsKey(name)) {
                    User user = users.get(name);
                    
                    if (user.getWordCount() == 1) {
                        message.respond(new Message(name + " hat schon ein Wort geschrieben."));
                    } else {
                        message.respond(new Message(name + " hat schon " + user.getWordCount()
                                + " Wörter geschrieben."));
                    }
                    
                } else {
                    message.respond(new Message("Das weiß ich nicht."));
                }
            }
            
            String userQuotient = "[Ww]ie ?viele [wW](ö|oe)rter pro [zZ]eile hat "
                    + "(.*?) (bisher|schon|bisher schon| im ([Dd]urch)?"
                    + "[sS]chnitt)? (gesagt|geschrieben)\\?";
            
            if (text.matches(userQuotient)) {
                String name = text.replaceAll(userQuotient, "$2");
                
                if (users.containsKey(name)) {
                    User user = users.get(name);
                    NumberFormat nf = NumberFormat.getNumberInstance();
                    nf.setMinimumFractionDigits(3);
                    nf.setMaximumFractionDigits(3);
                    message.respond(new Message(name + " hat im Schnitt "
                            + nf.format(user.getWordCount() / (double) user.getLineCount())
                            + " Wörter pro Zeile geschrieben."));
                } else {
                    message.respond(new Message("Das weiß ich nicht."));
                }
            }
            
            String who = "((Ü|ü|ue|Ue)ber wen|[vV]on wem) (hast|f(ü|ue)hrst) [dD]u (alles )?"
                    + "(eine [sS]tatistik|statistische ([iI]nfo(rmationen|s)|[dD]aten))\\?";
            
            if (text.matches(who)) {
                
                if (users.keySet().isEmpty()) {
                    message.respond(new Message("Momentan habe ich zu "
                            + "niemandem statistische Informationen."));
                } else if (users.keySet().size() == 1) {
                    message.respond(new Message("Momentan habe ich nur zu " + users.firstKey()
                            + " statistische Informationen."));
                } else {
                    message.respond(new Message("Ich sammle momentan statistische Daten über "
                            + BotApi.enumerate(users.keySet()) + "."));
                }
            }
            
            String howLongOnline = "[wW]ie lange (war|ist) (.*?) (schon )?"
                    + "(online|hier|da)( gewesen)?\\?";
            
            if (text.matches(howLongOnline)) {
                String name = text.replaceAll(howLongOnline, "$2");
                
                if (users.containsKey(name)) {
                    User user = users.get(name);
                    message.respond(new Message(name + " war schon " + getTime(user.getUptime())
                            + " online."));
                } else {
                    message.respond(new Message("Das weiß ich nicht."));
                }
            }
            
            String whenOnline = "[wW]ann (war|ist) (.*?) (zuletzt|"
                    + "zum letzten [mM]al) (online|da|hier)( gewesen)?\\?";
            
            if (text.matches(whenOnline)) {
                String name = text.replaceAll(whenOnline, "$2");
                
                if (users.containsKey(name)) {
                    Date lastOnline = users.get(name).getLastOnline();
                    
                    if (lastOnline == null) {
                        message.respond(new Message(name + " ist gerade online!"));
                    } else {
                        message.respond(new Message(name + " war zuletzt am " + getDate(lastOnline)
                                + " um " + getTime(lastOnline) + " online."));
                    }
                    
                } else {
                    message.respond(new Message("Das weiß ich nicht."));
                }
            }
        }
    }
    
    private String getDate(Date date) {
        String result = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        result += day < 10 ? "0" + day + "." : day + ".";
        result += month < 10 ? "0" + month + "." : month;
        return result + calendar.get(Calendar.YEAR);
    }
    
    private String getTime(Date date) {
        String result = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        result += hour < 10 ? "0" + hour + ":" : hour + ":";
        result += min < 10 ? "0" + min + ":" : min + ":";
        result += sec < 10 ? "0" + sec + ":" : sec;
        return result;
    }
    
    private String getTime(long time) {
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
