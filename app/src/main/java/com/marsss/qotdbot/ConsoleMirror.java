package com.marsss.qotdbot;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class ConsoleMirror extends JFrame {
    private final JTextArea textArea;

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
        startButton.addActionListener(actionEvent -> QOTDBot.start());

        buttonPanel.add(startButton, BorderLayout.WEST);

        JButton editButton = new JButton("Edit [config.yml]");
        editButton.addActionListener(actionEvent -> {
            System.out.println();
            ProcessBuilder pb = new ProcessBuilder("Notepad.exe", QOTDBot.getParent() + "/config.yml");
            try {
                pb.start();

                System.out.println("You will need to restart the program for new changes to take place.");
                JOptionPane.showMessageDialog(null,
                        "You will need to restart the program for new changes to take place.",
                        "QOTD BOT Warning",
                        JOptionPane.WARNING_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Notepad.exe not found:");
                System.out.println("\tUnable to open: " + QOTDBot.getParent() + "/config.yml");
                JOptionPane.showMessageDialog(null,
                        "Notepad.exe not found: Unable to open: " + QOTDBot.getParent() + "/config.yml",
                        "QOTD BOT Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(editButton, BorderLayout.EAST);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(new Dimension(500, 250));
        setVisible(true);
    }
}

class ConsoleOutputStream extends OutputStream {
    private final JTextArea textArea;

    public ConsoleOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) {
        textArea.append(String.valueOf((char) b));

        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}