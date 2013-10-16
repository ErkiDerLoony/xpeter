package erki.xpeter.parsers.help;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import erki.api.util.Log;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.Parser;
import erki.xpeter.util.BotApi;
import erki.xpeter.util.ParserFinder;

/**
 * This action provides a general help message for the bot that lists all available parsers.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class HelpAction extends Action<TextMessage> {
    
    /** Create a new HelpAction. */
    public HelpAction() {
        super(TextMessage.class, true);
    }
    
    @Override
    public String getRegex() {
        return "([hH][ieIE][lL][pPfF][Ee]?|[wW]elche [pP]arser gibt es|[wW]as f(ue|ü)r [pP]arser "
                + "(kennst|hast) [dD]u|[lL]ist [pP]arsers|[wW]as kannst [dD]u( eigentlich)?( "
                + "so)?)[\\.!\\?]*";
    }
    
    @Override
    public String getDescription() {
        return "Gibt eine Liste aller Parser aus. Aktuell geladene Parser sind mit (*) markiert.";
    }
    
    @Override
    public void execute(String[] args, TextMessage msg) {
        String response = "Ich kenne folgende Parser: ";
        Set<Parser> loadedParsers = getBot().getParsers();
        HashSet<Class<? extends Parser>> loadedClasses = new HashSet<>();
        
        for (Parser p : loadedParsers) {
            loadedClasses.add(p.getClass());
        }
        
        TreeSet<Class<? extends Parser>> foundParsers = ParserFinder.findParsers(BotApi
                .getParserDir());
        Log.debug("Loaded: " + loadedParsers.toString());
        Log.debug("Found: " + foundParsers.toString());
        
        for (Class<? extends Parser> foundParser : foundParsers) {
            
            if (loadedClasses.contains(foundParser)) {
                response += foundParser.getSimpleName() + " (*), ";
            } else {
                response += foundParser.getSimpleName() + ", ";
            }
        }
        
        response = response.substring(0, response.length() - 2) + ".\n";
        response += "Um mehr über den Parser <parser> zu erfahren, sende z.B. „hilfe <parser>“!";
        msg.respond(new DelayedMessage(response, 2000));
    }
}
