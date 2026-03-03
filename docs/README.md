# docs/ — Documentação do Assistente de IA para o BitHunter

Esta pasta contém as regras, guias e padrões que o assistente de IA deve seguir ao trabalhar neste repositório.

## Propósito

Garantir que **toda interação com o assistente de IA** neste projeto seja:
- **Consistente** — mesmo padrão de resposta, commits e estilo de código
- **Segura** — sem mudanças fora do escopo, sem push sem autorização
- **Repetível** — qualquer pessoa pode usar o `PROMPT_MASTER.md` e obter resultados previsíveis
- **Profissional** — Conventional Commits, código limpo, build verde

## Arquivos nesta pasta

| Arquivo                  | Descrição                                             |
| ------------------------ | ----------------------------------------------------- |
| `README.md`              | Este arquivo — visão geral da pasta                   |
| `workflow.md`            | Regras de trabalho do assistente de IA dentro do repo |
| `commit_policy.md`       | Padrão de commits, exemplos e regras de push          |
| `code_style.md`          | Convenções de código do projeto                       |
| `runbook.md`             | Como rodar, buildar, testar e empacotar               |
| `architecture.md`        | Visão geral da arquitetura, sistemas e fluxos         |
| `AI_GUIDE.md`            | Guia rápido de como trabalhar com a IA neste repo     |
| `gdd.md`                 | Game Design Document                                  |
| `BESTIARY_CATEGORIES.md` | Categorias do bestiário                               |
| `CHEAT_CODES.md`         | Cheat codes do jogo                                   |

## Como usar

1. O assistente de IA lê automaticamente o `.ai-rules.md` na raiz.
2. O `.ai-rules.md` referencia os arquivos desta pasta.
3. Para tarefas, use o template em `PROMPT_MASTER.md` (raiz do repo).
4. Para um guia rápido, veja `docs/AI_GUIDE.md`.

## Regra crítica

> O assistente de IA **DEVE** ler `.ai-rules.md` e os arquivos de `docs/` antes de executar qualquer tarefa.
> Nunca deve fazer push sem autorização explícita do usuário.
