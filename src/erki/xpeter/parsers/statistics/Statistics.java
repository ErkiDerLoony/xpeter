package erki.xpeter.parsers.statistics;

import java.util.TreeMap;

import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Parser;

public class Statistics implements Parser, Observer<TextMessage> {
    
    private TreeMap<String, User> mapping;
    
    @Override
    public void init(Bot bot) {
        bot.register(TextMessage.class, this);
    }

    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
    }

    @Override
    public void inform(TextMessage msg) {
        
        if (!mapping.containsKey(msg.getNick())) {
            mapping.put(msg.getNick(), new User(msg.getNick()));
        }
        
        mapping.get(msg.getNick()).addLine(msg.getText());
        
        String match = "[tT]op ?([0-9][0-9]?)";
        
        if (msg.getText().matches(match)) {
            int number = 0;
            
            try {
                number = Integer.parseInt(msg.getText().replaceAll(match, "$1"));
            } catch (NumberFormatException e) {
            }
            
            if (number > 0) {
                
            }
        }
    }
}
