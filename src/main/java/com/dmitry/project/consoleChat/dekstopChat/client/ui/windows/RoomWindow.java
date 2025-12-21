package com.dmitry.project.consoleChat.dekstopChat.client.ui.windows;

import com.dmitry.project.consoleChat.dekstopChat.client.net.NetworkClient;
import com.dmitry.project.consoleChat.dekstopChat.common.SimpleProtocol;

import javax.swing.*;
import java.awt.*;

public class RoomWindow extends JFrame {

    private final String username;
    private final Color userColor;
    private final NetworkClient client;

    private DefaultListModel<String> roomListModel;
    private JList<String> roomList;
    private JButton refreshButton;
    private JButton joinButton;

    public RoomWindow(String username, Color userColor, NetworkClient client) {
        this.username = username;
        this.userColor = userColor;
        this.client = client;

        setTitle("Выбор комнаты - " + username);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 400);
        setResizable(false);
        setLocationRelativeTo(null);

        initUI();
        loadRooms();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Выберите комнату или создайте новую");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        userPanel.add(new JLabel("Вы: " + username));
        JLabel colorSquare = new JLabel("   ");
        colorSquare.setOpaque(true);
        colorSquare.setBackground(userColor);
        colorSquare.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        userPanel.add(colorSquare);

        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(roomList);

        JPanel createPanel = new JPanel(new BorderLayout(5, 0));
        JTextField roomNameInput = new JTextField();
        JButton createButton = new JButton("Создать комнату");
        createButton.setBackground(new Color(50, 150, 50));
        createButton.setForeground(Color.WHITE);
        createButton.addActionListener(e -> {
            String roomName = roomNameInput.getText().trim();
            if (roomName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите название комнаты!");
                return;
            }
            createRoom(roomName);
            roomNameInput.setText("");
        });

        createPanel.add(new JLabel("Название:"), BorderLayout.WEST);
        createPanel.add(roomNameInput, BorderLayout.CENTER);
        createPanel.add(createButton, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        refreshButton = new JButton("Обновить");
        joinButton = new JButton("Войти в комнату");
        joinButton.setBackground(new Color(70, 130, 180));
        joinButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> loadRooms());
        joinButton.addActionListener(e -> joinSelectedRoom());
        buttonPanel.add(refreshButton);
        buttonPanel.add(joinButton);

        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(userPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(createPanel);
        southPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        southPanel.add(buttonPanel);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void createRoom(String roomName) {
        refreshButton.setEnabled(false);
        joinButton.setEnabled(false);

        new Thread(() -> {
            try {
                client.send(SimpleProtocol.createCreateRoom(roomName));
                String response = client.receive(5000);

                if (response != null && response.equals("OK")) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Комната создана: " + roomName);
                        loadRooms();
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Ошибка: " + response);
                        refreshButton.setEnabled(true);
                        joinButton.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
                    refreshButton.setEnabled(true);
                    joinButton.setEnabled(true);
                });
            }
        }).start();
    }

    private void loadRooms() {
        refreshButton.setEnabled(false);
        joinButton.setEnabled(false);
        roomList.setEnabled(false);

        new Thread(() -> {
            try {
                client.send(SimpleProtocol.createGetRooms());
                String response = client.receive(5000);

                if (response != null && response.startsWith("ROOMS|")) {
                    String roomsData = response.substring(6);
                    String[] rooms = roomsData.isEmpty() ? new String[0] : roomsData.split(",");
                    SwingUtilities.invokeLater(() -> updateRoomList(rooms));
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Ошибка: " + response);
                        updateRoomList(new String[0]);
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
                    updateRoomList(new String[0]);
                });
            }
        }).start();
    }

    private void updateRoomList(String[] rooms) {
        roomListModel.clear();

        if (rooms.length > 0) {
            for (String roomInfo : rooms) {
                String[] parts = roomInfo.split(":");
                if (parts.length == 2) {
                    try {
                        String roomName = parts[0];
                        int userCount = Integer.parseInt(parts[1]);
                        roomListModel.addElement(roomName + " (" + userCount + " чел.)");
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        refreshButton.setEnabled(true);
        joinButton.setEnabled(true);
        roomList.setEnabled(true);
    }

    private void joinSelectedRoom() {
        String selected = roomList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Выберите комнату из списка!");
            return;
        }

        String roomName = selected.split("\\s+\\(")[0].trim();
        refreshButton.setEnabled(false);
        joinButton.setEnabled(false);

        new Thread(() -> {
            try {
                client.send(SimpleProtocol.createJoinRoom(roomName));
                String response = client.receive(5000);

                if (response != null && response.equals("OK")) {
                    SwingUtilities.invokeLater(() -> {
                        dispose();
                        new ChatWindow(username, userColor, roomName, client).setVisible(true);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Ошибка: " + response);
                        refreshButton.setEnabled(true);
                        joinButton.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
                    refreshButton.setEnabled(true);
                    joinButton.setEnabled(true);
                });
            }
        }).start();
    }
}