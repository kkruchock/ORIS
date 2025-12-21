package com.dmitry.project.consoleChat.dekstopChat.client.ui.windows;

import com.dmitry.project.consoleChat.dekstopChat.client.net.NetworkClient;
import com.dmitry.project.consoleChat.dekstopChat.common.SimpleProtocol;

import javax.swing.*;
import java.awt.*;

public class AuthWindow extends JFrame {
    private JTextField usernameInput;
    private JButton colorButton;
    private Color selectedColor;
    private JButton connectButton;

    public AuthWindow() {
        setTitle("Авторизация в чате");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setResizable(false);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel usernameLabel = new JLabel("Ваше имя:");
        usernameInput = new JTextField();
        usernameInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        colorButton = new JButton("Выберите ваш цвет");
        colorButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        colorButton.addActionListener(e -> chooseColor());

        connectButton = new JButton("Подключиться к чату");
        connectButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        connectButton.setBackground(new Color(50, 150, 50));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFont(new Font("Arial", Font.BOLD, 14));
        connectButton.addActionListener(e -> connectToServer());

        mainPanel.add(usernameLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(usernameInput);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(colorButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(connectButton);

        add(mainPanel);
    }

    private void chooseColor() {
        Color newColor = JColorChooser.showDialog(this, "Выберите ваш цвет",
                selectedColor != null ? selectedColor : Color.BLUE);
        if (newColor != null) {
            selectedColor = newColor;
            colorButton.setBackground(newColor);
            colorButton.setForeground(getContrastColor(newColor));
        }
    }

    private Color getContrastColor(Color color) {
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }

    private void connectToServer() {
        String username = usernameInput.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите имя!");
            return;
        }

        if (selectedColor == null) {
            JOptionPane.showMessageDialog(this, "Выберите цвет!");
            return;
        }

        String colorHex = String.format("#%02X%02X%02X",
                selectedColor.getRed(),
                selectedColor.getGreen(),
                selectedColor.getBlue());

        connectButton.setEnabled(false);
        connectButton.setText("Подключаемся...");

        new Thread(() -> {
            try {
                NetworkClient client = new NetworkClient();
                if (!client.connect("localhost", 1010)) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Не удалось подключиться к серверу");
                        connectButton.setEnabled(true);
                        connectButton.setText("Подключиться к чату");
                    });
                    return;
                }

                String authCommand = SimpleProtocol.createAuth(username, colorHex);
                client.send(authCommand);

                String response = client.receive(5000);

                if (response != null && response.equals("OK")) {
                    SwingUtilities.invokeLater(() -> {
                        dispose();
                        new RoomWindow(username, selectedColor, client).setVisible(true);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Ошибка авторизации: " + response);
                        connectButton.setEnabled(true);
                        connectButton.setText("Подключиться к чату");
                        client.disconnect();
                    });
                }

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
                    connectButton.setEnabled(true);
                    connectButton.setText("Подключиться к чату");
                });
            }
        }).start();
    }
}