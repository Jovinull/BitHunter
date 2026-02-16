# <div align="center">BitHunter</div>
# <div align="center"><sub>Um RPG 2D top-down em Java (AWT/Swing) — engine “na unha”, com foco em sistemas, legibilidade e iteração rápida.</sub></div>
<br/>

<div align="center">
  <table>
    <tr>
      <td align="left">
        <b>🧭 O que você encontra aqui</b><br/>
        - Loop de jogo + render 2D (Swing)<br/>
        - Tiles/mapas via resources<br/>
        - Entidades (player/NPC/monstros/itens) + colisão<br/>
        - Save/Load (serialização)<br/>
        - UI modular (HUD, trade, opções, pet yard…)<br/>
        - i18n com <code>.properties</code><br/>
      </td>
      <td align="left">
        <b>⚠️ Observação importante sobre assets</b><br/>
        O código referencia <i>sprites</i>, <i>tiles</i>, fontes e sons via classpath (<code>/objects</code>, <code>/player</code>, <code>/tiles</code>, <code>/sounds</code>, <code>/font</code>…), mas este repositório (no estado atual) <b>não contém</b> arquivos <code>.png</code>/<code>.wav</code>/<code>.ttf</code> versionados.<br/>
        <br/>
        Isso significa que o projeto pode <b>não rodar “do zero”</b> sem adicionar esses recursos na pasta <code>resource/</code> (ver <a href="#recursos-assets-e-classpath">Recursos (assets) e classpath</a>).
      </td>
    </tr>
  </table>
</div>

<br/>

<div align="center">
  <table>
    <tr>
      <td align="center"><b>🎮</b><br/><sub>Jogo 2D top-down</sub></td>
      <td align="center"><b>☕</b><br/><sub>Java SE + AWT/Swing</sub></td>
      <td align="center"><b>🗺️</b><br/><sub>Mapas em <code>resource/maps/*.txt</code></sub></td>
      <td align="center"><b>🌍</b><br/><sub>i18n via <code>resource/lang</code></sub></td>
      <td align="center"><b>💾</b><br/><sub>Save/Load em <code>save.dat</code></sub></td>
    </tr>
  </table>
</div>

<br/>

> **TL;DR (honesto):** o repositório documenta e implementa boa parte da “engine” e dos sistemas (UI, entidades, mapas, eventos, pets, save/load…), mas **os assets binários não estão presentes**. O README abaixo explica como navegar, entender a arquitetura e o que é necessário para rodar localmente com o que existe hoje.

---

## Navegação

- [BitHunter](#bithunter)
- [Um RPG 2D top-down em Java (AWT/Swing) — engine “na unha”, com foco em sistemas, legibilidade e iteração rápida.](#um-rpg-2d-top-down-em-java-awtswing--engine-na-unha-com-foco-em-sistemas-legibilidade-e-iteração-rápida)
  - [Navegação](#navegação)
  - [O que é](#o-que-é)
  - [Para quem é](#para-quem-é)
  - [Destaques confirmados no código](#destaques-confirmados-no-código)
  - [Demonstração](#demonstração)
  - [Stack e tecnologias (detectadas no repo)](#stack-e-tecnologias-detectadas-no-repo)
  - [Como rodar localmente](#como-rodar-localmente)
    - [Via VS Code (config já no repo)](#via-vs-code-config-já-no-repo)
    - [Config persistida (config.txt)](#config-persistida-configtxt)
    - [Save/Load (save.dat)](#saveload-savedat)
  - [Estrutura do projeto](#estrutura-do-projeto)
  - [Arquitetura e fluxos](#arquitetura-e-fluxos)
    - [Visão “de cima” (sistemas)](#visão-de-cima-sistemas)
    - [Estados do jogo](#estados-do-jogo)
    - [Controles](#controles)
    - [Mapas e tiles](#mapas-e-tiles)
    - [Entidades, combate e progressão](#entidades-combate-e-progressão)
    - [Pets + incubadora](#pets--incubadora)
    - [Eventos, teleporte e cutscenes](#eventos-teleporte-e-cutscenes)
    - [UI modular (HUD, inventário, trade, opções…)](#ui-modular-hud-inventário-trade-opções)
    - [i18n (traduções)](#i18n-traduções)
    - [Áudio](#áudio)
    - [Iluminação / ciclo dia-noite](#iluminação--ciclo-dia-noite)
  - [Recursos (assets) e classpath](#recursos-assets-e-classpath)
  - [Padrões e convenções do repo](#padrões-e-convenções-do-repo)
  - [Testes e qualidade](#testes-e-qualidade)
  - [Troubleshooting (baseado no que o repo mostra)](#troubleshooting-baseado-no-que-o-repo-mostra)
  - [Automação / CI/CD](#automação--cicd)
  - [Roadmap](#roadmap)
    - [Backlog existente (GDD)](#backlog-existente-gdd)
    - [Próximos passos sugeridos (observando lacunas do repo)](#próximos-passos-sugeridos-observando-lacunas-do-repo)
  - [Contribuição](#contribuição)
  - [Licença](#licença)

---

## O que é

**BitHunter** é um projeto de jogo (RPG 2D top-down) implementado em **Java**, com renderização 2D e UI via **AWT/Swing**, organizado como uma “mini engine” com:

- **Loop de jogo** em thread dedicada (`main.GamePanel`)
- **Entidades** e gameplay (player/NPC/monstros/itens) via herança de `entity.Entity`
- **Mapas** em texto (`resource/maps/*.txt`) e tiles carregados por `tile.TileManager`
- **UI modular** por estados (HUD, inventário, trade, opções, pet yard, etc.) em `ui.*`
- **Save/Load** por serialização de um `data.DataStorage` em `save.dat`
- **Traduções** via `resource/lang/messages_*.properties` + `main.LanguageManager`

O repo também inclui um documento de design: `gdd.md` (**Game Design Document**) que descreve visão, loop central, controles e backlog.

## Para quem é

- **Quem estuda engenharia de jogos em Java**: arquitetura de sistemas (UI, entidades, mapas, eventos) em um projeto “real”.
- **Quem quer um playground de gameplay-systems**: inventário, trade, progressão, eventos/teleportes, IA simples com pathfinding.
- **Quem quer evoluir o projeto**: adicionar pipeline de build, empacotamento, assets, testes e automações.

---

## Destaques confirmados no código

<div>
  <table>
    <tr>
      <td>
        <b>🎛️ Estados e UI por módulos</b><br/>
        Estados como Title/Play/Pause/Dialogue/Options/Trade/Pet Yard/Map estão declarados em <code>main.GamePanel</code> e desenhados via <code>ui.UI</code> + sub-UIs.
      </td>
      <td>
        <b>🌍 i18n (multi-idioma)</b><br/>
        Textos via <code>main.LanguageManager</code> e arquivos <code>resource/lang/messages_*.properties</code> (com menu de idioma em Opções).
      </td>
    </tr>
    <tr>
      <td>
        <b>💾 Save/Load</b><br/>
        Persistência em <code>save.dat</code> com <code>ObjectOutputStream</code>/<code>ObjectInputStream</code> (classe <code>data.SaveLoad</code>).
      </td>
      <td>
        <b>🗺️ Mapa + mini-mapa</b><br/>
        Tela de mapa e mini-mapa (toggle) implementados em <code>tile.Map</code> e acionados por teclas (ver <a href="#controles">Controles</a>).
      </td>
    </tr>
    <tr>
      <td>
        <b>🤖 IA e pathfinding</b><br/>
        A* em <code>ai.PathFinder</code> com máscara de sólidos incluindo <code>InteractiveTile</code>.
      </td>
      <td>
        <b>🐾 Pets e incubação de ovos</b><br/>
        Coleção de pets, pet ativo, XP do pet e incubadora (<code>pets.PetIncubator</code> + <code>ui.PetYardUi</code>).
      </td>
    </tr>
    <tr>
      <td>
        <b>⚔️ Combate + guarda</b><br/>
        Ataque corpo-a-corpo (ENTER), projétil/magia (F) e sistema de guarda (SPACE) com stamina (em <code>entity.Player</code> + <code>entity.Entity</code>).
      </td>
      <td>
        <b>🌗 Iluminação e ciclo dia/noite</b><br/>
        Overlay com <code>RadialGradientPaint</code> e fases do dia em <code>environment.Lighting</code>.
      </td>
    </tr>
    <tr>
      <td>
        <b>📖 Bestiário</b><br/>
        Sistema de enciclopédia de criaturas com progressão por abates, 3 estágios de revelação, categorias e pontos de recompensa (<code>bestiary.*</code> + <code>ui.BestiaryUi</code>).
      </td>
      <td>
        <b>🔧 Ferramentas de validação</b><br/>
        Validador i18n (<code>tools/I18nValidator.java</code>) para garantir paridade de chaves entre todos os idiomas.
      </td>
    </tr>
  </table>
</div>

---

## Demonstração

Este repositório (no estado atual) **não contém imagens/screencasts** versionados em `public/`, `docs/` ou `assets/`.

<details>
  <summary><b>Como gerar uma demo a partir do repo (sem prometer output)</b></summary>
  <br/>

  - **Capturas de tela**: rode o jogo localmente e capture as telas mais “explicativas”:
    - Title Screen
    - HUD em gameplay (barras HP/MP/Guarda + XP no topo)
    - Inventário / tela de personagem
    - Trade com o comerciante
    - Pet Yard + painel de incubação
    - Mapa / mini-mapa
  - **Onde versionar**: crie uma pasta `docs/img/` e adicione as imagens lá (o README já está pronto para incorporar assets locais quando existirem).
</details>

---

## Stack e tecnologias (detectadas no repo)

- **Linguagem**: Java (código em `src/`).
- **UI / render**: Swing/AWT (`JFrame`, `JPanel`, `Graphics2D`) — entrypoint em `main.Main`.
- **Áudio**: `javax.sound.sampled` (`Clip`, `AudioSystem`) em `main.Sound`.
- **Imagens**: `ImageIO` + carga via classpath em `entity.Entity.setup(...)` e `tile.TileManager`.
- **i18n**: `java.util.Properties` + arquivos `.properties` em `resource/lang` (`main.LanguageManager`).
- **Persistência**: serialização Java em `data.SaveLoad` (`save.dat`).
- **Editor tooling**: configurações de VS Code em `.vscode/` (debug/launch/tasks + formatter).

---

## Build (com resources)

> **Requisito para rodar:** ter o **Java** instalado (JRE/JDK) e o comando `java` disponível no PATH.
> **Requisito para buildar:** ter o **JDK** instalado (com `javac` disponível).

```bash
rm -rf build && mkdir -p build/classes

# 1) Compila
javac --release 24 -d build/classes $(find src -name "*.java")

# 2) Copia resources para a raiz do classpath
# (isso cria build/classes/maps e build/classes/tiles, etc.)
cp -r resource/* build/classes/

# 3) Empacota
jar --create --file build/app.jar --main-class main.Main -C build/classes .

# 4) Confirma que entrou
jar tf build/app.jar | grep -E '^(maps|tiles)/' | head -n 50

# 5) Roda
java -jar build/app.jar
```

---

## Como rodar localmente

### Via VS Code (config já no repo)

O repositório inclui uma configuração de execução para Java:

- `.vscode/launch.json` define o launch **“Run BitHunter”** com `mainClass: "main.Main"`.
- `.vscode/settings.json` define `java.project.sourcePaths` como `["src", "resource"]` (ou seja, **a pasta `resource/` é tratada como source/resource path**).
- `.vscode/extensions.json` recomenda o ecossistema de Java para VS Code (RedHat Java + Java Pack + Debug/Test/Dependency).

**Fluxo recomendado (sem depender de comandos):**

1. Abra o workspace no VS Code.
2. Garanta que você tem um JDK configurado (o projeto é Java).
3. Selecione a configuração **Run BitHunter** e execute a classe `main.Main`.

> Se você estiver com problemas já na abertura do jogo (ícone, fontes, sprites, sons), pule para [Troubleshooting](#troubleshooting-baseado-no-que-o-repo-mostra) e para [Recursos (assets) e classpath](#recursos-assets-e-classpath).

<details>
  <summary><b>Task utilitária existente no repo</b></summary>
  <br/>

  Em `.vscode/tasks.json` existe a task **“Clean build”** que remove a pasta `bin/` e imprime “limpo.”.
  Observação: o comando configurado usa `rm -rf`, que é típico de ambientes Unix/macOS; em Windows pode não funcionar sem um shell compatível.
</details>

### Config persistida (config.txt)

O jogo salva/carrega preferências em `config.txt` (no diretório de trabalho).

O formato atual, linha a linha (`main.Config`), é:

| Linha | Conteúdo                | Exemplo | Efeito                                                            |
| ----: | ----------------------- | ------- | ----------------------------------------------------------------- |
|     1 | Fullscreen (`On`/`Off`) | `Off`   | Liga/desliga tela cheia (efeito completo após reiniciar — ver UI) |
|     2 | Volume música (0..5)    | `3`     | Ajusta `music.volumeScale`                                        |
|     3 | Volume efeitos (0..5)   | `3`     | Ajusta `se.volumeScale`                                           |
|     4 | Idioma (ex. `pt_BR`)    | `pt_BR` | Define idioma via `LanguageManager`                               |

### Save/Load (save.dat)

- O jogo usa `save.dat` como arquivo de save (`data.SaveLoad`).
- O `.gitignore` do repo ignora `*.dat`, então saves locais **não** entram em commit por padrão.

---

## Estrutura do projeto

> Abaixo está uma visão fiel ao que existe hoje (sem assumir build system ou monorepo).

```
.
├─ src/
│  ├─ ai/                 # Pathfinding (A*)
│  ├─ bestiary/           # Bestiário (registros, serviço, progressão)
│  ├─ data/               # Save/Load + estruturas serializáveis
│  ├─ entity/             # Entity base + Player + NPCs
│  ├─ environment/        # Iluminação / ambiente
│  ├─ main/               # Entry e “núcleo” (GamePanel, input, config, etc.)
│  ├─ monster/            # Monstros (inclui boss)
│  ├─ object/             # Itens/objetos do mundo (coin, chest, door...)
│  ├─ pets/               # Pets, ovos, incubadora
│  ├─ tile/               # Tiles, mapas e minimapa
│  ├─ tile_interactive/   # Tiles interativos/destrutíveis
│  └─ ui/                 # UI modular por telas/estados
│
├─ resource/
│  ├─ bestiary/           # dados do bestiário (creatures.properties)
│  ├─ lang/               # i18n: messages_*.properties
│  └─ maps/               # mapas em texto + tiledata.txt
│
├─ tools/
│  └─ I18nValidator.java  # validador de chaves de tradução
│
├─ .vscode/               # debug/launch/tasks + formatter + recomendações
├─ config.txt             # preferências (fullscreen/volumes/idioma)
├─ gdd.md                 # Game Design Document (visão + backlog)
└─ LICENSE                # licença do código e dos assets
```

---

## Arquitetura e fluxos

### Visão “de cima” (sistemas)

<div>
  <table>
    <tr>
      <td>
        <b>Bootstrap</b><br/>
        <code>main.Main</code> cria a janela (<code>JFrame</code>), adiciona <code>GamePanel</code>, carrega config e inicia a thread do jogo.
      </td>
      <td>
        <b>Loop</b><br/>
        <code>GamePanel.run()</code> atualiza + desenha a ~60 FPS (delta timing).
      </td>
    </tr>
    <tr>
      <td>
        <b>Update</b><br/>
        <code>GamePanel.update()</code> delega por estado (principalmente <code>PLAY_STATE</code>) e atualiza player/NPCs/monstros/projéteis/partículas/tiles interativos + ambiente + incubadora.
      </td>
      <td>
        <b>Render</b><br/>
        Desenha tiles → tiles interativos → entidades (ordenadas por <code>worldY</code>) → ambiente → mini-mapa → cutscene → UI.
      </td>
    </tr>
  </table>
</div>

### Estados do jogo

Os estados são centralizados em `main.GamePanel` e consumidos por `main.KeyHandler` (input) e `ui.UI` (render UI):

- `TITLE_STATE`, `PLAY_STATE`, `PAUSE_STATE`, `DIALOGUE_STATE`, `CHARACTER_STATE`
- `OPTIONS_STATE`, `GAME_OVER_STATE`, `TRANSITION_STATE`, `TRADE_STATE`, `SLEEP_STATE`
- `MAP_STATE`, `CUTSCENE_STATE`, `PET_YARD_STATE`, `BESTIARY_STATE`

### Controles

Baseado em `main.KeyHandler` e nos textos em `resource/lang/messages_*.properties`:

| Contexto       | Tecla     | Ação                                                    |
| -------------- | --------- | ------------------------------------------------------- |
| Gameplay       | `W/A/S/D` | mover                                                   |
| Gameplay       | `ENTER`   | confirmar / atacar / interagir (dependendo do contexto) |
| Gameplay       | `F`       | atirar/conjurar (projétil/habilidade)                   |
| Gameplay       | `SPACE`   | guarda (se houver stamina/escudo)                       |
| Gameplay       | `B`       | bestiário                                               |
| Gameplay       | `C`       | tela do personagem                                      |
| Gameplay       | `P`       | pausar                                                  |
| Gameplay       | `ESC`     | opções                                                  |
| Gameplay       | `M`       | mapa (tela cheia)                                       |
| Gameplay       | `X`       | liga/desliga mini-mapa                                  |
| Dev (gameplay) | `T`       | alterna debug (e desenha path)                          |
| Dev (gameplay) | `R`       | recarrega mapa atual (atalho)                           |
| Dev (gameplay) | `G`       | alterna “god mode”                                      |
| Menus          | `W/S`     | navegar                                                 |
| Menus          | `ENTER`   | confirmar                                               |
| Menus          | `ESC`     | voltar (onde aplicável)                                 |

> Nota: o atalho `R` chama `loadMap("/maps/interior01.txt", 1)` em `KeyHandler`, mas o repo contém `resource/maps/indoor01.txt`. Isso sugere um **mismatch** de nome de mapa no atalho (ver [Troubleshooting](#troubleshooting-baseado-no-que-o-repo-mostra)).

### Mapas e tiles

- **Mapas**: `resource/maps/world01.txt`, `indoor01.txt`, `dungeon01.txt`, `dungeon02.txt`.
- **Metadados de tiles**: `resource/maps/tiledata.txt` (lista + flag de colisão por tile).
- **Carregamento/desenho**: `tile.TileManager` mantém `mapTileNum` e desenha apenas o “view” visível baseado na posição do player.
- **Mini-mapa e mapa**: `tile.Map` herda `TileManager` e pré-renderiza um `BufferedImage` por mapa para desenhar:
  - o **mapa inteiro** em `MAP_STATE`
  - um **mini-mapa** com alpha em `PLAY_STATE` quando habilitado

### Entidades, combate e progressão

- **Base**: `entity.Entity` define direção, sprites, áreas de colisão/ataque, inventário, atributos, flags de estado e helpers (incluindo `setup(...)` para sprites).
- **Player**: `entity.Player`
  - atributos (level/HP/MP/STR/DEX/EXP/coins)
  - inventário (até 20)
  - ataque corpo-a-corpo + projétil (Fireball)
  - guarda com stamina (se houver escudo/guard capacity)
  - integração com pets (pet ativo, XP, etc.)
- **Monstros**: `monster.*` inclui inimigos comuns e `MonSkeletonLord` (boss) com fase 2 (rage) e sprites específicos.
- **Objetos/itens**: `object.*` (coin, key, chest, doors, potion, lantern, armas, etc.)
- **Interativos**: `tile_interactive.*` (árvores/parede destrutível/placa metálica/tronco…)

### Pets + incubadora

- **Registry/factory**: `pets.PetFactory` define IDs estáveis e criação de instâncias.
- **Incubação**: `pets.PetIncubator` gerencia 1 ovo por vez, tickando apenas em `PLAY_STATE`.
- **UI**: `ui.PetYardUi` oferece:
  - gerenciar/equipar pet
  - escolher ovo no inventário e iniciar incubação (com custo em moedas)
  - painel de status com progresso e tempo restante

### Eventos, teleporte e cutscenes

`main.EventHandler` centraliza eventos por tile (com `EventRect`):

- dano em “pit”
- “healing pool” que restaura vida/mana, respawna monstros e salva
- teleporte entre mapas (outside/indoor/dungeon/boss)
- trigger de boss (`Skeleton Lord`) iniciando cutscene

### UI modular (HUD, inventário, trade, opções…)

`ui.UI` é o orquestrador e instancia sub-módulos:

- `HudUi`: barras de HP/MP/Guarda, XP top bar, HP de monstros, mensagens, HUD do pet
- `TitleUi`: título + menu e seleção de classe
- `OptionsUi`: tela cheia, volume música/efeitos, controles (paginado), idioma (lista scrollável), sair para title
- `TradeUi`: compra/venda (com validações de moedas/inventário)
- `PetYardUi`: gerenciamento e incubação
- além de Pause/Dialogue/Character/Transition/GameOver etc.

### i18n (traduções)

- Arquivos disponíveis: `resource/lang/messages_{pt_BR,en_US,es_ES,fr_FR,it_IT,de_DE,ru_RU,ja_JP,ko_KR,zh_CN}.properties`
- Idiomas expostos no menu (hoje): definidos em `LanguageManager.getAvailableLanguages()`.
- Estratégia: `LanguageManager.text(key)` retorna `??key??` quando não encontra (com fallback para `pt_BR` quando aplicável).

<details>
  <summary><b>Validador de chaves i18n (tools/I18nValidator.java)</b></summary>
  <br/>

  O projeto inclui um utilitário em `tools/I18nValidator.java` que compara todos os arquivos de idioma com o idioma base (`en_US`) e aponta chaves faltantes ou sobrando. Sem dependências externas — Java puro.

  **Como usar:**

  ```bash
  # A partir da raiz do projeto
  javac tools/I18nValidator.java -d tools
  java -cp tools I18nValidator
  ```

  **Saída esperada (quando tudo está OK):**

  ```
  =============================================================
   i18n Key Validator
  =============================================================
  Idioma base: messages_en_US.properties (245 chaves)
  -------------------------------------------------------------

  [OK] messages_de_DE.properties (245 chaves)
    Todas as chaves estão presentes.
  ...
  =============================================================
  RESULTADO: TODOS OS IDIOMAS ESTÃO COMPLETOS!
  ```

  **Exit codes:**
  - `0` — todos os idiomas estão completos
  - `1` — erro de configuração (diretório/arquivo base não encontrado)
  - `2` — inconsistências detectadas (chaves faltantes ou sobrando)

  > **Dica:** rode o validador sempre que adicionar novas chaves de tradução para garantir que todos os idiomas ficaram sincronizados.
</details>

### Áudio

`main.Sound` mapeia sons por índice carregando `.wav` via `getResource("/sounds/...")`, com volume ajustável por `volumeScale` (0..5).

### Iluminação / ciclo dia-noite

`environment.Lighting` cria um overlay (`darknessFilter`) com gradiente radial baseado na luz equipada do player (quando existe) e alterna fases do dia (DAY/DUSK/NIGHT/DAWN) ajustando `filterAlpha`.

---

## Recursos (assets) e classpath

O projeto carrega recursos via classpath (ex.: `getResourceAsStream("/maps/world01.txt")`, `setup("/objects/coin_bronze", ...)`, `getResource("/sounds/coin.wav")`).

**O que existe versionado hoje em `resource/`:**

- `resource/maps/*.txt` + `tiledata.txt`
- `resource/lang/messages_*.properties`

**O que o código referencia (paths típicos) — mas que não está presente no repositório no estado atual (sem `.png/.wav/.ttf`):**

- `"/tiles/"` (tileset)
- `"/tiles_interactive/"` (tiles interativos)
- `"/objects/"` (itens/ícones/objetos)
- `"/player/Walking sprites/"`, `"/player/Attacking sprites/"`, `"/player/Guarding sprites/"` (sprites do player)
- `"/npc/"` (sprites de NPCs)
- `"/monster/"` (sprites de monstros/boss)
- `"/projectile/"` (sprites de projétil)
- `"/pets/"`, `"/eggs/"` (sprites de pets e ovos)
- `"/sounds/"` (áudios `.wav`)
- `"/font/"` (fontes `.ttf`)

<details>
  <summary><b>Como encaixar assets corretamente (modelo mental)</b></summary>
  <br/>

  - Como `.vscode/settings.json` define `resource` em `java.project.sourcePaths`, a pasta `resource/` entra no classpath do projeto quando você roda pelo VS Code.
  - Portanto, um path como `"/maps/world01.txt"` deve existir como `resource/maps/world01.txt`.
  - Um path como `"/objects/coin_bronze.png"` deve existir como `resource/objects/coin_bronze.png`.
  - E assim por diante, respeitando **maiúsculas/minúsculas e espaços** no nome da pasta (por exemplo: `"/player/Walking sprites/..."`).
</details>

---

## Padrões e convenções do repo

- **`.editorconfig`**: `LF`, UTF-8, trim de trailing whitespace, indentação de **2 espaços**.
- **VS Code**: `.vscode/settings.json` ativa format on save/paste e organiza imports em Java, usando um formatter XML em `.vscode/eclipse-formatter.xml`.

---

## Testes e qualidade

No estado atual, o repositório **não contém** suíte de testes automatizados (não há evidência de JUnit/Gradle/Maven nem diretório de testes).
Ainda assim, alguns pontos que já ajudam a manter qualidade:

- UI e gameplay separam responsabilidades por pacotes (`ui/`, `entity/`, `tile/`, `data/`, `main/`, `bestiary/`, etc.).
- `LanguageManager` e `Config` são módulos bem isolados.
- O projeto registra e ignora artefatos locais (ex.: `save.dat`) via `.gitignore`.
- **Validador i18n**: `tools/I18nValidator.java` verifica paridade de chaves entre todos os idiomas (ver seção [i18n](#i18n-traduções)).

---

## Troubleshooting (baseado no que o repo mostra)

<details>
  <summary><b>1) Jogo abre “quebrado”, sem sprites/sons/fontes, ou crasha ao carregar recursos</b></summary>
  <br/>

  **Causa provável:** o código referencia recursos (`.png`, `.wav`, `.ttf`) via classpath, mas o repo não contém esses arquivos.
  **Como confirmar:** procure pelos paths em `entity.Entity.setup(...)`, `main.Sound`, `ui.UI`, `tile.TileManager` e compare com a estrutura existente em `resource/`.
  <br/><br/>
  **Ação:** adicione os assets em `resource/` seguindo os paths esperados (ver [Recursos (assets) e classpath](#recursos-assets-e-classpath)).
</details>

<details>
  <summary><b>2) Atalho de dev “R” não recarrega o mapa indoor</b></summary>
  <br/>

  `KeyHandler` chama `loadMap("/maps/interior01.txt", 1)`, mas o repositório contém `resource/maps/indoor01.txt` (sem “interior”).
  **Sintoma:** recarregar o mapa pode falhar/silenciar erro (dependendo de logging/exception).
  **Ação:** alinhar o nome do arquivo (código vs resource).
</details>

<details>
  <summary><b>3) “Clean build” no Windows não funciona</b></summary>
  <br/>

  A task do VS Code usa `rm -rf bin`. Em Windows, isso depende do shell.
  **Ação:** rodar a task em ambiente compatível ou ajustar o comando na task para o seu shell.
</details>

<details>
  <summary><b>4) Tela cheia só aplica após reiniciar</b></summary>
  <br/>

  A UI exibe um aviso específico: `ui.options.fullscreenNotice` (“A alteração terá efeito após reiniciar o jogo.”).
  Isso é consistente com `main.Main`, que decide `setUndecorated(true)` no boot baseado na flag carregada.
</details>

<details>
  <summary><b>5) "The import [pacote] cannot be resolved" / "Unresolved compilation problems" (cache do JDT)</b></summary>
  <br/>

  **Sintoma:** ao rodar o jogo pelo VS Code/Cursor, o erro aparece como:
  ```
  Exception in thread "main" java.lang.Error: Unresolved compilation problems:
      The import bestiary cannot be resolved
      BestiaryService cannot be resolved to a type
      ...
  ```
  O código-fonte está correto e compila normalmente via `javac` no terminal, mas o Java Language Server (RedHat JDT) não reconhece pacotes ou classes — geralmente após adicionar novos pacotes/arquivos ao projeto.

  **Causa:** o workspace cache do JDT (armazenado em `%APPDATA%\Code\User\workspaceStorage\...\redhat.java\jdt_ws\`) ficou desatualizado e os `.class` gerados contêm erros embutidos.

  **Solução (recomendada):**
  1. Abra a **Command Palette** (`Ctrl+Shift+P`)
  2. Execute **"Java: Clean Java Language Server Workspace"**
  3. Confirme clicando em **"Restart and delete"**

  Isso limpa todo o cache de compilação e força uma reindexação completa do projeto.

  **Solução alternativa (manual):**
  ```powershell
  # Feche o Cursor/VS Code primeiro, depois execute:
  Remove-Item -Recurse -Force "$env:APPDATA\Code\User\workspaceStorage\*\redhat.java\jdt_ws"
  ```
  Reabra o editor em seguida — o JDT vai recompilar tudo do zero.

  **Como confirmar que o problema é cache (e não código):**
  ```powershell
  # Compilar direto pelo terminal (a partir da raiz do projeto):
  & "caminho\para\javac.exe" --enable-preview --source 25 -d out -sourcepath src src\main\GamePanel.java
  ```
  Se compilar sem erros, o problema é exclusivamente do cache do JDT.
</details>

---

## Automação / CI/CD

O repositório **não contém** `.github/workflows/` nem pipelines de CI detectáveis.
Também não há `Dockerfile`/`docker-compose`/Makefile.

Se você quiser evoluir isso, uma direção segura (sem “assumir stack”) é:

- adicionar um build system Java (Maven/Gradle) para padronizar build/run/test
- adicionar workflow de CI para build + lint + testes (quando existirem)
- criar release empacotado (JAR) + instruções de distribuição

---

## Roadmap

### Backlog existente (GDD)

O arquivo `gdd.md` contém uma seção “Conteúdo Futuro / Backlog” e vários pontos marcados como **Planejado**, incluindo ideias como:

- expansão de sistemas (crafting/imbuements, contratos de caça, seeds de mapa)
- UX/acessibilidade extra
- evolução de progressão/endgame

> Importante: o GDD descreve visão e planejamento; este README separa explicitamente o que é “implementado no código” do que está “planejado”.

### Próximos passos sugeridos (observando lacunas do repo)

Sem inventar escopo, estas são melhorias naturalmente sugeridas pelo estado atual:

- **Adicionar/commitar assets (ou documentar onde obtê-los)**: hoje o código referencia muitos recursos que não estão no repo.
- **Padronizar build/run** (Maven/Gradle) para facilitar onboarding fora do VS Code.
- **Documentar uma “matriz de recursos”** (quais arquivos existem para cada path) para reduzir troubleshooting.
- **Criar screenshots** e um `docs/` com guias curtos (por exemplo, “como adicionar um novo item/monstro/tile”).
- **Testes unitários** para módulos puros (ex.: i18n, save/load, regras de progressão).

---

## Contribuição

O repositório não contém `CONTRIBUTING.md` nem templates de PR/Issue. Ainda assim, um fluxo que combina com a estrutura atual:

- **Discussão técnica**: abra uma Issue descrevendo “o quê” e “por quê” (principalmente para mudanças em assets e gameplay).
- **PRs pequenas**: alterações focadas por subsistema (`ui/`, `entity/`, `tile/`, `data/`…).
- **Compatibilidade com saves**: mudanças em `internalId` e em `data.DataStorage` afetam `save.dat`. Evite renomear IDs estáveis sem estratégia de migração.
- **i18n**: sempre que adicionar textos, inclua chaves em `messages_pt_BR.properties` e (quando possível) nos demais idiomas.

---

## Licença

O arquivo `LICENSE` define um modelo “duplo”:

- **Código**: **MIT License**.
- **Assets** (arte/áudio/fontes/vídeo etc.): **CC BY-NC-ND 4.0** (atribuição obrigatória, não comercial, sem derivativos).

> Consulte `LICENSE` para os termos completos e para o racional (inclusive orientações práticas).

