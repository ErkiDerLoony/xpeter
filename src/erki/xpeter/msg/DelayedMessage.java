package erki.xpeter.msg;

import erki.xpeter.con.Connection;
import erki.xpeter.util.Delay;

/**
 * This message class is designed to be a response message of the bot that is only sent to the
 * server with some delay (to imitate typing time or sth. ;). It should not be used for incoming
 * messages from some chat server that are to be parsed by parsers. {@link Connection}s can use
 * {@link Delay} to implement the delay before actually sending {@code DelayedMessage}s.
 * 
 * @author Edgar Kalkowski
 */
public class DelayedMessage extends Message {
    
    private long timeOfCreation;
    
    private long delay;
    
    /**
     * Create a new DelayedMessage.
     * 
     * @param text
     *        The text to send to the chat server.
     * @param delay
     *        The time delay in milli-seconds after which the message shall be sent.
     */
    public DelayedMessage(String text, long delay) {
        super(text);
        this.delay = delay;
        timeOfCreation = System.currentTimeMillis();
    }
    
    /**
     * Access the delay after which this message shall be sent.
     * 
     * @return The delay in milli-seconds.
     */
    public long getDelay() {
        return delay;
    }
    
    /**
     * Access the time (in milli-seconds since 01.01.1970) at which this message was created.
     * {@link Connection}s may use this value to evaluate how long to actually wait before sending
     * the message.
     * 
     * @return The time this message was created as a UNIX time stamp.
     */
    public long getTimeOfCreation() {
        return timeOfCreation;
    }
}
