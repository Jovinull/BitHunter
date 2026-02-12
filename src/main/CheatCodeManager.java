package main;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Gerencia códigos secretos digitáveis durante o jogo.
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
 * Se o código estiver correto e não tiver sido usado nesta sessão,
 * o efeito é aplicado e um feedback aparece.
 * Se errar, nada acontece (silencioso).</p>
 *
 * <h3>Códigos registrados</h3>
 * <ul>
 *   <li>{@code trycatch}   → Adiciona o Escudo do Compilador (Java) ao inventário</li>
 *   <li>{@code nullpointer} → Easter egg: exibe mensagem divertida</li>
 * </ul>
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
 * 2. Pressione ' (aspas simples) ou " (aspas duplas).
 *    → Nada aparece na tela (captura silenciosa ativada).
 * 3. Digite: t r y c a t c h
 * 4. Pressione ENTER.
 *    → Mensagem "Código aceito!" aparece + escudo no inventário.
 * 5. Tente de novo: o código só funciona 1x por sessão.
 * 6. Para o easter egg: repita os passos 2-4 com "nullpointer".
 * </pre>
 */
public class CheatCodeManager {

  // ====================================================================
  // Constantes
  // ====================================================================

  /** Tempo máximo de captura antes de auto-confirmar (ms). */
  private static final long CAPTURE_TIMEOUT_MS = 4_000L;

  /** SFX tocado quando um código é aceito (level-up). */
  private static final int SUCCESS_SFX_ID = 8;

  // ====================================================================
  // Campos
  // ====================================================================

  private final GamePanel gp;

  /** Códigos registrados: chave (lowercase) → ação a executar. */
  private final Map<String, Runnable> registeredCodes = new LinkedHashMap<>();

  /** Códigos já utilizados nesta sessão (evita spam). */
  private final Set<String> usedCodes = new HashSet<>();

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

  private void registerCodes() {
    registeredCodes.put("trycatch", this::grantCompilerShield);
    registeredCodes.put("nullpointer", this::easterEggNullPointer);
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
  // Lógica interna
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

  private void confirm() {
    String code = buffer.toString().trim().toLowerCase();
    capturing = false;
    buffer.setLength(0);

    if (code.isEmpty()) {
      return;
    }

    Runnable action = registeredCodes.get(code);
    if (action != null && !usedCodes.contains(code)) {
      usedCodes.add(code);
      action.run();
    }
    // Código errado ou já usado → silencioso (sem feedback)
  }

  // ====================================================================
  // Ações dos códigos
  // ====================================================================

  /**
   * Código {@code "trycatch"}: concede o Escudo do Compilador (Java).
   *
   * <p>Escolha do código: "trycatch" referencia diretamente a mecânica
   * principal do escudo (defesa ativa try/catch), é fácil de lembrar
   * para qualquer programador Java, e não é óbvio para quem não conhece.</p>
   */
  private void grantCompilerShield() {
    LanguageManager lang = gp.getLanguageManager();
    gp.getGameUi().addMessage(lang.text("cheat.accepted"));
    gp.playSE(SUCCESS_SFX_ID);
    gp.getPlayer().addJavaShieldToInventory();
  }

  /**
   * Código {@code "nullpointer"}: easter egg divertido.
   *
   * <p>Não concede item; apenas exibe uma mensagem temática
   * de NullPointerException para diversão.</p>
   */
  private void easterEggNullPointer() {
    LanguageManager lang = gp.getLanguageManager();
    gp.getGameUi().addMessage(lang.text("cheat.accepted"));
    gp.getGameUi().addMessage(lang.text("cheat.nullpointer"));
    gp.playSE(SUCCESS_SFX_ID);
  }
}
