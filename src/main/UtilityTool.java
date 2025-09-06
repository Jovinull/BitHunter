package main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class UtilityTool {

    /**
     * Redimensiona a imagem mantendo o tipo do BufferedImage original.
     * 
     * @param original imagem de entrada (não nula)
     * @param width    largura destino (px)
     * @param height   altura destino (px)
     * @return nova imagem escalada
     * @throws NullPointerException     se original for nula
     * @throws IllegalArgumentException se width/height <= 0
     */
    public BufferedImage scaleImage(final BufferedImage original, final int width, final int height) {
        Objects.requireNonNull(original, "original image must not be null");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width/height must be > 0");
        }

        final BufferedImage scaledImage = new BufferedImage(width, height, original.getType());
        final Graphics2D g2 = scaledImage.createGraphics();
        try {
            g2.drawImage(original, 0, 0, width, height, null);
        } finally {
            g2.dispose();
        }
        return scaledImage;
    }
}
