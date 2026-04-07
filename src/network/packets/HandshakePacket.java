package network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import network.Packet;
import network.PacketType;

/**
 * Pacote de handshake enviado pelo cliente logo após a conexão TCP.
 * <p>
 * Payload: {@code [2:protocolVersion][1:characterClass][2:nameLen][utf8:playerName]}.
 * <p>
 * Se {@link #isVersionCompatible()} retornar {@code false}, o servidor deve
 * encerrar a conexão com {@link DisconnectPacket}.
 */
public final class HandshakePacket implements Packet {

  /** Versão atual do protocolo — incrementar ao alterar formato de qualquer pacote. */
  public static final short PROTOCOL_VERSION = 1;

  private final short protocolVersion;
  private final String playerName;
  private final byte characterClass;

  /**
   * Cria handshake com a versão de protocolo atual.
   *
   * @param playerName     nome do jogador (máx. 32 UTF-8 chars recomendado)
   * @param characterClass índice da classe (0=Guerreiro, 1=Ladrão, 2=Feiticeiro)
   */
  public HandshakePacket(String playerName, byte characterClass) {
    this.protocolVersion = PROTOCOL_VERSION;
    this.playerName = playerName;
    this.characterClass = characterClass;
  }

  private HandshakePacket(short protocolVersion, String playerName, byte characterClass) {
    this.protocolVersion = protocolVersion;
    this.playerName = playerName;
    this.characterClass = characterClass;
  }

  @Override
  public PacketType getType() {
    return PacketType.HANDSHAKE;
  }

  @Override
  public void write(DataOutputStream out) throws IOException {
    out.writeShort(protocolVersion);
    out.writeByte(characterClass);
    writeString(out, playerName);
  }

  /** Lê um HandshakePacket do stream (sem o byte de tipo). */
  public static HandshakePacket read(DataInputStream in) throws IOException {
    short version = in.readShort();
    byte charClass = in.readByte();
    String name = readString(in);
    return new HandshakePacket(version, name, charClass);
  }

  // ---- Helpers de string (2 bytes comprimento + UTF-8) ----

  private static void writeString(DataOutputStream out, String s) throws IOException {
    byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
    out.writeShort(bytes.length);
    out.write(bytes);
  }

  private static String readString(DataInputStream in) throws IOException {
    int len = in.readShort() & 0xFFFF;
    byte[] bytes = new byte[len];
    in.readFully(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  // ---- Getters ----

  public short getProtocolVersion() {
    return protocolVersion;
  }

  public String getPlayerName() {
    return playerName;
  }

  public byte getCharacterClass() {
    return characterClass;
  }

  /** Verifica se a versão do cliente é compatível com este servidor. */
  public boolean isVersionCompatible() {
    return protocolVersion == PROTOCOL_VERSION;
  }
}
