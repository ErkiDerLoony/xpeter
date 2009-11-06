package erki.xpeter.con.erkitalk;

import erki.api.util.Log;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.RawMessage;

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
        
        if (message instanceof RawMessage) {
            encoded = message.getText();
        } else {
            
            if (message.getText().contains("\n")) {
                
                for (String line : message.getText().split("\n")) {
                    encoded = "TEXT " + line + "\n";
                }
                
                encoded = encoded.substring(0, encoded.length() - 1);
            } else {
                encoded = "TEXT " + message.getText();
            }
        }
    }
    
    /**
     * Access the encoded message.
     * 
     * @return The encoded message ready to be sent to an ErkiTalk server.
     */
    public String get() {
        Log.debug("Sending “" + encoded + "” to the server.");
        return encoded;
    }
}
