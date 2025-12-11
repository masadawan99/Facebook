package com.facebook.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern text field with placeholder, focus animations, and validation support
 */
public class ModernTextField extends JTextField {

    private String placeholder;
    private Color placeholderColor = new Color(150, 150, 150);
    private Color normalBorderColor = new Color(219, 223, 231);
    private Color focusBorderColor = new Color(24, 119, 242);
    private Color errorBorderColor = new Color(220, 53, 69);
    private Color currentBorderColor;
    private int cornerRadius = 8;
    private boolean isFocused = false;
    private boolean hasError = false;
    private Timer borderAnimationTimer;
    private float borderAnimationProgress = 0f;
    private Color targetBorderColor;

    public ModernTextField(String placeholder) {
        this.placeholder = placeholder;
        this.currentBorderColor = normalBorderColor;
        this.targetBorderColor = normalBorderColor;
        setupTextField();
    }

    public ModernTextField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder;
        this.currentBorderColor = normalBorderColor;
        this.targetBorderColor = normalBorderColor;
        setupTextField();
    }

    private void setupTextField() {
        setOpaque(false);
        setBorder(new EmptyBorder(15, 16, 15, 16));
        setFont(new Font("Segoe UI", Font.PLAIN, 17));
        setForeground(new Color(28, 30, 33));
        setCaretColor(new Color(24, 119, 242));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                if (!hasError) {
                    animateBorderColor(focusBorderColor);
                }
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                if (!hasError) {
                    animateBorderColor(normalBorderColor);
                }
                repaint();
            }
        });
    }

    private void animateBorderColor(Color target) {
        targetBorderColor = target;

        if (borderAnimationTimer != null && borderAnimationTimer.isRunning()) {
            borderAnimationTimer.stop();
        }

        final Color startColor = currentBorderColor;
        borderAnimationProgress = 0f;
        final int steps = 10;

        borderAnimationTimer = new Timer(100 / steps, e -> {
            borderAnimationProgress += 1.0f / steps;
            if (borderAnimationProgress >= 1f) {
                borderAnimationProgress = 1f;
                currentBorderColor = targetBorderColor;
                ((Timer) e.getSource()).stop();
            } else {
                currentBorderColor = interpolateColor(startColor, targetBorderColor, borderAnimationProgress);
            }
            repaint();
        });
        borderAnimationTimer.start();
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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // Draw background
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));

        // Draw border
        g2.setColor(currentBorderColor);
        g2.setStroke(new BasicStroke(isFocused ? 2f : 1f));
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius));

        g2.dispose();

        super.paintComponent(g);

        // Draw placeholder
        if (getText().isEmpty() && !isFocused) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2d.setColor(placeholderColor);
            g2d.setFont(getFont());
            Insets insets = getInsets();
            g2d.drawString(placeholder, insets.left, getHeight() / 2 + g2d.getFontMetrics().getAscent() / 2 - 2);
            g2d.dispose();
        }
    }

    public void setError(boolean error) {
        this.hasError = error;
        if (error) {
            animateBorderColor(errorBorderColor);
        } else {
            animateBorderColor(isFocused ? focusBorderColor : normalBorderColor);
        }
    }

    public boolean hasError() {
        return hasError;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public String getPlaceholder() {
        return placeholder;
    }
}
