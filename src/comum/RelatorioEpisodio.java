package comum;
import java.io.Serializable;
public final class RelatorioEpisodio implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int episodio, passos, colisoes, celulasVisitadas, pedrasRecolhidas, revisitas, celulasLivres;
    public final String motivoTermino;
    public RelatorioEpisodio(int episodio, int passos, int colisoes, int celulasVisitadas,
            int pedrasRecolhidas, int revisitas, int celulasLivres, String motivoTermino) {
        this.episodio=episodio; this.passos=passos; this.colisoes=colisoes; this.celulasVisitadas=celulasVisitadas;
        this.pedrasRecolhidas=pedrasRecolhidas; this.revisitas=revisitas; this.celulasLivres=celulasLivres;
        this.motivoTermino=motivoTermino;
    }
}
