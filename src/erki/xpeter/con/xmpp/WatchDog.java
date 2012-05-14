package erki.xpeter.con.xmpp;

/**
 * A watch dog thread that assures the bot stays connected and has joined it’s chat room on XMPP
 * connections (because either jabber.org or jabber.exados.com is buggy). If the thread is not reset
 * via {@link #reset()} every now and then it will cause a reconnect after {@value #TIMEOUT}
 * milliseconds.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class WatchDog extends Thread {
    
    private static final int TIMEOUT = 24 * 60 * 60 * 1000;
    
    private XmppConnection con;
    
    private boolean reset = false;
    
    /**
     * Create a new WatchDog thread for some connection.
     * 
     * @param con
     *        The connection to reconnect if nothing happens for more than {@value #TIMEOUT}
     *        milliseconds.
     */
    public WatchDog(XmppConnection con) {
        this.con = con;
    }
    
    @Override
    public void run() {
        super.run();
        
        while (true) {
            
            try {
                Thread.sleep(TIMEOUT);
            } catch (InterruptedException e) {
            }
            
            if (!reset) {
                con.reconnect();
            } else {
                reset = false;
            }
        }
    }
    
    /**
     * Reset the countdown of this watch dog. This should be called every time something is received
     * from the XMPP server that indicates that the bot is still connected to it’s chat.
     */
    public void reset() {
        reset = true;
        interrupt();
    }
}
