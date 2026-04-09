package network;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import network.packets.DisconnectPacket;
import network.packets.EntityStatePacket;
import network.packets.EventPacket;
import network.packets.HandshakePacket;
import network.packets.InputPacket;
import network.packets.ItemSpawnPacket;
import network.packets.MapStatePacket;
import network.packets.PingPacket;
import network.packets.PongPacket;
import network.packets.ChatPacket;
import network.packets.PetSyncPacket;
import network.packets.TradePacket;

/**
 * Lê e desserializa {@link Packet}s do protocolo binário do BitHunter.
 * <p>
 * Bloqueia em {@link #receive()} até que um pacote completo esteja disponível.
 * Projetado para uso exclusivo em uma única thread de leitura.
 */
public class PacketDeserializer {

  private final DataInputStream in;

  /**
   * Cria um desserializador sobre o stream fornecido.
   * O stream é envolvido em {@link BufferedInputStream} internamente.
   *
   * @param inputStream stream de origem (ex.: {@code socket.getInputStream()})
   */
  public PacketDeserializer(InputStream inputStream) {
    this.in = new DataInputStream(new BufferedInputStream(inputStream));
  }

  /**
   * Bloqueia até receber um pacote completo e o retorna desserializado.
   *
   * @return pacote recebido (nunca {@code null})
   * @throws IOException se o stream fechar ou ocorrer erro de I/O
   * @throws IllegalArgumentException se o byte de tipo for desconhecido
   */
  public Packet receive() throws IOException {
    byte typeId = in.readByte();
    PacketType type = PacketType.fromId(typeId);
    return switch (type) {
      case HANDSHAKE    -> HandshakePacket.read(in);
      case INPUT        -> InputPacket.read(in);
      case ENTITY_STATE -> EntityStatePacket.read(in);
      case EVENT        -> EventPacket.read(in);
      case MAP_STATE    -> MapStatePacket.read(in);
      case PING         -> PingPacket.read(in);
      case PONG         -> PongPacket.read(in);
      case DISCONNECT   -> DisconnectPacket.read(in);
      case ITEM_SPAWN   -> ItemSpawnPacket.read(in);
      case TRADE        -> TradePacket.read(in);
      case CHAT         -> ChatPacket.read(in);
      case PET_SYNC     -> PetSyncPacket.read(in);
    };
  }
}
