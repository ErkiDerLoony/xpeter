package erki.xpeter.parsers.feeds;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

import org.gnu.stealthp.rsslib.RSSChannel;
import org.gnu.stealthp.rsslib.RSSException;
import org.gnu.stealthp.rsslib.RSSHandler;
import org.gnu.stealthp.rsslib.RSSItem;
import org.gnu.stealthp.rsslib.RSSParser;

import erki.api.storage.Storage;
import erki.api.util.Log;
import erki.xpeter.Bot;
import erki.xpeter.msg.Message;
import erki.xpeter.util.Keys;
import erki.xpeter.util.StorageKey;

/**
 * This thread periodically checks if there are any new items in some feed. If there are the new
 * items are broadcast through all connections the bot has.
 * 
 * @author Edgar Kalkowski
 */
public class UpdateThread extends Thread {
    
    private TreeMap<String, LinkedList<String>> feeds;
    
    private boolean killed = false;
    
    private Bot bot;
    
    private StorageKey<TreeMap<String, LinkedList<String>>> key;
    
    private Storage<Keys> storage;
    
    /**
     * Create a new UpdateThread.
     * 
     * @param feeds
     *        The feeds that shall be checked.
     * @param bot
     *        The bot instance to whose connections new items shall be broadcast.
     * @param key
     *        The key under which the feeds shall be stored.
     * @param storage
     *        The persistant storage facility to store the feeds to if something changed.
     */
    public UpdateThread(TreeMap<String, LinkedList<String>> feeds, Bot bot,
            StorageKey<TreeMap<String, LinkedList<String>>> key, Storage<Keys> storage) {
        super("RssUpdateThread");
        this.feeds = feeds;
        this.bot = bot;
        this.key = key;
        this.storage = storage;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        super.run();
        
        while (!killed) {
            Set<String> keys = feeds.keySet();
            
            for (String url : keys) {
                RSSHandler handler = new RSSHandler();
                String[] knownItems = feeds.get(url).toArray(new String[0]);
                
                try {
                    RSSParser.parseXmlFile(new URL(url), handler, false);
                    RSSChannel channel = handler.getRSSChannel();
                    
                    for (RSSItem item : (Iterable<RSSItem>) channel.getItems()) {
                        boolean known = false;
                        
                        for (String knownItem : knownItems) {
                            
                            if (knownItem.equals(item.toString())) {
                                known = true;
                                break;
                            }
                        }
                        
                        if (!known) {
                            feeds.get(url).add(item.toString());
                            storage.add(key, feeds);
                            bot.broadcast(new Message("[" + channel.getDescription() + "] "
                                    + item.getTitle() + " (" + item.getLink() + ")"));
                        }
                    }
                    
                    // Clean up old items.
                    for (String oldItem : knownItems) {
                        boolean contained = false;
                        
                        for (RSSItem item : (Iterable<RSSItem>) channel.getItems()) {
                            
                            if (item.toString().equals(oldItem)) {
                                contained = true;
                            }
                        }
                        
                        if (!contained) {
                            feeds.get(url).remove(oldItem);
                        }
                    }
                    
                } catch (RSSException e) {
                    Log.error(e);
                    bot.broadcast(new Message("There is something wrong with this rss here …"));
                } catch (MalformedURLException e) {
                    Log.error(e);
                    bot
                            .broadcast(new Message("There is something wrong with the url " + url
                                    + "!"));
                }
            }
            
            try {
                Thread.sleep(200000);
            } catch (InterruptedException e) {
            }
        }
    }
    
    /** Immediately stop this thread. */
    public void kill() {
        killed = true;
        interrupt();
    }
}
