package com.dmitry.project.consoleChat.dekstopChat.server;

import com.dmitry.project.consoleChat.dekstopChat.server.net.ChatServer;

import java.io.IOException;

public class ServerLauncher {

    public static void main(String[] args) {
        try {
            ChatServer server = new ChatServer(1010);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nзавершение...");
                server.stop();
            }));

            server.start();
        } catch (IOException e) {
            System.err.println("не удалось запустить сервер: " + e.getMessage());
        }
    }
}