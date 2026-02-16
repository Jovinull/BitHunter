# Padrões de Código — BitHunter

## Configuração base

| Propriedade | Valor |
|---|---|
| Indentação | 2 espaços (conforme `.editorconfig`) |
| Encoding | UTF-8 |
| Line endings | LF |
| Trailing whitespace | Remover |
| Final newline | Inserir |
| Formatter | Eclipse Formatter (`.vscode/eclipse-formatter.xml`) |

## Nomenclatura

### Classes
- **PascalCase** sempre
- Prefixos por tipo:
  - `Mon` — monstros (ex: `MonGreenSlime`, `MonSkeletonLord`)
  - `Obj` — objetos/itens (ex: `ObjKey`, `ObjSwordNormal`)
  - `Npc` — NPCs (ex: `NpcMerchant`, `NpcOldMan`)
  - `It` — tiles interativos (ex: `ItDryTree`, `ItDestructibleWall`)
- Sufixos por tipo:
  - `Component` — componentes de entidade (ex: `CombatComponent`)
  - `Service` — serviços (ex: `ParticleService`, `BestiaryService`)
  - `Ui` — sub-UIs (ex: `HudUi`, `TradeUi`)
  - `Manager` — gerenciadores (ex: `TileManager`, `LanguageManager`)

### Métodos e variáveis
- **camelCase** sempre
- Métodos descritivos (ex: `checkCollision`, `loadMap`, `setGameState`)
- Getters/setters: padrão Java (`getX()`, `setX()`, `isAlive()`)

### Constantes
- **UPPER_SNAKE_CASE**
- Declaradas como `public static final` ou `protected static final`
- Exemplos: `TILE_SIZE`, `MAX_MAP`, `TYPE_PLAYER`, `PLAY_STATE`

### IDs internos
- Notação com ponto: `"monster.greenSlime"`, `"object.key"`
- Chaves i18n: `"monster.greenSlime.name"`, `"ui.options.fullscreenNotice"`

## Estrutura de pacotes

```
src/
├── ai/                  # Pathfinding — A* e nós
├── bestiary/            # Bestiário — registro, serviço, progressão
├── data/                # Persistência — save/load, DataStorage
├── entity/              # Entidades base + NPCs
│   ├── components/      # Componentes (combate, movimento, etc.)
│   └── services/        # Serviços de entidade (partículas, tradução)
├── environment/         # Iluminação, ciclo dia/noite
├── main/                # Core — GamePanel, Main, input, config, som
├── monster/             # Monstros (herdam Entity)
├── object/              # Objetos/itens (herdam Entity)
├── pets/                # Sistema de pets e incubação
├── tile/                # Tiles, mapas, minimapa
├── tile_interactive/    # Tiles destrutíveis/interativos
└── ui/                  # Interface — UI principal + sub-UIs
```

## Padrões de implementação

### Novas entidades (monstro, NPC, objeto)
1. Criar classe no pacote correto com prefixo adequado
2. Herdar de `Entity` (ou classe base do tipo)
3. Definir `internalId` (notação com ponto)
4. Implementar `setAction()` para IA (se aplicável)
5. Implementar `checkDrop()` para loot (se aplicável)
6. Registrar em `EntityGenerator` (se necessário)
7. Posicionar via `AssetSetter`

### Novos componentes
1. Criar em `entity/components/`
2. Receber `Entity` no construtor
3. Manter responsabilidade única

### Novas UIs
1. Criar em `ui/` com sufixo `Ui`
2. Instanciar em `UI.java`
3. Delegar `draw()` pelo estado do jogo

### Novas chaves i18n
1. Adicionar em TODOS os arquivos `messages_*.properties`
2. Rodar o validador: `javac tools/I18nValidator.java -d tools && java -cp tools I18nValidator`
3. Garantir que nenhum idioma ficou com chave faltante

## Comentários

- **Português** para comentários descritivos e explicativos
- **JavaDoc** (`/** */`) para APIs públicas e métodos complexos
- **Inline** (`//`) para explicações pontuais
- Evitar comentários óbvios (ex: `// seta o nome` antes de `setName()`)
- Comentar o "porquê", não o "o quê"

## Anti-padrões a evitar

- **Números mágicos** — extrair para constantes nomeadas
- **Métodos longos** (>50 linhas) — considerar split
- **Classes gigantes** (>500 linhas) — considerar extração de componentes
- **Acesso direto a campos** de outras classes — usar getters
- **Strings hardcoded** na UI — usar chaves i18n via `LanguageManager`
- **catch vazio** — pelo menos logar o erro
