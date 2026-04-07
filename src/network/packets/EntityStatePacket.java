package network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import network.Packet;
import network.PacketType;

/**
 * Estado sincronizável de uma entidade do jogo.
 * <p>
 * Payload (20 bytes):
 * {@code [4:entityId][1:mapIndex][4:worldX][4:worldY][1:dirCode][1:spriteNum][2:life][2:maxLife][1:flags]}.
 * <p>
 * Flags (1 byte, LSB first):
 * <ul>
 *   <li>bit 0 — atacando</li>
 *   <li>bit 1 — vivo</li>
 *   <li>bit 2 — morrendo (animação)</li>
 *   <li>bit 3 — invencível</li>
 *   <li>bit 4 — guardando</li>
 * </ul>
 * <p>
 * Direções são codificadas como bytes para economizar espaço:
 * 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT.
 */
public final class EntityStatePacket implements Packet {

  // ---- Códigos de direção ----
  public static final byte DIR_CODE_UP    = 0;
  public static final byte DIR_CODE_DOWN  = 1;
  public static final byte DIR_CODE_LEFT  = 2;
  public static final byte DIR_CODE_RIGHT = 3;

  // ---- Máscaras de flags ----
  private static final byte FLAG_ATTACKING  = 0x01;
  private static final byte FLAG_ALIVE      = 0x02;
  private static final byte FLAG_DYING      = 0x04;
  private static final byte FLAG_INVINCIBLE = 0x08;
  private static final byte FLAG_GUARDING   = 0x10;

  private final int entityId;
  private final byte mapIndex;
  private final int worldX;
  private final int worldY;
  private final byte directionCode;
  private final byte spriteNum;
  private final short life;
  private final short maxLife;
  private final byte flags;

  /**
   * Cria um EntityStatePacket a partir dos campos individuais de estado.
   *
   * @param entityId      identificador único da entidade no mapa
   * @param mapIndex      índice do mapa onde a entidade está
   * @param worldX        posição X no mundo (pixels)
   * @param worldY        posição Y no mundo (pixels)
   * @param directionCode código de direção (usar {@link #encodeDirection(String)})
   * @param spriteNum     número do frame de sprite atual
   * @param life          pontos de vida atuais
   * @param maxLife       pontos de vida máximos
   * @param attacking     se está executando animação de ataque
   * @param alive         se está vivo
   * @param dying         se está na animação de morte
   * @param invincible    se está invencível (pós-dano)
   * @param guarding      se está bloqueando com escudo
   */
  public EntityStatePacket(int entityId, byte mapIndex, int worldX, int worldY,
      byte directionCode, byte spriteNum, short life, short maxLife,
      boolean attacking, boolean alive, boolean dying,
      boolean invincible, boolean guarding) {
    this.entityId = entityId;
    this.mapIndex = mapIndex;
    this.worldX = worldX;
    this.worldY = worldY;
    this.directionCode = directionCode;
    this.spriteNum = spriteNum;
    this.life = life;
    this.maxLife = maxLife;
    // Compacta os booleanos em 1 byte
    byte f = 0;
    if (attacking)  f |= FLAG_ATTACKING;
    if (alive)      f |= FLAG_ALIVE;
    if (dying)      f |= FLAG_DYING;
    if (invincible) f |= FLAG_INVINCIBLE;
    if (guarding)   f |= FLAG_GUARDING;
    this.flags = f;
  }

  /** Construtor interno para desserialização (flags já compactadas). */
  private EntityStatePacket(int entityId, byte mapIndex, int worldX, int worldY,
      byte directionCode, byte spriteNum, short life, short maxLife, byte flags) {
    this.entityId = entityId;
    this.mapIndex = mapIndex;
    this.worldX = worldX;
    this.worldY = worldY;
    this.directionCode = directionCode;
    this.spriteNum = spriteNum;
    this.life = life;
    this.maxLife = maxLife;
    this.flags = flags;
  }

  @Override
  public PacketType getType() {
    return PacketType.ENTITY_STATE;
  }

  @Override
  public void write(DataOutputStream out) throws IOException {
    out.writeInt(entityId);
    out.writeByte(mapIndex);
    out.writeInt(worldX);
    out.writeInt(worldY);
    out.writeByte(directionCode);
    out.writeByte(spriteNum);
    out.writeShort(life);
    out.writeShort(maxLife);
    out.writeByte(flags);
  }

  /** Lê um EntityStatePacket do stream (sem o byte de tipo). */
  public static EntityStatePacket read(DataInputStream in) throws IOException {
    int entityId    = in.readInt();
    byte mapIndex   = in.readByte();
    int worldX      = in.readInt();
    int worldY      = in.readInt();
    byte dirCode    = in.readByte();
    byte spriteNum  = in.readByte();
    short life      = in.readShort();
    short maxLife   = in.readShort();
    byte flags      = in.readByte();
    return new EntityStatePacket(entityId, mapIndex, worldX, worldY,
        dirCode, spriteNum, life, maxLife, flags);
  }

  // ---- Helpers de direção ----

  /**
   * Converte a string de direção usada por {@link entity.Entity} para código de byte.
   *
   * @param dir "up", "down", "left" ou "right"
   * @return código de byte correspondente
   */
  public static byte encodeDirection(String dir) {
    return switch (dir) {
      case "up"    -> DIR_CODE_UP;
      case "left"  -> DIR_CODE_LEFT;
      case "right" -> DIR_CODE_RIGHT;
      default      -> DIR_CODE_DOWN;
    };
  }

  /**
   * Converte um código de byte de volta para a string de direção de {@link entity.Entity}.
   *
   * @param code código de byte
   * @return string de direção
   */
  public static String decodeDirection(byte code) {
    return switch (code) {
      case DIR_CODE_UP    -> "up";
      case DIR_CODE_LEFT  -> "left";
      case DIR_CODE_RIGHT -> "right";
      default             -> "down";
    };
  }

  // ---- Getters ----

  public int getEntityId()       { return entityId; }
  public byte getMapIndex()      { return mapIndex; }
  public int getWorldX()         { return worldX; }
  public int getWorldY()         { return worldY; }
  public byte getDirectionCode() { return directionCode; }

  /** Direção decodificada como string compatível com {@link entity.Entity#getDirection()}. */
  public String getDirection()   { return decodeDirection(directionCode); }

  public byte getSpriteNum()     { return spriteNum; }
  public short getLife()         { return life; }
  public short getMaxLife()      { return maxLife; }

  public boolean isAttacking()   { return (flags & FLAG_ATTACKING)  != 0; }
  public boolean isAlive()       { return (flags & FLAG_ALIVE)      != 0; }
  public boolean isDying()       { return (flags & FLAG_DYING)      != 0; }
  public boolean isInvincible()  { return (flags & FLAG_INVINCIBLE) != 0; }
  public boolean isGuarding()    { return (flags & FLAG_GUARDING)   != 0; }
}
