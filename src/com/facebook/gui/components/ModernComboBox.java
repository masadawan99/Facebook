package com.facebook.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern styled combo box matching Facebook's design
 */
public class ModernComboBox<E> extends JComboBox<E> {

    private Color normalBorderColor = new Color(219, 223, 231);
    private Color hoverBorderColor = new Color(180, 180, 180);
    private Color focusBorderColor = new Color(24, 119, 242);
    private Color currentBorderColor;
    private int cornerRadius = 6;
    private boolean isHovered = false;
    private boolean isFocused = false;

    public ModernComboBox() {
        this.currentBorderColor = normalBorderColor;
        setupComboBox();
    }

    public ModernComboBox(E[] items) {
        super(items);
        this.currentBorderColor = normalBorderColor;
        setupComboBox();
    }

    private void setupComboBox() {
        setOpaque(false);
        setBackground(Color.WHITE);
        setForeground(new Color(28, 30, 33));
        setFont(new Font("Segoe UI", Font.PLAIN, 15));
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        setUI(new ModernComboBoxUI());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                if (!isFocused) {
                    currentBorderColor = hoverBorderColor;
                }
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                if (!isFocused) {
                    currentBorderColor = normalBorderColor;
                }
                repaint();
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                currentBorderColor = focusBorderColor;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                currentBorderColor = isHovered ? hoverBorderColor : normalBorderColor;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));

        // Draw border
        g2.setColor(currentBorderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius));

        g2.dispose();
        super.paintComponent(g);
    }

    private class ModernComboBoxUI extends BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton() {
                @Override
                public void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.setColor(getBackground());
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    // Draw arrow
                    g2.setColor(new Color(100, 100, 100));
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    int[] xPoints = { centerX - 5, centerX + 5, centerX };
                    int[] yPoints = { centerY - 2, centerY - 2, centerY + 4 };
                    g2.fillPolygon(xPoints, yPoints, 3);

                    g2.dispose();
                }
            };
            button.setBackground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            return button;
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox) {
                @Override
                protected JScrollPane createScroller() {
                    JScrollPane scroller = super.createScroller();
                    scroller.setBorder(BorderFactory.createLineBorder(new Color(219, 223, 231)));
                    return scroller;
                }
            };
            popup.setBorder(BorderFactory.createLineBorder(new Color(219, 223, 231)));
            return popup;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // Don't paint default background
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            Object value = comboBox.getSelectedItem();
            if (value != null) {
                g2.setColor(comboBox.getForeground());
                g2.setFont(comboBox.getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = bounds.y + (bounds.height + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(value.toString(), bounds.x + 12, y);
            }

            g2.dispose();
        }
    }
}
