package erki.xpeter.parsers.statistics;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * This class represents the statistics information and chat history of some user in a chat.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = 3164337442337922753L;
    
    private static final int HISTORY_SIZE = 10;
    
    private final String nick;
    
    private int words, lines;
    
    private LinkedList<HistoryObject> history = new LinkedList<HistoryObject>();
    
    /**
     * Create a new User instance for some user.
     * 
     * @param nick
     *        The nickname of the user. Must not be {@code null}!
     */
    public User(String nick) {
        
        if (nick == null) {
            throw new NullPointerException();
        }
        
        this.nick = nick;
    }
    
    /**
     * Add a line to this user’s statistics.
     * 
     * @param line
     *        The line of text the user said.
     */
    public void addLine(String line) {
        lines++;
        words += line.split(" ").length;
        history.offer(new HistoryObject(line));
        
        while (history.size() > HISTORY_SIZE) {
            history.poll();
        }
    }
    
    /**
     * Access the number of words this user has already said in the chat.
     * 
     * @return The number of words of this user.
     */
    public int getWords() {
        return words;
    }
    
    /**
     * Access the number of lines this user has already said in the chat.
     * 
     * @return The number of lines of this user.
     */
    public int getLines() {
        return lines;
    }
    
    /**
     * Access the chat history of this user.
     * 
     * @return A list that contains the last {@link #HISTORY_SIZE} lines this user said.
     */
    public LinkedList<HistoryObject> getHistory() {
        LinkedList<HistoryObject> result = new LinkedList<HistoryObject>();
        result.addAll(history);
        return result;
    }
    
    /**
     * Access the nickname of the user this statistics object belongs to.
     * 
     * @return
     */
    public String getNick() {
        return nick;
    }
}
