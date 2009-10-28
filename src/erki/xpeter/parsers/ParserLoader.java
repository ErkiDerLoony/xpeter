package erki.xpeter.parsers;

import java.util.TreeSet;

import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.BotApi;
import erki.xpeter.ParserFinder;
import erki.xpeter.con.Connection;
import erki.xpeter.msg.TextMessage;

/**
 * This parser manages the loading and unloading of parsers at runtime.
 * 
 * @author Edgar Kalkowski
 */
public class ParserLoader implements Parser, Observer<TextMessage> {
    
    private Bot bot;
    
    @Override
    public void init(Bot bot) {
        Log.debug("Initializing.");
        bot.register(TextMessage.class, this);
        this.bot = bot;
    }
    
    @Override
    public void destroy(Bot bot) {
        Log.debug("Destroying.");
        bot.deregister(TextMessage.class, this);
    }
    
    @Override
    public void inform(TextMessage msg) {
        Log.debug("Informed of " + msg + ".");
        Connection con = msg.getConnection();
        String text = msg.getText();
        
        if (!BotApi.addresses(text, con.getNick())) {
            return;
        }
        
        text = BotApi.trimNick(text, con.getNick());
        
        if (text.matches("[wW]elche [pP]arser gibt es\\.?\\??")
                || text.matches("[wW]as f(ü|ue)r [Pp]arser gibt es\\.?\\??")
                || text.matches("[Ww]elche [pP]arser (kennst|hast) [Dd]u\\.?\\??")) {
            String response = "Ich kenne folgende Parser: ";
            TreeSet<Class<? extends Parser>> loadedParsers = bot.getParsers();
            TreeSet<Class<? extends Parser>> foundParsers = ParserFinder.findParsers(BotApi
                    .getParserDir());
            Log.debug("Loaded: " + loadedParsers.toString());
            Log.debug("Found: " + foundParsers.toString());
            
            for (Class<? extends Parser> foundParser : foundParsers) {
                
                if (loadedParsers.contains(foundParser)) {
                    response += foundParser.getSimpleName() + " (*), ";
                } else {
                    response += foundParser.getSimpleName() + ", ";
                }
            }
            
            con.send(response.substring(0, response.length() - 2) + ".");
        }
        
        String match = "([Ll]ade|[Ll]oad) (.*?)";
        
        if (text.matches(match)) {
            String parser = text.replaceAll(match, "$2");
            
            if (parser.endsWith(".class")) {
                parser = parser.substring(0, parser.length() - ".class".length());
            }
            
            if (parser.endsWith("!") || parser.endsWith(".")) {
                parser = parser.substring(0, parser.length() - 1);
            }
            
            Log.debug("Recognized match for parser " + parser + ".");
            
            TreeSet<Class<? extends Parser>> foundParsers = ParserFinder.findParsers(BotApi
                    .getParserDir());
            boolean added = false;
            
            for (Class<? extends Parser> clazz : foundParsers) {
                
                if (clazz.getSimpleName().equals(parser)) {
                    bot.add(clazz);
                    added = true;
                }
            }
            
            if (added) {
                con.send("Ok.");
            } else {
                con.send("Ein Parser mit dem Namen " + parser + " wurde nicht gefunden!");
            }
        }
        
        match = "([Ee]ntlade|[uU]nload) (.*?)";
        
        if (text.matches(match)) {
            String parser = text.replaceAll(match, "$2");
            
            if (parser.endsWith(".class")) {
                parser = parser.substring(0, parser.length() - ".class".length());
            }
            
            if (parser.endsWith("!") || parser.endsWith(".")) {
                parser = parser.substring(0, parser.length() - 1);
            }
            
            Log.debug("Recognized match for parser " + parser + ".");
            
            if (parser.equals("ParserLoader")) {
                con.send("Der ParserLoader kann nicht entfernt werden!");
            } else {
                TreeSet<Class<? extends Parser>> parsers = bot.getParsers();
                boolean removed = false;
                
                for (Class<? extends Parser> clazz : parsers) {
                    Log.debug("Checking " + clazz.getSimpleName() + " against " + parser);
                    
                    if (clazz.getSimpleName().equals(parser)) {
                        Log.debug("Match!");
                        bot.remove(clazz);
                        removed = true;
                    }
                }
                
                if (removed) {
                    con.send("Ok.");
                } else {
                    con.send("Der Parser " + parser
                            + " ist entweder nicht geladen oder er existiert gar nicht!");
                }
            }
        }
    }
}
