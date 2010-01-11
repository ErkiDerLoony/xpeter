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

package erki.xpeter.parsers.statistics;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Parser;

public class Statistics implements Parser, Observer<TextMessage> {
    
    private static final String STAT_FILE = "config" + System.getProperty("file.separator")
            + "stats";
    
    private BotInterface bot;
    
    private TreeMap<String, User> users;
    
    @Override
    public void init(final BotInterface bot) {
        this.bot = bot;
        loadStats();
        bot.register(this);
        
        // Close all sessions if bot disconnects.
        bot.register(new DisconnectObserver() {
            
            @Override
            public void inform() {
                
                synchronized (users) {
                    
                    for (String user : users.keySet()) {
                        users.get(user).closeSession();
                        Log.debug("Closed statistics session for " + user + ".");
                    }
                }
            }
        });
        
        // Start a session for each online user if bot reconnects.
        bot.register(new ReconnectObserver() {
            
            @Override
            public void inform() {
                Collection<String> userList = bot.getUserList();
                
                synchronized (users) {
                    
                    for (String user : userList) {
                        
                        if (users.keySet().contains(user)) {
                            users.get(user).startSession();
                        } else {
                            users.put(user, new User(user));
                        }
                        
                        Log.debug("Started statistics session for " + user + ".");
                    }
                }
            }
        });
        
        // Start a new session for a joining user.
        bot.register(new LoginMessageObserver() {
            
            @Override
            public LinkedList<? extends ResponseMessage> inform(LoginMessage message) {
                
                synchronized (users) {
                    
                    if (users.keySet().contains(message.getName())) {
                        users.get(message.getName()).startSession();
                    } else {
                        users.put(message.getName(), new User(message.getName()));
                    }
                }
                
                Log.debug("Started statistics session for " + message.getName() + ".");
                return null;
            }
        });
        
        // Close session for leaving user.
        bot.register(new LogoutMessageObserver() {
            
            @Override
            public LinkedList<? extends ResponseMessage> inform(LogoutMessage message) {
                
                synchronized (users) {
                    
                    if (users.keySet().contains(message.getName())) {
                        
                        if (!bot.getUserList().contains(message.getName())) {
                            users.get(message.getName()).closeSession();
                            Log.debug("Closed statistics session for " + message.getName() + ".");
                        }
                        
                    } else {
                        Log.warning("User " + message.getName()
                                + " has left, but had no active statistics session!");
                    }
                    
                }
                
                return null;
            }
        });
        
        // Close session for old nick and start session for new nick.
        bot.register(new NickchangeMessageObserver() {
            
            @Override
            public LinkedList<? extends ResponseMessage> inform(NickchangeMessage message) {
                
                synchronized (users) {
                    
                    if (users.keySet().contains(message.getName())) {
                        users.get(message.getName()).closeSession();
                        Log.debug("Closed statistics session for " + message.getName() + ".");
                    }
                    
                    if (users.keySet().contains(message.getNewName())) {
                        users.get(message.getNewName()).startSession();
                    } else {
                        users.put(message.getNewName(), new User(message.getNewName()));
                    }
                }
                
                Log.debug("Started new statistics session for " + message.getNewName() + ".");
                return null;
            }
        });
        
        bot.register(new IndirectMessageObserver() {
            
            @Override
            public LinkedList<? extends ResponseMessage> inform(IndirectMessage message) {
                
                if (!users.containsKey(message.getName())) {
                    users.put(message.getName(), new User(message.getName()));
                    Log.warning(message.getName() + " said something, but "
                            + "had no active statistics session!");
                }
                
                // Count statistics for the user who said something.
                users.get(message.getName()).addLine(message.getText());
                return null;
            }
        });
        
        // Start thread that saves the statistical information to the STAT_FILE
        // from time to time in case the bot crashes or is terminated.
        new Thread() {
            
            @Override
            public void run() {
                super.run();
                
                while (true) {
                    
                    try {
                        Thread.sleep(300000);
                    } catch (InterruptedException e) {
                    }
                    
                    checkUserList();
                    saveStats();
                }
            }
            
        }.start();
    }
    
    private void checkUserList() {
        Collection<String> userList = bot.getUserList();
        
        synchronized (users) {
            
            for (String user : userList) {
                
                if (users.containsKey(user)) {
                    users.get(user).startSession();
                } else {
                    users.put(user, new User(user));
                }
            }
            
            for (String user : users.keySet()) {
                
                if (!userList.contains(user)) {
                    users.get(user).closeSession();
                }
            }
        }
    }
    
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
    public LinkedList<? extends ResponseMessage> inform(TextMessage message) {
        LinkedList<ResponseMessage> result = new LinkedList<ResponseMessage>();
        
        synchronized (users) {
            
            if (!users.containsKey(message.getName())) {
                users.put(message.getName(), new User(message.getName()));
                Log.warning(message.getName() + " said something, but "
                        + "has no active statistics session!");
            }
            
            // Count statistics for the user who said something.
            users.get(message.getName()).addLine(message.getText());
            
            String topLines = "(" + bot.getName() + "|" + bot.getName().toLowerCase()
                    + ")[:,] ?[tT]op ?(1?\\d) ?([lL]ines|[zZ]eilen)?[!\\.\\?]?";
            
            if (message.getText().matches(topLines)) {
                int count;
                
                try {
                    count = Integer.parseInt(message.getText().replaceAll(topLines, "$2"));
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
                            result.add(new ResponseMessage(number + ". " + line
                                    + " mit einer Zeile.", 100, 2000 + i * 333));
                            
                        } else {
                            result.add(new ResponseMessage(number + ". " + line + " mit "
                                    + users.lastKey() + " Zeilen.", 100, 2000 + i * 333));
                        }
                        
                        users.remove(users.lastKey());
                    }
                    
                } catch (NumberFormatException e) {
                    result.add(new ResponseMessage("Ich habe deine Zahl für die Zeilen ("
                            + message.getText().replaceAll(topLines, "$2") + ") nicht "
                            + "verstanden.", 100, 2000));
                }
            }
            
            String topWords = "(" + bot.getName() + "|" + bot.getName().toLowerCase()
                    + ")[:,] ?[tT]op ?(1?\\d) ?([wW]ords|[wW](ö|oe)rter)[!\\.\\?]?";
            
            if (message.getText().matches(topWords)) {
                int count;
                
                try {
                    count = Integer.parseInt(message.getText().replaceAll(topWords, "$2"));
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
                            result.add(new ResponseMessage(number + ". " + line
                                    + " mit einem Wort.", 100, 2000 + i * 333));
                            
                        } else {
                            result.add(new ResponseMessage(number + ". " + line + " mit "
                                    + users.lastKey() + " Wörtern.", 100, 2000 + i * 333));
                        }
                        
                        users.remove(users.lastKey());
                    }
                    
                } catch (NumberFormatException e) {
                    result.add(new ResponseMessage("Ich habe deine Zahl für die Wörter ("
                            + message.getText().replaceAll(topLines, "$2") + ") nicht "
                            + "verstanden.", 100, 2000));
                }
            }
            
            String topQuotient = "(" + bot.getName() + "|" + bot.getName().toLowerCase()
                    + ")[:,] ?[tT]op ?(1?\\d) ?"
                    + "([qQ]uotient|[wW](ö|oe)rter pro [zZ]eile|[pP]ro|"
                    + "[wW][pP][Zz])[!\\.\\?]?";
            
            if (message.getText().matches(topQuotient)) {
                int count;
                
                try {
                    count = Integer.parseInt(message.getText().replaceAll(topQuotient, "$2"));
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
                        
                        result.add(new ResponseMessage(number + ". " + line + " mit "
                                + nf.format(users.lastKey()) + " Wörtern pro Zeile.", 100,
                                2000 + i * 333));
                        users.remove(users.lastKey());
                    }
                    
                } catch (NumberFormatException e) {
                    result.add(new ResponseMessage("Ich habe deine Zahl ("
                            + message.getText().replaceAll(topLines, "$2") + ") nicht "
                            + "verstanden.", 100, 2000));
                }
            }
            
            String topUptime = "(" + bot.getName() + "|" + bot.getName().toLowerCase()
                    + ")[:,] ?[tT]op ?(1?\\d) ?([Uu]ptime|[zZ]eit online|[oO]nline ?[Zz]eit)"
                    + "[!\\.\\?]?";
            
            if (message.getText().matches(topUptime)) {
                int count;
                
                try {
                    count = Integer.parseInt(message.getText().replaceAll(topUptime, "$2"));
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
                        
                        result.add(new ResponseMessage(number + ". " + line + " mit "
                                + getTime(users.lastKey()) + ".", 100, 2000 + i * 333));
                        users.remove(users.lastKey());
                    }
                    
                } catch (NumberFormatException e) {
                    result.add(new ResponseMessage("Ich habe deine Zahl ("
                            + message.getText().replaceAll(topLines, "$2") + ") nicht "
                            + "verstanden.", 100, 2000));
                }
            }
            
            String userLines = "(" + bot.getName() + "|" + bot.getName().toLowerCase()
                    + ")[:,] ?[wW]ie ?viele [zZ]eilen hat (.*?) (bisher|schon|bisher schon)? "
                    + "(gesagt|geschrieben)\\?";
            
            if (message.getText().matches(userLines)) {
                String name = message.getText().replaceAll(userLines, "$2");
                
                if (users.containsKey(name)) {
                    User user = users.get(name);
                    result.add(new ResponseMessage(name + " hat schon " + user.getLineCount()
                            + " Zeilen geschrieben.", 100, 3000));
                } else {
                    result.add(new ResponseMessage("Das weiß ich nicht.", 100, 3000));
                }
            }
            
            String userWords = "(" + bot.getName() + "|" + bot.getName().toLowerCase()
                    + ")[:,] ?[wW]ie ?viele "
                    + "[wW](ö|oe)rter hat (.*?) (bisher|schon|bisher schon)? "
                    + "(gesagt|geschrieben)\\?";
            
            if (message.getText().matches(userWords)) {
                String name = message.getText().replaceAll(userWords, "$3");
                
                if (users.containsKey(name)) {
                    User user = users.get(name);
                    result.add(new ResponseMessage(name + " hat schon " + user.getWordCount()
                            + " Wörter geschrieben.", 100, 3000));
                } else {
                    result.add(new ResponseMessage("Das weiß ich nicht.", 100, 3000));
                }
            }
            
            String userQuotient = "(" + bot.getName() + "|" + bot.getName().toLowerCase()
                    + ")[:,] ?[Ww]ie ?viele [wW](ö|oe)rter pro [zZ]eile hat "
                    + "(.*?) (bisher|schon|bisher schon| im ([Dd]urch)?"
                    + "[sS]chnitt)? (gesagt|geschrieben)\\?";
            
            if (message.getText().matches(userQuotient)) {
                String name = message.getText().replaceAll(userQuotient, "$3");
                
                if (users.containsKey(name)) {
                    User user = users.get(name);
                    NumberFormat nf = NumberFormat.getNumberInstance();
                    nf.setMinimumFractionDigits(3);
                    nf.setMaximumFractionDigits(3);
                    result.add(new ResponseMessage(name + " hat im Schnitt "
                            + nf.format(user.getWordCount() / (double) user.getLineCount())
                            + " Wörter pro Zeile geschrieben.", 100, 3000));
                } else {
                    result.add(new ResponseMessage("Das weiß ich nicht.", 100, 3000));
                }
            }
            
            String who = "(" + bot.getName() + "|" + bot.getName().toLowerCase()
                    + ")[:,] ?((Ü|ü|ue|Ue)ber "
                    + "wen|[vV]on wem) (hast|f(ü|ue)hrst) [dD]u (alles )?"
                    + "(eine [sS]tatistik|statistische ([iI]nfo(rmationen|s)|[dD]aten))\\?";
            
            if (message.getText().matches(who)) {
                
                if (users.keySet().isEmpty()) {
                    result.add(new ResponseMessage("Momentan habe ich zu "
                            + "niemandem statistische Informationen.", 100, 2000));
                } else if (users.keySet().size() == 1) {
                    result.add(new ResponseMessage("Momentan habe ich nur zu " + users.firstKey()
                            + " statistische Informationen.", 100, 2000));
                } else {
                    String users = "";
                    String[] array = this.users.keySet().toArray(new String[0]);
                    
                    for (int i = 0; i < array.length; i++) {
                        
                        if (i < array.length - 1) {
                            users += array[i] + ", ";
                        } else {
                            users = users.substring(0, users.length() - 2);
                            users += " und " + array[i];
                        }
                    }
                    
                    result.add(new ResponseMessage("Ich sammle momentan "
                            + "statistische Daten über " + users + ".", 100, 2000));
                }
            }
            
            String howLongOnline = "(" + bot.getName() + "|" + bot.getName().toLowerCase()
                    + ")[:,] ?[wW]ie lange (war|ist) (.*?) (schon )?"
                    + "(online|hier|da)( gewesen)?\\?";
            
            if (message.getText().matches(howLongOnline)) {
                String name = message.getText().replaceAll(howLongOnline, "$3");
                
                if (users.containsKey(name)) {
                    User user = users.get(name);
                    result.add(new ResponseMessage(name + " war schon " + getTime(user.getUptime())
                            + " online.", 100, 3000));
                } else {
                    result.add(new ResponseMessage("Das weiß ich nicht.", 100, 2000));
                }
            }
            
            String whenOnline = "(" + bot.getName() + "|" + bot.getName().toLowerCase()
                    + ")[:,] ?[wW]ann (war|ist) (.*?) (zuletzt|"
                    + "zum letzten [mM]al) (online|da|hier)( gewesen)?\\?";
            
            if (message.getText().matches(whenOnline)) {
                String name = message.getText().replaceAll(whenOnline, "$3");
                
                if (users.containsKey(name)) {
                    Date lastOnline = users.get(name).getLastOnline();
                    
                    if (lastOnline == null) {
                        result.add(new ResponseMessage(name + " ist gerade online!", 100, 2000));
                    } else {
                        result.add(new ResponseMessage(name + " war zuletzt am "
                                + getDate(lastOnline) + " um " + getTime(lastOnline) + " online.",
                                100, 2000));
                    }
                    
                } else {
                    result.add(new ResponseMessage("Das weiß ich nicht.", 100, 2000));
                }
            }
        }
        
        return result;
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
