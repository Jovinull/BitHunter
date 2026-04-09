package ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import entity.Entity;
import main.GamePanel;
import network.PlayerTradeManager;
import network.PlayerTradeManager.TradeOffer;

/**
 * Tela de trade P2P entre jogadores.
 * <p>
 * Layout: inventário local à esquerda, ofertas locais no centro-esquerda,
 * ofertas remotas no centro-direita, status de confirmação embaixo.
 */
public final class PlayerTradeUi {

  private static final int SLOT_SIZE = GamePanel.TILE_SIZE;
  private static final int COLS = 5;
  private static final int OFFER_MAX_ROWS = 4;
  private static final Color COL_CONFIRMED = new Color(80, 200, 80);
  private static final Color COL_WAITING = new Color(200, 200, 80);

  private final UI ui;

  /** Cursor sobre o inventário local (para selecionar item a oferecer). */
  public int invCursorIndex = 0;

  /** Cursor sobre as ofertas locais (para remover item). */
  public int offerCursorIndex = 0;

  /**
   * Painel ativo: 0 = inventário local, 1 = ofertas locais.
   * Setas esquerda/direita alternam.
   */
  public int activePanel = 0;

  PlayerTradeUi(UI ui) {
    this.ui = ui;
  }

  void drawPlayerTradeScreen() {
    Graphics2D g2 = ui.g2;
    GamePanel gp = ui.getGamePanel();
    PlayerTradeManager tm = gp.getPlayerTradeManager();

    // Fundo semi-transparente
    g2.setColor(new Color(0, 0, 0, 180));
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    // Título
    g2.setFont(ui.fonts.displayBold(36F));
    String title = "Troca com " + tm.getPartnerName();
    ui.drawTextShadowCentered(title, GamePanel.TILE_SIZE, UI.COL_ACCENT, Color.black, 2);

    int centerX = GamePanel.SCREEN_WIDTH / 2;

    // ---- Painel esquerdo: inventário local ----
    drawInventoryPanel(g2, gp, centerX);

    // ---- Painel centro-esquerda: ofertas locais ----
    drawLocalOffers(g2, tm, centerX);

    // ---- Painel centro-direita: ofertas remotas ----
    drawRemoteOffers(g2, gp, tm, centerX);

    // ---- Status de confirmação ----
    drawConfirmationStatus(g2, tm);

    // ---- Instruções ----
    g2.setFont(ui.fonts.pixelPlain(14F));
    String hint = "ENTER=oferecer/remover • ←→=alternar painel • C=confirmar • ESC=cancelar";
    ui.drawTextShadowCentered(hint,
        GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE / 2,
        UI.COL_TEXT_DIM, Color.black, 1);
  }

  private void drawInventoryPanel(Graphics2D g2, GamePanel gp, int centerX) {
    var inv = gp.getPlayer().getInventory();
    int panelX = GamePanel.TILE_SIZE;
    int panelY = GamePanel.TILE_SIZE * 2;

    g2.setFont(ui.fonts.displayPlain(20F));
    g2.setColor(UI.COL_TEXT);
    g2.drawString("Seu inventário", panelX, panelY - 8);

    int cols = COLS;
    for (int i = 0; i < inv.size(); i++) {
      int col = i % cols;
      int row = i / cols;
      int x = panelX + col * SLOT_SIZE;
      int y = panelY + row * SLOT_SIZE;

      // Slot background
      Color bg = (activePanel == 0 && i == invCursorIndex)
          ? new Color(255, 255, 255, 80)
          : new Color(0, 0, 0, 120);
      g2.setColor(bg);
      g2.fillRoundRect(x, y, SLOT_SIZE - 2, SLOT_SIZE - 2, 8, 8);

      // Item image
      Entity item = inv.get(i);
      if (item.getDown1() != null) {
        g2.drawImage(item.getDown1(), x + 2, y + 2, SLOT_SIZE - 6, SLOT_SIZE - 6, null);
      }

      // Amount
      if (item.getAmount() > 1) {
        g2.setFont(ui.fonts.pixelPlain(12F));
        g2.setColor(Color.white);
        g2.drawString(String.valueOf(item.getAmount()), x + SLOT_SIZE - 16, y + SLOT_SIZE - 6);
      }

      // Verifica se está ofertado (marca visual)
      PlayerTradeManager tm = gp.getPlayerTradeManager();
      for (TradeOffer offer : tm.getLocalOffers()) {
        if (offer.internalId().equals(item.getInternalId())) {
          g2.setColor(new Color(255, 200, 0, 100));
          g2.fillRoundRect(x, y, SLOT_SIZE - 2, SLOT_SIZE - 2, 8, 8);
          break;
        }
      }
    }
  }

  private void drawLocalOffers(Graphics2D g2, PlayerTradeManager tm, int centerX) {
    int panelX = centerX - GamePanel.TILE_SIZE * 3;
    int panelY = GamePanel.TILE_SIZE * 2;

    g2.setFont(ui.fonts.displayPlain(20F));
    g2.setColor(UI.COL_ACCENT);
    g2.drawString("Sua oferta", panelX, panelY - 8);

    List<TradeOffer> offers = tm.getLocalOffers();
    for (int i = 0; i < OFFER_MAX_ROWS; i++) {
      int y = panelY + i * SLOT_SIZE;
      Color bg = (activePanel == 1 && i == offerCursorIndex)
          ? new Color(255, 255, 255, 80)
          : new Color(0, 0, 0, 120);
      g2.setColor(bg);
      g2.fillRoundRect(panelX, y, SLOT_SIZE * 2, SLOT_SIZE - 2, 8, 8);

      if (i < offers.size()) {
        TradeOffer offer = offers.get(i);
        Entity item = ui.getGamePanel().geteGenerator().getObject(offer.internalId());
        if (item != null && item.getDown1() != null) {
          g2.drawImage(item.getDown1(), panelX + 2, y + 2, SLOT_SIZE - 6, SLOT_SIZE - 6, null);
        }
        g2.setFont(ui.fonts.pixelPlain(12F));
        g2.setColor(UI.COL_TEXT);
        String name = item != null ? item.getName() : offer.internalId();
        g2.drawString(name, panelX + SLOT_SIZE, y + SLOT_SIZE / 2 + 4);
      }
    }
  }

  private void drawRemoteOffers(Graphics2D g2, GamePanel gp, PlayerTradeManager tm, int centerX) {
    int panelX = centerX + GamePanel.TILE_SIZE;
    int panelY = GamePanel.TILE_SIZE * 2;

    g2.setFont(ui.fonts.displayPlain(20F));
    g2.setColor(UI.COL_ACCENT);
    g2.drawString("Oferta de " + tm.getPartnerName(), panelX, panelY - 8);

    List<TradeOffer> offers = tm.getRemoteOffers();
    for (int i = 0; i < OFFER_MAX_ROWS; i++) {
      int y = panelY + i * SLOT_SIZE;
      g2.setColor(new Color(0, 0, 0, 120));
      g2.fillRoundRect(panelX, y, SLOT_SIZE * 2, SLOT_SIZE - 2, 8, 8);

      if (i < offers.size()) {
        TradeOffer offer = offers.get(i);
        Entity item = gp.geteGenerator().getObject(offer.internalId());
        if (item != null && item.getDown1() != null) {
          g2.drawImage(item.getDown1(), panelX + 2, y + 2, SLOT_SIZE - 6, SLOT_SIZE - 6, null);
        }
        g2.setFont(ui.fonts.pixelPlain(12F));
        g2.setColor(UI.COL_TEXT);
        String name = item != null ? item.getName() : offer.internalId();
        g2.drawString(name, panelX + SLOT_SIZE, y + SLOT_SIZE / 2 + 4);
      }
    }
  }

  private void drawConfirmationStatus(Graphics2D g2, PlayerTradeManager tm) {
    int y = GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE * 2;
    g2.setFont(ui.fonts.displayPlain(22F));

    String localStatus = tm.isLocalConfirmed() ? "Você: CONFIRMADO" : "Você: aguardando";
    Color localColor = tm.isLocalConfirmed() ? COL_CONFIRMED : COL_WAITING;
    ui.drawTextShadow(localStatus, GamePanel.TILE_SIZE * 2, y, localColor, Color.black, 2);

    String remoteStatus = tm.isRemoteConfirmed()
        ? tm.getPartnerName() + ": CONFIRMADO"
        : tm.getPartnerName() + ": aguardando";
    Color remoteColor = tm.isRemoteConfirmed() ? COL_CONFIRMED : COL_WAITING;
    ui.drawTextShadow(remoteStatus, GamePanel.SCREEN_WIDTH / 2 + GamePanel.TILE_SIZE, y,
        remoteColor, Color.black, 2);
  }

  void resetCursors() {
    invCursorIndex = 0;
    offerCursorIndex = 0;
    activePanel = 0;
  }
}
