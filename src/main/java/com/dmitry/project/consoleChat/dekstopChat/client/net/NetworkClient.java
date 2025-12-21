package com.dmitry.project.consoleChat.dekstopChat.client.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//сетевой клиент
public class NetworkClient {

    private SocketChannel channel;
    private Thread receiveThread;
    private boolean running;

    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public NetworkClient() {
        this.running = true;
    }

    public boolean connect(String host, int port) {
        try {
            System.out.println("[Client] Подключаемся к " + host + ":" + port);

            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));

            int attempts = 0;
            while (!channel.finishConnect()) {
                Thread.sleep(10);
                if (++attempts > 500) {
                    System.err.println("[Client] Таймаут подключения");
                    return false;
                }
            }

            startReceiver();
            System.out.println("[Client] Подключение установлено");
            return true;

        } catch (Exception e) {
            System.err.println("[Client] Ошибка подключения: " + e.getMessage());
            return false;
        }
    }

    public void send(String command) throws IOException {
        if (!isConnected()) {
            throw new IOException("Нет подключения");
        }

        System.out.println("[Client] Отправляем: " + command);
        ByteBuffer buffer = ByteBuffer.wrap((command + "\n").getBytes(StandardCharsets.UTF_8));

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    public String receive() throws InterruptedException {
        return messageQueue.take();
    }

    public String receive(int timeoutMs) throws IOException, InterruptedException {
        String message = messageQueue.poll(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        if (message == null) {
            throw new IOException("Таймаут ожидания ответа");
        }
        return message;
    }

    private void startReceiver() {
        stopReceiver();

        receiveThread = new Thread(() -> {
            System.out.println("[Client] Запущен прием сообщений");
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            StringBuilder dataBuffer = new StringBuilder();

            while (running && channel != null && channel.isConnected()) {
                try {
                    buffer.clear();
                    int bytesRead = channel.read(buffer);

                    if (bytesRead == -1) {
                        throw new IOException("Сервер закрыл соединение");
                    }

                    if (bytesRead > 0) {
                        String chunk = new String(buffer.array(), 0, bytesRead, StandardCharsets.UTF_8);
                        dataBuffer.append(chunk);

                        String data = dataBuffer.toString();
                        while (data.contains("\n")) {
                            int idx = data.indexOf("\n");
                            String message = data.substring(0, idx).trim();
                            data = data.substring(idx + 1);

                            if (!message.isEmpty()) {
                                System.out.println("[Client] Получено: " + message);
                                messageQueue.put(message);
                            }
                        }
                        dataBuffer = new StringBuilder(data);
                    }

                    Thread.sleep(10);

                } catch (IOException e) {
                    if (running) {
                        System.err.println("[Client] Ошибка приема: " + e.getMessage());
                        disconnect();
                    }
                    break;
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.err.println("[Client] Неожиданная ошибка: " + e.getMessage());
                }
            }
            System.out.println("[Client] Прием сообщений остановлен");
        });

        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    private void stopReceiver() {
        if (receiveThread != null && receiveThread.isAlive()) {
            System.out.println("[Client] Останавливаем прием сообщений");
            receiveThread.interrupt();
            try {
                receiveThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            receiveThread = null;
        }
        messageQueue.clear();
    }

    public void disconnect() {
        running = false;
        stopReceiver();

        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
                System.out.println("[Client] Соединение закрыто");
            }
        } catch (IOException e) {
            System.err.println("[Client] Ошибка при закрытии: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return channel != null && channel.isConnected() && running;
    }
}