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

package erki.xpeter.util;

import erki.xpeter.con.Connection;
import erki.xpeter.msg.DelayedMessage;

/**
 * This class realizes some delayed action. It can be used by {@link Connection}s to send
 * {@link DelayedMessage}s. There is one abstract method to be implemented which shall contain the
 * code to be executed after the delay has passed. As executing code takes time, too, it cannot be
 * guaranteed in any way that the given code will be executed at some <i>exact</i> point of time but
 * it is guaranteed that it is only executed after the delay has passed.
 * 
 * @author Edgar Kalkowski
 */
public abstract class Delay extends Thread {
    
    private long destTime;
    
    /**
     * Create a new Delay that executes {@link #delayedAction()} once if {@code
     * System#currentTimeMillis() == #destTime}.
     * 
     * @param destTime
     *        The UNIX timestamp at or after which {@link #delayedAction()} will be called (once).
     */
    public Delay(long destTime) {
        this.destTime = destTime;
    }
    
    /**
     * Create a new Delay that gets its timing information directly from a {@link DelayedMessage}.
     * 
     * @param msg
     *        The message whose timing information shall be used.
     */
    public Delay(DelayedMessage msg) {
        this(msg.getTimeOfCreation() + msg.getDelay());
    }
    
    @Override
    public void run() {
        super.run();
        
        while (destTime > System.currentTimeMillis()) {
            
            try {
                Thread.sleep(destTime - System.currentTimeMillis());
            } catch (InterruptedException e) {
            }
        }
        
        delayedAction();
    }
    
    /** The code contained in this method is executed (once) after the given delay has passed. */
    public abstract void delayedAction();
}
