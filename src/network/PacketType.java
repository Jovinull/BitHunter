package network;

/**
 * Tipos de pacotes trafegados pelo protocolo de rede do BitHunter.
 * <p>
 * O ordinal do enum é usado como identificador de 1 byte no protocolo binário.
 * A ordem dos valores <strong>não pode ser alterada</strong> sem incrementar
 * {@link network.packets.HandshakePacket#PROTOCOL_VERSION}.
 */
public enum PacketType {

  /** Handshake inicial: versão de protocolo, nome e classe do jogador. */
  HANDSHAKE,

  /** Input do jogador: teclas pressionadas naquele frame. */
  INPUT,

  /** Estado de uma entidade: posição, direção, vida, flags de animação. */
  ENTITY_STATE,

  /** Evento pontual de jogo: dano, cura, morte, mudança de mapa, etc. */
  EVENT,

  /** Snapshot de todas as entidades de um mapa (sincronização inicial). */
  MAP_STATE,

  /** Ping de latência: timestamp do remetente. */
  PING,

  /** Pong de latência: eco do timestamp do ping. */
  PONG,

  /** Notificação de desconexão com motivo textual. */
  DISCONNECT,

  /** Spawn autoritativo de item no mundo (drop de monstro, etc.). */
  ITEM_SPAWN,

  /** Trade P2P entre jogadores: solicitação, oferta, confirmação. */
  TRADE,

  /** Mensagem de chat entre jogadores. */
  CHAT,

  /** Sincronização de pet ativo de um jogador. */
  PET_SYNC;

  /**
   * Converte um byte de protocolo no enum correspondente.
   *
   * @param id byte lido do stream
   * @return PacketType correspondente
   * @throws IllegalArgumentException se o id for desconhecido
   */
  public static PacketType fromId(byte id) {
    PacketType[] values = values();
    // id é assinado em Java; nosso protocolo usa 0..N-1 (N <= 8), logo nunca negativo
    if (id < 0 || id >= values.length) {
      throw new IllegalArgumentException("ID de pacote desconhecido: " + id);
    }
    return values[id];
  }

  /** Retorna o identificador de 1 byte usado no protocolo binário. */
  public byte getId() {
    return (byte) ordinal();
  }
}
