package object;

import entity.Entity;
import main.GamePanel;

/**
 * Escudo do Compilador (Java) — Item lendário / relíquia.
 *
 * Um escudo especial que "compila" dano e "coleta lixo", com mecânicas únicas:
 *
 * 1) Passiva "Garbage Collector":
 *    A cada {@link #GC_HITS_THRESHOLD} hits recebidos, cura o player em
 *    {@link #GC_HEAL_AMOUNT} e gera um escudo temporário de
 *    {@link #GC_TEMP_SHIELD_HP} por {@link #GC_TEMP_SHIELD_DURATION_FRAMES}.
 *
 * 2) Defesa ativa "try/catch":
 *    Uma vez a cada {@link #TRY_CATCH_COOLDOWN_FRAMES}, se o player tomaria
 *    dano fatal, HP fica em 1 e ganha invulnerabilidade por
 *    {@link #TRY_CATCH_INVINCIBILITY_FRAMES}.
 *
 * 3) Bônus base:
 *    +{@link #SHIELD_DEFENSE} de defesa, {@link #SHIELD_GUARD_STAMINA} de guarda.
 *    Redução de dano de projéteis: não implementada (o jogo não separa dano
 *    por tipo de fonte; documentado aqui para futura extensão).
 *
 * Todas as constantes são centralizadas nesta classe para fácil ajuste de
 * balanceamento.
 */
public final class ObjShieldJava extends Entity {

  public static final String OBJ_ID = "item.shieldJava";

  // ====================================================================
  // Constantes configuráveis — Garbage Collector
  // ====================================================================
  /** Hits necessários para ativar o GC. */
  public static final int GC_HITS_THRESHOLD = 5;
  /** HP curado ao ativar o GC. */
  public static final int GC_HEAL_AMOUNT = 2;
  /** HP do escudo temporário gerado pelo GC. */
  public static final int GC_TEMP_SHIELD_HP = 4;
  /** Duração do escudo temporário em frames (5s × 60 FPS). */
  public static final int GC_TEMP_SHIELD_DURATION_FRAMES = 300;

  // ====================================================================
  // Constantes configuráveis — try/catch
  // ====================================================================
  /** Cooldown do try/catch em frames (60s × 60 FPS). */
  public static final int TRY_CATCH_COOLDOWN_FRAMES = 3600;
  /** Frames de invulnerabilidade ao ativar try/catch (2s × 60 FPS). */
  public static final int TRY_CATCH_INVINCIBILITY_FRAMES = 120;

  // ====================================================================
  // Bônus base
  // ====================================================================
  /** Valor de defesa do escudo. */
  public static final int SHIELD_DEFENSE = 4;
  /** Capacidade de stamina de guarda. */
  public static final int SHIELD_GUARD_STAMINA = 160;
  /** Preço do escudo (alto — item lendário, dificilmente vendido). */
  public static final int SHIELD_PRICE = 9999;

  // ====================================================================
  // Aquisição secreta
  // ====================================================================
  /** Moedas necessárias para abrir o baú secreto contendo este escudo. */
  public static final int SECRET_CHEST_COIN_COST = 300;

  public ObjShieldJava(GamePanel gp) {
    super(gp);

    setType(TYPE_SHIELD);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/shield_java", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(SHIELD_DEFENSE);
    setGuardStaminaCapacity(SHIELD_GUARD_STAMINA);
    setPrice(SHIELD_PRICE);
  }

  /**
   * Descrição dinâmica: inclui informação de cooldown do try/catch quando
   * o escudo está equipado.
   */
  @Override
  public String getDisplayDescription() {
    String baseDesc = super.getDisplayDescription();

    var player = gp.getPlayer();
    if (player != null && player.getCurrentShield() == this) {
      int cd = player.getJavaShieldTryCatchCooldown();
      String cdText;
      if (cd <= 0) {
        cdText = t("item.shieldJava.tryCatch.ready");
      } else {
        int seconds = (cd + 59) / 60; // arredonda para cima
        cdText = String.format(t("item.shieldJava.tryCatch.cooldown"), seconds);
      }
      return baseDesc + "\n" + cdText;
    }

    return baseDesc;
  }
}
