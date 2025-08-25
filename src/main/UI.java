package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.AlphaComposite;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import entity.Entity;
import object.OBJ_Heart;

public class UI {

    GamePanel gp; // Referência ao painel principal do jogo
    Graphics2D g2; // Contexto gráfico
    Font maruMonica, purisaB; // Fontes
    BufferedImage heart_full, heart_half, heart_blank; // Imagens de coração
    public boolean messageOn = false;
    ArrayList<String> message = new ArrayList<>();
    ArrayList<Integer> messageCounter = new ArrayList<>();
    public boolean gameFinished = false;
    public String currentDialogue = "";
    public int commandNum = 0;
    public int titleScreenState = 0;

    // Paleta temática (antiga/épica)
    private final Color COL_BG_DARK_1 = new Color(10, 8, 22);
    private final Color COL_BG_DARK_2 = new Color(36, 18, 8);
    private final Color COL_GOLD_LIGHT = new Color(245, 230, 161);
    private final Color COL_GOLD = new Color(212, 169, 71);
    private final Color COL_GOLD_DARK = new Color(138, 107, 42);
    private final Color COL_BORDER_SOFT = new Color(255, 255, 255, 220);
    private final Color COL_PARCHMENT_1 = new Color(240, 225, 195, 235);
    private final Color COL_PARCHMENT_2 = new Color(215, 195, 150, 235);

    // Ajuste fino: subir o fundo da seleção (sem mudar tamanho)
    private static final float MENU_PILL_TOP_FACTOR = 0.90f; // antes 0.75f

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

        // Criando HUD
        Entity heart = new OBJ_Heart(gp);
        heart_full = heart.image;
        heart_half = heart.image2;
        heart_blank = heart.image3;
    }

    // Exibe uma mensagem temporária (ex.: "Porta destrancada!")
    public void addMessage(String text) {

        message.add(text);
        messageCounter.add(0);
    }

    // Desenho principal
    public void draw(Graphics2D g2) {
        this.g2 = g2;

        // Suavizações
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fonte base
        g2.setFont(maruMonica != null ? maruMonica : g2.getFont());
        g2.setColor(Color.white);

        // Telas
        if (gp.gameState == gp.titleState) {
            drawTitleScreen();
        }

        if (gp.gameState == gp.playState) {
            drawPlayerLife();
            drawMessage();
        }

        if (gp.gameState == gp.pauseState) {
            drawPlayerLife();
            drawPauseScreen();
        }

        if (gp.gameState == gp.dialogueState) {
            drawPlayerLife();
            drawDialogueScreen();
        }

        if (gp.gameState == gp.characterState) {
            drawCharacterScreen();
        }
    }

    public void drawPlayerLife() {

        int x = gp.tileSize / 2;
        int y = gp.tileSize / 2;
        int i = 0;

        // DRAW MAX LIFE
        while (i < gp.player.maxLife / 2) {
            g2.drawImage(heart_blank, x, y, null);
            i++;
            x += gp.tileSize;
        }

        // RESET
        x = gp.tileSize / 2;
        y = gp.tileSize / 2;
        i = 0;

        // DRAW CURRENT LIFE
        while (i < gp.player.life) {
            g2.drawImage(heart_half, x, y, null);
            i++;
            if (i < gp.player.life) {
                g2.drawImage(heart_full, x, y, null);
            }
            i++;
            x += gp.tileSize;
        }
    }

    public void drawMessage() {

        int messageX = gp.tileSize;
        int messageY = gp.tileSize * 4;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32F));

        for (int i = 0; i < message.size(); i++) {

            if (message.get(i) != null) {

                g2.setColor(Color.black);
                g2.drawString(message.get(i), messageX + 2, messageY + 2);

                g2.setColor(Color.white);
                g2.drawString(message.get(i), messageX, messageY);

                int counter = messageCounter.get(i) + 1; // messageCounter++
                messageCounter.set(i, counter); // set the counter to the array
                messageY += 50;

                if (messageCounter.get(i) > 180) {
                    message.remove(i);
                    messageCounter.remove(i);
                }
            }
        }
    }

    // ---------- TITLE SCREEN ----------
    public void drawTitleScreen() {
        // Fundo com gradiente e vinheta
        drawEpicBackground();

        if (titleScreenState == 0) {
            // Título
            g2.setFont((purisaB != null ? purisaB : g2.getFont()).deriveFont(Font.BOLD, 96F));
            String text = "Bit Hunter";
            int y = gp.tileSize * 3;
            drawGoldTitleCentered(text, y);

            // Personagem principal
            int xImg = gp.screenWidth / 2 - (gp.tileSize * 2) / 2;
            int yImg = y + gp.tileSize * 2;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
            g2.drawImage(gp.player.down1, xImg, yImg, gp.tileSize * 2, gp.tileSize * 2, null);
            g2.setComposite(AlphaComposite.SrcOver);

            // Menu
            g2.setFont((purisaB != null ? purisaB : g2.getFont()).deriveFont(Font.BOLD, 44F));
            int yMenu = yImg + (int) (gp.tileSize * 3.5);

            drawMenuItem("NOVO JOGO", yMenu, commandNum == 0);
            drawMenuItem("CARREGAR JOGO", yMenu + gp.tileSize, commandNum == 1);
            drawMenuItem("SAIR", yMenu + gp.tileSize * 2, commandNum == 2);

            // Dica no rodapé
            g2.setFont((maruMonica != null ? maruMonica : g2.getFont()).deriveFont(Font.PLAIN, 18F));
            String hint = "Use W/S ou ↑/↓ para navegar • ENTER para confirmar";
            int xHint = getXforCenteredText(hint);
            int yHint = gp.screenHeight - gp.tileSize;
            drawTextWithShadow(hint, xHint, yHint, new Color(230, 230, 230), new Color(0, 0, 0, 140), 2);
        } else if (titleScreenState == 1) {
            // Seleção de classe
            g2.setFont((purisaB != null ? purisaB : g2.getFont()).deriveFont(42F));
            String text = "Selecione sua classe!";
            int y = gp.tileSize * 3;
            drawTextWithOutlineCentered(text, y, new Color(240, 235, 220), new Color(0, 0, 0, 170), 2.2f);

            int yStart = y + gp.tileSize * 3;

            drawMenuItem("Guerreiro", yStart, commandNum == 0);
            drawMenuItem("Ladrão", yStart + gp.tileSize, commandNum == 1);
            drawMenuItem("Feiticeiro", yStart + gp.tileSize * 2, commandNum == 2);

            drawMenuItem("Voltar", yStart + gp.tileSize * 4, commandNum == 3);
        }
    }

    // ---------- PAUSE ----------
    public void drawPauseScreen() {
        // Sobreposição escura
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g2.setColor(Color.black);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        g2.setComposite(AlphaComposite.SrcOver);

        // Caixa central
        int w = (int) (gp.screenWidth * 0.6);
        int h = gp.tileSize * 4;
        int x = (gp.screenWidth - w) / 2;
        int y = (gp.screenHeight - h) / 2;
        drawSubWindow(x, y, w, h);

        // Texto
        g2.setFont((purisaB != null ? purisaB : g2.getFont()).deriveFont(Font.PLAIN, 80F));
        String text = "PAUSADO";
        int tx = getXforCenteredText(text);
        int ty = y + (int) (h * 0.62);
        drawTextWithOutline(text, tx, ty, COL_GOLD_LIGHT, new Color(0, 0, 0, 180), 3.2f);
    }

    // ---------- DIALOGUE ----------
    public void drawDialogueScreen() {
        int x = gp.tileSize * 2;
        int y = gp.tileSize / 2;
        int width = gp.screenWidth - (gp.tileSize * 4);
        int height = gp.tileSize * 4;

        drawSubWindow(x, y, width, height);

        g2.setFont((maruMonica != null ? maruMonica : g2.getFont()).deriveFont(Font.BOLD, 27F));
        int innerX = x + gp.tileSize;
        int innerY = y + gp.tileSize;

        for (String line : currentDialogue.split("\n")) {
            drawTextWithShadow(line, innerX, innerY, new Color(35, 25, 10), new Color(255, 255, 255, 60), 1);
            innerY += 40;
        }
    }

    // ---------- SUBWINDOW ESTILIZADA (pergaminho + moldura) ----------
    public void drawSubWindow(int x, int y, int width, int height) {
        // Fundo pergaminho com leve gradiente
        LinearGradientPaint parchment = new LinearGradientPaint(
                x, y, x, y + height,
                new float[] { 0f, 1f },
                new Color[] { COL_PARCHMENT_1, COL_PARCHMENT_2 },
                CycleMethod.NO_CYCLE);
        g2.setPaint(parchment);
        g2.fillRoundRect(x, y, width, height, 28, 28);

        // Borda dupla (ouro + branco suave)
        g2.setStroke(new BasicStroke(4f));
        g2.setColor(COL_GOLD);
        g2.drawRoundRect(x + 2, y + 2, width - 4, height - 4, 24, 24);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(COL_BORDER_SOFT);
        g2.drawRoundRect(x + 6, y + 6, width - 12, height - 12, 18, 18);

        // Sombra interna leve (vinheta interna)
        RadialGradientPaint innerShade = new RadialGradientPaint(
                x + width / 2f, y + height / 2f, Math.max(width, height) / 1.2f,
                new float[] { 0.7f, 1f },
                new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 50) },
                CycleMethod.NO_CYCLE);
        g2.setPaint(innerShade);
        g2.fillRoundRect(x + 4, y + 4, width - 8, height - 8, 24, 24);
    }

    // ---------- HELPERS VISUAIS ----------
    private void drawEpicBackground() {
        // Gradiente de fundo
        LinearGradientPaint bg = new LinearGradientPaint(
                0, 0, 0, gp.screenHeight,
                new float[] { 0f, 0.5f, 1f },
                new Color[] { COL_BG_DARK_1, COL_BG_DARK_2, COL_BG_DARK_1 },
                CycleMethod.NO_CYCLE);
        g2.setPaint(bg);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Vinheta (borda escura)
        RadialGradientPaint vignette = new RadialGradientPaint(
                gp.screenWidth / 2f, gp.screenHeight / 2f, Math.max(gp.screenWidth, gp.screenHeight) / 1.2f,
                new float[] { 0f, 0.85f, 1f },
                new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 100), new Color(0, 0, 0, 180) },
                CycleMethod.NO_CYCLE);
        g2.setPaint(vignette);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
    }

    private void drawGoldTitleCentered(String text, int baselineY) {
        int x = getXforCenteredText(text);
        drawGoldTitle(text, x, baselineY);
    }

    private void drawGoldTitle(String text, int x, int y) {
        // Shape do texto
        GlyphVector gv = g2.getFont().createGlyphVector(g2.getFontRenderContext(), text);
        Shape textShape = gv.getOutline(x, y);

        // Sombra
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g2.setColor(Color.black);
        g2.translate(5, 5);
        g2.fill(textShape);
        g2.translate(-5, -5);
        g2.setComposite(AlphaComposite.SrcOver);

        // Preenchimento em ouro (gradiente)
        Rectangle2D bounds = textShape.getBounds2D();
        LinearGradientPaint gold = new LinearGradientPaint(
                (float) bounds.getMinX(), (float) bounds.getMinY(), (float) bounds.getMinX(), (float) bounds.getMaxY(),
                new float[] { 0f, 0.5f, 1f },
                new Color[] { COL_GOLD_LIGHT, COL_GOLD, COL_GOLD_DARK },
                CycleMethod.NO_CYCLE);
        g2.setPaint(gold);
        g2.fill(textShape);

        // Contorno
        g2.setStroke(new BasicStroke(3.2f));
        g2.setColor(new Color(0, 0, 0, 200));
        g2.draw(textShape);
    }

    private void drawMenuItem(String text, int y, boolean selected) {
        int w = (int) (gp.screenWidth * 0.6);
        int x = (gp.screenWidth - w) / 2;

        // Pílula de seleção (apenas ajusta posição para cima; tamanho igual)
        if (selected) {
            int pillTop = y - (int) (gp.tileSize * MENU_PILL_TOP_FACTOR); // <- subiu um pouco
            int pillH = (int) (gp.tileSize * 1.2);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
            g2.setColor(COL_GOLD);
            g2.fillRoundRect(x, pillTop, w, pillH, 24, 24);
            g2.setComposite(AlphaComposite.SrcOver);

            // Borda suave
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(255, 255, 255, 80));
            g2.drawRoundRect(x, pillTop, w, pillH, 24, 24);
        }

        // Texto centralizado
        int tx = getXforCenteredText(text);
        Color fill = selected ? new Color(255, 245, 210) : new Color(230, 230, 230);
        Color outline = new Color(0, 0, 0, selected ? 200 : 140);
        float ow = selected ? 2.2f : 1.6f;

        drawTextWithOutline(text, tx, y, fill, outline, ow);

        // Indicador à esquerda (triângulo) — permanece alinhado ao baseline do texto
        if (selected) {
            int triX = x - (int) (gp.tileSize * 0.25);
            int triY = y - (int) (gp.tileSize * 0.3);
            int triH = (int) (gp.tileSize * 0.6);
            int[] xs = { triX, triX + (int) (gp.tileSize * 0.4), triX };
            int[] ys = { triY, triY + triH / 2, triY + triH };
            g2.setColor(COL_GOLD);
            g2.fillPolygon(xs, ys, 3);
            g2.setColor(new Color(0, 0, 0, 160));
            g2.drawPolygon(xs, ys, 3);
        }
    }

    private void drawTextWithShadow(String text, int x, int y, Color fill, Color shadow, int offset) {
        g2.setColor(shadow);
        g2.drawString(text, x + offset, y + offset);
        g2.setColor(fill);
        g2.drawString(text, x, y);
    }

    private void drawTextWithOutlineCentered(String text, int y, Color fill, Color outline, float outlineW) {
        int x = getXforCenteredText(text);
        drawTextWithOutline(text, x, y, fill, outline, outlineW);
    }

    private void drawTextWithOutline(String text, int x, int y, Color fill, Color outline, float outlineW) {
        GlyphVector gv = g2.getFont().createGlyphVector(g2.getFontRenderContext(), text);
        Shape shape = gv.getOutline(x, y);

        // Sombra leve
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g2.setColor(new Color(0, 0, 0, 220));
        g2.translate(2, 2);
        g2.fill(shape);
        g2.translate(-2, -2);
        g2.setComposite(AlphaComposite.SrcOver);

        // Preenchimento
        g2.setColor(fill);
        g2.fill(shape);

        // Contorno
        g2.setStroke(new BasicStroke(outlineW));
        g2.setColor(outline);
        g2.draw(shape);
    }

    // ---------- UTIL ----------
    public int getXforCenteredText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.screenWidth / 2 - length / 2;
    }

    public void drawCharacterScreen() {

        // CREATE A FRAME
        final int frameX = gp.tileSize;
        final int frameY = gp.tileSize;
        final int frameWidth = gp.tileSize * 5;
        final int frameHeight = gp.tileSize * 10;
        drawSubWindow(frameX, frameY, frameWidth, frameHeight);

        // TEXT
        g2.setColor(Color.white);
        g2.setFont(g2.getFont().deriveFont(32F));

        int textX = frameX + 20;
        int textY = frameY + gp.tileSize;
        final int lineHeight = 32;

        // NAMES
        g2.drawString("Level", textX, textY);
        textY += lineHeight;
        g2.drawString("Life", textX, textY);
        textY += lineHeight;
        g2.drawString("Strength", textX, textY);
        textY += lineHeight;
        g2.drawString("Dexterity", textX, textY);
        textY += lineHeight;
        g2.drawString("Attack", textX, textY);
        textY += lineHeight;
        g2.drawString("Defense", textX, textY);
        textY += lineHeight;
        g2.drawString("Exp", textX, textY);
        textY += lineHeight;
        g2.drawString("Next Level", textX, textY);
        textY += lineHeight;
        g2.drawString("Coin", textX, textY);
        textY += lineHeight + 20;
        g2.drawString("Weapon", textX, textY);
        textY += lineHeight + 15;
        g2.drawString("Shield", textX, textY);
        textY += lineHeight;

        // VALUES
        int tailX = (frameX + frameWidth) - 30;
        // Reset textY
        textY = frameY + gp.tileSize;
        String value;

        value = String.valueOf(gp.player.level);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.life + "/" + gp.player.maxLife);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.strength);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.dexterity);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.attack);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.defense);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.exp);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.nextLevelExp);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.coin);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        g2.drawImage(gp.player.currentWeapon.down1, tailX - gp.tileSize, textY - 14, null);
        textY += gp.tileSize;
        g2.drawImage(gp.player.currentShield.down1, tailX - gp.tileSize, textY - 14, null);
    }

    public int getXforAlignToRightText(String text, int tailX) {

        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = tailX - length;
        return x;
    }
}
