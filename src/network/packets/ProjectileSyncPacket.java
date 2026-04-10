package network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import network.Packet;
import network.PacketType;

/**
 * Sincronização de projétil disparado por um jogador.
 * <p>
 * Payload (15 + idLen bytes):
 * {@code [4:ownerNetworkId][4:worldX][4:worldY][1:direction][2:idLen][utf8:projectileId]}.
 * <p>
 * {@code direction}: 0=up, 1=down, 2=left, 3=right.
 */
public final class ProjectileSyncPacket implements Packet {

  private final int ownerNetworkId;
  private final int worldX;
  private final int worldY;
  private final byte direction;
  private final String projectileId;

  public ProjectileSyncPacket(int ownerNetworkId, int worldX, int worldY,
      byte direction, String projectileId) {
    this.ownerNetworkId = ownerNetworkId;
    this.worldX = worldX;
    this.worldY = worldY;
    this.direction = direction;
    this.projectileId = projectileId;
  }

  @Override
  public PacketType getType() {
    return PacketType.PROJECTILE_SYNC;
  }

  @Override
  public void write(DataOutputStream out) throws IOException {
    out.writeInt(ownerNetworkId);
    out.writeInt(worldX);
    out.writeInt(worldY);
    out.writeByte(direction);
    byte[] idBytes = projectileId.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    out.writeShort(idBytes.length);
    out.write(idBytes);
  }

  /** Lê um ProjectileSyncPacket do stream (sem o byte de tipo). */
  public static ProjectileSyncPacket read(DataInputStream in) throws IOException {
    int ownerId = in.readInt();
    int wx = in.readInt();
    int wy = in.readInt();
    byte dir = in.readByte();
    int idLen = in.readUnsignedShort();
    byte[] idBytes = new byte[idLen];
    in.readFully(idBytes);
    String projId = new String(idBytes, java.nio.charset.StandardCharsets.UTF_8);
    return new ProjectileSyncPacket(ownerId, wx, wy, dir, projId);
  }

  public int getOwnerNetworkId() { return ownerNetworkId; }
  public int getWorldX() { return worldX; }
  public int getWorldY() { return worldY; }
  public byte getDirection() { return direction; }
  public String getProjectileId() { return projectileId; }

  /** Converte string de direção para byte de protocolo. */
  public static byte directionToByte(String dir) {
    return switch (dir) {
      case "up" -> (byte) 0;
      case "down" -> (byte) 1;
      case "left" -> (byte) 2;
      case "right" -> (byte) 3;
      default -> (byte) 0;
    };
  }

  /** Converte byte de protocolo para string de direção. */
  public static String byteToDirection(byte b) {
    return switch (b) {
      case 0 -> "up";
      case 1 -> "down";
      case 2 -> "left";
      case 3 -> "right";
      default -> "up";
    };
  }
}
