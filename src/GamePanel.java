import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GamePanel extends JPanel {
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private int score = 0;
    private final Random random = new Random();
    private int spawnTimer = 0;
    private int backgroundY = 0;
    private int scrollSpeed = 2;
    private final LevelManager levelManager;
    private boolean isLevelTransition = false;
    private long transitionStartTime;
    private boolean gameOver = false;
    private final String playerName;
    private final int initialDifficulty;
    private final GameWindow parent;
    private javax.swing.Timer gameTimer;
    private final List<Explosion> explosions = new ArrayList<>();
    public static NetworkManager networkManager;
    private boolean isMultiplayer = false;
    private boolean isCompetitive = false;
    private JTextArea chatArea;
    private JTextField chatInput;
    private JPanel chatPanel;
    private List<NetworkProjectile> networkProjectiles = new ArrayList<>();
    private Player player;
    private Image background = ResourceManager.getImage("/background.png");
    private Image playerLifeIcon;
    private boolean chatActive = false;
    private String opponentName = "";
    private int opponentScore = 0;
    private int opponentHealth = 3;
    private int opponentX = -100;
    private int opponentY = -100;
    private Image opponentShipImage;

    public GamePanel(GameWindow parent, String playerName, int difficulty, int shipType, boolean isMultiplayer, boolean isCompetitive, String serverIP) {
        this.parent = parent;
        this.playerName = playerName;
        this.initialDifficulty = difficulty;
        this.levelManager = new LevelManager(difficulty);
        this.isMultiplayer = isMultiplayer;
        this.isCompetitive = isCompetitive;
        this.player = new Player(380, 450, shipType);
        this.playerLifeIcon = ResourceManager.getImage("/ship_" + shipType + ".png")
                .getScaledInstance(30, 36, Image.SCALE_SMOOTH);
        this.opponentShipImage = ResourceManager.getImage("/ship_0.png"); // Image par défaut pour l'adversaire

        if (isMultiplayer) {
            this.networkManager = new NetworkManager(playerName, this, isCompetitive);
            if (!networkManager.connect(serverIP)) {
                JOptionPane.showMessageDialog(this, "Connection error", "Error", JOptionPane.ERROR_MESSAGE);
                parent.showMenu();
                return;
            }
            setupChatUI();
        }

        setFocusable(true);
        requestFocusInWindow();
        setupKeyListeners();
        startGameLoop();
    }

    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameOver && e.getKeyCode() == KeyEvent.VK_R) {
                    resetGame();
                    return;
                }

                if (e.getKeyCode() == KeyEvent.VK_C && isMultiplayer) {
                    toggleChatVisibility();
                    e.consume();
                    return;
                }

                if (!chatActive) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE && player.canShoot()) {
                        projectiles.add(new Projectile(player.getCenterX(), player.getY()));
                        player.shoot();
                        SoundManager.playSound("/shoot.wav");
                        if (isMultiplayer) {
                            networkManager.sendProjectileFired(player.getCenterX(), player.getY());
                        }
                    } else if (isMovementKey(e.getKeyCode())) {
                        player.handleKeyPress(e.getKeyCode());
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        parent.showMenu();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!chatActive && isMovementKey(e.getKeyCode())) {
                    player.handleKeyRelease(e.getKeyCode());
                }
            }
        });
    }

    private void setupChatUI() {
        chatArea = new JTextArea(3, 15);
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(0, 0, 0, 150));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 10));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        chatInput = new JTextField();
        chatInput.setBackground(new Color(30, 30, 50));
        chatInput.setForeground(Color.WHITE);
        chatInput.setFont(new Font("Arial", Font.PLAIN, 10));
        chatInput.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        chatInput.addActionListener(e -> {
            String message = chatInput.getText();
            if (!message.isEmpty()) {
                networkManager.sendChatMessage(message);
                chatInput.setText("");
            }
            requestFocusInWindow();
            chatActive = false;
        });

        chatInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                chatActive = true;
            }
        });

        chatPanel = new JPanel(new BorderLayout());
        chatPanel.setOpaque(false);
        chatPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(chatInput, BorderLayout.SOUTH);
        chatPanel.setBounds(10, getHeight() - 180, 180, 100);
        chatPanel.setVisible(false);
        add(chatPanel);
    }

    private void toggleChatVisibility() {
        boolean newVisibility = !chatPanel.isVisible();
        chatPanel.setVisible(newVisibility);
        if (newVisibility) {
            chatInput.requestFocusInWindow();
        } else {
            requestFocusInWindow();
        }
        repaint();
    }

    private boolean isMovementKey(int keyCode) {
        return keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT ||
                keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN;
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (chatPanel != null) {
            chatPanel.setLocation(10, getHeight() - 180);
        }
    }

    private void startGameLoop() {
        gameTimer = new Timer(16, e -> {
            if (!gameOver) {
                updateGame();
            }
            repaint();
        });
        gameTimer.start();
        SoundManager.playSound("/game_start.wav");
    }

    private void updateGame() {
        if (isMultiplayer) {
            List<String> messages = networkManager.getChatMessages();
            if (!messages.isEmpty()) {
                chatArea.setText("");
                messages.forEach(msg -> chatArea.append(msg + "\n"));
            }
        }

        explosions.forEach(Explosion::update);
        explosions.removeIf(e -> !e.isActive());

        if (isLevelTransition) {
            if (System.currentTimeMillis() - transitionStartTime > 2000) {
                isLevelTransition = false;
                levelManager.levelUp();
                scrollSpeed = 2 + levelManager.getCurrentLevel() / 3;
            }
            return;
        }

        player.update();
        updateBackground();

        if (isMultiplayer) {
            networkManager.sendPlayerUpdate(player.getX(), player.getY(), score, player.getHealth());
        }

        if (!isCompetitive && ++spawnTimer >= levelManager.getAdjustedSpawnInterval()) {
            int x = random.nextInt(getWidth() - 50);
            int type = random.nextInt(3);
            Enemy newEnemy = new Enemy(x, -50, levelManager.getEnemySpeed(), type);
            enemies.add(newEnemy);
            if (isMultiplayer) {
                networkManager.sendEnemySpawn(x, type);
            }
            spawnTimer = 0;
        }

        enemies.forEach(e -> e.update(scrollSpeed));
        projectiles.forEach(Projectile::update);
        networkProjectiles.forEach(NetworkProjectile::update);

        handleCollisions();
        handleNetworkCollisions();
        handleVersusCollisions();

        enemies.removeIf(e -> !e.isAlive() || e.isOutOfScreen(getHeight()));
        projectiles.removeIf(p -> !p.isActive());
        networkProjectiles.removeIf(p -> !p.isActive() || p.getY() < 0);

        if (!isCompetitive && levelManager.isLevelCompleted()) {
            isLevelTransition = true;
            transitionStartTime = System.currentTimeMillis();
            SoundManager.playSound("level_up.wav");
        }

        if (isCompetitive && (player.getHealth() <= 0 || opponentHealth <= 0)) {
            gameOver = true;
            SoundManager.playSound("/game_over.wav");
            DatabaseManager.saveGameResult(
                    playerName,
                    score,
                    levelManager.getCurrentLevel(),
                    getDifficultyString(initialDifficulty) + (isMultiplayer ? " (Multiplayer)" : "")
            );
        }
    }

    private void updateBackground() {
        backgroundY += scrollSpeed;
        if (backgroundY >= getHeight()) {
            backgroundY = 0;
        }
    }

    private void handleCollisions() {
        new ArrayList<>(enemies).forEach(enemy -> {
            new ArrayList<>(projectiles).forEach(projectile -> {
                if (projectile.isActive() && enemy.isAlive() &&
                        projectile.getHitbox().intersects(enemy.getHitbox())) {
                    enemy.takeDamage(1);
                    projectile.setActive(false);
                    if (!enemy.isAlive()) {
                        explosions.add(new Explosion(enemy.getCenterX(), enemy.getCenterY()));
                        addScore((enemy.getType() == 0) ? 10 : (enemy.getType() == 1) ? 15 : 30);
                        if (!isCompetitive) {
                            levelManager.enemyDefeated();
                        }
                        SoundManager.playSound("/explosion.wav");
                    } else {
                        SoundManager.playSound("/hit.wav");
                    }
                }
            });
        });

        new ArrayList<>(enemies).forEach(enemy -> {
            if (enemy.isAlive() && enemy.getHitbox().intersects(player.getHitbox())) {
                enemy.takeDamage(enemy.getMaxHealth());
                explosions.add(new Explosion(enemy.getCenterX(), enemy.getCenterY()));
                player.takeDamage();
                SoundManager.playSound("/player_hit.wav");
                if (player.getHealth() <= 0 && !isCompetitive) {
                    gameOver = true;
                    SoundManager.playSound("/game_over.wav");
                    DatabaseManager.saveGameResult(
                            playerName,
                            score,
                            levelManager.getCurrentLevel(),
                            getDifficultyString(initialDifficulty) + (isMultiplayer ? " (Multiplayer)" : "")
                    );
                }
            }
        });
    }

    private void handleNetworkCollisions() {
        new ArrayList<>(enemies).forEach(enemy -> {
            new ArrayList<>(networkProjectiles).forEach(projectile -> {
                if (projectile.isActive() && enemy.isAlive() &&
                        projectile.getHitbox().intersects(enemy.getHitbox())) {
                    enemy.takeDamage(1);
                    projectile.setActive(false);
                    if (!enemy.isAlive()) {
                        explosions.add(new Explosion(enemy.getCenterX(), enemy.getCenterY()));
                        if (!isCompetitive) {
                            levelManager.enemyDefeated();
                        }
                        SoundManager.playSound("/explosion.wav");
                        if (isMultiplayer) {
                            networkManager.sendScoreUpdate(projectile.getOwner(),
                                    (enemy.getType() == 0) ? 10 : (enemy.getType() == 1) ? 15 : 30);
                        }
                    }
                }
            });
        });
    }

    private void handleVersusCollisions() {
        if (!isCompetitive) return;

        // Check player projectiles hitting opponent
        // Check player projectiles hitting opponent
        new ArrayList<>(projectiles).forEach(projectile -> {
            Rectangle opponentHitbox = new Rectangle(opponentX, opponentY, 50, 60);
            if (projectile.isActive() && projectile.getHitbox().intersects(opponentHitbox)) {
                projectile.setActive(false);
                opponentHealth--;
                explosions.add(new Explosion(opponentX + 25, opponentY + 30));
                SoundManager.playSound("/player_hit.wav");

                // Envoyer la mise à jour
                if (isMultiplayer) {
                    networkManager.sendPlayerUpdate(player.getX(), player.getY(), score, player.getHealth());
                }

                if (opponentHealth <= 0) {
                    gameOver = true;
                    SoundManager.playSound("/game_over.wav");
                }
            }
        });

        // Check opponent projectiles hitting player
        new ArrayList<>(networkProjectiles).forEach(projectile -> {
            if (projectile.isActive() && projectile.getHitbox().intersects(player.getHitbox())) {
                projectile.setActive(false);
                player.takeDamage();
                explosions.add(new Explosion(player.getCenterX(), player.getY() + 30));
                SoundManager.playSound("/player_hit.wav");
                if (player.getHealth() <= 0) {
                    gameOver = true;
                    SoundManager.playSound("/game_over.wav");
                }
            }
        });
    }

    public void addScore(int points) {
        score += points;
    }

    public void spawnNetworkEnemy(int x, int type) {
        enemies.add(new Enemy(x, -50, levelManager.getEnemySpeed(), type));
    }

    public void addNetworkProjectile(String owner, int x, int y) {
        networkProjectiles.add(new NetworkProjectile(owner, x, y));
        SoundManager.playSound("/shoot.wav");
    }

    public void updateOpponent(int x, int y, int score, int health) {
        this.opponentX = x;
        this.opponentY = y;
        this.opponentScore = score;
        this.opponentHealth = health;
    }

    public void setOpponentName(String name) {
        this.opponentName = name;
    }

    public void setOpponentShipImage(int shipType) {
        this.opponentShipImage = ResourceManager.getImage("/ship_" + shipType + ".png");
    }

    private void resetGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        parent.showMenu();
    }

    public void cleanUp() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (isMultiplayer && networkManager != null) {
            networkManager.disconnect();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, backgroundY, getWidth(), getHeight(), null);
        g.drawImage(background, 0, backgroundY - getHeight(), getWidth(), getHeight(), null);

        enemies.forEach(e -> e.draw(g));
        projectiles.forEach(p -> p.draw(g));
        explosions.forEach(e -> e.draw(g));
        networkProjectiles.forEach(p -> p.draw(g));
        player.draw(g);

        if (isMultiplayer) {
            drawOtherPlayers(g);
        }

        if (!isCompetitive) {
            drawInfoBoard(g);
            drawLives(g);
        }
        

        if (isLevelTransition) {
            drawLevelTransition(g);
        }

        if (gameOver) {
            drawGameOverScreen(g);
        }
    }

    private void drawOtherPlayers(Graphics g) {
        if (networkManager != null) {
            for (NetworkManager.PlayerData playerData : networkManager.getOtherPlayers()) {
                drawOtherPlayer(g, playerData);
            }
        }
    }

    private void drawOtherPlayer(Graphics g, NetworkManager.PlayerData playerData) {
        // Utiliser l'image du vaisseau de l'adversaire
        Image shipImage = opponentShipImage;
        g.drawImage(shipImage, playerData.getX(), playerData.getY(), 50, 60, null);

        // Dessiner la barre de vie

        // Dessiner le nom et le score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString(playerData.getName(), playerData.getX(), playerData.getY() - 20);
        g.drawString("Score: " + playerData.getScore(), playerData.getX(), playerData.getY() + 70);
    }



    private void drawLevelTransition(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String message = "LEVEL " + (levelManager.getCurrentLevel() + 1) + "!";
        g.drawString(message,
                getWidth() / 2 - g.getFontMetrics().stringWidth(message) / 2,
                getHeight() / 2);
    }

    private void drawInfoBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(getWidth() - 160, 10, 150, 80, 15, 15);
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(getWidth() - 160, 10, 150, 80, 15, 15);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        int yPos = 30;
        g2d.drawString(playerName, getWidth() - 150, yPos);
        g2d.drawString("Score: " + score, getWidth() - 150, yPos + 20);
        g2d.drawString("Lvl: " + levelManager.getCurrentLevel(), getWidth() - 150, yPos + 40);

        if (!isCompetitive) {
            g2d.drawString("Enemies: " + levelManager.getEnemiesDefeated() + "/" +
                    levelManager.getEnemiesToNextLevel(), getWidth() - 150, yPos + 60);
        }
    }

    private void drawLives(Graphics g) {
        int x = 20;
        int y = getHeight() - 50;
        for (int i = 0; i < player.getHealth(); i++) {
            g.drawImage(playerLifeIcon, x + (i * 35), y, null);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Difficulty: " + getDifficultyString(initialDifficulty), x, y - 20);
    }

    private String getDifficultyString(int difficulty) {
        switch (difficulty) {
            case 1: return "Easy";
            case 3: return "Normal";
            case 5: return "Hard";
            default: return "Custom";
        }
    }

    private void drawGameOverScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 40));

        String message;
        if (isCompetitive && isMultiplayer) {
            if (player.getHealth() <= 0) {
                message = "YOU LOST!";
            } else {
                message = "YOU WON!";
            }
        } else {
            message = "GAME OVER - Score: " + score;
        }

        g.drawString(message,
                getWidth() / 2 - g.getFontMetrics().stringWidth(message) / 2,
                getHeight() / 2);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press R to return to menu",
                getWidth() / 2 - 100,
                getHeight() / 2 + 50);

        if (!isCompetitive) {
            List<String> highscores = DatabaseManager.getHighScores(20);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Top Scores:", 50, getHeight() / 2 + 100);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            for (int i = 0; i < Math.min(5, highscores.size()); i++) {
                g.drawString(highscores.get(i), 50, getHeight() / 2 + 130 + i * 25);
            }
        }
    }

    public String getOpponentName() {
        return opponentName;
    }
}