package erki.xpeter.util;

import erki.api.storage.Key;

/**
 * These keys can be used to store something into a persistant storage facility.
 * 
 * @author Edgar Kalkowski
 * @param <T>
 *        The type of data that shall be stored.
 */
public class StorageKey<T> extends Key<T, Keys> {
    
    private static final long serialVersionUID = -7769693426465537798L;
    
    /**
     * Create a new StorageKey.
     * 
     * @param id
     *        The unique identifier for the data item to store.
     */
    public StorageKey(Keys id) {
        super(id);
    }
}
