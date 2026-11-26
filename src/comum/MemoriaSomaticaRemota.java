package comum;
import java.rmi.*;
import java.util.List;
public interface MemoriaSomaticaRemota extends Remote {
    String NOME_PADRAO="MemoriaSomatica";
    void salvar(String chave, MarcadoresSomaticos marcadores) throws RemoteException;
    MarcadoresSomaticos carregar(String chave) throws RemoteException;
    List<String> listar() throws RemoteException;
}
