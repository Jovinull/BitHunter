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

        // Estagio do jogo: título/menu
        if (gp.gameState == gp.titleState) {
            if (gp.ui.titleScreenState == 0) {
                if (code == KeyEvent.VK_W) {
                    gp.ui.commandNum--; // Navega para cima no menu^
                    if (gp.ui.commandNum < 0) {
                        gp.ui.commandNum = 2; // Volta para a última opção se ultrapassar o limite
                    }
                }

                if (code == KeyEvent.VK_S) {
                    gp.ui.commandNum++; // Navega para baixo no menu
                    if (gp.ui.commandNum > 2) {
                        gp.ui.commandNum = 0; // Volta para a primeira opção se ultrapassar o limite
                    }
                }

                if (code == KeyEvent.VK_ENTER) {
                    if (gp.ui.commandNum == 0) {
                        gp.ui.titleScreenState = 1; // Inicia o jogo
                    }

                    if (gp.ui.commandNum == 1) {
                        // Exibe créditos ou informações do jogo
                        System.out.println("Exibindo créditos...");
                    }

                    if (gp.ui.commandNum == 2) {
                        System.exit(0); // Sai do jogo
                    }
                }
            }

            else if (gp.ui.titleScreenState == 1) {
                if (code == KeyEvent.VK_W) {
                    gp.ui.commandNum--; // Navega para cima no menu
                    if (gp.ui.commandNum < 0) {
                        gp.ui.commandNum = 3; // Volta para a última opção se ultrapassar o limite
                    }
                }

                if (code == KeyEvent.VK_S) {
                    gp.ui.commandNum++; // Navega para baixo no menu
                    if (gp.ui.commandNum > 3) {
                        gp.ui.commandNum = 0; // Volta para a primeira opção se ultrapassar o limite
                    }
                }

                if (code == KeyEvent.VK_ENTER) {
                    if (gp.ui.commandNum == 0) {
                        System.out.println("Executar ações específicas do Guerreiro");
                        gp.gameState = gp.playState;
                        gp.playMusic(0);
                    }

                    if (gp.ui.commandNum == 1) {
                        System.out.println("Executar ações específicas do Ladrão");
                        gp.gameState = gp.playState;
                        gp.playMusic(0);
                    }

                    if (gp.ui.commandNum == 2) {
                        System.out.println("Executar ações específicas do Feiticeiro");
                        gp.gameState = gp.playState;
                        gp.playMusic(0);
                    }

                    if (gp.ui.commandNum == 3) {
                        gp.ui.titleScreenState = 0; // Volta para o menu principal
                        gp.ui.commandNum = 0; // Reseta a seleção do menu
                    }
                }
            }
        }

        // Só responde a controles durante o jogo ativo
        else if (gp.gameState == gp.playState) {
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
