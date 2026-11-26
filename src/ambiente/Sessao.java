package ambiente;
import comum.*;
import infra.Movimento;
final class Sessao {
    final Mapa mapa;
    final int episodio;
    final boolean[][] pedras=new boolean[6][6], visitadas=new boolean[6][6];
    int linha,coluna,passos,colisoes,recolhidas,revisitas,visitadasTotal=1;
    Direcao orientacao;
    boolean encerrada;
    Sessao(Mapa mapa,int episodio) {
        this.mapa=mapa; this.episodio=episodio;
        linha=mapa.inicio.linha; coluna=mapa.inicio.coluna; orientacao=mapa.inicio.orientacao;
        visitadas[linha][coluna]=true;
        for(int l=0;l<6;l++) for(int c=0;c<6;c++) pedras[l][c]=mapa.celulas[l][c]=='P';
    }
    boolean sensor(Acao acao) {
        int d=Movimento.direcao(orientacao,acao);
        return mapa.bloqueada(linha+Movimento.LINHAS[d],coluna+Movimento.COLUNAS[d]);
    }
    LeituraSensores sensores() { return new LeituraSensores(sensor(Acao.FRENTE),sensor(Acao.DIREITA),sensor(Acao.ESQUERDA),sensor(Acao.TRAS),pedras[linha][coluna]); }
    ResultadoAcao executar(Acao acao) {
        if(encerrada||passos>=400) return new ResultadoAcao(false,false,false,true,"Episodio encerrado; acao nao aplicada");
        boolean moveu=false,colidiu=false,recolheu=false;
        passos++;
        if(acao==Acao.RECOLHER) {
            if(pedras[linha][coluna]) { pedras[linha][coluna]=false; recolhidas++; recolheu=true; }
        } else {
            int d=Movimento.direcao(orientacao,acao); orientacao=Direcao.values()[d];
            int l=linha+Movimento.LINHAS[d],c=coluna+Movimento.COLUNAS[d];
            if(mapa.bloqueada(l,c)) { colidiu=true; colisoes++; }
            else { linha=l; coluna=c; moveu=true;
                if(visitadas[l][c]) revisitas++; else { visitadas[l][c]=true; visitadasTotal++; }
            }
        }
        if(passos==400) encerrada=true;
        return new ResultadoAcao(moveu,colidiu,recolheu,passos>=400,
            acao+" moveu="+moveu+" colidiu="+colidiu+" pedra="+recolheu);
    }
    RelatorioEpisodio relatorio() {
        encerrada=true;
        String motivo=passos>=400?"LIMITE_400":visitadasTotal==mapa.livres&&recolhidas==mapa.pedras?"OBJETIVOS_CONCLUIDOS":"ENCERRADO_PELO_CLIENTE";
        return new RelatorioEpisodio(episodio,passos,colisoes,visitadasTotal,recolhidas,revisitas,mapa.livres,motivo);
    }
}
