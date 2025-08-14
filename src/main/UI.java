package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;

public class UI {

    GamePanel gp; // Referência ao painel principal do jogo, usado para acessar dimensões e
                  // objetos

    Graphics2D g2; // Usado para desenhar na tela

    Font maruMonica, purisaB; // Fontes utilizadas na renderização dos textos

    public boolean messageOn = false; // Define se uma mensagem temporária será exibida
    public String message = ""; // Mensagem a ser exibida
    int messageCounter = 0; // Controla por quanto tempo a mensagem ficará visível
    public boolean gameFinished = false; // Indica se o jogo foi finalizado
    public String currentDialogue = ""; // Armazena o diálogo atual com NPCs
    public int commandNum = 0; // Número do comando selecionado no menu de diálogo
    public int titleScreenState = 0; // Estado da tela de título

    public UI(GamePanel gp) {
        this.gp = gp;

        try {
            InputStream is = getClass().getResourceAsStream("/resource/font/x12y16pxMaruMonica.ttf");
            maruMonica = Font.createFont(Font.TRUETYPE_FONT, is);
            is = getClass().getResourceAsStream("/resource/font/Purisa Bold.ttf");
            purisaB = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        g2.setFont(maruMonica);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.white);

        // TELA DE TÍTULO
        if (gp.gameState == gp.titleState) {
            drawTitleScreen(); // Desenha a tela de título
        }

        // TELA DE JOGO
        if (gp.gameState == gp.playState) {
            // Do playState stuff later
        }

        if (gp.gameState == gp.pauseState) {
            drawPauseScreen(); // Desenha a tela de pausa
        }

        if (gp.gameState == gp.dialogueState) {
            drawDialogueScreen(); // Desenha a tela de diálogo
        }
    }

    public void drawTitleScreen() {
        if (titleScreenState == 0) {
            g2.setColor(new Color(0, 0, 0));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight); // Preenche o fundo com uma cor sólida

            // Título do jogo
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 96F));
            String text = "Bit Hunter";
            int x = getXforCenteredText(text);
            int y = gp.tileSize * 3;

            // Shadow do texto
            g2.setColor(Color.gray);
            g2.drawString(text, x + 5, y + 5);

            // Cor do texto
            g2.setColor(Color.white);
            g2.drawString(text, x, y);

            // Personagem principal
            x = gp.screenWidth / 2 - (gp.tileSize * 2) / 2; // Centraliza horizontalmente
            y += gp.tileSize * 2;
            g2.drawImage(gp.player.down1, x, y, gp.tileSize * 2, gp.tileSize * 2, null);

            // Menu
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48F));

            text = "NOVO JOGO";
            x = getXforCenteredText(text);
            y += gp.tileSize * 3.5;
            g2.drawString(text, x, y);
            if (commandNum == 0) {
                g2.drawString(">", x - gp.tileSize, y); // Indica o comando selecionado
            }

            text = "CARREGAR JOGO";
            x = getXforCenteredText(text);
            y += gp.tileSize;
            g2.drawString(text, x, y);
            if (commandNum == 1) {
                g2.drawString(">", x - gp.tileSize, y);
            }

            text = "SAIR";
            x = getXforCenteredText(text);
            y += gp.tileSize;
            g2.drawString(text, x, y);
            if (commandNum == 2) {
                g2.drawString(">", x - gp.tileSize, y);
            }
        } else if (titleScreenState == 1) {

            // CLASS SELECTION SCREEN
            g2.setColor(Color.white);
            g2.setFont(g2.getFont().deriveFont(42F));

            String text = "Select your class!";
            int x = getXforCenteredText(text);
            int y = gp.tileSize * 3;
            g2.drawString(text, x, y);

            text = "Fighter";
            x = getXforCenteredText(text);
            y += gp.tileSize * 3;
            g2.drawString(text, x, y);
            if (commandNum == 0) {
                g2.drawString(">", x - gp.tileSize, y);
            }

            text = "Thief";
            x = getXforCenteredText(text);
            y += gp.tileSize;
            g2.drawString(text, x, y);
            if (commandNum == 1) {
                g2.drawString(">", x - gp.tileSize, y);
            }

            text = "Sorcerer";
            x = getXforCenteredText(text);
            y += gp.tileSize;
            g2.drawString(text, x, y);
            if (commandNum == 2) {
                g2.drawString(">", x - gp.tileSize, y);
            }

            text = "Back";
            x = getXforCenteredText(text);
            y += gp.tileSize * 2;
            g2.drawString(text, x, y);
            if (commandNum == 3) {
                g2.drawString(">", x - gp.tileSize, y);
            }
        }
    }

    public void drawPauseScreen() {
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 80F));
        String text = "PAUSED";
        int x = getXforCenteredText(text);
        int y = gp.screenHeight / 2;

        g2.drawString(text, x, y);
    }

    public void drawDialogueScreen() {
        // Define posição e tamanho da janela de diálogo
        int x = gp.tileSize * 2;
        int y = gp.tileSize / 2;
        int width = gp.screenWidth - (gp.tileSize * 4);
        int height = gp.tileSize * 4;

        // Desenha fundo e borda arredondada
        drawSubWindow(x, y, width, height);

        // Ajusta fonte e deslocamento interno
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 27F));
        x += gp.tileSize;
        y += gp.tileSize;

        // Renderiza cada linha do texto de diálogo
        for (String line : currentDialogue.split("\n")) {
            g2.drawString(line, x, y);
            y += 40; // Espaçamento entre linhas
        }
    }

    public void drawSubWindow(int x, int y, int width, int height) {
        // Fundo semitransparente
        Color bg = new Color(0, 0, 0, 210);
        g2.setColor(bg);
        g2.fillRoundRect(x, y, width, height, 35, 35);

        // Borda sólida
        Color border = new Color(255, 255, 255);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25);
    }

    public int getXforCenteredText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = gp.screenWidth / 2 - length / 2;
        return x;
    }
}
