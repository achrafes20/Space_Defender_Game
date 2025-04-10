import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 5555;
    private static final int MAX_PLAYERS = 4;
    private ServerSocket serverSocket;
    private ExecutorService pool = Executors.newFixedThreadPool(MAX_PLAYERS);
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private List<String> chatMessages = new ArrayList<>();

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientThread = new ClientHandler(clientSocket, this);
                clients.add(clientThread);
                pool.execute(clientThread);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void broadcast(String message, ClientHandler exclude) {
        for (ClientHandler client : clients) {
            if (client != exclude) {
                client.sendMessage(message);
            }
        }
    }

    public void broadcastPlayerList() {
        StringBuilder playerList = new StringBuilder("PLAYER_LIST:");
        for (ClientHandler client : clients) {
            playerList.append(client.getPlayerName()).append(",");
        }
        broadcast(playerList.toString(), null);
    }

    public void addChatMessage(String message) {
        chatMessages.add(message);
        if (chatMessages.size() > 100) chatMessages.remove(0);
        broadcast("CHAT:" + message, null);
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        broadcastPlayerList();
    }

    public void stop() {
        try {
            if (serverSocket != null) serverSocket.close();
            pool.shutdown();
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private GameServer server;
        private PrintWriter out;
        private BufferedReader in;
        private String playerName;

        public ClientHandler(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
        }
        
        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("JOIN:")) {
                        playerName = message.substring("JOIN:".length());
                        server.broadcastPlayerList();
                    }
                    else if (message.startsWith("CHAT:")) {
                        server.addChatMessage(message.substring("CHAT:".length()));
                    }
                    else if (message.startsWith("UPDATE:")) {
                        String updateData = message.substring("UPDATE:".length());
                        server.broadcast("PLAYER_UPDATE:" + playerName + ":" + updateData, this);
                    }
                    else if (message.startsWith("ENEMY_SPAWN:")) {
                        server.broadcast(message, this);
                    }
                    else if (message.startsWith("PROJECTILE:")) {
                        server.broadcast(message, this);
                    }
                    else if (message.startsWith("SCORE_UPDATE:")) {
                        server.broadcast(message, this);
                    }
                }
            } catch (IOException e) {
                System.err.println("Client error: " + e.getMessage());
            } finally {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing streams: " + e.getMessage());
                }
                server.removeClient(this);
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public String getPlayerName() {
            return playerName;
        }
    }
}