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
    public boolean upPressed, downPressed, leftPressed, rightPressed, enterPressed;

    // DEBUG: exibir/ocultar tempo de desenho
    boolean showDebugText = false;

    // Limites de índices dos menus de título (evita "números mágicos")
    private static final int MENU_MAIN_MAX_INDEX = 2; // [Iniciar, Créditos, Sair]
    private static final int MENU_CLASS_MAX_INDEX = 3; // [Guerreiro, Ladrão, Feiticeiro, Voltar]

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
    }

    /**
     * Estado: Tela de título (menus iniciais).
     * titleScreenState 0 = menu principal; 1 = seleção de classe/personagem.
     */
    public void titleState(int key) {
        if (gp.ui.titleScreenState == 0) {
            // MENU PRINCIPAL
            if (key == KeyEvent.VK_W) {
                navigateUp(MENU_MAIN_MAX_INDEX);
            } else if (key == KeyEvent.VK_S) {
                navigateDown(MENU_MAIN_MAX_INDEX);
            } else if (key == KeyEvent.VK_ENTER) {
                handleTitleScreen0Enter();
            }
            return;
        }

        if (gp.ui.titleScreenState == 1) {
            // SELEÇÃO DE CLASSE
            if (key == KeyEvent.VK_W) {
                navigateUp(MENU_CLASS_MAX_INDEX);
            } else if (key == KeyEvent.VK_S) {
                navigateDown(MENU_CLASS_MAX_INDEX);
            } else if (key == KeyEvent.VK_ENTER) {
                handleTitleScreen1Enter();
            }
        }
    }

    /**
     * Estado: Jogando (movimento, pausa, status, debug, etc).
     */
    public void playState(int key) {
        // Movimento (WASD)
        if (key == KeyEvent.VK_W)
            upPressed = true;
        if (key == KeyEvent.VK_S)
            downPressed = true;
        if (key == KeyEvent.VK_A)
            leftPressed = true;
        if (key == KeyEvent.VK_D)
            rightPressed = true;

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

        // DEBUG: alternar exibição do tempo de renderização
        if (key == KeyEvent.VK_T) {
            if (showDebugText == false) {
                showDebugText = true;
            } else if (showDebugText == true) {
                showDebugText = false;
            }
        }

        if (key == KeyEvent.VK_R) {
            gp.tileM.loadMap("/maps/worldV2.txt");
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

        if (code == KeyEvent.VK_C) {
            gp.gameState = gp.playState;
        }

        if (code == KeyEvent.VK_W) {
            if (gp.ui.slotRow != 0) {
                gp.ui.slotRow--;
                gp.playSE(9);
            }
        }

        if (code == KeyEvent.VK_A) {
            if (gp.ui.slotCol != 0) {
                gp.ui.slotCol--;
                gp.playSE(9);
            }
        }

        if (code == KeyEvent.VK_S) {
            if (gp.ui.slotRow != 3) {
                gp.ui.slotRow++;
                gp.playSE(9);
            }
        }

        if (code == KeyEvent.VK_D) {
            if (gp.ui.slotCol != 4) {
                gp.ui.slotCol++;
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

        if (key == KeyEvent.VK_W)
            upPressed = false;
        if (key == KeyEvent.VK_S)
            downPressed = false;
        if (key == KeyEvent.VK_A)
            leftPressed = false;
        if (key == KeyEvent.VK_D)
            rightPressed = false;
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
