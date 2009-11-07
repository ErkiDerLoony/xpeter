package erki.xpeter.con.xmpp;

import erki.api.util.Log;

/**
 * The main purpose of this class so far is to detect if the connection is terminated because of
 * some error and trigger a reconnect if that happens.
 * 
 * @author Edgar Kalkowski
 */
public class ConnectionListener implements org.jivesoftware.smack.ConnectionListener {
    
    private XmppConnection con;
    
    public ConnectionListener(XmppConnection con) {
        this.con = con;
    }
    
    @Override
    public void connectionClosed() {
        Log.debug("The connection was closed.");
        con.reconnect();
    }
    
    @Override
    public void connectionClosedOnError(Exception e) {
        Log.debug("The connection was closed due to some error (" + e.getClass().getSimpleName()
                + ").");
        con.reconnect();
    }
    
    @Override
    public void reconnectingIn(int seconds) {
        Log.debug("Reconnecting in " + seconds + " s.");
    }
    
    @Override
    public void reconnectionFailed(Exception e) {
        Log.debug("Reconnection failed (" + e.getClass().getSimpleName() + ").");
        con.reconnect();
    }
    
    @Override
    public void reconnectionSuccessful() {
        Log.debug("Reconnection was successful.");
    }
}
