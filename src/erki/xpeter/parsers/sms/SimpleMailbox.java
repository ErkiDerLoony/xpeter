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

package erki.xpeter.parsers.sms;

import java.util.LinkedList;
import java.util.TreeMap;

import erki.api.storage.Storage;
import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.NickChangeMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.msg.UserJoinedMessage;
import erki.xpeter.parsers.Parser;
import erki.xpeter.util.BotApi;
import erki.xpeter.util.Keys;
import erki.xpeter.util.StorageKey;

public class SimpleMailbox implements Parser, Observer<TextMessage> {
    
    private TreeMap<String, LinkedList<ShortMessage>> msgs;
    
    private Observer<UserJoinedMessage> userJoinedObserver;
    
    private Observer<NickChangeMessage> nickChangeObserver;
    
    private Storage<Keys> storage;
    
    @Override
    public void init(Bot bot) {
        storage = bot.getStorage();
        StorageKey<TreeMap<String, LinkedList<ShortMessage>>> key = new StorageKey<TreeMap<String, LinkedList<ShortMessage>>>(
                Keys.SHORT_MESSAGES);
        
        if (storage.contains(key)) {
            msgs = storage.get(key);
        } else {
            msgs = new TreeMap<String, LinkedList<ShortMessage>>();
        }
        
        bot.register(TextMessage.class, this);
        
        userJoinedObserver = new Observer<UserJoinedMessage>() {
            
            @Override
            public void inform(UserJoinedMessage msg) {
                check(msg.getNick(), msg);
            }
        };
        
        nickChangeObserver = new Observer<NickChangeMessage>() {
            
            public void inform(NickChangeMessage msg) {
                check(msg.getNewNick(), msg);
            }
        };
        
        bot.register(UserJoinedMessage.class, userJoinedObserver);
        bot.register(NickChangeMessage.class, nickChangeObserver);
    }
    
    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
        bot.deregister(UserJoinedMessage.class, userJoinedObserver);
        bot.deregister(NickChangeMessage.class, nickChangeObserver);
        storage.add(new StorageKey<TreeMap<String, LinkedList<ShortMessage>>>(Keys.SHORT_MESSAGES),
                msgs);
    }
    
    private void check(String nick, Message msg) {
        
        synchronized (msgs) {
            
            if (msgs.containsKey(nick)) {
                LinkedList<ShortMessage> sms = msgs.get(nick);
                
                for (ShortMessage m : sms) {
                    msg.respond(new DelayedMessage(nick + ": Ich soll dir von " + m.getSender()
                            + " sagen: " + m.getMessage() + " (vor " + m.getDate() + ")", 2000));
                }
                
                msgs.remove(nick);
                storage.add(new StorageKey<TreeMap<String, LinkedList<ShortMessage>>>(
                        Keys.SHORT_MESSAGES), msgs);
            }
        }
    }
    
    @Override
    public void inform(TextMessage msg) {
        String text = msg.getText();
        
        check(msg.getNick(), msg);
        
        if (!BotApi.addresses(text, msg.getBotNick())) {
            return;
        }
        
        text = BotApi.trimNick(text, msg.getBotNick());
        
        if (text
                .matches("[fF](ue|ü)r wen (sind|hast du) ((so )?alles |im [Mm]oment)?[nN]achrichten "
                        + "gespeichert\\?")
                || text.matches("[wW]as f(ue|ü)r [nN]achrichten (hast|kennst) "
                        + "du( gespeichert| so)?\\?")
                || text.matches("[mM]ailbox ?[sS]tatus\\??!?\\.?")
                || text.matches("[sS]tatus der [mM]ailbox\\??!?\\.?")) {
            
            synchronized (msgs) {
                
                if (msgs.isEmpty()) {
                    msg.respond(new DelayedMessage("Im Moment sind keine Nachrichten gespeichert.",
                            3000));
                } else if (msgs.size() == 1
                        && msgs.get(msgs.keySet().iterator().next()).size() == 1) {
                    msg.respond(new DelayedMessage("Im Moment ist eine Nachricht für "
                            + msgs.keySet().iterator().next() + " gespeichert (seit "
                            + msgs.get(msgs.keySet().iterator().next()).get(0).getDate() + ").",
                            3000));
                } else {
                    String response = "Im Moment sind gespeichert:";
                    
                    for (String nick : msgs.keySet()) {
                        
                        if (msgs.get(nick).size() == 1) {
                            response += "\n – eine Nachricht für " + nick + " (seit "
                                    + msgs.get(nick).get(0).getDate() + ")";
                        } else {
                            response += "\n – " + BotApi.number(msgs.get(nick).size())
                                    + " Nachrichten für " + nick + " (die älteste seit "
                                    + msgs.get(nick).getFirst().getDate() + ", die neuste seit "
                                    + msgs.get(nick).getLast().getDate() + ")";
                        }
                    }
                    
                    msg.respond(new DelayedMessage(response, 4000));
                }
            }
            
            return;
        }
        
        String match = "[sS]ag( der| dem)? (.*?) mal( von mir)?[:;,] (.*)";
        
        if (text.matches(match)) {
            String nick = text.replaceAll(match, "$2");
            String message = text.replaceAll(match, "$4");
            Log.debug("Recognized “" + message + "” to »" + nick + "«.");
            add(nick, message, msg.getNick());
            msg.respond(new DelayedMessage("Ok, mach ich.", 2000));
            return;
        }
        
        match = "[sS]ag( der| dem)? (.*?)[:;,] (.*)";
        
        if (text.matches(match)) {
            String nick = text.replaceAll(match, "$2");
            String message = text.replaceAll(match, "$3");
            Log.debug("Recognized “" + message + "” to »" + nick + "«.");
            add(nick, message, msg.getNick());
            msg.respond(new DelayedMessage("Ok, mach ich.", 2000));
        }
    }
    
    private void add(String nick, String msg, String sender) {
        
        synchronized (msgs) {
            
            if (!msgs.containsKey(nick)) {
                msgs.put(nick, new LinkedList<ShortMessage>());
            }
            
            msgs.get(nick).addLast(new ShortMessage(msg, sender));
        }
        
        storage.add(new StorageKey<TreeMap<String, LinkedList<ShortMessage>>>(Keys.SHORT_MESSAGES),
                msgs);
    }
}
