package network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import network.Packet;
import network.PacketType;

/**
 * Pacote de medição de latência enviado periodicamente pelo servidor.
 * <p>
 * Payload (8 bytes): {@code [8:timestamp]} — valor de {@link System#nanoTime()}
 * no momento do envio, usado para calcular o RTT quando o {@link PongPacket}
 * retornar.
 */
public final class PingPacket implements Packet {

  private final long timestamp;

  /** Cria um PingPacket com timestamp atual ({@link System#nanoTime()}). */
  public PingPacket() {
    this(System.nanoTime());
  }

  /**
   * @param timestamp timestamp em nanoTime() no momento do envio
   */
  public PingPacket(long timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public PacketType getType() {
    return PacketType.PING;
  }

  @Override
  public void write(DataOutputStream out) throws IOException {
    out.writeLong(timestamp);
  }

  /** Lê um PingPacket do stream (sem o byte de tipo). */
  public static PingPacket read(DataInputStream in) throws IOException {
    return new PingPacket(in.readLong());
  }

  /** Timestamp {@link System#nanoTime()} do momento do envio. */
  public long getTimestamp() {
    return timestamp;
  }
}
