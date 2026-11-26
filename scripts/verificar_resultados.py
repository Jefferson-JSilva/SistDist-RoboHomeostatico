"""Confere integridade dos CSVs e correspondencia com os callbacks registrados."""
from pathlib import Path
import csv, json, collections
raiz=Path(__file__).resolve().parents[1]
pasta=raiz/'resultados'
def ler(nome):
    with (pasta/nome).open(encoding='utf-8') as entrada: return list(csv.DictReader(entrada))
treino=ler('treino_consolidado.csv')
transferencia=ler('transferencia_consolidada.csv')
assert len(treino)==150 and len(transferencia)==10
assert {(int(r['semente']),int(r['episodio'])) for r in treino}=={(s,e) for s in range(1,6) for e in range(1,31)}
assert {(int(r['semente']),r['condicao']) for r in transferencia}=={(s,c) for s in range(1,6) for c in ['ingenuo','transferido']}
for r in treino+transferencia:
    assert 1<=int(r['passos'])<=400
    assert 0<=float(r['energia_final'])<=100
    assert abs(float(r['cobertura'])-100*int(r['celulas_visitadas'])/int(r['celulas_livres']))<1e-5
    assert int(r['passos'])==int(r['colisoes'])+int(r['revisitas'])+int(r['celulas_visitadas'])-1+int(r['pedras'])
    if r['motivo_termino']=='OBJETIVOS_CONCLUIDOS':
        assert int(r['celulas_visitadas'])==29 and int(r['pedras'])==5
resumo={}
for nome,linhas in [('A',treino),('B',transferencia)]:
    log=(pasta/f'monitor{nome}.log').read_text(encoding='utf-8')
    passos=sum(int(r['passos']) for r in linhas)
    assert log.count('tipo=ACAO ')==passos
    assert log.count('tipo=ESTADO ')==passos
    assert log.count('tipo=FIM ')==len(linhas)
    resumo[nome]={'episodios':len(linhas),'passos':passos,'acoes_monitor':log.count('tipo=ACAO '),'estados_monitor':log.count('tipo=ESTADO ')}
resumo['resultado']='APROVADO'
(pasta/'validacao_resultados.json').write_text(json.dumps(resumo,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(resumo,ensure_ascii=False,indent=2))
