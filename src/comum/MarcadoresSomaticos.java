package comum;
import java.io.Serializable;
import java.util.*;
public final class MarcadoresSomaticos implements Serializable {
    private static final long serialVersionUID = 1L;
    public final Map<String,double[]> tabela;
    public MarcadoresSomaticos(Map<String,double[]> origem) {
        tabela=new TreeMap<String,double[]>();
        for (Map.Entry<String,double[]> item:origem.entrySet()) tabela.put(item.getKey(),item.getValue().clone());
    }
}
