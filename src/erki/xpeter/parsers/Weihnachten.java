/*
 * © Copyright 2008–2010 by Edgar Kalkowski <eMail@edgar-kalkowski.de>
 * 
 * This file is part of the chatbot xpeter.
 * 
 * The chatbot xpeter is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

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
