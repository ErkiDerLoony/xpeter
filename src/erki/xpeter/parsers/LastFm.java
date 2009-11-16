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

import java.util.Collection;

import net.roarsoftware.lastfm.Caller;
import net.roarsoftware.lastfm.Track;
import net.roarsoftware.lastfm.User;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.con.Connection;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.util.BotApi;

/**
 * Enables the bot to retrieve information from Last.FM and tell people what they are listening to.
 * 
 * @author Edgar Kalkowski
 */
public class LastFm implements Parser, Observer<TextMessage> {
    
    private static final String LAST_FM_API_KEY = "2ff20341447560ee47656ce6e572107e";
    
    @Override
    public void init(Bot bot) {
        bot.register(TextMessage.class, this);
        Caller.getInstance().setUserAgent("xpeter");
    }
    
    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
    }
    
    @Override
    public void inform(TextMessage msg) {
        Connection con = msg.getConnection();
        String nick = con.getNick();
        String text = msg.getText();
        
        if (!BotApi.addresses(text, nick)) {
            return;
        }
        
        text = BotApi.trimNick(text, nick);
        String queryNick = null;
        
        if (text.toLowerCase().trim().startsWith("np ")) {
            queryNick = text.substring("np ".length());
        }
        
        if (text.toLowerCase().trim().equals("np")) {
            queryNick = msg.getNick();
        }
        
        String match = "[wW]as h(oe|ö)rt (.*?)( gerade)?\\??";
        
        if (text.matches(match)) {
            queryNick = text.replaceAll(match, "$2");
        }
        
        match = "[wW]as h(oe|ö)re ich( gerade)?\\??";
        
        if (text.matches(match)) {
            queryNick = msg.getNick();
        }
        
        if (queryNick != null) {
            Collection<Track> tracks = User.getRecentTracks(queryNick, LAST_FM_API_KEY);
            
            if (!tracks.isEmpty()) {
                Track track = tracks.iterator().next();
                
                if (track.isNowPlaying()) {
                    con.send(new DelayedMessage(queryNick + " hört gerade " + formatTrack(track)
                            + ".", 1000));
                } else {
                    con.send(new DelayedMessage(queryNick + " hat zuletzt " + formatTrack(track)
                            + " gehört.", 1000));
                }
                
            } else {
                con.send(new DelayedMessage("Last.FM weiß leider nicht, was " + queryNick
                        + " gerade hört. :(", 1500));
                
                if (((int) (Math.random() * 3)) == 0) {
                    con.send(new DelayedMessage("Tut mir Leid.", 2500));
                }
            }
        }
    }
    
    private String formatTrack(Track track) {
        
        if (track.getAlbum() == null || track.getAlbum().equals("")) {
            return "„" + track.getName() + "“ von »" + track.getArtist() + "«";
        } else {
            return "„" + track.getName() + "“ von »" + track.getArtist() + "« auf dem Album „"
                    + track.getAlbum() + "“";
        }
    }
}
