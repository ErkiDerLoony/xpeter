package erki.xpeter.parsers.soccer;

import erki.xpeter.Bot;
import erki.xpeter.parsers.Parser;

public class WM implements Parser {
    
    private WmThread thread;
    
    @Override
    public void init(Bot bot) {
        thread = new WmThread(bot);
        thread.start();
    }
    
    @Override
    public void destroy(Bot bot) {
        thread.kill();
    }
}
