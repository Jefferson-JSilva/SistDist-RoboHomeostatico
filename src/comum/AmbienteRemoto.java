package comum;
import java.rmi.*;
public interface AmbienteRemoto extends Remote {
    int DIMENSAO=6;
    String NOME_PADRAO="Ambiente";
    String nome() throws RemoteException;
    Pose iniciarEpisodio(String idRobo, int episodio) throws RemoteException;
    LeituraSensores lerSensores(String idRobo) throws RemoteException;
    ResultadoAcao executar(String idRobo, Acao acao) throws RemoteException;
    void publicarEstado(String idRobo, EstadoInterno estado) throws RemoteException;
    RelatorioEpisodio encerrarEpisodio(String idRobo) throws RemoteException;
    void registrarMonitor(MonitorRemoto monitor) throws RemoteException;
    void removerMonitor(MonitorRemoto monitor) throws RemoteException;
}
