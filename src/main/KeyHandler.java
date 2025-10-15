package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Lida com eventos de teclado e delega o comportamento conforme o estado atual
 * do jogo.
 * Mantém flags de input para o loop principal (GamePanel) consumir.
 */
public class KeyHandler implements KeyListener {

  // Referência ao painel do jogo para ler/alterar estados
  private final GamePanel gp;

  // Flags de input contínuo (consumidas no update do jogo)
  public boolean upPressed, downPressed, leftPressed, rightPressed, enterPressed, shotKeyPressed;

  // DEBUG: exibir/ocultar tempo de desenho
  boolean showDebugText = false;

  // Limites de índices dos menus de título (evita "números mágicos")
  private static final int MENU_MAIN_MAX_INDEX = 2; // [Iniciar, Créditos, Sair]
  private static final int MENU_CLASS_MAX_INDEX = 3; // [Guerreiro, Ladrão, Feiticeiro, Voltar]

  // Limites do grid de slots na tela de personagem
  private static final int SLOT_ROW_MAX = 3;
  private static final int SLOT_COL_MAX = 4;

  public KeyHandler(GamePanel gp) {
    this.gp = gp;
  }

  /** Não utilizado (entrada de caracteres). */
  @Override
  public void keyTyped(KeyEvent e) {
    // Intencionalmente vazio
  }

  /**
   * Dispara ao pressionar uma tecla. Encaminha para o "estado" atual do jogo.
   */
  @Override
  public void keyPressed(KeyEvent e) {
    final int key = e.getKeyCode();

    if (gp.gameState == gp.titleState) {
      titleState(key);
      return;
    }
    if (gp.gameState == gp.playState) {
      playState(key);
      return;
    }
    if (gp.gameState == gp.pauseState) {
      pauseState(key);
      return;
    }
    if (gp.gameState == gp.dialogueState) {
      dialogueState(key);
      return;
    }
    if (gp.gameState == gp.characterState) {
      characterState(key);
    }
    if (gp.gameState == gp.optionsState) {
      optionsState(key);
    }
    if (gp.gameState == gp.gameOverState) {
      gameOverState(key);
    }
  }

  /**
   * Estado: Tela de título (menus iniciais).
   * titleScreenState 0 = menu principal; 1 = seleção de classe/personagem.
   */
  public void titleState(int code) {

    // ===== Configurações de limites =====
    final int MAIN_MENU_MAX = 2; // 0..2 (ex.: Novo Jogo, (Add later), Sair)
    final int CLASS_MENU_MAX = 3; // 0..3 (ex.: Guerreiro, Mago, Arqueiro) — ajuste ao seu caso

    // ===== TELA 0: MENU PRINCIPAL =====
    if (gp.ui.titleScreenState == 0) {

      if (code == KeyEvent.VK_W) {
        gp.ui.commandNum--;
        if (gp.ui.commandNum < 0)
          gp.ui.commandNum = MAIN_MENU_MAX;
      }

      if (code == KeyEvent.VK_S) {
        gp.ui.commandNum++;
        if (gp.ui.commandNum > MAIN_MENU_MAX)
          gp.ui.commandNum = 0;
      }

      if (code == KeyEvent.VK_ENTER) {
        if (gp.ui.commandNum == 0) {
          // Ir para a seleção de personagem (tela 1)
          gp.ui.titleScreenState = 1;
          gp.ui.commandNum = 0; // reset para o primeiro item da lista de classes
        }
        if (gp.ui.commandNum == 1) {
          // add later (ex.: Continue/Opções)
        }
        if (gp.ui.commandNum == 2) {
          System.exit(0);
        }
      }
      return; // importante: não cair na lógica da tela 1
    }

    // ===== TELA 1: SELEÇÃO DE PERSONAGEM =====
    if (gp.ui.titleScreenState == 1) {

      if (code == KeyEvent.VK_W) {
        gp.ui.commandNum--;
        if (gp.ui.commandNum < 0)
          gp.ui.commandNum = CLASS_MENU_MAX;
      }

      if (code == KeyEvent.VK_S) {
        gp.ui.commandNum++;
        if (gp.ui.commandNum > CLASS_MENU_MAX)
          gp.ui.commandNum = 0;
      }

      if (code == KeyEvent.VK_ENTER) {
        // Aqui você confirma a classe selecionada e inicia o jogo
        // Ex.: setar classe no player antes de iniciar
        // gp.player.setClazzByIndex(gp.ui.commandNum);

        // Somente inicia o jogo se NÃO for "Voltar" (índice 3)
        if (gp.ui.commandNum == 3) {
          // Volta para o menu principal
          gp.ui.titleScreenState = 0;
          gp.ui.commandNum = 0;
          return;
        }

        gp.gameState = gp.playState;
        gp.playMusic(0);
      }

      // (Opcional) Voltar para o menu principal
      if (code == KeyEvent.VK_ESCAPE) {
        gp.ui.titleScreenState = 0;
        gp.ui.commandNum = 0;
      }
    }
  }

  /**
   * Estado: Jogando (movimento, pausa, status, debug, etc).
   */
  public void playState(int key) {
    switch (key) {
      // Movimento (WASD)
      case KeyEvent.VK_W -> upPressed = true;
      case KeyEvent.VK_S -> downPressed = true;
      case KeyEvent.VK_A -> leftPressed = true;
      case KeyEvent.VK_D -> rightPressed = true;

      // Pausa
      case KeyEvent.VK_P -> gp.gameState = gp.pauseState;

      // Status do personagem
      case KeyEvent.VK_C -> gp.gameState = gp.characterState;

      // Confirmação (diálogo/ação)
      case KeyEvent.VK_ENTER -> enterPressed = true;

      // Tecla para soltar magia
      case KeyEvent.VK_F -> shotKeyPressed = true;

      case KeyEvent.VK_ESCAPE -> gp.gameState = gp.optionsState;

      // DEBUG: alternar exibição do tempo de renderização
      case KeyEvent.VK_T -> showDebugText = !showDebugText;

      // Recarregar mapa (atalho de dev)
      case KeyEvent.VK_R -> gp.tileM.loadMap("/maps/worldV2.txt");

      default -> {
        /* noop */ }
    }
  }

  /**
   * Estado: Pausado.
   */
  public void pauseState(int key) {
    if (key == KeyEvent.VK_P) {
      gp.gameState = gp.playState;
    }
  }

  /**
   * Estado: Diálogo aberto.
   */
  public void dialogueState(int key) {
    if (key == KeyEvent.VK_ENTER) {
      gp.gameState = gp.playState;
    }
  }

  /**
   * Estado: Tela de status do personagem.
   */
  public void characterState(int code) {
    switch (code) {
      case KeyEvent.VK_C -> gp.gameState = gp.playState;

      case KeyEvent.VK_W -> {
        if (gp.ui.slotRow != 0) {
          gp.ui.slotRow--;
          gp.playSE(9);
        }
      }
      case KeyEvent.VK_A -> {
        if (gp.ui.slotCol != 0) {
          gp.ui.slotCol--;
          gp.playSE(9);
        }
      }
      case KeyEvent.VK_S -> {
        if (gp.ui.slotRow != SLOT_ROW_MAX) {
          gp.ui.slotRow++;
          gp.playSE(9);
        }
      }
      case KeyEvent.VK_D -> {
        if (gp.ui.slotCol != SLOT_COL_MAX) {
          gp.ui.slotCol++;
          gp.playSE(9);
        }
      }
      case KeyEvent.VK_ENTER -> gp.player.selectItem();

      default -> {
        /* noop */ }
    }
  }

  public void optionsState(int code) {

    if (code == KeyEvent.VK_ESCAPE) {
      gp.gameState = gp.playState;
    }
    if (code == KeyEvent.VK_ENTER) {
      enterPressed = true;
    }

    int maxCommandNum = 0;
    switch (gp.ui.subState) {
      case 0:
        maxCommandNum = 5;
        break;
      case 3:
        maxCommandNum = 1;
        break;
    }

    if (code == KeyEvent.VK_W) {
      gp.ui.commandNum--;
      if (gp.ui.commandNum < 0) {
        gp.ui.commandNum = maxCommandNum;
      }
      gp.playSE(9);
    }
    if (code == KeyEvent.VK_S) {
      gp.ui.commandNum++;
      if (gp.ui.commandNum > maxCommandNum) {
        gp.ui.commandNum = 0;
      }
      gp.playSE(9);
    }

    if (code == KeyEvent.VK_A) {
      if (gp.ui.subState == 0) {
        if (gp.ui.commandNum == 1 && gp.music.volumeScale > 0) {
          gp.music.volumeScale--;
          gp.music.checkVolume();
          gp.playSE(9);
        }
        if (gp.ui.commandNum == 2 && gp.se.volumeScale > 0) {
          gp.se.volumeScale--;
          gp.playSE(9);
        }
      }
    }

    if (code == KeyEvent.VK_D) {
      if (gp.ui.subState == 0) {
        if (gp.ui.commandNum == 1 && gp.music.volumeScale < 5) {
          gp.music.volumeScale++;
          gp.music.checkVolume();
          gp.playSE(9);
        }
        if (gp.ui.commandNum == 2 && gp.se.volumeScale < 5) {
          gp.se.volumeScale++;
          gp.playSE(9);
        }
      }
    }
  }

  public void gameOverState(int code) {

    if (code == KeyEvent.VK_W) {
      gp.ui.commandNum--;
      if (gp.ui.commandNum < 0) {
        gp.ui.commandNum = 1;
      }
      gp.playSE(9);
    }

    if (code == KeyEvent.VK_S) {
      gp.ui.commandNum++;
      if (gp.ui.commandNum > 1) {
        gp.ui.commandNum = 0;
      }
      gp.playSE(9);
    }

    if (code == KeyEvent.VK_ENTER) {
      if (gp.ui.commandNum == 0) {
        gp.gameState = gp.playState;
        gp.retry();

      } else if (gp.ui.commandNum == 1) {
        gp.stopMusic();
        gp.restart();

        gp.ui.titleScreenState = 0;
        gp.ui.commandNum = 0;
        gp.ui.subState = 0;

        gp.gameState = gp.titleState;
      }
    }
  }

  /**
   * Dispara ao SOLTAR uma tecla. Zera flags de movimento correspondentes.
   */
  @Override
  public void keyReleased(KeyEvent e) {
    final int key = e.getKeyCode();

    switch (key) {
      case KeyEvent.VK_W -> upPressed = false;
      case KeyEvent.VK_S -> downPressed = false;
      case KeyEvent.VK_A -> leftPressed = false;
      case KeyEvent.VK_D -> rightPressed = false;
      case KeyEvent.VK_F -> shotKeyPressed = false;
      default -> {
        /* noop */ }
    }
  }

  // -----------------------
  // Helpers privados
  // -----------------------

  /** Move seleção para cima no menu com wrap-around. */
  private void navigateUp(int maxIndex) {
    gp.ui.commandNum--;
    if (gp.ui.commandNum < 0) {
      gp.ui.commandNum = maxIndex;
    }
  }

  /** Move seleção para baixo no menu com wrap-around. */
  private void navigateDown(int maxIndex) {
    gp.ui.commandNum++;
    if (gp.ui.commandNum > maxIndex) {
      gp.ui.commandNum = 0;
    }
  }

  /** Ações do Enter no menu principal (titleScreenState == 0). */
  private void handleTitleScreen0Enter() {
    if (gp.ui.commandNum == 0) {
      // Ir para seleção de classe
      gp.ui.titleScreenState = 1;
      return;
    }
    if (gp.ui.commandNum == 1) {
      // Créditos / informações
      System.out.println("Exibindo créditos...");
      return;
    }
    if (gp.ui.commandNum == 2) {
      // Sair
      System.exit(0);
    }
  }

  /** Ações do Enter na seleção de classe (titleScreenState == 1). */
  private void handleTitleScreen1Enter() {
    if (gp.ui.commandNum == 0) {
      System.out.println("Executar ações específicas do Guerreiro");
      startGameFromTitle();
      return;
    }
    if (gp.ui.commandNum == 1) {
      System.out.println("Executar ações específicas do Ladrão");
      startGameFromTitle();
      return;
    }
    if (gp.ui.commandNum == 2) {
      System.out.println("Executar ações específicas do Feiticeiro");
      startGameFromTitle();
      return;
    }
    if (gp.ui.commandNum == 3) {
      // Voltar ao menu principal
      gp.ui.titleScreenState = 0;
      gp.ui.commandNum = 0; // reset seleção
    }
  }

  /** Inicia o jogo a partir do título, mantendo o comportamento original. */
  private void startGameFromTitle() {
    gp.gameState = gp.playState;
    gp.playMusic(0);
  }
}
