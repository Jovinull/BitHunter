# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Language

ALWAYS respond in Portuguese (pt-BR).

## Project Overview

BitHunter is a 2D top-down RPG game written in pure Java (no frameworks, no build system). It features combat, inventory, pets, a bestiary system, and supports 10 languages.

- **Entry point:** `main.Main`
- **Game hub:** `main.GamePanel` — central dispatcher, game loop (~60 FPS), manages 14 game states
- **Java version:** 24+ (`--release 24`)
- **No build system** — manual `javac` compilation

## Build & Run

### Compile (bash)
```bash
rm -rf build && mkdir -p build/classes
javac --release 24 -d build/classes $(find src -name "*.java")
cp -r resource/* build/classes/
jar --create --file build/app.jar --main-class main.Main -C build/classes .
java -jar build/app.jar
```

### Quality checks (must pass before push)
```bash
# Compile with warnings as errors
javac --release 24 -Xlint:all -Werror -d build/classes $(find src -name "*.java")

# Validate i18n key parity across all 10 languages
javac tools/I18nValidator.java -d tools && java -cp tools I18nValidator
```

### VS Code
- **Run:** Launch config "Run BitHunter" (`.vscode/launch.json`)
- **Pre-push:** Task "Pre-push Check" runs both quality checks above

## Architecture

**Hub-and-spoke pattern** centered on `GamePanel`:
- `GamePanel` → `Player`, `TileManager`, `UI`, `KeyHandler`, `Sound`, `EventHandler`, `PathFinder`, `EnvironmentManager`, `BestiaryService`, `PetIncubator`, `CutsceneManager`, `SaveLoad`, `Config`

**14 game states** (TITLE=0, PLAY=1, PAUSE=2, DIALOGUE=3, CHARACTER=4, OPTIONS=5, GAME_OVER=6, TRANSITION=7, TRADE=8, SLEEP=9, MAP=10, CUTSCENE=11, PET_YARD=12, BESTIARY=13)

**Key patterns:**
- Component-based entities: `CombatComponent`, `MovementComponent` in `entity/components/`
- Services: `ParticleService`, `TranslationService`, `BestiaryService`
- UI delegation: `UI` orchestrator delegates to 12+ sub-UIs (`HudUi`, `TradeUi`, `BestiaryUi`, etc.)
- Factory: `EntityGenerator`, `PetFactory`

**Class prefixes:**
- `Mon` = monsters, `Obj` = objects/items, `Npc` = NPCs, `It` = interactive tiles

**New entity checklist:** Create class with prefix → inherit `Entity` → set `internalId` (dot notation like `"monster.greenSlime"`) → implement `setAction()`/`checkDrop()` → register in `EntityGenerator` → place via `AssetSetter`

## i18n

10 languages in `resource/lang/messages_*.properties`. When adding/modifying i18n keys:
1. Update ALL 10 property files
2. Run `I18nValidator` to verify key parity

Keys use path notation: `"monster.greenSlime.name"`, `"ui.options.fullscreenNotice"`

## Code Conventions

- **Indentation:** 2 spaces (`.editorconfig`)
- **Encoding:** UTF-8, line endings: LF
- Classes: `PascalCase`, methods/variables: `camelCase`, constants: `UPPER_SNAKE_CASE`
- Comments in Portuguese; JavaDoc for public APIs
- No magic numbers — extract to named constants
- No hardcoded UI strings — use `LanguageManager` i18n keys

## Mandatory Rules

- **Minimal scope:** Do NOT modify files outside the requested scope
- **No surprise refactors:** Never refactor code that wasn't requested
- **Green build:** Ensure compilation passes after every change
- **Fix warnings** in files you touched
- **Never remove** existing code/documentation without explicit authorization
- **Never commit directly to main** — always use branch + merge (`--no-ff`)

## Commit Policy

Conventional Commits format in Portuguese: `tipo(escopo): descrição curta`
- Types: `feat`, `fix`, `refactor`, `docs`, `style`, `test`, `chore`, `perf`
- Scopes: package or system name (`entity`, `ui`, `monster`, `i18n`, etc.)
- One responsibility per commit
- Always ask "Posso subir o commit?" before pushing

### Push flow
1. Verify compilation (if `.java` files changed)
2. Create branch from main (`tipo/descricao-curta`)
3. Separate changes into logical commits
4. Push branch → merge to main (`--no-ff`) → push main → delete branch (local + remote)

## Response Pattern

Every response should follow: Diagnóstico → Plano → Execução → Testes → Resumo + Commit

## Documentation

All project documentation is in `docs/`:
- `architecture.md` — system design and class hierarchy
- `code_style.md` — naming conventions and patterns
- `runbook.md` — build, run, test, troubleshoot
- `commit_policy.md` — commit format and branch workflow
- `workflow.md` — mandatory AI workflow
- `gdd.md` — Game Design Document
- `BESTIARY_CATEGORIES.md`, `CHEAT_CODES.md`

## No Automated Tests

There are no unit tests. Quality is enforced via:
- `javac -Xlint:all -Werror` (compilation warnings as errors)
- `tools/I18nValidator.java` (translation key validation)
- SonarLint (standalone static analysis in VS Code)

## Assets

Binary assets (sprites, sounds) are NOT in the repo. They must exist in `resource/` subdirectories (`tiles/`, `player/`, `monster/`, `objects/`, `sounds/`, etc.) for the game to run. Versioned resources: maps, translations, bestiary data, fonts, and artwork.
