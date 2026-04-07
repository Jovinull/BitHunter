package entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import main.GamePanel;
import network.packets.EntityStatePacket;
import network.packets.InputPacket;

/**
 * Representação local de um jogador remoto conectado via rede.
 * <p>
 * Ao contrário de {@link Player}, esta classe NÃO usa {@link main.KeyHandler}:
 * seu estado é controlado exclusivamente por pacotes recebidos da rede.
 * <ul>
 *   <li><strong>No host:</strong> recebe {@link InputPacket} e move a entidade
 *       localmente (com autoridade do servidor).</li>
 *   <li><strong>No cliente:</strong> recebe {@link EntityStatePacket} e interpola
 *       suavemente a posição para ocultar a diferença de frequência
 *       entre rede (~20 Hz) e renderização (~60 Hz).</li>
 * </ul>
 * <p>
 * A identificação de rede é feita pelo campo {@link #networkId}, atribuído
 * pelo host no momento da conexão (1, 2 ou 3 — 0 é reservado ao host).
 */
// this-escape: construtor chama getSolidArea() herdado de Entity.
// Seguro pois RemotePlayer não possui subclasses.
@SuppressWarnings("this-escape")
public class RemotePlayer extends Entity {

  /**
   * Fator de interpolação linear aplicado por frame (~60 Hz).
   * Valor 0.3 = converge ~90% da distância em ~7 frames (~116 ms a 60 Hz),
   * suficiente para absorver até 150 ms de latência sem teletransporte visível.
   */
  private static final float LERP_FACTOR = 0.3f;

  /**
   * Distância em pixels abaixo da qual o jogador é "snapado" direto ao alvo
   * (evita vibração de subpixel próximo ao destino).
   */
  private static final int SNAP_THRESHOLD = 4;

  /** Frames entre troca de sprites durante o movimento (espelha Player). */
  private static final int SPRITE_TICK = 12;

  private final String playerName;
  private final int characterClass;
  private final int networkId;

  // Posição alvo recebida do servidor — interpolamos worldX/Y em direção a ela
  private int targetX;
  private int targetY;

  /**
   * @param gp             referência ao painel do jogo
   * @param playerName     nome exibido acima do personagem
   * @param characterClass índice da classe (0=Guerreiro, 1=Ladrão, 2=Feiticeiro)
   * @param networkId      ID de rede atribuído pelo host (1–3)
   */
  public RemotePlayer(GamePanel gp, String playerName, int characterClass, int networkId) {
    super(gp);
    this.playerName = playerName;
    this.characterClass = characterClass;
    this.networkId = networkId;

    // Configuração inicial de atributos
    setType(TYPE_PLAYER);
    setName(playerName);
    setDefaultSpeed(3);
    setSpeed(3);
    setMaxLife(12);
    setLife(12);
    setDirection(DIR_DOWN);
    setWorldX(GamePanel.TILE_SIZE * 50);
    setWorldY(GamePanel.TILE_SIZE * 50);

    targetX = getWorldX();
    targetY = getWorldY();

    // Configura hitbox (igual ao Player)
    getSolidArea().x = 8;
    getSolidArea().y = 16;
    setSolidAreaDefaultX(8);
    setSolidAreaDefaultY(16);
    getSolidArea().width = 32;
    getSolidArea().height = 32;

    loadSprites();
  }

  /**
   * Carrega os sprites de caminhada do personagem base.
   * Usa os mesmos recursos do {@link Player} para garantir consistência visual.
   */
  private void loadSprites() {
    String base = "/player/Walking sprites/boy_";
    up1    = setup(base + "up_1",    GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    up2    = setup(base + "up_2",    GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down1  = setup(base + "down_1",  GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down2  = setup(base + "down_2",  GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left1  = setup(base + "left_1",  GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left2  = setup(base + "left_2",  GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right1 = setup(base + "right_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right2 = setup(base + "right_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }

  // ---- Aplicação de estado recebido pela rede ----

  /**
   * Aplica o estado autoritativo recebido do servidor.
   * Atualiza a posição alvo (interpolada em {@link #update()}) e os flags de animação.
   * <p>
   * Chamado pelo game loop do <strong>cliente</strong> ao receber
   * um {@link EntityStatePacket}.
   *
   * @param state pacote de estado recebido do servidor
   */
  public void applyState(EntityStatePacket state) {
    targetX = state.getWorldX();
    targetY = state.getWorldY();
    setDirection(state.getDirection());
    setSpriteNum(state.getSpriteNum());
    setLife(state.getLife());
    setMaxLife(state.getMaxLife());
    setAttacking(state.isAttacking());
    setAlive(state.isAlive());
    setDying(state.isDying());
    setInvincible(state.isInvincible());
    setGuarding(state.isGuarding());
  }

  /**
   * Aplica o input recebido do cliente e move este jogador remoto.
   * Movimento sem verificação de colisão (autoridade de colisão chega na Fase 3).
   * <p>
   * Chamado pelo game loop do <strong>host</strong> ao receber um {@link InputPacket}.
   *
   * @param input pacote de input recebido do cliente
   */
  public void applyInput(InputPacket input) {
    int spd = getSpeed();
    if (input.isUp()) {
      setDirection(DIR_UP);
      setWorldY(getWorldY() - spd);
    } else if (input.isDown()) {
      setDirection(DIR_DOWN);
      setWorldY(getWorldY() + spd);
    } else if (input.isLeft()) {
      setDirection(DIR_LEFT);
      setWorldX(getWorldX() - spd);
    } else if (input.isRight()) {
      setDirection(DIR_RIGHT);
      setWorldX(getWorldX() + spd);
    }
    // Sincroniza alvo com a posição real (no host, worldX/Y já é o alvo)
    targetX = getWorldX();
    targetY = getWorldY();
    // Avança a animação de sprite
    advanceSpriteCounter();
  }

  // ---- Ciclo de vida ----

  /**
   * Interpola suavemente worldX/Y em direção a targetX/Y (cliente) ou
   * mantém a posição já atualizada pelo input (host).
   */
  @Override
  public void update() {
    int dx = targetX - getWorldX();
    int dy = targetY - getWorldY();

    if (Math.abs(dx) <= SNAP_THRESHOLD) {
      setWorldX(targetX);
    } else {
      setWorldX(getWorldX() + (int) (dx * LERP_FACTOR));
    }

    if (Math.abs(dy) <= SNAP_THRESHOLD) {
      setWorldY(targetY);
    } else {
      setWorldY(getWorldY() + (int) (dy * LERP_FACTOR));
    }
  }

  /**
   * Renderiza o sprite do jogador remoto e sua etiqueta de nome.
   * Delega o desenho do sprite ao {@link entity.EntityRenderer} herdado.
   */
  @Override
  public void draw(Graphics2D g2) {
    super.draw(g2);
    drawNameTag(g2);
  }

  /** Exibe o nome do jogador acima do sprite. */
  private void drawNameTag(Graphics2D g2) {
    if (!isAlive()) {
      return;
    }
    int sx = getScreenX();
    int sy = getScreenY();
    g2.setColor(Color.WHITE);
    g2.setFont(new Font("Arial", Font.BOLD, 10));
    int nameWidth = g2.getFontMetrics().stringWidth(playerName);
    g2.drawString(playerName, sx + (GamePanel.TILE_SIZE - nameWidth) / 2, sy - 4);
  }

  @Override
  public void setAction() {
    // Sem IA — ação controlada exclusivamente pela rede
  }

  @Override
  public void checkDrop() {
    // Sem drops locais — gerenciado pelo servidor (Fase 4)
  }

  // ---- Helper privado ----

  private void advanceSpriteCounter() {
    setSpriteCounter(getSpriteCounter() + 1);
    if (getSpriteCounter() > SPRITE_TICK) {
      setSpriteNum(getSpriteNum() == 1 ? 2 : 1);
      setSpriteCounter(0);
    }
  }

  // ---- Getters ----

  public int getNetworkId()      { return networkId; }
  public String getPlayerName()  { return playerName; }
  public int getCharacterClass() { return characterClass; }
}
