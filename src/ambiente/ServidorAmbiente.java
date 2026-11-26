package ambiente;
import comum.*;
import infra.*;
import java.nio.file.*;
import java.rmi.server.*;
public final class ServidorAmbiente {
    public static void main(String[] argumentos) throws Exception {
        Parametros p=new Parametros(argumentos);
        ServicoAmbiente servico=new ServicoAmbiente(new Mapa(Paths.get(p.texto("mapa","mapas/ambienteA.txt"))),p.texto("nome","AmbienteA"));
        AmbienteRemoto remoto=(AmbienteRemoto)UnicastRemoteObject.exportObject(servico,p.inteiro("porta-objeto",1100));
        Registros.obterOuCriar(p.inteiro("porta",1099)).rebind(p.texto("nome","AmbienteA"),remoto);
        System.out.println("PRONTO ambiente="+servico.nome());
    }
}
