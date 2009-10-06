/*
 * © Copyright 2008–2009 by Edgar Kalkowski (eMail@edgar-kalkowski.de)
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

import erki.api.util.CommandLineParser;
import erki.api.util.Log;

/**
 * This class parses the command line or a config file if supplied. It then initializes the correct
 * {@link Parser}s and a correct subclass of {@link Bot} . So this class has to know which protocols
 * are supported whilst no other class relies on this information.
 * 
 * @author Edgar Kalkowski
 */
public class xpeter {
    
    /** The version of xpeter. To be used wherever needed. */
    public static final String VERSION = "0.0.1";
    
    private static final String CONFIG_FILE = ".botrc";
    
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
        System.out.println();
        System.out.println("The following parameters describe the connections to chat servers the");
        System.out.println("bot shall establish. It is recommended that at least one connection");
        System.out.println("is specified either in a .botrc file in the main directory or on the");
        System.out.println("command line.");
        System.out.println();
        System.out.println("  -c, --channel  The channel to join on the server. As ErkiTalk ");
        System.out.println("                 supports no channels this option is not necessary");
        System.out.println("                 for that protocol.");
        System.out.println("  -p, --port     The port to connect to on the server.");
        System.out.println("  --protocol     The protocol to use. IRC, Jabber (XMPP) and ErkiTalk");
        System.out.println("                 are supported at the moment.");
        System.out.println("  -s, --server, --host  The hostname of the server to connect to.");
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
        
        String server = null, port = null, name = "xpeter", protocol = null, channel = null;
        String parsers = null, logfile = null;
        LinkedList<Class<? extends Parser>> chosenParsers = new LinkedList<Class<? extends Parser>>();
        LinkedList<Con> cons = new LinkedList<Con>();
        
        if (new File(CONFIG_FILE).exists()) {
            
            try {
                BufferedReader fileIn = new BufferedReader(new FileReader(CONFIG_FILE));
                String line;
                
                while ((line = fileIn.readLine()) != null) {
                    
                    if (line.equals("")) {
                        continue;
                    }
                    
                    if (line.toLowerCase().startsWith("server=")) {
                        server = line.substring("server=".length());
                    } else if (line.toLowerCase().startsWith("port=")) {
                        port = line.substring("port=".length());
                    } else if (line.toLowerCase().startsWith("name=")) {
                        name = line.substring("name=".length());
                    } else if (line.toLowerCase().startsWith("protocol=")) {
                        protocol = line.substring("protocol=".length());
                    } else if (line.toLowerCase().startsWith("channel=")) {
                        channel = line.substring("channel=".length());
                    } else if (line.toLowerCase().startsWith("parsers=")) {
                        parsers = line.substring("parsers=".length());
                    } else if (line.toLowerCase().startsWith("logfile=")) {
                        logfile = line.substring("logfile=".length());
                    } else {
                        System.err.println("WARNING: Invalid line in config file: " + line);
                    }
                    
                    if (server != null && port != null && protocol != null) {
                        
                    }
                }
                
                fileIn.close();
            } catch (FileNotFoundException e) {
                Log.error(e);
                Log.warning("Though checked for existence the config file could not be found!");
                Log.info("This could mean serious trouble but we try to continue anyway.");
            } catch (IOException e) {
                Log.error(e);
                Log.warning("The config file could not be read.");
                Log.info("Perhaps we can continue with command line params only?");
            }
        }
        
        if (args.containsKey("-s")) {
            server = args.get("-s");
            args.remove("-s");
        }
        
        if (args.containsKey("--server")) {
            server = args.get("--server");
            args.remove("--server");
        }
        
        if (args.containsKey("--host")) {
            server = args.get("--host");
            args.remove("--host");
        }
        
        if (args.containsKey("-p")) {
            port = args.get("-p");
            args.remove("-p");
        }
        
        if (args.containsKey("--port")) {
            port = args.get("--port");
            args.remove("--port");
        }
        
        if (args.containsKey("--debug")) {
            Log.setLevel(Level.FINE);
            args.remove("--debug");
        }
        
        if (args.containsKey("-n")) {
            name = args.get("-n");
            args.remove("-n");
        }
        
        if (args.containsKey("--name")) {
            name = args.get("--name");
            args.remove("--name");
        }
        
        if (args.containsKey("--nick")) {
            name = args.get("--nick");
            args.remove("--nick");
        }
        
        if (args.containsKey("--protocol")) {
            protocol = args.get("--protocol");
            args.remove("--protocol");
        }
        
        if (args.containsKey("-c")) {
            
            for (String channel : args.get("-c").split(",")) {
                channels.add(channel.trim());
            }
            
            args.remove("-c");
        }
        
        if (args.containsKey("--channel")) {
            
            for (String channel : args.get("--channel").split(",")) {
                channels.add(channel.trim());
            }
            
            args.remove("--channel");
        }
        
        if (args.containsKey("--parsers")) {
            parsers = args.get("--parsers");
            args.remove("--parsers");
        }
        
        if (args.containsKey("--logfile")) {
            logfile = args.get("--logfile");
            args.remove("--logfile");
        }
        
        boolean correct = true;
        
        if (server == null) {
            System.err.println("You have to specify at least one server to connect to!");
            correct = false;
        }
        
        if (port == null) {
            System.err.println("You have to specify a port to connect to on the server!");
            correct = false;
        } else {
            
            try {
                
                if (Integer.parseInt(port) <= 0) {
                    System.err.println("The port you specified is not valid! "
                            + "It must be greater 0!");
                    correct = false;
                }
                
            } catch (NumberFormatException e) {
                System.err.println("The port you specified is not a number!");
                correct = false;
            }
        }
        
        if (protocol == null) {
            System.err.println("You have to specify a protocol that the bot shall speak!");
            correct = false;
        } else {
            
            if (!protocol.toLowerCase().equals("irc") && !protocol.toLowerCase().startsWith("erki")
                    && !protocol.toLowerCase().equals("jabber")
                    && !protocol.toLowerCase().equals("xmpp")) {
                System.err.println("The protocol you specified is not valid!");
                correct = false;
            }
        }
        
        if (protocol != null
                && (protocol.toLowerCase().equals("irc") || protocol.toLowerCase().equals("jabber") || protocol
                        .toLowerCase().equals("xmpp")) && channels.isEmpty()) {
            System.err.println("If your bot shall speak IRC or XMPP you have to "
                    + "provide at least one channel that the bot shall join!");
            correct = false;
        }
        
        if (parsers == null) {
            System.err.println("You have to specify at least one parser the bot shall use!");
            correct = false;
        }
        
        if (!correct) {
            System.err.println("Start this program with “--help” for more information.");
            return;
        }
        
        // Redirect log from stdout to specified logfile.
        if (logfile != null) {
            
            try {
                Log
                        .setHandler(new PrintStream(new FileOutputStream(logfile, false), true,
                                "UTF-8"));
            } catch (FileNotFoundException e) {
                System.err.println("FATAL: Could not open logfile " + logfile + "!");
                e.printStackTrace();
                System.exit(-1);
            } catch (UnsupportedEncodingException e) {
                System.err.println("FATAL: Your system does not support the "
                        + "UTF-8 which is needed by this program!");
                e.printStackTrace();
                System.exit(-1);
            }
        }
        
        Log.info("This is xpeter v" + VERSION + ".");
        TreeSet<Class<? extends Parser>> foundParsers = ParserFinder.findParsers(new File(".")
                .getAbsoluteFile());
        
        if (parsers.equals("*")) {
            chosenParsers.addAll(foundParsers);
        } else {
            
            for (String p : parsers.split(",")) {
                boolean foundMatching = false;
                
                for (Class<? extends Parser> c : foundParsers) {
                    
                    if (c.getSimpleName().equals(p)) {
                        chosenParsers.add(c);
                        foundMatching = true;
                    }
                }
                
                if (!foundMatching) {
                    Log.warning("Parser " + p + " could not be found!");
                }
            }
        }
        
        if (chosenParsers.isEmpty()) {
            Log.warning("No parsers loaded! This bot will do nothing except being online!");
        }
        
        try {
            
            if (protocol.toLowerCase().equals("irc")) {
                Log.info("Creating new IRC bot " + name + " for channels " + channels + ".");
                throw new IllegalStateException("Not yet implemented!");
            } else if (protocol.toLowerCase().startsWith("erki")) {
                Log.info("Creating new ErkiTalk bot " + name + ".");
                throw new IllegalStateException("Not yet implemented!");
            } else if (protocol.toLowerCase().equals("jabber")
                    || protocol.toLowerCase().equals("xmpp")) {
                Log.info("Creating new XMPP bot " + name + ".");
                throw new IllegalStateException("Not yet implemented!");
            }
            
        } catch (Throwable e) {
            Log.error(e);
            System.exit(-1);
        }
    }
    
    /**
     * Container class that is used to parse command line options and config file options into
     * Connection instances.
     * 
     * @author Edgar Kalkowski
     */
    private class Con {
        
        public String host;
        public int port;
        public String protocol;
        public String channel;
        
    }
}
