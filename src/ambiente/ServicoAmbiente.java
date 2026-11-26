package ambiente;
import comum.*;
import java.rmi.*;
import java.util.concurrent.*;
public final class ServicoAmbiente implements AmbienteRemoto {
    private final Mapa mapa;
    private final String nome;
    private final ConcurrentMap<String,Sessao> sessoes=new ConcurrentHashMap<String,Sessao>();
    private final Notificador notificador=new Notificador();
    public ServicoAmbiente(Mapa mapa,String nome) { this.mapa=mapa; this.nome=nome; }
    public String nome() { return nome; }
    private Sessao sessao(String id) throws RemoteException {
        if(id==null) throw new RemoteException("ID nulo");
        Sessao sessao=sessoes.get(id);
        if(sessao==null) throw new RemoteException("Inicie o episodio antes de agir");
        return sessao;
    }
    public Pose iniciarEpisodio(String id,int episodio) throws RemoteException {
        if(id==null||id.trim().isEmpty()||id.length()>120||episodio<1) throw new RemoteException("ID/episodio invalido");
        sessoes.put(id,new Sessao(mapa,episodio));
        notificador.publicar(new EventoRobo("INICIO",episodio,0,"id="+id,null)); return mapa.inicio;
    }
    public LeituraSensores lerSensores(String id) throws RemoteException { Sessao s=sessao(id); synchronized(s) { return s.sensores(); } }
    public ResultadoAcao executar(String id,Acao acao) throws RemoteException {
        if(acao==null) throw new RemoteException("Acao nula");
        Sessao s=sessao(id); synchronized(s) {
            ResultadoAcao resultado=s.executar(acao);
            notificador.publicar(new EventoRobo("ACAO",s.episodio,s.passos,"id="+id+" "+resultado.mensagem,null)); return resultado;
        }
    }
    public void publicarEstado(String id,EstadoInterno estado) throws RemoteException {
        Sessao s=sessao(id); synchronized(s) { notificador.publicar(new EventoRobo("ESTADO",s.episodio,s.passos,"id="+id,estado)); }
    }
    public RelatorioEpisodio encerrarEpisodio(String id) throws RemoteException {
        Sessao s=sessao(id); synchronized(s) {
            RelatorioEpisodio r=s.relatorio();
            notificador.publicar(new EventoRobo("FIM",s.episodio,s.passos,"id="+id+" "+r.motivoTermino,null)); return r;
        }
    }
    public void registrarMonitor(MonitorRemoto monitor) throws RemoteException { notificador.registrar(monitor); }
    public void removerMonitor(MonitorRemoto monitor) { notificador.remover(monitor); }
}
