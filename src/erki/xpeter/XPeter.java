package erki.xpeter;

import java.util.TreeMap;

import erki.api.util.CommandLineParser;

public class XPeter {
    
    private static void printHelp() {
        System.out.println("Usage: java erki.xpeter.XPeter");
    }
    
    public static void main(String[] arguments) {
        TreeMap<String, String> args = CommandLineParser.parse(arguments);
        
        if (args.containsKey("-h") || args.containsKey("--help")) {
            printHelp();
            return;
        }
    }
}
