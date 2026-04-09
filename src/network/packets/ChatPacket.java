package network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import network.Packet;
import network.PacketType;

/**
 * Mensagem de chat entre jogadores.
 * <p>
 * Payload: {@code [4:senderNetworkId][2:msgLen][utf8:message]}.
 */
public final class ChatPacket implements Packet {

  /** Tamanho máximo da mensagem em caracteres UTF-8. */
  public static final int MAX_MESSAGE_LENGTH = 200;

  private final int senderNetworkId;
  private final String message;

  public ChatPacket(int senderNetworkId, String message) {
    this.senderNetworkId = senderNetworkId;
    this.message = message.length() > MAX_MESSAGE_LENGTH
        ? message.substring(0, MAX_MESSAGE_LENGTH)
        : message;
  }

  @Override
  public PacketType getType() {
    return PacketType.CHAT;
  }

  @Override
  public void write(DataOutputStream out) throws IOException {
    out.writeInt(senderNetworkId);
    byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
    out.writeShort(msgBytes.length);
    out.write(msgBytes);
  }

  /** Lê um ChatPacket do stream (sem o byte de tipo). */
  public static ChatPacket read(DataInputStream in) throws IOException {
    int senderId = in.readInt();
    int msgLen = in.readShort() & 0xFFFF;
    byte[] msgBytes = new byte[msgLen];
    in.readFully(msgBytes);
    String msg = new String(msgBytes, StandardCharsets.UTF_8);
    return new ChatPacket(senderId, msg);
  }

  // ---- Getters ----

  public int getSenderNetworkId() { return senderNetworkId; }
  public String getMessage()      { return message; }
}
