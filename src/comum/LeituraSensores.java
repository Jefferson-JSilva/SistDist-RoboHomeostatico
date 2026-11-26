package comum;
import java.io.Serializable;
public final class LeituraSensores implements Serializable {
    private static final long serialVersionUID = 1L;
    public final boolean frente, direita, esquerda, tras, pedra;
    public LeituraSensores(boolean frente, boolean direita, boolean esquerda, boolean tras, boolean pedra) {
        this.frente=frente; this.direita=direita; this.esquerda=esquerda; this.tras=tras; this.pedra=pedra;
    }
}
