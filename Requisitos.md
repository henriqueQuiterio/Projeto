Requisitos

1. Gestão de Calendário

   1.1. Definir Estádio para um Jogo: O sistema deve permitir registar um estádio para cada jogo.

   1.2. Definição de Data e Hora: O sistema deve permitir selecionar uma data e hora válidas, garantindo que não existem sobreposições de jogos no mesmo estádio.

   1.3. Listagem de Calendário: O sistema deve gerar uma visualização de todos os jogos planeados, ordenados cronologicamente.

2. Gestão de Arbitragem

   2.1. Registo de Árbitros: O sistema deve permitir registar árbitros, armazenando nome, função (Principal, Assistente, VAR) e nacionalidade.

   2.2. Alocação de Equipa Técnica: O sistema deve permitir selecionar um painel completo (Principal, Assistentes e VAR) para cada jogo.

   2.3. Validação de Nacionalidade: O sistema deve impedir a alocação de um árbitro cuja nacionalidade seja igual à de qualquer uma das seleções em campo.

   2.4. Verificação de Disponibilidade: O sistema deve impedir que um árbitro seja alocado a jogos com horários sobrepostos.

   2.5. Histórico de Arbitragem: O sistema deve permitir consultar os jogos atribuídos a cada árbitro.

3. Emissão de Bilhetes: Disponibilidade de bilhetes por jogo com a opção de emitir um bilhete.

4. Alojamento: O sistema deve permitir associar cada seleção a um Centro de Treino/Hotel.

5. Resultado Final de um Jogo: O sistema deve permitir registar o resultado final.

6. Registar Eventos de um Jogo: O sistema deve permitir registar os eventos detalhados de cada partida (Marcadores e Assistentes, Jogadores que levaram cartão e Substituições)

7. Registo de Estatísticas de Jogo: O sistema deve permitir registar as estatísticas de cada partida (posse de bola em percentagem, nº de remates à baliza, nº de cantos, nº de faltas, nº de cartões).

8. Ficha de Pré-Jogo: O sistema deve permitir registar os onzes iniciais e capitães além de permitir consultar árbitro principal, estádio e horário para esse determinado jogo.

9. Classificação: O sistema deve atualizar as tabelas classificativas de cada grupo (A a H), consoante o resultado de cada jogo para cada seleção (Vitória, Empate, Derrota, Golos Marcados, Golos Sofridos e Pontos).

10. Informação de Cada Seleção Geral: O sistema deve permitir consultar informações gerais de cada Seleção (Ranking, Participações na Competição)

11. Perfil das Seleções Participantes: O sistema deve permitir consultar a ficha detalhada de cada seleção participante (Grupo, Convocados).

12. Melhores Marcadores:  O sistema deve permitir consultar os melhores marcadores e assistentes da competição.

13. Informação dos Estádios: O sistema deve permitir consultar informação sobre os estádios, incluindo a localização (cidade), o nome e a capacidade total de espectadores.

14. Registo de Prémios Individuais (Man of the Match): O sistema deve permitir o registo do melhor jogador em campo para cada partida.

15. Gestão de Melhores Terceiros Lugares: O sistema deve mostrar quais os melhores terceiros lugares que avançam para a fase a eliminar.

16. Gestão de Eliminatórias: O sistema deve gerar os confrontos das fases seguintes (Desaseis avos de final até à Final) com base nos resultados das classificações.

   16.1. Definição da Árvore de Torneio (Bracket): O sistema deve ter pré-definida a estrutura dos confrontos (ex: 1º Grupo A vs 2º Grupo B).

17. Preferência de Fuso Horário: O sistema deve permitir selecionar o fuso horário para cada jogo.

Modelo de Dominio      

Seleção             
Jogador           
Jogo              
Estádio
Estadia
Árbitro            
Evento            
Estatística      
Bilhete           

