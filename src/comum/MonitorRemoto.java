package comum;
import java.rmi.*;
public interface MonitorRemoto extends Remote {
    void notificar(EventoRobo evento) throws RemoteException;
}
