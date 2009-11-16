package erki.xpeter.con.erkitalk;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import erki.api.util.Log;
import erki.xpeter.Bot;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.NickChangeMessage;
import erki.xpeter.msg.RawMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.msg.UserJoinedMessage;
import erki.xpeter.msg.UserLeftMessage;

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
    
    private Collection<String> userList = new LinkedList<String>();
    
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
                } else if (line.toUpperCase().startsWith("NEWNICK ")) {
                    line = line.substring("NEWNICK ".length());
                    String oldNick = line.substring(0, line.indexOf(':'));
                    String newNick = line.substring(line.indexOf(':') + 2);
                    Log.info(oldNick + " is now known as " + newNick + ".");
                    
                    synchronized (userList) {
                        userList.remove(oldNick);
                        userList.add(newNick);
                    }
                    
                    bot.process(new NickChangeMessage(oldNick, newNick, con));
                } else if (line.toUpperCase().startsWith("QUIT ")) {
                    line = line.substring("QUIT ".length());
                    String nick, reason = "";
                    
                    if (line.contains(":")) {
                        nick = line.substring(0, line.indexOf(':'));
                        reason = line.substring(line.indexOf(':') + 2);
                    } else {
                        nick = line;
                    }
                    
                    synchronized (userList) {
                        userList.remove(nick);
                    }
                    
                    bot.process(new UserLeftMessage(nick, reason, con));
                } else if (line.toUpperCase().startsWith("JOIN ")) {
                    line = line.substring("JOIN ".length());
                    
                    synchronized (userList) {
                        userList.add(line);
                    }
                    
                    bot.process(new UserJoinedMessage(line, con));
                } else if (line.toUpperCase().startsWith("USER ")) {
                    line = line.substring("USER XX ".length());
                    
                    synchronized (userList) {
                        userList.add(line);
                    }
                    
                    bot.process(new UserJoinedMessage(line, con));
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
    
    /**
     * Access the currently online users of this chat. The returned Collection is copied so no harm
     * can be done editing it.
     * 
     * @return A Collection of currently online users of this chat.
     */
    public Collection<String> getUserList() {
        LinkedList<String> users = new LinkedList<String>();
        
        synchronized (userList) {
            users.addAll(userList);
        }
        
        return users;
    }
}
