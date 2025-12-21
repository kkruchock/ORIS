package com.dmitry.project.consoleChat.testing;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        try {
            System.out.println("Подключаемся к серверу...");

            try (Socket clientSocket = new Socket(InetAddress.getLocalHost(), 4004);
                 BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                 BufferedReader socketInput = new BufferedReader(
                         new InputStreamReader(clientSocket.getInputStream()));
                 BufferedWriter socketOutput = new BufferedWriter(
                         new OutputStreamWriter(clientSocket.getOutputStream()))) {

                System.out.println("Подключение установлено!");

                while (true) {
                    System.out.print("Введите сообщение: ");

                    String message = consoleReader.readLine();

                    socketOutput.write(message + "\n");
                    socketOutput.flush();
                    System.out.println("Сообщение отправлено серверу");

                    String serverResponse = socketInput.readLine();
                    System.out.println("Ответ сервера: " + serverResponse);
                }
            } catch (IOException e) {
                System.err.println("Ошибка при обмене данными с сервером: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
        }
    }
}