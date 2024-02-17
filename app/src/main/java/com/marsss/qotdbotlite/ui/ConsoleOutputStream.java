package com.marsss.qotdbotlite.ui;

import javax.swing.*;
import java.io.OutputStream;

public class ConsoleOutputStream extends OutputStream {
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
