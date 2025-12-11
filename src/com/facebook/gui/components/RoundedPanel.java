package com.facebook.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom rounded panel with shadow effect for Facebook-style cards
 */
public class RoundedPanel extends JPanel {

    private int cornerRadius;
    private Color shadowColor;
    private boolean drawShadow;
    private int shadowSize;

    public RoundedPanel(int radius) {
        this(radius, true);
    }

    public RoundedPanel(int radius, boolean shadow) {
        this.cornerRadius = radius;
        this.drawShadow = shadow;
        this.shadowColor = new Color(0, 0, 0, 20);
        this.shadowSize = 5;
        setOpaque(false);
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

        // Draw shadow
        if (drawShadow) {
            for (int i = 0; i < shadowSize; i++) {
                g2.setColor(new Color(0, 0, 0, 10 - i * 2));
                g2.fill(new RoundRectangle2D.Double(
                        i, i + 2,
                        width - i * 2, height - i * 2,
                        cornerRadius, cornerRadius));
            }
        }

        // Draw main panel
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Double(
                0, 0,
                width - (drawShadow ? shadowSize : 0),
                height - (drawShadow ? shadowSize : 0),
                cornerRadius, cornerRadius));

        g2.dispose();
        super.paintComponent(g);
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public void setShadow(boolean shadow) {
        this.drawShadow = shadow;
        repaint();
    }
}
