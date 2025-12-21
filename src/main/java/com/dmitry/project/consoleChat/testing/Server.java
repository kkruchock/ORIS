package com.dmitry.project.consoleChat.testing;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        // Используем try-with-resources для ServerSocket
        try (ServerSocket server = new ServerSocket(4004)) {
            System.out.println("Сервер запущен!");

            while (true) {
                // Ожидаем подключение клиента
                try (Socket clientSocket = server.accept();
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(clientSocket.getInputStream()));
                     BufferedWriter out = new BufferedWriter(
                             new OutputStreamWriter(clientSocket.getOutputStream()))) {

                    System.out.println("Клиент подключен!");

                    while (true) {
                        String word = in.readLine();

                        if (word == null || word.equals("stop")) {
                            break;
                        }
                        System.out.println("Получено от клиента: " + word);

                        out.write("Привет, это Сервер! Подтверждаю, вы написали: " + word + "\n");
                        out.flush();
                    }

                } catch (IOException e) {
                    System.err.println("Ошибка при работе с клиентом: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
        } finally {
            System.out.println("Сервер закрыт!");
        }
    }
}