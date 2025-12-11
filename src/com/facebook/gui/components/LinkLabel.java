package com.facebook.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * Animated link label with hover underline effect
 */
public class LinkLabel extends JLabel {

    private Color normalColor;
    private Color hoverColor;
    private boolean isHovered = false;
    private boolean underlineOnHover = true;
    private ActionListener actionListener;

    public LinkLabel(String text) {
        this(text, new Color(24, 119, 242), new Color(22, 100, 200));
    }

    public LinkLabel(String text, Color normalColor, Color hoverColor) {
        super(text);
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
        setupLabel();
    }

    private void setupLabel() {
        setForeground(normalColor);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFont(new Font("Segoe UI", Font.PLAIN, 14));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                setForeground(hoverColor);
                if (underlineOnHover) {
                    applyUnderline(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                setForeground(normalColor);
                if (underlineOnHover) {
                    applyUnderline(false);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (actionListener != null) {
                    actionListener.actionPerformed(new ActionEvent(
                            LinkLabel.this,
                            ActionEvent.ACTION_PERFORMED,
                            getText()));
                }
            }
        });
    }

    private void applyUnderline(boolean underline) {
        Font font = getFont();
        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.UNDERLINE, underline ? TextAttribute.UNDERLINE_ON : -1);
        setFont(font.deriveFont(attributes));
    }

    public void addActionListener(ActionListener listener) {
        this.actionListener = listener;
    }

    public void setUnderlineOnHover(boolean underline) {
        this.underlineOnHover = underline;
    }

    public void setColors(Color normal, Color hover) {
        this.normalColor = normal;
        this.hoverColor = hover;
        setForeground(isHovered ? hoverColor : normalColor);
    }
}
