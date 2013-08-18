package erki.xpeter.parsers.mensa;

import erki.xpeter.BotInterface;
import erki.xpeter.parsers.SuperParser;

/**
 * Queries information from the web which meals are served at the Mensa of University of Kassel.
 * 
 * @author Edgar Kalkowski <edgar.kalkowski@uni-kassel.de>
 */
public class Mensa extends SuperParser {
    
    @Override
    public void createActions(final BotInterface bot) {
        actions.add(new AddFavourite(bot.getStorage()));
    }
    
    @Override
    public String getDescription() {
        return "Find out what is served at the Mensa of University of Kassel and remind users of "
                + "feeding time.";
    }
}
