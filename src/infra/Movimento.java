package infra;
import comum.*;
public final class Movimento {
    public static final int[] LINHAS={-1,0,1,0}, COLUNAS={0,1,0,-1};
    private Movimento() { }
    public static int direcao(Direcao atual,Acao acao) {
        int giro=acao==Acao.DIREITA?1:acao==Acao.ESQUERDA?3:acao==Acao.TRAS?2:0;
        return (atual.ordinal()+giro)%4;
    }
    public static boolean dentro(int linha,int coluna) { return linha>=0&&linha<6&&coluna>=0&&coluna<6; }
}
