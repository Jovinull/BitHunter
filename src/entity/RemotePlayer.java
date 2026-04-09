package entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

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

  /** Cooldown entre ataques (em frames de 60 Hz). */
  private static final int ATTACK_COOLDOWN = 30;
  /** Alcance do golpe corpo-a-corpo em pixels à frente do remoto. */
  private static final int ATTACK_REACH = 40;
  /** Dano base aplicado a monstros atingidos pelo remoto. */
  private static final int ATTACK_DAMAGE = 2;

  private int attackCooldown = 0;

  private final String playerName;
  private final int characterClass;
  private final int networkId;

  // Posição alvo recebida do servidor — interpolamos worldX/Y em direção a ela
  private int targetX;
  private int targetY;

  // Dead-reckoning: velocidade estimada para extrapolação quando pacotes atrasam
  private int velocityX;
  private int velocityY;
  private int prevTargetX;
  private int prevTargetY;

  /** Frames desde a última atualização de estado recebida. */
  private int framesSinceUpdate = 0;

  /** Número máximo de frames de extrapolação antes de congelar. */
  private static final int MAX_EXTRAPOLATION_FRAMES = 12;

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
    // Estima velocidade para dead-reckoning (delta entre atualizações)
    prevTargetX = targetX;
    prevTargetY = targetY;
    targetX = state.getWorldX();
    targetY = state.getWorldY();
    velocityX = targetX - prevTargetX;
    velocityY = targetY - prevTargetY;
    framesSinceUpdate = 0;

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
   * Aplica o input recebido do cliente e move este jogador remoto, validando
   * colisão server-side (autoridade de servidor — Fase 3).
   * <p>
   * O host é a fonte da verdade: o movimento só é aplicado se a colisão contra
   * tiles, objetos, NPCs, monstros, tiles interativos e o jogador local
   * permitir. Se houver colisão, a posição atual é mantida e o cliente
   * eventualmente convergirá via {@link EntityStatePacket}.
   * <p>
   * Chamado pelo game loop do <strong>host</strong> ao receber um {@link InputPacket}.
   *
   * @param input pacote de input recebido do cliente
   */
  public void applyInput(InputPacket input) {
    boolean moving = true;
    if (input.isUp()) {
      setDirection(DIR_UP);
    } else if (input.isDown()) {
      setDirection(DIR_DOWN);
    } else if (input.isLeft()) {
      setDirection(DIR_LEFT);
    } else if (input.isRight()) {
      setDirection(DIR_RIGHT);
    } else {
      moving = false;
    }

    // Estado de guarda (Fase 8): tecla space (mapeada a GUARD no KeyHandler)
    setGuarding(input.isSpace() && !moving);

    if (moving) {
      // Validação de colisão server-side (mesma lógica do Player local)
      setCollisionOn(false);
      var cc = gp.getcChecker();
      cc.checkTile(this);
      cc.checkObject(this);
      cc.checkEntity(this, gp.getNpc());
      cc.checkEntity(this, gp.getMonster());
      cc.checkEntity(this, gp.getiTile());
      cc.checkPlayer(this);

      if (!isCollisionOn()) {
        int spd = getSpeed();
        switch (getDirection()) {
          case DIR_UP    -> setWorldY(getWorldY() - spd);
          case DIR_DOWN  -> setWorldY(getWorldY() + spd);
          case DIR_LEFT  -> setWorldX(getWorldX() - spd);
          case DIR_RIGHT -> setWorldX(getWorldX() + spd);
          default -> { /* sem direção válida */ }
        }
      }
      advanceSpriteCounter();
    }

    // Sincroniza alvo com a posição real (no host, worldX/Y já é o alvo)
    targetX = getWorldX();
    targetY = getWorldY();

    // Ataque server-side (Fase 5): tecla space ou shotKey acionam um golpe
    // corpo-a-corpo simples contra monstros à frente.
    if (attackCooldown > 0) {
      attackCooldown--;
    }
    // Atacar só pelo shotKey — space é GUARD no KeyHandler
    if (input.isShot() && attackCooldown == 0) {
      performRemoteAttack();
      attackCooldown = ATTACK_COOLDOWN;
    }
    setAttacking(attackCooldown > ATTACK_COOLDOWN - 8);
  }

  /**
   * Aplica dano simplificado a monstros adjacentes na direção atual.
   * Executado exclusivamente no host como autoridade de combate (Fase 5).
   */
  private void performRemoteAttack() {
    Rectangle hit = new Rectangle(
        getWorldX() + getSolidArea().x,
        getWorldY() + getSolidArea().y,
        getSolidArea().width,
        getSolidArea().height);
    switch (getDirection()) {
      case DIR_UP    -> hit.y -= ATTACK_REACH;
      case DIR_DOWN  -> hit.y += ATTACK_REACH;
      case DIR_LEFT  -> hit.x -= ATTACK_REACH;
      case DIR_RIGHT -> hit.x += ATTACK_REACH;
      default -> { /* nada */ }
    }
    Entity[] monsters = gp.getMonster()[gp.getCurrentMap()];
    for (int i = 0; i < monsters.length; i++) {
      Entity m = monsters[i];
      if (m == null || !m.isAlive() || m.isDying()) {
        continue;
      }
      Rectangle mRect = new Rectangle(
          m.getWorldX() + m.getSolidArea().x,
          m.getWorldY() + m.getSolidArea().y,
          m.getSolidArea().width,
          m.getSolidArea().height);
      if (hit.intersects(mRect)) {
        int dmg = Math.max(1, ATTACK_DAMAGE - m.getDefense());
        m.setLife(m.getLife() - dmg);
        gp.broadcastDamage(networkId, 1000 + i, dmg);
        if (m.getLife() <= 0) {
          m.setLife(0);
          m.setDying(true);
          gp.broadcastDeath(1000 + i);
        }
      }
    }
  }

  // ---- Ciclo de vida ----

  /**
   * Interpola suavemente worldX/Y em direção a targetX/Y (cliente) ou
   * mantém a posição já atualizada pelo input (host).
   * <p>
   * Quando não há atualizações recentes, aplica dead-reckoning (extrapolação)
   * usando a velocidade estimada, até {@link #MAX_EXTRAPOLATION_FRAMES}.
   */
  @Override
  public void update() {
    framesSinceUpdate++;

    // Alvo efetivo: target + extrapolação se pacotes atrasarem
    int effectiveTargetX = targetX;
    int effectiveTargetY = targetY;
    if (framesSinceUpdate > 3 && framesSinceUpdate <= MAX_EXTRAPOLATION_FRAMES) {
      // Extrapola baseado na velocidade estimada (escalada para 60Hz)
      // velocityX/Y é o delta entre as duas últimas atualizações (~20Hz, 3 frames)
      float extrapolationFactor = (framesSinceUpdate - 3) / 3.0f;
      effectiveTargetX += (int) (velocityX * extrapolationFactor);
      effectiveTargetY += (int) (velocityY * extrapolationFactor);
    }

    int dx = effectiveTargetX - getWorldX();
    int dy = effectiveTargetY - getWorldY();

    if (Math.abs(dx) <= SNAP_THRESHOLD) {
      setWorldX(effectiveTargetX);
    } else {
      setWorldX(getWorldX() + (int) (dx * LERP_FACTOR));
    }

    if (Math.abs(dy) <= SNAP_THRESHOLD) {
      setWorldY(effectiveTargetY);
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
