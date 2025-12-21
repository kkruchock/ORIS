package com.dmitry.project.consoleChat.consoleTreads;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ClientHm {
    public static void main(String[] args) {
        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in)))
        {
            Socket clientSocket = createSocket(consoleReader);

            try (clientSocket;
                 BufferedReader socketInput = new BufferedReader(
                         new InputStreamReader(clientSocket.getInputStream())
                 );
                 BufferedWriter socketOutput = new BufferedWriter(
                         new OutputStreamWriter(clientSocket.getOutputStream())
                 )) {

                System.out.print("введите ваше имя: ");
                String userName = consoleReader.readLine();
                socketOutput.write(userName + "\n");
                socketOutput.flush();

                System.out.println("подключение установлено. для выхода введите 'stop'");

                Thread messageReceiver = new Thread(() -> {
                    try {
                        String incomingMessage;
                        while ((incomingMessage = socketInput.readLine()) != null) {
                            System.out.println(incomingMessage);
                        }
                    } catch (IOException e) {
                        System.out.println("соединение с сервером разорвано");
                    }
                });
                messageReceiver.start();

                while (true) {

                    String message = consoleReader.readLine();

                    if ("stop".equalsIgnoreCase(message)) {
                        break;
                    }

                    socketOutput.write(message + "\n");
                    socketOutput.flush();
                }

            } catch (IOException e) {
                System.err.println("ошибка при обмене данными с сервером: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("неожиданная ошибка: " + e.getMessage());
        }
    }

    private static Socket createSocket(BufferedReader consoleReader)  throws IOException {

        Socket clientSocket = null;

        while (clientSocket == null) {
            try {
                System.out.print("введите порт сервера: ");
                int port = Integer.parseInt(consoleReader.readLine());

                clientSocket = new Socket(InetAddress.getLocalHost(), port);
                System.out.println("подключение к localhost:" + port + " установлено");
            } catch (IOException e) {
                System.err.println("ошибка подключения: " + e.getMessage());
                System.err.println("пожалуйста, попробуйте другой порт");
            } catch (NumberFormatException e) {
                System.err.println("неверный формат порта. введите число");
            }
        }
        return clientSocket;
    }
}