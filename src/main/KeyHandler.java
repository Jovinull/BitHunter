package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Classe que lida com os eventos de teclado
public class KeyHandler implements KeyListener {

    // Flags para verificar se as teclas estão pressionadas
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    // DEBUG
    boolean checkDrawTime = false;

    // Não utilizaremos esse método por enquanto (usado para entrada de caracteres)
    @Override
    public void keyTyped(KeyEvent e) {
    }

    // Executado quando uma tecla é pressionada
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode(); // Obtém o código da tecla pressionada

        // Define a flag da tecla pressionada como true
        if (code == KeyEvent.VK_W) {
            upPressed = true;
        }
        if (code == KeyEvent.VK_S) {
            downPressed = true;
        }
        if (code == KeyEvent.VK_A) {
            leftPressed = true;
        }
        if (code == KeyEvent.VK_D) {
            rightPressed = true;
        }
        // DEBUG
        if (code == KeyEvent.VK_T) {
            if(checkDrawTime == false) {
                checkDrawTime = true; // Ativa o modo de debug para tempo de desenho
                System.out.println("DEBUG: Tempo de desenho ativado");
            } else {
                checkDrawTime = false; // Desativa o modo de debug
                System.out.println("DEBUG: Tempo de desenho desativado");
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
