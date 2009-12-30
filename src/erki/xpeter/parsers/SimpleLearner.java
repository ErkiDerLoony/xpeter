package erki.xpeter.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.TreeMap;

import erki.api.util.Log;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.con.Connection;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.util.BotApi;

public class SimpleLearner implements Parser, Observer<TextMessage> {
    
    private static final String CONFIG_FILE = "config" + File.separator + "knowledge";
    
    private TreeMap<String, LinkedList<String>> knowledge = new TreeMap<String, LinkedList<String>>();
    
    @Override
    public void init(Bot bot) {
        load();
        bot.register(TextMessage.class, this);
    }
    
    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
    }
    
    @SuppressWarnings("unchecked")
    private void load() {
        
        try {
            ObjectInputStream fileIn = new ObjectInputStream(new FileInputStream(CONFIG_FILE));
            knowledge = (TreeMap<String, LinkedList<String>>) fileIn.readObject();
            fileIn.close();
            
            for (String key : knowledge.keySet()) {
                Log.debug("Loaded knowledge mapping “" + key + "” → “" + knowledge.get(key) + "”");
            }
            
            Log.info("Knowledge base successfully loaded from " + CONFIG_FILE + ".");
        } catch (FileNotFoundException e) {
            Log.info("Knowledge base could not be loaded because the file " + CONFIG_FILE
                    + " could not be found.");
            Log.info("Trying to learn something anyway.");
        } catch (IOException e) {
            Log.warning("An error occurred while trying to load the knowledge base from "
                    + CONFIG_FILE + ".");
            Log.info("Trying to learn something anyway.");
        } catch (ClassNotFoundException e) {
            Log.warning("Knowledge base could not be loaded because a necessary class was not "
                    + "found.");
            Log.info("Trying to learn something anyway.");
        }
    }
    
    private void save() {
        
        try {
            ObjectOutputStream fileOut = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE));
            fileOut.writeObject(knowledge);
            fileOut.close();
            Log.info("Knowledge base successfully stored to " + CONFIG_FILE + ".");
        } catch (FileNotFoundException e) {
            Log.warning("Knowledge base could not be stored because the file " + CONFIG_FILE
                    + " could not be found!");
        } catch (IOException e) {
            Log.warning("An error occurred while storing the knowledge base to " + CONFIG_FILE
                    + "!");
        }
    }
    
    @Override
    public void inform(TextMessage msg) {
        Connection con = msg.getConnection();
        String text = msg.getText();
        String nick = con.getNick();
        boolean addresses = false;
        
        if (BotApi.addresses(text, nick)) {
            addresses = true;
            text = BotApi.trimNick(text, nick);
        }
        
        String knowledge = "(.*?) (" + verbs() + ") (.*?)";
        Log.debug("Matching “" + text + "” with “" + knowledge + "”.");
        
        if (text.matches(knowledge)) {
            String key = text.replaceAll(knowledge, "$1");
            String verb = text.replaceAll(knowledge, "$2");
            String value = text.replaceAll(knowledge, "$3");
            Log.debug("Recognized knowledge: “" + key + "” → “" + verb + "” → “" + value + "”.");
            
            if (!this.knowledge.containsKey(key)) {
                this.knowledge.put(key, new LinkedList<String>());
            }
            
            this.knowledge.get(key).add(verb + " " + value);
            save();
            
            if (addresses) {
                con.send(new DelayedMessage("Ok, weißsch Bescheid.", 2000));
            }
        }
        
        String query = "[wW]as (wei(ss|ß)t [dD]u|kannst [dD]u sagen) (ü|ue)ber (.*?)\\??";
        
        if (addresses && text.matches(query)) {
            String key = text.replaceAll(query, "$4");
            Log.debug("Recognized query for “" + key + "”.");
            
            if (this.knowledge.containsKey(key)) {
                LinkedList<String> hits = this.knowledge.get(key);
                String hit = hits.get((int) (Math.random() * hits.size()));
                con.send(new DelayedMessage(key + " " + hit, 3000));
            } else {
                con.send(new DelayedMessage("Darüber weiß ich nichts.", 3000));
            }
        }
    }
    
    private static String verbs() {
        return "ist|hat|kann|wohnt|sind|wird";
    }
}
