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
  private static final int LIST_FRAME_X_TILES = 1;
  private static final int LIST_FRAME_Y_DIVISOR = 2;
  private static final int LIST_FRAME_HORIZONTAL_MARGIN_TILES = 2;
  private static final int LIST_FRAME_BOTTOM_MARGIN_TILES = 1;
  private static final int LIST_TITLE_Y_OFFSET = 10;
  private static final int LIST_TITLE_SPACING = 36;
  private static final int HINT_BOTTOM_Y_OFFSET = 30;
  private static final int CLASS_ITEM_SELECTED_X_OFFSET = 4;
  private static final int CLASS_ITEM_SELECTED_Y_OFFSET = 4;
  private static final int CLASS_ITEM_SELECTED_WIDTH_OFFSET = 8;
  private static final int CLASS_ITEM_HEIGHT_OFFSET = 4;
  private static final int CLASS_NAME_X_OFFSET = 8;
  private static final int CLASS_NAME_Y_OFFSET = 22;
  private static final int CLASS_COUNT_RIGHT_OFFSET = 8;
  private static final int SEARCH_BOX_Y_OFFSET = 16;
  private static final int SEARCH_BOX_HEIGHT = 24;
  private static final int SEARCH_BOX_ROUND = 6;
  private static final int SEARCH_TEXT_X_OFFSET = 6;
  private static final int SEARCH_SECTION_SPACING = 28;
  private static final int CREATURE_ITEM_SELECTED_Y_OFFSET = 8;
  private static final int CREATURE_SPRITE_SIZE = 40;
  private static final int CREATURE_SPRITE_Y_OFFSET = 4;
  private static final int CREATURE_SPRITE_ROUND = 6;
  private static final int CREATURE_SILHOUETTE_MARGIN = 8;
  private static final int CREATURE_NAME_X_OFFSET = 12;
  private static final int CREATURE_NAME_Y_OFFSET = 14;
  private static final int MINI_PROGRESS_WIDTH = 120;
  private static final int MINI_PROGRESS_HEIGHT = 10;
  private static final int STAGE_LABEL_RIGHT_OFFSET = 8;
  private static final int SCROLL_INFO_RIGHT_OFFSET = 80;
  private static final int SCROLL_INFO_BOTTOM_OFFSET = 40;
  private static final int DETAIL_FRAME_X_DIVISOR = 2;
  private static final int DETAIL_FRAME_Y_DIVISOR = 4;
  private static final int DETAIL_FRAME_HORIZONTAL_MARGIN_TILES = 1;
  private static final int DETAIL_FRAME_BOTTOM_DIVISOR = 2;
  private static final int DETAIL_TEXT_Y_OFFSET = 8;
  private static final int DETAIL_HEADER_SIZE_EXTRA = 16;
  private static final int DETAIL_HEADER_ROUND = 10;
  private static final int DETAIL_HEADER_ICON_MARGIN = 8;
  private static final int DETAIL_INFO_X_OFFSET = 16;
  private static final int DETAIL_INFO_Y_OFFSET = 8;
  private static final int DETAIL_NAME_Y_OFFSET = 14;
  private static final int DETAIL_DIFF_SPACING = 28;
  private static final int DETAIL_RARITY_SPACING = 20;
  private static final int DETAIL_PROGRESS_SPACING = 24;
  private static final int DETAIL_PROGRESS_WIDTH = 260;
  private static final int DETAIL_PROGRESS_HEIGHT = 14;
  private static final int DETAIL_BLOCK_Y_OFFSET = 20;
  private static final int DETAIL_BLOCK_INNER_PADDING = 8;
  private static final int DETAIL_BLOCK_INNER_WIDTH_REDUCTION = 16;
  private static final int STAT_VALUE_X_OFFSET = 40;
  private static final int SPEED_VALUE_X_OFFSET = 120;
  private static final int ARMOR_VALUE_X_OFFSET = 120;
  private static final int BEHAVIOR_VALUE_X_OFFSET = 140;
  private static final int STAGE_EXTRA_GAP_SMALL = 4;
  private static final int STAGE_EXTRA_GAP_MEDIUM = 8;
  private static final int STAGE_EXTRA_GAP_LARGE = 12;
  private static final int STAGE_BLOCK_HEADER_HEIGHT = 28;
  private static final int STAGE_BLOCK_ROUND = 6;
  private static final int STAGE_BLOCK_TITLE_X_OFFSET = 8;
  private static final int STAGE_BLOCK_TITLE_Y_OFFSET = 19;
  private static final int STAGE_BLOCK_LOCKED_TEXT_Y_OFFSET = 14;
  private static final int PROGRESS_BAR_ROUND = 6;
  private static final int PROGRESS_TEXT_X = 4;
  private static final int PROGRESS_TEXT_Y_BOTTOM = 4;
  private static final int PROGRESS_TEXT_SHADOW_OFFSET = 1;
  private static final int MINI_PROGRESS_ROUND = 4;
  private static final Color COL_DETAIL_HEADER_BG = new Color(10, 10, 15, 190);
  private static final Color COL_RARITY = new Color(200, 80, 255);
  private static final Color COL_STAGE_HEADER_UNLOCKED = new Color(20, 35, 55);
  private static final Color COL_STAGE_HEADER_LOCKED = new Color(25, 25, 35);

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
    int frameX = GamePanel.TILE_SIZE * LIST_FRAME_X_TILES;
    int frameY = GamePanel.TILE_SIZE / LIST_FRAME_Y_DIVISOR;
    int frameW = GamePanel.SCREEN_WIDTH - GamePanel.TILE_SIZE * LIST_FRAME_HORIZONTAL_MARGIN_TILES;
    int frameH = GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE * LIST_FRAME_BOTTOM_MARGIN_TILES;
    ui.drawSubWindow(frameX, frameY, frameW, frameH);

    // Título
    int textX = frameX + FRAME_PADDING;
    int textY = frameY + FRAME_PADDING + LIST_TITLE_Y_OFFSET;

    g2.setFont(ui.fonts.pixelBold(28F));
    ui.drawTextShadow(ui.t("bestiary.title"), textX, textY, UI.COL_TEXT, Color.black, 2);

    // Pontos do Bestiário
    String pointsText = ui.t("bestiary.points") + ": " + service.getBestiaryPoints();
    g2.setFont(ui.fonts.pixelPlain(18F));
    g2.setColor(UI.COL_TEXT_DIM);
    int pointsX = frameX + frameW - FRAME_PADDING
        - g2.getFontMetrics().stringWidth(pointsText);
    g2.drawString(pointsText, pointsX, textY);

    textY += LIST_TITLE_SPACING;

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
        g2.fillRoundRect(
            textX - CLASS_ITEM_SELECTED_X_OFFSET,
            itemY - CLASS_ITEM_SELECTED_Y_OFFSET,
            frameW - FRAME_PADDING * 2 + CLASS_ITEM_SELECTED_WIDTH_OFFSET,
            ITEM_H - CLASS_ITEM_HEIGHT_OFFSET,
            STAGE_BLOCK_ROUND,
            STAGE_BLOCK_ROUND);
      }

      // Nome da classe
      g2.setFont(ui.fonts.pixelBold(22F));
      g2.setColor(selected ? UI.COL_ACCENT : UI.COL_TEXT);
      g2.drawString(ui.t(cc.getTranslationKey()), textX + CLASS_NAME_X_OFFSET, itemY + CLASS_NAME_Y_OFFSET);

      // Contador
      int total = service.getRegistry().getCreaturesByClass(cc).size();
      int known = service.getKnownCountForClass(cc);
      int completed = service.getCompletedCountForClass(cc);

      g2.setFont(ui.fonts.pixelPlain(18F));
      g2.setColor(UI.COL_TEXT_DIM);
      String countText = known + "/" + total
          + "  (" + ui.t("bestiary.completed") + ": " + completed + ")";
      int countX = frameX + frameW - FRAME_PADDING - g2.getFontMetrics().stringWidth(countText) - CLASS_COUNT_RIGHT_OFFSET;
      g2.drawString(countText, countX, itemY + CLASS_NAME_Y_OFFSET);
    }

    // Hint
    drawHint(g2, frameX, frameY + frameH - HINT_BOTTOM_Y_OFFSET, frameW,
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

    int frameX = GamePanel.TILE_SIZE * LIST_FRAME_X_TILES;
    int frameY = GamePanel.TILE_SIZE / LIST_FRAME_Y_DIVISOR;
    int frameW = GamePanel.SCREEN_WIDTH - GamePanel.TILE_SIZE * LIST_FRAME_HORIZONTAL_MARGIN_TILES;
    int frameH = GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE * LIST_FRAME_BOTTOM_MARGIN_TILES;
    ui.drawSubWindow(frameX, frameY, frameW, frameH);

    int textX = frameX + FRAME_PADDING;
    int textY = frameY + FRAME_PADDING + LIST_TITLE_Y_OFFSET;

    // Título: nome da classe
    g2.setFont(ui.fonts.pixelBold(26F));
    ui.drawTextShadow(ui.t(safeClass.getTranslationKey()),
        textX, textY, UI.COL_TEXT, Color.black, 2);

    textY += LIST_TITLE_SPACING;

    // Pesquisa
    if (searchMode) {
      g2.setColor(COL_SEARCH_BG);
      g2.fillRoundRect(textX, textY - SEARCH_BOX_Y_OFFSET, frameW - FRAME_PADDING * 2, SEARCH_BOX_HEIGHT,
          SEARCH_BOX_ROUND, SEARCH_BOX_ROUND);
      g2.setFont(ui.fonts.pixelPlain(18F));
      g2.setColor(UI.COL_ACCENT);
      g2.drawString("> " + searchQuery + "_", textX + SEARCH_TEXT_X_OFFSET, textY);
      textY += SEARCH_SECTION_SPACING;
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
        g2.fillRoundRect(
            textX - CLASS_ITEM_SELECTED_X_OFFSET,
            itemY - CREATURE_ITEM_SELECTED_Y_OFFSET,
            frameW - FRAME_PADDING * 2 + CLASS_ITEM_SELECTED_WIDTH_OFFSET,
            ITEM_H - CLASS_ITEM_HEIGHT_OFFSET,
            STAGE_BLOCK_ROUND,
            STAGE_BLOCK_ROUND);
      }

      // Sprite ou silhueta
      int spriteX = textX + 4;
      int spriteY = itemY - CREATURE_SPRITE_Y_OFFSET;
      int spriteSize = CREATURE_SPRITE_SIZE;

      if (stage == BestiaryStage.LOCKED) {
        // Silhueta
        g2.setColor(COL_LOCKED);
        g2.fillRoundRect(spriteX, spriteY, spriteSize, spriteSize, CREATURE_SPRITE_ROUND, CREATURE_SPRITE_ROUND);
        g2.setColor(COL_SILHOUETTE);
        g2.fillOval(
            spriteX + CREATURE_SILHOUETTE_MARGIN,
            spriteY + CREATURE_SILHOUETTE_MARGIN,
            spriteSize - CREATURE_SILHOUETTE_MARGIN * 2,
            spriteSize - CREATURE_SILHOUETTE_MARGIN * 2);
      } else {
        // Sprite real
        BufferedImage sprite = service.getCreatureSprite(creature);
        if (sprite != null) {
          g2.drawImage(sprite, spriteX, spriteY, spriteSize, spriteSize, null);
        } else {
          g2.setColor(COL_LOCKED);
          g2.fillRoundRect(spriteX, spriteY, spriteSize, spriteSize, CREATURE_SPRITE_ROUND, CREATURE_SPRITE_ROUND);
        }
      }

      // Nome
      int nameX = spriteX + spriteSize + CREATURE_NAME_X_OFFSET;
      g2.setFont(ui.fonts.pixelBold(20F));

      if (stage == BestiaryStage.LOCKED) {
        g2.setColor(COL_STAGE_LOCKED);
        g2.drawString("???", nameX, itemY + CREATURE_NAME_Y_OFFSET);
      } else {
        g2.setColor(selected ? UI.COL_ACCENT : UI.COL_TEXT);
        g2.drawString(service.getCreatureDisplayName(creature), nameX, itemY + CREATURE_NAME_Y_OFFSET);
      }

      // Estágio / Barra de progresso (compacta)
      if (stage != BestiaryStage.LOCKED) {
        drawMiniProgressBar(g2, nameX, itemY + CLASS_NAME_Y_OFFSET, MINI_PROGRESS_WIDTH, MINI_PROGRESS_HEIGHT, creature, service);
      }

      // Indicador de estágio
      String stageLabel = getStageLabel(stage);
      g2.setFont(ui.fonts.pixelPlain(16F));
      g2.setColor(stage == BestiaryStage.COMPLETED ? COL_STAGE_DONE : UI.COL_TEXT_DIM);
      int stageLabelX = frameX + frameW - FRAME_PADDING
          - g2.getFontMetrics().stringWidth(stageLabel) - STAGE_LABEL_RIGHT_OFFSET;
      g2.drawString(stageLabel, stageLabelX, itemY + CREATURE_NAME_Y_OFFSET);
    }

    // Scroll indicator
    if (creatures.size() > MAX_VISIBLE_ITEMS) {
      g2.setFont(ui.fonts.pixelPlain(16F));
      g2.setColor(UI.COL_TEXT_DIM);
      String scrollText = (scrollOffset + 1) + "-"
          + Math.min(scrollOffset + MAX_VISIBLE_ITEMS, creatures.size())
          + " / " + creatures.size();
      g2.drawString(scrollText, frameX + frameW - FRAME_PADDING - SCROLL_INFO_RIGHT_OFFSET,
          frameY + frameH - SCROLL_INFO_BOTTOM_OFFSET);
    }

    // Hint
    drawHint(g2, frameX, frameY + frameH - HINT_BOTTOM_Y_OFFSET, frameW,
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

    int frameX = GamePanel.TILE_SIZE / DETAIL_FRAME_X_DIVISOR;
    int frameY = GamePanel.TILE_SIZE / DETAIL_FRAME_Y_DIVISOR;
    int frameW = GamePanel.SCREEN_WIDTH - GamePanel.TILE_SIZE * DETAIL_FRAME_HORIZONTAL_MARGIN_TILES;
    int frameH = GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE / DETAIL_FRAME_BOTTOM_DIVISOR;
    ui.drawSubWindow(frameX, frameY, frameW, frameH);

    int textX = frameX + FRAME_PADDING;
    int textY = frameY + FRAME_PADDING + DETAIL_TEXT_Y_OFFSET;

    // === Cabeçalho: Sprite + Nome + Dificuldade + Raridade ===
    int headerSpriteSize = GamePanel.TILE_SIZE + DETAIL_HEADER_SIZE_EXTRA;
    BufferedImage sprite = service.getCreatureSprite(creature);
    if (sprite != null) {
      // Fundo do sprite
      g2.setColor(COL_DETAIL_HEADER_BG);
      g2.fillRoundRect(textX, textY, headerSpriteSize, headerSpriteSize, DETAIL_HEADER_ROUND, DETAIL_HEADER_ROUND);
      g2.drawImage(
          sprite,
          textX + DETAIL_HEADER_ICON_MARGIN,
          textY + DETAIL_HEADER_ICON_MARGIN,
          headerSpriteSize - DETAIL_HEADER_ICON_MARGIN * 2,
          headerSpriteSize - DETAIL_HEADER_ICON_MARGIN * 2,
          null);
    }

    int infoX = textX + headerSpriteSize + DETAIL_INFO_X_OFFSET;
    int infoY = textY + DETAIL_INFO_Y_OFFSET;

    // Nome
    g2.setFont(ui.fonts.pixelBold(26F));
    g2.setColor(UI.COL_TEXT);
    g2.drawString(service.getCreatureDisplayName(creature), infoX, infoY + DETAIL_NAME_Y_OFFSET);

    // Dificuldade + Raridade
    infoY += DETAIL_DIFF_SPACING;
    g2.setFont(ui.fonts.pixelPlain(18F));
    g2.setColor(COL_DIFFICULTY);
    String diffText = ui.t("bestiary.difficulty") + ": "
        + ui.t("bestiary.diff." + creature.getDifficulty().name());
    g2.drawString(diffText, infoX, infoY);

    if (creature.getRarity() != bestiary.BestiaryRarity.NORMAL) {
      infoY += DETAIL_RARITY_SPACING;
      g2.setColor(COL_RARITY);
      g2.drawString(ui.t("bestiary.rarity." + creature.getRarity().name()), infoX, infoY);
    }

    // Barra de progresso
    infoY += DETAIL_PROGRESS_SPACING;
    drawProgressBar(g2, infoX, infoY, DETAIL_PROGRESS_WIDTH, DETAIL_PROGRESS_HEIGHT, creature, progress, service);

    // === Blocos de informação por estágio ===
    int blockY = textY + headerSpriteSize + DETAIL_BLOCK_Y_OFFSET;
    int blockX = textX;
    int blockW = frameW - FRAME_PADDING * 2;

    // Scroll para detalhes
    blockY -= detailScrollOffset * LINE_H;

    // Estágio 1: Stats básicos + drops comuns
    blockY = drawStageBlock(g2, blockX, blockY, blockW,
        ui.t("bestiary.stage1.title"),
        stage.ordinal() >= BestiaryStage.STAGE_1.ordinal(),
        () -> drawStage1Content(
            g2,
            blockX + DETAIL_BLOCK_INNER_PADDING,
            blockW - DETAIL_BLOCK_INNER_WIDTH_REDUCTION,
            creature,
            progress,
            service));

    // Estágio 2: Drops incomuns + Resistências + Localizações
    blockY = drawStageBlock(g2, blockX, blockY, blockW,
        ui.t("bestiary.stage2.title"),
        stage.ordinal() >= BestiaryStage.STAGE_2.ordinal(),
        () -> drawStage2Content(
            g2,
            blockX + DETAIL_BLOCK_INNER_PADDING,
            blockW - DETAIL_BLOCK_INNER_WIDTH_REDUCTION,
            creature,
            progress,
            service));

    // Estágio Final: Drops raros + Pontos
    drawStageBlock(g2, blockX, blockY, blockW,
        ui.t("bestiary.stageFinal.title"),
        stage == BestiaryStage.COMPLETED,
        () -> drawStageFinalContent(
            g2,
            blockX + DETAIL_BLOCK_INNER_PADDING,
            blockW - DETAIL_BLOCK_INNER_WIDTH_REDUCTION,
            creature,
            progress,
            service));

    // Hint
    drawHint(g2, frameX, frameY + frameH - (HINT_BOTTOM_Y_OFFSET - STAGE_EXTRA_GAP_SMALL), frameW,
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
    g2.drawString(String.valueOf(creature.getHp()), x + STAT_VALUE_X_OFFSET, y);
    y += LINE_H;

    // Velocidade
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t("bestiary.detail.speed") + ": ", x, y);
    g2.setColor(UI.COL_TEXT);
    g2.drawString(String.valueOf(creature.getSpeed()), x + SPEED_VALUE_X_OFFSET, y);
    y += LINE_H;

    // Armadura
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t("bestiary.detail.armor") + ": ", x, y);
    g2.setColor(UI.COL_TEXT);
    g2.drawString(String.valueOf(creature.getArmor()), x + ARMOR_VALUE_X_OFFSET, y);
    y += LINE_H;

    // Comportamento
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t("bestiary.detail.behavior") + ": ", x, y);
    g2.setColor(UI.COL_TEXT);
    String behaviorText = ui.t("bestiary.behavior." + creature.getBehavior());
    if (creature.isUsesSpells()) {
      behaviorText += " + " + ui.t("bestiary.detail.spells");
    }
    g2.drawString(behaviorText, x + BEHAVIOR_VALUE_X_OFFSET, y);
    y += LINE_H + STAGE_EXTRA_GAP_SMALL;

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
        y += LINE_H - STAGE_EXTRA_GAP_SMALL;
        hasCommon = true;
      }
    }
    if (!hasCommon) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString("  " + ui.t("bestiary.detail.noDrops"), x, y);
      y += LINE_H - STAGE_EXTRA_GAP_SMALL;
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
        y += LINE_H - STAGE_EXTRA_GAP_SMALL;
        hasUncommon = true;
      }
    }
    if (!hasUncommon) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString("  " + ui.t("bestiary.detail.noDrops"), x, y);
      y += LINE_H - STAGE_EXTRA_GAP_SMALL;
    }

    y += STAGE_EXTRA_GAP_MEDIUM;

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
      y += LINE_H - STAGE_EXTRA_GAP_SMALL;
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
        y += LINE_H - STAGE_EXTRA_GAP_SMALL;
        hasRare = true;
      }
    }

    // VERY_RARE revelados
    for (BestiaryDrop drop : visibleDrops) {
      if (drop.tier() == bestiary.LootTier.VERY_RARE) {
        g2.setColor(COL_RARITY);
        g2.drawString("  - " + getDropDisplayName(drop) + " ★", x, y);
        y += LINE_H - STAGE_EXTRA_GAP_SMALL;
        hasRare = true;
      }
    }

    if (!hasRare) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString("  " + ui.t("bestiary.detail.noDrops"), x, y);
      y += LINE_H - STAGE_EXTRA_GAP_SMALL;
    }

    y += STAGE_EXTRA_GAP_LARGE;

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

    int headerH = STAGE_BLOCK_HEADER_HEIGHT;

    // Cabeçalho do bloco
    g2.setColor(unlocked ? COL_STAGE_HEADER_UNLOCKED : COL_STAGE_HEADER_LOCKED);
    g2.fillRoundRect(x, y, w, headerH, STAGE_BLOCK_ROUND, STAGE_BLOCK_ROUND);

    g2.setFont(ui.fonts.pixelBold(18F));
    g2.setColor(unlocked ? COL_STAGE_DONE : COL_STAGE_LOCKED);
    String icon = unlocked ? "✓ " : "✗ ";
    g2.drawString(icon + title, x + STAGE_BLOCK_TITLE_X_OFFSET, y + STAGE_BLOCK_TITLE_Y_OFFSET);

    y += headerH + STAGE_EXTRA_GAP_SMALL;

    if (unlocked) {
      currentContentY = y;
      y = renderer.render();
      y += STAGE_EXTRA_GAP_MEDIUM;
    } else {
      g2.setFont(ui.fonts.pixelPlain(16F));
      g2.setColor(COL_STAGE_LOCKED);
      g2.drawString("  " + ui.t("bestiary.stage.locked"), x + STAGE_BLOCK_TITLE_X_OFFSET, y + STAGE_BLOCK_LOCKED_TEXT_Y_OFFSET);
      y += LINE_H + STAGE_EXTRA_GAP_MEDIUM;
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
    g2.fillRoundRect(x, y, w, h, PROGRESS_BAR_ROUND, PROGRESS_BAR_ROUND);

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
        g2.fillRoundRect(x, y, fillW, h, PROGRESS_BAR_ROUND, PROGRESS_BAR_ROUND);
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
      g2.drawString(text, x + PROGRESS_TEXT_X + PROGRESS_TEXT_SHADOW_OFFSET, y + h - PROGRESS_TEXT_Y_BOTTOM + PROGRESS_TEXT_SHADOW_OFFSET);
      g2.setColor(UI.COL_TEXT);
      g2.drawString(text, x + PROGRESS_TEXT_X, y + h - PROGRESS_TEXT_Y_BOTTOM);
    }
  }

  private void drawMiniProgressBar(Graphics2D g2, int x, int y, int w, int h,
      BestiaryCreature creature, BestiaryService service) {
    BestiaryProgress progress = service.getProgress(creature.getId());
    BestiaryStage stage = progress.getStage(creature.getDifficulty(), creature.getRarity());

    g2.setColor(COL_PROGRESS_BG);
    g2.fillRoundRect(x, y, w, h, MINI_PROGRESS_ROUND, MINI_PROGRESS_ROUND);

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
      g2.fillRoundRect(x, y, fillW, h, MINI_PROGRESS_ROUND, MINI_PROGRESS_ROUND);
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
