package comum;
import java.io.Serializable;
public final class EventoRobo implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String tipo, descricao;
    public final int episodio, passo;
    public final EstadoInterno estado;
    public EventoRobo(String tipo, int episodio, int passo, String descricao, EstadoInterno estado) {
        this.tipo=tipo; this.episodio=episodio; this.passo=passo; this.descricao=descricao; this.estado=estado;
    }
}
