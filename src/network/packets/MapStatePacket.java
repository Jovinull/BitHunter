package network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import network.Packet;
import network.PacketType;

/**
 * Snapshot completo do estado de entidades de um mapa.
 * <p>
 * Payload: {@code [1:mapIndex][2:entityCount][entityState × N]}.
 * <p>
 * Enviado pelo servidor ao cliente nas seguintes situações:
 * <ul>
 *   <li>Logo após o handshake (sincronização inicial do mapa atual)</li>
 *   <li>Após mudança de mapa do cliente (carrega estado do novo mapa)</li>
 * </ul>
 * Para atualizações contínuas de posição, use {@link EntityStatePacket}
 * individualmente a ~20 Hz.
 */
public final class MapStatePacket implements Packet {

  private final byte mapIndex;
  private final List<EntityStatePacket> entityStates;

  /**
   * @param mapIndex     índice do mapa cujo estado está sendo enviado
   * @param entityStates lista de estados das entidades presentes no mapa
   */
  public MapStatePacket(byte mapIndex, List<EntityStatePacket> entityStates) {
    this.mapIndex = mapIndex;
    this.entityStates = Collections.unmodifiableList(new ArrayList<>(entityStates));
  }

  @Override
  public PacketType getType() {
    return PacketType.MAP_STATE;
  }

  @Override
  public void write(DataOutputStream out) throws IOException {
    out.writeByte(mapIndex);
    out.writeShort(entityStates.size());
    for (EntityStatePacket esp : entityStates) {
      esp.write(out);
    }
  }

  /** Lê um MapStatePacket do stream (sem o byte de tipo). */
  public static MapStatePacket read(DataInputStream in) throws IOException {
    byte mapIdx = in.readByte();
    int count = in.readShort() & 0xFFFF;
    List<EntityStatePacket> states = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      states.add(EntityStatePacket.read(in));
    }
    return new MapStatePacket(mapIdx, states);
  }

  public byte getMapIndex() {
    return mapIndex;
  }

  public List<EntityStatePacket> getEntityStates() {
    return entityStates;
  }
}
