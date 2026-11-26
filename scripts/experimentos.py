"""Orquestra JVMs reais; nao participa do algoritmo do robo. Python 3, biblioteca padrao."""
from pathlib import Path
import subprocess, time, sys, csv, json, statistics, os

RAIZ = Path(__file__).resolve().parents[1]
SAIDA = RAIZ / 'resultados'
SAIDA.mkdir(exist_ok=True)
PROCESSOS = []
ARQUIVOS = []
configuracao = subprocess.run(['java','-XshowSettings:properties','-version'],capture_output=True,text=True,check=True)
java_home = next(l.split('=',1)[1].strip() for l in configuracao.stderr.splitlines() if 'java.home =' in l)
# Usa o executavel real: o redirecionador javapath do Windows cria um processo filho.
executavel = str(Path(java_home)/'bin'/('java.exe' if os.name=='nt' else 'java'))
JAVA = [executavel, '-Djava.rmi.server.hostname=127.0.0.1', '-Dsun.rmi.transport.tcp.responseTimeout=2500', '-cp', str(RAIZ/'bin')]

def iniciar(nome, classe, *argumentos):
    caminho = SAIDA / (nome+'.log')
    arquivo = caminho.open('w', encoding='utf-8')
    ARQUIVOS.append(arquivo)
    processo = subprocess.Popen(JAVA+[classe]+list(argumentos), cwd=RAIZ, stdout=arquivo, stderr=subprocess.STDOUT,
        creationflags=getattr(subprocess,'CREATE_NO_WINDOW',0))
    PROCESSOS.append(processo)
    prazo=time.monotonic()+20
    while time.monotonic()<prazo:
        if processo.poll() is not None: raise RuntimeError(caminho.read_text(encoding='utf-8',errors='replace'))
        if 'PRONTO' in caminho.read_text(encoding='utf-8',errors='replace'): return processo
        time.sleep(.1)
    raise TimeoutError('Inicializacao: '+nome)

def robo(nome, ambiente, porta, semente, episodios, carregar=False, condicao='treino'):
    argumentos=['robo.Robo','--porta',str(porta),'--nome',ambiente,'--memoria-porta','22101',
        '--id',nome,'--semente',str(semente),'--episodios',str(episodios),'--csv',str(SAIDA/(nome+'.csv')),
        '--chave','semente-'+str(semente),'--carregar',str(carregar).lower(),'--salvar',str(condicao=='treino').lower(),
        '--condicao',condicao]
    with (SAIDA/(nome+'.log')).open('w',encoding='utf-8') as saida:
        subprocess.run(JAVA+argumentos,cwd=RAIZ,stdout=saida,stderr=subprocess.STDOUT,check=True,timeout=180)

def resumir():
    treino=[]; transferencia=[]
    for s in range(1,6):
        with (SAIDA/f'treino_{s}.csv').open() as f: treino.extend(list(csv.DictReader(f)))
        for condicao in ['ingenuo','transferido']:
            with (SAIDA/f'{condicao}_{s}.csv').open() as f: transferencia.extend(list(csv.DictReader(f)))
    metricas=['passos','colisoes','cobertura','pedras','revisitas','energia_final']
    def agregar(linhas):
        return {m:{'media':statistics.mean(float(l[m]) for l in linhas),'dp':statistics.stdev(float(l[m]) for l in linhas) if len(linhas)>1 else 0} for m in metricas}
    resumo={'treino_primeiros5':agregar([l for l in treino if int(l['episodio'])<=5]),
        'treino_ultimos5':agregar([l for l in treino if int(l['episodio'])>25]),
        'transferencia':{c:agregar([l for l in transferencia if l['condicao']==c]) for c in ['ingenuo','transferido']},
        'episodios':{str(e):agregar([l for l in treino if int(l['episodio'])==e]) for e in range(1,31)},
        'sucessos_treino':sum(l['motivo_termino']=='OBJETIVOS_CONCLUIDOS' for l in treino),
        'total_treino':len(treino), 'total_transferencia':len(transferencia)}
    (SAIDA/'resumo.json').write_text(json.dumps(resumo,ensure_ascii=False,indent=2),encoding='utf-8')
    for nome,linhas in [('treino_consolidado',treino),('transferencia_consolidada',transferencia)]:
        with (SAIDA/(nome+'.csv')).open('w',newline='',encoding='utf-8') as f:
            w=csv.DictWriter(f,fieldnames=linhas[0].keys()); w.writeheader(); w.writerows(linhas)
    print(json.dumps({k:v for k,v in resumo.items() if k!='episodios'},ensure_ascii=False,indent=2),flush=True)

if __name__=='__main__':
    try:
        iniciar('servidorA','ambiente.ServidorAmbiente','--porta','22099','--porta-objeto','22100','--nome','AmbienteA')
        memoria=iniciar('memoria','memoria.ServidorMemoria','--porta','22101','--porta-objeto','22102','--pasta','dados/experimentos')
        iniciar('monitorA','monitor.Monitor','--porta','22099','--nome','AmbienteA','--porta-objeto','22103')
        for s in range(1,6):
            robo(f'treino_{s}','AmbienteA',22099,s,30)
            print(f'Treino semente {s}: concluido',flush=True)
        # Reinicio real: a transferencia precisa ler o arquivo persistido, nao um cache.
        memoria.terminate(); memoria.wait(timeout=10)
        iniciar('memoria_reiniciada','memoria.ServidorMemoria','--porta','22101','--porta-objeto','22102','--pasta','dados/experimentos')
        iniciar('servidorB','ambiente.ServidorAmbiente','--porta','22109','--porta-objeto','22110','--nome','AmbienteB','--mapa','mapas/ambienteB.txt')
        iniciar('monitorB','monitor.Monitor','--porta','22109','--nome','AmbienteB','--porta-objeto','22113')
        for s in range(1,6):
            for condicao in ['ingenuo','transferido']:
                robo(f'{condicao}_{s}','AmbienteB',22109,s,1,condicao=='transferido',condicao)
        time.sleep(1)
        resumir()
    finally:
        for p in reversed(PROCESSOS):
            if p.poll() is None:
                p.terminate()
                try: p.wait(timeout=5)
                except subprocess.TimeoutExpired: p.kill(); p.wait()
        for f in ARQUIVOS: f.close()
