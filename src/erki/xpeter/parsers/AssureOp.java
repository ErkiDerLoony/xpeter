package erki.xpeter.parsers;

import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.con.Connection;
import erki.xpeter.con.irc.IrcConnection;
import erki.xpeter.msg.RawMessage;

/**
 * This parser periodically checks if the bot is alone in the channel and if so checks if it has
 * operator status. If not it reconnects and thus regains operator status.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class AssureOp implements Parser, Observer<RawMessage> {
    
    private Thread thread;
    
    private boolean killed = false;
    
    @Override
    public void init(final Bot bot) {
        bot.register(RawMessage.class, this);
        
        thread = new Thread() {
            
            public void run() {
                
                while (!killed) {
                    
                    for (Connection con : bot.getConnections()) {
                        
                        if (con instanceof IrcConnection) {
                            
                            if (con.getUserList().size() == 1) {
                                Log.debug("Sending raw WHO message.");
                                con.send(new RawMessage("WHO " + con.getNick()));
                            }
                        }
                    }
                    
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        
        thread.start();
    }
    
    @Override
    public void destroy(Bot bot) {
        killed = true;
        thread.interrupt();
        bot.deregister(RawMessage.class, this);
    }
    
    @Override
    public void inform(RawMessage msg) {
        String text = msg.getText();
        
        // 352: testpeter #dsspa ~PircBot p5DC40715.dip0.t-ipconnect.de *.quakenet.org
        // testpeter H :0 PircBot 1.5.0 Java IRC Bot - www.jibble.org
        
        if (text.startsWith("352: ")) {
            text = text.substring("352: ".length());
            text = text.substring(0, text.indexOf(':')).trim();
            
            if (!text.endsWith("@")) {
                msg.respond(new RawMessage("QUIT"));
            }
        }
    }
}
