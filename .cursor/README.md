# .cursor/ — Configuração do Cursor (Copos) para o BitHunter

Esta pasta contém as regras, guias e padrões que o Cursor deve seguir ao trabalhar neste repositório.

## Propósito

Garantir que **toda interação com o Cursor** neste projeto seja:
- **Consistente** — mesmo padrão de resposta, commits e estilo de código
- **Segura** — sem mudanças fora do escopo, sem push sem autorização
- **Repetível** — qualquer pessoa pode usar o `PROMPT_MASTER.md` e obter resultados previsíveis
- **Profissional** — Conventional Commits, código limpo, build verde

## Arquivos nesta pasta

| Arquivo | Descrição |
|---|---|
| `README.md` | Este arquivo — visão geral da pasta |
| `workflow.md` | Regras de trabalho do Cursor dentro do repo |
| `commit_policy.md` | Padrão de commits, exemplos e regras de push |
| `code_style.md` | Convenções de código do projeto |
| `runbook.md` | Como rodar, buildar, testar e empacotar |
| `architecture.md` | Visão geral da arquitetura, sistemas e fluxos |

## Como usar

1. O Cursor lê automaticamente o `.cursorrules` na raiz.
2. O `.cursorrules` referencia os arquivos desta pasta.
3. Para tarefas, use o template em `PROMPT_MASTER.md` (raiz do repo).
4. Para um guia rápido, veja `docs/CURSOR_GUIDE.md`.

## Regra crítica

> O Cursor **DEVE** ler `.cursorrules` e os arquivos de `.cursor/` antes de executar qualquer tarefa.
> Nunca deve fazer push sem autorização explícita do usuário.
