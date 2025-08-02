package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import object.OBJ_Key;

public class UI {

    GamePanel gp; // Referência ao painel principal do jogo, usado para acessar dimensões e
                  // objetos
    Font arial_40, arial_80B; // Fontes utilizadas na renderização dos textos
    BufferedImage keyImage; // Imagem da chave coletável no HUD

    public boolean messageOn = false; // Define se uma mensagem temporária será exibida
    public String message = ""; // Mensagem a ser exibida
    int messageCounter = 0; // Controla por quanto tempo a mensagem ficará visível

    public boolean gameFinished = false; // Sinaliza se o jogo foi concluído

    double playTime; // Tempo total de jogo (em segundos)
    DecimalFormat dFormat = new DecimalFormat("#0.00"); // Formato para exibir o tempo com 2 casas decimais

    public UI(GamePanel gp) {
        this.gp = gp;

        // Inicializa as fontes
        arial_40 = new Font("Arial", Font.PLAIN, 40);
        arial_80B = new Font("Arial", Font.BOLD, 80);

        // Carrega a imagem da chave para exibir no HUD
        OBJ_Key key = new OBJ_Key(gp);
        keyImage = key.image;
    }

    // Exibe uma mensagem temporária na tela (por ex: "Porta destrancada!")
    public void showMessage(String text) {
        message = text;
        messageOn = true;
    }

    // Método principal de renderização do HUD e mensagens no jogo
    public void draw(Graphics2D g2) {

        // Caso o jogo tenha sido finalizado
        if (gameFinished == true) {

            g2.setFont(arial_40);
            g2.setColor(Color.white);

            // Mensagem 1 — "Você encontrou o tesouro!"
            String text = "Você encontrou o tesouro!";
            int textLength = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
            int x = gp.screenWidth / 2 - textLength / 2; // Centraliza horizontalmente
            int y = gp.screenHeight / 2 - (gp.tileSize * 3); // Um pouco acima do centro
            g2.drawString(text, x, y);

            // Mensagem 2 — tempo de jogo formatado
            text = "Seu tempo de jogo: " + dFormat.format(playTime) + "!";
            textLength = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
            x = gp.screenWidth / 2 - textLength / 2;
            y = gp.screenHeight / 2 + (gp.tileSize * 4); // Abaixo do centro
            g2.drawString(text, x, y);

            // Mensagem 3 — "Parabéns!!" em destaque
            g2.setFont(arial_80B);
            g2.setColor(Color.yellow);
            text = "Parabéns!!";
            textLength = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
            x = gp.screenWidth / 2 - textLength / 2;
            y = gp.screenHeight / 2 + (gp.tileSize * 2); // Logo acima do tempo
            g2.drawString(text, x, y);

            // Finaliza o jogo parando a thread principal (game loop)
            gp.gameThread = null;

        } else {
            // HUD padrão enquanto o jogo está em andamento

            g2.setFont(arial_40);
            g2.setColor(Color.white);

            // Desenha a imagem da chave no canto superior esquerdo
            g2.drawImage(keyImage, gp.tileSize / 2, gp.tileSize / 2, gp.tileSize, gp.tileSize, null);

            // Mostra quantas chaves o jogador possui
            g2.drawString("x " + gp.player.hasKey, 74, 65); // Posição um pouco à direita da imagem

            // Atualiza e exibe o tempo de jogo
            playTime += (double) 1 / 60; // Como o jogo roda a 60 FPS, cada frame equivale a ~1/60s
            g2.drawString("Tempo: " + dFormat.format(playTime), gp.tileSize * 11, 65); // Exibe na superior direita

            // Exibe mensagens temporárias, como "Porta destrancada"
            if (messageOn == true) {
                g2.setFont(g2.getFont().deriveFont(25F)); // Usa uma fonte menor
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
}
