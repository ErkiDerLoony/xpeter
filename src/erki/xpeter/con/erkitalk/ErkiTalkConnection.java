package erki.xpeter.con.erkitalk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import erki.api.util.Log;
import erki.xpeter.Bot;
import erki.xpeter.con.Connection;
import erki.xpeter.msg.Message;

public class ErkiTalkConnection implements Connection {
    
    private Bot bot;
    
    private String host, nick;
    
    private int port;
    
    private Queue<Message> sendQueue = new LinkedList<Message>();
    
    private boolean reconnect = false;
    
    private Socket socket;
    
    public ErkiTalkConnection(Bot bot, String host, int port, String nick) {
        this.bot = bot;
        this.host = host;
        this.port = port;
        this.nick = nick;
    }
    
    @Override
    public String getNick() {
        return nick;
    }
    
    @Override
    public String getShortId() {
        return "ErkiTalk";
    }
    
    @Override
    public void send(Message msg) {
        
        synchronized (sendQueue) {
            sendQueue.offer(msg);
            sendQueue.notify();
        }
    }
    
    @Override
    public void send(String msg) {
        send(new Message(msg, this));
    }
    
    /**
     * Used by {@link ServerInputReader} to trigger a reconnect if the connection was lost and thus
     * no more input could be read from the server.
     */
    public void reconnect() {
        
        synchronized (sendQueue) {
            reconnect = true;
            sendQueue.notify();
        }
    }
    
    @Override
    public void run() {
        boolean pause = false;
        
        while (true) {
            
            try {
                Log.info("Trying to connect to " + host + ":" + port + ".");
                socket = new Socket(host, port);
                
                BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket
                        .getInputStream(), "UTF-8"));
                ServerInputReader inputReader = new ServerInputReader(bot, this, socketIn);
                inputReader.start();
                
                PrintWriter socketOut = new PrintWriter(new OutputStreamWriter(socket
                        .getOutputStream(), "UTF-8"), true);
                pause = false;
                Log.info("Connection established. Logging in.");
                socketOut.println("PONG");
                socketOut.println("NICK " + nick);
                
                while (!reconnect) {
                    
                    synchronized (sendQueue) {
                        
                        while (!sendQueue.isEmpty()) {
                            socketOut.println(new MessageEncoder(sendQueue.poll()).get());
                        }
                        
                        try {
                            sendQueue.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
                
            } catch (UnknownHostException e) {
                Log.error(e);
            } catch (IOException e) {
                Log.error(e);
            } finally {
                
                if (socket != null) {
                    
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                }
                
                if (pause) {
                    Log.info("Lost connection to ErkiTalk server. Trying to reconnect in 5 min.");
                    
                    try {
                        Thread.sleep(300000);
                    } catch (InterruptedException e) {
                    }
                    
                } else {
                    pause = true;
                    Log.info("Lost connection to ErkiTalk server. Trying to reconnect.");
                }
            }
        }
    }
}
