package monitor;
import comum.*;
import infra.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.Locale;
public final class Monitor implements MonitorRemoto {
    public synchronized void notificar(EventoRobo evento) {
        String estado="";
        if(evento.estado!=null) estado=String.format(Locale.ROOT," emocao=%s bemEstar=%.4f recompensa=%.4f %s",evento.estado.emocao,evento.estado.bemEstar,evento.estado.recompensa,evento.estado.variaveis);
        System.out.println("tipo="+evento.tipo+" episodio="+evento.episodio+" passo="+evento.passo+" "+evento.descricao+estado);
    }
    public static void main(String[] argumentos) throws Exception {
        Parametros p=new Parametros(argumentos);
        AmbienteRemoto ambiente=(AmbienteRemoto)LocateRegistry.getRegistry(p.texto("host","localhost"),p.inteiro("porta",1099)).lookup(p.texto("nome","AmbienteA"));
        Monitor objeto=new Monitor();
        MonitorRemoto remoto=(MonitorRemoto)UnicastRemoteObject.exportObject(objeto,p.inteiro("porta-objeto",1103));
        ambiente.registrarMonitor(remoto);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { try { ambiente.removerMonitor(remoto); } catch(Exception ignorada) { } }));
        System.out.println("PRONTO monitor");
    }
}
