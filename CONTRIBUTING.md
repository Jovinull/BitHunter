# Contribuindo para o BitHunter

Obrigado pelo interesse em contribuir! Pull requests e issues são bem-vindos.

Antes de contribuir, leia este documento por completo — ele protege você e o projeto.

---

## Regras gerais

1. **Abra uma Issue antes de grandes mudanças.** Para correções pequenas (typos, bugs óbvios), pode ir direto ao PR.
2. **PRs focados.** Alterações pequenas e coerentes por subsistema (`ui/`, `entity/`, `tile/`, `data/`…) são mais fáceis de revisar.
3. **Compatibilidade com saves.** Mudanças em `internalId` ou em `data.DataStorage` afetam `save.dat`. Não renomeie IDs estáveis sem estratégia de migração.
4. **i18n.** Sempre que adicionar textos, inclua chaves em `messages_pt_BR.properties` e (quando possível) nos demais idiomas. Rode o validador (`tools/I18nValidator.java`) para verificar.
5. **Build verde.** Garanta que o projeto compila sem erros (`javac`) antes de submeter o PR.

---

## Termos de contribuição (importante!)

> **Resumo**: ao enviar um PR, você declara que tem direito sobre o que enviou e concorda que a contribuição será incorporada sob a mesma licença proprietária do projeto (All Rights Reserved — Felipe Jovino).

### Ao submeter uma contribuição (pull request, patch, issue com código), você declara que:

- Você é o **autor original** da contribuição, ou possui direitos suficientes para submetê-la.
- A contribuição **não infringe** direitos de propriedade intelectual de terceiros.
- Você **não copiou** código, assets ou conteúdo de outros projetos sem permissão compatível com a política deste repositório.

### Ao submeter uma contribuição, você concorda que:

- A contribuição poderá ser **incorporada ao projeto** sob a mesma licença descrita no arquivo `LICENSE` (proprietária, All Rights Reserved, copyright de Felipe Jovino).
- Você concede a Felipe Jovino uma licença **perpétua, mundial, não exclusiva, irrevogável e livre de royalties** para usar, modificar, distribuir e sublicenciar a contribuição como parte deste projeto.
- Você **renuncia a qualquer reivindicação** de compensação adicional ou atribuição além do que é costumeiro (ex.: histórico de commits do Git).

### Contribuições devem ser:

- **Originais** (criadas por você), ou
- **Compatíveis** com a política do projeto (não extraídas de projetos com licenças incompatíveis como GPL, ou de repositórios cujo código não pode ser relicenciado).

Se não tiver certeza sobre a compatibilidade de algo que quer incluir, pergunte antes abrindo uma Issue.

---

## Developer Certificate of Origin (DCO) — alternativa opcional

Se o projeto adotar o DCO no futuro, contribuidores assinarão cada commit com `Signed-off-by`, atestando que têm direito de submeter a contribuição. Isso é um mecanismo leve e amplamente usado (Linux kernel, GitLab, etc.).

Por enquanto, a submissão do PR já implica concordância com os termos acima. Se o DCO for ativado, este documento será atualizado com instruções.

---

## Como contribuir (passo a passo)

1. **Fork** este repositório (apenas para preparar o PR — não publique o fork como projeto próprio).
2. Crie uma **branch** descritiva: `feat/novo-monstro`, `fix/colisao-diagonal`, etc.
3. Faça suas alterações seguindo as [convenções do projeto](#convenções).
4. Garanta que o projeto **compila sem erros**.
5. Envie o **pull request** para este repositório.

---

## Convenções

- **Commits**: Conventional Commits — `tipo(escopo): descrição` (ex.: `feat(entity): adicionar novo monstro`).
- **Classes**: PascalCase. **Métodos/variáveis**: camelCase. **Constantes**: UPPER_SNAKE_CASE.
- **Prefixos de classe**: `Mon` (monstros), `Obj` (objetos), `Npc` (NPCs), `It` (tiles interativos).
- **Indentação**: 2 espaços, UTF-8, line endings LF (conforme `.editorconfig`).

---

## O que NÃO fazer

- **Não publique seu fork como projeto independente.** Forks existem apenas para preparar PRs de volta para este repositório.
- **Não copie código/assets de outros projetos** sem verificar compatibilidade e sem informar na Issue/PR.
- **Não redistribua** builds, binários ou partes do projeto fora deste repositório.

---

## Licença

Este projeto é **source-available / proprietário** (All Rights Reserved).
Consulte o arquivo [`LICENSE`](LICENSE) para os termos completos.

Código, assets, sons, imagens, fontes, mapas e textos são todos protegidos sob a mesma licença e **não podem ser reutilizados fora deste repositório**.
