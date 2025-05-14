import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class NetworkManager {
    private static final int PORT = 5555;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private List<String> chatMessages = new ArrayList<>();
    private List<PlayerData> otherPlayers = new ArrayList<>();
    private GamePanel gamePanel;
    private boolean isCompetitive;
    private String currentMatchId;

    public NetworkManager(String playerName, GamePanel gamePanel, boolean isCompetitive) {
        this.playerName = playerName;
        this.gamePanel = gamePanel;
        this.isCompetitive = isCompetitive;
    }

    public boolean connect(String serverIP) {
        try {
            socket = new Socket(serverIP, PORT);
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
                        .ifPresent(p -> p.update(p.getX(), p.getY(), p.getScore() + points,p.getHealth()));
            }
        }
        else if (message.startsWith("MATCH_START:")) {
            String[] parts = message.substring("MATCH_START:".length()).split(":");
            currentMatchId = parts[0];
            String opponent = parts[1];
            gamePanel.setOpponentName(opponent);
        }
        else if (message.startsWith("MATCH_INVITE:")) {
            String opponent = message.substring("MATCH_INVITE:".length());
            int response = JOptionPane.showConfirmDialog(gamePanel,
                    opponent + " wants to play 1vs1. Accept?", "Match Invitation",
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                out.println("MATCH_ACCEPT:" + opponent);
            } else {
                out.println("MATCH_DECLINE:" + opponent);
            }
        }
        else if (message.startsWith("MATCH_DECLINED:")) {
            String opponent = message.substring("MATCH_DECLINED:".length());
            JOptionPane.showMessageDialog(gamePanel, opponent + " declined your invitation");
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
        String[] parts = updateData.split(":");
        String name = parts[0];
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int score = Integer.parseInt(parts[3]);
        int health = parts.length > 4 ? Integer.parseInt(parts[4]) : 3;

        Optional<PlayerData> player = otherPlayers.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst();

        if (player.isPresent()) {
            player.get().update(x, y, score, health); // Ajoutez health à la mise à jour
        } else {
            otherPlayers.add(new PlayerData(name, x, y, score, health));
        }

        // Toujours mettre à jour l'adversaire en mode compétitif
        if (isCompetitive && name.equals(gamePanel.getOpponentName())) {
            gamePanel.updateOpponent(x, y, score, health);
        }
    }

    public void sendPlayerUpdate(int x, int y, int score, int health) {
        if (out != null) {
            String message = "UPDATE:" + x + ":" + y + ":" + score + ":" + health;
            out.println(message);
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

    public void sendMatchRequest(String opponent) {
        if (out != null) {
            out.println("MATCH_REQUEST:" + opponent);
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
        private int health;

        public PlayerData(String name) {
            this(name, 0, 0, 0, 3);
        }

        public PlayerData(String name, int x, int y, int score, int health) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.score = score;
            this.health = health;
        }

        public void update(int x, int y, int score, int health) {
            this.x = x;
            this.y = y;
            this.score = score;
            this.health = health;
        }

        // Ajoutez getter pour health
        public int getHealth() { return health; }


        public int getScore() {
            return score ;
        }

        public String getName() {
            return name;
        }

        public int getX() {
            return x;
        }
        public int getY() {
            return y;
        }
    }
}