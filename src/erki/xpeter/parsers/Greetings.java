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

package erki.xpeter.parsers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.TreeMap;

import javax.swing.Timer;

import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.msg.UserJoinedMessage;
import erki.xpeter.util.BotApi;

/**
 * A {@link Parser} that greets people when they greet the bot.
 * 
 * @author Edgar Kalkowski
 */
public class Greetings implements Parser, Observer<TextMessage> {
    
    private LinkedList<String> hello;
    private LinkedList<String> cu;
    private LinkedList<String> phrases;
    
    private TreeMap<String, Timer> timers = new TreeMap<String, Timer>();
    
    private static final String GREETINGS_FILE = "config" + File.separator + "greetings";
    
    private Observer<UserJoinedMessage> userJoinedObserver;
    
    @Override
    public void init(final Bot bot) {
        Log.debug("Initializing.");
        loadGreetings();
        bot.register(TextMessage.class, this);
        
        userJoinedObserver = new Observer<UserJoinedMessage>() {
            
            private Timer timer = null;
            
            private LinkedList<String> joins = new LinkedList<String>();
            
            @Override
            public void inform(final UserJoinedMessage msg) {
                
                // prevent self-greeting
                if (msg.getNick().equals(msg.getBotNick())) {
                    return;
                }
                
                // Don’t greet *every*body.
                if (Math.random() > 0.1) {
                    return;
                }
                
                synchronized (joins) {
                    
                    if (timer == null && checkTimer(msg.getNick())) {
                        joins.add(msg.getNick());
                        
                        timer = new Timer(3000, new ActionListener() {
                            
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                
                                synchronized (joins) {
                                    
                                    // only greet if it’s not too many people
                                    if (joins.size() < 3) {
                                        int rnd = (int) (Math.random() * hello.size());
                                        msg.respond(new Message(hello.get(rnd).substring(0, 1)
                                                .toUpperCase()
                                                + hello.get(rnd).substring(1)
                                                + " "
                                                + BotApi.enumerate(joins) + "!"));
                                    }
                                    
                                    joins.clear();
                                    timer = null;
                                }
                            }
                        });
                        
                        timer.setRepeats(false);
                        timer.start();
                    } else if (timer != null && checkTimer(msg.getNick())) {
                        joins.add(msg.getNick());
                        timer.restart();
                    }
                }
            }
        };
        
        bot.register(UserJoinedMessage.class, userJoinedObserver);
    }
    
    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
        bot.deregister(UserJoinedMessage.class, userJoinedObserver);
    }
    
    @SuppressWarnings("unchecked")
    private void loadGreetings() {
        
        try {
            ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(GREETINGS_FILE));
            hello = (LinkedList<String>) objectIn.readObject();
            cu = (LinkedList<String>) objectIn.readObject();
            phrases = (LinkedList<String>) objectIn.readObject();
            objectIn.close();
        } catch (FileNotFoundException e) {
            Log.info("No greetings file found. Initializing with default values.");
            hello = new LinkedList<String>();
            hello.add("Hallo");
            hello.add("Hi");
            hello.add("Tach");
            cu = new LinkedList<String>();
            cu.add("Cu");
            cu.add("Tschüss");
            cu.add("Ciao");
            cu.add("Auf wiedersehen");
            phrases = new LinkedList<String>();
            phrases.add("Schön Dich zu sehen!");
            phrases.add("Du schon wieder ...");
        } catch (IOException e) {
            Log.error(e);
            Log.warning("Greetings file could not be loaded. "
                    + "Initializing with default values.");
            hello = new LinkedList<String>();
            hello.add("Hallo");
            hello.add("Hi");
            hello.add("Tach");
            cu = new LinkedList<String>();
            cu.add("Cu");
            cu.add("Tschüss");
            cu.add("Ciao");
            cu.add("Auf wiedersehen");
            phrases = new LinkedList<String>();
            phrases.add("Schön Dich zu sehen!");
            phrases.add("Du schon wieder ...");
        } catch (ClassNotFoundException e) {
            Log.error(e);
            Log.warning("Greetings file could not be loaded. "
                    + "Initializing with default values.");
            hello = new LinkedList<String>();
            hello.add("Hallo");
            hello.add("Hi");
            hello.add("Tach");
            cu = new LinkedList<String>();
            cu.add("Cu");
            cu.add("Tschüss");
            cu.add("Ciao");
            cu.add("Auf wiedersehen");
            phrases = new LinkedList<String>();
            phrases.add("Schön Dich zu sehen!");
            phrases.add("Du schon wieder ...");
        }
    }
    
    private void saveGreetings() {
        
        try {
            ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(
                    GREETINGS_FILE));
            objectOut.writeObject(hello);
            objectOut.writeObject(cu);
            objectOut.writeObject(phrases);
            objectOut.close();
        } catch (FileNotFoundException e) {
            Log.error(e);
            Log.warning("Greetings could not be saved!");
        } catch (IOException e) {
            Log.error(e);
            Log.warning("Greetings could not be saved!");
        }
    }
    
    @Override
    public void inform(final TextMessage msg) {
        String nick = msg.getBotNick();
        String text = msg.getText();
        boolean addresses = false;
        
        if (BotApi.addresses(text, nick)) {
            addresses = true;
        }
        
        Log.debug("Informed of " + msg + " (addresses = " + addresses + ").");
        
        text = BotApi.trimNick(text, nick);
        
        String hello = "[nN]eue [bB]egr(ü|ue)(ß|ss)ung:(.*?)";
        
        if (addresses && text.matches(hello)) {
            String newHello = text.replaceAll(hello, "$3").trim();
            
            while (newHello.endsWith(".") || newHello.endsWith("!") || newHello.endsWith("?")) {
                newHello = newHello.substring(0, newHello.length() - 1);
            }
            
            if (!this.hello.contains(newHello)) {
                this.hello.add(newHello);
                saveGreetings();
                Log.info("Neue Begrüßung „" + newHello + "“ hinzugefügt.");
                msg.respond(new Message("Ich habe die neue Begrüßung „" + newHello + "“ gelernt!"));
            } else {
                msg.respond(new Message("Diese Begrüßung kenne ich schon."));
            }
        }
        
        String cu = "[nN]eue [vV]erabschiedung:(.*?)";
        
        if (addresses && text.matches(cu)) {
            String newCu = text.replaceAll(cu, "$1").trim();
            
            while (newCu.endsWith("!") || newCu.endsWith(".") || newCu.endsWith("?")) {
                newCu = newCu.substring(0, newCu.length() - 1);
            }
            
            if (!this.cu.contains(newCu)) {
                this.cu.add(newCu);
                saveGreetings();
                Log.info("Neue Verabschiedung „" + newCu + "“ hinzugefügt.");
                msg
                        .respond(new Message("Ich habe die neue Verabschiedung „" + newCu
                                + "“ gelernt!"));
            } else {
                msg.respond(new Message("Diese Verabschiedung kenne ich schon."));
            }
        }
        
        String phrase = "[nN]eue [bB]egr(ü|ue)(ss|ß)ungs(floskel|formel):(.*)";
        
        if (addresses && text.matches(phrase)) {
            String newPhrase = text.replaceAll(phrase, "$4").trim();
            
            if (!phrases.contains(newPhrase)) {
                phrases.add(newPhrase);
                saveGreetings();
                Log.info("Neue Begrüßungsfloskel „" + newPhrase + "“ hinzugefügt.");
                msg.respond(new Message("Ich habe die neue Begrüßungsfloskel „" + newPhrase
                        + "“ gelernt!"));
            } else {
                msg.respond(new Message("Diese Begrüßungsfloskel kenne ich schon."));
            }
        }
        
        String greeting = "(" + nick + "[:,;]?[ ]?" + greeting() + "[\\.,!?]?|" + greeting()
                + "[:,;]? " + nick + "[\\.,!?]?)";
        
        if (msg.getText().matches(greeting)) {
            
            if (checkTimer(msg.getNick())) {
                Log.info(msg.getNick() + " hat mich begrüßt.");
                int rnd = (int) (Math.random() * 4);
                
                if (rnd == 0) {
                    rnd = (int) (Math.random() * this.hello.size());
                    int rnd2 = (int) (Math.random() * phrases.size());
                    msg.respond(new Message(this.hello.get(rnd).substring(0, 1).toUpperCase()
                            + this.hello.get(rnd).substring(1) + " " + msg.getNick() + "!"));
                    msg.respond(new DelayedMessage(phrases.get(rnd2), 2000));
                } else {
                    rnd = (int) (Math.random() * this.hello.size());
                    msg.respond(new Message(this.hello.get(rnd).substring(0, 1).toUpperCase()
                            + this.hello.get(rnd).substring(1) + " " + msg.getNick() + "!"));
                }
            }
            
        }
        
        String bye = "(" + nick + "[:,;]?[ ]?" + bye() + "[\\.,!?]?|" + bye() + "[:,;]? " + nick
                + "[\\.,!?]?)";
        
        if (msg.getText().matches(bye)) {
            Log.info(msg.getNick() + " hat sich von mir verabschiedet.");
            int rnd = (int) (Math.random() * this.cu.size());
            msg.respond(new Message(this.cu.get(rnd).substring(0, 1).toUpperCase()
                    + this.cu.get(rnd).substring(1) + " " + msg.getNick() + "!"));
        }
        
        String listHello = "[wW]elche [bB]egr(ü|ue)(ss|ß)ungen kennst [Dd]u( alles| so( alles)?)?\\?";
        
        if (addresses && text.matches(listHello)) {
            
            if (this.hello.size() > 1) {
                msg
                        .respond(new Message("Ich kenne die Begrüßungen "
                                + BotApi.enumerate(this.hello)));
            } else if (this.hello.size() == 1) {
                msg.respond(new Message("Ich kenne lediglich die Begrüßung " + this.hello.get(0)
                        + "."));
            } else {
                msg.respond(new Message("Ich kenne gar keine Begrüßungen. :("));
            }
        }
        
        String listCu = "[wW]elche [vV]erabschiedungen kennst [dD]u( alles)?\\?";
        
        if (addresses && text.matches(listCu)) {
            
            if (this.cu.size() > 1) {
                msg.respond(new Message("Ich kenne die Verabschiedungen "
                        + BotApi.enumerate(this.cu)));
            } else if (this.cu.size() == 1) {
                msg.respond(new Message("Ich kenne lediglich die Verabschiedung " + this.cu.get(0)
                        + "."));
            } else {
                msg.respond(new Message("Ich kenne gar keine Verabschiedungen. :("));
            }
        }
        
        String listPhrases = "[wW]elche [bB]egr(ü|ue)(ss|ß)"
                + "ungs(floskeln|formeln) kennst [dD]u( alles)?\\?";
        
        if (addresses && text.matches(listPhrases)) {
            
            if (this.phrases.size() > 1) {
                msg.respond(new Message("Ich kenne die Begrüßungen "
                        + BotApi.enumerate(this.phrases)));
            } else if (this.phrases.size() == 1) {
                msg.respond(new Message("Ich kenne lediglich die eine Begrüßungsfloskel "
                        + this.phrases.get(0) + "."));
            } else {
                msg.respond(new Message("Ich kenne leider gar keine Begrüßungsfloskeln. :("));
            }
        }
        
        String forgetHello = "[vV]ergiss die [bB]egr(ü|ue)(ss|ß)ung(: )?(.*)";
        
        if (addresses && text.matches(forgetHello)) {
            String forget = text.replaceAll(forgetHello, "$4").trim();
            
            if (this.hello.contains(forget)) {
                this.hello.remove(forget);
                saveGreetings();
                msg.respond(new Message("Schon vergessen ..."));
            } else {
                
                if (this.hello.contains(forget.substring(0, forget.length() - 1))) {
                    this.hello.remove(forget.substring(0, forget.length() - 1));
                    saveGreetings();
                    msg.respond(new Message("Schon vergessen ..."));
                } else {
                    msg.respond(new Message("Die Begrüßung „" + forget + "“ kenne ich gar nicht!"));
                }
            }
        }
        
        String forgetCu = "[vV]ergiss die [vV]erabschiedung(: )?(.*)";
        
        if (addresses && text.matches(forgetCu)) {
            String forget = text.replaceAll(forgetCu, "$2").trim();
            
            if (this.cu.contains(forget)) {
                this.cu.remove(forget);
                saveGreetings();
                msg.respond(new Message("Schon vergessen ..."));
            } else {
                
                if (this.cu.contains(forget.substring(0, forget.length() - 1))) {
                    this.cu.remove(forget.substring(0, forget.length() - 1));
                    saveGreetings();
                    msg.respond(new Message("Schon vergessen ..."));
                } else {
                    msg.respond(new Message("Die Verabschiedung „" + forget
                            + "“ kenne ich gar nicht!"));
                }
            }
        }
        
        String forgetPhrase = "[vV]ergiss die [bB]egr(ü|ue)(ss|ß)ungs(formel|floskel)(: )?(.*)";
        
        if (addresses && text.matches(forgetPhrase)) {
            String forget = text.replaceAll(forgetPhrase, "$5");
            
            if (phrases.contains(forget)) {
                phrases.remove(forget);
                saveGreetings();
                msg.respond(new Message("Schon vergessen ..."));
            } else {
                
                if (phrases.contains(forget.substring(0, forget.length() - 1))) {
                    phrases.remove(forget.substring(0, forget.length() - 1));
                    saveGreetings();
                    msg.respond(new Message("Schon vergessen ..."));
                } else {
                    msg.respond(new Message("Die Begrüßungsfloskel „" + forget
                            + "“ kenne ich gar nicht!"));
                }
            }
        }
    }
    
    private boolean checkTimer(final String name) {
        
        if (timers.containsKey(name)) {
            return false;
        } else {
            
            Timer timer = new Timer(300000, new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    timers.remove(name);
                }
            });
            
            timer.setRepeats(false);
            timers.put(name, timer);
            timer.start();
            return true;
        }
    }
    
    private String greeting() {
        String result = "(";
        
        for (String s : hello) {
            result += s + "|";
        }
        
        return result.substring(0, result.length() - 1) + ")";
    }
    
    private String bye() {
        String result = "(";
        
        for (String s : cu) {
            result += s + "|";
        }
        
        return result.substring(0, result.length() - 1) + ")";
    }
    
    public static void main(String[] args) {
        String nick = "xpeter";
        String greeting = "(" + nick + "[:,;]?[ ]?(Hallo|Hi)[\\.,!?]?|(Hallo|Hi)[:,;]? " + nick
                + "[\\.,!?]?)";
        System.out.println("Hallo xpeter. -> " + "Hallo xpeter.".matches(greeting));
        System.out.println("Hallo xpeter! -> " + "Hallo xpeter!".matches(greeting));
        System.out.println("Hallo xpeter -> " + "Hallo xpeter".matches(greeting));
        System.out.println("xpeter: Hallo. -> " + "xpeter: Hallo.".matches(greeting));
        System.out.println("xpeter: Hallo! -> " + "xpeter: Hallo!".matches(greeting));
        System.out.println("xpeter: Hallo -> " + "xpeter: Hallo".matches(greeting));
    }
}
