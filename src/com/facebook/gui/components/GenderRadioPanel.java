package com.facebook.gui.components;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern radio button panel for gender selection matching Facebook's design
 */
public class GenderRadioPanel extends JPanel {

    private JRadioButton femaleRadio;
    private JRadioButton maleRadio;
    private JRadioButton customRadio;
    private ButtonGroup buttonGroup;
    private Color borderColor = new Color(219, 223, 231);
    private int cornerRadius = 6;

    public GenderRadioPanel() {
        setOpaque(false);
        setLayout(new GridLayout(1, 3, 10, 0));
        setupRadioButtons();
    }

    private void setupRadioButtons() {
        buttonGroup = new ButtonGroup();

        femaleRadio = createStyledRadioButton("Female");
        maleRadio = createStyledRadioButton("Male");
        customRadio = createStyledRadioButton("Custom");

        buttonGroup.add(femaleRadio);
        buttonGroup.add(maleRadio);
        buttonGroup.add(customRadio);

        add(createRadioPanel(femaleRadio));
        add(createRadioPanel(maleRadio));
        add(createRadioPanel(customRadio));
    }

    private JRadioButton createStyledRadioButton(String text) {
        JRadioButton radio = new JRadioButton(text);
        radio.setOpaque(false);
        radio.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        radio.setForeground(new Color(28, 30, 33));
        radio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        radio.setFocusPainted(false);
        return radio;
    }

    private JPanel createRadioPanel(JRadioButton radio) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8)) {
            private boolean isHovered = false;

            {
                setOpaque(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));

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

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        radio.setSelected(true);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw background
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));

                // Draw border
                g2.setColor(isHovered ? new Color(180, 180, 180) : borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2, cornerRadius,
                        cornerRadius));

                g2.dispose();
                super.paintComponent(g);
            }
        };

        panel.add(radio);
        return panel;
    }

    public String getSelectedGender() {
        if (femaleRadio.isSelected())
            return "Female";
        if (maleRadio.isSelected())
            return "Male";
        if (customRadio.isSelected())
            return "Custom";
        return null;
    }

    public boolean isGenderSelected() {
        return buttonGroup.getSelection() != null;
    }

    public void clearSelection() {
        buttonGroup.clearSelection();
    }
}
