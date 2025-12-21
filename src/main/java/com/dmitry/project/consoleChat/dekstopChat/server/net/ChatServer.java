package com.dmitry.project.consoleChat.dekstopChat.server.net;

import com.dmitry.project.consoleChat.dekstopChat.common.SimpleProtocol;
import com.dmitry.project.consoleChat.dekstopChat.server.service.ClientSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//сам сервер
public class ChatServer {

    private Selector selector;
    private ServerSocketChannel serverChannel;
    private boolean running;

    private final Map<String, ClientSession> clients = new ConcurrentHashMap<>();
    private final Map<String, List<ClientSession>> rooms = new ConcurrentHashMap<>();
    private int clientIdCounter = 1;

    public ChatServer(int port) throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        running = true;
        System.out.println("Сервер запущен на порту " + port);
    }

    //основной цикл
    public void start() {
        System.out.println("Сервер начал работу");

        while (running) {
            try {
                selector.select();

                for (SelectionKey key : selector.selectedKeys()) {
                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) handleAccept(key);
                    if (key.isReadable()) handleRead(key);
                }
                selector.selectedKeys().clear();

            } catch (IOException e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
    }

    //принимаем подключения
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = server.accept();
        clientChannel.configureBlocking(false);

        String clientId = "client-" + clientIdCounter++;
        ClientSession session = new ClientSession(clientId, clientChannel);
        clients.put(clientId, session);

        clientChannel.register(selector, SelectionKey.OP_READ).attach(session);
        System.out.println("Подключен: " + clientId);
    }

    //читаем данные
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientSession session = (ClientSession) key.attachment();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            disconnectClient(session);
            channel.close();
            key.cancel();
            return;
        }

        if (bytesRead > 0) {
            String data = new String(buffer.array(), 0, bytesRead).trim();
            for (String line : data.split("\n")) {
                line = line.trim();
                if (!line.isEmpty()) {
                    processCommand(line, session);
                }
            }
        }
    }

    //обработчик команд
    private void processCommand(String command, ClientSession session) throws IOException {
        String[] parts = SimpleProtocol.parseCommand(command);
        if (parts.length == 0) return;

        switch (parts[0]) {
            case SimpleProtocol.AUTH:
                if (parts.length >= 3) {
                    session.setUsername(parts[1]);
                    session.setColor(parts[2]);
                    send(session, SimpleProtocol.OK);
                }
                break;

            case SimpleProtocol.CREATE_ROOM:
                if (parts.length >= 2) {
                    String roomName = parts[1];
                    if (!rooms.containsKey(roomName)) {
                        rooms.put(roomName, new ArrayList<>());
                        send(session, SimpleProtocol.OK);
                    } else {
                        send(session, SimpleProtocol.createError("Комната существует"));
                    }
                }
                break;

            case SimpleProtocol.JOIN_ROOM:
                if (parts.length >= 2) {
                    String roomName = parts[1];

                    if (!rooms.containsKey(roomName)) {
                        rooms.put(roomName, new ArrayList<>());
                    }

                    String oldRoom = session.getCurrentRoom();
                    if (oldRoom != null) {
                        rooms.get(oldRoom).remove(session);
                    }

                    rooms.get(roomName).add(session);
                    session.setCurrentRoom(roomName);

                    send(session, SimpleProtocol.OK);

                    broadcast(roomName, SimpleProtocol.createSystemMessage(session.getUsername() + " зашел"), session);
                }
                break;

            case SimpleProtocol.LEAVE_ROOM:
                String roomName = session.getCurrentRoom();
                if (roomName != null) {
                    rooms.get(roomName).remove(session);
                    // Сначала отправляем OK клиенту
                    send(session, SimpleProtocol.OK);
                    // Потом рассылаем системное сообщение другим
                    broadcast(roomName, SimpleProtocol.createSystemMessage(session.getUsername() + " вышел"), session);
                    session.setCurrentRoom(null);
                } else {
                    send(session, SimpleProtocol.OK);
                }
                break;

            case SimpleProtocol.SEND_MSG:
                if (parts.length >= 2 && session.getCurrentRoom() != null) {
                    String text = SimpleProtocol.unescape(parts[1]);
                    String msg = SimpleProtocol.createChatMessage(
                            session.getUsername(),
                            session.getColor(),
                            text
                    );
                    broadcast(session.getCurrentRoom(), msg, session);
                }
                break;

            case SimpleProtocol.GET_ROOMS:
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, List<ClientSession>> entry : rooms.entrySet()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(entry.getKey()).append(":").append(entry.getValue().size());
                }
                send(session, SimpleProtocol.createRoomList(sb.toString()));
                break;
        }
    }

    //рассылка
    private void broadcast(String roomName, String message, ClientSession exclude) throws IOException {
        List<ClientSession> roomClients = rooms.get(roomName);
        if (roomClients == null) return;

        for (ClientSession client : roomClients) {
            if (client != exclude) {  // Не отправляем сообщение самому себе
                send(client, message);
            }
        }
    }

    //отправка сообщения
    private void send(ClientSession session, String message) throws IOException {
        if (session.getChannel().isOpen()) {
            ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes());
            session.getChannel().write(buffer);
        }
    }

    //отключение
    private void disconnectClient(ClientSession session) {
        String roomName = session.getCurrentRoom();
        if (roomName != null) {
            rooms.get(roomName).remove(session);
            try {
                broadcast(roomName, SimpleProtocol.createSystemMessage(session.getUsername() + " отключился"), null);
            } catch (IOException e) {}
        }
        clients.remove(session.getId());
        System.out.println("Отключен: " + session.getUsername());
    }


    //остановка сервера
    public void stop() {
        running = false;
        if (selector != null) {
            selector.wakeup();
        }
        System.out.println("Сервер остановлен");
    }
}