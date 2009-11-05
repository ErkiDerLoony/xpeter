package erki.xpeter.msg;

/**
 * This class wraps a raw line of text that shall be sent to the server as is.
 * 
 * @author Edgar Kalkowski
 */
public class RawMessage extends Message {
    
    /**
     * Create a new RawMessage.
     * 
     * @param text
     *        The text to send to the server (unchainged).
     */
    public RawMessage(String text) {
        super(text);
    }
}
