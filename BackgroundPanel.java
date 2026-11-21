package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * BackgroundPanel.java
 * Draws a picture behind everything placed on it.
 * The picture fills the panel without stretching (extra edges are cropped).
 */
public class BackgroundPanel extends JPanel {

    private BufferedImage image;
    private final int overlayAlpha;  // darkness of the layer over the photo (0 = none, 255 = black)
    private final float anchorX;     // which part of the photo to keep: 0 = left, 0.5 = middle, 1 = right

    public BackgroundPanel(String imagePath, LayoutManager layout, int overlayAlpha, float anchorX) {
        super(layout);
        this.overlayAlpha = overlayAlpha;
        this.anchorX = anchorX;
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (Exception e) {
            System.out.println("Could not load background image: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) return; // image not found -> plain background, app still works

        int pw = getWidth(), ph = getHeight();
        double scale = Math.max((double) pw / image.getWidth(), (double) ph / image.getHeight());
        int w = (int) (image.getWidth() * scale);
        int h = (int) (image.getHeight() * scale);
        int x = -(int) ((w - pw) * anchorX);
        int y = (ph - h) / 2;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(image, x, y, w, h, null);
        g2.setColor(new Color(0, 0, 0, overlayAlpha));
        g2.fillRect(0, 0, pw, ph);
        g2.dispose();
    }
}