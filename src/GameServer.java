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
    private Map<String, Match> matches = new ConcurrentHashMap<>();

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

    public void broadcastToMatch(String matchId, String message, ClientHandler exclude) {
        Match match = matches.get(matchId);
        if (match != null) {
            match.getPlayers().forEach(player -> {
                if (!player.equals(exclude.getPlayerName())) {
                    findClientByName(player).ifPresent(c -> c.sendMessage(message));
                }
            });
        }
    }

    private Optional<ClientHandler> findClientByName(String playerName) {
        return clients.stream()
                .filter(c -> c.getPlayerName().equals(playerName))
                .findFirst();
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
        matches.values().removeIf(match -> match.removePlayer(client.getPlayerName()));
        broadcastPlayerList();
    }

    public void createMatch(String matchId, String player1, String player2) {
        matches.put(matchId, new Match(matchId, player1, player2));
        findClientByName(player1).ifPresent(c -> c.sendMessage("MATCH_START:" + matchId + ":" + player2));
        findClientByName(player2).ifPresent(c -> c.sendMessage("MATCH_START:" + matchId + ":" + player1));
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

    private static class Match {
        private String matchId;
        private Set<String> players = new HashSet<>();

        public Match(String matchId, String player1, String player2) {
            this.matchId = matchId;
            this.players.add(player1);
            this.players.add(player2);
        }

        public boolean removePlayer(String playerName) {
            players.remove(playerName);
            return players.isEmpty();
        }

        public Set<String> getPlayers() {
            return players;
        }

        public String getMatchId() {
            return matchId;
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private GameServer server;
        private PrintWriter out;
        private BufferedReader in;
        private String playerName;
        private String currentMatchId;

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
                        if (currentMatchId != null) {
                            server.broadcastToMatch(currentMatchId, "PLAYER_UPDATE:" + playerName + ":" + updateData, this);
                        } else {
                            server.broadcast("PLAYER_UPDATE:" + playerName + ":" + updateData, this);
                        }
                    }
                    else if (message.startsWith("ENEMY_SPAWN:")) {
                        if (currentMatchId != null) {
                            server.broadcastToMatch(currentMatchId, message, this);
                        } else {
                            server.broadcast(message, this);
                        }
                    }
                    else if (message.startsWith("PROJECTILE:")) {
                        if (currentMatchId != null) {
                            server.broadcastToMatch(currentMatchId, message, this);
                        } else {
                            server.broadcast(message, this);
                        }
                    }
                    else if (message.startsWith("SCORE_UPDATE:")) {
                        if (currentMatchId != null) {
                            server.broadcastToMatch(currentMatchId, message, this);
                        } else {
                            server.broadcast(message, this);
                        }
                    }
                    else if (message.startsWith("MATCH_REQUEST:")) {
                        String opponent = message.substring("MATCH_REQUEST:".length());
                        server.findClientByName(opponent).ifPresent(c -> {
                            c.sendMessage("MATCH_INVITE:" + playerName);
                        });
                    }
                    else if (message.startsWith("MATCH_ACCEPT:")) {
                        String opponent = message.substring("MATCH_ACCEPT:".length());
                        String matchId = playerName + "_" + opponent + "_" + System.currentTimeMillis();
                        currentMatchId = matchId;
                        server.createMatch(matchId, playerName, opponent);
                    }
                    else if (message.startsWith("MATCH_DECLINE:")) {
                        String opponent = message.substring("MATCH_DECLINE:".length());
                        server.findClientByName(opponent).ifPresent(c -> {
                            c.sendMessage("MATCH_DECLINED:" + playerName);
                        });
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