package ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import main.GamePanel;
import skill.Skill;
import skill.SkillTree;

/**
 * Tela de Árvore de Habilidades — 5 ramos × 5 habilidades.
 *
 * <p>Navegação: A/D (←→) trocam de ramo; W/S (↑↓) navegam dentro do ramo.
 * Enter desbloqueia. Esc/K fecha.
 */
public final class SkillTreeUi {

  // ── Layout (calculado para 960×576, TILE_SIZE=48) ─────────────────────────
  private static final int FRAME_PAD     = 14;
  private static final int HEADER_H      = 48;
  private static final int BRANCH_TTL_H  = 24;
  private static final int NODE_H        = 55;
  private static final int NODE_GAP      = 9;   // gap vertical entre nós
  private static final int COL_GAP       = 8;   // gap horizontal entre colunas
  private static final int CONNECTOR_W   = 3;
  private static final int DETAIL_H      = 80;
  private static final int HINT_H        = 18;
  private static final int ARC           = 8;

  // ── Cores ────────────────────────────────────────────────────────────────
  private static final Color COL_UNLOCKED        = new Color(70, 195, 110);
  private static final Color COL_AVAILABLE       = new Color(210, 185, 50);
  private static final Color COL_LOCKED          = new Color(75, 75, 88);
  private static final Color COL_SEL_BORDER      = new Color(255, 220, 60);
  private static final Color COL_CONNECTOR       = new Color(100, 100, 120);
  private static final Color COL_DETAIL_BG       = new Color(18, 18, 32, 225);
  private static final Color COL_BG_UNLOCKED     = new Color(25, 65, 38);
  private static final Color COL_BG_AVAILABLE    = new Color(58, 52, 18);
  private static final Color COL_BG_LOCKED       = new Color(28, 28, 38);

  // ── Estado de navegação ──────────────────────────────────────────────────
  private int selectedCol = 0;
  private int selectedRow = 0;

  // ── Feedback ─────────────────────────────────────────────────────────────
  private String feedbackMsg  = "";
  private int    feedbackTimer = 0;
  private boolean feedbackOk  = true;  // true=verde, false=vermelho
  private static final int FEEDBACK_DURATION = 90;

  private final UI ui;

  public SkillTreeUi(UI ui) {
    this.ui = ui;
  }

  // ── Ponto de entrada ──────────────────────────────────────────────────────

  void drawSkillTreeScreen() {
    Graphics2D g2 = ui.g2;
    var gp = ui.getGamePanel();
    int ts = GamePanel.TILE_SIZE;

    int frameX = ts / 2;
    int frameY = ts / 3;
    int frameW = GamePanel.SCREEN_WIDTH  - ts;
    int frameH = GamePanel.SCREEN_HEIGHT - ts * 2 / 3;
    ui.drawSubWindow(frameX, frameY, frameW, frameH);

    int ix = frameX + FRAME_PAD;   // inner left
    int iy = frameY + FRAME_PAD;   // inner top
    int iw = frameW - FRAME_PAD * 2;

    // Cabeçalho
    drawHeader(g2, gp, ix, iy, iw);
    iy += HEADER_H;

    // Largura de cada coluna
    int numCols = SkillTree.BRANCHES.size();
    int colW    = (iw - COL_GAP * (numCols - 1)) / numCols;

    // Grid de habilidades
    drawBranches(g2, gp, ix, iy, colW);

    // Painel de detalhes
    int detailY = frameY + frameH - DETAIL_H - HINT_H - FRAME_PAD * 2;
    drawDetailPanel(g2, gp, ix, detailY, iw, DETAIL_H);

    // Hint de navegação
    int hintY = frameY + frameH - FRAME_PAD - 4;
    g2.setFont(ui.fonts.pixelPlain(13F));
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t("ui.skillTree.hint.nav"), ix, hintY);

    // Feedback animado
    if (feedbackTimer > 0) {
      feedbackTimer--;
      float alpha = Math.min(1f, feedbackTimer / 20f);
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
      g2.setFont(ui.fonts.pixelBold(22F));
      g2.setColor(feedbackOk ? COL_UNLOCKED : new Color(210, 70, 70));
      int fw = g2.getFontMetrics().stringWidth(feedbackMsg);
      g2.drawString(feedbackMsg,
          frameX + (frameW - fw) / 2,
          frameY + frameH / 2 - 10);
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
  }

  // ── Cabeçalho ────────────────────────────────────────────────────────────

  private void drawHeader(Graphics2D g2, GamePanel gp, int x, int y, int w) {
    int pts = gp.getPlayer().getSkillTree().getSkillPoints();

    g2.setFont(ui.fonts.pixelBold(24F));
    String title = ui.t("ui.skillTree.title");
    ui.drawTextShadow(title, x, y + 24, UI.COL_TEXT, Color.BLACK, 2);

    g2.setFont(ui.fonts.pixelPlain(18F));
    g2.setColor(pts > 0 ? COL_AVAILABLE : UI.COL_TEXT_DIM);
    String ptsText = ui.t("ui.skillTree.points") + ": " + pts;
    int pw = g2.getFontMetrics().stringWidth(ptsText);
    g2.drawString(ptsText, x + w - pw, y + 24);
  }

  // ── Grid de ramos ─────────────────────────────────────────────────────────

  private void drawBranches(Graphics2D g2, GamePanel gp, int startX, int startY, int colW) {
    var skillTree = gp.getPlayer().getSkillTree();
    List<List<String>> branches = SkillTree.BRANCHES;
    List<String> branchKeys     = SkillTree.BRANCH_KEYS;

    for (int col = 0; col < branches.size(); col++) {
      int nodeX   = startX + col * (colW + COL_GAP);
      List<String> branch = branches.get(col);

      // Highlight de coluna selecionada (fundo suave)
      if (col == selectedCol) {
        int colH = BRANCH_TTL_H + branch.size() * NODE_H + (branch.size() - 1) * NODE_GAP;
        g2.setColor(new Color(255, 220, 60, 18));
        g2.fillRoundRect(nodeX - 4, startY - 4, colW + 8, colH + 8, ARC, ARC);
      }

      // Título do ramo
      g2.setFont(ui.fonts.pixelBold(14F));
      g2.setColor(col == selectedCol ? COL_AVAILABLE : UI.COL_TEXT_DIM);
      String branchTitle = ui.t(branchKeys.get(col));
      int tw = g2.getFontMetrics().stringWidth(branchTitle);
      g2.drawString(branchTitle, nodeX + (colW - tw) / 2, startY + 15);

      int nodeY            = startY + BRANCH_TTL_H;
      int prevNodeBottomY  = -1;

      for (int row = 0; row < branch.size(); row++) {
        String skillId = branch.get(row);
        Skill  skill   = skillTree.getSkill(skillId);

        boolean unlocked  = skillTree.isUnlocked(skillId);
        boolean available = !unlocked && skillTree.prerequisitesMet(skillId);
        boolean selected  = (col == selectedCol && row == selectedRow);

        // Conector vertical entre nós
        if (prevNodeBottomY >= 0) {
          int cx = nodeX + colW / 2 - CONNECTOR_W / 2;
          g2.setColor(COL_CONNECTOR);
          g2.fillRect(cx, prevNodeBottomY, CONNECTOR_W, nodeY - prevNodeBottomY);
        }

        drawSkillNode(g2, skill, nodeX, nodeY, colW, NODE_H,
            unlocked, available, selected);

        prevNodeBottomY = nodeY + NODE_H;
        nodeY += NODE_H + NODE_GAP;
      }
    }
  }

  // ── Nó individual ────────────────────────────────────────────────────────

  private void drawSkillNode(Graphics2D g2, Skill skill,
      int x, int y, int w, int h,
      boolean unlocked, boolean available, boolean selected) {

    // Fundo
    g2.setColor(unlocked ? COL_BG_UNLOCKED : available ? COL_BG_AVAILABLE : COL_BG_LOCKED);
    g2.fillRoundRect(x, y, w, h, ARC, ARC);

    // Borda
    float borderWidth = selected ? 2.5f : 1.2f;
    g2.setStroke(new BasicStroke(borderWidth));
    g2.setColor(selected  ? COL_SEL_BORDER
              : unlocked  ? COL_UNLOCKED
              : available ? COL_AVAILABLE
              :             COL_LOCKED);
    g2.drawRoundRect(x, y, w, h, ARC, ARC);
    g2.setStroke(new BasicStroke(1f));

    // Círculo de status
    int r  = 7;
    int cx = x + 12 + r;
    int cy = y + h / 2;
    g2.setColor(unlocked ? COL_UNLOCKED : available ? COL_AVAILABLE : COL_LOCKED);
    g2.fillOval(cx - r, cy - r, r * 2, r * 2);
    if (unlocked) {
      // check mark simples
      g2.setColor(Color.WHITE);
      g2.setStroke(new BasicStroke(1.8f));
      g2.drawLine(cx - 4, cy, cx - 1, cy + 3);
      g2.drawLine(cx - 1, cy + 3, cx + 4, cy - 3);
      g2.setStroke(new BasicStroke(1f));
    }

    int textX = x + 12 + r * 2 + 6;

    // Nome
    g2.setFont(ui.fonts.pixelBold(13F));
    g2.setColor(unlocked ? COL_UNLOCKED : available ? COL_AVAILABLE : UI.COL_TEXT_DIM);
    String name = ui.t(skill.getTranslationKey() + ".name");
    g2.drawString(name, textX, y + 20);

    // Custo / estado
    g2.setFont(ui.fonts.pixelPlain(12F));
    g2.setColor(new Color(160, 160, 170));
    String costLabel = unlocked
        ? ui.t("ui.skillTree.status.unlocked")
        : ui.t("ui.skillTree.cost") + ": " + skill.getCost() + " pt";
    g2.drawString(costLabel, textX, y + 36);
  }

  // ── Painel de detalhes ────────────────────────────────────────────────────

  private void drawDetailPanel(Graphics2D g2, GamePanel gp,
      int x, int y, int w, int h) {
    var skillTree = gp.getPlayer().getSkillTree();
    Skill skill   = skillTree.getSkill(getSelectedSkillId());
    if (skill == null) return;

    // Fundo
    g2.setColor(COL_DETAIL_BG);
    g2.fillRoundRect(x, y, w, h, ARC, ARC);
    g2.setStroke(new BasicStroke(1.4f));
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRoundRect(x, y, w, h, ARC, ARC);
    g2.setStroke(new BasicStroke(1f));

    int tx = x + 14;
    int ty = y + 20;

    // Nome + custo
    boolean unlocked = skillTree.isUnlocked(skill.getId());
    g2.setFont(ui.fonts.pixelBold(16F));
    g2.setColor(unlocked ? COL_UNLOCKED : UI.COL_TEXT);
    g2.drawString(ui.t(skill.getTranslationKey() + ".name"), tx, ty);

    // Custo alinhado à direita
    if (!unlocked) {
      g2.setFont(ui.fonts.pixelPlain(14F));
      g2.setColor(UI.COL_TEXT_DIM);
      String c = skill.getCost() + " pt";
      int cw = g2.getFontMetrics().stringWidth(c);
      g2.drawString(c, x + w - 14 - cw, ty);
    }

    // Descrição
    ty += 20;
    g2.setFont(ui.fonts.pixelPlain(14F));
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t(skill.getTranslationKey() + ".desc"), tx, ty);

    // Linha de ação / status
    ty += 20;
    if (unlocked) {
      g2.setColor(COL_UNLOCKED);
      g2.drawString(ui.t("ui.skillTree.status.unlocked"), tx, ty);
    } else {
      int pts     = gp.getPlayer().getSkillTree().getSkillPoints();
      boolean met = skillTree.prerequisitesMet(skill.getId());
      boolean canAfford = pts >= skill.getCost();

      if (!met) {
        // mostra pré-requisito faltando
        String prereqId = skill.getPrerequisiteIds().isEmpty()
            ? "" : skill.getPrerequisiteIds().get(0);
        Skill prereq = skillTree.getSkill(prereqId);
        g2.setColor(COL_LOCKED);
        String req = ui.t("ui.skillTree.prereq") + ": "
            + (prereq != null ? ui.t(prereq.getTranslationKey() + ".name") : "?");
        g2.drawString(req, tx, ty);
      } else if (!canAfford) {
        g2.setColor(new Color(200, 80, 80));
        g2.drawString(ui.t("ui.skillTree.hint.noPoints"), tx, ty);
      } else {
        g2.setColor(COL_AVAILABLE);
        g2.drawString(ui.t("ui.skillTree.hint.unlock"), tx, ty);
      }
    }
  }

  // ── Navegação ─────────────────────────────────────────────────────────────

  public void navigateRow(int delta) {
    int rows = SkillTree.BRANCHES.get(selectedCol).size();
    selectedRow = Math.floorMod(selectedRow + delta, rows);
  }

  public void navigateColumn(int delta) {
    selectedCol = Math.floorMod(selectedCol + delta, SkillTree.BRANCHES.size());
    int rows = SkillTree.BRANCHES.get(selectedCol).size();
    if (selectedRow >= rows) selectedRow = rows - 1;
  }

  public void tryUnlockSelected() {
    var gp     = ui.getGamePanel();
    var player = gp.getPlayer();
    var st     = player.getSkillTree();
    String id  = getSelectedSkillId();

    if (st.isUnlocked(id)) {
      showFeedback(ui.t("ui.skillTree.status.unlocked"), false);
      return;
    }

    boolean ok = st.tryUnlock(id, player);
    if (ok) {
      gp.playSE(8);
      showFeedback(ui.t("ui.skillTree.feedback.unlocked"), true);
    } else {
      Skill skill = st.getSkill(id);
      boolean notMet = skill != null && !st.prerequisitesMet(id);
      showFeedback(
          notMet ? ui.t("ui.skillTree.status.locked")
                 : ui.t("ui.skillTree.hint.noPoints"),
          false);
    }
  }

  public void resetState() {
    selectedCol  = 0;
    selectedRow  = 0;
    feedbackMsg  = "";
    feedbackTimer = 0;
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private String getSelectedSkillId() {
    List<String> branch = SkillTree.BRANCHES.get(selectedCol);
    return branch.get(Math.min(selectedRow, branch.size() - 1));
  }

  private void showFeedback(String msg, boolean ok) {
    feedbackMsg   = msg;
    feedbackTimer = FEEDBACK_DURATION;
    feedbackOk    = ok;
  }
}
