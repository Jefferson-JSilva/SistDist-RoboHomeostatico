"""Empacota somente os artefatos da atividade, preservando o material recebido."""
from pathlib import Path
import zipfile, hashlib
raiz=Path(__file__).resolve().parents[1]
entrega=raiz/'entrega'; entrega.mkdir(exist_ok=True)
pastas=['src','mapas','resultados','testes','scripts','relatorio','dados/experimentos']
arquivos=[raiz/'README.md',raiz/'VALIDACAO_ENUNCIADO.md',entrega/'Relatorio_Robo_Homeostatico.pdf']
for pasta in pastas:
    arquivos.extend(p for p in (raiz/pasta).rglob('*') if p.is_file() and '__pycache__' not in p.parts and 'revisao' not in p.parts)
zip_path=entrega/'RoboHomeostatico_Entrega.zip'
with zipfile.ZipFile(zip_path,'w',zipfile.ZIP_DEFLATED) as pacote:
    for p in sorted(set(arquivos)):
        pacote.write(p,'RoboHomeostatico/'+p.relative_to(raiz).as_posix())
checks=[]
for p in [zip_path,entrega/'Relatorio_Robo_Homeostatico.pdf']:
    checks.append(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name)
(entrega/'SHA256SUMS.txt').write_text('\n'.join(checks)+'\n',encoding='ascii')
print(zip_path)
print('Arquivos:',len(set(arquivos)),'Bytes:',zip_path.stat().st_size)
