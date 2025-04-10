import java.io.*;
import java.net.*;
import java.util.*;

public class NetworkManager {
    private static final int PORT = 5555;
    private static final String SERVER_IP = "localhost";

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private List<String> chatMessages = new ArrayList<>();
    private List<PlayerData> otherPlayers = new ArrayList<>();
    private GamePanel gamePanel;

    public NetworkManager(String playerName, GamePanel gamePanel) {
        this.playerName = playerName;
        this.gamePanel = gamePanel;
    }

    public boolean connect() {
        try {
            socket = new Socket(SERVER_IP, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("JOIN:" + playerName);

            new Thread(this::listenForMessages).start();
            return true;
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            return false;
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                processServerMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Server read error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void processServerMessage(String message) {
        if (message.startsWith("PLAYER_LIST:")) {
            updatePlayerList(message.substring("PLAYER_LIST:".length()));
        }
        else if (message.startsWith("CHAT:")) {
            chatMessages.add(message.substring("CHAT:".length()));
            if (chatMessages.size() > 50) chatMessages.remove(0);
        }
        else if (message.startsWith("PLAYER_UPDATE:")) {
            updateOtherPlayers(message.substring("PLAYER_UPDATE:".length()));
        }
        else if (message.startsWith("ENEMY_SPAWN:")) {
            String[] parts = message.substring("ENEMY_SPAWN:".length()).split(":");
            int x = Integer.parseInt(parts[0]);
            int type = Integer.parseInt(parts[1]);
            gamePanel.spawnNetworkEnemy(x, type);
        }
        else if (message.startsWith("PROJECTILE:")) {
            String[] parts = message.substring("PROJECTILE:".length()).split(":");
            String owner = parts[0];
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            gamePanel.addNetworkProjectile(owner, x, y);
        }
        else if (message.startsWith("SCORE_UPDATE:")) {
            String[] parts = message.substring("SCORE_UPDATE:".length()).split(":");
            String player = parts[0];
            int points = Integer.parseInt(parts[1]);

            if (player.equals(this.playerName)) {
                gamePanel.addScore(points);
            } else {
                otherPlayers.stream()
                        .filter(p -> p.getName().equals(player))
                        .findFirst()
                        .ifPresent(p -> p.update(p.getX(), p.getY(), p.getScore() + points));
            }
        }
    }

    private void updatePlayerList(String playerList) {
        otherPlayers.clear();
        String[] players = playerList.split(",");
        for (String player : players) {
            if (!player.isEmpty() && !player.equals(playerName)) {
                otherPlayers.add(new PlayerData(player));
            }
        }
    }

    private void updateOtherPlayers(String updateData) {
        String[] updates = updateData.split(";");
        for (String update : updates) {
            String[] parts = update.split(":");
            String name = parts[0];
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int score = Integer.parseInt(parts[3]);

            Optional<PlayerData> player = otherPlayers.stream()
                    .filter(p -> p.getName().equals(name))
                    .findFirst();

            if (player.isPresent()) {
                player.get().update(x, y, score);
            } else {
                otherPlayers.add(new PlayerData(name, x, y, score));
            }
        }
    }

    public void sendPlayerUpdate(int x, int y, int score) {
        if (out != null) {
            out.println("UPDATE:" + x + ":" + y + ":" + score);
        }
    }

    public void sendChatMessage(String message) {
        if (out != null) {
            out.println("CHAT:" + playerName + ": " + message);
        }
    }

    public void sendEnemySpawn(int x, int type) {
        if (out != null) {
            out.println("ENEMY_SPAWN:" + x + ":" + type);
        }
    }

    public void sendProjectileFired(int x, int y) {
        if (out != null) {
            out.println("PROJECTILE:" + playerName + ":" + x + ":" + y);
        }
    }

    public void sendScoreUpdate(String playerName, int points) {
        if (out != null) {
            out.println("SCORE_UPDATE:" + playerName + ":" + points);
        }
    }

    public void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Disconnection error: " + e.getMessage());
        }
    }

    public List<String> getChatMessages() {
        return new ArrayList<>(chatMessages);
    }

    public List<PlayerData> getOtherPlayers() {
        return new ArrayList<>(otherPlayers);
    }

    public String getPlayerName() {
        return playerName;
    }

    public static class PlayerData {
        private String name;
        private int x, y;
        private int score;

        public PlayerData(String name) {
            this(name, 0, 0, 0);
        }

        public PlayerData(String name, int x, int y, int score) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.score = score;
        }

        public void update(int x, int y, int score) {
            this.x = x;
            this.y = y;
            this.score = score;
        }

        public String getName() { return name; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getScore() { return score; }
    }
}