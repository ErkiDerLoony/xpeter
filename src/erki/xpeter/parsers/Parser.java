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

import erki.xpeter.Bot;
import erki.xpeter.con.Connection;
import erki.xpeter.msg.Message;

/**
 * Implemented by all classes that want to parse messages received from the chats the bot is
 * connected to. If you want to add functionality to the bot you want to implement this one.
 * 
 * @author Edgar Kalkowski
 */
public interface Parser {
    
    /**
     * As the parser classes are instanciated using reflection (and thus no constructor can be used)
     * this method is used for initializing this parser and associating it to the instance of
     * {@link Bot} it belongs to.
     * 
     * @param bot
     *        The instance of {@link Bot} this parser object belongs to and through which e.g. all
     *        the connections to the different chats may be accessed.
     */
    public void init(Bot bot);
    
    /**
     * Process a message that was received over some {@link Connection}. Response messages to the
     * same connection may be sent directly through the connection instance that may be obtained
     * from the message object. If a response shall be sent to a different or all connections the
     * whole list of connections may be accessed via the associated instance of {@link Bot}.
     * 
     * @param msg
     *        The message to process.
     */
    public void parse(Message msg);
}
