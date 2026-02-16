# Runbook — BitHunter

## Pré-requisitos

- **JDK** instalado (Java 24+ recomendado, conforme `--release 24` no build)
- `java` e `javac` disponíveis no PATH
- Assets gráficos/sonoros em `resource/` (ver seção "Assets")

## Rodar pelo VS Code / Cursor

1. Abrir o workspace no editor
2. Garantir que o JDK está configurado
3. Usar a configuração de launch **"Run BitHunter"** (`.vscode/launch.json`)
4. Main class: `main.Main`
5. Source paths: `src` e `resource` (já configurados em `.vscode/settings.json`)

## Build manual (terminal)

### Linux / macOS
```bash
rm -rf build && mkdir -p build/classes

# 1) Compilar
javac --release 24 -d build/classes $(find src -name "*.java")

# 2) Copiar resources para classpath
cp -r resource/* build/classes/

# 3) Empacotar JAR
jar --create --file build/app.jar --main-class main.Main -C build/classes .

# 4) Rodar
java -jar build/app.jar
```

### Windows (PowerShell)
```powershell
Remove-Item -Recurse -Force build -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path build\classes -Force

# 1) Compilar
$sources = Get-ChildItem -Path src -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
javac --release 24 -d build/classes $sources

# 2) Copiar resources
Copy-Item -Recurse -Force resource\* build\classes\

# 3) Empacotar JAR
jar --create --file build/app.jar --main-class main.Main -C build/classes .

# 4) Rodar
java -jar build/app.jar
```

## Clean build (VS Code task)

A task **"Clean build"** em `.vscode/tasks.json` remove a pasta `bin/`:
```bash
rm -rf bin && echo 'limpo.'
```
> Nota: funciona apenas em shells Unix. No Windows, usar `Remove-Item -Recurse -Force bin`.

## Validar traduções (i18n)

```bash
javac tools/I18nValidator.java -d tools
java -cp tools I18nValidator
```

**Exit codes:**
- `0` — todos os idiomas completos
- `1` — erro de configuração
- `2` — inconsistências detectadas

## Assets

O código referencia recursos via classpath que devem existir em `resource/`:

| Path no código | Pasta em resource/ | Tipo |
|---|---|---|
| `/tiles/` | `resource/tiles/` | Sprites de tiles (.png) |
| `/tiles_interactive/` | `resource/tiles_interactive/` | Sprites interativos (.png) |
| `/objects/` | `resource/objects/` | Sprites de itens (.png) |
| `/player/` | `resource/player/` | Sprites do jogador (.png) |
| `/npc/` | `resource/npc/` | Sprites de NPCs (.png) |
| `/monster/` | `resource/monster/` | Sprites de monstros (.png) |
| `/projectile/` | `resource/projectile/` | Sprites de projéteis (.png) |
| `/pets/` | `resource/pets/` | Sprites de pets (.png) |
| `/eggs/` | `resource/eggs/` | Sprites de ovos (.png) |
| `/sounds/` | `resource/sounds/` | Efeitos sonoros (.wav) |
| `/font/` | `resource/font/` | Fontes (.ttf) |

> **Importante:** os assets binários NÃO estão versionados no repo. O jogo pode não rodar sem eles.

## Arquivos de configuração do jogo

| Arquivo | Descrição | Versionado? |
|---|---|---|
| `config.txt` | Preferências (fullscreen, volumes, idioma) | Sim |
| `save.dat` | Save do jogador (serialização Java) | Não (`.gitignore`) |

### Formato do config.txt
```
Linha 1: Fullscreen (On/Off)
Linha 2: Volume música (0-5)
Linha 3: Volume efeitos (0-5)
Linha 4: Idioma (ex: pt_BR, en_US)
```

## Troubleshooting rápido

| Problema | Causa provável | Solução |
|---|---|---|
| Jogo não abre / crash | Assets faltando | Adicionar sprites/sons/fontes em `resource/` |
| "Unresolved compilation" no editor | Cache JDT desatualizado | `Ctrl+Shift+P` → "Java: Clean Java Language Server Workspace" |
| Task "Clean build" falha no Windows | Comando Unix (`rm -rf`) | Usar `Remove-Item` no PowerShell |
| Tela cheia só aplica após restart | Comportamento esperado | `setUndecorated(true)` é definido no boot |
