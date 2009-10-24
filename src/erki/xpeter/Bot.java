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

import java.util.Collection;
import java.util.LinkedList;

import erki.api.util.Log;
import erki.xpeter.msg.Message;

public class Bot extends Thread {
    
    private Iterable<? extends Connection> cons;
    
    private Collection<Parser> parsers = new LinkedList<Parser>();
    
    public Bot(Iterable<? extends Connection> cons, Iterable<Class<? extends Parser>> parsers) {
        this.cons = cons;
        
        /*
         * Connect all connections to this bot. This is somewhat ugly but I can’t think of an
         * elegant other way to do it.
         */
        for (Connection con : cons) {
            con.setBot(this);
        }
        
        for (Class<? extends Parser> clazz : parsers) {
            
            try {
                this.parsers.add(clazz.newInstance());
            } catch (InstantiationException e) {
                Log.error(e);
                Log.warning("Parser " + clazz.getCanonicalName() + " could not be loaded!");
                Log.info("Trying to continue without this parser.");
            } catch (IllegalAccessException e) {
                Log.error(e);
                Log.warning("You are not allowed to instanciate the parser class "
                        + clazz.getCanonicalName() + ". Please check your security settings!");
                Log.info("Trying to continue without this parser.");
            }
        }
    }
    
    @Override
    public void run() {
        super.run();
        
        for (Connection con : cons) {
            new Thread(con).start();
        }
    }
    
    /**
     * Processes a message that was received from some connection. This method just delegates the
     * message to all available parsers and lets them do the work.
     * 
     * @param msg
     *        The message to process.
     */
    public void process(Message msg) {
        
    }
}
