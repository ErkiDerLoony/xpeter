package erki.xpeter.parsers.help;

import erki.xpeter.BotInterface;
import erki.xpeter.parsers.SuperParser;

/**
 * This parser implements a generic help system for all parsers deriving from {@link SuperParser}.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class Help extends SuperParser {
    
    @Override
    public void createActions(BotInterface bot) {
        actions.add(new HelpAction());
        actions.add(new HelpParserAction());
    }
    
    @Override
    public String getDescription() {
        return "Bietet hilfreiche Informationen über Parser, die das unterstützen";
    }
}
