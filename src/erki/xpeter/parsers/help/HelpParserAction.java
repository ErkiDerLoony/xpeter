package erki.xpeter.parsers.help;

import java.util.List;
import java.util.TreeSet;

import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.Parser;
import erki.xpeter.parsers.SuperParser;
import erki.xpeter.util.BotApi;
import erki.xpeter.util.ParserFinder;

/**
 * This action provides help about a specific parser.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class HelpParserAction extends Action<TextMessage> {
    
    /** Create a new HelpParserAction. */
    public HelpParserAction() {
        super(TextMessage.class, true);
    }
    
    @Override
    public String getRegex() {
        return "[hH][eEiI][lL][pPfF][eE]? (.*)";
    }
    
    @Override
    public String getDescription() {
        return "Gibt detaillierte Informationen über einen speziellen Parser aus (sofern vorhanden).";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        TreeSet<Class<? extends Parser>> foundParsers = ParserFinder.findParsers(BotApi
                .getParserDir());
        TreeSet<Class<? extends Parser>> loadedParsers = getBot().getParsers();
        String suggestion = null;
        boolean found = false;
        
        for (Class<? extends Parser> parser : foundParsers) {
            
            if (parser.getSimpleName().equals(args[0])) {
                found = true;
                String description = null;
                
                try {
                    Parser instance = parser.newInstance();
                    
                    if (instance instanceof SuperParser) {
                        description = ((SuperParser) instance).getDescription();
                        description = args[0]
                                + (loadedParsers.contains(parser) ? " (aktuell geladen)" : "")
                                + ": " + description
                                + "\nDieser Parser versteht folgende Ausdrücke:\n";
                        List<Action<? extends Message>> actions = ((SuperParser) instance)
                                .getActions();
                        
                        for (int i = 0; i < actions.size(); i++) {
                            Action<? extends Message> action = actions.get(i);
                            
                            description += action.getRegex() + "\n   " + action.getDescription();
                            
                            if (i < actions.size() - 1) {
                                description += "\n";
                            }
                        }
                        
                    } else {
                        description = "Über den Parser „" + args[0]
                                + "“ sind leider keine weiteren Informationen vorhanden.";
                    }
                    
                } catch (InstantiationException e) {
                    description = "Über den Parser „" + args[0]
                            + "“ konnten leider keine weiteren Informationen abgerufen werden.";
                } catch (IllegalAccessException e) {
                    description = "Über den Parser „" + args[0]
                            + "“ konnten leider keine weiteren Informationen abgerufen werden.";
                }
                
                message.respond(new DelayedMessage(description, 2500));
            } else if (parser.getSimpleName().toLowerCase().equals(args[0].toLowerCase())) {
                suggestion = parser.getSimpleName();
            }
        }
        
        if (!found) {
            message.respond(new DelayedMessage("Ein Parser mit dem Namen „" + args[0]
                    + "“ konnte leider nicht gefunden werden.", 1500));
            
            if (suggestion != null) {
                message.respond(new DelayedMessage("Meintest du vielleicht den Parser „"
                        + suggestion + "“?", 2500));
            }
        }
    }
}
