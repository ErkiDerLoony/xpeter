package erki.xpeter.parsers.statistics;

import java.util.Date;

/**
 * Immutable object that encapsulates timestamp and content of a message someone said to a chat.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class HistoryObject {
    
    private final Date time;
    
    private final String line;
    
    /**
     * Create a new HistoryObject for some message.
     * 
     * @param line
     *        The content of the message. Must not be {@code null}!
     */
    public HistoryObject(String line) {
        
        if (line == null) {
            throw new NullPointerException();
        }
        
        this.time = new Date();
        this.line = line;
    }
    
    /**
     * Access the timestamp this message was received.
     * 
     * @return A deep copy of the timestamp this message was received.
     */
    public Date getTime() {
        return new Date(time.getTime());
    }
    
    /**
     * Access the content of this message.
     * 
     * @return A deep copy of the content of this message.
     */
    public String getLine() {
        return line;
    }
}
