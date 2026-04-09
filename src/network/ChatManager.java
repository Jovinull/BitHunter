package network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entity.RemotePlayer;
import main.GamePanel;
import network.packets.ChatPacket;

/**
 * Gerencia o histórico de chat multiplayer e o envio de mensagens.
 */
public class ChatManager {

  /** Número máximo de mensagens mantidas no histórico. */
  private static final int MAX_HISTORY = 50;

  /** Tempo (em frames a 60 Hz) que mensagens recentes ficam visíveis na HUD. */
  public static final int VISIBLE_DURATION = 360; // 6 segundos

  private final GamePanel gp;
  private final List<ChatEntry> history = new ArrayList<>();

  /** Se true, o campo de input de chat está ativo. */
  private boolean inputActive = false;

  /** Buffer de texto sendo digitado. */
  private final StringBuilder inputBuffer = new StringBuilder();

  public ChatManager(GamePanel gp) {
    this.gp = gp;
  }

  /** Entrada no histórico de chat. */
  public record ChatEntry(String senderName, String message, int frameTimestamp) {}

  // ---- Envio ----

  /** Envia a mensagem digitada e limpa o buffer. */
  public void sendCurrentInput() {
    String msg = inputBuffer.toString().trim();
    if (msg.isEmpty()) {
      return;
    }
    int localId = gp.getNetworkManager().isHost() ? 0 : gp.getLocalNetworkId();
    ChatPacket pkt = new ChatPacket(localId, msg);

    if (gp.getNetworkManager().isHost()) {
      gp.getNetworkManager().broadcast(pkt);
    } else {
      gp.getNetworkManager().sendToServer(pkt);
    }

    // Adiciona localmente também
    addEntry(localId, msg);
    inputBuffer.setLength(0);
  }

  // ---- Recebimento ----

  /** Processa um pacote de chat recebido da rede. */
  public void handleChatPacket(ChatPacket pkt) {
    addEntry(pkt.getSenderNetworkId(), pkt.getMessage());
  }

  private void addEntry(int senderId, String message) {
    String name = resolvePlayerName(senderId);
    int frame = (int) gp.getCollisionFrameCounter();
    history.add(new ChatEntry(name, message, frame));
    if (history.size() > MAX_HISTORY) {
      history.removeFirst();
    }
  }

  // ---- Input ----

  /** Alterna o campo de input (abre/fecha). */
  public void toggleInput() {
    inputActive = !inputActive;
    if (!inputActive) {
      inputBuffer.setLength(0);
    }
  }

  /** Abre o campo de input. */
  public void openInput() {
    inputActive = true;
  }

  /** Fecha o campo de input e limpa o buffer. */
  public void closeInput() {
    inputActive = false;
    inputBuffer.setLength(0);
  }

  /** Adiciona um caractere ao buffer de input. */
  public void typeChar(char c) {
    if (inputBuffer.length() < ChatPacket.MAX_MESSAGE_LENGTH) {
      inputBuffer.append(c);
    }
  }

  /** Remove o último caractere do buffer. */
  public void backspace() {
    if (!inputBuffer.isEmpty()) {
      inputBuffer.deleteCharAt(inputBuffer.length() - 1);
    }
  }

  // ---- Getters ----

  public boolean isInputActive()      { return inputActive; }
  public String getInputText()        { return inputBuffer.toString(); }
  public List<ChatEntry> getHistory() { return Collections.unmodifiableList(history); }

  /**
   * Retorna as mensagens recentes visíveis na HUD (dentro de {@link #VISIBLE_DURATION}).
   */
  public List<ChatEntry> getRecentMessages() {
    int currentFrame = (int) gp.getCollisionFrameCounter();
    int cutoff = currentFrame - VISIBLE_DURATION;
    List<ChatEntry> recent = new ArrayList<>();
    for (int i = history.size() - 1; i >= 0 && recent.size() < 8; i--) {
      ChatEntry entry = history.get(i);
      if (entry.frameTimestamp() >= cutoff) {
        recent.add(entry);
      } else {
        break;
      }
    }
    Collections.reverse(recent);
    return recent;
  }

  private String resolvePlayerName(int networkId) {
    if (networkId == 0) {
      return gp.getNetworkManager().isHost()
          ? gp.getPlayer().getPlayerName()
          : "Host";
    }
    int localId = gp.getLocalNetworkId();
    if (networkId == localId) {
      return gp.getPlayer().getPlayerName();
    }
    for (RemotePlayer rp : gp.getRemotePlayers()) {
      if (rp.getNetworkId() == networkId) {
        return rp.getPlayerName();
      }
    }
    return "Jogador #" + networkId;
  }
}
