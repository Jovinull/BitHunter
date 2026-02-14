package main;

import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import entity.Entity;

/**
 * Gerencia códigos secretos digitáveis durante o jogo (sistema CRC).
 *
 * <h3>Arquitetura de repetibilidade</h3>
 * <p>Cada cheat é registrado com uma {@link Repeatability}:</p>
 * <ul>
 *   <li>{@code ONE_TIME} — pode ser usado apenas uma vez por save.
 *       O estado é persistido via {@link data.DataStorage} para impedir
 *       reuso mesmo após fechar e reabrir o jogo.</li>
 *   <li>{@code REPEATABLE} — pode ser usado livremente, sem restrição.</li>
 * </ul>
 *
 * <h3>Modo de uso (para o jogador)</h3>
 * <ol>
 *   <li>No estado PLAY, pressione aspas ({@code '} ou {@code "}) para ativar
 *       o modo captura silencioso.</li>
 *   <li>Digite o código secreto (letras, dígitos ou ponto; case-insensitive).</li>
 *   <li>Pressione ENTER para confirmar, ou ESC para cancelar.
 *       Se expirar o timeout ({@value #CAPTURE_TIMEOUT_MS}ms), o código
 *       é validado automaticamente.</li>
 * </ol>
 *
 * <p>Nenhuma indicação visual aparece durante a digitação.
 * Se o código estiver correto e elegível para uso, o efeito é aplicado
 * e um feedback aparece no HUD. Códigos ONE_TIME já usados exibem aviso.
 * Código inválido → silencioso.</p>
 *
 * <h3>Códigos registrados</h3>
 * <table>
 *   <tr><th>Código</th><th>Efeito</th><th>Tipo</th></tr>
 *   <tr><td>{@code trycatch}</td><td>Escudo do Compilador (Java)</td><td>ONE_TIME</td></tr>
 *   <tr><td>{@code nullpointer}</td><td>Easter egg: mensagem divertida</td><td>REPEATABLE</td></tr>
 *   <tr><td>{@code overflow}</td><td>+1000 moedas (Stack Overflow)</td><td>REPEATABLE</td></tr>
 *   <tr><td>{@code hotfix}</td><td>Restaura vida e mana ao máximo</td><td>REPEATABLE</td></tr>
 *   <tr><td>{@code sudo}</td><td>+5 chaves (acesso root)</td><td>REPEATABLE</td></tr>
 *   <tr><td>{@code printf}</td><td>+5 poções vermelhas</td><td>REPEATABLE</td></tr>
 *   <tr><td>{@code gitpush}</td><td>Força salvar o jogo</td><td>REPEATABLE</td></tr>
 *   <tr><td>{@code finalclass}</td><td>Alterna Modo Deus</td><td>REPEATABLE</td></tr>
 *   <tr><td>{@code refactor}</td><td>Sobe 1 nível</td><td>ONE_TIME</td></tr>
 * </table>
 *
 * <h3>Persistência</h3>
 * <p>Os códigos ONE_TIME já utilizados são salvos em
 * {@link data.DataStorage#getUsedCheatCodes()} e restaurados ao carregar
 * o jogo via {@link data.SaveLoad}.</p>
 *
 * <h3>Compatibilidade de teclado (ABNT2 / US)</h3>
 * <p>A ativação é feita via {@link #onCharTyped(char)}, que recebe o caractere
 * real digitado (independente do layout físico). Isso garante que tanto
 * {@code '} (aspas simples) quanto {@code "} (aspas duplas) funcionem
 * em qualquer layout — ABNT2, US-International, etc.</p>
 *
 * <h3>Passos de reprodução para teste manual</h3>
 * <pre>
 * 1. Inicie o jogo e entre em PLAY_STATE (novo jogo ou carregar).
 * 2. Pressione ' ou " para ativar a captura silenciosa.
 * 3. Digite o código desejado (ex: hotfix).
 * 4. Pressione ENTER para confirmar.
 *    → Feedback aparece no HUD + efeito aplicado.
 * 5. Para ONE_TIME: tente usar novamente → "Código já utilizado!"
 * 6. Salve e carregue o jogo → ONE_TIME continua bloqueado.
 * </pre>
 */
public class CheatCodeManager {

  // ====================================================================
  // Repetibilidade
  // ====================================================================

  /**
   * Define se um cheat pode ser usado uma única vez ou repetidamente.
   */
  enum Repeatability {
    /** Uso único por save — persistido entre sessões. */
    ONE_TIME,
    /** Sem restrição de uso. */
    REPEATABLE
  }

  /**
   * Registro interno de um cheat: regra de uso e ação a executar.
   */
  private record CheatEntry(Repeatability repeatability, Runnable action) {}

  // ====================================================================
  // Constantes
  // ====================================================================

  /** Tempo máximo de captura antes de auto-confirmar (ms). */
  private static final long CAPTURE_TIMEOUT_MS = 4_000L;

  /** SFX tocado quando um código é aceito. */
  private static final int SUCCESS_SFX_ID = 8;

  /** Moedas concedidas pelo cheat {@code overflow}. */
  private static final int OVERFLOW_COINS = 1_000;

  /** Chaves concedidas pelo cheat {@code sudo}. */
  private static final int SUDO_KEYS = 5;

  /** Poções vermelhas concedidas pelo cheat {@code printf}. */
  private static final int PRINTF_POTIONS = 5;

  // IDs internos de itens usados pelos cheats
  private static final String ITEM_KEY_ID = "item.key";
  private static final String ITEM_POTION_RED_ID = "item.potionRed";

  // ====================================================================
  // Campos
  // ====================================================================

  private final GamePanel gp;

  /** Códigos registrados: chave (lowercase) → entrada com ação e repetibilidade. */
  private final Map<String, CheatEntry> registeredCodes = new LinkedHashMap<>();

  /**
   * Códigos ONE_TIME já utilizados neste save.
   * <p>Persistido via {@link data.DataStorage} para sobreviver ao ciclo
   * save/load. Códigos REPEATABLE nunca entram aqui.</p>
   */
  private final Set<String> usedOneTimeCodes = new HashSet<>();

  // --- Estado de captura ---
  private boolean capturing;
  private final StringBuilder buffer = new StringBuilder();
  private long captureStartMs;

  // ====================================================================
  // Construtor
  // ====================================================================

  public CheatCodeManager(GamePanel gp) {
    this.gp = gp;
    registerCodes();
  }

  // ====================================================================
  // Registro de códigos
  // ====================================================================

  /**
   * Registra todos os códigos secretos com suas respectivas ações e
   * regras de repetibilidade. Cada entrada documenta o motivo de ser
   * ONE_TIME ou REPEATABLE.
   */
  private void registerCodes() {

    // --- Códigos originais ---

    // "trycatch" → Concede o Escudo do Compilador (Java) ao inventário.
    // ONE_TIME: item único e poderoso; permitir repetição desbalancearia o jogo.
    registeredCodes.put("trycatch",
        new CheatEntry(Repeatability.ONE_TIME, this::grantCompilerShield));

    // "nullpointer" → Easter egg: exibe mensagem temática de NullPointerException.
    // REPEATABLE: não concede vantagem mecânica, apenas diversão.
    registeredCodes.put("nullpointer",
        new CheatEntry(Repeatability.REPEATABLE, this::easterEggNullPointer));

    // --- Novos códigos: recursos ---

    // "overflow" → +1000 moedas (referência a Stack Overflow → overflow de moedas).
    // REPEATABLE: moedas são consumíveis e o jogador gasta naturalmente.
    registeredCodes.put("overflow",
        new CheatEntry(Repeatability.REPEATABLE, this::grantGold));

    // "hotfix" → Restaura vida e mana ao máximo (deploy de hotfix = correção rápida).
    // REPEATABLE: efeito instantâneo e não acumulável, útil em emergências.
    registeredCodes.put("hotfix",
        new CheatEntry(Repeatability.REPEATABLE, this::restoreVitals));

    // "sudo" → +5 chaves (super user do = acesso irrestrito).
    // REPEATABLE: chaves são consumidas ao abrir portas/baús.
    registeredCodes.put("sudo",
        new CheatEntry(Repeatability.REPEATABLE, this::grantKeys));

    // "printf" → +5 poções vermelhas (printf = output de dados → output de cura).
    // REPEATABLE: poções são consumíveis e se esgotam com uso.
    registeredCodes.put("printf",
        new CheatEntry(Repeatability.REPEATABLE, this::grantPotions));

    // --- Novos códigos: utilidade ---

    // "gitpush" → Força salvar o jogo (git push --force!).
    // REPEATABLE: salvar o jogo é sempre seguro e desejável.
    registeredCodes.put("gitpush",
        new CheatEntry(Repeatability.REPEATABLE, this::forceSave));

    // "finalclass" → Alterna Modo Deus (final class = não pode ser herdado/derrotado).
    // REPEATABLE: é um toggle on/off, precisa repetir para desligar.
    registeredCodes.put("finalclass",
        new CheatEntry(Repeatability.REPEATABLE, this::toggleGodMode));

    // "refactor" → Sobe 1 nível instantaneamente (refatoração = melhoria contínua).
    // ONE_TIME: level up permanente é impactante demais para repetir via cheat.
    registeredCodes.put("refactor",
        new CheatEntry(Repeatability.ONE_TIME, this::grantLevelUp));
  }

  // ====================================================================
  // API pública (chamada pelo KeyHandler)
  // ====================================================================

  /**
   * Processa um caractere digitado ({@code keyTyped}).
   *
   * <p>Retorna {@code true} se o evento foi consumido e não deve
   * ser propagado para outros handlers.</p>
   *
   * <p><b>Compatibilidade ABNT2/US:</b> aceita tanto {@code '} quanto {@code "}
   * como caractere de ativação, pois o mapeamento de VK_QUOTE varia entre
   * layouts. O {@code keyTyped} fornece o caractere real independentemente
   * do layout físico do teclado.</p>
   */
  public boolean onCharTyped(char c) {
    if (!capturing) {
      // Verifica se é o caractere de ativação (aspas simples ou duplas)
      if (c == '\'' || c == '"') {
        startCapture();
        return true;
      }
      return false;
    }

    // Durante captura: aceita letras, dígitos e ponto
    if (Character.isLetterOrDigit(c) || c == '.') {
      buffer.append(Character.toLowerCase(c));
    }
    // Qualquer outro char durante captura é ignorado mas consumido
    return true;
  }

  /**
   * Processa uma tecla pressionada ({@code keyPressed}) durante captura.
   *
   * <p>Retorna {@code true} se o evento foi consumido. Durante o modo
   * captura, <b>todas</b> as teclas são consumidas para impedir que
   * ações do jogo (WASD, T, R, G etc.) sejam disparadas.</p>
   */
  public boolean onKeyPressed(int keyCode) {
    if (!capturing) {
      return false;
    }

    if (keyCode == KeyEvent.VK_ENTER) {
      confirm();
      return true;
    }
    if (keyCode == KeyEvent.VK_ESCAPE) {
      cancel();
      return true;
    }

    // Durante captura, consome TODAS as teclas para não
    // disparar ações do jogo (WASD, T, R, G, etc.)
    return true;
  }

  /**
   * Verifica se o manager está em modo de captura.
   *
   * <p>Usado pelo {@link KeyHandler} para saber se deve bloquear
   * {@code keyReleased} de flags de movimento durante a digitação.</p>
   */
  public boolean isCapturing() {
    return capturing;
  }

  /**
   * Chamado a cada frame pelo {@link GamePanel#update()}.
   * Verifica o timeout de captura e auto-confirma se expirado.
   */
  public void tick() {
    if (!capturing) {
      return;
    }
    long elapsed = System.currentTimeMillis() - captureStartMs;
    if (elapsed >= CAPTURE_TIMEOUT_MS) {
      confirm();
    }
  }

  // ====================================================================
  // Persistência (chamada pelo SaveLoad)
  // ====================================================================

  /**
   * Retorna cópia imutável dos códigos ONE_TIME já usados.
   * <p>Utilizado por {@link data.SaveLoad} ao salvar o jogo.</p>
   */
  public Set<String> getUsedOneTimeCodes() {
    return Collections.unmodifiableSet(usedOneTimeCodes);
  }

  /**
   * Restaura os códigos ONE_TIME já usados a partir do save.
   * <p>Utilizado por {@link data.SaveLoad} ao carregar o jogo.</p>
   *
   * @param codes conjunto de códigos já usados (null-safe)
   */
  public void setUsedOneTimeCodes(Set<String> codes) {
    usedOneTimeCodes.clear();
    if (codes != null) {
      usedOneTimeCodes.addAll(codes);
    }
  }

  // ====================================================================
  // Lógica interna de captura
  // ====================================================================

  private void startCapture() {
    capturing = true;
    buffer.setLength(0);
    captureStartMs = System.currentTimeMillis();
  }

  private void cancel() {
    capturing = false;
    buffer.setLength(0);
  }

  /**
   * Valida o código digitado e executa a ação correspondente.
   *
   * <p>Fluxo de decisão:</p>
   * <ol>
   *   <li>Código vazio → ignora.</li>
   *   <li>Código não registrado → silencioso (sem feedback).</li>
   *   <li>ONE_TIME já usado → mensagem de aviso e bloqueio.</li>
   *   <li>Código válido → marca ONE_TIME como usado, executa ação.</li>
   * </ol>
   */
  private void confirm() {
    String code = buffer.toString().trim().toLowerCase();
    capturing = false;
    buffer.setLength(0);

    if (code.isEmpty()) {
      return;
    }

    CheatEntry entry = registeredCodes.get(code);
    if (entry == null) {
      // Código inválido → silencioso (não revela que o sistema existe)
      return;
    }

    // Verifica bloqueio de ONE_TIME já utilizado
    if (entry.repeatability() == Repeatability.ONE_TIME
        && usedOneTimeCodes.contains(code)) {
      gp.getGameUi().addMessage(
          gp.getLanguageManager().text("cheat.alreadyUsed"));
      return;
    }

    // Marca ONE_TIME como usado ANTES de executar (evita duplo uso)
    if (entry.repeatability() == Repeatability.ONE_TIME) {
      usedOneTimeCodes.add(code);
    }

    entry.action().run();
  }

  // ====================================================================
  // Ações dos códigos
  // ====================================================================

  /**
   * {@code "trycatch"} — Concede o Escudo do Compilador (Java).
   *
   * <p>ONE_TIME: referencia a mecânica try/catch do escudo;
   * é um item poderoso e exclusivo que não deve ser duplicado.</p>
   */
  private void grantCompilerShield() {
    var lang = gp.getLanguageManager();
    gp.getGameUi().addMessage(lang.text("cheat.accepted"));
    gp.playSE(SUCCESS_SFX_ID);
    gp.getPlayer().addJavaShieldToInventory();
  }

  /**
   * {@code "nullpointer"} — Easter egg temático de NullPointerException.
   *
   * <p>REPEATABLE: não concede vantagem mecânica, apenas exibe
   * mensagem divertida para diversão do jogador.</p>
   */
  private void easterEggNullPointer() {
    var lang = gp.getLanguageManager();
    gp.getGameUi().addMessage(lang.text("cheat.accepted"));
    gp.getGameUi().addMessage(lang.text("cheat.nullpointer"));
    gp.playSE(SUCCESS_SFX_ID);
  }

  /**
   * {@code "overflow"} — Adiciona {@value #OVERFLOW_COINS} moedas.
   *
   * <p>REPEATABLE: referência a Stack Overflow = overflow de riqueza.
   * Moedas são consumíveis (compras, baús), então repetir é equilibrado.</p>
   */
  private void grantGold() {
    var lang = gp.getLanguageManager();
    var player = gp.getPlayer();
    player.setCoin(player.getCoin() + OVERFLOW_COINS);
    gp.getGameUi().addMessage(lang.text("cheat.overflow"));
    gp.playSE(SUCCESS_SFX_ID);
  }

  /**
   * {@code "hotfix"} — Restaura vida e mana ao máximo.
   *
   * <p>REPEATABLE: efeito instantâneo e não acumulável.
   * "Hotfix" = correção emergencial implantada em produção.</p>
   */
  private void restoreVitals() {
    var lang = gp.getLanguageManager();
    var player = gp.getPlayer();
    player.setLife(player.getMaxLife());
    player.setMana(player.getMaxMana());
    gp.getGameUi().addMessage(lang.text("cheat.hotfix"));
    gp.playSE(SUCCESS_SFX_ID);
  }

  /**
   * {@code "sudo"} — Adiciona {@value #SUDO_KEYS} chaves ao inventário.
   *
   * <p>REPEATABLE: chaves são consumidas ao abrir portas/baús.
   * "sudo" = super user do → acesso irrestrito a tudo.</p>
   */
  private void grantKeys() {
    var lang = gp.getLanguageManager();
    if (addStackableItems(ITEM_KEY_ID, SUDO_KEYS)) {
      gp.getGameUi().addMessage(lang.text("cheat.sudo"));
      gp.playSE(SUCCESS_SFX_ID);
    } else {
      gp.getGameUi().addMessage(lang.text("player.message.cannotCarryMore"));
    }
  }

  /**
   * {@code "printf"} — Adiciona {@value #PRINTF_POTIONS} poções vermelhas.
   *
   * <p>REPEATABLE: poções são consumíveis e se esgotam naturalmente.
   * "printf" = output de dados → output de cura.</p>
   */
  private void grantPotions() {
    var lang = gp.getLanguageManager();
    if (addStackableItems(ITEM_POTION_RED_ID, PRINTF_POTIONS)) {
      gp.getGameUi().addMessage(lang.text("cheat.printf"));
      gp.playSE(SUCCESS_SFX_ID);
    } else {
      gp.getGameUi().addMessage(lang.text("player.message.cannotCarryMore"));
    }
  }

  /**
   * {@code "gitpush"} — Força salvar o jogo imediatamente.
   *
   * <p>REPEATABLE: salvar o jogo é sempre seguro e desejável.
   * "git push --force" = forçar envio do estado atual.</p>
   */
  private void forceSave() {
    var lang = gp.getLanguageManager();
    gp.saveLoad.save();
    gp.getGameUi().addMessage(lang.text("cheat.gitpush"));
    gp.playSE(SUCCESS_SFX_ID);
  }

  /**
   * {@code "finalclass"} — Alterna o Modo Deus (god mode).
   *
   * <p>REPEATABLE: é um toggle on/off, precisa repetir para desligar.
   * "final class" = classe que não pode ser herdada/sobrescrita/derrotada.</p>
   */
  private void toggleGodMode() {
    var lang = gp.getLanguageManager();
    gp.getKeyH().toggleGodMode();
    String key = gp.getKeyH().isGodModeOn()
        ? "cheat.finalclass.on"
        : "cheat.finalclass.off";
    gp.getGameUi().addMessage(lang.text(key));
    gp.playSE(SUCCESS_SFX_ID);
  }

  /**
   * {@code "refactor"} — Concede XP suficiente para subir 1 nível.
   *
   * <p>ONE_TIME: level up permanente é cumulativo e impactante demais
   * para repetir livremente via cheat.</p>
   *
   * <p>Reutiliza {@link entity.Player#checkLevelUp()} para disparar
   * toda a lógica padrão de level up (stats, diálogo, SFX).</p>
   */
  private void grantLevelUp() {
    var lang = gp.getLanguageManager();
    var player = gp.getPlayer();
    // Injeta XP suficiente para disparar o level up padrão,
    // reutilizando toda a lógica existente de progressão.
    player.setExp(player.getNextLevelExp());
    player.checkLevelUp();
    gp.getGameUi().addMessage(lang.text("cheat.refactor"));
  }

  // ====================================================================
  // Utilitário interno
  // ====================================================================

  /**
   * Adiciona uma quantidade de itens empilháveis ao inventário do jogador.
   *
   * <p>Se já existir um stack do item, incrementa a quantidade.
   * Caso contrário, cria um novo slot (se houver espaço).</p>
   *
   * @param internalId id interno do item (ex: {@code "item.key"})
   * @param count      quantidade a adicionar
   * @return {@code true} se os itens foram adicionados com sucesso
   */
  private boolean addStackableItems(String internalId, int count) {
    var player = gp.getPlayer();
    var inv = player.getInventory();
    int idx = player.searchItemInInventory(internalId);

    if (idx != 999) {
      // Stack existente → incrementa quantidade
      var existing = inv.get(idx);
      existing.setAmount(existing.getAmount() + count);
      return true;
    }

    // Sem stack → cria novo slot se o inventário tiver espaço
    if (inv.size() < Entity.MAX_INVENTORY_SIZE) {
      var item = gp.geteGenerator().getObject(internalId);
      item.setAmount(count);
      inv.add(item);
      return true;
    }

    return false;
  }
}
