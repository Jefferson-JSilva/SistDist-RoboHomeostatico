package infra;
import java.rmi.*;
import java.rmi.registry.*;
public final class Registros {
    private Registros() { }
    public static Registry obterOuCriar(int porta) throws RemoteException {
        try { return LocateRegistry.createRegistry(porta); }
        catch(java.rmi.server.ExportException ocupado) {
            Registry registro=LocateRegistry.getRegistry("localhost",porta); registro.list(); return registro;
        }
    }
}
