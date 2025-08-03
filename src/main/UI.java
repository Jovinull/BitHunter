package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class UI {

    GamePanel gp; // Referência ao painel principal do jogo, usado para acessar dimensões e
                  // objetos
    Font arial_20; // Fontes utilizadas na renderização dos textos

    public boolean messageOn = false; // Define se uma mensagem temporária será exibida
    public String message = ""; // Mensagem a ser exibida
    int messageCounter = 0; // Controla por quanto tempo a mensagem ficará visível

    public UI(GamePanel gp) {
        this.gp = gp;

        // Inicializa as fontes
        arial_20 = new Font("Arial", Font.PLAIN, 20);
    }

    // Exibe uma mensagem temporária na tela (por ex: "Porta destrancada!")
    public void showMessage(String text) {
        message = text;
        messageOn = true;
    }

    // Método principal de renderização do HUD e mensagens no jogo
    public void draw(Graphics2D g2) {

        g2.setFont(arial_20);
        g2.setColor(Color.white);

        // Exibe mensagens temporárias, como "Porta destrancada"
        if (messageOn == true) {
            g2.drawString(message, gp.tileSize / 2, gp.tileSize * 6); // Exibe mais ao centro da tela

            messageCounter++;

            // Mensagem desaparece após 120 frames (~2 segundos)
            if (messageCounter > 120) {
                messageCounter = 0;
                messageOn = false;
            }
        }
    }
}
