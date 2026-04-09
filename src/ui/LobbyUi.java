package ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import entity.RemotePlayer;
import main.GamePanel;

/**
 * Tela de lobby multiplayer: lista jogadores conectados e instrui o host
 * a pressionar ENTER para iniciar a partida (ou ESC para cancelar).
 */
final class LobbyUi {

  private final UI ui;

  LobbyUi(UI ui) {
    this.ui = ui;
  }

  void drawLobbyScreen() {
    Graphics2D g2 = ui.g2;
    GamePanel gp = ui.getGamePanel();

    g2.setColor(UI.COL_BG);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    // Título
    g2.setFont(ui.fonts.displayBold(56F));
    String title = gp.getNetworkManager().isHost() ? "LOBBY (HOST)" : "LOBBY (CLIENTE)";
    ui.drawTextShadowCentered(title, GamePanel.TILE_SIZE * 2, UI.COL_TEXT, Color.black, 3);

    // Lista de jogadores
    g2.setFont(ui.fonts.displayPlain(28F));
    int y = GamePanel.TILE_SIZE * 4;
    int lineH = (int) (GamePanel.TILE_SIZE * 0.9);

    // Jogador local primeiro
    String localName = gp.getPlayer().getPlayerName();
    String localTag = gp.getNetworkManager().isHost() ? " (host)" : " (você)";
    ui.drawTextShadowCentered("• " + localName + localTag, y, UI.COL_ACCENT, Color.black, 2);
    y += lineH;

    // Jogadores remotos
    List<RemotePlayer> remotes = gp.getRemotePlayers();
    if (remotes.isEmpty()) {
      ui.drawTextShadowCentered("(aguardando jogadores…)", y, UI.COL_TEXT_DIM, Color.black, 2);
    } else {
      for (RemotePlayer rp : remotes) {
        ui.drawTextShadowCentered("• " + rp.getPlayerName(), y, UI.COL_TEXT, Color.black, 2);
        y += lineH;
      }
    }

    // Instruções
    g2.setFont(ui.fonts.pixelPlain(16F));
    String hint;
    if (gp.getNetworkManager().isHost()) {
      hint = "ENTER para iniciar • ESC para cancelar";
    } else {
      hint = "Aguardando o host iniciar… • ESC para sair";
    }
    ui.drawTextShadowCentered(hint, GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE,
        UI.COL_TEXT_DIM, Color.black, 2);
  }
}
