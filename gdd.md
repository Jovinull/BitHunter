# Game Design Document — **BitHUnter**

## Sumário

- [Game Design Document — **BitHUnter**](#game-design-document--bithunter)
  - [Sumário](#sumário)
  - [Visão Geral](#visão-geral)
    - [Introdução](#introdução)
    - [Pitch do Jogo](#pitch-do-jogo)
    - [Público-Alvo](#público-alvo)
    - [Plataformas e Tecnologias](#plataformas-e-tecnologias)
  - [Estrutura de Jogo](#estrutura-de-jogo)
    - [Loop Central](#loop-central)
    - [Progressão](#progressão)
    - [Economia](#economia)
  - [Controles](#controles)
  - [Sistemas de Pontuação e Atributos](#sistemas-de-pontuação-e-atributos)
    - [HP / MP / Stamina](#hp--mp--stamina)
    - [XP e Níveis](#xp-e-níveis)
    - [Moedas e Recompensas](#moedas-e-recompensas)
    - [Missões e Pontos de Tarefa](#missões-e-pontos-de-tarefa)
  - [Interface (HUD) e UX](#interface-hud-e-ux)
    - [HUD](#hud)
    - [Mapas e Mini-mapa](#mapas-e-mini-mapa)
    - [Feedbacks Visuais e Sonoros](#feedbacks-visuais-e-sonoros)
  - [Percepção e Mundo](#percepção-e-mundo)
    - [Iluminação e Ciclo Dia/Noite](#iluminação-e-ciclo-dianoite)
    - [Clima e Ambiente](#clima-e-ambiente)
  - [Ações do Jogador](#ações-do-jogador)
    - [Movimentação](#movimentação)
    - [Interação](#interação)
    - [Combate](#combate)
    - [Habilidades](#habilidades)
    - [Inventário e Equipamentos](#inventário-e-equipamentos)
  - [Simulação e Regras de Mundo](#simulação-e-regras-de-mundo)
    - [Terreno e Colisão](#terreno-e-colisão)
    - [IA: NPCs e Inimigos](#ia-npcs-e-inimigos)
    - [Itens e Objetos Inanimados](#itens-e-objetos-inanimados)
  - [Elementos de Jogo](#elementos-de-jogo)
    - [Classes/Arquétipos (opcional)](#classesarquétipos-opcional)
    - [Inimigos e Facções](#inimigos-e-facções)
    - [Chefes](#chefes)
  - [Estrutura de Fases/Capítulos](#estrutura-de-fasescapítulos)
    - [Capítulo 1 — Bairro das Ruínas](#capítulo-1--bairro-das-ruínas)
    - [Capítulo 2 — Mercado Subterrâneo](#capítulo-2--mercado-subterrâneo)
    - [Capítulo 3 — Fortaleza dos Caçadores](#capítulo-3--fortaleza-dos-caçadores)
  - [Sessenta Segundos de Jogo](#sessenta-segundos-de-jogo)
  - [Conteúdo Futuro / Backlog](#conteúdo-futuro--backlog)
  - [Referências](#referências)

---

## Visão Geral

Segue a revisão — mantendo suas ideias, corrigindo ortografia e deixando mais claro — e o original logo abaixo.

### Introdução

**BitHUnter** é um RPG 2D top-down de ação e exploração. O jogador é lançado em um mundo desconhecido, sem uma narrativa prévia ou uma história linear que se desenvolva ao longo do jogo. O foco é uma **aventura praticamente infinita**, em que o jogador pode **explorar e reexplorar** ambientes (no estilo **Tibia**).
No longo prazo, haverá **dungeons infinitas** nas quais o jogador desce e avança continuamente, com **aumento progressivo de dificuldade** e **recompensas melhores** (inspiradas na **Torre de DanMachi** e na **Mina de Caverna** de *Stardew Valley*).
Também está nos planos um **modo multijogador colaborativo** e mecânicas de **end-game**, como **refinamento e nivelamento de equipamentos**, **roda de habilidades** (como em **Tibia**) e um sistema de **ascensão**, no qual o personagem é resetado em troca de **bônus permanentes**.

**Inspirações**

* **Pokémon FireRed**
* **The Legend of Zelda: A Link to the Past (SNES)**
* **Rucoy Online**
* **Tibia** (para o loop de exploração/reexploração e progressão longa)
* **DanMachi** (conceito de torre/andar)
* **Stardew Valley** (minas com progressão de profundidade)

### Pitch do Jogo

**BitHUnter** é um RPG 2D top-down de ação e exploração focado em progressão consistente e rejogabilidade. Em um mundo sem narrativa linear, o jogador avança por áreas interconectadas e **dungeons de profundidade infinita**, onde a dificuldade cresce de forma previsível e as recompensas acompanham o risco. O ciclo central é direto: **explorar → enfrentar inimigos → obter recursos → evoluir o personagem e o equipamento**.

O projeto prioriza **clareza mecânica**, **leitura visual limpa** e **desempenho estável**, com sistemas de progressão de longo prazo (refino e nivelamento de equipamentos, árvore/roda de habilidades e **ascensão** com bônus permanentes). A visão inclui **modo cooperativo** para coordenação de rotas e papéis, mantendo o foco em decisões táticas e domínio do mapa.

**Diferenciais de proposta:**

* Progressão longa sem depender de enredo: desafio e recompensa como motor do jogo.
* Dungeons infinitas com escalonamento transparente de risco/recompensa.
* End-game orientado a builds, metas pessoais e eficiência de rotas.
* Base técnica enxuta para atualizações frequentes de conteúdo e balanceamento.

### Público-Alvo

* **Eu/estudo pessoal:** projeto de aprendizado e prática de engenharia de jogos em **Java**, priorizando performance, clareza e ciclos curtos de iteração.
* **Amigos/testers e comunidade indie:** jogadores de **RPG 2D top-down** que curtem progresso longo, exploração recorrente e **dungeons de profundidade** sem foco em narrativa linear.
* **Dev-entusiastas/open source:** interessados em ler, testar e contribuir com código limpo (HUD, IA simples, iluminação, balanceamento de risco/recompensa).
* **Perfil de jogo esperado:** sessões curtas a médias, progresso cumulativo, leitura visual clara, dificuldade crescente previsível e baixo consumo de recursos.

### Plataformas e Tecnologias

* **Plataforma:** PC (Windows/Linux/Mac)
* **Engine/Stack:** Java puro + AWT/Swing (render 2D), projeto modular
* **Arte:** Pixel art
* **Áudio:** Efeitos leves e trilhas em loop (formato otimizado)

---

## Estrutura de Jogo

### Loop Central

1. **Explorar** áreas e salas

   * Descobrir rotas, atalhos e salas com risco/recompensa claros.
   * Ambiente telegráfa perigo (iluminação, som, tipagem de inimigos) antes do confronto.

2. **Combater** inimigos e **coletar** itens/moedas

   * Combate curto, legível e responsivo; telegráfos simples, punindo ganância.
   * Auto-pickup para moedas e drops.

3. **Completar** missões/tarefas

   * Objetivos diretos (ex.: abrir portas, ativar alavancas, eliminar elite, escoltar).
   * Missões secundárias aumentam eficiência de rotas e desbloqueiam utilidades.

4. **Evoluir** (XP, skills, equipamentos)

   * XP por combate e descoberta; talentos incrementais e claros.
   * Equipamentos sobem de nível/refino com custos crescentes e limites.

5. **Desbloquear** novas áreas/chefes

   * Atalhos permanentes; chefes como testes de leitura de padrões e build.
   * Cada desbloqueio amplia a malha de rotas e opções de farm eficiente.

**Fail loop (retentivo e justo):** morte retorna ao último ponto de renascimento, não perde os recursos “em mãos” e nem o progresso estrutural (atalhos, mapas, receitas, ascensão), caso morra perde apenas parte do nível e XP.

---

### Progressão

* **Linear-ramificada por regiões**, com backtracking leve (portas/atalhos) para otimizar rotas.
* **Gates** de progressão:

  * **Chaves** (hard gate): portas, chefes, mecanismos.
  * **Nível/atributos** (soft gate): recomendação de poder para reduzir atrito.
  * **Habilidade** (skill gate): mecânicas exigem domínio (parry/dash, leitura de padrão).
* **Dungeons de profundidade** (infinito): escalonamento previsível de dano/vida/drops; checkpoints a cada n andares.
* **End-game**: refino/upgrade de equipamentos, roda de habilidades expandida e **ascensão** (reset com bônus permanentes focados em qualidade de vida e pequenas vantagens percentuais).
* **Marcos de capítulo**: cada região introduz um conceito (novo tipo de inimigo/terreno) e concede uma utilidade permanente (ex.: slot rápido extra, mapa da área, vendedor).

---

### Economia

**Fontes (income):**

* Inimigos (moedas base + chance de materiais), mini-chefes (drop garantido), eventos de sala, missões.
* Multiplicadores moderados em andares profundos (recompensa proporcional ao risco).

**Gastos (sinks):**

* **Upgrades/refino** de armas/armaduras (custos crescentes e caps por capítulo).
* **Consumíveis** (poções, bombas, utilitários).
* **Desbloqueios** (slots de inventário, mapas, marcadores).

**Materiais:**

* Usados em **crafting/imbuements** opcionais (elemental, crítico, utilidade).
* **Rareza** determina probabilidade e profundidade mínima.
* Conversão limitada entre materiais comuns → raros para controle de inflação.

**Regras de estabilidade:**

* Tabelas de drop com piso/teto (bad-luck protection).
* Recompensa de missão não escala indefinidamente (evita farm infinito trivial).
* Lojas atualizam estoque por dias; preços ancorados na dificuldade da região.

> Objetivo: uma economia **legível e estável**, onde cada upgrade é sentido no gameplay, sem exigir grind excessivo, mas valorizando rotas eficientes e domínio do combate.

---

## Controles

* **WASD/Setas:** mover
* **J/K/L (configurável):** ataque leve / pesado / habilidade
* **E:** interagir (falar, abrir baú/porta, alavanca)
* **Q:** usar consumível rápido (poção)
* **I:** inventário
* **M:** mapa/minimapa
* **Esc:** pausa/opções

> Suporte a **rebind** e **acessibilidade** (alto contraste, fontes maiores, reduzir animações).

---

## Sistemas de Pontuação e Atributos

### HP / MP / Stamina

* **HP** (corações na HUD): dano físico/mágico reduz corações; regeneração por itens/descanso.
* **MP** (cristais): gasto por habilidades; recarrega por tempo lento + itens.
* **Stamina** (opcional): custo para dash/ataque pesado, regenera fora de combate.

### XP e Níveis

* **XP** por inimigos, missões e descobertas.
* Ao **subir de nível**, jogador recebe pontos de atributo/talento (força, destreza, espírito).

### Moedas e Recompensas

* **Coins** (bronze→prata→ouro) para lojistas/upgrades.
* Baús, eventos e mini-desafios concedem recompensas.

### Missões e Pontos de Tarefa

* **Missões principais** (história) e **secundárias** (exploração).
* **Pontos de Tarefa** (internos) medem conclusão de capítulo e liberam recompensas cumulativas.

---

## Interface (HUD) e UX

### HUD

* **Corações** (HP), **Cristais** (MP), **Ícone de moeda + valor**.
* **Barra de XP** discreta.
* **Slots rápidos** (consumível/skill).
* **Indicadores de status**: veneno, sangramento, buff.

### Mapas e Mini-mapa

* Mini-mapa opcional; mapa de área com salas exploradas e **marcadores** (portas trancadas, chefes).

### Feedbacks Visuais e Sonoros

* Dano: flash leve no sprite + sfx.
* Acerto crítico: número flutuante com ênfase.
* Loot: brilho pulsante; pickup sfx distinto.
* Acessibilidade: **alto contraste**, **fontes escaláveis**, **redução de motion**.

---

## Percepção e Mundo

### Iluminação e Ciclo Dia/Noite

* **Sombras** por máscara (foco no jogador).
* **Estados**: dia / crepúsculo / noite / amanhecer (afetando alcance de visão e spawn).
* **Filtro de cor** e **opacidade** ajustados por estado (performance-friendly).

### Clima e Ambiente

* Efeitos leves (chuva/névoa) que influenciam **velocidade**, **visão** ou **IA** (opcional).

---

## Ações do Jogador

### Movimentação

* Top-down 8 direções (ou grid-based).
* **Dash** curto com i-frames (cooldown).

### Interação

* Abrir portas/baús (chaves), alavancas, conversar, ler placas.
* Coletar itens (auto-pickup para moedas).

### Combate

* **Ataque leve** (rápido) / **pesado** (lento, mais dano/poise).
* **Parry/Block** (opcional), **stagger** em inimigos.
* **Habilidades** que consomem MP (ex.: disparo arcano, golpe giratório).
* **Elementos** (físico, fogo, gelo, sombra) criam fraquezas/resistências simples.

### Habilidades

* **Árvore de talentos** simples (3 trilhas: Ofensiva, Defensiva, Utilitária).
* Sinergias com equipamentos (ex.: +chance crítico com adagas).

### Inventário e Equipamentos

* Slots: **arma**, **armadura**, **acessório**.
* Raridades (comuns → lendários).
* Peso/encumbrance (opcional, leve).

---

## Simulação e Regras de Mundo

### Terreno e Colisão

* Tilemap com camadas: **colisão**, **decoração**, **interação**.
* Propriedades por tile: escorregadio, venenoso, lento (ex.: lama).

### IA: NPCs e Inimigos

* **NPCs**: diálogo, loja, missão, dicas de exploração.
* **Inimigos**: estados simples (patrulha, perseguir, atacar, recuar).
* **Partículas/Traços** (opcional): vestígios em áreas perigosas ou buffadas.

### Itens e Objetos Inanimados

* **Rochas/Crates** (quebráveis), **Portas** (chave/interruptor), **Altares** (buffs).
* **Moedas** (OBJ_Coin_Bronze, etc.), **Corações**, **Cristais de Mana** (itens já presentes no seu código).

---

## Elementos de Jogo

### Classes/Arquétipos (opcional)

* **Caçador** (crit/dash), **Guardião** (defesa/poise), **Arcanista** (skills/MP).

### Inimigos e Facções

* **Ratos de Dados** (rápidos, fracos), **Specters** (fantasmas, vulneráveis a luz),
* **Mimics** (baús falsos), **Cultistas** (humanóides com magia).
* Facções influenciam **agressividade** e **drops** por área.

### Chefes

* 1 chefe por capítulo, com **padrões telegráficos**, **2–3 fases** e **drops únicos**.

---

## Estrutura de Fases/Capítulos

### Capítulo 1 — Bairro das Ruínas

* **Objetivo:** aprender combate básico, abrir atalhos, derrotar mini-chefe.
* **Missões:** recuperar chave antiga, ativar 2 alavancas.
* **Chefão:** Guardião Quebrado (telegráfos simples, golpe pesado).

### Capítulo 2 — Mercado Subterrâneo

* **Objetivo:** introduzir **lojas**, **crafting leve** e inimigos elementais.
* **Missões:** escoltar NPC, limpar ninho, encontrar reagentes.
* **Chefão:** Tecelã de Sombras (fase 2 com adds).

### Capítulo 3 — Fortaleza dos Caçadores

* **Objetivo:** domínio de build; encontros combinando mobs + elites.
* **Missões:** sabotar portões, recuperar relíquia “Bit-Núcleo”.
* **Chefão:** Mestre dos BitHunters (parry, combos, punição a greed).

---

## Sessenta Segundos de Jogo

O jogador nasce no **Bairro das Ruínas**, HUD mostra **corações**, **cristais** e **coins**. Dois inimigos patrulham um beco escuro. O filtro de **crepúsculo** reduz o alcance de visão; um **baú** brilha ao fundo. O jogador avança com **WASD**, usa **J** para um ataque leve e elimina o primeiro inimigo; o segundo telegrava um golpe pesado — **dash** com **Shift** para sair da linha e contra-atacar. Cai um **coração** e **moedas** (pickup automático). A alavanca à direita libera um atalho marcado no **mini-mapa**. Um NPC próximo oferece uma **poção** e uma dica do **chefão** da área. O jogador segue a trilha de tochas rumo ao portão.

---

## Conteúdo Futuro / Backlog

* Sistema de **crafting** (imbuir armas com bits/essências).
* **Contratos de caça** (mini-chefes opcionais).
* **Seeds** de mapa para variação leve (replayability).
* **Acessibilidade extra**: daltônico, alto contraste ampliado, modo texto grande global.

---

## Referências

* *The Legend of Zelda* (top-down combat/readability de telegráfos)
* *Hyper Light Drifter* (dash/combat vibe)
* *Enter the Gungeon* (telegráfos/variedade de encontros)
* Documentos e códigos do próprio projeto (**UI**, **Lighting**, **OBJ_Coin_Bronze**, **OBJ_Heart**, **OBJ_ManaCrystal**) como guias de HUD/iluminação.

---

> **Notas para produção**
>
> * Manter o jogo **leve**: sprites otimizados, culling simples, partículas com limites.
> * **Comentário e organização** de código (Clean Code), escalando sistemas (HUD, IA, Itens) em módulos.
> * Este documento é a **base**: use como “esqueleto”, detalhando cada seção à medida que os assets e as mecânicas forem fechando.
