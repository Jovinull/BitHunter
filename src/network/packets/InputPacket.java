package network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import network.Packet;
import network.PacketType;

/**
 * Captura o estado de input de um jogador em um determinado frame.
 * <p>
 * Payload (9 bytes): {@code [2:sequenceId][1:up][1:down][1:left][1:right][1:space][1:shot][1:enter]}.
 * <p>
 * O {@code sequenceId} é um contador monotônico por cliente, usado para
 * descartar inputs obsoletos recebidos fora de ordem.
 * <p>
 * <strong>Fase 2:</strong> o game loop local enviará este pacote a cada frame
 * (via {@link network.NetworkManager#sendToServer}) e o servidor o processará
 * para mover o {@code RemotePlayer} correspondente.
 */
public final class InputPacket implements Packet {

  private final short sequenceId;
  private final boolean up;
  private final boolean down;
  private final boolean left;
  private final boolean right;
  private final boolean space;
  private final boolean shot;
  private final boolean enter;

  /**
   * @param sequenceId contador de frame monotônico do cliente
   * @param up         tecla de movimento para cima pressionada
   * @param down       tecla de movimento para baixo pressionada
   * @param left       tecla de movimento para esquerda pressionada
   * @param right      tecla de movimento para direita pressionada
   * @param space      tecla de ação/guarda pressionada
   * @param shot       tecla de disparo pressionada
   * @param enter      tecla de confirmação pressionada
   */
  public InputPacket(short sequenceId, boolean up, boolean down, boolean left,
      boolean right, boolean space, boolean shot, boolean enter) {
    this.sequenceId = sequenceId;
    this.up = up;
    this.down = down;
    this.left = left;
    this.right = right;
    this.space = space;
    this.shot = shot;
    this.enter = enter;
  }

  @Override
  public PacketType getType() {
    return PacketType.INPUT;
  }

  @Override
  public void write(DataOutputStream out) throws IOException {
    out.writeShort(sequenceId);
    out.writeBoolean(up);
    out.writeBoolean(down);
    out.writeBoolean(left);
    out.writeBoolean(right);
    out.writeBoolean(space);
    out.writeBoolean(shot);
    out.writeBoolean(enter);
  }

  /** Lê um InputPacket do stream (sem o byte de tipo). */
  public static InputPacket read(DataInputStream in) throws IOException {
    short seq = in.readShort();
    boolean upVal = in.readBoolean();
    boolean downVal = in.readBoolean();
    boolean leftVal = in.readBoolean();
    boolean rightVal = in.readBoolean();
    boolean spaceVal = in.readBoolean();
    boolean shotVal = in.readBoolean();
    boolean enterVal = in.readBoolean();
    return new InputPacket(seq, upVal, downVal, leftVal, rightVal, spaceVal, shotVal, enterVal);
  }

  // ---- Getters ----

  public short getSequenceId() { return sequenceId; }
  public boolean isUp()        { return up; }
  public boolean isDown()      { return down; }
  public boolean isLeft()      { return left; }
  public boolean isRight()     { return right; }
  public boolean isSpace()     { return space; }
  public boolean isShot()      { return shot; }
  public boolean isEnter()     { return enter; }
}
