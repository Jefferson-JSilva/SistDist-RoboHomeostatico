package comum;
import java.io.Serializable;
import java.util.*;
public final class EstadoInterno implements Serializable {
    private static final long serialVersionUID = 1L;
    public final Map<String,Double> variaveis;
    public final String emocao;
    public final double bemEstar, recompensa;
    public EstadoInterno(Map<String,Double> variaveis, String emocao, double bemEstar, double recompensa) {
        this.variaveis=Collections.unmodifiableMap(new LinkedHashMap<String,Double>(variaveis));
        this.emocao=emocao; this.bemEstar=bemEstar; this.recompensa=recompensa;
    }
}
