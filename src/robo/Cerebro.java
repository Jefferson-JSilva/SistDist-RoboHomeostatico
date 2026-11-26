package robo;
import comum.*;
import java.util.*;
public final class Cerebro {
    private final Map<String,double[]> tabela=new TreeMap<String,double[]>();
    private final Random aleatorio;
    public String decisao="MARCADORES";
    public Cerebro(long semente,MarcadoresSomaticos carregados) {
        aleatorio=new Random(semente);
        if(carregados!=null) for(Map.Entry<String,double[]> item:carregados.tabela.entrySet()) tabela.put(item.getKey(),item.getValue().clone());
    }
    // Chave local por acao: nao carrega coordenadas nem mapa para outro ambiente.
    public String situacao(LeituraSensores sensores,MemoriaEspacial memoria,Acao acao) {
        if(acao==Acao.RECOLHER) return "pedra="+sensores.pedra;
        return "bloqueio="+memoria.bloqueio(sensores,acao)+";visitada="+memoria.visitada(acao);
    }
    private double[] valores(String chave) {
        double[] valores=tabela.get(chave);
        if(valores==null) { valores=new double[5]; tabela.put(chave,valores); } return valores;
    }
    public double valor(String chave,Acao acao) { return valores(chave)[acao.ordinal()]; }
    public Acao escolher(LeituraSensores sensores,MemoriaEspacial memoria,Corpo corpo) {
        if(sensores.pedra) { decisao="REFLEXO"; return Acao.RECOLHER; }
        // O impulso de tedio permanece ativo mesmo se medo/angustia forem dominantes.
        if(corpo.tedio>=50&&!memoria.temVizinhoNovo(sensores)) {
            Acao caminho=memoria.caminhoAteFronteira();
            if(caminho!=null) { decisao="MEMORIA"; return caminho; }
        }
        decisao="MARCADORES";
        double[] pesos=new double[4]; double maior=Double.NEGATIVE_INFINITY,total=0;
        for(int a=0;a<4;a++) { pesos[a]=valor(situacao(sensores,memoria,Acao.values()[a]),Acao.values()[a]); maior=Math.max(maior,pesos[a]); }
        // Nenhuma acao de movimento e eliminada por um sensor de obstaculo.
        for(int a=0;a<4;a++) { pesos[a]=Math.exp((pesos[a]-maior)/corpo.emocao.temperatura); total+=pesos[a]; }
        double sorteio=aleatorio.nextDouble()*total;
        for(int a=0;a<4;a++) { sorteio-=pesos[a]; if(sorteio<=0) return Acao.values()[a]; }
        return Acao.TRAS;
    }
    public void aprender(String anterior,Acao acao,double recompensa,LeituraSensores sensores,MemoriaEspacial memoria,boolean terminal) {
        double futuro=0;
        if(!terminal) {
            if(sensores.pedra) futuro=valor(situacao(sensores,memoria,Acao.RECOLHER),Acao.RECOLHER);
            else { futuro=Double.NEGATIVE_INFINITY;
                for(int a=0;a<4;a++) futuro=Math.max(futuro,valor(situacao(sensores,memoria,Acao.values()[a]),Acao.values()[a]));
            }
        }
        double[] valores=valores(anterior); int indice=acao.ordinal();
        valores[indice]+=0.5*(recompensa+0.9*futuro-valores[indice]);
    }
    public MarcadoresSomaticos exportar() { return new MarcadoresSomaticos(tabela); }
}
