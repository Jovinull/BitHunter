package network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import network.Packet;
import network.PacketType;

/**
 * Evento pontual de jogo entre entidades.
 * <p>
 * Payload (14 bytes):
 * {@code [1:eventType][4:sourceEntityId][4:targetEntityId][4:value][1:mapIndex]}.
 * <p>
 * Semântica do campo {@code value} varia por {@code eventType}:
 * <ul>
 *   <li>{@link #EVT_DAMAGE} / {@link #EVT_HEAL} — quantidade de pontos</li>
 *   <li>{@link #EVT_ITEM_PICKUP} — slot do item no array {@code obj[]}</li>
 *   <li>{@link #EVT_MAP_CHANGE} — índice do mapa de destino</li>
 *   <li>{@link #EVT_LEVEL_UP} — novo nível</li>
 *   <li>demais — reservado (0)</li>
 * </ul>
 */
public final class EventPacket implements Packet {

  // ---- Tipos de evento ----
  /** Entidade {@code source} causou {@code value} pontos de dano em {@code target}. */
  public static final byte EVT_DAMAGE       = 0x01;

  /** Entidade {@code source} curou {@code value} pontos de vida em {@code target}. */
  public static final byte EVT_HEAL         = 0x02;

  /** Entidade {@code target} morreu. */
  public static final byte EVT_DEATH        = 0x03;

  /** Jogador {@code source} coletou o item de slot {@code value} no mapa {@code mapIndex}. */
  public static final byte EVT_ITEM_PICKUP  = 0x04;

  /** Entidade {@code source} mudou para o mapa {@code value}. */
  public static final byte EVT_MAP_CHANGE   = 0x05;

  /** Entidade {@code source} iniciou um ataque (para sinc. de animação). */
  public static final byte EVT_ATTACK_START = 0x06;

  /** Jogador {@code source} subiu para o nível {@code value}. */
  public static final byte EVT_LEVEL_UP     = 0x07;

  /**
   * Servidor atribuiu o ID de rede {@code value} ao cliente {@code sourceEntityId}.
   * Enviado pelo host imediatamente após processar o handshake do cliente.
   */
  public static final byte EVT_PLAYER_ASSIGN = 0x08;

  /**
   * Host removeu a entidade de id {@code value} (slot {@code value-base} do
   * array correspondente). Cliente deve nulificar o slot ao receber.
   */
  public static final byte EVT_ENTITY_REMOVE = 0x09;

  /** Host iniciou a partida — clientes saem do lobby para PLAY_STATE. */
  public static final byte EVT_GAME_START = 0x0A;

  /**
   * Host notifica que o jogador {@code source} está em cutscene {@code value}
   * (0 = saiu da cutscene). Apenas jogadores no mesmo mapa e próximos recebem.
   */
  public static final byte EVT_CUTSCENE = 0x0B;

  // ---- Campos ----
  private final byte eventType;
  private final int sourceEntityId;
  private final int targetEntityId;
  private final int value;
  private final byte mapIndex;

  /**
   * @param eventType      tipo do evento (usar constantes {@code EVT_*})
   * @param sourceEntityId id da entidade causadora
   * @param targetEntityId id da entidade afetada (0 se não aplicável)
   * @param value          valor numérico do evento (semântica depende do tipo)
   * @param mapIndex       mapa onde o evento ocorreu
   */
  public EventPacket(byte eventType, int sourceEntityId, int targetEntityId,
      int value, byte mapIndex) {
    this.eventType = eventType;
    this.sourceEntityId = sourceEntityId;
    this.targetEntityId = targetEntityId;
    this.value = value;
    this.mapIndex = mapIndex;
  }

  @Override
  public PacketType getType() {
    return PacketType.EVENT;
  }

  @Override
  public void write(DataOutputStream out) throws IOException {
    out.writeByte(eventType);
    out.writeInt(sourceEntityId);
    out.writeInt(targetEntityId);
    out.writeInt(value);
    out.writeByte(mapIndex);
  }

  /** Lê um EventPacket do stream (sem o byte de tipo). */
  public static EventPacket read(DataInputStream in) throws IOException {
    byte evtType  = in.readByte();
    int sourceId  = in.readInt();
    int targetId  = in.readInt();
    int val       = in.readInt();
    byte mapIdx   = in.readByte();
    return new EventPacket(evtType, sourceId, targetId, val, mapIdx);
  }

  // ---- Getters ----

  public byte getEventType()      { return eventType; }
  public int getSourceEntityId()  { return sourceEntityId; }
  public int getTargetEntityId()  { return targetEntityId; }
  public int getValue()           { return value; }
  public byte getMapIndex()       { return mapIndex; }
}
