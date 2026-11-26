package robo;
import comum.*;
import infra.Movimento;
import java.util.*;
public final class MemoriaEspacial {
    private final boolean[][] visitadas=new boolean[6][6], livres=new boolean[6][6];
    public int linha,coluna;
    public Direcao orientacao;
    public MemoriaEspacial(Pose inicio) {
        linha=inicio.linha; coluna=inicio.coluna; orientacao=inicio.orientacao;
        visitadas[linha][coluna]=true; livres[linha][coluna]=true;
    }
    public boolean bloqueio(LeituraSensores sensores,Acao acao) {
        switch(acao) { case FRENTE:return sensores.frente; case DIREITA:return sensores.direita;
            case ESQUERDA:return sensores.esquerda; case TRAS:return sensores.tras; default:return false; }
    }
    public void observar(LeituraSensores sensores) {
        for(int a=0;a<4;a++) {
            Acao acao=Acao.values()[a]; int d=Movimento.direcao(orientacao,acao);
            int l=linha+Movimento.LINHAS[d],c=coluna+Movimento.COLUNAS[d];
            if(Movimento.dentro(l,c)) livres[l][c]=!bloqueio(sensores,acao);
        }
    }
    public boolean visitada(Acao acao) {
        int d=Movimento.direcao(orientacao,acao),l=linha+Movimento.LINHAS[d],c=coluna+Movimento.COLUNAS[d];
        return Movimento.dentro(l,c)&&visitadas[l][c];
    }
    public boolean atualizar(Acao acao,ResultadoAcao resultado) {
        if(acao==Acao.RECOLHER) return false;
        int d=Movimento.direcao(orientacao,acao); orientacao=Direcao.values()[d];
        if(!resultado.moveu) return false;
        linha+=Movimento.LINHAS[d]; coluna+=Movimento.COLUNAS[d];
        boolean nova=!visitadas[linha][coluna]; visitadas[linha][coluna]=true; livres[linha][coluna]=true; return nova;
    }
    public boolean temVizinhoNovo(LeituraSensores sensores) {
        for(int a=0;a<4;a++) if(!bloqueio(sensores,Acao.values()[a])&&!visitada(Acao.values()[a])) return true;
        return false;
    }
    public boolean concluida() {
        for(int l=0;l<6;l++) for(int c=0;c<6;c++) if(livres[l][c]&&!visitadas[l][c]) return false;
        return true;
    }
    public Acao caminhoAteFronteira() {
        int origem=linha*6+coluna;
        int[] anterior=new int[36]; Arrays.fill(anterior,-1); anterior[origem]=origem;
        Queue<Integer> fila=new ArrayDeque<Integer>(); fila.add(origem);
        int destino=-1;
        while(!fila.isEmpty()) {
            int p=fila.remove(); if(!visitadas[p/6][p%6]) { destino=p; break; }
            for(int d=0;d<4;d++) {
                int l=p/6+Movimento.LINHAS[d],c=p%6+Movimento.COLUNAS[d];
                if(Movimento.dentro(l,c)&&livres[l][c]&&anterior[l*6+c]<0) { anterior[l*6+c]=p; fila.add(l*6+c); }
            }
        }
        if(destino<0) return null;
        while(anterior[destino]!=origem) destino=anterior[destino];
        for(int a=0;a<4;a++) { int d=Movimento.direcao(orientacao,Acao.values()[a]);
            if(linha+Movimento.LINHAS[d]==destino/6&&coluna+Movimento.COLUNAS[d]==destino%6) return Acao.values()[a];
        }
        return null;
    }
}
