package com.facebook.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern password field with placeholder, focus animations, and show/hide
 * toggle
 */
public class ModernPasswordField extends JPasswordField {

    private String placeholder;
    private Color placeholderColor = new Color(150, 150, 150);
    private Color normalBorderColor = new Color(219, 223, 231);
    private Color focusBorderColor = new Color(24, 119, 242);
    private Color errorBorderColor = new Color(220, 53, 69);
    private Color currentBorderColor;
    private int cornerRadius = 8;
    private boolean isFocused = false;
    private boolean hasError = false;
    private boolean showPassword = false;
    private Timer borderAnimationTimer;
    private float borderAnimationProgress = 0f;
    private Color targetBorderColor;

    // Eye icon for show/hide
    private Rectangle eyeIconBounds;
    private boolean eyeHovered = false;

    public ModernPasswordField(String placeholder) {
        this.placeholder = placeholder;
        this.currentBorderColor = normalBorderColor;
        this.targetBorderColor = normalBorderColor;
        setupPasswordField();
    }

    public ModernPasswordField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder;
        this.currentBorderColor = normalBorderColor;
        this.targetBorderColor = normalBorderColor;
        setupPasswordField();
    }

    private void setupPasswordField() {
        setOpaque(false);
        setBorder(new EmptyBorder(15, 16, 15, 45)); // Extra right padding for eye icon
        setFont(new Font("Segoe UI", Font.PLAIN, 17));
        setForeground(new Color(28, 30, 33));
        setCaretColor(new Color(24, 119, 242));
        setEchoChar('●');

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

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (eyeIconBounds != null && eyeIconBounds.contains(e.getPoint())) {
                    togglePasswordVisibility();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                eyeHovered = false;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean wasHovered = eyeHovered;
                eyeHovered = eyeIconBounds != null && eyeIconBounds.contains(e.getPoint());
                if (wasHovered != eyeHovered) {
                    setCursor(eyeHovered ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.TEXT_CURSOR));
                    repaint();
                }
            }
        });
    }

    private void togglePasswordVisibility() {
        showPassword = !showPassword;
        setEchoChar(showPassword ? (char) 0 : '●');
        repaint();
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

        // Draw eye icon
        int iconSize = 20;
        int iconX = getWidth() - iconSize - 15;
        int iconY = (getHeight() - iconSize) / 2;
        eyeIconBounds = new Rectangle(iconX - 5, iconY - 5, iconSize + 10, iconSize + 10);

        g2.setColor(eyeHovered ? new Color(24, 119, 242) : new Color(150, 150, 150));
        g2.setStroke(new BasicStroke(1.5f));

        // Draw eye shape
        int eyeCenterX = iconX + iconSize / 2;
        int eyeCenterY = iconY + iconSize / 2;

        // Eye outline
        g2.drawOval(iconX + 2, iconY + 5, iconSize - 4, iconSize - 10);
        // Pupil
        g2.fillOval(eyeCenterX - 3, eyeCenterY - 3, 6, 6);

        if (!showPassword) {
            // Draw line through eye when hidden
            g2.drawLine(iconX + 2, iconY + iconSize - 2, iconX + iconSize - 2, iconY + 2);
        }

        g2.dispose();

        super.paintComponent(g);

        // Draw placeholder
        if (getPassword().length == 0 && !isFocused) {
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

    public String getPasswordText() {
        return new String(getPassword());
    }
}
