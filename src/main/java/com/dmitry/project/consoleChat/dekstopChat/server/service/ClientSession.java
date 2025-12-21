package com.dmitry.project.consoleChat.dekstopChat.server.service;

import java.nio.channels.SocketChannel;

//храним состояние клиента
public class ClientSession {

    private final String id;
    private String username;
    private String color;
    private String currentRoom;
    private final SocketChannel channel;

    public ClientSession(String id, SocketChannel channel) {
        this.id = id;
        this.channel = channel;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(String currentRoom) {
        this.currentRoom = currentRoom;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    //для отладки
    @Override
    public String toString() {
        return username != null ? username : id;
    }
}