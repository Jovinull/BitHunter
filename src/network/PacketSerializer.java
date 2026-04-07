package network;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializa {@link Packet}s em um stream de saída usando o protocolo
 * binário do BitHunter.
 * <p>
 * Formato por pacote: {@code [1 byte: PacketType.id][N bytes: payload]}.
 * <p>
 * O método {@link #send(Packet)} é {@code synchronized} para permitir chamadas
 * de threads distintas no mesmo stream (ex.: game loop + thread de controle).
 */
public class PacketSerializer {

  private final DataOutputStream out;

  /**
   * Cria um serializador sobre o stream fornecido.
   * O stream é envolvido em {@link BufferedOutputStream} internamente para
   * minimizar syscalls — não envolva externamente.
   *
   * @param outputStream stream de destino (ex.: {@code socket.getOutputStream()})
   */
  public PacketSerializer(OutputStream outputStream) {
    this.out = new DataOutputStream(new BufferedOutputStream(outputStream));
  }

  /**
   * Serializa e envia um pacote de forma atômica.
   * Escreve o byte de tipo seguido do payload e realiza {@code flush()}.
   *
   * @param packet pacote a enviar
   * @throws IOException se ocorrer erro de I/O durante o envio
   */
  public synchronized void send(Packet packet) throws IOException {
    out.writeByte(packet.getType().getId());
    packet.write(out);
    out.flush();
  }
}
