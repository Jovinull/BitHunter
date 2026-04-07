package network;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Contrato base de todos os pacotes de rede do BitHunter.
 * <p>
 * Cada implementação é responsável por serializar seu próprio payload
 * via {@link #write(DataOutputStream)}. O byte de tipo é escrito pelo
 * {@link PacketSerializer} antes de chamar este método.
 */
public interface Packet {

  /** Retorna o tipo deste pacote (usado para serialização do cabeçalho). */
  PacketType getType();

  /**
   * Serializa o payload deste pacote no stream de saída.
   * <p>
   * <strong>Não</strong> deve escrever o byte de tipo — isso é responsabilidade
   * do {@link PacketSerializer}.
   *
   * @param out stream de saída já posicionado após o byte de tipo
   * @throws IOException se ocorrer erro de I/O
   */
  void write(DataOutputStream out) throws IOException;
}
