package erki.xpeter.parsers.rss;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import org.gnu.stealthp.rsslib.RSSItem;

/**
 * This class wraps all the information available about one rss feed. Especially it stores all items
 * of the feed the bot has already broadcast once. This class does not store the {@link RSSItem}s
 * directly (because that class is not serializable) but rather uses a string representation. So be
 * careful to always use the same string representation, for example {@link RSSItem#toString()}!
 * 
 * @author Edgar Kalkowski
 */
public class FeedData implements Serializable {
    
    private static final long serialVersionUID = -6001142169582413565L;
    
    private final String url;
    
    private final LinkedList<String> knownItems;
    
    private String title, description;
    
    private boolean verbose = false;
    
    /**
     * Create a new FeedData instance.
     * 
     * @param title
     *        The title of this feed.
     * @param description
     *        The description of this feed.
     * @param url
     *        The url of this feed. Must not be {@code null}!
     * @param knownItems
     *        The items that are initially marked as already known. Must not be {@code null} (give
     *        an empty list if no items shall be marked as already known initially)!
     */
    public FeedData(String title, String description, String url, Collection<String> knownItems) {
        this.url = url;
        this.title = title;
        this.knownItems = new LinkedList<String>(knownItems);
    }
    
    /**
     * Access the url of this feed.
     * 
     * @return A string representation of the url of this feed.
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Access the title of this feed.
     * 
     * @return The title of this feed.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Change the title of this feed.
     * 
     * @param title
     *        The new title.
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Access the description of this feed.
     * 
     * @return The description.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Change the description of this feed.
     * 
     * @param description
     *        The new description.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Add an item to the list of already known items.
     * 
     * @param item
     *        The item to add. Must not be {@code null}!
     */
    public void add(String item) {
        knownItems.add(item);
    }
    
    /**
     * Check if a rss item is already known to this feed.
     * 
     * @param item
     *        A string representation of the item to check.
     * @return {@code true} if the given item is already known or {@code false} otherwise.
     */
    public boolean isKnown(String item) {
        return knownItems.contains(item);
    }
    
    /**
     * Clean up the known items of this feed.
     * 
     * @param items
     *        All items that are not contained in this collection are deleted from the list of known
     *        items.
     */
    public void cleanup(Collection<? extends String> items) {
        String[] knownItems = this.knownItems.toArray(new String[0]);
        
        for (String item : knownItems) {
            
            if (!items.contains(item)) {
                this.knownItems.remove(item);
            }
        }
    }
    
    /**
     * Check if verbose information shall be printed for this feed.
     * 
     * @return {@code true} if verbose information shall be printed, {@code false} otherwise.
     */
    public boolean isVerbose() {
        return verbose;
    }
    
    /**
     * Change whether or not verbose information shall be printed for this feed or not.
     * 
     * @param verbose
     *        If {@code true} is given verbose information shall be printed from now on, if {@code
     *        false} is verbose information shall no longer be printed.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
