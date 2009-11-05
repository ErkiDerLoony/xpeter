package erki.xpeter.con.erkitalk;

import java.io.BufferedReader;
import java.io.IOException;

import erki.api.util.Log;
import erki.xpeter.Bot;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.RawMessage;
import erki.xpeter.msg.TextMessage;

/**
 * Processes input from an ErkiTalk server and parses it into the bot’s internal {@link Message}
 * instances.
 * 
 * @author Edgar Kalkowski
 */
public class ServerInputReader extends Thread {
    
    private Bot bot;
    
    private BufferedReader socketIn;
    
    private ErkiTalkConnection con;
    
    public ServerInputReader(Bot bot, ErkiTalkConnection con, BufferedReader socketIn) {
        this.bot = bot;
        this.con = con;
        this.socketIn = socketIn;
    }
    
    @Override
    public void run() {
        super.run();
        
        while (true) {
            
            try {
                String line = socketIn.readLine();
                
                if (line == null) {
                    con.reconnect();
                    break;
                }
                
                Log.debug("Received “" + line + "” from server.");
                
                if (line.toUpperCase().startsWith("TEXT ")) {
                    line = line.substring("TEXT ".length());
                    String nick = line.substring(0, line.indexOf(':'));
                    String text = line.substring(line.indexOf(':') + 2);
                    TextMessage msg = new TextMessage(nick, text, con);
                    Log.info("Received " + msg + ".");
                    bot.process(msg);
                } else if (line.toUpperCase().equals("PING")) {
                    con.send(new RawMessage("PONG"));
                } else {
                    Log.warning("Unparsable message received: “" + line + "”.");
                }
                
            } catch (IOException e) {
                con.reconnect();
                break;
            }
        }
    }
}
