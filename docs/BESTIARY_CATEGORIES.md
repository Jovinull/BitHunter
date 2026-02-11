# Bestiary Categories (Categorias do Bestiário)

## Visão Geral

O Bestiário agrupa criaturas por **classes** (categorias). Cada classe representa
um tipo/família de criaturas. As classes são definidas no enum `bestiary.CreatureClass`
e a atribuição de cada criatura a uma classe é feita no arquivo de dados
`resource/bestiary/creatures.properties`, no campo `<id>.class`.

A **ordem de exibição** das classes na UI é controlada pela propriedade `classes=`
no `creatures.properties`.

---

## Todas as Classes

### 1. SLIMES
- **Chave de tradução:** `bestiary.class.slimes`
- **Nome exibido (en):** Slimes | **(pt-BR):** Slimes
- **Descrição:** Criaturas amorfas gelatinosas. Geralmente fracas,
  mas algumas variantes são mais perigosas.

| Bestiary ID    | Monster Ref            | Nome (en)     | Dificuldade | Raridade | HP  | Localização |
|----------------|------------------------|---------------|-------------|----------|-----|-------------|
| `greenSlime`   | `monster.greenSlime`   | Green Slime   | INOFENSIVO  | NORMAL   | 8   | World       |
| `redSlime`     | `monster.redSlime`     | Red Slime     | TRIVIAL     | NORMAL   | 16  | World       |

### 2. ANIMAIS
- **Chave de tradução:** `bestiary.class.animais`
- **Nome exibido (en):** Animals | **(pt-BR):** Animais
- **Descrição:** Criaturas da fauna selvagem encontradas em cavernas e
  ambientes subterrâneos.

| Bestiary ID | Monster Ref      | Nome (en) | Dificuldade | Raridade | HP  | Localização |
|-------------|------------------|-----------|-------------|----------|-----|-------------|
| `bat`       | `monster.bat`    | Bat       | INOFENSIVO  | NORMAL   | 10  | Dungeon     |

### 3. HUMANOIDES
- **Chave de tradução:** `bestiary.class.humanoides`
- **Nome exibido (en):** Humanoids | **(pt-BR):** Humanoides
- **Descrição:** Criaturas inteligentes com forma humanoide.
  Geralmente mais fortes e com ataques corpo a corpo poderosos.

| Bestiary ID | Monster Ref      | Nome (en) | Dificuldade | Raridade | HP  | Localização |
|-------------|------------------|-----------|-------------|----------|-----|-------------|
| `orc`       | `monster.orc`    | Orc       | FACIL       | NORMAL   | 30  | World       |

### 4. MORTOS_VIVOS
- **Chave de tradução:** `bestiary.class.mortos_vivos`
- **Nome exibido (en):** Undead | **(pt-BR):** Mortos-vivos
- **Descrição:** Criaturas que transcenderam a morte. Inclui bosses.

| Bestiary ID     | Monster Ref              | Nome (en)      | Dificuldade  | Raridade | HP  | Localização |
|-----------------|--------------------------|----------------|--------------|----------|-----|-------------|
| `skeletonLord`  | `monster.skeletonLord`   | Skeleton Lord  | DESAFIADOR   | NORMAL   | 80  | Boss Room   |

---

## Como uma criatura é atribuída a uma classe

1. No arquivo `resource/bestiary/creatures.properties`, cada criatura tem um campo:
   ```
   <bestiaryId>.class=<NOME_DO_ENUM>
   ```
   Exemplo: `greenSlime.class=SLIMES`

2. O valor deve corresponder exatamente a um valor do enum `bestiary.CreatureClass`.

3. As classes disponíveis e sua ordem de exibição são definidas na propriedade:
   ```
   classes=SLIMES,ANIMAIS,HUMANOIDES,MORTOS_VIVOS
   ```

4. Para adicionar uma nova classe:
   - Adicionar o valor no enum `CreatureClass.java` com sua chave de tradução
   - Adicionar o nome na lista `classes=` do `creatures.properties`
   - Adicionar as traduções nos arquivos `messages_*.properties`

---

## Bug NPE Corrigido — Análise e Solução

### O que acontecia

Ao abrir o Bestiário (tecla B), ocorria:
```
NullPointerException: Cannot invoke "bestiary.CreatureClass.getTranslationKey()"
because "this.currentClass" is null
    at ui.BestiaryUi.drawCreatureListScreen(BestiaryUi.java:170)
```

### Causa raiz: Race Condition (TOCTOU)

O jogo possui **duas threads** que acessam o estado do bestiário simultaneamente:

| Thread              | O que faz                                      |
|---------------------|-------------------------------------------------|
| **Game Loop**       | Executa `drawBestiaryScreen()` — lê `subState` e `currentClass` |
| **AWT EDT**         | Executa `KeyHandler.keyPressed()` — modifica `subState` e `currentClass` |

O problema ocorria especificamente no método `goBack()`, que fazia:
```java
// ANTES (ordem incorreta)
currentClass = null;        // ← campo anulado PRIMEIRO
// ... (outras limpezas)
ui.setSubState(0);          // ← subState atualizado DEPOIS
```

Entre essas duas instruções, a thread do Game Loop podia ler `subState = 1`
(ainda não atualizado) e chamar `drawCreatureListScreen()`, mas encontrar
`currentClass = null` (já anulado pela EDT). A guarda `if (currentClass == null) return`
existia, mas sem sincronização/volatile, o compilador podia reordenar leituras ou
a atualização podia não ser visível pela outra thread no momento exato.

### Correção aplicada (3 camadas de proteção)

1. **`resolveCurrentClass()`** — novo método que faz snapshot em variável local e tem
   fallback automático para a primeira classe disponível. Elimina completamente
   o padrão TOCTOU.

2. **Ordem de operações invertida** — `goBack()` agora muda `subState` ANTES de
   qualquer outra alteração. E `currentClass` não é mais anulado (mantido como cache).

3. **Inicialização segura** — `resetState()` agora inicializa `currentClass` com a
   primeira classe disponível em vez de `null`.

4. **Fallback no draw** — `drawBestiaryScreen()` verifica o retorno de
   `resolveCurrentClass()` e faz fallback para a tela de classes se null,
   com log de warning para diagnóstico.

### Resultado
- NPE impossível de ocorrer, mesmo com interleaving adverso entre threads.
- Nenhuma mudança na lógica de combate/loot/engine.

---

## Fluxo de Navegação da UI

```
[PLAY_STATE] --B--> [BESTIARY_STATE, subState=0: Lista de Classes]
                           |
                      ENTER (seleciona classe)
                           v
                     [subState=1: Lista de Criaturas da Classe]
                           |
                      ENTER (seleciona criatura desbloqueada)
                           v
                     [subState=2: Detalhes da Criatura]
                           |
                         ESC (volta)
                           v
                     [subState=1] --ESC--> [subState=0] --ESC/B--> [PLAY_STATE]
```
