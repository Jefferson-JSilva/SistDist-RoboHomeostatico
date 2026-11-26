package memoria;
import comum.*;
import infra.*;
import java.nio.file.*;
import java.rmi.server.*;
public final class ServidorMemoria {
    public static void main(String[] argumentos) throws Exception {
        Parametros p=new Parametros(argumentos);
        ServicoMemoria servico=new ServicoMemoria(Paths.get(p.texto("pasta","dados/memoria")));
        MemoriaSomaticaRemota remoto=(MemoriaSomaticaRemota)UnicastRemoteObject.exportObject(servico,p.inteiro("porta-objeto",1102));
        Registros.obterOuCriar(p.inteiro("porta",1101)).rebind(p.texto("nome",MemoriaSomaticaRemota.NOME_PADRAO),remoto);
        System.out.println("PRONTO memoria");
    }
}
