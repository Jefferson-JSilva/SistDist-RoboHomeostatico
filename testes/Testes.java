package testes;
import comum.*;
import robo.*;
import ambiente.*;
import memoria.*;
import java.nio.file.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.*;
public final class Testes {
    static int verificacoes;
    static void exigir(boolean condicao,String mensagem) { verificacoes++; if(!condicao) throw new AssertionError(mensagem); }
    static void perto(double a,double b,String mensagem) { exigir(Math.abs(a-b)<1e-8,mensagem+": "+a); }
    public static void main(String[] argumentos) throws Exception {
        Corpo corpo=new Corpo(); corpo.energia=80; corpo.tedio=40;
        corpo.sentir(new ResultadoAcao(false,true,false,false,""),false);
        perto(corpo.recompensa,-.395,"Recompensa colisao"); exigir(corpo.emocao==Emocao.MEDO,"Medo");
        corpo=new Corpo(); corpo.energia=80; corpo.tedio=40;
        corpo.sentir(new ResultadoAcao(true,false,false,false,""),true);
        perto(corpo.recompensa,.29,"Recompensa novidade"); exigir(corpo.emocao==Emocao.ALEGRIA,"Alegria");
        corpo=new Corpo(); corpo.energia=80; corpo.tedio=40;
        corpo.sentir(new ResultadoAcao(true,false,false,false,""),false);
        perto(corpo.recompensa,-.11,"Recompensa revisita"); exigir(corpo.emocao==Emocao.TEDIO,"Tedio");
        corpo=new Corpo(); corpo.energia=70; corpo.integridade=90; corpo.tedio=20;
        corpo.sentir(new ResultadoAcao(false,false,true,false,""),false); perto(corpo.recompensa,.17,"Recolher");
        corpo=new Corpo(); corpo.energia=24;
        corpo.sentir(new ResultadoAcao(false,false,false,false,""),false);
        exigir(corpo.emocao==Emocao.ANGUSTIA,"Angustia"); perto(corpo.emocao.temperatura,.05,"Temperatura angustia");
        corpo=new Corpo(); corpo.sentir(new ResultadoAcao(false,false,false,false,""),false);
        exigir(corpo.emocao==Emocao.CALMA,"Calma"); perto(Emocao.TEDIO.temperatura,.30,"Temperatura tedio");
        corpo.energia=0; corpo.integridade=0;
        corpo.sentir(new ResultadoAcao(false,true,false,false,""),false);
        perto(corpo.energia,0,"Energia saturada"); perto(corpo.integridade,0,"Integridade saturada");
        Cerebro cerebro=new Cerebro(1,null); MemoriaEspacial espacial=new MemoriaEspacial(new Pose(0,0,Direcao.NORTE));
        LeituraSensores sensores=new LeituraSensores(true,false,true,false,false);
        cerebro.aprender("teste",Acao.FRENTE,-.395,sensores,espacial,true); perto(cerebro.valor("teste",Acao.FRENTE),-.1975,"TD terminal");
        Set<Acao> escolhidas=new HashSet<Acao>(); cerebro=new Cerebro(1,null);
        for(int i=0;i<1000;i++) escolhidas.add(cerebro.escolher(sensores,espacial,new Corpo()));
        exigir(escolhidas.size()==4,"Politica inicial nao filtra obstaculos");
        ServicoAmbiente local=new ServicoAmbiente(new Mapa(Paths.get("mapas/ambienteA.txt")),"teste");
        AmbienteRemoto remoto=(AmbienteRemoto)UnicastRemoteObject.exportObject(local,0);
        MonitorRemoto lento=evento -> { try { Thread.sleep(6000); } catch(InterruptedException ex) { Thread.currentThread().interrupt(); } };
        MonitorRemoto falho=evento -> { throw new RemoteException("Falha simulada"); };
        CountDownLatch recebido=new CountDownLatch(1);
        MonitorRemoto rapido=evento -> recebido.countDown();
        List<MonitorRemoto> objetos=Arrays.asList(lento,falho,rapido);
        try {
            for(MonitorRemoto m:objetos) remoto.registrarMonitor((MonitorRemoto)UnicastRemoteObject.exportObject(m,0));
            long inicio=System.nanoTime(); remoto.iniciarEpisodio("a",1); remoto.iniciarEpisodio("b",1);
            exigir(remoto.lerSensores("a").frente,"Sensor borda norte");
            ResultadoAcao r=remoto.executar("a",Acao.ESQUERDA); exigir(r.colidiu,"Colisao oeste");
            exigir(remoto.lerSensores("a").direita,"Orientacao muda na colisao");
            exigir(!remoto.lerSensores("b").direita,"Sessao b independente");
            exigir(remoto.executar("a",Acao.TRAS).moveu,"Mover leste apos colisao oeste");
            exigir(recebido.await(2,TimeUnit.SECONDS),"Callback remoto recebido");
            exigir(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-inicio)<3000,"Monitor lento nao bloqueia");
            remoto.iniciarEpisodio("pedras",1);
            remoto.executar("pedras",Acao.TRAS); remoto.executar("pedras",Acao.FRENTE); remoto.executar("pedras",Acao.FRENTE);
            exigir(remoto.lerSensores("pedras").pedra,"Pedra em (3,0)");
            exigir(remoto.executar("pedras",Acao.RECOLHER).pedraRecolhida,"Recolheu");
            exigir(!remoto.executar("pedras",Acao.RECOLHER).pedraRecolhida,"Sem coleta dupla");
            remoto.iniciarEpisodio("pedras",2);
            remoto.executar("pedras",Acao.TRAS); remoto.executar("pedras",Acao.FRENTE); remoto.executar("pedras",Acao.FRENTE);
            exigir(remoto.lerSensores("pedras").pedra,"Pedra reposta");
            remoto.iniciarEpisodio("concorrente",1);
            ExecutorService grupo=Executors.newFixedThreadPool(8); List<Future<?>> tarefas=new ArrayList<Future<?>>();
            for(int i=0;i<8;i++) tarefas.add(grupo.submit(() -> { for(int j=0;j<75;j++) try { remoto.executar("concorrente",Acao.RECOLHER); } catch(Exception ex) { throw new RuntimeException(ex); } }));
            for(Future<?> f:tarefas) f.get(); grupo.shutdown();
            RelatorioEpisodio relatorio=remoto.encerrarEpisodio("concorrente");
            exigir(relatorio.passos==400,"Limite atomico sob concorrencia"); exigir(relatorio.celulasVisitadas==1,"Contagem da origem");
            exigir(relatorio.celulasLivres==29,"Denominador cobertura");
            Path pasta=Files.createTempDirectory("memoria-robo-teste-");
            try {
                ServicoMemoria memoria=new ServicoMemoria(pasta); memoria.salvar("teste",cerebro.exportar());
                ServicoMemoria nova=new ServicoMemoria(pasta); exigir(nova.carregar("teste")!=null,"Persistencia em nova instancia");
                exigir(nova.carregar("ausente")==null,"Chave ausente"); exigir(nova.listar().equals(Arrays.asList("teste")),"Listagem");
            } finally { try(DirectoryStream<Path> arquivos=Files.newDirectoryStream(pasta)) { for(Path p:arquivos) Files.delete(p); } Files.delete(pasta); }
            System.out.println("APROVADO: "+verificacoes+" verificacoes; RMI real, concorrencia, callback, corpo e persistencia.");
        } finally {
            for(MonitorRemoto m:objetos) UnicastRemoteObject.unexportObject(m,true);
            UnicastRemoteObject.unexportObject(local,true);
        }
    }
}
