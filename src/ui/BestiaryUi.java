package ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Logger;

import bestiary.BestiaryCreature;
import bestiary.BestiaryDrop;
import bestiary.BestiaryLocation;
import bestiary.BestiaryProgress;
import bestiary.BestiaryService;
import bestiary.BestiaryStage;
import bestiary.CreatureClass;
import main.GamePanel;

/**
 * UI do Bestiário — estilo Tibia (Cyclopedia > Bestiary).
 *
 * <p>Sub-estados (subState):</p>
 * <ul>
 *   <li>0 — Lista de Classes</li>
 *   <li>1 — Lista de Criaturas de uma Classe</li>
 *   <li>2 — Detalhes de uma Criatura</li>
 * </ul>
 *
 * <p>Navegação reutiliza commandNum (W/S para navegar, ENTER para entrar, ESC para voltar).
 * Campo {@code searchQuery} para pesquisa rápida.</p>
 */
public class BestiaryUi {

  private static final Logger LOG = Logger.getLogger(BestiaryUi.class.getName());

  // --- Cores específicas do Bestiário ---
  private static final Color COL_LOCKED = new Color(40, 40, 50, 200);
  private static final Color COL_SILHOUETTE = new Color(15, 15, 25);
  private static final Color COL_STAGE_LOCKED = new Color(80, 80, 100);
  private static final Color COL_STAGE_DONE = new Color(80, 200, 120);
  private static final Color COL_PROGRESS_BG = new Color(30, 35, 55);
  private static final Color COL_PROGRESS_FILL = UI.COL_ACCENT;
  private static final Color COL_DIFFICULTY = new Color(255, 180, 50);
  private static final Color COL_SEARCH_BG = new Color(15, 20, 35, 220);

  // --- Layout ---
  private static final int FRAME_PADDING = 20;
  private static final int LINE_H = 24;
  private static final int ITEM_H = 52;
  private static final int MAX_VISIBLE_ITEMS = 8;

  private final UI ui;

  // --- Estado de navegação ---
  private int selectedClassIndex = 0;
  private int selectedCreatureIndex = 0;
  private int scrollOffset = 0;
  private int detailScrollOffset = 0;

  // --- Pesquisa ---
  private String searchQuery = "";
  private boolean searchMode = false;

  // --- Cache da classe selecionada ---
  private CreatureClass currentClass = null;

  BestiaryUi(UI ui) {
    this.ui = ui;
  }

  // ===== Entrypoint principal =====

  void drawBestiaryScreen() {
    int subState = ui.getSubState();

    switch (subState) {
      case 0 -> drawClassListScreen();
      case 1 -> {
        // Captura em variável local para eliminar race condition (TOCTOU)
        // entre a thread do game loop (draw) e a EDT (KeyHandler).
        CreatureClass safeClass = resolveCurrentClass();
        if (safeClass == null) {
          // Fallback: sem classes registradas — voltar para tela de classes
          LOG.warning("[Bestiary] currentClass null in subState=1, falling back to class list");
          ui.setSubState(0);
          drawClassListScreen();
        } else {
          drawCreatureListScreen(safeClass);
        }
      }
      case 2 -> {
        CreatureClass safeClass = resolveCurrentClass();
        if (safeClass == null) {
          LOG.warning("[Bestiary] currentClass null in subState=2, falling back to class list");
          ui.setSubState(0);
          drawClassListScreen();
        } else {
          drawCreatureDetailScreen(safeClass);
        }
      }
      default -> drawClassListScreen();
    }
  }

  /**
   * Resolve o currentClass de forma segura:
   * 1. Se currentClass já tem valor, retorna-o.
   * 2. Se é null, tenta usar a primeira classe disponível como fallback.
   * 3. Se não há classes no registro, retorna null (cenário extremo).
   *
   * <p>Essa abordagem garante que nunca haverá NPE mesmo com race conditions
   * entre a thread de draw e a EDT (KeyHandler).</p>
   */
  private CreatureClass resolveCurrentClass() {
    CreatureClass cc = currentClass; // snapshot em variável local
    if (cc != null) {
      return cc;
    }

    // Fallback: selecionar primeira classe disponível
    List<CreatureClass> classes = ui.getGamePanel().getBestiaryService()
        .getRegistry().getOrderedClasses();
    if (!classes.isEmpty()) {
      cc = classes.get(0);
      currentClass = cc; // propagar o fallback para evitar repetir
      LOG.fine("[Bestiary] currentClass was null, defaulted to: " + cc.name());
    }
    return cc;
  }

  // ===== Tela 0: Lista de Classes =====

  private void drawClassListScreen() {
    Graphics2D g2 = ui.g2;
    var gp = ui.getGamePanel();
    BestiaryService service = gp.getBestiaryService();

    // Fundo
    int frameX = GamePanel.TILE_SIZE;
    int frameY = GamePanel.TILE_SIZE / 2;
    int frameW = GamePanel.SCREEN_WIDTH - GamePanel.TILE_SIZE * 2;
    int frameH = GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE;
    ui.drawSubWindow(frameX, frameY, frameW, frameH);

    // Título
    int textX = frameX + FRAME_PADDING;
    int textY = frameY + FRAME_PADDING + 10;

    g2.setFont(ui.fonts.pixelBold(28F));
    ui.drawTextShadow(ui.t("bestiary.title"), textX, textY, UI.COL_TEXT, Color.black, 2);

    // Pontos do Bestiário
    String pointsText = ui.t("bestiary.points") + ": " + service.getBestiaryPoints();
    g2.setFont(ui.fonts.pixelPlain(18F));
    g2.setColor(UI.COL_TEXT_DIM);
    int pointsX = frameX + frameW - FRAME_PADDING
        - g2.getFontMetrics().stringWidth(pointsText);
    g2.drawString(pointsText, pointsX, textY);

    textY += 36;

    // Lista de classes
    List<CreatureClass> classes = service.getRegistry().getOrderedClasses();
    g2.setFont(ui.fonts.pixelPlain(22F));

    for (int i = 0; i < classes.size(); i++) {
      CreatureClass cc = classes.get(i);
      boolean selected = (i == ui.getCommandNum());

      int itemY = textY + i * ITEM_H;

      // Fundo de seleção
      if (selected) {
        g2.setColor(UI.COL_HIGHLIGHT_BG);
        g2.fillRoundRect(textX - 4, itemY - 4, frameW - FRAME_PADDING * 2 + 8, ITEM_H - 4, 8, 8);
      }

      // Nome da classe
      g2.setFont(ui.fonts.pixelBold(22F));
      g2.setColor(selected ? UI.COL_ACCENT : UI.COL_TEXT);
      g2.drawString(ui.t(cc.getTranslationKey()), textX + 8, itemY + 22);

      // Contador
      int total = service.getRegistry().getCreaturesByClass(cc).size();
      int known = service.getKnownCountForClass(cc);
      int completed = service.getCompletedCountForClass(cc);

      g2.setFont(ui.fonts.pixelPlain(18F));
      g2.setColor(UI.COL_TEXT_DIM);
      String countText = known + "/" + total
          + "  (" + ui.t("bestiary.completed") + ": " + completed + ")";
      int countX = frameX + frameW - FRAME_PADDING - g2.getFontMetrics().stringWidth(countText) - 8;
      g2.drawString(countText, countX, itemY + 22);
    }

    // Hint
    drawHint(g2, frameX, frameY + frameH - 30, frameW,
        ui.t("bestiary.hint.classNav"));
  }

  // ===== Tela 1: Lista de Criaturas =====

  /**
   * Desenha a lista de criaturas de uma classe.
   * Recebe a classe já resolvida (garantidamente não-null) via parâmetro
   * para evitar race conditions com o campo de instância.
   */
  private void drawCreatureListScreen(CreatureClass safeClass) {
    Graphics2D g2 = ui.g2;
    var gp = ui.getGamePanel();
    BestiaryService service = gp.getBestiaryService();

    int frameX = GamePanel.TILE_SIZE;
    int frameY = GamePanel.TILE_SIZE / 2;
    int frameW = GamePanel.SCREEN_WIDTH - GamePanel.TILE_SIZE * 2;
    int frameH = GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE;
    ui.drawSubWindow(frameX, frameY, frameW, frameH);

    int textX = frameX + FRAME_PADDING;
    int textY = frameY + FRAME_PADDING + 10;

    // Título: nome da classe
    g2.setFont(ui.fonts.pixelBold(26F));
    ui.drawTextShadow(ui.t(safeClass.getTranslationKey()),
        textX, textY, UI.COL_TEXT, Color.black, 2);

    textY += 36;

    // Pesquisa
    if (searchMode) {
      g2.setColor(COL_SEARCH_BG);
      g2.fillRoundRect(textX, textY - 16, frameW - FRAME_PADDING * 2, 24, 6, 6);
      g2.setFont(ui.fonts.pixelPlain(18F));
      g2.setColor(UI.COL_ACCENT);
      g2.drawString("> " + searchQuery + "_", textX + 6, textY);
      textY += 28;
    }

    // Lista de criaturas (usa safeClass para thread-safety)
    List<BestiaryCreature> creatures = getFilteredCreatures(service, safeClass);
    int visibleCount = Math.min(
        Math.max(0, creatures.size() - scrollOffset), MAX_VISIBLE_ITEMS);

    g2.setFont(ui.fonts.pixelPlain(20F));

    for (int i = 0; i < visibleCount; i++) {
      int idx = scrollOffset + i;
      if (idx >= creatures.size()) break; // guarda defensiva extra
      BestiaryCreature creature = creatures.get(idx);
      BestiaryStage stage = service.getStage(creature.getId());
      boolean selected = (idx == selectedCreatureIndex);

      int itemY = textY + i * ITEM_H;

      // Fundo de seleção
      if (selected) {
        g2.setColor(UI.COL_HIGHLIGHT_BG);
        g2.fillRoundRect(textX - 4, itemY - 8, frameW - FRAME_PADDING * 2 + 8, ITEM_H - 4, 8, 8);
      }

      // Sprite ou silhueta
      int spriteX = textX + 4;
      int spriteY = itemY - 4;
      int spriteSize = 40;

      if (stage == BestiaryStage.LOCKED) {
        // Silhueta
        g2.setColor(COL_LOCKED);
        g2.fillRoundRect(spriteX, spriteY, spriteSize, spriteSize, 6, 6);
        g2.setColor(COL_SILHOUETTE);
        g2.fillOval(spriteX + 8, spriteY + 8, spriteSize - 16, spriteSize - 16);
      } else {
        // Sprite real
        BufferedImage sprite = service.getCreatureSprite(creature);
        if (sprite != null) {
          g2.drawImage(sprite, spriteX, spriteY, spriteSize, spriteSize, null);
        } else {
          g2.setColor(COL_LOCKED);
          g2.fillRoundRect(spriteX, spriteY, spriteSize, spriteSize, 6, 6);
        }
      }

      // Nome
      int nameX = spriteX + spriteSize + 12;
      g2.setFont(ui.fonts.pixelBold(20F));

      if (stage == BestiaryStage.LOCKED) {
        g2.setColor(COL_STAGE_LOCKED);
        g2.drawString("???", nameX, itemY + 14);
      } else {
        g2.setColor(selected ? UI.COL_ACCENT : UI.COL_TEXT);
        g2.drawString(service.getCreatureDisplayName(creature), nameX, itemY + 14);
      }

      // Estágio / Barra de progresso (compacta)
      if (stage != BestiaryStage.LOCKED) {
        drawMiniProgressBar(g2, nameX, itemY + 22, 120, 10, creature, service);
      }

      // Indicador de estágio
      String stageLabel = getStageLabel(stage);
      g2.setFont(ui.fonts.pixelPlain(16F));
      g2.setColor(stage == BestiaryStage.COMPLETED ? COL_STAGE_DONE : UI.COL_TEXT_DIM);
      int stageLabelX = frameX + frameW - FRAME_PADDING
          - g2.getFontMetrics().stringWidth(stageLabel) - 8;
      g2.drawString(stageLabel, stageLabelX, itemY + 14);
    }

    // Scroll indicator
    if (creatures.size() > MAX_VISIBLE_ITEMS) {
      g2.setFont(ui.fonts.pixelPlain(16F));
      g2.setColor(UI.COL_TEXT_DIM);
      String scrollText = (scrollOffset + 1) + "-"
          + Math.min(scrollOffset + MAX_VISIBLE_ITEMS, creatures.size())
          + " / " + creatures.size();
      g2.drawString(scrollText, frameX + frameW - FRAME_PADDING - 80, frameY + frameH - 40);
    }

    // Hint
    drawHint(g2, frameX, frameY + frameH - 30, frameW,
        ui.t("bestiary.hint.creatureNav"));
  }

  // ===== Tela 2: Detalhes da Criatura =====

  /**
   * Desenha os detalhes de uma criatura.
   * Recebe a classe já resolvida (garantidamente não-null) via parâmetro.
   */
  private void drawCreatureDetailScreen(CreatureClass safeClass) {
    Graphics2D g2 = ui.g2;
    var gp = ui.getGamePanel();
    BestiaryService service = gp.getBestiaryService();

    List<BestiaryCreature> creatures = getFilteredCreatures(service, safeClass);
    if (selectedCreatureIndex < 0 || selectedCreatureIndex >= creatures.size()) return;

    BestiaryCreature creature = creatures.get(selectedCreatureIndex);
    BestiaryProgress progress = service.getProgress(creature.getId());
    BestiaryStage stage = progress.getStage(creature.getDifficulty(), creature.getRarity());

    int frameX = GamePanel.TILE_SIZE / 2;
    int frameY = GamePanel.TILE_SIZE / 4;
    int frameW = GamePanel.SCREEN_WIDTH - GamePanel.TILE_SIZE;
    int frameH = GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE / 2;
    ui.drawSubWindow(frameX, frameY, frameW, frameH);

    int textX = frameX + FRAME_PADDING;
    int textY = frameY + FRAME_PADDING + 8;

    // === Cabeçalho: Sprite + Nome + Dificuldade + Raridade ===
    int headerSpriteSize = GamePanel.TILE_SIZE + 16;
    BufferedImage sprite = service.getCreatureSprite(creature);
    if (sprite != null) {
      // Fundo do sprite
      g2.setColor(new Color(10, 10, 15, 190));
      g2.fillRoundRect(textX, textY, headerSpriteSize, headerSpriteSize, 10, 10);
      g2.drawImage(sprite, textX + 8, textY + 8,
          headerSpriteSize - 16, headerSpriteSize - 16, null);
    }

    int infoX = textX + headerSpriteSize + 16;
    int infoY = textY + 8;

    // Nome
    g2.setFont(ui.fonts.pixelBold(26F));
    g2.setColor(UI.COL_TEXT);
    g2.drawString(service.getCreatureDisplayName(creature), infoX, infoY + 14);

    // Dificuldade + Raridade
    infoY += 28;
    g2.setFont(ui.fonts.pixelPlain(18F));
    g2.setColor(COL_DIFFICULTY);
    String diffText = ui.t("bestiary.difficulty") + ": "
        + ui.t("bestiary.diff." + creature.getDifficulty().name());
    g2.drawString(diffText, infoX, infoY);

    if (creature.getRarity() != bestiary.BestiaryRarity.NORMAL) {
      infoY += 20;
      g2.setColor(new Color(200, 80, 255));
      g2.drawString(ui.t("bestiary.rarity." + creature.getRarity().name()), infoX, infoY);
    }

    // Barra de progresso
    infoY += 24;
    drawProgressBar(g2, infoX, infoY, 260, 14, creature, progress, service);

    // === Blocos de informação por estágio ===
    int blockY = textY + headerSpriteSize + 20;
    int blockX = textX;
    int blockW = frameW - FRAME_PADDING * 2;

    // Scroll para detalhes
    blockY -= detailScrollOffset * LINE_H;

    // Estágio 1: Stats básicos + drops comuns
    blockY = drawStageBlock(g2, blockX, blockY, blockW,
        ui.t("bestiary.stage1.title"),
        stage.ordinal() >= BestiaryStage.STAGE_1.ordinal(),
        () -> drawStage1Content(g2, blockX + 8, blockW - 16, creature, progress, service));

    // Estágio 2: Drops incomuns + Resistências + Localizações
    blockY = drawStageBlock(g2, blockX, blockY, blockW,
        ui.t("bestiary.stage2.title"),
        stage.ordinal() >= BestiaryStage.STAGE_2.ordinal(),
        () -> drawStage2Content(g2, blockX + 8, blockW - 16, creature, progress, service));

    // Estágio Final: Drops raros + Pontos
    drawStageBlock(g2, blockX, blockY, blockW,
        ui.t("bestiary.stageFinal.title"),
        stage == BestiaryStage.COMPLETED,
        () -> drawStageFinalContent(g2, blockX + 8, blockW - 16, creature, progress, service));

    // Hint
    drawHint(g2, frameX, frameY + frameH - 26, frameW,
        ui.t("bestiary.hint.detailNav"));
  }

  // ===== Conteúdo dos estágios =====

  private int currentContentY;

  private int drawStage1Content(Graphics2D g2, int x, int w,
      BestiaryCreature creature, BestiaryProgress progress,
      BestiaryService service) {
    int y = currentContentY;

    g2.setFont(ui.fonts.pixelPlain(18F));

    // HP
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString("HP: ", x, y);
    g2.setColor(UI.COL_TEXT);
    g2.drawString(String.valueOf(creature.getHp()), x + 40, y);
    y += LINE_H;

    // Velocidade
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t("bestiary.detail.speed") + ": ", x, y);
    g2.setColor(UI.COL_TEXT);
    g2.drawString(String.valueOf(creature.getSpeed()), x + 120, y);
    y += LINE_H;

    // Armadura
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t("bestiary.detail.armor") + ": ", x, y);
    g2.setColor(UI.COL_TEXT);
    g2.drawString(String.valueOf(creature.getArmor()), x + 120, y);
    y += LINE_H;

    // Comportamento
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t("bestiary.detail.behavior") + ": ", x, y);
    g2.setColor(UI.COL_TEXT);
    String behaviorText = ui.t("bestiary.behavior." + creature.getBehavior());
    if (creature.isUsesSpells()) {
      behaviorText += " + " + ui.t("bestiary.detail.spells");
    }
    g2.drawString(behaviorText, x + 140, y);
    y += LINE_H + 4;

    // Drops comuns (COMMON)
    g2.setFont(ui.fonts.pixelBold(18F));
    g2.setColor(UI.COL_ACCENT);
    g2.drawString(ui.t("bestiary.detail.commonDrops") + ":", x, y);
    y += LINE_H;

    g2.setFont(ui.fonts.pixelPlain(16F));
    List<BestiaryDrop> visibleDrops = service.getVisibleDrops(creature, progress);
    boolean hasCommon = false;
    for (BestiaryDrop drop : visibleDrops) {
      if (drop.tier() == bestiary.LootTier.COMMON) {
        g2.setColor(UI.COL_TEXT);
        g2.drawString("  - " + getDropDisplayName(drop), x, y);
        y += LINE_H - 4;
        hasCommon = true;
      }
    }
    if (!hasCommon) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString("  " + ui.t("bestiary.detail.noDrops"), x, y);
      y += LINE_H - 4;
    }

    return y;
  }

  private int drawStage2Content(Graphics2D g2, int x, int w,
      BestiaryCreature creature, BestiaryProgress progress,
      BestiaryService service) {
    int y = currentContentY;

    // Drops incomuns
    g2.setFont(ui.fonts.pixelBold(18F));
    g2.setColor(UI.COL_ACCENT);
    g2.drawString(ui.t("bestiary.detail.uncommonDrops") + ":", x, y);
    y += LINE_H;

    g2.setFont(ui.fonts.pixelPlain(16F));
    List<BestiaryDrop> visibleDrops = service.getVisibleDrops(creature, progress);
    boolean hasUncommon = false;
    for (BestiaryDrop drop : visibleDrops) {
      if (drop.tier() == bestiary.LootTier.UNCOMMON) {
        g2.setColor(UI.COL_TEXT);
        g2.drawString("  - " + getDropDisplayName(drop), x, y);
        y += LINE_H - 4;
        hasUncommon = true;
      }
    }
    if (!hasUncommon) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString("  " + ui.t("bestiary.detail.noDrops"), x, y);
      y += LINE_H - 4;
    }

    y += 8;

    // Resistências
    g2.setFont(ui.fonts.pixelBold(18F));
    g2.setColor(UI.COL_ACCENT);
    g2.drawString(ui.t("bestiary.detail.resistances") + ":", x, y);
    y += LINE_H;
    g2.setFont(ui.fonts.pixelPlain(16F));
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString("  " + ui.t("bestiary.detail.resistNotImpl"), x, y);
    y += LINE_H;

    y += 4;

    // Localizações
    g2.setFont(ui.fonts.pixelBold(18F));
    g2.setColor(UI.COL_ACCENT);
    g2.drawString(ui.t("bestiary.detail.locations") + ":", x, y);
    y += LINE_H;

    g2.setFont(ui.fonts.pixelPlain(16F));
    for (BestiaryLocation loc : creature.getLocations()) {
      g2.setColor(UI.COL_TEXT);
      g2.drawString("  - " + loc.regionName(), x, y);
      y += LINE_H - 4;
    }

    return y;
  }

  private int drawStageFinalContent(Graphics2D g2, int x, int w,
      BestiaryCreature creature, BestiaryProgress progress,
      BestiaryService service) {
    int y = currentContentY;

    // Drops raros e semi-raros
    g2.setFont(ui.fonts.pixelBold(18F));
    g2.setColor(UI.COL_ACCENT);
    g2.drawString(ui.t("bestiary.detail.rareDrops") + ":", x, y);
    y += LINE_H;

    g2.setFont(ui.fonts.pixelPlain(16F));
    List<BestiaryDrop> visibleDrops = service.getVisibleDrops(creature, progress);
    boolean hasRare = false;
    for (BestiaryDrop drop : visibleDrops) {
      if (drop.tier() == bestiary.LootTier.SEMI_RARE
          || drop.tier() == bestiary.LootTier.RARE) {
        g2.setColor(UI.COL_TEXT);
        g2.drawString("  - " + getDropDisplayName(drop), x, y);
        y += LINE_H - 4;
        hasRare = true;
      }
    }

    // VERY_RARE revelados
    for (BestiaryDrop drop : visibleDrops) {
      if (drop.tier() == bestiary.LootTier.VERY_RARE) {
        g2.setColor(new Color(200, 80, 255));
        g2.drawString("  - " + getDropDisplayName(drop) + " ★", x, y);
        y += LINE_H - 4;
        hasRare = true;
      }
    }

    if (!hasRare) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString("  " + ui.t("bestiary.detail.noDrops"), x, y);
      y += LINE_H - 4;
    }

    y += 12;

    // Pontos de recompensa
    g2.setFont(ui.fonts.pixelBold(18F));
    g2.setColor(COL_STAGE_DONE);
    g2.drawString(ui.t("bestiary.detail.rewardPoints") + ": +"
        + creature.getCompletionPoints(), x, y);
    y += LINE_H;

    return y;
  }

  // ===== Bloco de estágio (genérico) =====

  @FunctionalInterface
  private interface ContentRenderer {
    int render();
  }

  /**
   * Desenha um bloco de estágio com cabeçalho e conteúdo.
   * Se locked, mostra cadeado. Retorna Y após o bloco.
   */
  private int drawStageBlock(Graphics2D g2, int x, int y, int w,
      String title, boolean unlocked, ContentRenderer renderer) {

    int headerH = 28;

    // Cabeçalho do bloco
    g2.setColor(unlocked ? new Color(20, 35, 55) : new Color(25, 25, 35));
    g2.fillRoundRect(x, y, w, headerH, 6, 6);

    g2.setFont(ui.fonts.pixelBold(18F));
    g2.setColor(unlocked ? COL_STAGE_DONE : COL_STAGE_LOCKED);
    String icon = unlocked ? "✓ " : "✗ ";
    g2.drawString(icon + title, x + 8, y + 19);

    y += headerH + 4;

    if (unlocked) {
      currentContentY = y;
      y = renderer.render();
      y += 8;
    } else {
      g2.setFont(ui.fonts.pixelPlain(16F));
      g2.setColor(COL_STAGE_LOCKED);
      g2.drawString("  " + ui.t("bestiary.stage.locked"), x + 8, y + 14);
      y += LINE_H + 8;
    }

    return y;
  }

  // ===== Barras de progresso =====

  private void drawProgressBar(Graphics2D g2, int x, int y, int w, int h,
      BestiaryCreature creature, BestiaryProgress progress,
      BestiaryService service) {
    BestiaryStage stage = progress.getStage(creature.getDifficulty(), creature.getRarity());

    // Fundo
    g2.setColor(COL_PROGRESS_BG);
    g2.fillRoundRect(x, y, w, h, 6, 6);

    // Preenchimento
    if (stage != BestiaryStage.LOCKED) {
      int kills = progress.getKills();
      int nextTarget = progress.getKillsForNextStage(
          creature.getDifficulty(), creature.getRarity());

      float ratio;
      if (stage == BestiaryStage.COMPLETED) {
        ratio = 1.0f;
      } else if (nextTarget > 0) {
        ratio = Math.min(1.0f, (float) kills / nextTarget);
      } else {
        ratio = 0;
      }

      int fillW = (int) (w * ratio);
      if (fillW > 0) {
        g2.setColor(stage == BestiaryStage.COMPLETED ? COL_STAGE_DONE : COL_PROGRESS_FILL);
        g2.fillRoundRect(x, y, fillW, h, 6, 6);
      }

      // Texto
      g2.setFont(ui.fonts.pixelPlain(14F));
      String text;
      if (stage == BestiaryStage.COMPLETED) {
        text = ui.t("bestiary.stage.complete");
      } else {
        text = kills + " / " + nextTarget;
      }
      g2.setColor(Color.black);
      g2.drawString(text, x + 5, y + h - 3);
      g2.setColor(UI.COL_TEXT);
      g2.drawString(text, x + 4, y + h - 4);
    }
  }

  private void drawMiniProgressBar(Graphics2D g2, int x, int y, int w, int h,
      BestiaryCreature creature, BestiaryService service) {
    BestiaryProgress progress = service.getProgress(creature.getId());
    BestiaryStage stage = progress.getStage(creature.getDifficulty(), creature.getRarity());

    g2.setColor(COL_PROGRESS_BG);
    g2.fillRoundRect(x, y, w, h, 4, 4);

    if (stage != BestiaryStage.LOCKED) {
      int kills = progress.getKills();
      int nextTarget = progress.getKillsForNextStage(
          creature.getDifficulty(), creature.getRarity());

      float ratio;
      if (stage == BestiaryStage.COMPLETED) {
        ratio = 1.0f;
      } else if (nextTarget > 0) {
        ratio = Math.min(1.0f, (float) kills / nextTarget);
      } else {
        ratio = 0;
      }

      int fillW = Math.max(1, (int) (w * ratio));
      g2.setColor(stage == BestiaryStage.COMPLETED ? COL_STAGE_DONE : COL_PROGRESS_FILL);
      g2.fillRoundRect(x, y, fillW, h, 4, 4);
    }
  }

  // ===== Helpers =====

  private String getStageLabel(BestiaryStage stage) {
    return switch (stage) {
      case LOCKED -> ui.t("bestiary.stageLabel.locked");
      case DISCOVERED -> ui.t("bestiary.stageLabel.discovered");
      case STAGE_1 -> ui.t("bestiary.stageLabel.stage1");
      case STAGE_2 -> ui.t("bestiary.stageLabel.stage2");
      case COMPLETED -> ui.t("bestiary.stageLabel.completed");
    };
  }

  private String getDropDisplayName(BestiaryDrop drop) {
    // Tenta traduzir o itemId como chave i18n (ex.: "item.coin.bronze" → "item.coin.bronze.name")
    String translated = ui.getGamePanel().getLanguageManager()
        .text(drop.itemId() + ".name");
    if (!translated.equals(drop.itemId() + ".name")) {
      return translated;
    }
    // Fallback: usar o itemName do registro
    return drop.itemName();
  }

  private void drawHint(Graphics2D g2, int x, int y, int w, String text) {
    g2.setFont(ui.fonts.pixelPlain(14F));
    g2.setColor(UI.COL_TEXT_DIM);
    int textW = g2.getFontMetrics().stringWidth(text);
    g2.drawString(text, x + (w - textW) / 2, y);
  }

  /**
   * Retorna criaturas da classe fornecida, filtradas pela query de pesquisa (se ativa).
   * Recebe a classe como parâmetro para não depender do campo de instância.
   */
  private List<BestiaryCreature> getFilteredCreatures(BestiaryService service, CreatureClass cc) {
    List<BestiaryCreature> all = service.getRegistry().getCreaturesByClass(cc);
    if (searchQuery.isEmpty()) return all;

    String query = searchQuery.toLowerCase();
    return all.stream()
        .filter(c -> {
          String name = service.getCreatureDisplayName(c).toLowerCase();
          return name.contains(query);
        })
        .toList();
  }

  /**
   * Overload que usa currentClass (para chamadas de navegação que não passam pela draw path).
   * Utiliza resolveCurrentClass() para segurança.
   */
  private List<BestiaryCreature> getFilteredCreatures(BestiaryService service) {
    CreatureClass cc = resolveCurrentClass();
    if (cc == null) return List.of();
    return getFilteredCreatures(service, cc);
  }

  // ===== Navegação (chamado pelo KeyHandler) =====

  /** Entra na lista de criaturas da classe selecionada. */
  public void enterSelectedClass() {
    var gp = ui.getGamePanel();
    BestiaryService service = gp.getBestiaryService();
    List<CreatureClass> classes = service.getRegistry().getOrderedClasses();

    if (ui.getCommandNum() >= 0 && ui.getCommandNum() < classes.size()) {
      currentClass = classes.get(ui.getCommandNum());
      selectedCreatureIndex = 0;
      scrollOffset = 0;
      ui.setSubState(1);
      ui.setCommandNum(0);
    }
  }

  /** Entra nos detalhes da criatura selecionada. */
  public void enterSelectedCreature() {
    var gp = ui.getGamePanel();
    BestiaryService service = gp.getBestiaryService();

    List<BestiaryCreature> creatures = getFilteredCreatures(service);
    if (selectedCreatureIndex >= 0 && selectedCreatureIndex < creatures.size()) {
      BestiaryStage stage = service.getStage(
          creatures.get(selectedCreatureIndex).getId());
      // Só entra se não estiver locked
      if (stage != BestiaryStage.LOCKED) {
        detailScrollOffset = 0;
        ui.setSubState(2);
      }
    }
  }

  /** Volta para a tela anterior. */
  public void goBack() {
    int sub = ui.getSubState();
    if (sub == 2) {
      ui.setSubState(1);
      ui.setCommandNum(0);
    } else if (sub == 1) {
      // IMPORTANTE: mudar subState ANTES de limpar currentClass.
      // Isso garante que a thread de draw nunca veja subState=1 com currentClass=null
      // (race condition que causava NPE).
      ui.setSubState(0);
      ui.setCommandNum(selectedClassIndex);

      // Agora é seguro limpar — a draw thread já vê subState=0 (tela de classes)
      selectedCreatureIndex = 0;
      scrollOffset = 0;
      searchQuery = "";
      searchMode = false;
      // Não anulamos currentClass — a resolveCurrentClass() + o snapshot local
      // nas draw methods já garantem segurança. Manter o valor anterior
      // como cache é seguro e evita a janela de race condition.
    } else {
      // Fechar bestiário
      ui.getGamePanel().setGameState(GamePanel.PLAY_STATE);
    }
  }

  /** Move a seleção para cima (lista de criaturas). */
  public void navigateUp() {
    int sub = ui.getSubState();
    if (sub == 1) {
      var gp = ui.getGamePanel();
      List<BestiaryCreature> creatures = getFilteredCreatures(gp.getBestiaryService());
      if (creatures.isEmpty()) return;

      selectedCreatureIndex--;
      if (selectedCreatureIndex < 0) {
        selectedCreatureIndex = creatures.size() - 1;
      }
      ensureCreatureVisible();
    } else if (sub == 2) {
      if (detailScrollOffset > 0) {
        detailScrollOffset--;
      }
    }
  }

  /** Move a seleção para baixo (lista de criaturas). */
  public void navigateDown() {
    int sub = ui.getSubState();
    if (sub == 1) {
      var gp = ui.getGamePanel();
      List<BestiaryCreature> creatures = getFilteredCreatures(gp.getBestiaryService());
      if (creatures.isEmpty()) return;

      selectedCreatureIndex++;
      if (selectedCreatureIndex >= creatures.size()) {
        selectedCreatureIndex = 0;
      }
      ensureCreatureVisible();
    } else if (sub == 2) {
      detailScrollOffset++;
    }
  }

  /** Armazena o índice da classe ao mudar para lista de criaturas. */
  public void setSelectedClassIndex(int index) {
    this.selectedClassIndex = index;
  }

  /**
   * Reseta estado ao abrir o bestiário.
   * Inicializa currentClass com a primeira classe disponível (nunca null se há dados).
   * Garante que o estado é consistente para a draw thread.
   */
  public void resetState() {
    // Mudar subState primeiro (antes de qualquer outra coisa)
    // para que a draw thread veja estado consistente
    ui.setSubState(0);
    ui.setCommandNum(0);
    selectedClassIndex = 0;
    selectedCreatureIndex = 0;
    scrollOffset = 0;
    detailScrollOffset = 0;
    searchQuery = "";
    searchMode = false;

    // Inicializar currentClass com default seguro (nunca null se há classes registradas)
    List<CreatureClass> classes = ui.getGamePanel().getBestiaryService()
        .getRegistry().getOrderedClasses();
    currentClass = classes.isEmpty() ? null : classes.get(0);
  }

  /** Retorna o número máximo de itens no menu de classes. */
  public int getClassCount() {
    return ui.getGamePanel().getBestiaryService()
        .getRegistry().getOrderedClasses().size();
  }

  // --- Scroll helpers ---

  private void ensureCreatureVisible() {
    if (selectedCreatureIndex < scrollOffset) {
      scrollOffset = selectedCreatureIndex;
    } else if (selectedCreatureIndex >= scrollOffset + MAX_VISIBLE_ITEMS) {
      scrollOffset = selectedCreatureIndex - MAX_VISIBLE_ITEMS + 1;
    }
  }
}
