# PROMPT MASTER — BitHunter

> **Como usar:** copie o template abaixo, substitua `[TAREFA]` pela sua necessidade e cole no Cursor.
> O Cursor (Copos) vai seguir o fluxo padrão automaticamente.

---

## Template de Prompt

```markdown
## Tarefa
[TAREFA — descreva o que você quer que seja feito]

## Contexto adicional (opcional)
[Qualquer informação extra: arquivos envolvidos, comportamento esperado, referências]

## Restrições (opcional)
[Ex: "não altere a UI", "mantenha compatibilidade com save.dat", "só toque no pacote X"]
```

---

## O que o Cursor vai fazer automaticamente

Ao receber um prompt seguindo este template, o Cursor (Copos) deve:

### 1. Antes de começar
- [ ] Ler `.cursorrules`
- [ ] Ler `.cursor/workflow.md`, `.cursor/code_style.md`, `.cursor/architecture.md`
- [ ] Entender a arquitetura do projeto
- [ ] Definir o escopo exato da tarefa

### 2. Diagnóstico
- [ ] Listar arquivos que serão lidos/alterados
- [ ] Identificar dependências entre eles
- [ ] Listar riscos (quebra de save, breaking change, performance)
- [ ] Confirmar se o escopo está claro

### 3. Plano de execução
- [ ] Passos numerados e ordenados
- [ ] Cada passo com uma responsabilidade clara
- [ ] Indicação de riscos por passo

### 4. Implementação
- [ ] Seguir o plano passo a passo
- [ ] Respeitar `code_style.md`
- [ ] Não alterar arquivos fora do escopo
- [ ] Não refatorar código não solicitado
- [ ] Corrigir warnings nos arquivos tocados
- [ ] Manter build verde

### 5. Testes
- [ ] Descrever como validar a mudança
- [ ] Listar cenários de teste (o que testar manualmente)
- [ ] Verificar que funcionalidades existentes não quebraram

### 6. Resumo do que mudou
- [ ] Lista em bullets de cada alteração
- [ ] Arquivos criados/modificados

### 7. Commit proposto
- [ ] Formato: `tipo(escopo): descrição` (Conventional Commits)
- [ ] Bullets resumindo as mudanças
- [ ] Seguir `.cursor/commit_policy.md`

### 8. Perguntar antes de subir
- [ ] **"Posso subir o commit?"**
- [ ] Se "não" → deixar tudo pronto localmente
- [ ] Se "sim" → seguir fluxo de branch obrigatório:
  1. Criar branch nova (ex: `feat/descricao`, `fix/descricao`)
  2. Commits separados agrupando arquivos que fazem sentido juntos
  3. Push da branch
  4. Merge na main (`--no-ff`)
  5. Push da main

---

## Exemplos de Prompt

### Exemplo 1 — Correção de warnings

```markdown
## Tarefa
Corrigir todos os warnings de compilação nos arquivos do pacote `ui/`.

## Restrições
- Não alterar comportamento visual
- Não alterar lógica de negócio
- Apenas corrigir warnings (imports não usados, variáveis não utilizadas, tipos raw)
```

### Exemplo 2 — Adicionar feature pequena

```markdown
## Tarefa
Adicionar um novo item "ObjPotionBlue" que restaura 3 de mana ao ser consumido.

## Contexto adicional
- Seguir o padrão de `ObjPotionRed` (que restaura vida)
- Sprite será `/objects/potion_blue.png`
- Preço de venda: 3 moedas
- Adicionar chaves i18n em todos os idiomas

## Restrições
- Não alterar outros itens
- Registrar em EntityGenerator
```

### Exemplo 3 — Refactor controlado

```markdown
## Tarefa
Extrair os números mágicos de tamanho de tela do `GamePanel` para constantes nomeadas.

## Restrições
- Não alterar comportamento
- Não mexer em outros arquivos além de `GamePanel.java`
- Manter os valores exatos atuais
```

### Exemplo 4 — Adicionar novo mapa/evento

```markdown
## Tarefa
Adicionar um novo mapa "dungeon03.txt" (50x50 tiles) com:
- Um evento de teleporte conectando ao dungeon02
- Um healing pool na posição (25, 25)
- 3 MonBat posicionados via AssetSetter

## Contexto adicional
- O mapa já existe em `resource/maps/dungeon03.txt`
- Conectar ao dungeon02 na posição (10, 45)

## Restrições
- Não alterar mapas existentes
- Manter o padrão de coordenadas do EventHandler
```

### Exemplo 5 — Otimização/Performance

```markdown
## Tarefa
Otimizar o `TileManager.draw()` para não iterar sobre todos os tiles do mapa —
desenhar apenas os tiles visíveis na tela (camera culling).

## Contexto adicional
- Atualmente o método já faz culling parcial, mas quero garantir que está eficiente
- O cálculo deve considerar a posição do player e o tamanho da tela

## Restrições
- Não alterar a aparência visual
- Não alterar o formato dos mapas
- Manter compatibilidade com o sistema de debug path (tecla T)
```

---

## Dicas de uso

1. **Seja específico** — quanto mais claro o pedido, melhor o resultado
2. **Use restrições** — elas evitam mudanças indesejadas
3. **Mencione arquivos** — se sabe quais arquivos envolver, liste-os
4. **Contexto ajuda** — referências a classes/métodos existentes aceleram o trabalho
5. **Um pedido por vez** — prompts focados geram resultados melhores
