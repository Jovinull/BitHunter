package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import main.GamePanel;
import skill.Skill;
import skill.SkillTree;

/**
 * Tela de Árvore de Habilidades.
 *
 * <p>Layout: 3 colunas (ramos) × 3 linhas (habilidades).
 * Navega com ←→ entre colunas e ↑↓ entre linhas.
 * Enter desbloqueia a habilidade selecionada.
 */
public final class SkillTreeUi {

  // ── Layout ──────────────────────────────────────────────────────────────
  private static final int FRAME_PADDING    = 18;
  private static final int HEADER_H         = 52;
  private static final int BRANCH_TITLE_H   = 32;
  private static final int NODE_W           = 160;
  private static final int NODE_H           = 68;
  private static final int NODE_GAP_H       = 18;  // espaço vertical entre nós
  private static final int COL_GAP          = 20;  // espaço horizontal entre colunas
  private static final int CONNECTOR_W      = 3;
  private static final int DETAIL_H         = 90;
  private static final int ARC              = 10;

  // ── Cores ────────────────────────────────────────────────────────────────
  private static final Color COL_UNLOCKED   = new Color(80, 200, 120);
  private static final Color COL_AVAILABLE  = new Color(200, 180, 60);
  private static final Color COL_LOCKED     = new Color(80, 80, 90);
  private static final Color COL_SELECTED_BORDER = new Color(255, 220, 80);
  private static final Color COL_CONNECTOR  = new Color(120, 120, 140);
  private static final Color COL_DETAIL_BG  = new Color(20, 20, 35, 220);

  // ── Estado de navegação ──────────────────────────────────────────────────
  private int selectedCol = 0;  // 0=Combate, 1=Defesa, 2=Utilidade
  private int selectedRow = 0;  // 0..2

  // ── Mensagem de feedback ─────────────────────────────────────────────────
  private String feedbackMsg = "";
  private int feedbackTimer = 0;
  private static final int FEEDBACK_DURATION = 90; // frames

  private final UI ui;

  public SkillTreeUi(UI ui) {
    this.ui = ui;
  }

  // ── Ponto de entrada ──────────────────────────────────────────────────────

  void drawSkillTreeScreen() {
    Graphics2D g2 = ui.g2;
    var gp = ui.getGamePanel();
    int ts = GamePanel.TILE_SIZE;

    // Frame principal
    int frameX = ts;
    int frameY = ts / 2;
    int frameW = GamePanel.SCREEN_WIDTH  - ts * 2;
    int frameH = GamePanel.SCREEN_HEIGHT - ts;
    ui.drawSubWindow(frameX, frameY, frameW, frameH);

    int innerX = frameX + FRAME_PADDING;
    int innerY = frameY + FRAME_PADDING;

    // Cabeçalho
    drawHeader(g2, gp, innerX, innerY, frameW - FRAME_PADDING * 2);
    innerY += HEADER_H;

    // Grid de habilidades
    int gridW = frameW - FRAME_PADDING * 2;
    int colW = (gridW - COL_GAP * 2) / 3;
    drawBranches(g2, gp, innerX, innerY, colW);

    // Painel de detalhes (fixo na parte inferior do frame)
    int detailY = frameY + frameH - DETAIL_H - FRAME_PADDING;
    drawDetailPanel(g2, gp, frameX + FRAME_PADDING, detailY,
        frameW - FRAME_PADDING * 2, DETAIL_H);

    // Hint de navegação
    int hintY = frameY + frameH - FRAME_PADDING / 2;
    g2.setFont(ui.fonts.pixelPlain(14F));
    g2.setColor(UI.COL_TEXT_DIM);
    String hint = ui.t("ui.skillTree.hint.nav");
    g2.drawString(hint, innerX, hintY);

    // Feedback (ex.: "Sem pontos", "Desbloqueada!")
    if (feedbackTimer > 0) {
      feedbackTimer--;
      float alpha = Math.min(1f, feedbackTimer / 20f);
      g2.setComposite(java.awt.AlphaComposite.getInstance(
          java.awt.AlphaComposite.SRC_OVER, alpha));
      g2.setFont(ui.fonts.pixelBold(20F));
      g2.setColor(UI.COL_ACCENT);
      int fw = g2.getFontMetrics().stringWidth(feedbackMsg);
      g2.drawString(feedbackMsg, frameX + (frameW - fw) / 2,
          frameY + frameH / 2);
      g2.setComposite(java.awt.AlphaComposite.getInstance(
          java.awt.AlphaComposite.SRC_OVER, 1f));
    }
  }

  // ── Cabeçalho ────────────────────────────────────────────────────────────

  private void drawHeader(Graphics2D g2, GamePanel gp,
      int x, int y, int w) {
    var player = gp.getPlayer();
    int pts = player.getSkillTree().getSkillPoints();

    g2.setFont(ui.fonts.pixelBold(26F));
    String title = ui.t("ui.skillTree.title");
    ui.drawTextShadow(title, x, y + 26, UI.COL_TEXT, Color.BLACK, 2);

    String ptsText = ui.t("ui.skillTree.points") + ": " + pts;
    g2.setFont(ui.fonts.pixelPlain(20F));
    g2.setColor(pts > 0 ? COL_AVAILABLE : UI.COL_TEXT_DIM);
    int pw = g2.getFontMetrics().stringWidth(ptsText);
    g2.drawString(ptsText, x + w - pw, y + 26);
  }

  // ── Grid de ramos ─────────────────────────────────────────────────────────

  private void drawBranches(Graphics2D g2, GamePanel gp,
      int startX, int startY, int colW) {
    var skillTree = gp.getPlayer().getSkillTree();
    List<List<String>> branches = SkillTree.BRANCHES;
    List<String> branchKeys = SkillTree.BRANCH_KEYS;

    for (int col = 0; col < branches.size(); col++) {
      int nodeX = startX + col * (colW + COL_GAP);
      List<String> branch = branches.get(col);

      // Título do ramo
      g2.setFont(ui.fonts.pixelBold(16F));
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString(ui.t(branchKeys.get(col)), nodeX, startY + 16);

      int nodeY = startY + BRANCH_TITLE_H;
      String prevId = null;
      int prevNodeCenterY = 0;

      for (int row = 0; row < branch.size(); row++) {
        String skillId = branch.get(row);
        Skill skill = skillTree.getSkill(skillId);

        boolean unlocked  = skillTree.isUnlocked(skillId);
        boolean available = !unlocked && skillTree.prerequisitesMet(skillId);
        boolean selected  = (col == selectedCol && row == selectedRow);

        // Linha conectora entre nós
        if (prevId != null) {
          int connX = nodeX + colW / 2 - CONNECTOR_W / 2;
          int connY = prevNodeCenterY;
          int connH = nodeY - prevNodeCenterY;
          g2.setColor(COL_CONNECTOR);
          g2.fillRect(connX, connY, CONNECTOR_W, connH);
        }

        drawSkillNode(g2, skill, skillTree,
            nodeX, nodeY, colW, NODE_H,
            unlocked, available, selected);

        prevId = skillId;
        prevNodeCenterY = nodeY + NODE_H;
        nodeY += NODE_H + NODE_GAP_H;
      }
    }
  }

  // ── Nó individual ────────────────────────────────────────────────────────

  private void drawSkillNode(Graphics2D g2, Skill skill, SkillTree skillTree,
      int x, int y, int w, int h,
      boolean unlocked, boolean available, boolean selected) {

    // Fundo
    Color bgColor = unlocked  ? new Color(30, 70, 40)
                  : available ? new Color(60, 55, 20)
                  :             new Color(30, 30, 40);
    g2.setColor(bgColor);
    g2.fillRoundRect(x, y, w, h, ARC, ARC);

    // Borda
    g2.setStroke(new BasicStroke(selected ? 2.5f : 1.5f));
    g2.setColor(selected  ? COL_SELECTED_BORDER
              : unlocked  ? COL_UNLOCKED
              : available ? COL_AVAILABLE
              :             COL_LOCKED);
    g2.drawRoundRect(x, y, w, h, ARC, ARC);
    g2.setStroke(new BasicStroke(1f));

    // Indicador de status (círculo colorido à esquerda)
    int circR = 8;
    int circX = x + 14;
    int circY = y + h / 2 - circR;
    g2.setColor(unlocked ? COL_UNLOCKED : available ? COL_AVAILABLE : COL_LOCKED);
    g2.fillOval(circX, circY, circR * 2, circR * 2);

    // Nome da habilidade
    g2.setFont(ui.fonts.pixelBold(15F));
    g2.setColor(unlocked ? COL_UNLOCKED : available ? COL_AVAILABLE : UI.COL_TEXT_DIM);
    String name = ui.t(skill.getTranslationKey() + ".name");
    g2.drawString(name, x + 34, y + 24);

    // Custo
    g2.setFont(ui.fonts.pixelPlain(13F));
    g2.setColor(UI.COL_TEXT_DIM);
    String costStr = unlocked ? ui.t("ui.skillTree.status.unlocked")
                   : ui.t("ui.skillTree.cost") + ": " + skill.getCost();
    g2.drawString(costStr, x + 34, y + 42);

    // Pré-requisitos (se tiver)
    if (!skill.getPrerequisiteIds().isEmpty() && !unlocked) {
      Skill prereq = skillTree.getSkill(skill.getPrerequisiteIds().get(0));
      if (prereq != null) {
        String prereqName = ui.t(prereq.getTranslationKey() + ".name");
        g2.setFont(ui.fonts.pixelPlain(12F));
        g2.setColor(new Color(150, 150, 160));
        g2.drawString("← " + prereqName, x + 34, y + 57);
      }
    }
  }

  // ── Painel de detalhes ────────────────────────────────────────────────────

  private void drawDetailPanel(Graphics2D g2, GamePanel gp,
      int x, int y, int w, int h) {
    var skillTree = gp.getPlayer().getSkillTree();
    String skillId = getSelectedSkillId();
    Skill skill = skillTree.getSkill(skillId);
    if (skill == null) return;

    // Fundo semi-transparente
    g2.setColor(COL_DETAIL_BG);
    g2.fillRoundRect(x, y, w, h, ARC, ARC);
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.setStroke(new BasicStroke(1.5f));
    g2.drawRoundRect(x, y, w, h, ARC, ARC);
    g2.setStroke(new BasicStroke(1f));

    int tx = x + 14;
    int ty = y + 22;

    g2.setFont(ui.fonts.pixelBold(16F));
    g2.setColor(UI.COL_TEXT);
    g2.drawString(ui.t(skill.getTranslationKey() + ".name"), tx, ty);

    g2.setFont(ui.fonts.pixelPlain(14F));
    g2.setColor(UI.COL_TEXT_DIM);
    ty += 20;
    g2.drawString(ui.t(skill.getTranslationKey() + ".desc"), tx, ty);

    ty += 18;
    boolean unlocked = skillTree.isUnlocked(skillId);
    if (unlocked) {
      g2.setColor(COL_UNLOCKED);
      g2.drawString(ui.t("ui.skillTree.status.unlocked"), tx, ty);
    } else {
      boolean canAfford = gp.getPlayer().getSkillTree().getSkillPoints() >= skill.getCost();
      boolean metPrereq = skillTree.prerequisitesMet(skillId);
      g2.setColor(canAfford && metPrereq ? COL_AVAILABLE : COL_LOCKED);
      String action = (canAfford && metPrereq)
          ? ui.t("ui.skillTree.hint.unlock")
          : !metPrereq ? ui.t("ui.skillTree.status.locked")
          : ui.t("ui.skillTree.hint.noPoints");
      g2.drawString(action, tx, ty);
    }
  }

  // ── Navegação ─────────────────────────────────────────────────────────────

  public void navigateRow(int delta) {
    int rows = SkillTree.BRANCHES.get(selectedCol).size();
    selectedRow = Math.floorMod(selectedRow + delta, rows);
  }

  public void navigateColumn(int delta) {
    int cols = SkillTree.BRANCHES.size();
    selectedCol = Math.floorMod(selectedCol + delta, cols);
    // Garante que a linha selecionada é válida na nova coluna
    int rows = SkillTree.BRANCHES.get(selectedCol).size();
    if (selectedRow >= rows) selectedRow = rows - 1;
  }

  public void tryUnlockSelected() {
    var gp = ui.getGamePanel();
    var player = gp.getPlayer();
    var skillTree = player.getSkillTree();
    String skillId = getSelectedSkillId();

    if (skillTree.isUnlocked(skillId)) {
      showFeedback(ui.t("ui.skillTree.status.unlocked"));
      return;
    }

    boolean ok = skillTree.tryUnlock(skillId, player);
    if (ok) {
      gp.playSE(8); // som de level up
      showFeedback(ui.t("ui.skillTree.feedback.unlocked"));
    } else {
      Skill skill = skillTree.getSkill(skillId);
      if (skill != null && !skillTree.prerequisitesMet(skillId)) {
        showFeedback(ui.t("ui.skillTree.status.locked"));
      } else {
        showFeedback(ui.t("ui.skillTree.hint.noPoints"));
      }
    }
  }

  public void resetState() {
    selectedCol = 0;
    selectedRow = 0;
    feedbackMsg = "";
    feedbackTimer = 0;
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private String getSelectedSkillId() {
    List<String> branch = SkillTree.BRANCHES.get(selectedCol);
    int row = Math.min(selectedRow, branch.size() - 1);
    return branch.get(row);
  }

  private void showFeedback(String msg) {
    feedbackMsg = msg;
    feedbackTimer = FEEDBACK_DURATION;
  }
}
