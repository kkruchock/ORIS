package com.dmitry.project.consoleChat.dekstopChat.client;

import com.dmitry.project.consoleChat.dekstopChat.client.ui.windows.AuthWindow;

import javax.swing.*;

public class ClientApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AuthWindow authWindow = new AuthWindow();
            authWindow.setVisible(true);
        });
    }
}