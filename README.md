# Robô homeostático distribuído com Java RMI

Implementação textual com quatro processos: ambiente, memória somática, monitor
por callback e robô. Aluno informado: **Jefferson Silva**; RA ainda não informado.

**Pendência de integração:** `src/comum` é uma reconstrução local do contrato
descrito no PDF. O pacote oficial não veio no material recebido. Consulte
`src/comum/LEIA-ME.md` e `VALIDACAO_ENUNCIADO.md` antes do laboratório.

## Requisitos e compilação

Aplicação: somente biblioteca padrão Java, código compatível com Java 8.
Validado com Oracle JDK **25.0.1**, compilação `--release 8` (sem dependências).
Os avisos do JDK 25 sobre o alvo 8 ser antigo não são erros.

Windows/PowerShell, na pasta do projeto:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/compilar.ps1
```

Linux/macOS (comando do enunciado):

```bash
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name '*.java')
```

Se usar JDK 8 no Windows, compile diretamente (o script com `--release` requer 9+):

```powershell
$fontes = Get-ChildItem src -Recurse -Filter *.java | ForEach-Object FullName
javac -encoding UTF-8 -d bin $fontes
```

## Iniciar os quatro processos

Execute cada comando em um terminal diferente, a partir desta pasta, nesta ordem.
Os servidores criam seus Registries internamente; não é preciso `rmiregistry`.

1. Servidor do ambiente A:

```powershell
java -Djava.rmi.server.hostname=127.0.0.1 -Dsun.rmi.transport.tcp.responseTimeout=2500 -cp bin ambiente.ServidorAmbiente --mapa mapas/ambienteA.txt --nome AmbienteA --porta 1099 --porta-objeto 1100
```

2. Servidor de memória persistente:

```powershell
java -Djava.rmi.server.hostname=127.0.0.1 -cp bin memoria.ServidorMemoria --pasta dados/memoria --porta 1101 --porta-objeto 1102
```

3. Monitor (exporta objeto remoto, depois registra o stub no ambiente):

```powershell
java -Djava.rmi.server.hostname=127.0.0.1 -cp bin monitor.Monitor --host localhost --porta 1099 --nome AmbienteA --porta-objeto 1103
```

4. Robô, treino de 30 episódios:

```powershell
java -cp bin robo.Robo --host localhost --porta 1099 --nome AmbienteA --id jefferson-treino --memoria-host localhost --memoria-porta 1101 --episodios 30 --semente 1 --chave treino-1 --csv resultados/manual_treino.csv
```

Não há GUI nem impressão do mapa. Encerre os servidores e o monitor com Ctrl+C.
No robô, uma falha remota interrompe a execução e preserva as linhas CSV já
concluídas. Não se repete automaticamente `executar`: uma resposta perdida pode
ter ocorrido depois de a ação ser aplicada, e a repetição duplicaria o passo.

## Transferir para o ambiente B

Inicie outro servidor (pode manter o A), outro monitor nas respectivas portas,
e mantenha/reinicie o servidor de memória com a mesma pasta:

```powershell
java -Djava.rmi.server.hostname=127.0.0.1 -Dsun.rmi.transport.tcp.responseTimeout=2500 -cp bin ambiente.ServidorAmbiente --mapa mapas/ambienteB.txt --nome AmbienteB --porta 1119 --porta-objeto 1120
java -Djava.rmi.server.hostname=127.0.0.1 -cp bin monitor.Monitor --host localhost --porta 1119 --nome AmbienteB --porta-objeto 1123
```

Os dois comandos abaixo terminam após um episódio cada. A condição ingênua
também aprende durante esse primeiro episódio; só difere na inicialização.

```powershell
java -cp bin robo.Robo --porta 1119 --nome AmbienteB --id ingenuo-1 --episodios 1 --semente 1 --carregar false --salvar false --condicao ingenuo --csv resultados/manual_ingenuo.csv
java -cp bin robo.Robo --porta 1119 --nome AmbienteB --id transferido-1 --episodios 1 --semente 1 --carregar true --salvar false --chave treino-1 --condicao transferido --csv resultados/manual_transferido.csv
```

## Máquinas diferentes e servidor do professor

Substitua `127.0.0.1` pelo IP alcançável da máquina **que exporta** cada objeto,
inclusive no monitor. O robô usa `--host IP_AMBIENTE --porta PORTA_REGISTRY
--nome NOME_REGISTRADO --id ID_UNICO --memoria-host IP_MEMORIA --memoria-porta PORTA`.
Os nomes da memória também podem ser passados em `--memoria-nome` e, no servidor,
`--nome`. Nenhum endereço está fixado no código do robô.

As portas Registry/objeto precisam estar acessíveis entre as máquinas. As portas
dos objetos são fixas por padrão e ajustáveis por `--porta-objeto`. A propriedade
`sun.rmi.transport.tcp.responseTimeout` é uma proteção adicional do Oracle/OpenJDK;
o isolamento dos callbacks já é feito pela aplicação e não depende dela.

Antes de conectar ao professor, substituir a reconstrução pelo pacote oficial
inalterado, adaptar o código do grupo aos DTOs oficiais e recompilar. A simples
igualdade dos nomes das interfaces não garante a serialização dos DTOs.

Use um ID por processo de robô. Repetir `iniciarEpisodio` com o mesmo ID reinicia a
sessão, como no contrato; dois clientes usando o mesmo ID vão compartilhar e
interferir na mesma sessão. O contrato não oferece autenticação nem token de posse.

## Verificações e reprodução dos experimentos

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/testar.ps1
python scripts/experimentos.py
```

O segundo comando é um utilitário opcional de orquestração em **Python 3, somente
biblioteca padrão**. A simulação e toda comunicação entre seus processos são Java
RMI. Ele executa 30 episódios para sementes 1–5, reinicia a JVM da memória e faz
cinco pares ingênuo/transferido no B. Usa portas 22099–22113 e encerra somente as
JVMs que criou. Gera CSVs, logs, marcadores e `resumo.json` em `resultados/`.
Uma nova execução sobrescreve os resultados dessas sementes.

Os marcadores são novos no início de cada semente, persistem entre episódios e
são salvos no servidor ao fim do treino. Corpo e memória espacial são reiniciados
a cada episódio. Os números de passos, colisões, cobertura, pedras, revisitas e
motivo vêm de `encerrarEpisodio`; energia vem do corpo do robô.

Resultados obtidos: 150/150 treinos com cobertura 100% e cinco pedras. No B,
colisões médias 5,6 (ingênuo) versus 0,8 (transferido); passos 67,4 versus 58,0.
Não se deve interpretar esse resultado como garantia para qualquer mapa.

## Organização

- `src/comum`: interfaces e DTOs locais provisórios, com aviso de compatibilidade.
- `src/ambiente`: mapa, sessões, serviço RMI e callbacks assíncronos.
- `src/memoria`: persistência por chave, escrita temporária e substituição atômica.
- `src/robo`: corpo, emoções, odometria/memória, marcadores e cliente.
- `src/monitor`, `src/infra`: observador e utilitários de linha de comando/RMI.
- `mapas`: entradas usadas exclusivamente pelo servidor.
- `resultados`: evidências reais dos experimentos.
- `testes`: verificações de regras e integração RMI.
- `relatorio`: texto editável, figuras e dados de autoria.
- `entrega`: PDF e ZIP para revisão antes do envio.
- `rmi`: exemplos da aula recebidos do usuário, preservados e fora da entrega.

## Atualizar capa e gerar PDF

Edite `relatorio/integrantes.json` com os nomes/RAs reais e a divisão confirmada.
Para regenerar o PDF e o ZIP, ferramentas de documentação (separadas da aplicação):

```powershell
python -m pip install matplotlib reportlab
python scripts/gerar_relatorio.py
python scripts/empacotar.py
```

O relatório explica explicitamente a alteração na deliberação por tédio, a
codificação local dos marcadores, o caráter abstrato das emoções e as pendências.
