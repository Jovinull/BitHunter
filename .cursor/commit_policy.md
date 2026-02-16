# Política de Commits — BitHunter

## Formato (Conventional Commits)

```
tipo(escopo): descrição curta em português

[corpo opcional — explica o "porquê"]

[rodapé opcional — breaking changes, issues]
```

## Tipos permitidos

| Tipo | Quando usar | Exemplo |
|---|---|---|
| `feat` | Nova funcionalidade | `feat(pets): adicionar sistema de incubação de ovos` |
| `fix` | Correção de bug | `fix(collision): corrigir detecção de colisão diagonal` |
| `refactor` | Refatoração sem mudar comportamento | `refactor(entity): extrair lógica de combate para CombatComponent` |
| `docs` | Documentação | `docs: atualizar README com instruções de build` |
| `style` | Formatação, espaços, imports | `style(ui): organizar imports e ajustar indentação` |
| `test` | Testes | `test(data): adicionar teste para SaveLoad` |
| `chore` | Manutenção, configs | `chore: atualizar .editorconfig` |
| `perf` | Otimização de performance | `perf(tile): cachear tiles visíveis no render` |

## Escopos comuns

| Escopo | Pacote/Sistema |
|---|---|
| `ai` | Pathfinding (A*) |
| `bestiary` | Sistema de bestiário |
| `data` | Save/Load, persistência |
| `entity` | Entidades base, componentes |
| `environment` | Iluminação, ambiente |
| `main` | Core (GamePanel, Config, etc.) |
| `monster` | Monstros |
| `object` | Itens e objetos do mundo |
| `pets` | Sistema de pets |
| `tile` | Tiles, mapas, minimapa |
| `tile_interactive` | Tiles interativos |
| `ui` | Interface do usuário |
| `i18n` | Traduções |
| `collision` | Sistema de colisão |
| `event` | Eventos e teleportes |
| `sound` | Sistema de áudio |

## Regras

1. **Descrição em português**, concisa (máx. ~72 caracteres na primeira linha)
2. **Escopo obrigatório** quando a mudança é localizada em um pacote
3. **Escopo opcional** quando afeta múltiplos sistemas (ex: `docs: ...`)
4. **Corpo opcional** — usar quando o "porquê" não é óbvio
5. **Uma responsabilidade por commit** — não misturar feat + fix + refactor

## Exemplos completos

```
feat(monster): adicionar MonOrc com IA de patrulha

Novo monstro com comportamento de patrulha entre waypoints
e ataque corpo-a-corpo quando detecta o player.
```

```
fix(event): corrigir teleporte duplicado no dungeon02

O evento de teleporte estava disparando duas vezes por causa
de um eventDone que não era resetado corretamente.
```

```
refactor(ui): separar HudUi do UI principal

Extrair a lógica de desenho do HUD (barras HP/MP, mensagens)
para uma classe dedicada HudUi, reduzindo o tamanho do UI.java.
```

## Fluxo de push

1. O Cursor propõe o(s) commit(s) com mensagens formatadas
2. O Cursor lista em bullets o que mudou
3. O Cursor pergunta: **"Posso subir o commit?"**
4. Só faz push se o usuário autorizar explicitamente
5. Se "não" → tudo fica pronto localmente (staged + committed, sem push)
6. Se "sim" → seguir o fluxo de branch obrigatório (abaixo)

## Fluxo de branch (OBRIGATÓRIO)

Ao receber autorização para subir, o Cursor **DEVE**:

1. **Criar uma branch nova** a partir da `main`
   - Padrão de nome: `tipo/descricao-curta` (ex: `feat/novo-monstro`, `fix/colisao-diagonal`, `docs/atualizar-readme`)
   - Tipos de branch: `feat/`, `fix/`, `refactor/`, `docs/`, `chore/`, `perf/`
2. **Separar as mudanças em commits lógicos**
   - Cada commit agrupa arquivos que fazem sentido juntos
   - Ex: não misturar mudanças de `entity/` com mudanças de `ui/` no mesmo commit, a menos que sejam interdependentes
   - Ordem dos commits: da base/infra para funcionalidade de ponta
3. **Fazer push da branch** para o remote
4. **Fazer merge na main** com `--no-ff` (preserva histórico da branch)
5. **Fazer push da main** para o remote

### Exemplo de fluxo completo

```bash
# 1. Criar branch
git checkout -b feat/novo-item-potion-blue

# 2. Commits separados e lógicos
git add src/object/ObjPotionBlue.java
git commit -m "feat(object): adicionar ObjPotionBlue com restauração de mana"

git add src/main/EntityGenerator.java
git commit -m "feat(main): registrar ObjPotionBlue no EntityGenerator"

git add resource/lang/messages_*.properties
git commit -m "feat(i18n): adicionar chaves de tradução para ObjPotionBlue"

# 3. Push da branch
git push -u origin feat/novo-item-potion-blue

# 4. Merge na main
git checkout main
git merge feat/novo-item-potion-blue --no-ff -m "merge: integrar ObjPotionBlue na main"

# 5. Push da main
git push origin main
```

### Regra crítica
> **NUNCA** commitar direto na main. Toda mudança entra via branch + merge.
