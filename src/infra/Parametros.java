package infra;
import java.util.*;
public final class Parametros {
    private final Map<String,String> valores=new HashMap<String,String>();
    public Parametros(String[] argumentos) {
        if (argumentos.length%2!=0) throw new IllegalArgumentException("Use pares --opcao valor");
        for(int i=0;i<argumentos.length;i+=2) {
            if(!argumentos[i].startsWith("--")) throw new IllegalArgumentException("Opcao invalida: "+argumentos[i]);
            valores.put(argumentos[i].substring(2),argumentos[i+1]);
        }
    }
    public String texto(String chave,String padrao) { return valores.containsKey(chave)?valores.get(chave):padrao; }
    public int inteiro(String chave,int padrao) { return Integer.parseInt(texto(chave,""+padrao)); }
    public long longo(String chave,long padrao) { return Long.parseLong(texto(chave,""+padrao)); }
}
