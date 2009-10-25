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

package erki.xpeter;

/**
 * Interface implemented by all connection supported by xpeter.
 * 
 * @author Edgar Kalkowski
 */
public interface Connection extends Runnable {
    
    /**
     * Associates this connection to an instance of {@link Bot} that will be used to process
     * messages received via this connection. <b>This has to be done before this connection is
     * started (and respective the {@link #run()} method is called)!</b>
     * 
     * @param bot
     *        The associated bot instance for this connection.
     */
    public void setBot(Bot bot);
}
