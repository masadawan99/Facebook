package com.facebook.gui.components;

import com.facebook.gui.FacebookGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Icon button for top navigation bar with badge support
 */
public class IconButton extends JButton {

    private String icon;
    private String tooltip;
    private boolean isActive;
    private boolean isHovered;
    private int badgeCount = 0;

    public IconButton(String icon, String tooltip, boolean active) {
        this.icon = icon;
        this.tooltip = tooltip;
        this.isActive = active;

        setPreferredSize(new Dimension(112, 40));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText(tooltip);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int width = getWidth();
        int height = getHeight();

        // Draw background
        if (isActive) {
            g2.setColor(new Color(235, 243, 255));
            g2.fillRoundRect(0, 0, width, height, 8, 8);
        } else if (isHovered) {
            g2.setColor(new Color(244, 244, 244));
            g2.fillRoundRect(0, 0, width, height, 8, 8);
        }

        // Draw bottom border for active
        if (isActive) {
            g2.setColor(FacebookGUI.FB_BLUE);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(0, height - 2, width, height - 2);
        }

        // Draw icon
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        g2.setColor(isActive ? FacebookGUI.FB_BLUE : new Color(101, 103, 107));
        FontMetrics fm = g2.getFontMetrics();
        int iconX = (width - fm.stringWidth(icon)) / 2;
        int iconY = (height + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(icon, iconX, iconY);

        // Draw badge if count > 0
        if (badgeCount > 0) {
            int badgeSize = 20;
            int badgeX = width - badgeSize - 5;
            int badgeY = 5;

            g2.setColor(new Color(250, 45, 70));
            g2.fillOval(badgeX, badgeY, badgeSize, badgeSize);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String countStr = badgeCount > 9 ? "9+" : String.valueOf(badgeCount);
            FontMetrics badgeFm = g2.getFontMetrics();
            int textX = badgeX + (badgeSize - badgeFm.stringWidth(countStr)) / 2;
            int textY = badgeY + (badgeSize + badgeFm.getAscent() - badgeFm.getDescent()) / 2;
            g2.drawString(countStr, textX, textY);
        }

        g2.dispose();
    }

    public void setActive(boolean active) {
        this.isActive = active;
        repaint();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setBadgeCount(int count) {
        this.badgeCount = count;
        repaint();
    }

    public int getBadgeCount() {
        return badgeCount;
    }
}
