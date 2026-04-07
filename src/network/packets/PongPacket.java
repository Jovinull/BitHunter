package network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import network.Packet;
import network.PacketType;

/**
 * Resposta ao {@link PingPacket}, ecoa o timestamp original para cálculo de RTT.
 * <p>
 * Payload (8 bytes): {@code [8:pingTimestamp]}.
 * <p>
 * RTT em nanosegundos = {@code System.nanoTime() - pong.getPingTimestamp()}.
 */
public final class PongPacket implements Packet {

  private final long pingTimestamp;

  /**
   * @param pingTimestamp timestamp original do {@link PingPacket} que originou este pong
   */
  public PongPacket(long pingTimestamp) {
    this.pingTimestamp = pingTimestamp;
  }

  /** Cria um PongPacket a partir do PingPacket recebido. */
  public static PongPacket replyTo(PingPacket ping) {
    return new PongPacket(ping.getTimestamp());
  }

  @Override
  public PacketType getType() {
    return PacketType.PONG;
  }

  @Override
  public void write(DataOutputStream out) throws IOException {
    out.writeLong(pingTimestamp);
  }

  /** Lê um PongPacket do stream (sem o byte de tipo). */
  public static PongPacket read(DataInputStream in) throws IOException {
    return new PongPacket(in.readLong());
  }

  /** Timestamp original do ping, para cálculo de RTT. */
  public long getPingTimestamp() {
    return pingTimestamp;
  }

  /**
   * Calcula o RTT aproximado em nanosegundos.
   * Deve ser chamado imediatamente após receber este pacote.
   */
  public long getRoundTripNanos() {
    return System.nanoTime() - pingTimestamp;
  }

  /** RTT em milissegundos (inteiro). */
  public long getRoundTripMs() {
    return getRoundTripNanos() / 1_000_000L;
  }
}
