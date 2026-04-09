package network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import network.Packet;
import network.PacketType;

/**
 * Spawn autoritativo de um item no mundo (drop de monstro, etc.).
 * <p>
 * Payload: {@code [1:mapIdx][2:slot][4:worldX][4:worldY][2:idLen][utf8:internalId]}.
 * <p>
 * O cliente recebe e instancia o item via {@code EntityGenerator.getObject(internalId)},
 * posicionando-o no slot correspondente do array {@code obj[mapIdx]}.
 */
public final class ItemSpawnPacket implements Packet {

  private final byte mapIndex;
  private final short slot;
  private final int worldX;
  private final int worldY;
  private final String internalId;

  public ItemSpawnPacket(byte mapIndex, short slot, int worldX, int worldY, String internalId) {
    this.mapIndex = mapIndex;
    this.slot = slot;
    this.worldX = worldX;
    this.worldY = worldY;
    this.internalId = internalId;
  }

  @Override
  public PacketType getType() {
    return PacketType.ITEM_SPAWN;
  }

  @Override
  public void write(DataOutputStream out) throws IOException {
    out.writeByte(mapIndex);
    out.writeShort(slot);
    out.writeInt(worldX);
    out.writeInt(worldY);
    byte[] idBytes = internalId.getBytes(StandardCharsets.UTF_8);
    out.writeShort(idBytes.length);
    out.write(idBytes);
  }

  /** Lê um ItemSpawnPacket do stream (sem o byte de tipo). */
  public static ItemSpawnPacket read(DataInputStream in) throws IOException {
    byte mapIdx = in.readByte();
    short slt = in.readShort();
    int wx = in.readInt();
    int wy = in.readInt();
    int idLen = in.readShort() & 0xFFFF;
    byte[] idBytes = new byte[idLen];
    in.readFully(idBytes);
    String id = new String(idBytes, StandardCharsets.UTF_8);
    return new ItemSpawnPacket(mapIdx, slt, wx, wy, id);
  }

  // ---- Getters ----

  public byte getMapIndex()    { return mapIndex; }
  public short getSlot()       { return slot; }
  public int getWorldX()       { return worldX; }
  public int getWorldY()       { return worldY; }
  public String getInternalId() { return internalId; }
}
