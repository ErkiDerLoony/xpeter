package erki.xpeter.parsers;

import java.text.NumberFormat;
import java.util.Calendar;

import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.TextMessage;

public class Weihnachten implements Parser, Observer<TextMessage> {
    
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
        String text = msg.getText();
        
        if (text.contains("Weihnachtsbaum") || text.contains("Weihnachten")
                || text.contains("weihnachten") || text.contains("weihnachtsbaum")) {
            msg.respond(new DelayedMessage(getTree(), 3000));
        }
    }
    
    private String getTree() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(3);
        nf.setMaximumIntegerDigits(3);
        Calendar xmas = Calendar.getInstance();
        xmas.set(Calendar.MONTH, Calendar.DECEMBER);
        xmas.set(Calendar.DAY_OF_MONTH, 24);
        Calendar now = Calendar.getInstance();
        
        if (now.after(xmas)) {
            xmas.add(Calendar.YEAR, 1);
        }
        
        long diff = xmas.getTimeInMillis() - now.getTimeInMillis();
        long days = diff / 1000 / 60 / 60 / 24;
        
        String result = "\n";
        result += "    *    \n";
        result += "   / \\   \n";
        result += "  / o \\  \n";
        result += "  /o o\\  \n";
        result += " /  o  \\ \n";
        result += " / " + nf.format(days + 1) + " \\ \n";
        result += "/ o   o \\\n";
        result += "^^^[_]^^^";
        return result;
    }
}
