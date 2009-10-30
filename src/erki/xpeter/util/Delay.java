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
