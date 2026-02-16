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

## Verificação de Query / SonarLint

### Status atual

**NÃO existe** um SonarQube/SonarCloud configurado (connected mode).
O SonarLint roda no **modo standalone** — analisa o código localmente usando regras padrão.

### Como confirmar que o SonarLint está funcionando

1. Abrir qualquer arquivo `.java` no editor
2. Verificar na barra de status inferior se aparece **"SonarLint"** (ícone de raio)
3. Se aparecer, está rodando — os warnings aparecem no painel **Problems** (`Ctrl+Shift+M`)
4. Para forçar uma análise: `Ctrl+Shift+P` → `SonarLint: Analyze current file`
5. Se NÃO aparecer:
   - Verificar se a extensão está instalada: `Ctrl+Shift+X` → buscar "SonarLint"
   - Instalar se necessário (`SonarSource.sonarlint-vscode`)
   - Recarregar o editor (`Ctrl+Shift+P` → `Developer: Reload Window`)

### Como configurar connected mode (SonarQube/SonarCloud)

> Só necessário se a equipe tiver um servidor SonarQube/SonarCloud.

1. Obter um **token pessoal** no painel do SonarQube (My Account → Security → Generate Token)
2. Nas **settings do usuário** (`Ctrl+,` → buscar "sonarlint connected"):
   ```json
   "sonarlint.connectedMode.connections.sonarqube": [
     {
       "connectionId": "bithunter-sonar",
       "serverUrl": "https://SEU-SONARQUBE-AQUI",
       "token": "<token-pessoal>"
     }
   ]
   ```
3. Nas **settings do workspace** (`.vscode/settings.json`), adicionar:
   ```json
   "sonarlint.connectedMode.project": {
     "connectionId": "bithunter-sonar",
     "projectKey": "BitHunter"
   }
   ```
4. Validar: `Ctrl+Shift+P` → `SonarLint: Check connection` (deve retornar sucesso)

> **IMPORTANTE:** Nunca commitar tokens. A config de `connections.sonarqube` fica nas settings do **usuário**, não do workspace.

### Link de validação rápida

| Item | Como verificar |
|---|---|
| SonarLint ativo | Barra de status → ícone "SonarLint" presente |
| Modo standalone | `Ctrl+Shift+P` → `SonarLint: Show SonarLint Output` → deve mostrar "standalone" |
| Connected mode | `Ctrl+Shift+P` → `SonarLint: Check connection` → "Connection successful" |
| Dashboard (se houver) | Acessar URL do SonarQube no navegador → projeto "BitHunter" |

---

## Checklist de qualidade pré-push

### Opção 1 — Via Task do VS Code (RECOMENDADO)

1. `Ctrl+Shift+P` → `Tasks: Run Task` → **"Pre-push Check"**
2. Essa task executa **em sequência**:
   - **Quality: Warnings Check** — compila com `-Xlint:all -Werror` (falha se houver warnings)
   - **Quality: i18n Check** — valida traduções (falha se houver chaves faltando)
3. Ambas devem terminar com `[OK]`

### Opção 2 — Via terminal (PowerShell no Windows)

```powershell
# 1) Compilar com warnings
New-Item -ItemType Directory -Path build/classes -Force -ErrorAction SilentlyContinue | Out-Null
$sources = Get-ChildItem -Path src -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
javac --release 24 -Xlint:all -Werror -d build/classes $sources

# 2) Validar i18n
javac tools/I18nValidator.java -d tools
java -cp tools I18nValidator
```

### Opção 3 — Via terminal (Bash no Linux/macOS)

```bash
# 1) Compilar com warnings
mkdir -p build/classes
javac --release 24 -Xlint:all -Werror -d build/classes $(find src -name "*.java")

# 2) Validar i18n
javac tools/I18nValidator.java -d tools && java -cp tools I18nValidator
```

### O que cada check verifica

| Check | O que faz | Exit code |
|---|---|---|
| Warnings Check | Compila com `-Xlint:all -Werror` — transforma warnings em erros | `0` = OK, `≠0` = warnings encontrados |
| i18n Check | Roda `I18nValidator` — compara chaves entre idiomas | `0` = OK, `2` = inconsistências |

### Problems do VS Code que NÃO aparecem no build

Alguns warnings são exclusivos do editor (Java Language Server / SonarLint):

| Fonte | Exemplos | Como verificar |
|---|---|---|
| Java Language Server (Red Hat) | Variáveis não usadas, imports redundantes, cast desnecessário | Painel Problems (`Ctrl+Shift+M`) |
| SonarLint | Code smells, bugs potenciais, segurança | Painel Problems (prefixo "sonarlint") |
| Checkstyle | Violações de estilo (se configurado) | Painel Problems (prefixo "checkstyle") |
| ErrorLens | Mostra inline os mesmos warnings acima | Highlights na linha do código |

**Antes de push**: abrir `Ctrl+Shift+M` e filtrar pelos arquivos que foram alterados. Corrigir warnings relevantes.

---

## Configurações do VS Code

| Arquivo | Descrição |
|---|---|
| `.vscode/settings.json` | Configurações do projeto (formatter, encoding, Java paths, SonarLint) |
| `.vscode/tasks.json` | Tasks: Build, Clean, Quality Checks, Pre-push Check |
| `.vscode/launch.json` | Configuração de debug/execução ("Run BitHunter") |
| `.vscode/extensions.json` | Extensões recomendadas para o projeto |
| `.vscode/eclipse-formatter.xml` | Regras do Eclipse formatter para Java |

### Extensões recomendadas (instalação automática)

Ao abrir o projeto pela primeira vez, o VS Code oferece instalar as extensões listadas em `.vscode/extensions.json`. As mais importantes para qualidade:

| Extensão | Função |
|---|---|
| `redhat.java` | Java Language Server (warnings, autocomplete, refactor) |
| `SonarSource.sonarlint-vscode` | Análise estática (bugs, smells, segurança) |
| `shengchen.vscode-checkstyle` | Verificação de estilo Java |
| `usernamehw.errorlens` | Mostra erros/warnings inline no código |

---

## Troubleshooting rápido

| Problema | Causa provável | Solução |
|---|---|---|
| Jogo não abre / crash | Assets faltando | Adicionar sprites/sons/fontes em `resource/` |
| "Unresolved compilation" no editor | Cache JDT desatualizado | `Ctrl+Shift+P` → "Java: Clean Java Language Server Workspace" |
| Tela cheia só aplica após restart | Comportamento esperado | `setUndecorated(true)` é definido no boot |
| SonarLint não aparece na barra | Extensão não instalada | `Ctrl+Shift+X` → instalar "SonarLint" → reload |
| SonarLint não analisa arquivos | JDK não detectado | Verificar que `java` está no PATH e JDK ≥17 |
| Task "Pre-push Check" falha | Warnings de javac | Corrigir warnings listados na saída do terminal |
| Task "Build" não encontra arquivos | Diretório `src/` incorreto | Verificar que está na raiz do workspace |
