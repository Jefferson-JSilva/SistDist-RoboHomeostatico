package ambiente;
import comum.*;
import java.rmi.*;
import java.util.concurrent.*;
import java.util.*;
final class Notificador {
    private final ConcurrentMap<MonitorRemoto,Canal> canais=new ConcurrentHashMap<MonitorRemoto,Canal>();
    private final ScheduledExecutorService vigia=Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t=new Thread(r,"vigia-callback"); t.setDaemon(true); return t;
    });
    Notificador() { vigia.scheduleAtFixedRate(() -> {
        for(Canal canal:canais.values()) if(canal.inicio!=0&&System.nanoTime()-canal.inicio>TimeUnit.SECONDS.toNanos(3)) remover(canal.monitor);
    },1,1,TimeUnit.SECONDS); }
    synchronized void registrar(MonitorRemoto monitor) throws RemoteException {
        if(monitor==null) throw new RemoteException("Monitor nulo");
        if(canais.containsKey(monitor)) return;
        if(canais.size()>=32) throw new RemoteException("Limite de monitores");
        Canal canal=new Canal(monitor); canais.put(monitor,canal); canal.trabalhador.start();
    }
    void remover(MonitorRemoto monitor) {
        Canal canal=canais.remove(monitor);
        if(canal!=null) { canal.ativo=false; canal.trabalhador.interrupt(); canal.fila.clear(); }
    }
    void publicar(EventoRobo evento) {
        // offer nunca espera: sobrecarga remove apenas o observador lento.
        for(Canal canal:canais.values()) if(!canal.fila.offer(evento)) remover(canal.monitor);
    }
    private final class Canal {
        final MonitorRemoto monitor;
        final BlockingQueue<EventoRobo> fila=new ArrayBlockingQueue<EventoRobo>(8192);
        final Thread trabalhador;
        volatile boolean ativo=true;
        volatile long inicio;
        Canal(MonitorRemoto monitor) {
            this.monitor=monitor;
            trabalhador=new Thread(() -> {
                try { while(ativo) {
                    EventoRobo evento=fila.take(); inicio=System.nanoTime();
                    monitor.notificar(evento); inicio=0;
                } } catch(Exception falha) { remover(monitor); }
            },"callback-monitor"); trabalhador.setDaemon(true);
        }
    }
}
