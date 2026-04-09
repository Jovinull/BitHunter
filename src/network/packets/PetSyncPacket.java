package network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import network.Packet;
import network.PacketType;

/**
 * Sincroniza o pet ativo de um jogador.
 * <p>
 * Payload: {@code [4:ownerNetworkId][4:petWorldX][4:petWorldY][2:idLen][utf8:petInternalId]}.
 * Se {@code petInternalId} for vazio, o jogador não tem pet ativo.
 */
public final class PetSyncPacket implements Packet {

  private final int ownerNetworkId;
  private final int petWorldX;
  private final int petWorldY;
  private final String petInternalId;

  public PetSyncPacket(int ownerNetworkId, int petWorldX, int petWorldY,
      String petInternalId) {
    this.ownerNetworkId = ownerNetworkId;
    this.petWorldX = petWorldX;
    this.petWorldY = petWorldY;
    this.petInternalId = petInternalId != null ? petInternalId : "";
  }

  @Override
  public PacketType getType() {
    return PacketType.PET_SYNC;
  }

  @Override
  public void write(DataOutputStream out) throws IOException {
    out.writeInt(ownerNetworkId);
    out.writeInt(petWorldX);
    out.writeInt(petWorldY);
    byte[] idBytes = petInternalId.getBytes(StandardCharsets.UTF_8);
    out.writeShort(idBytes.length);
    out.write(idBytes);
  }

  /** Lê um PetSyncPacket do stream (sem o byte de tipo). */
  public static PetSyncPacket read(DataInputStream in) throws IOException {
    int ownerId = in.readInt();
    int wx = in.readInt();
    int wy = in.readInt();
    int idLen = in.readShort() & 0xFFFF;
    byte[] idBytes = new byte[idLen];
    in.readFully(idBytes);
    String petId = new String(idBytes, StandardCharsets.UTF_8);
    return new PetSyncPacket(ownerId, wx, wy, petId);
  }

  // ---- Getters ----

  public int getOwnerNetworkId()  { return ownerNetworkId; }
  public int getPetWorldX()       { return petWorldX; }
  public int getPetWorldY()       { return petWorldY; }
  public String getPetInternalId() { return petInternalId; }
}
