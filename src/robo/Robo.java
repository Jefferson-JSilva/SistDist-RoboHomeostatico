package robo;
import comum.*;
import infra.*;
import java.rmi.registry.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
public final class Robo {
    public static void main(String[] argumentos) throws Exception {
        Parametros p=new Parametros(argumentos);
        String id=p.texto("id","robo-1"),chave=p.texto("chave","treino-1");
        int episodios=p.inteiro("episodios",30); if(episodios<1) throw new IllegalArgumentException("Episodios deve ser positivo");
        long semente=p.longo("semente",1);
        AmbienteRemoto ambiente=(AmbienteRemoto)LocateRegistry.getRegistry(p.texto("host","localhost"),p.inteiro("porta",1099)).lookup(p.texto("nome","AmbienteA"));
        MemoriaSomaticaRemota repositorio=(MemoriaSomaticaRemota)LocateRegistry.getRegistry(p.texto("memoria-host","localhost"),p.inteiro("memoria-porta",1101)).lookup(p.texto("memoria-nome",MemoriaSomaticaRemota.NOME_PADRAO));
        MarcadoresSomaticos carregados=null;
        if(Boolean.parseBoolean(p.texto("carregar","false"))) {
            carregados=repositorio.carregar(chave);
            if(carregados==null) throw new IllegalStateException("Memoria nao encontrada: "+chave);
        }
        Cerebro cerebro=new Cerebro(semente,carregados);
        Path csv=Paths.get(p.texto("csv","resultados/treino.csv"));
        if(csv.toAbsolutePath().getParent()!=null) Files.createDirectories(csv.toAbsolutePath().getParent());
        try(PrintWriter saida=new PrintWriter(Files.newBufferedWriter(csv,StandardCharsets.UTF_8))) {
            saida.println("semente,episodio,passos,colisoes,cobertura,pedras,revisitas,energia_final,motivo_termino,celulas_visitadas,celulas_livres,condicao");
            for(int episodio=1;episodio<=episodios;episodio++) {
                Corpo corpo=new Corpo(); MemoriaEspacial memoria=new MemoriaEspacial(ambiente.iniciarEpisodio(id,episodio));
                LeituraSensores sensores=ambiente.lerSensores(id); memoria.observar(sensores);
                for(int passo=0;passo<400;passo++) {
                    Acao acao=cerebro.escolher(sensores,memoria,corpo);
                    String anterior=cerebro.situacao(sensores,memoria,acao);
                    ResultadoAcao resultado=ambiente.executar(id,acao);
                    corpo.sentir(resultado,memoria.atualizar(acao,resultado));
                    LeituraSensores proximos=ambiente.lerSensores(id); memoria.observar(proximos);
                    boolean terminal=resultado.limiteAtingido||(memoria.concluida()&&!proximos.pedra);
                    cerebro.aprender(anterior,acao,corpo.recompensa,proximos,memoria,terminal);
                    ambiente.publicarEstado(id,corpo.estado(cerebro.decisao)); sensores=proximos;
                    if(terminal) break;
                }
                RelatorioEpisodio r=ambiente.encerrarEpisodio(id);
                saida.printf(Locale.ROOT,"%d,%d,%d,%d,%.6f,%d,%d,%.2f,%s,%d,%d,%s%n",semente,r.episodio,r.passos,r.colisoes,
                    100.0*r.celulasVisitadas/r.celulasLivres,r.pedrasRecolhidas,r.revisitas,corpo.energia,r.motivoTermino,
                    r.celulasVisitadas,r.celulasLivres,p.texto("condicao","treino")); saida.flush();
                System.out.printf(Locale.ROOT,"episodio=%d passos=%d colisoes=%d cobertura=%.2f pedras=%d fim=%s%n",episodio,r.passos,r.colisoes,100.0*r.celulasVisitadas/r.celulasLivres,r.pedrasRecolhidas,r.motivoTermino);
            }
        }
        if(Boolean.parseBoolean(p.texto("salvar","true"))) repositorio.salvar(chave,cerebro.exportar());
        Path marcadores=Paths.get(csv.toString().replaceFirst("\\.csv$","")+"_marcadores.csv");
        try(PrintWriter saida=new PrintWriter(Files.newBufferedWriter(marcadores,StandardCharsets.UTF_8))) {
            saida.println("situacao,acao,valor");
            for(Map.Entry<String,double[]> item:cerebro.exportar().tabela.entrySet()) for(Acao acao:Acao.values())
                saida.printf(Locale.ROOT,"%s,%s,%.9f%n",item.getKey(),acao,item.getValue()[acao.ordinal()]);
        }
    }
}
