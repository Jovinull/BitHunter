# Cheat Codes (Códigos Secretos)

## Visão Geral

O BitHunter possui um sistema de **códigos secretos** (cheats) digitáveis durante o gameplay.
O sistema é gerenciado pela classe `main.CheatCodeManager` e funciona de forma **silenciosa**:
nenhuma indicação visual aparece durante a digitação, e códigos inválidos não produzem feedback.

---

## Como Usar

### Passo a passo

1. Esteja no estado **PLAY** (gameplay normal).
2. Pressione **`'`** (aspas simples) ou **`"`** (aspas duplas) para ativar o modo de captura silencioso.
   - Nada aparece na tela — a captura é invisível.
   - Funciona em qualquer layout de teclado (ABNT2, US-International, etc.).
3. Digite o código desejado (letras, dígitos ou ponto; **case-insensitive**).
   - Durante a digitação, **todas as teclas são consumidas** pelo sistema de captura — WASD, Enter, ESC e outras ações do jogo ficam bloqueadas temporariamente.
4. Pressione **ENTER** para confirmar, ou **ESC** para cancelar.
   - Se nenhuma tecla for pressionada em **4 segundos**, o código é confirmado automaticamente (timeout).
5. Resultado:
   - **Código válido e elegível** → efeito aplicado + mensagem no HUD + SFX de confirmação.
   - **Código ONE_TIME já usado** → mensagem "Este código já foi utilizado!" no HUD.
   - **Código inválido** → silencioso (nada acontece).

### Dicas

- Os códigos são **case-insensitive**: `HotFix`, `hotfix` e `HOTFIX` funcionam igualmente.
- Não é necessário digitar rápido — o timeout de 4 segundos é por inatividade total, não por velocidade de digitação.
- Cheats do tipo **ONE_TIME** são persistidos no save. Mesmo fechando e reabrindo o jogo, eles continuam bloqueados.

---

## Tipos de Repetibilidade

| Tipo | Descrição |
|---|---|
| **ONE_TIME** | Pode ser usado **apenas uma vez** por save. O uso é persistido via `DataStorage` e sobrevive ao ciclo save/load. |
| **REPEATABLE** | Pode ser usado **quantas vezes quiser**, sem restrição. |

---

## Lista de Códigos

### Códigos Originais

| Código | Efeito | Tipo | Referência temática |
|---|---|---|---|
| `trycatch` | Adiciona o **Escudo do Compilador (Java)** ao inventário | ONE_TIME | `try/catch` do Java — mecânica principal do escudo (defesa ativa) |
| `nullpointer` | **Easter egg**: exibe mensagem divertida de NullPointerException | REPEATABLE | `NullPointerException` — erro clássico do Java |

### Códigos de Recursos

| Código | Efeito | Tipo | Referência temática |
|---|---|---|---|
| `overflow` | **+1000 moedas** | REPEATABLE | Stack Overflow — overflow de riqueza na carteira |
| `hotfix` | **Restaura vida e mana** ao máximo | REPEATABLE | Hotfix — correção emergencial implantada em produção |
| `sudo` | **+5 chaves** ao inventário | REPEATABLE | `sudo` (super user do) — acesso root irrestrito |
| `printf` | **+5 poções vermelhas** ao inventário | REPEATABLE | `printf` — output de dados, aqui output de cura |

### Códigos de Utilidade

| Código | Efeito | Tipo | Referência temática |
|---|---|---|---|
| `gitpush` | **Força salvar** o jogo imediatamente | REPEATABLE | `git push --force` — forçar envio do estado atual |
| `finalclass` | **Alterna Modo Deus + NoClip** (god mode on/off) | REPEATABLE | `final class` — classe que não pode ser herdada/sobrescrita/derrotada |
| `refactor` | **Sobe 1 nível** instantaneamente (dispara level up completo) | ONE_TIME | Refatoração — melhoria contínua do código/personagem |
| `summon[monstro]` | **Invocar Monstro** dinamicamente próximo ao jogador (não dá respawn). Ex.: `summonbat`, `summonslime`. | REPEATABLE | Ferramenta de testes para escalar monstros e verificar IA sem procurá-los pelo mapa |

> **Nota sobre `finalclass` (God Mode + NoClip):**
> Quando ativo, o jogador torna-se invencível (não morre) e pode **atravessar paredes, tiles sólidos e entidades** (noclip).
> Interações normais (pickups, NPCs, monstros, eventos) continuam funcionando.
> God Mode é ativado **exclusivamente** via este cheat code — **não existe** atalho de teclado direto.

---

## Detalhes Técnicos

### Persistência (ONE_TIME)

Os códigos ONE_TIME já utilizados são armazenados em `DataStorage.usedCheatCodes` (um `Set<String>`)
e salvos/carregados pelo `SaveLoad` junto com o restante dos dados do jogador.

- **Saves antigos** (criados antes da implementação dos cheats) são tratados de forma segura:
  o campo será `null` na desserialização, e o getter retorna um `HashSet` vazio.
  Nenhum cheat estará bloqueado — comportamento correto.
- O `serialVersionUID` do `DataStorage` permanece inalterado (`1L`), garantindo
  **retrocompatibilidade total** com saves existentes.

### Arquitetura

```
CheatCodeManager (main/)
├── enum Repeatability { ONE_TIME, REPEATABLE }
├── record CheatEntry(Repeatability, Runnable)
├── registeredCodes: Map<String, CheatEntry>      ← códigos registrados
├── usedOneTimeCodes: Set<String>                  ← persistido via DataStorage
├── onCharTyped(char) / onKeyPressed(int)          ← API para KeyHandler
├── tick()                                         ← chamado a cada frame
├── getUsedOneTimeCodes() / setUsedOneTimeCodes()  ← API para SaveLoad
└── confirm()                                      ← valida e executa
```

**Fluxo de decisão no `confirm()`:**

```
código vazio?          → ignora
código não registrado? → silencioso
ONE_TIME já usado?     → mensagem de aviso
válido                 → marca ONE_TIME + executa ação
```

### Arquivos Envolvidos

| Arquivo | Responsabilidade |
|---|---|
| `main/CheatCodeManager.java` | Registro, captura, validação e execução dos cheats |
| `data/DataStorage.java` | Campo `usedCheatCodes` para persistência |
| `data/SaveLoad.java` | `copyCheatCodesToStorage()` / `applyCheatCodes()` |
| `main/KeyHandler.java` | `toggleGodMode()` (chamado pelo cheat `finalclass`) |
| `resource/lang/messages_*.properties` | Chaves `cheat.*` para feedback traduzido (10 idiomas) |

### Chaves de Tradução (i18n)

Todas as mensagens de feedback estão traduzidas em 10 idiomas:

| Chave | Uso |
|---|---|
| `cheat.accepted` | Código aceito (genérico) |
| `cheat.alreadyUsed` | ONE_TIME já utilizado |
| `cheat.nullpointer` | Easter egg NullPointerException |
| `cheat.overflow` | +1000 moedas |
| `cheat.hotfix` | Vida e mana restauradas |
| `cheat.sudo` | +5 chaves |
| `cheat.printf` | +5 poções |
| `cheat.gitpush` | Jogo salvo |
| `cheat.finalclass.on` | God mode ativado |
| `cheat.finalclass.off` | God mode desativado |
| `cheat.refactor` | Level up |

---

## Como Adicionar Novos Cheats

1. No método `registerCodes()` do `CheatCodeManager`, adicione uma nova entrada:

```java
// "nomedocodigo" → Descrição do efeito.
// ONE_TIME ou REPEATABLE: justificativa da escolha.
registeredCodes.put("nomedocodigo",
    new CheatEntry(Repeatability.REPEATABLE, this::metodoDeAcao));
```

2. Implemente o método de ação (seção "Ações dos códigos"):

```java
private void metodoDeAcao() {
    var lang = gp.getLanguageManager();
    // ... lógica do cheat ...
    gp.getGameUi().addMessage(lang.text("cheat.nomedocodigo"));
    gp.playSE(SUCCESS_SFX_ID);
}
```

3. Adicione a chave de tradução em todos os 10 arquivos `messages_*.properties`:

```properties
cheat.nomedocodigo=Mensagem de feedback para o jogador.
```

4. Se o cheat for **ONE_TIME**, nenhuma alteração adicional é necessária — a persistência é automática.
