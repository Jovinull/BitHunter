package network;

/**
 * Envelope de pacote recebido pelo servidor, associando o pacote ao
 * {@link ClientHandler} que o enviou.
 * <p>
 * Instâncias são enfileiradas pelo thread de I/O e consumidas pelo game loop
 * via {@link NetworkServer#pollIncoming()}.
 */
public final class IncomingPacket {

  private final ClientHandler source;
  private final Packet packet;

  /**
   * @param source  handler do cliente que enviou o pacote
   * @param packet  pacote desserializado
   */
  public IncomingPacket(ClientHandler source, Packet packet) {
    this.source = source;
    this.packet = packet;
  }

  /** Handler do cliente remetente. */
  public ClientHandler getSource() {
    return source;
  }

  /** Pacote desserializado recebido. */
  public Packet getPacket() {
    return packet;
  }
}
