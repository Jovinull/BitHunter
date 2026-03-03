# Guia Rápido — Como trabalhar neste repo com um Assistente de IA

## TL;DR

1. Use o template de `PROMPT_MASTER.md` para pedir tarefas
2. O assistente de IA lê automaticamente `.ai-rules.md` e `docs/*` antes de agir
3. Toda entrega termina com um commit proposto + pergunta de autorização

---

## Estrutura de configuração

```
BitHunter/
├── .ai-rules.md               ← Regras automáticas (lido pelo assistente de IA)
├── PROMPT_MASTER.md            ← Template de prompt para tarefas
└── docs/
    ├── workflow.md             ← Fluxo de trabalho obrigatório
    ├── commit_policy.md        ← Padrão de commits
    ├── code_style.md           ← Convenções de código
    ├── runbook.md              ← Como rodar, buildar, testar
    ├── architecture.md         ← Arquitetura do projeto
    ├── gdd.md                  ← Game Design Document
    ├── BESTIARY_CATEGORIES.md  ← Categorias do bestiário
    ├── CHEAT_CODES.md          ← Cheat codes
    └── AI_GUIDE.md             ← Este arquivo
```

## Como pedir uma tarefa

### Opção 1 — Prompt rápido (informal)
Basta descrever o que quer. O assistente de IA vai seguir as regras de `.ai-rules.md` automaticamente.

> "Adiciona um novo monstro MonSpider no dungeon02"

### Opção 2 — Prompt estruturado (recomendado para tarefas complexas)
Copie o template de `PROMPT_MASTER.md`:

```markdown
## Tarefa
[O que você quer]

## Contexto adicional
[Informações extras]

## Restrições
[O que NÃO deve mudar]
```

## O que esperar do assistente de IA

Toda resposta do assistente segue este padrão:

1. **Diagnóstico** — resumo do que precisa ser feito
2. **Plano** — passos numerados
3. **Execução** — código alterado
4. **Testes** — como validar
5. **Resumo + Commit** — o que mudou + commit proposto
6. **Pergunta** — "Posso subir o commit?"

## Garantias

O assistente **sempre** vai:
- Ler as regras antes de agir
- Trabalhar em passos claros
- Evitar mudanças fora do escopo
- Verificar compilação antes de subir (se `.java` foi alterado)
- Manter o build verde
- Propor commit antes de subir
- Pedir autorização para push
- Criar branch separada (nunca commitar direto na main)
- Separar commits em grupos lógicos
- Fazer merge da branch na main após autorização
- Deletar a branch (local + remote) após merge bem-sucedido

O assistente **nunca** vai:
- Fazer push sem sua autorização
- Commitar direto na main (sempre via branch + merge)
- Refatorar código que você não pediu
- Apagar documentação/código existente
- Inventar funcionalidades não solicitadas

## Referências úteis

| O que você quer saber   | Onde encontrar                |
| ----------------------- | ----------------------------- |
| Arquitetura do jogo     | `docs/architecture.md`        |
| Como rodar o projeto    | `docs/runbook.md`             |
| Padrão de commits       | `docs/commit_policy.md`       |
| Convenções de código    | `docs/code_style.md`          |
| Fluxo de trabalho       | `docs/workflow.md`            |
| Template de prompt      | `PROMPT_MASTER.md`            |
| Design do jogo (GDD)    | `docs/gdd.md`                 |
| Cheat codes             | `docs/CHEAT_CODES.md`         |
| Categorias do bestiário | `docs/BESTIARY_CATEGORIES.md` |

## Dicas

- **Prompts focados** geram resultados melhores que pedidos vagos
- **Mencione restrições** para evitar mudanças indesejadas
- **Uma tarefa por vez** é mais seguro que múltiplas simultâneas
- Se algo parecer errado, diga "pare" — o assistente vai parar e perguntar
- Para tarefas grandes, peça primeiro um **plano** antes da execução
