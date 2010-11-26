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

import java.util.TreeSet;

import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.util.BotApi;
import erki.xpeter.util.ParserFinder;

/**
 * This parser manages the loading and unloading of parsers at runtime.
 * 
 * @author Edgar Kalkowski
 */
public class ParserLoader implements Parser, Observer<TextMessage> {
    
    private Bot bot;
    
    @Override
    public void init(Bot bot) {
        bot.register(TextMessage.class, this);
        this.bot = bot;
    }
    
    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void inform(TextMessage msg) {
        String text = msg.getText();
        
        if (!BotApi.addresses(text, msg.getBotNick())) {
            return;
        }
        
        text = BotApi.trimNick(text, msg.getBotNick());
        
        if (text.matches("([wW]elche|[wW]as f(ü|ue)r) [pP]arser sind geladen\\.?\\??")) {
            String response = "Momentan sind die Parser ";
            TreeSet<Class<? extends Parser>> loaded = bot.getParsers();
            Class<? extends Parser>[] loadedParsers = loaded.toArray(new Class[0]);
            
            if (loadedParsers.length > 1) {
                
                for (int i = 0; i < loadedParsers.length; i++) {
                    
                    if (i < loadedParsers.length - 2) {
                        response += loadedParsers[i].getSimpleName() + ", ";
                    } else if (i == loadedParsers.length - 2) {
                        response += loadedParsers[i].getSimpleName() + " und ";
                    } else {
                        response += loadedParsers[i].getSimpleName() + " geladen.";
                    }
                }
                
                msg.respond(new DelayedMessage(response, 3000));
            } else if (loadedParsers.length == 1) {
                msg.respond(new DelayedMessage("Im Moment ist nur der Parser "
                        + loadedParsers[0].getSimpleName() + " geladen.", 1500));
            } else {
                msg.respond(new DelayedMessage(
                        "Hm, es scheint so, als ob kein einiger Parser geladen wäre.", 3500));
                msg.respond(new DelayedMessage(
                        "Und doch bearbeitet dieser Parser gerade eine Nachricht.", 7500));
                msg.respond(new DelayedMessage("Das ist doch nicht möglich! ...", 11000));
                msg.respond(new DelayedMessage("Waaah! Ich werde verrückt!", 15000));
            }
        }
        
        String match = "([Ll]ade|[Ll]oad) (.*?)";
        
        if (text.matches(match)) {
            String parser = text.replaceAll(match, "$2");
            
            if (parser.endsWith(".class")) {
                parser = parser.substring(0, parser.length() - ".class".length());
            }
            
            if (parser.endsWith("!") || parser.endsWith(".")) {
                parser = parser.substring(0, parser.length() - 1);
            }
            
            Log.debug("Recognized match for parser " + parser + ".");
            
            TreeSet<Class<? extends Parser>> foundParsers = ParserFinder.findParsers(BotApi
                    .getParserDir());
            boolean added = false;
            
            for (Class<? extends Parser> clazz : foundParsers) {
                
                if (clazz.getSimpleName().equals(parser)) {
                    bot.add(clazz);
                    added = true;
                }
            }
            
            if (added) {
                msg.respond(new Message("Ok."));
            } else {
                msg.respond(new DelayedMessage("Ein Parser mit dem Namen " + parser
                        + " wurde nicht gefunden!", 2000));
            }
        }
        
        match = "([Ee]ntlade|[uU]nload) (.*?)";
        
        if (text.matches(match)) {
            String parser = text.replaceAll(match, "$2");
            
            if (parser.endsWith(".class")) {
                parser = parser.substring(0, parser.length() - ".class".length());
            }
            
            if (parser.endsWith("!") || parser.endsWith(".")) {
                parser = parser.substring(0, parser.length() - 1);
            }
            
            Log.debug("Recognized match for parser " + parser + ".");
            
            if (parser.equals("ParserLoader")) {
                msg
                        .respond(new DelayedMessage("Der ParserLoader kann nicht entfernt werden!",
                                2000));
            } else {
                TreeSet<Class<? extends Parser>> parsers = bot.getParsers();
                boolean removed = false;
                
                for (Class<? extends Parser> clazz : parsers) {
                    Log.debug("Checking " + clazz.getSimpleName() + " against " + parser);
                    
                    if (clazz.getSimpleName().equals(parser)) {
                        Log.debug("Match!");
                        bot.remove(clazz);
                        removed = true;
                    }
                }
                
                if (removed) {
                    msg.respond(new Message("Ok."));
                } else {
                    msg.respond(new DelayedMessage("Der Parser " + parser
                            + " ist entweder nicht geladen oder er existiert gar nicht!", 2000));
                }
            }
        }
    }
}
