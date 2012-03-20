package erki.xpeter.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import erki.api.util.Log;
import erki.api.util.PathUtil;
import erki.xpeter.Bot;
import erki.xpeter.msg.Message;

/**
 * This parser monitors a special file and broadcasts everything that is echoed to that file to all
 * chats it is connected to. Be careful: The file is read periodically and the content is cleared
 * when read. So make sure not to save intermediate results to that file or they will perhaps be
 * posted. Doing something like “echo "blub" >> file” (with file being the special file) should be
 * fine.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class RemoteControl implements Parser {
    
    private static final File FILE = PathUtil.getRessource("remote");
    
    private Thread thread;
    
    private boolean killed = false;
    
    @Override
    public void init(final Bot bot) {
        
        thread = new Thread() {
            
            public void run() {
                
                while (!killed) {
                    Log.debug("Checking if content file " + FILE + " exists.");
                    
                    if (FILE.isFile() && FILE.canWrite()) {
                        
                        try {
                            BufferedReader fileIn = new BufferedReader(new InputStreamReader(
                                    new FileInputStream(FILE)));
                            String line;
                            
                            while ((line = fileIn.readLine()) != null) {
                                
                                if (!line.trim().startsWith("#")) {
                                    bot.broadcast(new Message(line));
                                }
                            }
                            
                            fileIn.close();
                            new RandomAccessFile(FILE, "rws").setLength(0);
                        } catch (FileNotFoundException e) {
                            Log.error(e);
                            Log.warning("Remote control file could not be found!");
                        } catch (IOException e) {
                            Log.error(e);
                            Log.warning("Remote control file could not be read!");
                        }
                    }
                    
                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        
        thread.start();
    }
    
    @Override
    public void destroy(Bot bot) {
        killed = true;
        thread.interrupt();
    }
}
