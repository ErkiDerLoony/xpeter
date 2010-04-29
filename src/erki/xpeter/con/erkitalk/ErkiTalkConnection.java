/*
 * © Copyright 2008–2010 by Edgar Kalkowski <eMail@edgar-kalkowski.de>
 * 
 * This file is part of the chatbot xpeter.
 * 
 * The chatbot xpeter is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package erki.xpeter.con.erkitalk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import erki.api.util.Log;
import erki.xpeter.Bot;
import erki.xpeter.con.Connection;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.Message;
import erki.xpeter.util.Delay;

/**
 * This class implements a connection to an ErkiTalk server. If the connection breaks because the
 * server is no longer reachable or something else happens all messages that shall be sent are
 * buffered until the connection can be re-established.
 * 
 * @author Edgar Kalkowski
 */
public class ErkiTalkConnection implements Connection {
    
    private Bot bot;
    
    private String host, nick;
    
    private int port;
    
    private Queue<Message> sendQueue = new LinkedList<Message>();
    
    private boolean reconnect = false;
    
    private Socket socket;
    
    private ServerInputReader serverInputReader;
    
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
    public void send(final Message msg) {
        
        if (msg instanceof DelayedMessage) {
            
            new Delay((DelayedMessage) msg) {
                
                @Override
                public void delayedAction() {
                    
                    synchronized (sendQueue) {
                        sendQueue.offer(msg);
                        sendQueue.notify();
                    }
                }
                
            }.start();
            
        } else {
            
            synchronized (sendQueue) {
                sendQueue.offer(msg);
                sendQueue.notify();
            }
        }
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
                serverInputReader = new ServerInputReader(bot, this, socketIn);
                serverInputReader.start();
                
                final PrintWriter socketOut = new PrintWriter(new OutputStreamWriter(socket
                        .getOutputStream(), "UTF-8"), true);
                pause = false;
                reconnect = false;
                Log.info("Connection established. Logging in.");
                socketOut.println("PONG");
                socketOut.println("NICK " + nick);
                socketOut.println("LIST");
                socketOut.flush();
                
                while (!reconnect) {
                    
                    synchronized (sendQueue) {
                        
                        while (!sendQueue.isEmpty()) {
                            Message msg = sendQueue.poll();
                            socketOut.println(new MessageEncoder(msg).get());
                            socketOut.flush();
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
            } catch (Throwable e) {
                // See that _everything_ goes to the log.
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
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                    }
                    
                } else {
                    pause = true;
                    Log.info("Lost connection to ErkiTalk server. Trying to reconnect.");
                }
            }
        }
    }
    
    @Override
    public Collection<String> getUserList() {
        return serverInputReader.getUserList();
    }
}
