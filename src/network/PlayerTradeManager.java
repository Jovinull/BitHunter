package network;

import java.util.ArrayList;
import java.util.List;

import entity.Entity;
import entity.RemotePlayer;
import main.GamePanel;
import network.packets.TradePacket;

/**
 * Gerencia o estado de um trade P2P entre dois jogadores.
 * <p>
 * O host atua como árbitro: valida ofertas, processa a troca final
 * e transmite as atualizações para ambos os lados.
 */
public class PlayerTradeManager {

  private final GamePanel gp;

  /** ID de rede do parceiro de trade (ou -1 se não há trade ativo). */
  private int tradePartnerId = -1;

  /** Itens oferecidos pelo jogador local (internalId + amount). */
  private final List<TradeOffer> localOffers = new ArrayList<>();

  /** Itens oferecidos pelo parceiro remoto (internalId + amount). */
  private final List<TradeOffer> remoteOffers = new ArrayList<>();

  /** Jogador local confirmou a troca. */
  private boolean localConfirmed = false;

  /** Parceiro remoto confirmou a troca. */
  private boolean remoteConfirmed = false;

  /** Há uma solicitação de trade pendente de outro jogador? */
  private int pendingRequestFrom = -1;

  public PlayerTradeManager(GamePanel gp) {
    this.gp = gp;
  }

  // ---- Ofertas ----

  /** Representação de uma oferta de item no trade. */
  public record TradeOffer(String internalId, int amount) {}

  // ---- Solicitação ----

  /**
   * Jogador local solicita trade com o jogador mais próximo.
   * Retorna true se encontrou um jogador e enviou a solicitação.
   */
  public boolean requestTradeWithNearest() {
    RemotePlayer nearest = findNearestPlayer();
    if (nearest == null) {
      return false;
    }
    int localId = gp.getNetworkManager().isHost() ? 0 : getLocalNetworkId();
    TradePacket pkt = new TradePacket(
        TradePacket.ACTION_REQUEST, localId, nearest.getNetworkId(), null, 0);
    sendPacket(pkt);
    return true;
  }

  /** Aceita a solicitação pendente. */
  public void acceptPendingRequest() {
    if (pendingRequestFrom < 0) {
      return;
    }
    int localId = gp.getNetworkManager().isHost() ? 0 : getLocalNetworkId();
    TradePacket pkt = new TradePacket(
        TradePacket.ACTION_ACCEPT, localId, pendingRequestFrom, null, 0);
    sendPacket(pkt);
    startTrade(pendingRequestFrom);
    pendingRequestFrom = -1;
  }

  /** Recusa a solicitação pendente. */
  public void declinePendingRequest() {
    if (pendingRequestFrom < 0) {
      return;
    }
    int localId = gp.getNetworkManager().isHost() ? 0 : getLocalNetworkId();
    TradePacket pkt = new TradePacket(
        TradePacket.ACTION_DECLINE, localId, pendingRequestFrom, null, 0);
    sendPacket(pkt);
    pendingRequestFrom = -1;
  }

  // ---- Ofertar/Remover itens ----

  /**
   * Oferece um item do inventário local na mesa de trade.
   *
   * @param itemIndex índice do item no inventário do jogador
   */
  public void offerItem(int itemIndex) {
    var inv = gp.getPlayer().getInventory();
    if (itemIndex < 0 || itemIndex >= inv.size()) {
      return;
    }
    Entity item = inv.get(itemIndex);
    // Não permitir oferecer itens equipados
    if (isEquipped(item)) {
      return;
    }
    // Verifica se já ofertou esse item
    for (TradeOffer offer : localOffers) {
      if (offer.internalId().equals(item.getInternalId())) {
        return;
      }
    }
    localOffers.add(new TradeOffer(item.getInternalId(), 1));
    localConfirmed = false;
    remoteConfirmed = false;

    int localId = gp.getNetworkManager().isHost() ? 0 : getLocalNetworkId();
    sendPacket(new TradePacket(
        TradePacket.ACTION_OFFER, localId, tradePartnerId,
        item.getInternalId(), 1));
  }

  /**
   * Remove um item da oferta local.
   *
   * @param offerIndex índice na lista de ofertas locais
   */
  public void removeOffer(int offerIndex) {
    if (offerIndex < 0 || offerIndex >= localOffers.size()) {
      return;
    }
    TradeOffer removed = localOffers.remove(offerIndex);
    localConfirmed = false;
    remoteConfirmed = false;

    int localId = gp.getNetworkManager().isHost() ? 0 : getLocalNetworkId();
    sendPacket(new TradePacket(
        TradePacket.ACTION_REMOVE, localId, tradePartnerId,
        removed.internalId(), removed.amount()));
  }

  /** Jogador local confirma a troca. */
  public void confirmTrade() {
    localConfirmed = true;
    int localId = gp.getNetworkManager().isHost() ? 0 : getLocalNetworkId();
    sendPacket(new TradePacket(
        TradePacket.ACTION_CONFIRM, localId, tradePartnerId, null, 0));
    if (localConfirmed && remoteConfirmed) {
      executeTrade();
    }
  }

  /** Jogador local cancela o trade. */
  public void cancelTrade() {
    if (tradePartnerId >= 0) {
      int localId = gp.getNetworkManager().isHost() ? 0 : getLocalNetworkId();
      sendPacket(new TradePacket(
          TradePacket.ACTION_CANCEL, localId, tradePartnerId, null, 0));
    }
    reset();
    gp.setGameState(GamePanel.PLAY_STATE);
  }

  // ---- Processamento de pacotes recebidos ----

  /**
   * Processa um pacote de trade recebido da rede.
   *
   * @param pkt pacote de trade recebido
   */
  public void handleTradePacket(TradePacket pkt) {
    int localId = gp.getNetworkManager().isHost() ? 0 : getLocalNetworkId();
    switch (pkt.getAction()) {
      case TradePacket.ACTION_REQUEST -> {
        if (tradePartnerId >= 0) {
          // Já em trade, recusa automaticamente
          sendPacket(new TradePacket(
              TradePacket.ACTION_DECLINE, localId, pkt.getSourcePlayerId(), null, 0));
          return;
        }
        pendingRequestFrom = pkt.getSourcePlayerId();
        gp.addUiMessage(getPlayerName(pkt.getSourcePlayerId())
            + " quer trocar! T=aceitar / ESC=recusar");
      }
      case TradePacket.ACTION_ACCEPT -> {
        startTrade(pkt.getSourcePlayerId());
      }
      case TradePacket.ACTION_DECLINE -> {
        pendingRequestFrom = -1;
        gp.addUiMessage("Troca recusada.");
      }
      case TradePacket.ACTION_OFFER -> {
        if (pkt.getSourcePlayerId() == tradePartnerId) {
          remoteOffers.add(new TradeOffer(pkt.getItemInternalId(), pkt.getAmount()));
          localConfirmed = false;
          remoteConfirmed = false;
        }
      }
      case TradePacket.ACTION_REMOVE -> {
        if (pkt.getSourcePlayerId() == tradePartnerId) {
          remoteOffers.removeIf(o -> o.internalId().equals(pkt.getItemInternalId()));
          localConfirmed = false;
          remoteConfirmed = false;
        }
      }
      case TradePacket.ACTION_CONFIRM -> {
        if (pkt.getSourcePlayerId() == tradePartnerId) {
          remoteConfirmed = true;
          if (localConfirmed && remoteConfirmed) {
            executeTrade();
          }
        }
      }
      case TradePacket.ACTION_CANCEL -> {
        if (pkt.getSourcePlayerId() == tradePartnerId) {
          gp.addUiMessage("Troca cancelada pelo outro jogador.");
          reset();
          gp.setGameState(GamePanel.PLAY_STATE);
        }
      }
      default -> { /* ação desconhecida */ }
    }
  }

  // ---- Execução da troca ----

  /**
   * Executa a troca: remove itens ofertados de cada inventário
   * e adiciona os itens recebidos.
   */
  private void executeTrade() {
    var player = gp.getPlayer();
    var inv = player.getInventory();

    // Remove itens que o jogador local ofereceu
    for (TradeOffer offer : localOffers) {
      for (int i = inv.size() - 1; i >= 0; i--) {
        if (inv.get(i).getInternalId().equals(offer.internalId())) {
          Entity item = inv.get(i);
          if (item.getAmount() > offer.amount()) {
            item.setAmount(item.getAmount() - offer.amount());
          } else {
            inv.remove(i);
          }
          break;
        }
      }
    }

    // Adiciona itens que o parceiro ofereceu
    for (TradeOffer offer : remoteOffers) {
      Entity newItem = gp.geteGenerator().getObject(offer.internalId());
      if (newItem != null) {
        newItem.setAmount(offer.amount());
        player.canObtainItem(newItem);
      }
    }

    gp.addUiMessage("Troca realizada com sucesso!");
    reset();
    gp.setGameState(GamePanel.PLAY_STATE);
  }

  /**
   * Chamado quando o parceiro de trade desconecta (ou reconexão é disparada).
   * Cancela qualquer trade ativo e volta ao PLAY_STATE.
   */
  public void onPartnerDisconnected() {
    if (tradePartnerId >= 0) {
      gp.addUiMessage("Troca cancelada — jogador desconectou.");
      reset();
      if (gp.getGameState() == GamePanel.PLAYER_TRADE_STATE) {
        gp.setGameState(GamePanel.PLAY_STATE);
      }
    }
    pendingRequestFrom = -1;
  }

  // ---- Helpers ----

  private void startTrade(int partnerId) {
    tradePartnerId = partnerId;
    localOffers.clear();
    remoteOffers.clear();
    localConfirmed = false;
    remoteConfirmed = false;
    pendingRequestFrom = -1;
    gp.setGameState(GamePanel.PLAYER_TRADE_STATE);
  }

  private void reset() {
    tradePartnerId = -1;
    localOffers.clear();
    remoteOffers.clear();
    localConfirmed = false;
    remoteConfirmed = false;
    pendingRequestFrom = -1;
  }

  private void sendPacket(TradePacket pkt) {
    if (gp.getNetworkManager().isHost()) {
      gp.getNetworkManager().broadcast(pkt);
    } else {
      gp.getNetworkManager().sendToServer(pkt);
    }
  }

  private RemotePlayer findNearestPlayer() {
    var player = gp.getPlayer();
    int px = player.getWorldX();
    int py = player.getWorldY();
    int threshold = GamePanel.TILE_SIZE * 2;
    RemotePlayer nearest = null;
    int minDist = Integer.MAX_VALUE;
    for (RemotePlayer rp : gp.getRemotePlayers()) {
      int dx = rp.getWorldX() - px;
      int dy = rp.getWorldY() - py;
      int dist = dx * dx + dy * dy;
      if (dist < minDist && dist < threshold * threshold) {
        minDist = dist;
        nearest = rp;
      }
    }
    return nearest;
  }

  private String getPlayerName(int networkId) {
    if (networkId == 0) {
      return "Host";
    }
    for (RemotePlayer rp : gp.getRemotePlayers()) {
      if (rp.getNetworkId() == networkId) {
        return rp.getPlayerName();
      }
    }
    return "Jogador #" + networkId;
  }

  private int getLocalNetworkId() {
    return gp.getLocalNetworkId();
  }

  private boolean isEquipped(Entity item) {
    var p = gp.getPlayer();
    return item == p.getCurrentWeapon()
        || item == p.getCurrentShield()
        || item == p.getCurrentBoots()
        || item == p.getCurrentHelmet()
        || item == p.getCurrentChest()
        || item == p.getCurrentLegs();
  }

  // ---- Getters para a UI ----

  public int getTradePartnerId()         { return tradePartnerId; }
  public List<TradeOffer> getLocalOffers()  { return localOffers; }
  public List<TradeOffer> getRemoteOffers() { return remoteOffers; }
  public boolean isLocalConfirmed()      { return localConfirmed; }
  public boolean isRemoteConfirmed()     { return remoteConfirmed; }
  public boolean hasPendingRequest()     { return pendingRequestFrom >= 0; }
  public int getPendingRequestFrom()     { return pendingRequestFrom; }

  public String getPartnerName() {
    return getPlayerName(tradePartnerId);
  }
}
