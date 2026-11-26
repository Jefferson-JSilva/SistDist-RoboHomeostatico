package memoria;
import comum.*;
import java.rmi.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
public final class ServicoMemoria implements MemoriaSomaticaRemota {
    private final Path pasta;
    public ServicoMemoria(Path pasta) throws IOException { this.pasta=pasta; Files.createDirectories(pasta); }
    private Path arquivo(String chave) throws Exception {
        if(chave==null||chave.isEmpty()||chave.length()>200) throw new IllegalArgumentException("Chave invalida");
        byte[] resumo=MessageDigest.getInstance("SHA-256").digest(chave.getBytes(StandardCharsets.UTF_8));
        StringBuilder nome=new StringBuilder(); for(byte b:resumo) nome.append(String.format("%02x",b&255));
        return pasta.resolve(nome+".ser");
    }
    public synchronized void salvar(String chave,MarcadoresSomaticos marcadores) throws RemoteException {
        Path temporario=null;
        try {
            if(marcadores==null) throw new IllegalArgumentException("Marcadores nulos");
            Path destino=arquivo(chave); temporario=Files.createTempFile(pasta,"memoria-",".tmp");
            try(ObjectOutputStream saida=new ObjectOutputStream(Files.newOutputStream(temporario))) {
                saida.writeUTF(chave); saida.writeObject(new MarcadoresSomaticos(marcadores.tabela));
            }
            try { Files.move(temporario,destino,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch(AtomicMoveNotSupportedException ex) { Files.move(temporario,destino,StandardCopyOption.REPLACE_EXISTING); }
        } catch(Exception ex) { throw new RemoteException("Falha ao salvar",ex); }
        finally { if(temporario!=null) try { Files.deleteIfExists(temporario); } catch(IOException ignorada) { } }
    }
    public synchronized MarcadoresSomaticos carregar(String chave) throws RemoteException {
        try {
            Path caminho=arquivo(chave); if(!Files.exists(caminho)) return null;
            try(ObjectInputStream entrada=new ObjectInputStream(Files.newInputStream(caminho))) {
                if(!chave.equals(entrada.readUTF())) throw new IOException("Chave inconsistente");
                return (MarcadoresSomaticos)entrada.readObject();
            }
        } catch(Exception ex) { throw new RemoteException("Falha ao carregar",ex); }
    }
    public synchronized List<String> listar() throws RemoteException {
        List<String> chaves=new ArrayList<String>();
        try(DirectoryStream<Path> arquivos=Files.newDirectoryStream(pasta,"*.ser")) {
            for(Path caminho:arquivos) try(ObjectInputStream entrada=new ObjectInputStream(Files.newInputStream(caminho))) { chaves.add(entrada.readUTF()); }
            Collections.sort(chaves); return chaves;
        } catch(IOException ex) { throw new RemoteException("Falha ao listar",ex); }
    }
}
