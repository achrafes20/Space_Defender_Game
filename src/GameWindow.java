import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class GameWindow extends JFrame {
    private MenuPanel menuPanel;
    private GamePanel gamePanel;
    private HighscorePanel highscorePanel;
    private SettingsPanel settingsPanel;
    private MultiplayerPanel multiplayerPanel;

    public GameWindow() {
        setTitle("Space Defender");
        setSize(800, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);


        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/game_icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Icon image not found, using default");
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Couldn't set system look and feel");
        }

        showMenu();
        setVisible(true);
    }

    public void showMenu() {
        cleanUpCurrentPanel();
        menuPanel = new MenuPanel(this);
        switchToPanel(menuPanel);
    }

    public void startGame(String playerName, int difficulty, int shipType, boolean isMultiplayer, boolean isCompetitive, String serverIP) {
        cleanUpCurrentPanel();
        gamePanel = new GamePanel(this, playerName, difficulty, shipType, isMultiplayer, isCompetitive, serverIP);
        switchToPanel(gamePanel);
    }

    public void showHighscores() {
        cleanUpCurrentPanel();
        highscorePanel = new HighscorePanel(this);
        switchToPanel(highscorePanel);
    }

    public void showSettings() {
        cleanUpCurrentPanel();
        settingsPanel = new SettingsPanel(this);
        switchToPanel(settingsPanel);
    }

    public void showMultiplayerPanel() {
        cleanUpCurrentPanel();
        multiplayerPanel = new MultiplayerPanel(this);
        switchToPanel(multiplayerPanel);
    }

    private void cleanUpCurrentPanel() {
        if (gamePanel != null) {
            gamePanel.cleanUp();
            remove(gamePanel);
            gamePanel = null;
        }
        if (menuPanel != null) {
            remove(menuPanel);
            menuPanel = null;
        }
        if (highscorePanel != null) {
            remove(highscorePanel);
            highscorePanel = null;
        }
        if (settingsPanel != null) {
            remove(settingsPanel);
            settingsPanel = null;
        }
        if (multiplayerPanel != null) {
            remove(multiplayerPanel);
            multiplayerPanel = null;
        }
    }

    private void switchToPanel(JPanel panel) {
        add(panel);
        revalidate();
        repaint();
        panel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ResourceManager.preloadResources();
            GameWindow window = new GameWindow();
            window.setLocationRelativeTo(null);

            // Animation d'ouverture

            Timer fadeIn = new Timer(20, e -> {
                float opacity = window.getOpacity();
                if (opacity < 1f) {
                    window.setOpacity(opacity + 0.05f);
                } else {
                    ((Timer) e.getSource()).stop();
                }
            });
            fadeIn.start();
        });
    }

    private static class HighscorePanel extends JPanel {
        public HighscorePanel(GameWindow parent) {
            setLayout(new BorderLayout());
            setBackground(new Color(30, 30, 50));

            JLabel title = new JLabel("HIGH SCORES", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 36));
            title.setForeground(new Color(255, 215, 0));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

            JTextArea scoresArea = new JTextArea();
            scoresArea.setEditable(false);
            scoresArea.setBackground(new Color(30, 30, 50));
            scoresArea.setForeground(Color.WHITE);
            scoresArea.setFont(new Font("Arial", Font.PLAIN, 18));

            List<String> highscores = DatabaseManager.getHighScores(10);
            if (highscores.isEmpty()) {
                scoresArea.setText("No scores recorded yet");
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < highscores.size(); i++) {
                    sb.append(String.format("%2d. %s%n", i + 1, highscores.get(i)));
                }
                scoresArea.setText(sb.toString());
            }

            JScrollPane scrollPane = new JScrollPane(scoresArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            add(scrollPane, BorderLayout.CENTER);

            JButton backButton = new JButton("BACK TO MENU");
            backButton.addActionListener(e -> parent.showMenu());
            SettingsPanel.styleButton(backButton, new Color(70, 130, 180));

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(new Color(30, 30, 50));
            buttonPanel.add(backButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
    }

    private static class MultiplayerPanel extends JPanel {
        public MultiplayerPanel(GameWindow parent) {
            setLayout(new GridBagLayout());
            setBackground(new Color(30, 30, 50));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 15, 15, 15);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;

            JLabel title = new JLabel("MULTIPLAYER", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 36));
            title.setForeground(new Color(255, 215, 0));
            add(title, gbc);

            JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
            optionsPanel.setBackground(new Color(0, 0, 0, 120));
            optionsPanel.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
            gbc.gridy++;
            add(optionsPanel, gbc);

            // Player Name
            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            namePanel.setBackground(new Color(0, 0, 0, 0));
            JLabel nameLabel = new JLabel("Player Name:");
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            namePanel.add(nameLabel);

            JTextField nameField = new JTextField(15);
            nameField.setText("Player1");
            nameField.setFont(new Font("Arial", Font.PLAIN, 12));
            nameField.setForeground(Color.WHITE);
            nameField.setBackground(new Color(50, 50, 80));
            nameField.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 150)));
            namePanel.add(nameField);
            optionsPanel.add(namePanel);

            // Server IP
            JPanel ipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            ipPanel.setBackground(new Color(0, 0, 0, 0));
            JLabel ipLabel = new JLabel("Server IP:");
            ipLabel.setForeground(Color.WHITE);
            ipLabel.setFont(new Font("Arial", Font.BOLD, 14));
            ipPanel.add(ipLabel);

            JTextField ipField = new JTextField(15);
            ipField.setText("localhost");
            ipField.setFont(new Font("Arial", Font.PLAIN, 12));
            ipField.setForeground(Color.WHITE);
            ipField.setBackground(new Color(50, 50, 80));
            ipField.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 150)));
            ipPanel.add(ipField);
            optionsPanel.add(ipPanel);

            // Game Mode Selection
            JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            modePanel.setBackground(new Color(0, 0, 0, 0));
            JLabel modeLabel = new JLabel("Game Mode:");
            modeLabel.setForeground(Color.WHITE);
            modeLabel.setFont(new Font("Arial", Font.BOLD, 14));
            modePanel.add(modeLabel);

            ButtonGroup modeGroup = new ButtonGroup();
            JRadioButton coopButton = new JRadioButton("Cooperative");
            JRadioButton versusButton = new JRadioButton("1vs1");

            coopButton.setSelected(true);
            coopButton.setForeground(Color.WHITE);
            coopButton.setBackground(new Color(50, 50, 80));
            coopButton.setOpaque(true);
            coopButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    coopButton.setBackground(new Color(70, 70, 100));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    coopButton.setBackground(new Color(50, 50, 80));
                }
            });

            versusButton.setForeground(Color.WHITE);
            versusButton.setBackground(new Color(50, 50, 80));
            coopButton.setOpaque(true);

            modeGroup.add(coopButton);
            modeGroup.add(versusButton);

            modePanel.add(coopButton);
            modePanel.add(versusButton);
            optionsPanel.add(modePanel);

            // Ship Selection
            JPanel shipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            shipPanel.setBackground(new Color(0, 0, 0, 0));
            JLabel shipLabel = new JLabel("Ship:");
            shipLabel.setForeground(Color.WHITE);
            shipLabel.setFont(new Font("Arial", Font.BOLD, 14));
            shipPanel.add(shipLabel);

            DefaultComboBoxModel<MenuPanel.ShipItem> model = new DefaultComboBoxModel<>();
            model.addElement(new MenuPanel.ShipItem("Standard", 0));
            model.addElement(new MenuPanel.ShipItem("Fast", 1));
            model.addElement(new MenuPanel.ShipItem("Heavy", 2));

            JComboBox<MenuPanel.ShipItem> shipCombo = new JComboBox<>(model);
            shipCombo.setFont(new Font("Arial", Font.PLAIN, 12));
            shipCombo.setForeground(Color.BLACK);
            shipCombo.setBackground(new Color(50, 50, 80));
            shipCombo.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            shipCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                              boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    MenuPanel.ShipItem item = (MenuPanel.ShipItem)value;
                    setText(item.toString());
                    setIcon(new ImageIcon(ResourceManager.getImage("/ship_" + item.type + ".png")
                            .getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
                    setBackground(isSelected ? new Color(70, 130, 180) : new Color(50, 50, 80));
                    setForeground(Color.WHITE);
                    setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                    return this;
                }
            });
            shipPanel.add(shipCombo);
            optionsPanel.add(shipPanel);

            // Buttons
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
            buttonPanel.setBackground(new Color(0, 0, 0, 0));

            JButton joinButton = new JButton("JOIN GAME");
            joinButton.addActionListener(e -> {
                String playerName = nameField.getText().trim();
                if (playerName.isEmpty()) playerName = "Player1";
                String serverIP = ipField.getText().trim();
                boolean isCompetitive = versusButton.isSelected();
                int shipType = ((MenuPanel.ShipItem)shipCombo.getSelectedItem()).type;

                parent.startGame(playerName, 3, shipType, true, isCompetitive, serverIP);
            });
            SettingsPanel.styleButton(joinButton, new Color(70, 130, 180));

            JButton hostButton = new JButton("HOST GAME");
            hostButton.addActionListener(e -> {
                String playerName = nameField.getText().trim();
                if (playerName.isEmpty()) playerName = "Player1";
                boolean isCompetitive = versusButton.isSelected();
                int shipType = ((MenuPanel.ShipItem)shipCombo.getSelectedItem()).type;

                new Thread(() -> {
                    GameServer server = new GameServer();
                    server.start();
                }).start();

                parent.startGame(playerName, 3, shipType, true, isCompetitive, "localhost");
            });
            SettingsPanel.styleButton(hostButton, new Color(46, 139, 87));

            buttonPanel.add(joinButton);
            buttonPanel.add(hostButton);
            gbc.gridy++;
            add(buttonPanel, gbc);

            JButton backButton = new JButton("BACK TO MENU");
            backButton.addActionListener(e -> parent.showMenu());
            SettingsPanel.styleButton(backButton, new Color(139, 0, 139));
            gbc.gridy++;
            add(backButton, gbc);
        }
    }

    private static class SettingsPanel extends JPanel {
        public SettingsPanel(GameWindow parent) {
            setLayout(new GridBagLayout());
            setBackground(new Color(30, 30, 50));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 15, 15, 15);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;

            JLabel title = new JLabel("SETTINGS", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 36));
            title.setForeground(new Color(255, 215, 0));
            add(title, gbc);

            JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
            optionsPanel.setBackground(new Color(0, 0, 0, 150));
            optionsPanel.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
            gbc.gridy++;
            add(optionsPanel, gbc);

            addVolumeControls(optionsPanel);

            JButton backButton = new JButton("BACK TO MENU");
            backButton.addActionListener(e -> parent.showMenu());
            styleButton(backButton, new Color(70, 130, 180));
            gbc.gridy++;
            add(backButton, gbc);
        }

        private void addVolumeControls(JPanel panel) {
            JPanel musicPanel = new JPanel();
            musicPanel.setBackground(new Color(0, 0, 0, 0));
            JLabel musicLabel = new JLabel("Music Volume:");
            musicLabel.setForeground(Color.white);
            musicPanel.add(musicLabel);

            JSlider musicSlider = new JSlider(0, 100, SoundManager.getMusicVolume());
            musicSlider.addChangeListener(e -> SoundManager.setMusicVolume(musicSlider.getValue()));
            musicPanel.add(musicSlider);
            panel.add(musicPanel);

            JPanel soundPanel = new JPanel();
            soundPanel.setBackground(new Color(0, 0, 0, 0));
            JLabel soundLabel = new JLabel("Sound Volume:");
            soundLabel.setForeground(Color.white);
            soundPanel.add(soundLabel);

            JSlider soundSlider = new JSlider(0, 100, SoundManager.getSoundVolume());
            soundSlider.addChangeListener(e -> SoundManager.setSoundVolume(soundSlider.getValue()));
            soundPanel.add(soundSlider);
            panel.add(soundPanel);

            musicSlider.setBackground(new Color(200, 200, 200));
            musicSlider.setForeground(Color.BLACK);
            soundSlider.setBackground(new Color(200, 200, 200));
            soundSlider.setForeground(Color.BLACK);
        }

        private static void styleButton(JButton button, Color color) {
            button.setFont(new Font("Arial", Font.BOLD, 16));
            button.setBackground(color);
            button.setForeground(Color.BLACK);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(color.brighter());
                    button.setForeground(Color.BLACK);
                    SoundManager.playSound("/button_hover.wav");
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(color);
                    button.setForeground(Color.BLACK);
                }
            });
        }
    }
}