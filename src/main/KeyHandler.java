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
  public boolean upPressed, downPressed, leftPressed, rightPressed, enterPressed, shotKeyPressed, spacePressed;

  // DEBUG: exibir/ocultar tempo de desenho
  public boolean showDebugText = false;
  public boolean godModeOn = false;

  // Limites de índices dos menus de título (evita "números mágicos")
  private static final int MENU_MAIN_MAX_INDEX = 2; // [Novo jogo, Carregar, Sair]
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
    if (gp.gameState == gp.dialogueState || gp.gameState == gp.cutsceneState) {
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
    if (gp.gameState == gp.tradeState) {
      tradeState(key);
    }
    if (gp.gameState == gp.mapState) {
      mapState(key);
    }
  }

  /**
   * Estado: Tela de título (menus iniciais).
   * titleScreenState 0 = menu principal; 1 = seleção de classe/personagem.
   */
  public void titleState(int code) {

    // ===== TELA 0: MENU PRINCIPAL =====
    if (gp.ui.titleScreenState == 0) {

      if (code == KeyEvent.VK_W) {
        navigateUp(MENU_MAIN_MAX_INDEX);
      }

      if (code == KeyEvent.VK_S) {
        navigateDown(MENU_MAIN_MAX_INDEX);
      }

      if (code == KeyEvent.VK_ENTER) {
        if (gp.ui.commandNum == 0) {
          // Ir para a seleção de personagem (tela 1)
          gp.ui.titleScreenState = 1;
          gp.ui.commandNum = 0; // reset para o primeiro item da lista de classes
        }
        if (gp.ui.commandNum == 1) {
          gp.saveLoad.load();
          gp.gameState = gp.playState;
          gp.playMusic(0);
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
        navigateUp(MENU_CLASS_MAX_INDEX);
      }

      if (code == KeyEvent.VK_S) {
        navigateDown(MENU_CLASS_MAX_INDEX);
      }

      if (code == KeyEvent.VK_ENTER) {
        // Somente inicia o jogo se NÃO for "Voltar" (índice 3)
        if (gp.ui.commandNum == 3) {
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

    // Movimento (WASD)
    if (key == KeyEvent.VK_W) {
      upPressed = true;
    }
    if (key == KeyEvent.VK_S) {
      downPressed = true;
    }
    if (key == KeyEvent.VK_A) {
      leftPressed = true;
    }
    if (key == KeyEvent.VK_D) {
      rightPressed = true;
    }

    // Pausa
    if (key == KeyEvent.VK_P) {
      gp.gameState = gp.pauseState;
    }

    // Status do personagem
    if (key == KeyEvent.VK_C) {
      gp.gameState = gp.characterState;
    }

    // Confirmação (diálogo/ação)
    if (key == KeyEvent.VK_ENTER) {
      enterPressed = true;
    }

    // Tecla para soltar magia
    if (key == KeyEvent.VK_F) {
      shotKeyPressed = true;
    }

    // Opções
    if (key == KeyEvent.VK_ESCAPE) {
      gp.gameState = gp.optionsState;
    }

    // Mapa (tela cheia)
    if (key == KeyEvent.VK_M) {
      gp.gameState = gp.mapState;
    }

    // Mini-mapa (toggle com encapsulamento)
    if (key == KeyEvent.VK_X) {
      gp.map.setMiniMapOn(!gp.map.isMiniMapOn());
    }

    if (key == KeyEvent.VK_SPACE) {
      spacePressed = true;
    }

    // DEBUG (alternar exibição do texto de debug)
    if (key == KeyEvent.VK_T) {
      showDebugText = !showDebugText;
      gp.tileM.setDrawPath(showDebugText);
    }

    // Recarregar mapa (atalho de dev)
    if (key == KeyEvent.VK_R) {
      switch (gp.currentMap) {
        case 0 -> gp.tileM.loadMap("/maps/world01.txt", 0);
        case 1 -> gp.tileM.loadMap("/maps/interior01.txt", 1);
        default -> {
          // noop
        }
      }
    }

    // God mode (toggle)
    if (key == KeyEvent.VK_G) {
      godModeOn = !godModeOn;
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
      enterPressed = true;
    }
  }

  /**
   * Estado: Tela de status do personagem.
   */
  public void characterState(int code) {
    if (code == KeyEvent.VK_C) {
      gp.gameState = gp.playState;
    } else if (code == KeyEvent.VK_ENTER) {
      gp.player.selectItem();
    }

    // Navegação do inventário com W/A/S/D
    playerInventory(code);
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
      case 0 -> maxCommandNum = 5;
      case 3 -> maxCommandNum = 1;
      default -> {
        // noop
      }
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
        gp.resetGame(false);
        gp.playMusic(0);

      } else if (gp.ui.commandNum == 1) {
        gp.stopMusic();
        gp.resetGame(true);

        gp.ui.titleScreenState = 0;
        gp.ui.commandNum = 0;
        gp.ui.subState = 0;

        gp.gameState = gp.titleState;
      }
    }
  }

  public void tradeState(int code) {

    if (code == KeyEvent.VK_ENTER) {
      enterPressed = true;
    }

    if (gp.ui.subState == 0) {
      if (code == KeyEvent.VK_W) {
        gp.ui.commandNum--;
        if (gp.ui.commandNum < 0) {
          gp.ui.commandNum = 2;
        }
        gp.playSE(9);
      }

      if (code == KeyEvent.VK_S) {
        gp.ui.commandNum++;
        if (gp.ui.commandNum > 2) {
          gp.ui.commandNum = 0;
        }
        gp.playSE(9);
      }
    }

    if (gp.ui.subState == 1) {

      npcInventory(code);
      if (code == KeyEvent.VK_ESCAPE) {
        gp.ui.subState = 0;
      }
    }

    if (gp.ui.subState == 2) {

      playerInventory(code);
      if (code == KeyEvent.VK_ESCAPE) {
        gp.ui.subState = 0;
      }
    }
  }

  public void mapState(int code) {
    if (code == KeyEvent.VK_M) {
      gp.gameState = gp.playState;
    }
  }

  public void playerInventory(int code) {
    if (code == KeyEvent.VK_W) {
      if (gp.ui.playerSlotRow != 0) {
        gp.ui.playerSlotRow--;
        gp.playSE(9);
      }
    } else if (code == KeyEvent.VK_A) {
      if (gp.ui.playerSlotCol != 0) {
        gp.ui.playerSlotCol--;
        gp.playSE(9);
      }
    } else if (code == KeyEvent.VK_S) {
      if (gp.ui.playerSlotRow != SLOT_ROW_MAX) {
        gp.ui.playerSlotRow++;
        gp.playSE(9);
      }
    } else if (code == KeyEvent.VK_D) {
      if (gp.ui.playerSlotCol != SLOT_COL_MAX) {
        gp.ui.playerSlotCol++;
        gp.playSE(9);
      }
    }
  }

  public void npcInventory(int code) {
    if (code == KeyEvent.VK_W) {
      if (gp.ui.npcSlotRow != 0) {
        gp.ui.npcSlotRow--;
        gp.playSE(9);
      }
    } else if (code == KeyEvent.VK_A) {
      if (gp.ui.npcSlotCol != 0) {
        gp.ui.npcSlotCol--;
        gp.playSE(9);
      }
    } else if (code == KeyEvent.VK_S) {
      if (gp.ui.npcSlotRow != SLOT_ROW_MAX) {
        gp.ui.npcSlotRow++;
        gp.playSE(9);
      }
    } else if (code == KeyEvent.VK_D) {
      if (gp.ui.npcSlotCol != SLOT_COL_MAX) {
        gp.ui.npcSlotCol++;
        gp.playSE(9);
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
      case KeyEvent.VK_ENTER -> enterPressed = false;
      case KeyEvent.VK_SPACE -> spacePressed = false;
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
}
