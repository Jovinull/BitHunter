package bestiary;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.UtilityTool;

/**
 * Serviço central do Bestiário.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Registrar kills e calcular progressão de estágios</li>
 *   <li>Gerar notificações de mudança de estágio</li>
 *   <li>Fornecer dados para a UI (progresso, criaturas, sprites)</li>
 *   <li>Gerenciar Bestiary Points</li>
 *   <li>Expor dados para persistência (save/load)</li>
 * </ul>
 */
public final class BestiaryService {

  private static final Logger LOG = Logger.getLogger(BestiaryService.class.getName());

  private final GamePanel gp;
  private final BestiaryRegistry registry;

  /** Progresso por bestiaryId → BestiaryProgress */
  private final Map<String, BestiaryProgress> progressMap = new HashMap<>();

  /** Fila de notificações pendentes para exibir na HUD. */
  private final Queue<String> notifications = new LinkedList<>();

  /** Pontos do Bestiário acumulados pelo jogador. */
  private int bestiaryPoints = 0;

  /** Cache de sprites já carregados (bestiaryId → BufferedImage). */
  private final Map<String, BufferedImage> spriteCache = new HashMap<>();

  public BestiaryService(GamePanel gp) {
    this.gp = gp;
    this.registry = new BestiaryRegistry();
  }

  // ===== API Principal =====

  /**
   * Chamado quando um monstro é morto.
   *
   * @param monsterRef o internalId do monstro (ex.: "monster.greenSlime")
   */
  public void recordKill(String monsterRef) {
    if (monsterRef == null || monsterRef.isBlank()) return;

    String bestiaryId = registry.getBestiaryIdForMonster(monsterRef);
    if (bestiaryId == null) {
      // Monstro não cadastrado no bestiário — ignorar silenciosamente
      return;
    }

    BestiaryCreature creature = registry.getById(bestiaryId);
    if (creature == null) return;

    BestiaryProgress progress = progressMap.computeIfAbsent(bestiaryId, k -> new BestiaryProgress());
    BestiaryStage oldStage = progress.getStage(creature.getDifficulty(), creature.getRarity());

    progress.incrementKills();

    BestiaryStage newStage = progress.getStage(creature.getDifficulty(), creature.getRarity());

    // Notificações de mudança de estágio
    if (oldStage != newStage) {
      handleStageTransition(creature, oldStage, newStage);
    }

    // Primeira kill: sempre notificar discovery
    if (progress.getKills() == 1 && oldStage == BestiaryStage.LOCKED) {
      String name = getCreatureDisplayName(creature);
      notifications.add(gp.getLanguageManager().text("bestiary.notify.discovered")
          .replace("%s", name));
    }
  }

  /**
   * Revela um item VERY_RARE no bestiário de uma criatura.
   * Deve ser chamado quando o jogador efetivamente recebe o drop.
   */
  public void revealVeryRareDrop(String monsterRef, String itemId) {
    String bestiaryId = registry.getBestiaryIdForMonster(monsterRef);
    if (bestiaryId == null) return;

    BestiaryProgress progress = progressMap.get(bestiaryId);
    if (progress == null) return;

    if (!progress.isVeryRareRevealed(itemId)) {
      progress.revealVeryRareItem(itemId);
      LOG.fine(() -> "Very rare drop revealed: " + itemId + " for " + bestiaryId);
    }
  }

  // ===== Notificações =====

  /** Retorna e remove a próxima notificação da fila, ou null se vazia. */
  public String pollNotification() {
    return notifications.poll();
  }

  public boolean hasNotifications() {
    return !notifications.isEmpty();
  }

  // ===== Dados para UI =====

  public BestiaryRegistry getRegistry() {
    return registry;
  }

  public BestiaryProgress getProgress(String bestiaryId) {
    return progressMap.getOrDefault(bestiaryId, new BestiaryProgress());
  }

  public BestiaryStage getStage(String bestiaryId) {
    BestiaryCreature creature = registry.getById(bestiaryId);
    if (creature == null) return BestiaryStage.LOCKED;
    return getProgress(bestiaryId).getStage(creature.getDifficulty(), creature.getRarity());
  }

  public int getBestiaryPoints() {
    return bestiaryPoints;
  }

  /** Conta criaturas conhecidas (DISCOVERED+) em uma classe. */
  public int getKnownCountForClass(CreatureClass cc) {
    int count = 0;
    for (BestiaryCreature c : registry.getCreaturesByClass(cc)) {
      if (getStage(c.getId()) != BestiaryStage.LOCKED) {
        count++;
      }
    }
    return count;
  }

  /** Conta criaturas completadas em uma classe. */
  public int getCompletedCountForClass(CreatureClass cc) {
    int count = 0;
    for (BestiaryCreature c : registry.getCreaturesByClass(cc)) {
      if (getStage(c.getId()) == BestiaryStage.COMPLETED) {
        count++;
      }
    }
    return count;
  }

  /** Retorna o nome de exibição da criatura (traduzido). */
  public String getCreatureDisplayName(BestiaryCreature creature) {
    String name = gp.getLanguageManager().text(creature.getNameKey());
    // Se a tradução retornar a chave (não encontrada), usar o ID
    if (name.equals(creature.getNameKey())) {
      return creature.getId();
    }
    return name;
  }

  /** Carrega o sprite de uma criatura (com cache). */
  public BufferedImage getCreatureSprite(BestiaryCreature creature) {
    return spriteCache.computeIfAbsent(creature.getId(), id -> loadSprite(creature.getSpriteRef()));
  }

  /**
   * Retorna os drops visíveis para o estágio e progresso atuais.
   * VERY_RARE só aparece se revelado.
   */
  public List<BestiaryDrop> getVisibleDrops(BestiaryCreature creature, BestiaryProgress progress) {
    BestiaryStage stage = progress.getStage(creature.getDifficulty(), creature.getRarity());
    List<BestiaryDrop> visible = new ArrayList<>();

    for (BestiaryDrop drop : creature.getDrops()) {
      if (isDropVisible(drop, stage, progress)) {
        visible.add(drop);
      }
    }
    return visible;
  }

  // ===== Persistência (chamado por SaveLoad) =====

  /** Retorna dados de kills para serialização. Chave=bestiaryId, Valor=kills. */
  public Map<String, Integer> getKillsMap() {
    Map<String, Integer> map = new HashMap<>();
    for (var entry : progressMap.entrySet()) {
      map.put(entry.getKey(), entry.getValue().getKills());
    }
    return map;
  }

  /** Retorna itens VERY_RARE revelados para serialização. */
  public Map<String, Set<String>> getRevealedVeryRareMap() {
    Map<String, Set<String>> map = new HashMap<>();
    for (var entry : progressMap.entrySet()) {
      Set<String> revealed = entry.getValue().getRevealedVeryRareItems();
      if (!revealed.isEmpty()) {
        map.put(entry.getKey(), revealed);
      }
    }
    return map;
  }

  /** Restaura estado do bestiário a partir de dados salvos. */
  public void restoreFromSave(Map<String, Integer> killsMap,
      Map<String, Set<String>> revealedMap,
      int savedPoints) {
    progressMap.clear();
    bestiaryPoints = Math.max(0, savedPoints);

    if (killsMap != null) {
      for (var entry : killsMap.entrySet()) {
        String id = entry.getKey();
        // Ignorar IDs desconhecidos (compatibilidade)
        if (registry.getById(id) == null) {
          LOG.fine(() -> "Ignoring unknown bestiary ID from save: " + id);
          continue;
        }
        Set<String> revealed = (revealedMap != null && revealedMap.containsKey(id))
            ? revealedMap.get(id)
            : Set.of();
        progressMap.put(id, new BestiaryProgress(entry.getValue(), revealed));
      }
    }
  }

  /** Limpa todo o progresso (para reset de jogo). */
  public void reset() {
    progressMap.clear();
    bestiaryPoints = 0;
    notifications.clear();
  }

  // ===== Helpers internos =====

  private void handleStageTransition(BestiaryCreature creature,
      BestiaryStage oldStage, BestiaryStage newStage) {
    String name = getCreatureDisplayName(creature);

    String msgKey = switch (newStage) {
      case STAGE_1 -> "bestiary.notify.stage1";
      case STAGE_2 -> "bestiary.notify.stage2";
      case COMPLETED -> "bestiary.notify.completed";
      default -> null;
    };

    if (msgKey != null) {
      notifications.add(gp.getLanguageManager().text(msgKey).replace("%s", name));
    }

    // Pontos ao completar
    if (newStage == BestiaryStage.COMPLETED) {
      bestiaryPoints += creature.getCompletionPoints();
    }
  }

  private boolean isDropVisible(BestiaryDrop drop, BestiaryStage stage,
      BestiaryProgress progress) {
    return switch (drop.tier()) {
      case COMMON -> stage.ordinal() >= BestiaryStage.STAGE_1.ordinal();
      case UNCOMMON -> stage.ordinal() >= BestiaryStage.STAGE_2.ordinal();
      case SEMI_RARE, RARE -> stage == BestiaryStage.COMPLETED;
      case VERY_RARE -> progress.isVeryRareRevealed(drop.itemId());
    };
  }

  private BufferedImage loadSprite(String spriteRef) {
    if (spriteRef == null || spriteRef.isBlank()) return null;

    try {
      var stream = getClass().getResourceAsStream(spriteRef + ".png");
      if (stream == null) return null;
      BufferedImage raw = ImageIO.read(stream);
      UtilityTool tool = new UtilityTool();
      return tool.scaleImage(raw, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "Failed to load bestiary sprite: " + spriteRef, e);
      return null;
    }
  }
}
