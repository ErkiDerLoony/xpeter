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

import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.NickChangeMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.msg.UserJoinedMessage;
import erki.xpeter.msg.UserLeftMessage;

/**
 * This parser forwards everything that is said in one chat to all other chats.
 * 
 * @author Edgar Kalkowski
 */
public class Interconnector implements Parser, Observer<TextMessage> {
    
    private Bot bot;
    
    private Observer<UserJoinedMessage> userJoinedObserver;
    
    private Observer<UserLeftMessage> userLeftObserver;
    
    private Observer<NickChangeMessage> nickChangeObserver;
    
    @Override
    public void init(final Bot bot) {
        this.bot = bot;
        bot.register(TextMessage.class, this);
        
        userJoinedObserver = new Observer<UserJoinedMessage>() {
            
            @Override
            public void inform(UserJoinedMessage msg) {
                bot.broadcast(new Message(msg.getConnection().getShortId() + ": " + msg.getNick()
                        + " hat den Chat betreten."), msg.getConnection());
            }
        };
        
        userLeftObserver = new Observer<UserLeftMessage>() {
            
            @Override
            public void inform(UserLeftMessage msg) {
                bot.broadcast(new Message(msg.getConnection().getShortId() + ": " + msg.getNick()
                        + " hat den Chat verlassen."), msg.getConnection());
            }
        };
        
        nickChangeObserver = new Observer<NickChangeMessage>() {
            
            @Override
            public void inform(NickChangeMessage msg) {
                bot.broadcast(new Message(msg.getConnection().getShortId() + ": "
                        + msg.getOldNick() + " heißt jetzt " + msg.getNewNick() + "."), msg
                        .getConnection());
            }
        };
        
        bot.register(UserJoinedMessage.class, userJoinedObserver);
        bot.register(UserLeftMessage.class, userLeftObserver);
        bot.register(NickChangeMessage.class, nickChangeObserver);
    }
    
    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
        bot.deregister(UserJoinedMessage.class, userJoinedObserver);
        bot.deregister(UserLeftMessage.class, userLeftObserver);
        bot.deregister(NickChangeMessage.class, nickChangeObserver);
    }
    
    @Override
    public void inform(TextMessage msg) {
        
        if (!msg.getNick().equals(msg.getConnection().getNick())) {
            bot.broadcast(new Message(msg.getNick() + "@" + msg.getConnection().getShortId()
                    + ": »" + msg.getText() + "«"), msg.getConnection());
        }
    }
}
