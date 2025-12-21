package com.dmitry.project.consoleChat.dekstopChat.client.ui.windows;

import com.dmitry.project.consoleChat.dekstopChat.client.net.NetworkClient;
import com.dmitry.project.consoleChat.dekstopChat.common.SimpleProtocol;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class ChatWindow extends JFrame {

    private final String username;
    private final Color userColor;
    private final String roomName;
    private final NetworkClient client;

    private JTextPane chatPane;
    private StyledDocument doc;
    private JTextField messageInput;
    private Thread receiveThread;
    private boolean running = true;

    public ChatWindow(String username, Color userColor, String roomName, NetworkClient client) {
        this.username = username;
        this.userColor = userColor;
        this.roomName = roomName;
        this.client = client;

        setTitle("Чат: " + roomName + " - " + username);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 500);
        setResizable(false);
        setLocationRelativeTo(null);

        initUI();
        startMessageReceiver();
        addSystemMessage("Добро пожаловать в " + roomName + "!");
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("Комната: " + roomName + " | Вы: "));
        JLabel nameLabel = new JLabel(username);
        nameLabel.setForeground(userColor);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(nameLabel);

        JButton leaveButton = new JButton("Выйти из комнаты");
        leaveButton.setBackground(new Color(200, 50, 50));
        leaveButton.setForeground(Color.WHITE);
        leaveButton.addActionListener(e -> leaveRoom());
        headerPanel.add(infoPanel, BorderLayout.WEST);
        headerPanel.add(leaveButton, BorderLayout.EAST);

        chatPane = new JTextPane();
        chatPane.setEditable(false);
        doc = chatPane.getStyledDocument();
        JScrollPane scrollPane = new JScrollPane(chatPane);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        messageInput = new JTextField();
        messageInput.addActionListener(e -> sendMessage());
        JButton sendButton = new JButton("Отправить");
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.WHITE);
        sendButton.addActionListener(e -> sendMessage());
        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void startMessageReceiver() {
        receiveThread = new Thread(() -> {
            while (running && client.isConnected()) {
                try {
                    String message = client.receive();
                    if (message != null) {
                        handleIncomingMessage(message);
                    }
                } catch (Exception e) {
                    if (running) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this,
                                    "Ошибка соединения: " + e.getMessage());
                            disconnectAndReturn();
                        });
                        break;
                    }
                }
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    private void handleIncomingMessage(String message) {
        String[] parts = SimpleProtocol.parseCommand(message);
        if (parts.length == 0) return;

        switch (parts[0]) {
            case SimpleProtocol.CHAT_MSG:
                if (parts.length >= 4) {
                    String sender = SimpleProtocol.unescape(parts[1]);
                    String colorHex = SimpleProtocol.unescape(parts[2]);
                    StringBuilder textBuilder = new StringBuilder();
                    for (int i = 3; i < parts.length; i++) {
                        textBuilder.append(parts[i]);
                        if (i < parts.length - 1) textBuilder.append("|");
                    }
                    String text = SimpleProtocol.unescape(textBuilder.toString());
                    try {
                        Color color = Color.decode(colorHex);
                        addMessage(sender, color, text);
                    } catch (NumberFormatException e) {
                        addSystemMessage("Ошибка цвета у сообщения от " + sender);
                    }
                }
                break;

            case SimpleProtocol.SYSTEM_MSG:
                if (parts.length >= 2) {
                    StringBuilder textBuilder = new StringBuilder();
                    for (int i = 1; i < parts.length; i++) {
                        textBuilder.append(parts[i]);
                        if (i < parts.length - 1) textBuilder.append("|");
                    }
                    String text = SimpleProtocol.unescape(textBuilder.toString());
                    addSystemMessage(text);
                }
                break;

            case SimpleProtocol.ERROR:
                if (parts.length >= 2) {
                    StringBuilder errorBuilder = new StringBuilder();
                    for (int i = 1; i < parts.length; i++) {
                        errorBuilder.append(parts[i]);
                        if (i < parts.length - 1) errorBuilder.append("|");
                    }
                    String error = SimpleProtocol.unescape(errorBuilder.toString());
                    addSystemMessage("Ошибка: " + error);
                }
                break;
        }
    }

    private void addMessage(String sender, Color senderColor, String text) {
        SwingUtilities.invokeLater(() -> {
            try {
                Style nameStyle = chatPane.addStyle("NameStyle", null);
                StyleConstants.setForeground(nameStyle, senderColor);
                StyleConstants.setBold(nameStyle, true);
                Style textStyle = chatPane.addStyle("TextStyle", null);
                StyleConstants.setForeground(textStyle, Color.BLACK);
                doc.insertString(doc.getLength(), sender + ": ", nameStyle);
                doc.insertString(doc.getLength(), text + "\n", textStyle);
                chatPane.setCaretPosition(doc.getLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void addSystemMessage(String text) {
        SwingUtilities.invokeLater(() -> {
            try {
                Style systemStyle = chatPane.addStyle("SystemStyle", null);
                StyleConstants.setForeground(systemStyle, Color.GRAY);
                StyleConstants.setItalic(systemStyle, true);
                doc.insertString(doc.getLength(), "[Система] " + text + "\n", systemStyle);
                chatPane.setCaretPosition(doc.getLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty()) return;
        messageInput.setText("");
        addMessage(username, userColor, text);

        new Thread(() -> {
            try {
                client.send(SimpleProtocol.createSendMessage(text));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Ошибка отправки: " + e.getMessage());
                    messageInput.setText(text);
                });
            }
        }).start();
    }

    private void leaveRoom() {
        running = false;
        new Thread(() -> {
            try {
                client.send(SimpleProtocol.createLeaveRoom());
                client.receive(500);
            } catch (Exception ignored) {}

            SwingUtilities.invokeLater(() -> {
                dispose();
                new RoomWindow(username, userColor, client).setVisible(true);
            });
        }).start();
    }

    private void disconnectAndReturn() {
        running = false;
        client.disconnect();
        SwingUtilities.invokeLater(() -> {
            dispose();
            new AuthWindow().setVisible(true);
        });
    }

    @Override
    public void dispose() {
        running = false;
        if (receiveThread != null) {
            receiveThread.interrupt();
        }
        super.dispose();
    }
}