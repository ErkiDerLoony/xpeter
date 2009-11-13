package erki.xpeter.msg;

import erki.xpeter.con.Connection;

/**
 * A message that indicates that a user left the chat. The text of this message is the leave message
 * of the user that left if he gave any.
 * 
 * @author Edgar Kalkowski
 */
public class UserLeftMessage extends Message {
    
    private String nick;
    
    /**
     * Create a new LeaveMessage.
     * 
     * @param nick
     *        The nickname of the user that left the chat.
     * @param text
     *        The leave message left by the leaving user.
     */
    public UserLeftMessage(String nick, String text, Connection con) {
        super(text, con);
        this.nick = nick;
    }
    
    /**
     * Access the nick of the user that left the chat.
     * 
     * @return The nickname of the user that left the chat.
     */
    public String getNick() {
        return nick;
    }
}
