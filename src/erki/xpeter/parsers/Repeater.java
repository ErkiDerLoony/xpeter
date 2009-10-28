/*
 * © Copyright 2008–2009 by Edgar Kalkowski (eMail@edgar-kalkowski.de)
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

import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.BotApi;
import erki.xpeter.con.Connection;
import erki.xpeter.msg.TextMessage;

/**
 * A dumb parser for testing purposes that repeats everything someone says to the bot.
 * 
 * @author Edgar Kalkowski
 */
public class Repeater implements Parser, Observer<TextMessage> {
    
    @Override
    public void init(Bot bot) {
        Log.debug("Initializing.");
    }
    
    @Override
    public void inform(TextMessage msg) {
        Connection con = msg.getConnection();
        String nick = con.getNick();
        String text = msg.getText();
        
        if (BotApi.addresses(text, nick)) {
            text = BotApi.trimNick(text, nick);
            con.send(text);
        }
    }
}
