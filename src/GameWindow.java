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

    public GameWindow() {
        setTitle("Space Defender");
        setSize(800, 600);
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

    public void startGame(String playerName, int difficulty, int shipType, boolean isMultiplayer) {
        cleanUpCurrentPanel();
        gamePanel = new GamePanel(this, playerName, difficulty, shipType, isMultiplayer);
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
            window.setOpacity(0f);
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

            // Modification ici - texte en noir
            JLabel musicLabel = new JLabel("Music Volume:");
            musicLabel.setForeground(Color.white); // Texte en noir
            musicPanel.add(musicLabel);

            JSlider musicSlider = new JSlider(0, 100, SoundManager.getMusicVolume());
            musicSlider.addChangeListener(e -> SoundManager.setMusicVolume(musicSlider.getValue()));
            musicPanel.add(musicSlider);
            panel.add(musicPanel);

            JPanel soundPanel = new JPanel();
            soundPanel.setBackground(new Color(0, 0, 0, 0));

            // Modification ici - texte en noir
            JLabel soundLabel = new JLabel("Sound Volume:");
            soundLabel.setForeground(Color.white); // Texte en noir
            soundPanel.add(soundLabel);

            JSlider soundSlider = new JSlider(0, 100, SoundManager.getSoundVolume());
            soundSlider.addChangeListener(e -> SoundManager.setSoundVolume(soundSlider.getValue()));
            soundPanel.add(soundSlider);
            panel.add(soundPanel);
            musicSlider.setBackground(new Color(200, 200, 200)); // Fond clair
            musicSlider.setForeground(Color.BLACK); // Curseur en noir

// Pour le slider de son
            soundSlider.setBackground(new Color(200, 200, 200)); // Fond clair
            soundSlider.setForeground(Color.BLACK); // Curseur en noir
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