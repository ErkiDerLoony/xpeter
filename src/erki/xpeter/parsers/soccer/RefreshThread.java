package erki.xpeter.parsers.soccer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import erki.api.util.Log;
import erki.xpeter.Bot;
import erki.xpeter.msg.Message;
import erki.xpeter.util.BotApi;

public class RefreshThread extends Thread {
    
    private boolean killed = false;
    
    private final Bot bot;
    
    private Soccer soccerParser;
    
    private final String host, query;
    
    private LinkedList<String> knownElements = null;
    
    public RefreshThread(Bot bot, Soccer soccerParser, String host, String query) {
        this.bot = bot;
        this.soccerParser = soccerParser;
        this.host = host;
        this.query = query;
    }
    
    private String convert(String text) {
        text = text.replaceAll("<br ?/>", "");
        text = text.replaceAll("<img src=.*?/>", "");
        return text.trim();
    }
    
    private LinkedList<String> getElements(String website) {
        LinkedList<String> result = new LinkedList<String>();
        String start = "<span class=\"ardTTUhr\">";
        String titleMatch = "<title>(.*?) : (.*?)  ([0-9]*) : ([0-9]*) .*</title>";
        String prefix = "";
        
        if (website.matches(titleMatch)) {
            String home = website.replaceAll(titleMatch, "$1");
            String guest = website.replaceAll(titleMatch, "$2");
            String homeG = website.replaceAll(titleMatch, "$3");
            String guestG = website.replaceAll(titleMatch, "$4");
            prefix = home + " " + homeG + " : " + guestG + " " + guest;
        }
        
        while (website.contains(start)) {
            website = website.substring(website.indexOf(start) + start.length());
            String time = website.substring(0, website.indexOf("</span>"));
            website = website.substring(website.indexOf("</span>") + "</span>".length());
            String text = website.substring(0, website.indexOf("</p>"));
            website = website.substring(website.indexOf("</p>") + "</p>".length());
            result.add("(" + prefix + ") " + time + ": " + convert(text));
        }
        
        return result;
    }
    
    @Override
    public void run() {
        super.run();
        
        while (!killed) {
            
            try {
                String website = BotApi.getWebsite(host, query, "ISO-8859-1");
                Log.finest(website);
                
                if (website.startsWith("Fehler: ")) {
                    Log.fine("This game (" + host + query + ") seems finished.");
                    soccerParser.finished(host, query);
                    return;
                }
                
                LinkedList<String> sendQueue = new LinkedList<String>();
                
                if (knownElements == null) {
                    Log.fine("Reading initial elements.");
                    knownElements = new LinkedList<String>(getElements(website));
                    Log.finest("Initial elements are now: " + knownElements);
                } else {
                    
                    for (String element : getElements(website)) {
                        Log.fine("Processing element “" + element + "”.");
                        
                        if (!knownElements.contains(element)) {
                            Log.fine("It is unknown so far.");
                            knownElements.add(element);
                            sendQueue.addLast(element);
                        }
                    }
                    
                    if (!sendQueue.isEmpty()) {
                        Log.fine("Processing send queue.");
                        int i = 0;
                        
                        for (String element : sendQueue) {
                            
                            if (i > 2) {
                                break;
                            } else {
                                Log.fine("Sending “" + element + "”.");
                                bot.broadcast(new Message(element));
                            }
                            
                            i++;
                        }
                    }
                }
                
            } catch (UnknownHostException e1) {
                Log.error(e1);
                Log.info("It might be that this error is not fatal. So let’s move on.");
            } catch (IOException e1) {
                Log.error(e1);
                Log.info("It might be that this error is not fatal. So let’s move on.");
            }
            
            synchronized (this) {
                Log.fine("Waiting a few seconds until the next check.");
                
                try {
                    wait(30000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
    
    public synchronized void kill() {
        this.killed = true;
        notify();
    }
}
