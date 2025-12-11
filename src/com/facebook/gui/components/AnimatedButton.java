package com.facebook.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom animated button with hover effects, color transitions, and ripple
 * animation
 */
public class AnimatedButton extends JButton {

    private Color normalColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color currentColor;
    private Color targetColor;
    private int cornerRadius = 6;
    private Timer animationTimer;
    private float animationProgress = 0f;
    private static final int ANIMATION_DURATION = 150; // ms
    private static final int ANIMATION_STEPS = 10;

    // Ripple effect
    private Point rippleCenter;
    private float rippleRadius = 0;
    private float maxRippleRadius = 0;
    private Timer rippleTimer;
    private boolean isRippling = false;

    public AnimatedButton(String text) {
        super(text);
        setupButton();
    }

    public AnimatedButton(String text, Color bgColor, Color hoverColor) {
        super(text);
        this.normalColor = bgColor;
        this.hoverColor = hoverColor;
        this.pressedColor = darkenColor(bgColor, 0.15f);
        this.currentColor = bgColor;
        this.targetColor = bgColor;
        setupButton();
    }

    private void setupButton() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 20));

        if (normalColor == null) {
            normalColor = new Color(24, 119, 242);
            hoverColor = new Color(22, 100, 200);
            pressedColor = new Color(18, 80, 170);
            currentColor = normalColor;
            targetColor = normalColor;
        }

        // Mouse listeners for hover animation
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animateToColor(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                animateToColor(normalColor);
                stopRipple();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                currentColor = pressedColor;
                startRipple(e.getPoint());
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                animateToColor(hoverColor);
            }
        });
    }

    private void animateToColor(Color target) {
        this.targetColor = target;

        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        final Color startColor = currentColor;
        animationProgress = 0f;

        animationTimer = new Timer(ANIMATION_DURATION / ANIMATION_STEPS, e -> {
            animationProgress += 1.0f / ANIMATION_STEPS;
            if (animationProgress >= 1f) {
                animationProgress = 1f;
                currentColor = targetColor;
                ((Timer) e.getSource()).stop();
            } else {
                currentColor = interpolateColor(startColor, targetColor, animationProgress);
            }
            repaint();
        });
        animationTimer.start();
    }

    private void startRipple(Point center) {
        rippleCenter = center;
        rippleRadius = 0;
        maxRippleRadius = (float) Math.hypot(getWidth(), getHeight());
        isRippling = true;

        if (rippleTimer != null && rippleTimer.isRunning()) {
            rippleTimer.stop();
        }

        rippleTimer = new Timer(16, e -> { // ~60fps
            rippleRadius += maxRippleRadius / 15;
            if (rippleRadius >= maxRippleRadius) {
                stopRipple();
            }
            repaint();
        });
        rippleTimer.start();
    }

    private void stopRipple() {
        isRippling = false;
        if (rippleTimer != null) {
            rippleTimer.stop();
        }
        repaint();
    }

    private Color interpolateColor(Color c1, Color c2, float ratio) {
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * ratio);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * ratio);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * ratio);
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private Color darkenColor(Color color, float factor) {
        return new Color(
                Math.max(0, (int) (color.getRed() * (1 - factor))),
                Math.max(0, (int) (color.getGreen() * (1 - factor))),
                Math.max(0, (int) (color.getBlue() * (1 - factor))));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // Draw button background
        g2.setColor(currentColor);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

        // Draw ripple effect
        if (isRippling && rippleCenter != null) {
            g2.setClip(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
            float alpha = 1f - (rippleRadius / maxRippleRadius);
            g2.setColor(new Color(255, 255, 255, (int) (50 * alpha)));
            g2.fillOval(
                    (int) (rippleCenter.x - rippleRadius),
                    (int) (rippleCenter.y - rippleRadius),
                    (int) (rippleRadius * 2),
                    (int) (rippleRadius * 2));
        }

        // Draw text
        g2.setFont(getFont());
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), x, y);

        g2.dispose();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public void setColors(Color normal, Color hover) {
        this.normalColor = normal;
        this.hoverColor = hover;
        this.pressedColor = darkenColor(normal, 0.15f);
        this.currentColor = normal;
        this.targetColor = normal;
        repaint();
    }
}
