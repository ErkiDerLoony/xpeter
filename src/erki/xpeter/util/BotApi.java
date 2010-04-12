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

package erki.xpeter.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;

import erki.api.util.Log;

/**
 * This class contains static helper methods that are convenient for many parser implementations. If
 * these methods are used consistently in all parser implementations it ensures that the bot always
 * reacts in the same way.
 * 
 * @author Edgar Kalkowski
 */
public class BotApi {
    
    /**
     * Retrieves the raw output for a given query from a webserver.
     * 
     * @param host
     *        The hostname of the webserver to query.
     * @param query
     *        This can either be the number of a quote to query or the special term “action/random”
     *        to retrieve a random quote.
     * @return the raw output of the server.
     * @throws UnknownHostException
     *         if the hostname can not be resolved.
     * @throws IOException
     *         if an error occurred writing to or reading from the socket.
     */
    public static String getWebsite(String host, String query) throws UnknownHostException,
            IOException {
        Socket socket = new Socket(host, 80);
        BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter socketOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        Log.debug("Making request “" + query + "” to " + host + ".");
        socketOut.write("GET " + query + " HTTP/1.0\r\nHost:" + host
                + "\r\nUser-Agent: xpeter\r\n\r\n");
        socketOut.flush();
        
        String result = "", line;
        Log.debug("Waiting for results.");
        
        while ((line = socketIn.readLine()) != null) {
            result += line;
        }
        
        Log.debug("Results received.");
        socket.close();
        return result;
    }
    
    /**
     * Create a string representation of a number. If the number is less or equal than 12 the number
     * is written out in letters. Otherwise the returned string directly contains the number.
     * 
     * @param number
     *        The number to transform into a String. Must be greater or equal 0 and not equal 1!
     * @return A syntactically correct representation of the given number.
     */
    public static String number(long number) {
        
        if (number == 0) {
            return "null";
        } else if (number == 1) {
            throw new IllegalArgumentException();
        } else if (number == 2) {
            return "zwei";
        } else if (number == 3) {
            return "drei";
        } else if (number == 4) {
            return "vier";
        } else if (number == 5) {
            return "fünf";
        } else if (number == 6) {
            return "sechs";
        } else if (number == 7) {
            return "sieben";
        } else if (number == 8) {
            return "acht";
        } else if (number == 9) {
            return "neun";
        } else if (number == 10) {
            return "zehn";
        } else if (number == 11) {
            return "elf";
        } else if (number == 12) {
            return "zwölf";
        } else {
            return "" + number;
        }
    }
    
    /**
     * Enumerate things, i.e. place them in one string separated by commas except for the last two
     * items which are separated by “und”.
     * 
     * @param things
     *        The things to enumerate.
     * @return The one string that enumerates all the given things.
     */
    public static String enumerate(Collection<? extends Object> things) {
        String response = "";
        int i = 0;
        Iterator<? extends Object> it = things.iterator();
        
        while (it.hasNext()) {
            Object thing = it.next();
            
            if (i < things.size() - 2) {
                response += thing.toString() + ", ";
            } else if (i == things.size() - 2) {
                response += thing.toString() + " und ";
            } else {
                response += thing.toString();
            }
            
            i++;
        }
        
        return response;
    }
    
    /**
     * Find out whether the current directory already is the “bin” directory or not and try to
     * assume the correct place to search for parsers. As this is only an heuristic it is not
     * guaranteed to work but it’s the best I can do for now.
     * 
     * @return The folder that probably contains the parsers.
     */
    public static File getParserDir() {
        
        if (new File("bin").isDirectory()) {
            return new File("bin").getAbsoluteFile();
        } else {
            return new File(".");
        }
    }
    
    /**
     * Checks if the text of a certain message addresses a certain nickname, i.e. the message starts
     * with the nickname optionally followed by a separation character such as a colon.
     * 
     * @param msg
     *        The text of the message to check.
     * @param nick
     *        The nickname to check.
     * @return {@code true} if {@code message} addresses {@code nick}, {@code false} otherwise.
     */
    public static boolean addresses(String msg, String nick) {
        
        if (msg == null || nick == null) {
            return false;
        }
        
        if (msg.startsWith(nick + ":") || msg.startsWith(nick + ",") || msg.startsWith(nick + ";")
                || msg.startsWith(nick)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Trims the nickname from the beginning of a message if it addresses the nickname (see
     * {@link #addresses(String, String)}).
     * 
     * @param msg
     *        The message to trim.
     * @param nick
     *        The nickname to trim from the beginning of {@code message}.
     * @return {@code msg} if
     */
    public static String trimNick(String msg, String nick) {
        
        if (msg == null) {
            return null;
        }
        
        if (nick == null) {
            return msg;
        }
        
        if (addresses(msg, nick)) {
            msg = msg.substring(nick.length());
            
            if (msg.startsWith(":") || msg.startsWith(";") || msg.startsWith(",")) {
                msg = msg.substring(1);
            }
            
            if (msg.startsWith(" ")) {
                msg = msg.substring(1);
            }
            
            return msg;
        } else {
            return msg;
        }
    }
}
