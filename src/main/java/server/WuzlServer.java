package server;

import client.ClientSocket;
import misc.WuzlConfig;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : Emilio Zottel (4AHIF)
 * @since : 07.11.2022, Mo.
 **/
public class WuzlServer implements AutoCloseable {
    private final ServerSocket serverSocket;
    private final List<ClientSocket> clientList;
    private final WuzlConfig config;
    private final AtomicBoolean clientConnecting = new AtomicBoolean();

    public WuzlServer(WuzlConfig config) throws IOException {
        this.config = config;
        this.serverSocket = new ServerSocket(config.communicationPort());
        this.clientList = new ArrayList<>();
        System.out.println("Server started: " + serverSocket);
    }

    public WuzlServer() throws IOException {
        this(WuzlConfig.defaultSettings());
    }


    public static void main(String[] args) {
        try (var server = new WuzlServer()) {
            server.start();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendObject(Object o, Socket target) throws IOException {
        new ObjectOutputStream(target.getOutputStream()).writeObject(o);
    }

    public void start() throws InterruptedException {
        Thread clientAcceptor = new Thread(this::acceptClients);
        clientAcceptor.setDaemon(true);
        clientAcceptor.start();

        // maybe start a broadcast loop thread for every single client (in case there are latency issues)
        while (true) {
            waitForNewClientToFinishConnecting();
            broadcastEach();
        }
    }

    /**
     * Could also synchronize on "this", but then we would have to notify every thread waiting on "this",
     * which is bad because then we would also notify the thread that is waiting for a new client to connect.
     * That isn't much of a problem, but then there will be more than 1 "Client connected" message,
     * which is not cool, so we just use a separate lock object.
     */
    private void waitForNewClientToFinishConnecting() throws InterruptedException {
        while (clientConnecting.get()) {
            synchronized (clientConnecting) {
                clientConnecting.wait();
            }
        }
    }

    public void acceptClients() {
        while (true) {
            acceptClient();
        }
    }

    public void acceptClient() {
        try {
            Socket client = serverSocket.accept();  // Waiting for a client
            clientConnecting.set(true);
            sendObject(config, client);
            System.out.println("+Client connected: " + client);

            // Synchronize AFTER waiting for the client,
            // so the lock on "this" is not blocked while waiting for a client to connect
            synchronized (this) {
                clientList.add(new ClientSocket(client, config.bufferSize()));
                notifyAll();
            }

            synchronized (clientConnecting) {
                clientConnecting.set(false);
                clientConnecting.notifyAll();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void waitUntilClientCountExceedsN(int n) {
        try {
            if (clientList.size() <= n) {
                while (clientList.size() <= n) {
                    System.out.printf("Waiting for more clients... (%d/%d)%n", clientList.size(), n + 1);
                    wait();
                }

                System.out.printf("Just got notified that we have enough clients! (%d/%d)%n", clientList.size(), n + 1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // TODO works fine for 2 clients, but noisy for 3+ clients
    private synchronized void broadcastEach() {
        // Probably useless
        //Arrays.fill(buffer, (byte) 0);

        waitUntilClientCountExceedsN(1);
        clientList.removeIf(client -> client.receiveBuffer(true) == Integer.MIN_VALUE);
        Iterator<ClientSocket> iterator = clientList.iterator();

        while (iterator.hasNext()) {
            ClientSocket source = iterator.next();

            if (source.isActive()) {
                broadcastToEveryoneElseThan(source, new byte[]{1});  // 1 = Do not separate
                broadcastToEveryoneElseThan(source, source.getBuffer());
            } else {
                iterator.remove();
                source.printDisconnectMessage();  // Logging the disconnect message for "broadcastToEveryoneElseThan"
            }
        }

        broadcast(new byte[]{0});  // 0 = (null)-Separate
    }

    /**
     * @param buffer the buffer to broadcast
     */
    private void broadcast(byte[] buffer) {
        clientList.removeIf(client -> !client.isActive() || !client.sendBuffer(buffer, true));
    }

    /**
     * @param source The client that should not receive the message (the client that sent the message)
     * @param buffer the buffer to broadcast
     */
    private void broadcastToEveryoneElseThan(ClientSocket source, byte[] buffer) {
        clientList.stream()
                .filter(client -> client != source && !client.sendBuffer(buffer, false))
                .forEach(client -> client.setActive(false));
    }


    @Override
    public void close() throws Exception {
        serverSocket.close();
    }

}
