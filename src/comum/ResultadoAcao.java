package comum;
import java.io.Serializable;
public final class ResultadoAcao implements Serializable {
    private static final long serialVersionUID = 1L;
    public final boolean moveu, colidiu, pedraRecolhida, limiteAtingido;
    public final String mensagem;
    public ResultadoAcao(boolean moveu, boolean colidiu, boolean pedraRecolhida, boolean limiteAtingido, String mensagem) {
        this.moveu=moveu; this.colidiu=colidiu; this.pedraRecolhida=pedraRecolhida;
        this.limiteAtingido=limiteAtingido; this.mensagem=mensagem;
    }
}
