package erki.xpeter.con.erkitalk;

import erki.xpeter.msg.Message;

/**
 * Encoded instances of {@link Message} (or of subclasses) to a raw string that can be sent to an
 * ErkiTalk server.
 * 
 * @author Edgar Kalkowski
 */
public class MessageEncoder {
    
    private String encoded;
    
    /**
     * Create a new MessageEncoder for a specific message.
     * 
     * @param message
     *        The message to encode.
     */
    public MessageEncoder(Message message) {
        encoded = "TEXT " + message.getText();
    }
    
    /**
     * Access the encoded message.
     * 
     * @return The encoded message ready to be sent to an ErkiTalk server.
     */
    public String get() {
        return encoded;
    }
}
