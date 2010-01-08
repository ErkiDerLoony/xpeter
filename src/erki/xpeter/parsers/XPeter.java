/*
 * © Copyright 2008–2009 by Edgar Kalkowski (eMail@edgar-kalkowski.de)
 * 
 * This file is part of the chatbot ABCPeter.
 * 
 * The chatbot ABCPeter is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package erki.xpeter.parsers;

import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.Message;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.util.BotApi;

/**
 * Do some basic things the bot could always do.
 * 
 * @author Edgar Kalkowski
 */
public class XPeter implements Parser, Observer<TextMessage> {
    
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
        String nick = msg.getBotNick();
        boolean addresses = false;
        
        if (BotApi.addresses(text, nick)) {
            addresses = true;
            text = BotApi.trimNick(text, nick);
        }
        
        String whoRYou = "(([wW]a[ts][\\?,] )?[wW]er bist? (denn )?"
                + "[dD]u( denn)?|[wW]ho (are|r) you)\\??";
        
        if (addresses && text.matches(whoRYou)) {
            msg.respond(new DelayedMessage("My name is Peter, I'm old.", 3000));
        }
        
        String job = "[wW]as machst [dD]u( denn)?( so)?( hier( so)?)?\\?";
        String job2 = "([wW]arum|[wW]ozu) bist [dD]u( denn)? (hier|da)\\?";
        
        if (addresses && (text.matches(job) || text.matches(job2))) {
            int rnd = (int) (Math.random() * 2);
            
            if (rnd == 0) {
                msg.respond(new DelayedMessage("I'm a drummer in a band and I'm old.", 2500));
            } else {
                msg.respond(new DelayedMessage("I'm grabbing my grave behind the tent.", 3000));
                msg.respond(new DelayedMessage("One millimeter a week.", 6000));
                msg.respond(new DelayedMessage("'Cause I'm too schwach to hold the schaufel "
                        + "with my arms.", 11000));
            }
        }
        
        if ((msg.getText().trim().equals("oO") || msg.getText().trim().equals("o0"))
                && ((int) (Math.random() * 3)) == 0 && !msg.getNick().equals(nick)) {
            
            if ((int) (Math.random() * 2) == 0) {
                msg.respond(new Message("Oo"));
            } else {
                msg.respond(new Message("0o"));
            }
        }
        
        if ((msg.getText().trim().equals("Oo") || msg.getText().trim().equals("0o"))
                && ((int) (Math.random() * 5)) == 0 && !msg.getNick().equals(nick)) {
            
            if ((int) (Math.random() * 2) == 0) {
                msg.respond(new Message("oO"));
            } else {
                msg.respond(new Message("o0"));
            }
        }
        
        if (msg.getText().trim().equals("^^") && ((int) (Math.random() * 4)) == 0) {
            msg.respond(new Message("vv"));
        }
        
        if (msg.getText().toLowerCase().trim().equals("asdf") && ((int) (Math.random() * 3)) == 0
                && !msg.getNick().equals(nick)) {
            msg.respond(new Message("fdsa"));
        }
        
        if (msg.getText().toLowerCase().trim().equals("fdsa") && ((int) (Math.random() * 3)) == 0
                && !msg.getNick().equals(nick)) {
            msg.respond(new Message("asdf"));
        }
        
        if ((msg.getText().toLowerCase().trim().equals("ok")
                || msg.getText().toLowerCase().trim().equals("ok.") || msg.getText().toLowerCase()
                .trim().equals("ok!"))
                && ((int) (Math.random() * 3)) == 0) {
            msg.respond(new Message("K.O."));
        }
        
        if (addresses && text.matches("[gG]eh kacken( [jJ]unge)?( es?cht [gj]et?zt?)?!?\\.?")) {
            int rnd = (int) (Math.random() * 4);
            
            if (rnd == 0) {
                msg.respond(new Message("Ja, wo denn?!"));
            } else if (rnd == 1) {
                msg.respond(new Message("Kommst mit?"));
            } else if (rnd == 2) {
                msg.respond(new Message("Selba! :P"));
            } else {
                msg.respond(new Message("Ich weiß ja net, wo!"));
            }
        }
        
        String whatGoes = "([Aa]lt(er|a)[:;,!]? )?[wW]as geht[?.!]?[?.!]?[?.!]?";
        
        if (addresses && text.matches(whatGoes)) {
            msg.respond(new DelayedMessage("Alles, was Beine hat.", 1500));
        }
        
        String please = "[Bb]itte\\.?!?";
        
        if (addresses && text.matches(please)) {
            msg.respond(new DelayedMessage("Danke!", 2000));
        }
        
        String thanks = "[dD]anke\\.?!?";
        
        if (addresses && text.matches(thanks)) {
            msg.respond(new DelayedMessage("Bitte!", 2000));
        }
        
        String wellDone = "[gG]ut gemacht\\.!?";
        
        if (addresses && text.matches(wellDone)) {
            msg.respond(new DelayedMessage("Danke! :)", 2000));
        }
        
        String why = "[wW]arum\\??\\??\\?";
        
        if (addresses && text.matches(why)) {
            msg.respond(new DelayedMessage("Weil Gott es so gewollt hat und Einstein Unrecht hat.",
                    2000));
            msg.respond(new DelayedMessage("Gott würfelt *doch*!", 5000));
        }
    }
}
