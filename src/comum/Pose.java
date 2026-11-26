package comum;
import java.io.Serializable;
public final class Pose implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int linha, coluna;
    public final Direcao orientacao;
    public Pose(int linha, int coluna, Direcao orientacao) {
        this.linha = linha; this.coluna = coluna; this.orientacao = orientacao;
    }
}
