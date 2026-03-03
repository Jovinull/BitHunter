# Arquitetura — BitHunter

## Visão geral

BitHunter é um **RPG 2D top-down** em Java puro (AWT/Swing), sem engine externa.
A arquitetura segue um padrão de **hub central** (`GamePanel`) com sistemas modulares.

```
┌─────────────────────────────────────────────────────┐
│                    main.Main                         │
│              (Bootstrap + JFrame)                     │
└────────────────────┬────────────────────────────────┘
                     │ cria
                     ▼
┌─────────────────────────────────────────────────────┐
│                 main.GamePanel                        │
│          (Hub central + Game Loop ~60 FPS)            │
│                                                       │
│  ┌──────────┐  ┌───────────┐  ┌──────────────────┐  │
│  │KeyHandler│  │  Sound ×2 │  │CollisionChecker   │  │
│  │ (input)  │  │(music+SE) │  │                   │  │
│  └──────────┘  └───────────┘  └──────────────────┘  │
│                                                       │
│  ┌──────────┐  ┌───────────┐  ┌──────────────────┐  │
│  │  Config  │  │EventHandler│  │  AssetSetter     │  │
│  │(config.txt)│ │(teleport, │  │(posiciona ents.) │  │
│  └──────────┘  │ eventos)  │  └──────────────────┘  │
│                └───────────┘                          │
│  ┌──────────┐  ┌───────────┐  ┌──────────────────┐  │
│  │TileManager│ │ PathFinder│  │EntityGenerator    │  │
│  │(mapas)    │ │  (A*)     │  │(fábrica de ents.)│  │
│  └──────────┘  └───────────┘  └──────────────────┘  │
│                                                       │
│  ┌──────────┐  ┌───────────┐  ┌──────────────────┐  │
│  │   UI     │  │Environment│  │CutsceneManager   │  │
│  │(sub-UIs) │  │Manager    │  │                   │  │
│  └──────────┘  └───────────┘  └──────────────────┘  │
│                                                       │
│  ┌──────────┐  ┌───────────┐  ┌──────────────────┐  │
│  │  Map     │  │SaveLoad   │  │LanguageManager   │  │
│  │(minimapa)│  │(save.dat) │  │(i18n)            │  │
│  └──────────┘  └───────────┘  └──────────────────┘  │
│                                                       │
│  ┌──────────┐  ┌───────────┐  ┌──────────────────┐  │
│  │BestiaryService│ │PetIncubator│ │CheatCodeManager│  │
│  └──────────┘  └───────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────┘
```

## Ciclo principal (Game Loop)

```
Main.main()
  └→ GamePanel (JPanel + Runnable)
       └→ run() — loop com delta timing (~60 FPS)
            ├→ update()
            │    ├→ Player.update()
            │    ├→ NPC[].update()
            │    ├→ Monster[].update()
            │    ├→ Projectile[].update()
            │    ├→ Particle[].update()
            │    ├→ InteractiveTile[].update()
            │    ├→ EnvironmentManager.update()
            │    └→ PetIncubator.tick()
            │
            └→ draw()
                 ├→ TileManager.draw()        (camada de fundo)
                 ├→ InteractiveTile[].draw()   (tiles interativos)
                 ├→ Entities ordenadas por Y   (player, NPCs, monstros, itens)
                 ├→ EnvironmentManager.draw()  (iluminação overlay)
                 ├→ Map.drawMiniMap()          (mini-mapa se ativo)
                 ├→ CutsceneManager.draw()     (cutscenes se ativas)
                 └→ UI.draw()                  (HUD, menus, diálogos)
```

## Estados do jogo

O `GamePanel` gerencia 14 estados que controlam input e renderização:

| Estado | Const | Descrição |
|---|---|---|
| Título | `TITLE_STATE` | Menu principal + seleção de classe |
| Jogo | `PLAY_STATE` | Gameplay ativo |
| Pausa | `PAUSE_STATE` | Jogo pausado |
| Diálogo | `DIALOGUE_STATE` | Conversa com NPC |
| Personagem | `CHARACTER_STATE` | Tela de stats/equipamento |
| Opções | `OPTIONS_STATE` | Configurações |
| Game Over | `GAME_OVER_STATE` | Tela de morte |
| Transição | `TRANSITION_STATE` | Fade entre mapas |
| Trade | `TRADE_STATE` | Compra/venda com NPC |
| Sleep | `SLEEP_STATE` | Animação de dormir |
| Mapa | `MAP_STATE` | Mapa em tela cheia |
| Cutscene | `CUTSCENE_STATE` | Cutscene de boss |
| Pet Yard | `PET_YARD_STATE` | Gerenciamento de pets |
| Bestiário | `BESTIARY_STATE` | Enciclopédia de criaturas |

## Sistema de entidades

### Hierarquia

```
Entity (base)
├── Player
├── PlayerDummy
├── NpcOldMan
├── NpcMerchant
├── NpcBigRock
├── NpcPetYardKeeper
├── MonGreenSlime
├── MonRedSlime
├── MonBat
├── MonOrc
├── MonSkeletonLord (boss)
├── Projectile
│   └── ObjFireball
│   └── ObjRock
├── Particle
├── PetEntity
│   ├── GreenSnail
│   └── YellowSnail
├── InteractiveTile
│   ├── ItDryTree
│   ├── ItDestructibleWall
│   ├── ItMetalPlate
│   └── ItTrunk
└── Obj* (itens: ObjKey, ObjChest, ObjDoor, etc.)
```

### Componentes (entity/components/)

| Componente | Responsabilidade |
|---|---|
| `CombatComponent` | Ataque, janela de dano, hit detection |
| `MovementComponent` | Movimento, knockback, invencibilidade |
| `PathfindingComponent` | Integração com A* |
| `RespawnComponent` | Respawn com delay e animação |
| `InventoryComponent` | Lista de inventário |
| `EquipmentComponent` | Slots de equipamento |
| `DialogueComponent` | Sistema de diálogo com tradução |
| `ItemAttributesComponent` | Atributos de item (atk, def, preço) |

### Serviços (entity/services/)

| Serviço | Responsabilidade |
|---|---|
| `ParticleService` | Geração de partículas |
| `TranslationService` | Helper de tradução (i18n) |

## Sistema de mapas

### Estrutura
- Mapas em texto (`resource/maps/*.txt`) — IDs de tiles separados por espaço
- Metadata de tiles (`resource/maps/tiledata.txt`) — nome de imagem + flag de colisão
- 4 mapas ativos: `world01` (overworld), `indoor01`, `dungeon01`, `dungeon02`
- Array 3D: `mapTileNum[mapIndex][col][row]`

### Áreas
| Área | Const | Mapas |
|---|---|---|
| Outside | `OUTSIDE` | world01 (mapa 0) |
| Indoor | `INDOOR` | indoor01 (mapa 1) |
| Dungeon | `DUNGEON` | dungeon01 (mapa 2), dungeon02 (mapa 3) |

## Sistema de UI

```
UI (orquestrador)
├── HudUi          — HP/MP/guarda/XP/mensagens/pet HUD/HP monstros
├── TitleUi        — Title screen + seleção de classe
├── PauseUi        — Menu de pausa
├── DialogueUi     — Janela de diálogo
├── CharacterUi    — Tela de stats
├── InventoryUi    — Inventário (grid)
├── OptionsUi      — Configurações (fullscreen, volume, idioma)
├── TradeUi        — Compra/venda
├── TransitionUi   — Fades
├── GameOverUi     — Game over
├── PetYardUi      — Gerenciamento de pets + incubação
└── BestiaryUi     — Enciclopédia de criaturas
```

## Sistema de eventos

O `EventHandler` define triggers por tile usando `EventRect[map][col][row]`:
- **Damage pit** — causa dano ao pisar
- **Healing pool** — cura HP/MP, respawna monstros, salva
- **Teleporte** — transição entre mapas (com fade)
- **Boss trigger** — inicia cutscene do Skeleton Lord

> Coordenadas hardcoded em `checkEvent()` — candidato a refatoração futura.

## Persistência

- **Save/Load**: `data.SaveLoad` serializa `data.DataStorage` em `save.dat`
- **Config**: `main.Config` lê/grava `config.txt` (fullscreen, volumes, idioma)
- **Progress**: `data.Progress` — flags globais (ex: boss derrotado)

## i18n

- 10 idiomas em `resource/lang/messages_*.properties`
- `LanguageManager.text(key)` com fallback `??key??`
- Validador: `tools/I18nValidator.java`

## Pontos de atenção (dívida técnica)

| Área | Problema | Impacto |
|---|---|---|
| `GamePanel` | 747 linhas, hub central | Difícil de manter |
| `KeyHandler` | 857 linhas, switch por estado | Difícil de estender |
| `Player` | ~1452 linhas | Muita responsabilidade |
| `Entity` | ~1336 linhas | Base muito pesada |
| `AssetSetter` | Coordenadas hardcoded | Não escalável |
| `EventHandler` | Eventos hardcoded | Não extensível |
| Arrays fixos | `Entity[MAX_MAP][20]` | Limite de entidades |
| Números mágicos | Espalhados pelo código | Manutenção difícil |
| Sem testes | Nenhuma suíte automatizada | Risco em refactors |

## Recomendações de evolução (prioridade sugerida)

1. **Extrair constantes** — substituir números mágicos por nomeados
2. **Data-driven assets** — mover coordenadas do `AssetSetter` para JSON/CSV
3. **Data-driven events** — mover eventos do `EventHandler` para arquivo
4. **Enum para estados** — substituir `int` por enum type-safe
5. **Enum para direções** — substituir strings por enum
6. **Split de classes grandes** — `Player`, `Entity`, `KeyHandler`
7. **Build system** — adotar Maven ou Gradle
8. **Testes unitários** — começar por módulos puros (i18n, save/load, progressão)
9. **CI/CD** — GitHub Actions para build + validação i18n
