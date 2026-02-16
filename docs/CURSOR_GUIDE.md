# Guia Rápido — Como trabalhar neste repo com o Cursor

## TL;DR

1. Use o template de `PROMPT_MASTER.md` para pedir tarefas
2. O Cursor lê automaticamente `.cursorrules` e `.cursor/*` antes de agir
3. Toda entrega termina com um commit proposto + pergunta de autorização

---

## Estrutura de configuração

```
BitHunter/
├── .cursorrules              ← Regras automáticas (lido pelo Cursor)
├── .cursor/
│   ├── README.md             ← Visão geral da pasta de config
│   ├── workflow.md           ← Fluxo de trabalho obrigatório
│   ├── commit_policy.md      ← Padrão de commits
│   ├── code_style.md         ← Convenções de código
│   ├── runbook.md            ← Como rodar, buildar, testar
│   └── architecture.md       ← Arquitetura do projeto
├── PROMPT_MASTER.md          ← Template de prompt para tarefas
└── docs/
    └── CURSOR_GUIDE.md       ← Este arquivo
```

## Como pedir uma tarefa

### Opção 1 — Prompt rápido (informal)
Basta descrever o que quer. O Cursor vai seguir as regras de `.cursorrules` automaticamente.

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

## O que esperar do Cursor

Toda resposta do Cursor (Copos) segue este padrão:

1. **Diagnóstico** — resumo do que precisa ser feito
2. **Plano** — passos numerados
3. **Execução** — código alterado
4. **Testes** — como validar
5. **Resumo + Commit** — o que mudou + commit proposto
6. **Pergunta** — "Posso subir o commit?"

## Garantias

O Cursor **sempre** vai:
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

O Cursor **nunca** vai:
- Fazer push sem sua autorização
- Commitar direto na main (sempre via branch + merge)
- Refatorar código que você não pediu
- Apagar documentação/código existente
- Inventar funcionalidades não solicitadas

## Referências úteis

| O que você quer saber | Onde encontrar |
|---|---|
| Arquitetura do jogo | `.cursor/architecture.md` |
| Como rodar o projeto | `.cursor/runbook.md` |
| Padrão de commits | `.cursor/commit_policy.md` |
| Convenções de código | `.cursor/code_style.md` |
| Fluxo de trabalho | `.cursor/workflow.md` |
| Template de prompt | `PROMPT_MASTER.md` |
| Design do jogo (GDD) | `docs/gdd.md` |
| Cheat codes | `docs/CHEAT_CODES.md` |
| Categorias do bestiário | `docs/BESTIARY_CATEGORIES.md` |

## Dicas

- **Prompts focados** geram resultados melhores que pedidos vagos
- **Mencione restrições** para evitar mudanças indesejadas
- **Uma tarefa por vez** é mais seguro que múltiplas simultâneas
- Se algo parecer errado, diga "pare" — o Cursor vai parar e perguntar
- Para tarefas grandes, peça primeiro um **plano** antes da execução
