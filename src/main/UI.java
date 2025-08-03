package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class UI {

    GamePanel gp; // Referência ao painel principal do jogo, usado para acessar dimensões e
                  // objetos

    Graphics2D g2; // Usado para desenhar na tela

    Font arial_20, arial80B; // Fontes utilizadas na renderização dos textos

    public boolean messageOn = false; // Define se uma mensagem temporária será exibida
    public String message = ""; // Mensagem a ser exibida
    int messageCounter = 0; // Controla por quanto tempo a mensagem ficará visível

    public UI(GamePanel gp) {
        this.gp = gp;

        // Inicializa as fontes
        arial_20 = new Font("Arial", Font.PLAIN, 20);
        arial80B = new Font("Arial", Font.BOLD, 80);
    }

    // Exibe uma mensagem temporária na tela (por ex: "Porta destrancada!")
    public void showMessage(String text) {
        message = text;
        messageOn = true;
    }

    // Método principal de renderização do HUD e mensagens no jogo
    public void draw(Graphics2D g2) {

        this.g2 = g2; // Recebe o contexto gráfico para desenhar

        // Define a fonte e cor do texto
        g2.setFont(arial_20);
        g2.setColor(Color.white);

        if (gp.gameState == gp.playState) {
            // Do playState stuff later
        }

        if (gp.gameState == gp.pauseState) {
            drawPauseScreen(); // Desenha a tela de pausa
        }
    }

    public void drawPauseScreen() {
        g2.setFont(arial80B);
        String text = "PAUSED";
        int x = getXforCenteredText(text);
        int y = gp.screenHeight / 2;

        g2.drawString(text, x, y);
    }

    public int getXforCenteredText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = gp.screenWidth / 2 - length / 2;
        return x;
    }
}
