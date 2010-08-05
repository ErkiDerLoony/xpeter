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

package erki.xpeter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import erki.api.storage.JavaObjectStorage;
import erki.api.storage.Storage;
import erki.api.util.CommandLineParser;
import erki.api.util.Log;
import erki.xpeter.con.erkitalk.ErkiTalkConnection;
import erki.xpeter.con.irc.IrcConnection;
import erki.xpeter.con.xmpp.XmppConnection;
import erki.xpeter.parsers.Parser;
import erki.xpeter.util.BotApi;
import erki.xpeter.util.Keys;
import erki.xpeter.util.ParserFinder;
import erki.xpeter.util.StorageKey;

/**
 * This class parses the command line or a config file if present. It then initializes the correct
 * {@link Parser}s and a correct subclass of {@link Bot} . So this class has to know which protocols
 * are supported whilst no other class relies on this information.
 * 
 * @author Edgar Kalkowski
 */
public class xpeter {
    
    /** The version of xpeter. To be used wherever needed. */
    public static final String VERSION = "0.5.0";
    
    /** Prints the "--help" message to stdout. */
    private static void printHelp() {
        System.out.println("Usage: java erki.xpeter.xpeter [params]");
        System.out.println();
        System.out.println("  --debug        Print a lot of debug " + "information to the log.");
        System.out.println("  -h, --help     Print this help message and exit.");
        System.out.println("  -l, --list     List all available parser classes and exit.");
        System.out.println("  --logfile      Specify a logfile. If no logfile is specified all");
        System.out.println("                 log information is printed to stdout.");
        System.out.println("  -n, --name, --nick  The nickname of the bot (if different from the ");
        System.out.println("                 default “xpeter”).");
        System.out.println("  --parsers      A comma separated list containing the names of the");
        System.out.println("                 parser classes the bot shall use. If this parameter");
        System.out.println("                 equals “*” all found parsers will be used.");
        System.out.println("  -v, --version  Print version information and exit.");
        System.out.println("  -c, --con, --connection  Specify one or more chats to connect to.");
        System.out.println("                 the parameter of this option must be of the form");
        System.out.println("                 <protocol>://[<channel>@]<server>:<port> where");
        System.out.println("                   – protocol is one of “erkitalk”, “irc”, “xmpp” or");
        System.out.println("                     “jabber”,");
        System.out.println("                   – channel is the name of the channel to join,");
        System.out.println("                   – server is the hostname of the server to connect");
        System.out.println("                     to and");
        System.out.println("                   – port is the port number.");
        System.out.println("                 <channel> is optional as e.g. ErkiTalk has no");
        System.out.println("                 channels. You should specify at least one connection");
        System.out.println("                 on the command line or in your .botrc file. Use one");
        System.out.println("                 -c switch for each connection you want the bot to");
        System.out.println("                 establish.");
        System.out.println("  --config       Specify a config file to use. This defaults to be");
        System.out.println("                 a file called .botrc in the main directory of the");
        System.out.println("                 program (the directory that also contains the src,");
        System.out.println("                 lib and bin folders).");
        System.out.println("  -s, --storage  Specify a file that can be used by the parsers to");
        System.out.println("                 store various information. This defaults to be a");
        System.out.println("                 file called .storage in the program’s main");
        System.out.println("                 directory.");
        System.out.println();
        System.out.println("All command line options can also be specified in a file called");
        System.out.println(".botrc located in the directory where the bot is executed. Beware");
        System.out.println("however that command line options supersede (and thus replace)");
        System.out.println("options specified in the config file!");
        System.out.println();
        System.out.println("© 2008–2009 by Edgar Kalkowski <eMail@edgar-kalkowski.de>");
    }
    
    /**
     * Reads the config file (if any) and evaluates the command line arguments. If any option is
     * specified in both the config file and the command line the command line will override the
     * option in the config file.
     * 
     * @param arguments
     *        The command line arguments.
     */
    public static void main(String[] arguments) {
        TreeMap<String, String> args = CommandLineParser.parse(arguments);
        
        if (args.containsKey("--help") || args.containsKey("-h")) {
            printHelp();
            return;
        }
        
        if (args.containsKey("--list")) {
            System.out.println("The following parsers are available:");
            
            for (Class<? extends Parser> parser : ParserFinder.findParsers(new File(".")
                    .getAbsoluteFile())) {
                System.out.println(parser.getSimpleName());
            }
            
            return;
        }
        
        if (args.containsKey("--version") || args.containsKey("-v")) {
            System.out.println("This is xpeter v" + VERSION + ".");
            return;
        }
        
        String nick = "xpeter", configFile = ".botrc", storageFile = ".storage";
        String parsers = null, logfile = null;
        LinkedList<Class<? extends Parser>> chosenParsers = new LinkedList<Class<? extends Parser>>();
        LinkedList<Con> cons = new LinkedList<Con>();
        
        if (args.containsKey("-s")) {
            storageFile = args.get("-s");
            args.remove("-s");
        }
        
        if (args.containsKey("--storage")) {
            storageFile = args.get("--storage");
            args.remove("--storage");
        }
        
        if (args.containsKey("--config")) {
            configFile = args.get("--config");
            args.remove("--config");
        }
        
        if (new File(configFile).exists()) {
            
            try {
                BufferedReader fileIn = new BufferedReader(new FileReader(configFile));
                String line;
                
                while ((line = fileIn.readLine()) != null) {
                    
                    if (line.trim().equals("") || line.trim().startsWith("//")
                            || line.trim().startsWith("#")) {
                        continue;
                    }
                    
                    if (line.toLowerCase().startsWith("name=")) {
                        nick = line.substring("name=".length());
                    } else if (line.toLowerCase().startsWith("nick=")) {
                        nick = line.substring("nick=".length());
                    } else if (line.toLowerCase().startsWith("connection=")) {
                        parseCon(cons, line.substring("connection=".length()));
                    } else if (line.toLowerCase().startsWith("parsers=")) {
                        parsers = line.substring("parsers=".length());
                    } else if (line.toLowerCase().startsWith("logfile=")) {
                        logfile = line.substring("logfile=".length());
                    } else {
                        System.err.println("WARNING: Invalid line in config file: " + line);
                    }
                }
                
                fileIn.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.err.println("Though checked for existence the config file could not be "
                        + "found!");
                System.err.println("This could mean serious trouble but we try to continue "
                        + "anyway.");
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("The config file could not be read.");
                System.err.println("Perhaps we can continue with command line params only?");
            }
        }
        
        if (args.containsKey("--debug")) {
            Log.setLevel(Level.FINE);
            args.remove("--debug");
        }
        
        if (args.containsKey("-n")) {
            nick = args.get("-n");
            args.remove("-n");
        }
        
        if (args.containsKey("--name")) {
            nick = args.get("--name");
            args.remove("--name");
        }
        
        if (args.containsKey("--nick")) {
            nick = args.get("--nick");
            args.remove("--nick");
        }
        
        if (args.containsKey("--parsers")) {
            parsers = args.get("--parsers");
            args.remove("--parsers");
        }
        
        if (args.containsKey("--logfile")) {
            logfile = args.get("--logfile");
            args.remove("--logfile");
        }
        
        // Redirect log from stdout to specified logfile.
        if (logfile != null) {
            
            try {
                Log.setHandler(new PrintStream(new FileOutputStream(logfile, false), true, "UTF-8"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.err.println("FATAL: Could not open logfile " + logfile + "!");
                System.exit(-1);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                System.err.println("FATAL: Your system does not support the "
                        + "UTF-8 which is needed by this program!");
                System.exit(-1);
            }
        }
        
        if (args.containsKey("-c")) {
            parseCon(cons, args.get("-c"));
            args.remove("-c");
        }
        
        if (args.containsKey("--con")) {
            parseCon(cons, args.get("--con"));
            args.remove("--con");
        }
        
        if (args.containsKey("--connection")) {
            parseCon(cons, args.get("--connection"));
            args.remove("--connection");
        }
        
        Log.info("This is xpeter v" + VERSION + ".");
        
        TreeSet<Class<? extends Parser>> foundParsers = ParserFinder.findParsers(BotApi
                .getParserDir());
        
        if (parsers != null && parsers.equals("*")) {
            Log.debug("Using all found parsers.");
            chosenParsers.addAll(foundParsers);
        } else if (parsers != null) {
            
            for (String p : parsers.split(",")) {
                boolean foundMatching = false;
                
                for (Class<? extends Parser> c : foundParsers) {
                    
                    if (c.getSimpleName().equals(p)) {
                        chosenParsers.add(c);
                        foundMatching = true;
                    }
                }
                
                if (!foundMatching) {
                    Log.warning("Parser “" + p + "” could not be found!");
                }
            }
        }
        
        if (parsers == null || chosenParsers.isEmpty()) {
            Log.warning("No parsers loaded! This bot will do nothing except being online!");
        }
        
        if (cons == null || cons.isEmpty()) {
            Log.error("There was no connection specified!");
            Log.warning("The bot will exit now as it seems to be useless without any connection.");
            System.exit(17);
        }
        
        Storage<Keys> storage = new JavaObjectStorage<Keys>(storageFile);
        
        // Remove deprecated entries.
        @SuppressWarnings("deprecation")
        StorageKey<LinkedList<String>> oldKey = new StorageKey<LinkedList<String>>(
                Keys.SOCCER_THREADS);
        
        if (storage.contains(oldKey)) {
            storage.remove(oldKey);
        }
        
        Bot bot = new Bot(chosenParsers, storage);
        
        for (Con con : cons) {
            
            if (con.protocol.equals("irc")) {
                Log.info("Creating an IRC connection to " + con.host + ":" + con.port + ".");
                bot.add(new IrcConnection(bot, con.host, con.port, con.channel, nick));
            } else if (con.protocol.equals("erki") || con.protocol.equals("erkitalk")) {
                Log.info("Creating an ErkiTalk connection to " + con.host + ":" + con.port + ".");
                bot.add(new ErkiTalkConnection(bot, con.host, con.port, nick));
            } else if (con.protocol.equals("jabber") || con.protocol.equals("xmpp")) {
                Log.info("Creating an XMPP connection to " + con.channel + "@" + con.host + ":"
                        + con.port + ".");
                bot.add(new XmppConnection(bot, con.host, con.port, con.channel, nick));
            }
        }
    }
    
    /**
     * Parses a line that matches {@code <protocol>://[<channel>@]<server>:<port>} into an instance
     * of {@link Con} and checks that all connection parameters are valid. If something is invalid a
     * fatal error message is printed and the program aborted.
     * 
     * @param cons
     *        The list of connections to which the new connection is appended if it is valid.
     * @param line
     *        A line of text that specifies a connection (see above).
     */
    private static void parseCon(LinkedList<Con> cons, String line) {
        
        if (!line.contains(":")) {
            System.err.println("FATAL ERROR!");
            System.err.println("Invalid connection specification: " + line);
            System.err.println("It does not match <protocol>://[<channel>@]<server>:<port>!");
            System.exit(1);
        }
        
        String protocol = line.substring(0, line.indexOf(':')).toLowerCase();
        
        if (!protocol.equals("xmpp") && !protocol.equals("jabber") && !protocol.equals("irc")
                && !protocol.equals("erkitalk") && !protocol.equals("erki")) {
            System.err.println("FATAL ERROR!");
            System.err.println("The protocol you specified (" + protocol
                    + ") is not known to this bot!");
            System.exit(5);
        }
        
        if (line.indexOf(':') == line.length() - 1) {
            System.err.println("FATAL ERROR!");
            System.err.println("Invalid connection specification: " + line);
            System.err.println("The connection specification must end in the port number!");
            System.exit(2);
        }
        
        String sPort = line.substring(line.lastIndexOf(':') + 1, line.length());
        int port = -1;
        
        try {
            port = Integer.parseInt(sPort);
        } catch (NumberFormatException e) {
            System.err.println("FATAL ERROR!");
            System.err.println("The port you specified (" + sPort
                    + ") could not be parsed into a number!");
            System.exit(4);
        }
        
        String server, channel;
        
        if (line.contains("@")) {
            
            if (line.indexOf('@') > line.lastIndexOf(':')) {
                System.err.println("FATAL ERROR!");
                System.err.println("Invalid connection specification: " + line);
                System.err.println("The channel must be noted before the server!");
                System.exit(3);
            }
            
            channel = line.substring(line.indexOf(':') + 3, line.lastIndexOf('@'));
            server = line.substring(line.lastIndexOf('@') + 1, line.lastIndexOf(':'));
        } else {
            channel = null;
            server = line.substring(line.indexOf(':') + 3, line.lastIndexOf(':'));
        }
        
        cons.add(new Con(server, port, protocol, channel));
    }
}

/**
 * Container class that is used to parse command line options and config file options into
 * Connection instances.
 * 
 * @author Edgar Kalkowski
 */
class Con {
    
    public String host;
    public int port;
    public String protocol;
    public String channel;
    
    public Con(String host, int port, String protocol, String channel) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.channel = channel;
        
        if (!protocol.equals("irc") && !protocol.equals("jabber") && !protocol.equals("xmpp")
                && !protocol.equals("erkitalk")) {
            throw new IllegalArgumentException("Invalid protocol: " + protocol);
        }
    }
}