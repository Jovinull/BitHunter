# Workflow do Cursor (Copos) — BitHunter

## Fluxo obrigatório para qualquer tarefa

### 1. Preparação (ANTES de escrever código)
- [ ] Ler `.cursorrules`
- [ ] Ler os arquivos relevantes em `.cursor/` (pelo menos `architecture.md` e `code_style.md`)
- [ ] Entender o escopo exato da tarefa
- [ ] Identificar quais arquivos serão afetados
- [ ] Listar riscos (quebra de save, mudança de API, etc.)

### 2. Diagnóstico
- Resumir em 2-3 linhas o que precisa ser feito
- Listar arquivos envolvidos
- Identificar dependências entre os arquivos

### 3. Plano de execução
- Passos numerados e ordenados
- Cada passo deve ser atômico (uma responsabilidade)
- Indicar se há riscos em cada passo

### 4. Implementação
- Seguir o plano passo a passo
- Respeitar `code_style.md`
- Nunca alterar arquivos fora do escopo
- Nunca refatorar código que não foi pedido
- Corrigir warnings nos arquivos tocados

### 5. Validação
- Garantir que o projeto compila (ver `runbook.md`)
- Testar manualmente o fluxo afetado (descrever como)
- Verificar que não quebrou funcionalidades existentes

### 6. Entrega
- Resumir em bullets o que mudou
- Propor commit(s) no padrão Conventional Commits (ver `commit_policy.md`)
- Agrupar arquivos em commits que façam sentido juntos
- Perguntar: "Posso subir o commit?"
- Ao receber autorização: criar branch → commits separados → push → merge na main → push main
- **NUNCA** commitar direto na main

---

## Regras de conduta

### O que SEMPRE fazer
- Ler as regras antes de agir
- Trabalhar em passos claros e documentados
- Manter o build verde
- Propor commit e pedir permissão antes de push
- Usar branch separada para toda mudança (nunca commitar direto na main)
- Separar commits em grupos lógicos (arquivos que fazem sentido juntos)
- Fazer merge da branch na main após push
- Responder em Português (pt-BR)
- Seguir o padrão de resposta do Copos (Diagnóstico → Plano → Execução → Testes → Resumo)

### O que NUNCA fazer
- Alterar arquivos fora do escopo pedido
- Fazer push sem autorização
- Refatorar código não solicitado
- Remover documentação ou código existente sem pedir
- Criar arquivos desnecessários
- Inventar funcionalidades não pedidas
- Prometer "depois eu faço" — tudo deve ser entregue no mesmo ciclo
- Usar comandos que não existem no projeto (verificar antes)

### Tratamento de conflitos
- Se uma mudança pedida pode quebrar outra parte do sistema, **avisar antes de implementar**
- Se o escopo crescer, **perguntar ao usuário se quer expandir**
- Se encontrar um bug durante a tarefa, **reportar mas não corrigir** (a menos que esteja no escopo)

---

## Prioridades (em ordem)

1. **Não quebrar** o que já funciona
2. **Fazer o que foi pedido** — nada mais, nada menos
3. **Manter qualidade** — warnings, estilo, documentação
4. **Comunicar** — diagnóstico, plano, resumo claro
