# Game Design Document — **BitHunter**

## Sumário

- [Game Design Document — **BitHunter**](#game-design-document--bithunter)
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
    - [HP / MP / (Stamina)](#hp--mp--stamina)
    - [XP e Níveis](#xp-e-níveis)
    - [Moedas e Recompensas](#moedas-e-recompensas)
    - [Missões e Pontos de Tarefa](#missões-e-pontos-de-tarefa)
  - [Interface (HUD) e UX](#interface-hud-e-ux)
    - [HUD (implementado)](#hud-implementado)
    - [Inventário e Trade (implementado)](#inventário-e-trade-implementado)
    - [Feedbacks Visuais e Sonoros (implementado)](#feedbacks-visuais-e-sonoros-implementado)
    - [Mapas e Mini-mapa (planejado)](#mapas-e-mini-mapa-planejado)
    - [Indicadores de Status (planejado)](#indicadores-de-status-planejado)
    - [Barra de XP (planejado)](#barra-de-xp-planejado)
    - [Acessibilidade e UX](#acessibilidade-e-ux)
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
  - [Conteúdo Futuro / Backlog](#conteúdo-futuro--backlog)
  - [Referências](#referências)
    - [Notas rápidas de implementação (pragmáticas)](#notas-rápidas-de-implementação-pragmáticas)
  - [Sistema de Identificadores (`internalId`) e Localização de Texto (`translationKey`)](#sistema-de-identificadores-internalid-e-localização-de-texto-translationkey)
    - [Objetivo](#objetivo)
    - [`internalId` — Identificador técnico estável](#internalid--identificador-técnico-estável)
    - [`translationKey` — Ponte para os textos traduzidos](#translationkey--ponte-para-os-textos-traduzidos)
    - [Por que `translationKey` é “opcional” agora, mas útil a longo prazo?](#por-que-translationkey-é-opcional-agora-mas-útil-a-longo-prazo)
    - [Contextos diferentes para o mesmo item (várias descrições)](#contextos-diferentes-para-o-mesmo-item-várias-descrições)
    - [Relação entre `internalId` e `translationKey` no BitHUnter](#relação-entre-internalid-e-translationkey-no-bithunter)
  - [Player – Progressão de Status](#player--progressão-de-status)
    - [Tabela de Atributos por Nível (Níveis 1–10)](#tabela-de-atributos-por-nível-níveis-110)
    - [Fórmulas de Combate (GDD)](#fórmulas-de-combate-gdd)
  - [Monstros – Status Gerais](#monstros--status-gerais)
    - [Tabela de Monstros](#tabela-de-monstros)
    - [Detalhes Adicionais de Boss (Skeleton Lord)](#detalhes-adicionais-de-boss-skeleton-lord)
  - [Equipamentos Ofensivos (Armas / Ferramentas)](#equipamentos-ofensivos-armas--ferramentas)
    - [Tabela de Armas](#tabela-de-armas)
  - [Equipamentos Defensivos](#equipamentos-defensivos)
    - [Tabela de Escudos e Botas](#tabela-de-escudos-e-botas)
    - [Sistema de Guarda (Stamina de Escudo)](#sistema-de-guarda-stamina-de-escudo)
  - [Magias e Consumíveis](#magias-e-consumíveis)
    - [Projectile – Fireball](#projectile--fireball)
    - [Consumível – Poção Vermelha](#consumível--poção-vermelha)
  - [Resumo Rápido de Balanceamento (para consulta no GDD)](#resumo-rápido-de-balanceamento-para-consulta-no-gdd)

---

## Visão Geral

Segue a revisão — mantendo suas ideias, corrigindo ortografia e deixando mais claro — e o original logo abaixo.

### Introdução

**BitHunter** é um RPG 2D top-down de ação e exploração. O jogador é lançado em um mundo desconhecido, sem uma narrativa prévia ou uma história linear que se desenvolva ao longo do jogo. O foco é uma **aventura praticamente infinita**, em que o jogador pode **explorar e reexplorar** ambientes (no estilo **Tibia**).
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

**BitHunter** é um RPG 2D top-down de ação e exploração focado em progressão consistente e rejogabilidade. Em um mundo sem narrativa linear, o jogador avança por áreas interconectadas e **dungeons de profundidade infinita**, onde a dificuldade cresce de forma previsível e as recompensas acompanham o risco. O ciclo central é direto: **explorar → enfrentar inimigos → obter recursos → evoluir o personagem e o equipamento**.

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

* **W/A/S/D:** mover (e navegar em listas/slots de inventário)
* **Enter:** confirmar/interagir (diálogos, menus, trade, usar/equipar item na tela de personagem)
* **P:** pausar / retomar
* **C:** abrir/fechar status do personagem
* **Esc:** abrir opções (em jogo) / voltar (em submenus e seleção de classe)
* **F:** disparo/magia
* **T:** alternar texto de debug e exibição de caminho (dev)
* **R:** recarregar mapa atual (atalho de desenvolvimento)
* **W/S (menus):** navegar para cima/baixo; **Enter** seleciona

  * Tela de Game Over: **W/S** navega, **Enter** confirma
  * Trade/Inventários: **W/A/S/D** navegam slots; **Esc** volta

> Observações
>
> * Não há suporte a **rebind** no código atual (as opções tratam volume e navegação).
> * As teclas **E**, **Q**, **J/K/L** e **M** não estão implementadas no `KeyHandler`, então foram removidas desta seção para evitar inconsistências.
> * Se quiser manter “atalhos planejados” (ex.: **M** para mapa, **Q** para consumível rápido), recomendo listá-los numa subseção “Planejados” e só movê-los para “Controles” após implementar no `KeyHandler`.

---

Aqui vai a seção reescrita com base direta no que o seu código já implementa hoje (e sinalizando o que está “Planejado” mas ainda não existe no código):

## Sistemas de Pontuação e Atributos

### HP / MP / (Stamina)

* **HP (vida)**: inteiro com limite por `maxLife`. Dano recebido = `attackInimigo - defense` (mínimo 0).

  * **i-frames** (invencibilidade) ao tomar dano: ~60 frames no player; monstros têm i-frames próprios e barra de HP opcional na cabeça.
  * Vida é **restaurada** por itens (`consumable.use(this)`) e por rotinas de descanso/recuperação como `restoreLifeAndMana()`.
* **MP (mana)**: inteiro com limite por `maxMana`.

  * **Gasto** por projéteis/habilidades (ex.: `OBJ_Fireball` via `haveResource()`/`subtractResource()`).
  * **Recuperação** por itens/descanso (não há regen passiva no loop principal).
* **Stamina**: **Planejado**. Não há custo de dash/ataque pesado no código atual.

**Atributos e combate**

* **Força** e **Destreza** escalam ataque/defesa:

  * `attack = strength * currentWeapon.attackValue`
  * `defense = dexterity * currentShield.defenseValue`
* **Knockback**: armas podem ter `knockBackPower`; aplica recuo no alvo.
* **Ammunition** (`ammo`): existe no player; o uso depende do projétil. No caso do **Fireball**, o custo tende a ser **mana** (não munição).

---

### XP e Níveis

* **XP** ganho ao matar monstros (`exp += monster.exp`), com feedback na UI.
* **Level up** em `checkLevelUp()`:

  * `nextLevelExp` **dobra** a cada nível.
  * +2 em **maxLife**, +1 em **strength** e **dexterity**; recalcula `attack/defense`; **vida é cheia**.
  * Mensagem de nível na UI (estado de diálogo).
* **Fontes de XP**: combate e (no futuro) objetivos; não há XP por exploração no código atual.

---

### Moedas e Recompensas

* **Coins**: inteiro (`coin`), inicial ~`500`. Usado para lojas/compras (**Planejado** no GDD; no código base, já há `price` nos itens, mas a economia completa depende das telas de trade/loja).
* **Drops de itens**: via `checkDrop()`/`dropItem()` em monstros (estrutura pronta; lógica de quais itens ainda é sua).
* **Tipos de item**: espada, machado, escudo, consumível, luz (lanterna), chave, pickupOnly, obstáculo etc.
* **Inventário**: até **20** slots; itens **stackable** incrementam `amount`. Mensagens de pickup na UI.

> Observação: a ideia de **tiers visuais** (bronze→prata→ouro) pode existir só como **apresentação**; tecnicamente `coin` é um número.

---

### Missões e Pontos de Tarefa

* **Missões principais/secundárias**: **Planejado**. Não há sistema de quests por hora.
* **Pontos de Tarefa** (medir capítulo/progredir recompensas cumulativas): **Planejado**.
* **Hoje**: o ciclo de progresso é orientado por **combate + XP + equipamento**, com estados de UI para diálogo, trade e game over já estruturados.

---

## Interface (HUD) e UX

### HUD (implementado)

* **Vida (HP):** corações desenhados no canto superior esquerdo (`drawPlayerLife`), com **vazio / meio / cheio**.
* **Mana (MP):** cristais abaixo dos corações, exibindo **máximo** e **atual**.
* **Mensagens rápidas:** feed de mensagens flutuantes na tela (ex.: “Você obteve…”, “X de dano!”) com sombra leve e expiração automática (`drawMessage`).
* **Status do personagem:** tela dedicada com **nível, vida, mana, força, destreza, ataque, defesa, EXP, EXP para próximo nível, moedas**, além de **arma/escudo** equipados (`drawCharacterScreen`).
* **Game Over:** overlay com opções **Reviver** / **Sair**.
* **Pausa / Diálogo / Opções:** janelas modais com fundo e bordas discretos.

> Nota: **Moedas** e **EXP** não aparecem na HUD de “play” por padrão; aparecem na tela de personagem e em janelas (trade/opções). Se quiser moedas/EXP sempre visíveis, é uma adição simples ao `drawPlayerLife()` ou criando `drawTopBar()`.

### Inventário e Trade (implementado)

* **Inventário com grid** (até 20 itens), **highlight** para itens equipados (arma/escudo/luz) e **contador** para itens empilháveis.
* **Cursor** com borda e **janela de descrição** do item selecionado.
* **Trade** (comprar/vender) com janela de preço, saldo do jogador (ícone de moeda) e navegação por W/A/S/D + Enter/Esc.

### Feedbacks Visuais e Sonoros (implementado)

* **Dano recebido:** som de acerto (`playSE(6)`) e **transparência** temporária do player (i-frames), com **HP bar** opcional em monstros quando atingidos.
* **Pickup/uso de item:** som dedicado e mensagem no feed.
* **Transições / dormir:** fades leves com controle de alpha.
* **Legibilidade:** fontes bitmap limpas, **sombra de texto** para contraste, paleta sóbria (fundo/painéis/bordas/destaques).

### Mapas e Mini-mapa (planejado)

* Não há mini-mapa nem mapa de área no código atual.

  * **Sugestão rápida:** criar `drawMapOverlay()` com matriz de salas exploradas e marcadores simples (porta trancada, chefe), acionado por tecla (ex.: `M`).

### Indicadores de Status (planejado)

* Ícones para **veneno / sangramento / buff** não estão implementados.

  * **Sugestão:** reservar uma faixa à direita dos cristais e desenhar pequenos `BufferedImage` com tooltip em `drawPlayerLife()`.

### Barra de XP (planejado)

* EXP está visível na **tela de personagem**, mas não há **barra de XP** na HUD.

  * **Sugestão:** em `drawPlayerLife()`, após MP, desenhar uma barra fina proporcional a `player.exp / player.nextLevelExp`.

### Acessibilidade e UX

* **Já presente:** alto contraste funcional (fundo escuro, texto claro), **sombra de texto**, tamanhos de fonte ajustáveis via `deriveFont` (embora fixos em runtime), navegação de menus por teclado com feedback visual.
* **Planejado:**

  * **Escala de fonte** configurável nas **Opções** (slider como o de volume).
  * **Redução de motion:** flag nas **Opções** que limita fades/transições (pular `transitionState`/`sleepState` ou reduzir alpha-step).
  * **Indicadores auditivos opcionais** (ex.: leve beep ao focar item no inventário).

---

## Percepção e Mundo

### Iluminação e Ciclo Dia/Noite

* **Implementado:** máscara/overlay com **alpha** variável (filtro de cor/opacidade) controlado por `eManager.lighting.filterAlpha`, usado também em transições (ex.: tela de dormir). Estados de dia/noite já existem como `dayState`/`day`, alternados com fade.
* **Efeito prático:** foco visual no player (região clara) e ambiente escurecido à medida que o alpha sobe; solução **performance-friendly** (um overlay, sem pós-processo pesado).
* **Planejado:** ampliar os **estados** (dia / crepúsculo / noite / amanhecer) para afetar **alcance de visão** e **spawn**. Hoje o overlay muda a luminosidade, mas não altera IA/spawn.

### Clima e Ambiente

* **Planejado:** efeitos leves (chuva/névoa) alterando **velocidade**, **visão** ou **IA**.

  * (Não há efeitos climáticos no código atual; a mesma técnica de overlay e partículas pode ser reaproveitada.)

---

## Ações do Jogador

### Movimentação

* **Implementado:** top-down **4 direções** (W/A/S/D), com colisão de tiles/objetos/entidades e animação de caminhada.
* **Planejado:** 8 direções ou modo grid-based (hoje não há).
* **Dash:** **planejado**. Não há dash nem custo de stamina no código atual.

### Interação

* **Implementado:**

  * **Coleta** de itens ao colidir (`pickUpObject`), com **stack**, mensagem e som; itens `pickupOnly` são usados automaticamente.
  * **Chaves/obstáculos:** há `OBJ_Key` e `type_obstacle` com `interact()` quando o jogador confirma (Enter).
  * **Conversar/diálogo** com NPCs (Enter), com janela dedicada.
* **Planejado:** **auto-pickup só para moedas** (hoje a coleta é por colisão + lógica do item; ajustar para moedas se quiser esse comportamento).
* **Placas/alavancas:** parcialmente coberto via `type_obstacle`/`interact()`; expandir conforme necessidade.

### Combate

* **Implementado:**

  * **Ataque corpo-a-corpo** com Enter (espada/machado), caixa de ataque temporária e cálculo de **dano = ataque − defesa** (mín. 0).
  * **Projétil/habilidade** (tecla **F**, ex.: **Fireball**) consumindo **mana** com cooldown.
  * **Knockback** opcional por arma (`knockBackPower`).
  * **i-frames** ao sofrer dano (player e monstros).
* **Planejado:**

  * **Ataque pesado** e **parry/block ativo** (o escudo hoje só dá **defesa** passiva).
  * **Stagger** por poise/ataque pesado.
  * **Elementos** (físico/fogo/gelo/sombra) com resistências/fraquezas; o Fireball sugere “fogo”, mas não há resistências no dano.

### Habilidades

* **Planejado:** **árvore/roda de talentos** com trilhas Ofensiva/Defensiva/Utilitária e sinergias de build.

  * **Hoje:** progressão por **nível** (vida/força/destreza sobem) e troca de **arma/escudo/luz**; não há sistema de talentos.

### Inventário e Equipamentos

* **Implementado:**

  * **Slots funcionais:** **arma**, **escudo**, **luz** (lanterna).
  * **Inventário** com até **20** itens; itens **empilháveis**; preço/compra/venda em **Trade**; destaque para itens equipados; descrição contextual.
* **Planejado:**

---

## Simulação e Regras de Mundo

### Terreno e Colisão

* **Implementado (no código):**

  * **Tilemap com colisão** via `TileManager` + `CollisionChecker.checkTile(this)`. Tiles sólidos são respeitados no movimento de player/NPC/monstros.
  * **Camada de interação**: `InteractiveTile` (ex.: tiles destruíveis, vida, `destructible`, `invincible`, `getDestroyedForm()`), atualizados em `GamePanel.update()` e desenhados antes das entidades.
  * **Objetos interativos** (portas/obstáculos/baús): `type_obstacle` com `interact()` e coleta via `pickUpObject`.
* **Planejado (extensões fáceis com a base atual):**

  * **Camada de decoração** sem colisão (apenas visual) já é suportada pelo fluxo de draw; seguir pelo `TileManager`.
  * **Propriedades por tile** (escorregadio/venenoso/lento): aproveitar `InteractiveTile` ou um mapa de propriedades no `TileManager` e aplicar efeitos no `CollisionChecker`/`EventHandler` (ex.: aplicar slow por N frames).

### IA: NPCs e Inimigos

* **Implementado (no código):**

  * **NPCs**: diálogo com `speak()` (UI de diálogo dedicada); comércio (`tradeState`, inventário do NPC, compra/venda com preço e validações).
  * **Inimigos**: ciclo básico com `update()` e flags (`alive`, `dying`, `invincible`, `hpBarOn`), dano em player (`damagePlayer`), **knockback**, drops (`checkDrop()`), e A* com `PathFinder` e `onPath`/`searchPath`.
  * **Estados práticos** já emergem: **patrulha** (via `setAction()`), **perseguir** (quando `onPath`), **atacar** (hitbox de ataque do player + cálculo de dano), **recuo** (knockback).
  * **Partículas**: sistema de `Particle` já integrado (`generateParticle`, `particleList`).
* **Planejado:**

  * Tabelar estados de IA (patrulha → perseguir → atacar → recuar) numa FSM simples por `Entity.setAction()`.
  * Variantes de percepção (raio/cone) e timers de aggro/desaggro (usar `actionLockCounter` já existente).

### Itens e Objetos Inanimados

* **Implementado (no código):**

  * **Moedas** (`ObjCoinBronze`), **Corações** (`ObjHeart`), **Cristais de Mana** (`ObjManaCrystal`) com HUD pronto e coleta.
  * **Portas/Chaves**: `OBJ_Key` + `type_obstacle` com `interact()` (chaves consumíveis; portas/obstáculos destravados/abertos por ação).
  * **Baús** e pickups (`type_pickupOnly`) com uso instantâneo.
* **Planejado:**

  * **Rochas/Crates** quebráveis como `InteractiveTile` (destructible, vida, som) com possibilidade de **drop**.
  * **Altares** (buffs temporários) usando `interact()` + aplicação de status no player (ex.: +força por 60s).

---

## Elementos de Jogo

### Classes/Arquétipos (opcional)

* **Base disponível:** tela de **seleção de classe** (title `state == 1`) já existe.
* **Planejado:**

  * **Caçador** (crit/dash), **Guardião** (defesa/poise), **Arcanista** (skills/MP).
  * Aplicar no `Player.setDefaultValues()` ajustes de **força/destreza/mana**, arma inicial e passivas leves (ex.: +crit com adaga para Caçador).

### Inimigos e Facções

* **Implementado:** variedade pronta para crescer (arrays por mapa, `checkDrop`, HP bar, A*).
* **Planejado:**

  * **Ratos de Dados** (rápidos, fracos), **Specters** (vulneráveis à luz → sinergia com `currentLight`), **Mimics** (baú que ataca), **Cultistas** (projéteis/MP drain).
  * Facções por área para modular **agressividade** e **tabela de drop** (usar prefixo/flag em `Entity.name` + handler de loot).

---

## Conteúdo Futuro / Backlog

* **Crafting/Imbuir** armas com **bits/essências** (slots de elemento).
* **Contratos de caça**: mini-chefes opcionais com recompensas únicas.
* **Seeds** de mapa para variação leve por sessão.
* **Acessibilidade extra**: daltônico, alto contraste expandido, modo texto grande global.

---

## Referências

* *The Legend of Zelda* (telegráfos legíveis, combate top-down)
* *Hyper Light Drifter* (dash/combat feel)
* *Enter the Gungeon* (padrões/variedade)
* Seu próprio código: **UI** (HUD e mensagens), **Lighting/Environment** (overlay e transições), **ObjCoinBronze / ObjHeart / ObjManaCrystal** (coleta/HUD), **PathFinder** (A*), **InteractiveTile** (destrutíveis).

---

### Notas rápidas de implementação (pragmáticas)

* **Terreno “lento/venenoso/escorregadio”**: adicionar mapa de propriedades por tile (ex.: `tileProps[row][col]`) e aplicar em `CollisionChecker`/`EventHandler` (slow por N frames, tick de dano, mod de atrito).
* **Dash**: flag no `Player` com **cooldown** e **i-frames**, custo opcional de **Stamina** (se ativar barra).
* **Elementos**: atributo `element` em arma/projétil/monstro; tabela simples de multiplicadores.
* **Parry/Block**: tecla de defesa ativando **i-frames curtos** e “stagger” se perfect-timing.
* **Boss fights**: use `transitionState` para setup de arena + música; fases controladas por limiares de HP.

## Sistema de Identificadores (`internalId`) e Localização de Texto (`translationKey`)

### Objetivo

O jogo usa dois conceitos diferentes para identificar e exibir o conteúdo das entidades (itens, NPCs, objetos de cenário, etc.):

* **`internalId`** → identificação **técnica**, usada pela engine.
* **`translationKey`** → identificação **textual**, usada pelo sistema de tradução (i18n).

Separar essas duas responsabilidades evita acoplamento entre **lógica de jogo** e **texto apresentado ao jogador**, facilita evolução futura e abre espaço para descrições/contextos diferentes de um mesmo item.

---

### `internalId` — Identificador técnico estável

`internalId` é o “CPF técnico” da entidade. É um valor **não traduzido** e **não exibido diretamente ao jogador**, pensado para:

* **Save/Load**

  * O `SaveLoad` armazena apenas `internalId` no `DataStorage` (inventário, objetos no mapa, loot, etc.).
  * Ao carregar, o jogo chama algo como `generator.getObject(internalId)` para recriar a entidade correta.

* **Lógica de jogo**

  * Comparações de tipo:

    * Ex.: verificar se um objeto é uma porta de ferro ou uma placa metálica.
  * Eventos e regras específicas:

    * Ex.: “se for `item.pickaxe`, permite quebrar certos blocos”.

* **Consistência e estabilidade**

  * **Não deve mudar** entre versões, ou saves antigos quebram.
  * Formato típico:

    * `item.axe`
    * `item.coin.bronze`
    * `npc.oldMan`
    * `tile.metalPlate`
    * `object.doorIron`

> Em resumo: `internalId` é **fixo** e pensado para a **engine**. O jogador nunca deveria ver esse valor na tela.

---

### `translationKey` — Ponte para os textos traduzidos

`translationKey` é a chave usada para buscar textos nos arquivos de idioma (por exemplo, os `.properties` que você está montando).

A função dele é servir de **ponte entre a entidade e as strings traduzidas**:

* A entidade guarda **apenas a base**, por exemplo:

  * `item.axe`
  * `item.coin.bronze`
  * `npc.oldMan`

* A UI monta as chaves completas:

  * Nome do item:

    * `item.axe.name`
  * Descrição padrão:

    * `item.axe.desc`
  * Mensagem de pickup:

    * `item.coin.bronze.pickup`
  * Diálogo:

    * `npc.oldMan.dialog.0.line1`, etc.

Isso permite:

* Centralizar todos os textos em arquivos de idioma.
* Trocar/ajustar textos sem tocar nas classes Java.
* Suportar facilmente múltiplos idiomas.

> Hoje, normalmente você faz algo como:
>
> ```java
> setInternalId(OBJ_ID);      // ex.: "item.axe"
> setTranslationKey(OBJ_ID);  // mesma base
> ```
>
> Ou seja: o valor é o mesmo, mas o **uso** é diferente.

---

### Por que `translationKey` é “opcional” agora, mas útil a longo prazo?

Tecnicamente, o jogo poderia funcionar só com `internalId` e strings hardcoded nas classes.
Por isso eu comentei que `translationKey` é **“opcional” para o funcionamento**, mas **muito útil para a arquitetura**.

A longo prazo, manter `translationKey` separado traz vantagens claras:

1. **Refatorar textos sem mexer em lógica / saves**

   * Você pode reorganizar os arquivos de idioma, renomear chaves, ajustar textos, etc.
   * O `internalId` continua igual, então **nada quebra na parte de save/load**.

2. **Ter várias entidades diferentes compartilhando o mesmo texto**

   * Ex.: dois itens diferentes (`item.sword.iron`, `item.sword.rusty`) podem apontar para a mesma mensagem de tutorial ou dica.

3. **Flexibilidade para mudanças futuras**

   * Se um dia você precisar alterar um padrão de `internalId` (por ex. versão 2 de um item, outro balanceamento, etc.), ainda pode manter o `translationKey` antigo para não perder as traduções — ou o contrário.

4. **Ferramentas externas e pipelines de tradução**

   * Fica mais fácil jogar esses `.properties` em ferramentas de tradução e ter um fluxo à parte da programação.

Resumindo: **não é estritamente necessário para o jogo rodar**, mas **prepara o projeto para crescer sem virar um caos de texto hardcoded**.

---

### Contextos diferentes para o mesmo item (várias descrições)

Separar `internalId` de `translationKey` também facilita ter **textos diferentes para o mesmo item**, dependendo do contexto. Alguns exemplos práticos:

* **Descrição padrão (inventário)**

  * `item.axe.desc`
  * Ex.: “Um pouco enferrujado, mas ainda corta algumas árvores.”

* **Descrição em loja (foco em valor/custo)**

  * `item.axe.desc.shop`
  * Ex.: “Um machado simples, ideal para quem está começando. Corta árvores básicas.”

* **Descrição expandida quando equipado**

  * `item.axe.desc.equipped`
  * Ex.: “Equipado: concede +X de ataque e permite cortar árvores específicas.”

* **Mensagem de pickup**

  * `item.axe.pickup`
  * Ex.: “Você obteve o Machado de Lenhador!”

No código, a entidade só precisa guardar **uma base**:

```java
setInternalId("item.axe");
setTranslationKey("item.axe");
```

E a UI decide **qual contexto** usar:

```java
// Inventário
String name = t(entity.getTranslationKey() + ".name");
String desc = t(entity.getTranslationKey() + ".desc");

// Loja
String shopDesc = t(entity.getTranslationKey() + ".desc.shop");

// Mensagem de pickup
String pickupMsg = t(entity.getTranslationKey() + ".pickup");
```

Isso abre espaço para:

* Descrições mais curtas ou técnicas em certos lugares (HUD).
* Textos mais “flavor” em diálogos, inventário ou lojas.
* Mesmo item, **múltiplas “vozes”** dependendo da situação, sem duplicar lógica.

---

### Relação entre `internalId` e `translationKey` no BitHUnter

Na implementação atual do BitHUnter:

* O **`internalId`**:

  * É usado em **Save/Load**, geração de entidades e comparações de lógica.
  * **Não muda** entre sessões e versões (idealmente).

* O **`translationKey`**:

  * É geralmente igual ao `internalId` (por conveniência).
  * É usado pela UI/sistema de i18n para montar chaves do tipo `.name`, `.desc`, `.pickup`, `.dialog.*`.

Hoje, manter os dois com o mesmo valor é **simples e suficiente**.
Mas o fato de já estarem **conceitualmente separados** garante que, no futuro, você possa:

* Mudar a estrutura de textos sem tocar no sistema de save.
* Adicionar contextos extras de descrição por tela/situação.
* Evoluir o sistema de tradução de forma independente da lógica de gameplay.

Em outras palavras:

> **`internalId` garante a identidade técnica da entidade.
> `translationKey` garante que o texto mostrado possa evoluir à vontade, sem amarrar a narrativa/interface à estrutura interna do código.**

Segue tudo organizado em formato de GDD, já com os **valores sugeridos** que comentei.

## Player – Progressão de Status

### Tabela de Atributos por Nível (Níveis 1–10)

> Premissas:
>
> * HP inicial: 12, +3 por nível.
> * MP inicial: 6, **não** aumenta por enquanto.
> * STR: +1 por nível.
> * DEX: +1 em níveis pares.
> * Ataque c/ Espada Normal = `STR * 2`.
> * Defesa c/ Escudo de Madeira = `DEX * 1`.

| Nível | HP Máx | MP Máx | STR | DEX | Ataque (Espada Normal) | Defesa (Escudo Madeira) | XP total p/ próximo nível |
| ----- | ------ | ------ | --- | --- | ---------------------- | ----------------------- | ------------------------- |
| 1     | 12     | 6      | 2   | 1   | 4                      | 1                       | 10                        |
| 2     | 15     | 6      | 3   | 2   | 6                      | 2                       | 21                        |
| 3     | 18     | 6      | 4   | 2   | 8                      | 2                       | 35                        |
| 4     | 21     | 6      | 5   | 3   | 10                     | 3                       | 52                        |
| 5     | 24     | 6      | 6   | 3   | 12                     | 3                       | 72                        |
| 6     | 27     | 6      | 7   | 4   | 14                     | 4                       | 95                        |
| 7     | 30     | 6      | 8   | 4   | 16                     | 4                       | 121                       |
| 8     | 33     | 6      | 9   | 5   | 18                     | 5                       | 150                       |
| 9     | 36     | 6      | 10  | 5   | 20                     | 5                       | 182                       |
| 10    | 39     | 6      | 11  | 6   | 22                     | 6                       | 217                       |

### Fórmulas de Combate (GDD)

| Conceito                   | Fórmula / Regra                                                                                            |
| -------------------------- | ---------------------------------------------------------------------------------------------------------- |
| Ataque base do Jogador     | `AttackJogador = STR * weapon.attackValue`                                                                 |
| Defesa base do Jogador     | `DefenseJogador = DEX * shield.defenseValue + soma(defesaBoots + defesaHelmet + defesaChest + defesaLegs)` |
| Dano do Jogador no Monstro | `dano = AttackJogador - DefenseMonstro` (se < 0, vira 0)                                                   |
| Dano do Monstro no Jogador | `dano = AttackMonstro - DefenseJogador` (se < 1, vira 1)                                                   |
| XP para próximo nível      | Começa em **10** no nível 1. Ao subir de nível: `NextLevelExp = NextLevelExp + 5 + (novoNível * 3)`        |

---

## Monstros – Status Gerais

### Tabela de Monstros

| ID Interno             | Nome (GDD)    | HP Máx | ATK                     | DEF | SPD (default)         | EXP | Observações de Comportamento / Drops                                                                                                                                                                                                           |
| ---------------------- | ------------- | ------ | ----------------------- | --- | --------------------- | --- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `monster.greenSlime`   | Green Slime   | 8      | 3                       | 0   | 1                     | 3   | Inimigo básico. Pode perseguir jogador e atirar `ObjRock` em certas condições. Drop: 50% Bronze Coin, 25% Heart, 25% Mana Crystal.                                                                                                             |
| `monster.bat`          | Bat           | 10     | 4                       | 0   | 4                     | 5   | Rápido, dano moderado. Movimento aleatório perseguindo jogador em área. Drop: 50% Bronze Coin, 25% Heart, 25% Mana Crystal.                                                                                                                    |
| `monster.redSlime`     | Red Slime     | 16     | 7                       | 2   | 2                     | 10  | Slime de mid game. Persegue e atira `ObjRock` com maior frequência. Drop: 40% Bronze Coin, 30% Heart, 30% Mana Crystal.                                                                                                                        |
| `monster.orc`          | Orc           | 30     | 9                       | 3   | 1                     | 20  | Inimigo “elite”. Usa área de ataque corpo a corpo (48×48), tem KnockBackPower 5. Drop: 50% Bronze Coin, 25% Heart, 25% Mana Crystal.                                                                                                           |
| `monster.skeletonLord` | Skeleton Lord | 80     | 10 (fase 1) / 15 (rage) | 4   | 1 (fase 1) / 2 (rage) | 100 | Boss. Inicialmente `sleep = true`. Ao ficar abaixo de 50% HP entra em “rage” (mais ataque e velocidade). Área de ataque 170×170. Drop padrão de moedas/corações/mana + eventos de fim de boss (remover portas de ferro, atualizar `Progress`). |

### Detalhes Adicionais de Boss (Skeleton Lord)

| Atributo          | Valor / Comportamento                                                                |
| ----------------- | ------------------------------------------------------------------------------------ |
| `isBoss`          | `true`                                                                               |
| HP Máx            | 80                                                                                   |
| ATK Fase 1        | 10                                                                                   |
| ATK Fase 2 (rage) | 15                                                                                   |
| DEF               | 4                                                                                    |
| SPD Fase 1        | 1                                                                                    |
| SPD Fase 2        | 2 (aumenta em 1 ao entrar em rage)                                                   |
| KnockBackPower    | 5                                                                                    |
| AttackArea        | 170×170                                                                              |
| Motion1Duration   | 25                                                                                   |
| Motion2Duration   | 50                                                                                   |
| Trigger de Rage   | Quando `life < maxLife / 2` → liga `inRage`, atualiza sprites e dobra agressividade. |

---

## Equipamentos Ofensivos (Armas / Ferramentas)

### Tabela de Armas

| ID Interno         | Nome (GDD)    | Tipo    | AttackValue | Área de Ataque (LxA) | KnockBack | Motion1 | Motion2 | Preço | Observações                                           |
| ------------------ | ------------- | ------- | ----------: | -------------------: | --------: | ------: | ------: | ----: | ----------------------------------------------------- |
| `item.swordNormal` | Espada Normal | Sword   |           2 |                36×36 |         2 |       5 |      25 |    30 | Arma inicial / early game. Ataque rápido e confiável. |
| `item.axe`         | Machado       | Axe     |           3 |                30×30 |        10 |      25 |      50 |   120 | Dano alto, muito knockback, animação mais lenta.      |
| `item.pickaxe`     | Picareta      | Pickaxe |           2 |                30×30 |         6 |      15 |      30 |    60 | Ferramenta de mineração, usável como arma mediana.    |

> **Fórmula de dano do jogador com arma:**
> `AttackJogador = STR * AttackValueDaArma`.

---

## Equipamentos Defensivos

### Tabela de Escudos e Botas

| ID Interno        | Nome (GDD)        | Tipo   | DefenseValue | GuardStaminaCap | SpeedValue | Preço | Observações                                                                                          |
| ----------------- | ----------------- | ------ | -----------: | --------------: | ---------: | ----: | ---------------------------------------------------------------------------------------------------- |
| `item.shieldWood` | Escudo de Madeira | Shield |            1 |              40 |          0 |    40 | Escudo básico do início do jogo. Fornece pouca defesa e pouca stamina de guarda.                    |
| `item.shieldBlue` | Escudo Azul       | Shield |            3 |              80 |          0 |   300 | Escudo mid game. Grande aumento de defesa (DEF = DEX * 3) e maior reserva de stamina de guarda.     |
| `item.boots`      | Botas de Couro    | Boots  |            1 |               – |          1 |   400 | Aumenta velocidade em +1 e fornece +1 de defesa. Item caro, voltado para mid/late game.             |

> **Fórmula de defesa considerando escudo e botas:**
> `DefenseJogador = DEX * shield.defenseValue + boots.defenseValue + armorPiecesDefense`.

---

### Sistema de Guarda (Stamina de Escudo)

- Cada **escudo** possui um atributo de **stamina de guarda máxima** (`GuardStaminaCap`), que define quanto “fôlego de guarda” ele oferece.
- O **player** possui uma barra de **stamina de guarda atual**, que vai de `0` até `GuardStaminaCap` do escudo equipado.
- Enquanto o jogador **segura o botão de guarda (Barra de Espaço)**:
  - A stamina de guarda é **consumida gradualmente ao longo do tempo** (dreno passivo contínuo).
- Quando o jogador **defende um ataque** com o escudo:
  - É consumido um **chunk grande de stamina**, baseado no **ataque do monstro** (quanto mais forte o monstro, maior o custo de stamina ao bloquear).
  - Se a stamina chegar a `0`, o escudo **não consegue mais guardar** até recuperar stamina; o jogador passa a tomar o dano cheio.
- Se a stamina de guarda está em `0`, o jogador **não pode entrar em pose de guarda** até recuperar um mínimo de stamina.

> **Observação importante (recarga de stamina):**
> A **stamina de guarda só começa a recarregar depois que o jogador solta a Barra de Espaço por um período de tempo**.
> Enquanto a tecla de guarda estiver pressionada, **a barra de stamina não regenera**, mesmo que já esteja zerada.


---

## Magias e Consumíveis

### Projectile – Fireball

| ID Interno      | Nome (GDD) | Tipo       |  ATK |  SPD | MaxLife (frames) | KnockBack | Custo de MP (`useCost`) | Observações                                                                      |
| --------------- | ---------- | ---------- | ---: | ---: | ---------------: | --------: | ----------------------: | -------------------------------------------------------------------------------- |
| `item.fireball` | Fireball   | Projectile |    3 |    5 |               80 |         4 |                       2 | Feitiço de ataque à distância. Consome mana do jogador, gera partículas de fogo. |

### Consumível – Poção Vermelha

| ID Interno       | Nome (GDD)     | Tipo       | Valor (cura) | Stackable | Preço | Observações                                                                                     |
| ---------------- | -------------- | ---------- | -----------: | --------: | ----: | ----------------------------------------------------------------------------------------------- |
| `item.potionRed` | Poção Vermelha | Consumable |         5 HP |      true |    25 | Ao usar, toca SE, exibe diálogo e cura 5 pontos de vida (respeitando limite de HP Máx do alvo). |

---

## Resumo Rápido de Balanceamento (para consulta no GDD)

* **Early Game**

  * Player L1 (Espada Normal + Escudo de Madeira):

    * Atk ≈ 4, Def ≈ 1, HP 12.
  * Green Slime: HP 8, Atk 3 → morre em 2–3 hits; mata o player em ~6 hits.
  * Bat: HP 10, Atk 4 → morre em 3 hits; mata o player em ~4 hits.

* **Mid Game**

  * Player L3–L5 com equipamentos melhores (Machado, Escudo Azul, Botas):

    * Atk com Machado vai de 8–18, defesa pode chegar perto de 10 com DEX alta.
  * Red Slime e Orc: mais HP, mais dano, exigem uso de poções e defesa.

* **Boss (Skeleton Lord)**

  * HP 80, DEF 4, duas fases (ATK 10 → 15, SPD 1 → 2).
  * Complayer por volta do nível 5, luta dura ~10 hits bem encaixados e punidos se tomar combos na fase 2.
