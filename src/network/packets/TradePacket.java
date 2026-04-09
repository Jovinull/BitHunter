package network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import network.Packet;
import network.PacketType;

/**
 * Pacote de trade P2P entre jogadores.
 * <p>
 * Payload:
 * {@code [1:action][4:sourcePlayerId][4:targetPlayerId][2:idLen][utf8:itemInternalId][4:amount]}.
 * <p>
 * Ações:
 * <ul>
 *   <li>{@link #ACTION_REQUEST} — jogador solicita trade</li>
 *   <li>{@link #ACTION_ACCEPT} — jogador aceita a solicitação</li>
 *   <li>{@link #ACTION_DECLINE} — jogador recusa a solicitação</li>
 *   <li>{@link #ACTION_OFFER} — jogador oferece um item na mesa de trade</li>
 *   <li>{@link #ACTION_REMOVE} — jogador remove item da oferta</li>
 *   <li>{@link #ACTION_CONFIRM} — jogador confirma a troca</li>
 *   <li>{@link #ACTION_CANCEL} — jogador cancela o trade</li>
 * </ul>
 */
public final class TradePacket implements Packet {

  // ---- Ações ----
  /** Solicitar trade com outro jogador. */
  public static final byte ACTION_REQUEST = 0x01;
  /** Aceitar solicitação de trade. */
  public static final byte ACTION_ACCEPT = 0x02;
  /** Recusar solicitação de trade. */
  public static final byte ACTION_DECLINE = 0x03;
  /** Oferecer item na mesa de trade. */
  public static final byte ACTION_OFFER = 0x04;
  /** Remover item da oferta. */
  public static final byte ACTION_REMOVE = 0x05;
  /** Confirmar a troca (ambos precisam confirmar). */
  public static final byte ACTION_CONFIRM = 0x06;
  /** Cancelar o trade. */
  public static final byte ACTION_CANCEL = 0x07;

  // ---- Campos ----
  private final byte action;
  private final int sourcePlayerId;
  private final int targetPlayerId;
  private final String itemInternalId;
  private final int amount;

  public TradePacket(byte action, int sourcePlayerId, int targetPlayerId,
      String itemInternalId, int amount) {
    this.action = action;
    this.sourcePlayerId = sourcePlayerId;
    this.targetPlayerId = targetPlayerId;
    this.itemInternalId = itemInternalId != null ? itemInternalId : "";
    this.amount = amount;
  }

  @Override
  public PacketType getType() {
    return PacketType.TRADE;
  }

  @Override
  public void write(DataOutputStream out) throws IOException {
    out.writeByte(action);
    out.writeInt(sourcePlayerId);
    out.writeInt(targetPlayerId);
    byte[] idBytes = itemInternalId.getBytes(StandardCharsets.UTF_8);
    out.writeShort(idBytes.length);
    out.write(idBytes);
    out.writeInt(amount);
  }

  /** Lê um TradePacket do stream (sem o byte de tipo). */
  public static TradePacket read(DataInputStream in) throws IOException {
    byte act = in.readByte();
    int srcId = in.readInt();
    int tgtId = in.readInt();
    int idLen = in.readShort() & 0xFFFF;
    byte[] idBytes = new byte[idLen];
    in.readFully(idBytes);
    String itemId = new String(idBytes, StandardCharsets.UTF_8);
    int amt = in.readInt();
    return new TradePacket(act, srcId, tgtId, itemId, amt);
  }

  // ---- Getters ----

  public byte getAction()         { return action; }
  public int getSourcePlayerId()  { return sourcePlayerId; }
  public int getTargetPlayerId()  { return targetPlayerId; }
  public String getItemInternalId() { return itemInternalId; }
  public int getAmount()          { return amount; }
}
