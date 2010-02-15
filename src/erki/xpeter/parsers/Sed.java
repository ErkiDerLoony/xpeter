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

import java.util.TreeMap;

import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;

public class Sed implements Parser, Observer<TextMessage> {
    
    private TreeMap<String, String> lastSaid = new TreeMap<String, String>();
    
    @Override
    public void init(Bot bot) {
        bot.register(TextMessage.class, this);
    }
    
    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
    }
    
    @Override
    public void inform(TextMessage msg) {
        String text = msg.getText();
        String nick = msg.getNick();
        
        if (lastSaid.containsKey(nick) && text.startsWith("s") && text.length() > 2) {
            String regex = null, replacement = null;
            String delimiter = text.substring(1, 2);
            
            /*
             * Only accept non-word characters as delimiters because the bot otherwise reacts to too
             * many normal sentences.
             */
            if (!delimiter.matches("[A-Za-z0-9]")) {
                
                String rest = text.substring(2, text.length() - 1);
                String longrest = text.substring(2);
                
                if (text.endsWith(delimiter) && rest.contains(delimiter)) {
                    regex = rest.substring(0, rest.indexOf(delimiter));
                    replacement = rest.substring(rest.indexOf(delimiter) + 1);
                    Log.debug("Replacing " + regex + " with " + replacement + ".");
                    String result = lastSaid.get(nick).replaceAll(regex, replacement);
                    msg.respond(new Message(nick + " meinte: " + result));
                    return;
                }
                
                if (longrest.contains(delimiter)) {
                    regex = longrest.substring(0, longrest.indexOf(delimiter));
                    replacement = longrest.substring(longrest.indexOf(delimiter) + 1);
                    Log.debug("Replacing " + regex + " with " + replacement + ".");
                    String result = lastSaid.get(nick).replaceAll(regex, replacement);
                    msg.respond(new Message(nick + " meinte: " + result));
                    return;
                }
            }
        }
        
        lastSaid.put(nick, text);
    }
}
