package erki.xpeter.parsers.help;

import java.util.List;
import java.util.Set;

import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.parsers.Action;
import erki.xpeter.parsers.Parser;
import erki.xpeter.parsers.SuperParser;

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
        return "Gibt detaillierte Informationen über einen aktuell geladenen Parser aus.";
    }
    
    @Override
    public void execute(String[] args, TextMessage message) {
        Set<Parser> parsers = getBot().getParsers();
        String suggestion = null;
        boolean found = false;
        
        for (Parser parser : parsers) {
            
            if (parser.getClass().getSimpleName().equals(args[0])) {
                found = true;
                String description = null;
                
                if (parser instanceof SuperParser) {
                    SuperParser p = (SuperParser) parser;
                    description = args[0] + ": " + p.getDescription()
                            + "\nDieser Parser versteht folgende Ausdrücke:\n";
                    List<Action<? extends Message>> actions = p.getActions();
                    
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
                
                message.respond(new DelayedMessage(description, 2500));
            } else if (parser.getClass().getSimpleName().toLowerCase()
                    .equals(args[0].toLowerCase())) {
                suggestion = parser.getClass().getSimpleName();
            }
        }
        
        if (!found) {
            message.respond(new DelayedMessage("Ein Parser mit dem Namen „" + args[0]
                    + "“ ist leider aktuell nicht geladen.", 1500));
            
            if (suggestion != null) {
                message.respond(new DelayedMessage("Meintest du vielleicht den Parser „"
                        + suggestion + "“?", 2500));
            }
        }
    }
}
