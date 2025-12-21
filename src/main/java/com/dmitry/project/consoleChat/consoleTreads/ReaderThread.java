package com.dmitry.project.consoleChat.consoleTreads;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;

public class ReaderThread extends Thread {

    private BufferedReader reader;
    private String clientName;
    private ChatClientHandler clientHandler;
    private static final int MAX_MESSAGE_SIZE = 20;

    public ReaderThread(BufferedReader reader, String clientName, ChatClientHandler handler) {
        this.reader = reader;
        this.clientName = clientName;
        this.clientHandler = handler;
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null && !message.equalsIgnoreCase("stop")) {
                if (message.length() > MAX_MESSAGE_SIZE) {
                    clientHandler.send("ошибка: сообщение превышает " + MAX_MESSAGE_SIZE + " символов");
                    continue;
                }
                String formattedMessage = "[" + clientName + "] " + message;
                System.out.println(LocalDateTime.now() + " " + formattedMessage);
                clientHandler.broadcastMessage(formattedMessage);
            }
        } catch (IOException e) {
            System.err.println("ошибка чтения от " + clientName);
        } finally {
            clientHandler.cleanup();
        }
    }
}