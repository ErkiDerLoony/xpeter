package erki.xpeter.parsers.soccer;

import erki.xpeter.Bot;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.Message;

public class WmThread extends Thread {
    
    private boolean killed = false;
    
    private Bot bot;
    
    public WmThread(Bot bot) {
        this.bot = bot;
    }
    
    @Override
    public void run() {
        super.run();
        
        while (!killed) {
            
            switch ((int) (Math.random() * 11)) {
                case 0:
                    bot.broadcast(new Message("Olé, olé!"));
                    bot.broadcast(new DelayedMessage("Wir fahr’n in Puff nach Bazelona.", 5000));
                    break;
                case 1:
                    bot.broadcast(new Message("Mir soache, bis mia umfalln!"));
                    break;
                case 2:
                    bot.broadcast(new Message("Ohne Ballack, fahr’n wir zur WM! :P"));
                    bot.broadcast(new DelayedMessage("*singfreuhust*", 6000));
                    break;
                case 3:
                    bot.broadcast(new Message("Deutschland vor: Noch ein Tor!!"));
                    break;
                case 4:
                    bot.broadcast(new Message("Fußball ist unser Leben,"));
                    bot
                            .broadcast(new DelayedMessage("denn König Fußball regiert die Welt!",
                                    15000));
                    bot.broadcast(new DelayedMessage("Wir trinken und halten zusammen,", 25000));
                    bot.broadcast(new DelayedMessage(
                            "bis dann ein Tor nach dem andern fällt. *sing*", 34000));
                    break;
                case 5:
                    bot.broadcast(new Message("Lala, lala, lalalalalalaaaaa *trööööt*"));
                    break;
                case 6:
                    bot.broadcast(new Message(
                            "Vierundfünfzig, vierundziebzig, neunzig, zweitausendzehn …"));
                    bot.broadcast(new DelayedMessage("Ja, da stimmen wir alle ein!", 12000));
                    bot.broadcast(new DelayedMessage("Los, auf geht’s! *sumsumrumsing*", 26000));
                    break;
                case 7:
                    bot.broadcast(new Message("Allee, Allee, Allee, Allee, Alleeeeee …"));
                    bot.broadcast(new DelayedMessage("Eine Straße, viele Bäume …", 10000));
                    bot.broadcast(new DelayedMessage("Ja, das ist eine Alleeeeee!", 20000));
                    break;
                case 8:
                    bot.broadcast(new Message(
                            "Schwarz, Rot, Gold: Wir steh’n an eurer Seite! *sing*"));
                    break;
                case 9:
                    bot.broadcast(new Message("Schlaaaand, schlaaaaaaaand!"));
                    break;
                default:
                    bot.broadcast(new Message("Alle Spiele (von mir ;)) kommentiert: /join "
                            + "wm@conference.jabber.exados.com!"));
            }
            
            synchronized (this) {
                
                try {
                    wait(1200000 + (int) (Math.random() * 1200000));
                } catch (InterruptedException e) {
                }
            }
        }
    }
    
    public synchronized void kill() {
        killed = true;
        notify();
    }
}
