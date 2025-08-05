package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Classe que lida com os eventos de teclado
public class KeyHandler implements KeyListener {

    GamePanel gp; // Referência ao GamePanel para acessar seus métodos e atributos
    // Flags para verificar se as teclas estão pressionadas
    public boolean upPressed, downPressed, leftPressed, rightPressed, enterPressed;
    // DEBUG
    boolean checkDrawTime = false;

    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    // Não utilizaremos esse método por enquanto (usado para entrada de caracteres)
    @Override
    public void keyTyped(KeyEvent e) {
    }

    // Chamado sempre que uma tecla é pressionada
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode(); // Código da tecla

        // Só responde a controles durante o jogo ativo
        if (gp.gameState == gp.playState) {
            // Movimento: W/A/S/D
            if (code == KeyEvent.VK_W)
                upPressed = true;
            if (code == KeyEvent.VK_S)
                downPressed = true;
            if (code == KeyEvent.VK_A)
                leftPressed = true;
            if (code == KeyEvent.VK_D)
                rightPressed = true;

            // Pausa
            if (code == KeyEvent.VK_P) {
                gp.gameState = gp.pauseState;
            }
            // Confirmação de diálogo ou seleção
            if (code == KeyEvent.VK_ENTER) {
                enterPressed = true;
            }

            // Modo DEBUG: alterna exibição do tempo de renderização
            if (code == KeyEvent.VK_T) {
                checkDrawTime = !checkDrawTime;
                System.out.println("DEBUG: Tempo de desenho "
                        + (checkDrawTime ? "ativado" : "desativado"));
            }

        } else if (gp.gameState == gp.pauseState) {
            // Despausar com P
            if (code == KeyEvent.VK_P) {
                gp.gameState = gp.playState;
            }

        } else if (gp.gameState == gp.dialogueState) {
            // Fechar diálogo com Enter
            if (code == KeyEvent.VK_ENTER) {
                gp.gameState = gp.playState;
            }
        }
    }

    // Executado quando uma tecla é solta
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode(); // Obtém o código da tecla liberada

        // Define a flag da tecla como false
        if (code == KeyEvent.VK_W) {
            upPressed = false;
        }
        if (code == KeyEvent.VK_S) {
            downPressed = false;
        }
        if (code == KeyEvent.VK_A) {
            leftPressed = false;
        }
        if (code == KeyEvent.VK_D) {
            rightPressed = false;
        }
    }
}
