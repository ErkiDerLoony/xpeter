package erki.xpeter.msg;

import erki.xpeter.con.Connection;

/**
 * This message indicates that some user joined the chat.
 * 
 * @author Edgar Kalkowski
 */
public class UserJoinedMessage extends Message {
    
    /**
     * Create a new UserJoinedMessage.
     * 
     * @param nick
     *        The nickname of the user who joined the chat.
     * @param con
     *        The connection this message belongs to.
     */
    public UserJoinedMessage(String nick, Connection con) {
        super(nick, con);
    }
    
    /**
     * Access the nickname of the user who joined the chat.
     * 
     * @return The nickname of the user who joined the chat.
     */
    public String getNick() {
        return getText();
    }
}
