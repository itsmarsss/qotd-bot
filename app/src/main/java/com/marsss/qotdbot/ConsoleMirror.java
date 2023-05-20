package com.marsss.qotdbot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class ConsoleMirror extends JFrame {
    private JTextArea textArea;

    public ConsoleMirror() {
        super("QOTD Bot Console");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));

        PrintStream printStream = new PrintStream(new ConsoleOutputStream(textArea));
        System.setOut(printStream);

        JScrollPane scrollPane = new JScrollPane(textArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                QOTDBot.start();
            }
        });

        buttonPanel.add(startButton, BorderLayout.WEST);

        JButton editButton = new JButton("Edit [config.yml]");
        buttonPanel.add(editButton, BorderLayout.EAST);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(new Dimension(500, 250));
        setVisible(true);
    }
}

class ConsoleOutputStream extends OutputStream {
    private JTextArea textArea;

    public ConsoleOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        textArea.append(String.valueOf((char) b));

        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}