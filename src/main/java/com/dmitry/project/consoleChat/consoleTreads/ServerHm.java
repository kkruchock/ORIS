package com.dmitry.project.consoleChat.consoleTreads;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerHm {

    //todo сделать обертку
    public static int port;
    public static final ConcurrentLinkedQueue<ChatClientHandler> serverList = new ConcurrentLinkedQueue<>();


    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        try (ServerSocket server = createServerSocket(scanner)) {
            ConnectionListener listener = new ConnectionListener(server);
            listener.start();
        } catch (IOException e) {
            System.err.println("ошибка запуска сервера: " + e.getMessage());
        } finally {
            System.out.println("---сервер на порту " + port + " остановлен---");
            for (ChatClientHandler client : serverList) {
                try {
                    client.cleanup();
                } catch (Exception e) {
                    System.err.println("ошибка при очистке клиента: " + e.getMessage());
                }
            }
            serverList.clear();
        }
    }
    private static ServerSocket createServerSocket(Scanner scanner) {

        ServerSocket server = null;
        while (server == null) {
            try {
                System.out.print("введите номер порта для запуска сервера: ");
                port = scanner.nextInt();
                scanner.nextLine();
                server = new ServerSocket(port);
                System.out.println("--- сервер запущен на порту " + port + " ---");
            } catch (IOException e) {
                System.err.println("ошибка запуска сервера: " + e.getMessage());
                System.err.println("пожалуйста, попробуйте выбрать другой порт.");
            }
        }
        return server;
    }
}