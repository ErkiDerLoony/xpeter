package erki.xpeter.msg;

import erki.xpeter.con.Connection;

/**
 * A message that indicates that some user changed his/her nickname.
 * 
 * @author Edgar Kalkowski
 */
public class NickChangeMessage extends Message {
    
    private String newNick;
    
    /**
     * Create a new NickChangeMessage.
     * 
     * @param oldNick
     *        The old nickname of the user.
     * @param newNick
     *        The new nickname of the user.
     * @param con
     *        The connection associated with this message.
     */
    public NickChangeMessage(String oldNick, String newNick, Connection con) {
        super(oldNick, con);
        this.newNick = newNick;
    }
    
    /** @return The old nickname of the user. */
    public String getOldNick() {
        return getText();
    }
    
    /** @return The new nickname of the user. */
    public String getNewNick() {
        return newNick;
    }
}
