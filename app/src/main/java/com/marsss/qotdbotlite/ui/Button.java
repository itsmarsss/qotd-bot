package com.marsss.qotdbotlite.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Button extends JButton {
    public Button(String text, Color color, Color hover, Color active) {
        super(text);
        setBackground(color);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setPreferredSize(new Dimension(125, 40));
        setFont(getFont().deriveFont(Font.BOLD));
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hover);
                setLocation(getX(), getY() + 1);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(color);
                setLocation(getX(), getY() - 1);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(active);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setBackground(color);
            }
        });
    }
}
