package network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import network.Packet;
import network.PacketType;

/**
 * Notificação de desconexão com motivo textual.
 * <p>
 * Payload: {@code [2:reasonLen][utf8:reason]}.
 * <p>
 * Enviado antes de fechar o socket para que o outro lado possa exibir
 * uma mensagem amigável ao jogador (ex.: "Servidor cheio",
 * "Versão de protocolo incompatível", "Host encerrou a sessão").
 */
public final class DisconnectPacket implements Packet {

  /** Motivo padrão quando nenhum motivo específico é fornecido. */
  public static final String REASON_GENERIC          = "Desconectado";
  public static final String REASON_SERVER_FULL      = "Servidor cheio";
  public static final String REASON_BAD_VERSION      = "Versão de protocolo incompatível";
  public static final String REASON_HOST_CLOSED      = "O host encerrou a sessão";
  public static final String REASON_KICKED           = "Você foi removido da sessão";

  private final String reason;

  /**
   * @param reason mensagem de motivo exibida ao jogador desconectado
   */
  public DisconnectPacket(String reason) {
    this.reason = reason;
  }

  /** Cria um DisconnectPacket com o motivo padrão genérico. */
  public DisconnectPacket() {
    this(REASON_GENERIC);
  }

  @Override
  public PacketType getType() {
    return PacketType.DISCONNECT;
  }

  @Override
  public void write(DataOutputStream out) throws IOException {
    byte[] bytes = reason.getBytes(StandardCharsets.UTF_8);
    out.writeShort(bytes.length);
    out.write(bytes);
  }

  /** Lê um DisconnectPacket do stream (sem o byte de tipo). */
  public static DisconnectPacket read(DataInputStream in) throws IOException {
    int len = in.readShort() & 0xFFFF;
    byte[] bytes = new byte[len];
    in.readFully(bytes);
    return new DisconnectPacket(new String(bytes, StandardCharsets.UTF_8));
  }

  /** Motivo da desconexão para exibição ao jogador. */
  public String getReason() {
    return reason;
  }
}
