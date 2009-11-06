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

package erki.xpeter.parsers;

import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.con.Connection;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.util.BotApi;

/**
 * This {@link Parser} can answer questions sent to the chat in the form: [ ] A oder [ ] B?
 * 
 * @author Edgar Kalkowski
 */
public class Answer implements Parser, Observer<TextMessage> {
    
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
        Connection con = msg.getConnection();
        String nick = con.getNick();
        
        if (!BotApi.addresses(text, nick)) {
            return;
        }
        
        text = BotApi.trimNick(text, nick);
        String question = "\\[ \\](.*?)\\[ \\](.*?)";
        
        if (text.matches(question)) {
            String response = "";
            String[] split = text.split("\\[ \\]");
            int rnd = (int) (Math.random() * (split.length - 1));
            
            for (int i = 0; i < split.length; i++) {
                
                if (i != rnd) {
                    response += split[i] + "[ ]";
                } else {
                    response += split[i] + "[x]";
                }
            }
            
            con.send(response.substring(0, response.length() - 3));
        }
    }
}
