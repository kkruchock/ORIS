package com.dmitry.project.consoleChat.consoleTreads;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static com.dmitry.project.consoleChat.consoleTreads.ServerHm.serverList;

public class ConnectionListener {

    private final ServerSocket serverSocket;

    public ConnectionListener(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void start() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();

                if (serverList.size() >= 10) {
                    System.out.println("достигнут лимит в 10 участников." +
                            " подключение" + socket.getInetAddress() + "отклонено");

//                    try {
//                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//                        writer.write("чат полон, попробуйте подключиться позже \n");
//                        writer.flush();
                    //тут короче надо задержку усыпить поток
//
//                    } catch (IOException e) {
//                        System.err.println("не удалось отправить сообщение об отказе: " + e.getMessage());
//                    }

                    socket.close();
                    continue;
                }
                System.out.println("новое подключение: " + socket.getInetAddress());

                ChatClientHandler clientHandler = new ChatClientHandler(socket);
                serverList.add(clientHandler);
            } catch (IOException e) {
                System.err.println("ошибка при принятии подключения: " + e.getMessage());
            }
        }
    }
}