# Validação do enunciado e estado da entrega

Documento: `Enunciado_Tarefa_Robo_Homeostatico_RMI.pdf`, 15 páginas, novembro de 2026.
O documento foi tratado como especificação da atividade solicitada pelo usuário.

## Exigências

| Item | Evidência preparada | Situação |
|---|---|---|
| Relatório PDF de 10 a 20 páginas | `entrega/Relatorio_Robo_Homeostatico.pdf` | Gerado pelo script do relatório |
| Capa com nomes e RAs, grupo de 5 a 6 | Jefferson Silva informado | RA e demais integrantes pendentes |
| Código Java, biblioteca padrão, quatro JVMs | `src/ambiente`, `src/memoria`, `src/monitor`, `src/robo` | Implementado e executado |
| Pacote comum oficial inalterado | Apenas interfaces e descrições no PDF | **Pendente: pacote oficial não recebido** |
| Sensores relativos, odometria, sem acesso do robô ao mapa | `MemoriaEspacial`, `Robo` | Implementado |
| Sem GUI nem matriz na tela | Logs textuais; gráficos só no relatório | Atendido |
| Sessões, concorrência, callback não bloqueante | `ServicoAmbiente`, `Notificador`, `testes/Testes.java` | Testado |
| Três variáveis, pelo menos quatro emoções efetivas | `Corpo`, `Emocao`, `Cerebro` | Implementado |
| Aprendizado pela variação de bem-estar | `Cerebro.aprender` | Implementado; marcadores iniciais zero |
| 30 episódios e 5 sementes | `resultados/treino_consolidado.csv` | 150 episódios reais |
| Persistência e teste em outro mapa | Memória reiniciada; 5 pares no B | 10 episódios reais |
| CSVs, mapas, README, ZIP | Pastas e arquivos de entrega | Preparados |
| Duas indagações respondidas e referências | Relatório | Incluídas |
| Laboratório no servidor do professor | Depende do contrato e do acesso | Não realizado |

## Ambiguidades e decisões

1. A figura do A informa 7 obstáculos, 5 pedras e 29 células livres. São 29
   **transitáveis no total**, incluindo 5 pedras e a origem; há 23 pontos `.`.
   A cobertura usa visitadas/29, contando a origem.
2. Não há data de entrega ou de laboratório preenchida. Não se inferiu prazo.
3. O contrato das interfaces está explícito, mas os DTOs não têm especificação
   completa de campos, acessores, construtores ou UID. A reconstrução local não
   permite afirmar interoperabilidade com o professor.
4. O corpo inicia em E=100, I=100, T=0. A energia zero não encerra o episódio,
   pois o enunciado só impõe o limite de 400 passos. Valores são saturados em 0–100.
5. Revisita conta entrada com movimento em célula já visitada; recolher e colidir
   não incrementam revisitas. Recolher custa um passo e uma unidade de energia.
6. O robô pode encerrar antes de 400 quando não restar fronteira conhecida e a
   célula atual não tiver pedra; em mapas conexos isso certifica exploração total.
   O servidor calcula as métricas reais no encerramento.
7. Ajuste explícito ao modelo de referência: a deliberação por tédio usa T>=50
   mesmo quando outra emoção é dominante. Evita suprimir a exploração quando
   energia/integridade estão baixas. O relatório explica a escolha.
8. Os exemplos RMI recebidos foram mantidos intactos em `rmi/`. Não fazem parte
   do código autoral da atividade nem são o contrato do robô.

## Pendências que dependem do aluno/professor

- Obter o pacote `comum` oficial e validar a integração sem alterá-lo.
- Informar o RA de Jefferson Silva e, conforme a regra da turma, os outros
  integrantes e a divisão real de tarefas. Não foram inventados nomes ou RAs.
- Confirmar as datas em branco e realizar a apresentação/teste no laboratório.
- Estudar o código e revisar o relatório antes da entrega no ambiente virtual.

Nenhuma entrega externa foi enviada automaticamente.
