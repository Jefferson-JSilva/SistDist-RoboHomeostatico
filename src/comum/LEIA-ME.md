# Contrato local provisório — NÃO é o pacote oficial

O material recebido contém o enunciado e exemplos RMI de banco/calculadora, mas
não contém as classes oficiais `comum` do robô. As três interfaces e os enums
foram transcritos do enunciado. As classes de dados foram reconstruídas para
permitir implementar e testar o projeto localmente.

Isso **não garante compatibilidade binária/serialização com o professor**:
construtores, campos, métodos e `serialVersionUID` não estão totalmente definidos
no PDF. Quando o pacote oficial chegar, preserve-o integralmente e adapte somente
os pacotes do grupo aos seus acessores/construtores. Recompile todas as JVMs,
refaça os testes e os experimentos e teste o lookup/chamadas no laboratório.

Não apresente este diretório como uma cópia inalterada do contrato oficial.
