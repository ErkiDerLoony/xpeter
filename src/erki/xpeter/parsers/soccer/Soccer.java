package erki.xpeter.parsers.soccer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.TreeMap;

import erki.api.storage.Storage;
import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Parser;
import erki.xpeter.util.BotApi;
import erki.xpeter.util.Keys;
import erki.xpeter.util.StorageKey;

public class Soccer implements Parser, Observer<TextMessage> {
    
    private TreeMap<String, RefreshThread> threads = new TreeMap<String, RefreshThread>();
    
    private DetectorThread detectorThread;
    
    private Bot bot;
    
    @Override
    public void init(Bot bot) {
        this.bot = bot;
        detectorThread = new DetectorThread(this,
                "http://www.sportschau.de/sp/layout/php/ticker/index.phtml?tid=");
        detectorThread.start();
        bot.register(TextMessage.class, this);
        Storage<Keys> storage = bot.getStorage();
        
        if (storage.contains(new StorageKey<LinkedList<String>>(Keys.SOCCER_THREADS))) {
            Log.info("Trying to load soccer thread information from the storage database.");
            int counter = 0;
            
            for (String url : storage.get(new StorageKey<LinkedList<String>>(Keys.SOCCER_THREADS))) {
                RefreshThread thread = new RefreshThread(bot, this, getHost(url), getQuery(url));
                threads.put(url, thread);
                thread.start();
                counter++;
            }
            
            if (counter == 1) {
                Log.info("One soccer thread loaded.");
            } else {
                Log.info(counter + " soccer threads loaded.");
            }
            
        } else {
            Log.info("No stored soccer threads found.");
        }
    }
    
    String getHost(String url) {
        url = url.startsWith("http://") ? url.substring("http://".length()) : url;
        
        if (url.contains("/")) {
            return url.substring(0, url.indexOf('/'));
        } else {
            return url;
        }
    }
    
    String getQuery(String url) {
        url = url.startsWith("http://") ? url.substring("http://".length()) : url;
        
        if (url.contains("/")) {
            return url.substring(url.indexOf('/'));
        } else {
            return "";
        }
    }
    
    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
        
        for (RefreshThread thread : threads.values()) {
            thread.kill();
        }
        
        store();
        threads.clear();
        detectorThread.kill();
    }
    
    private void store() {
        LinkedList<String> threads = new LinkedList<String>();
        String[] threadsArray = this.threads.keySet().toArray(new String[0]);
        
        for (String url : threadsArray) {
            threads.add(url);
        }
        
        bot.getStorage().add(new StorageKey<LinkedList<String>>(Keys.SOCCER_THREADS), threads);
    }
    
    /**
     * This method is called by {@link RefreshThread} to indicate that the thread is finished and
     * can be removed from the list of active threads.
     * 
     * @param host
     *        The hostname of the finished thread.
     * @param query
     *        The query part of the url of the finished thread.
     */
    public void finished(String host, String query) {
        
        if (threads.containsKey(host + query)) {
            threads.remove(host + query);
            store();
        } else {
            Log.warning("No thread with id “" + host + query + "” found!");
        }
    }
    
    public void add(String url) {
        RefreshThread thread = new RefreshThread(bot, this, getHost(url), getQuery(url));
        threads.put(url, thread);
        store();
        thread.start();
    }
    
    @Override
    public void inform(TextMessage msg) {
        
        if (!BotApi.addresses(msg.getText(), msg.getBotNick())) {
            return;
        }
        
        String text = BotApi.trimNick(msg.getText(), msg.getBotNick());
        String match = "([fF]olge dem [sS]piel|[vV]erfolge das [sS]piel) (.*)";
        
        if (text.matches(match)) {
            String url = text.replaceAll(match, "$2");
            Log.debug("Recognized soccer url “" + url + "”.");
            
            if (threads.containsKey(url)) {
                msg.respond(new DelayedMessage("Das tu ich doch schon!", 1500));
            } else {
                
                try {
                    
                    if (BotApi.getWebsite(getHost(url), getQuery(url), "ISO-8859-1").contains(
                            "<div id=\"ardTickerTableau\">")) {
                        add(url);
                        msg.respond(new DelayedMessage("Ok.", 1500));
                    } else {
                        msg.respond(new DelayedMessage("Das scheint mir kein gültiger "
                                + "sportschau.de Ticker zu sein!", 1500));
                    }
                    
                } catch (UnknownHostException e) {
                    Log.error(e);
                    Log.info("It seems someone gave an malformed url.");
                    msg.respond(new DelayedMessage("Unter der URL kann ich nichts finden.", 1500));
                } catch (IOException e) {
                    Log.error(e);
                    Log.info("It seems someone gave an malformed url.");
                    msg.respond(new DelayedMessage("Unter der URL kann ich nichts finden.", 1500));
                }
            }
        }
        
        match = "([Ww]elchen [sS]pielen folgst|([Ww]elche|[wW]as f(ue|ü)r) [Ss]piele verfolgst) "
                + "du( gerade)?\\??";
        
        if (text.matches(match)) {
            
            if (threads.isEmpty()) {
                msg.respond(new DelayedMessage("Ich verfolge gerade gar kein Spiel.", 1500));
            } else if (threads.keySet().size() == 1) {
                msg.respond(new DelayedMessage("Ich verfolge gerade das Spiel "
                        + threads.keySet().iterator().next() + ".", 2000));
            } else {
                String result = "Ich verfolge gerade die Spiele:";
                
                for (String url : threads.keySet()) {
                    result += "\n– " + url;
                }
                
                msg.respond(new DelayedMessage(result, 2500));
            }
        }
        
        match = "[iI]gnoriere das [sS]piel (.*)";
        
        if (text.matches(match)) {
            String url = text.replaceAll(match, "$1");
            
            if (threads.containsKey(url)) {
                threads.get(url).kill();
                threads.remove(url);
                store();
                msg.respond(new DelayedMessage("Ok.", 1500));
            } else {
                msg.respond(new DelayedMessage("Das Spiel „" + url + "“ verfolge ich gar nicht!",
                        1500));
            }
        }
    }
}
