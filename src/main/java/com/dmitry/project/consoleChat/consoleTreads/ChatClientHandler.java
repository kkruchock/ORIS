package com.dmitry.project.consoleChat.consoleTreads;

import java.io.*;
import java.net.Socket;

public class ChatClientHandler {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String clientName;
    private ReaderThread readerThread;

    public ChatClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        this.clientName = reader.readLine();
        System.out.println(clientName + " подключился");

        this.readerThread = new ReaderThread(reader, clientName, this);
        readerThread.start();
    }

    public void broadcastMessage(String message) {
        for (ChatClientHandler client : ServerHm.serverList) {
            if (!client.socket.isClosed()) { // (client != this && !client.socket.isClosed())
                client.send(message);
            }
        }
    }

    public void send(String message) {
        try {
            if (writer != null && !socket.isClosed()) {
                writer.write(message + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            cleanup();
        }
    }

    public void cleanup() {
        try {
            ServerHm.serverList.remove(this);
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
            System.out.println(clientName + " отключился");
        } catch (IOException e) {
            System.err.println("ошибка cleanup: " + e.getMessage());
        }
    }
}