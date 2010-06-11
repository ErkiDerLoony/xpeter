package erki.xpeter.parsers.soccer;

import java.io.IOException;
import java.net.UnknownHostException;

import erki.xpeter.util.BotApi;

public class DetectorThread extends Thread {
    
    private Soccer soccer;
    
    private String base;
    
    private boolean killed = false;
    
    private static final int MIN = 1660, MAX = 1800;
    
    public DetectorThread(Soccer soccer, String base) {
        this.soccer = soccer;
        this.base = base;
    }
    
    @Override
    public void run() {
        super.run();
        int test = MIN;
        
        while (!killed) {
            String url = base + test;
            
            try {
                
                if (BotApi.getWebsite(soccer.getHost(url), soccer.getQuery(url), "ISO-8859-1")
                        .contains("<div id=\"ardTickerTableau\">")) {
                    soccer.add(url);
                } else {
                }
                
            } catch (UnknownHostException e) {
            } catch (IOException e) {
            }
            
            synchronized (this) {
                
                if (test == MAX) {
                    
                    try {
                        wait(6000000);
                    } catch (InterruptedException e) {
                    }
                    
                } else {
                    
                    try {
                        wait(60000);
                    } catch (InterruptedException e) {
                    }
                }
            }
            
            test += 1;
            
            if (test == MAX) {
                test = MIN;
            }
        }
    }
    
    public synchronized void kill() {
        killed = true;
        notify();
    }
}
