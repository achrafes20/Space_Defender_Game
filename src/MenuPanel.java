import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.sound.sampled.Clip;

public class MenuPanel extends JPanel {
    private final GameWindow parent;
    private Clip menuMusic;
    private Image backgroundImage;

    public MenuPanel(GameWindow parent) {
        this.parent = parent;
        this.backgroundImage = ResourceManager.getImage("/menu_background.png");
        setLayout(new GridBagLayout());
        setupUI();
        playMenuMusic();
    }

    private void setupUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        addTitle(gbc);
        addOptionsPanel(gbc);
        addStartButton(gbc);
        addMultiplayerButton(gbc);
        addHighscoresButton(gbc);
        addSettingsButton(gbc);
        setupKeyboardShortcut();
    }

    private void addTitle(GridBagConstraints gbc) {
        JLabel title = new JLabel("SPACE DEFENDER", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(new Color(215, 112, 126));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, gbc);
    }

    private void addOptionsPanel(GridBagConstraints gbc) {
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBackground(new Color(0, 0, 0, 120));
        optionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180, 150)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        gbc.gridy++;
        add(optionsPanel, gbc);

        GridBagConstraints gbcOptions = new GridBagConstraints();
        gbcOptions.insets = new Insets(5, 5, 5, 5);
        gbcOptions.gridx = 0;
        gbcOptions.gridy = 0;
        gbcOptions.anchor = GridBagConstraints.LINE_START;

        addPlayerNameField(optionsPanel, gbcOptions);
        addDifficultyCombo(optionsPanel, gbcOptions);
        addShipCombo(optionsPanel, gbcOptions);
    }

    private void addPlayerNameField(JPanel panel, GridBagConstraints gbc) {
        JLabel nameLabel = new JLabel("Player Name:");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(nameLabel, gbc);

        gbc.gridy++;
        JTextField nameField = new JTextField(12) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(50, 50, 80));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.setColor(new Color(100, 100, 150));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        nameField.setFont(new Font("Arial", Font.PLAIN, 12));
        nameField.setText("Player1");
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        nameField.setOpaque(false);
        nameField.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        panel.add(nameField, gbc);
    }

    private void addDifficultyCombo(JPanel panel, GridBagConstraints gbc) {
        gbc.gridy++;
        JLabel levelLabel = new JLabel("Difficulty:");
        levelLabel.setForeground(Color.white);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(levelLabel, gbc);

        gbc.gridy++;
        String[] levels = { "Easy", "Normal", "Hard", "Extreme" };
        JComboBox<String> levelCombo = new JComboBox<String>(levels) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(50, 50, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(new Color(100, 100, 150));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        levelCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        levelCombo.setSelectedIndex(1);
        levelCombo.setForeground(Color.black);
        levelCombo.setBackground(new Color(50, 50, 80));
        levelCombo.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        levelCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(70, 130, 180) : new Color(50, 50, 80));
                setForeground(Color.WHITE);
                setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        panel.add(levelCombo, gbc);
    }

    private void addShipCombo(JPanel panel, GridBagConstraints gbc) {
        gbc.gridy++;
        JLabel shipLabel = new JLabel("Ship:");
        shipLabel.setForeground(Color.WHITE);
        shipLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(shipLabel, gbc);

        gbc.gridy++;
        DefaultComboBoxModel<ShipItem> model = new DefaultComboBoxModel<>();
        model.addElement(new ShipItem("Standard", 0));
        model.addElement(new ShipItem("Fast", 1));
        model.addElement(new ShipItem("Heavy", 2));

        JComboBox<ShipItem> shipCombo = new JComboBox<ShipItem>(model) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(50, 50, 80));

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(new Color(100, 100, 150));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        shipCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        shipCombo.setForeground(Color.black);
        shipCombo.setBackground(new Color(50, 50, 80));
        shipCombo.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        shipCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                ShipItem item = (ShipItem)value;
                setText(item.toString());
                setIcon(new ImageIcon(ResourceManager.getImage("/ship_" + item.type + ".png")
                        .getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
                setBackground(isSelected ? new Color(70, 130, 180) : new Color(50, 50, 80));
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        panel.add(shipCombo, gbc);
    }

    public static class ShipItem {
        String name;
        int type;

        public ShipItem(String name, int type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private void addStartButton(GridBagConstraints gbc) {
        gbc.gridy++;
        JButton startButton = createStyledButton("START GAME", new Color(70, 130, 180));
        startButton.addActionListener(e -> startGame());
        add(startButton, gbc);
    }

    private void addMultiplayerButton(GridBagConstraints gbc) {
        gbc.gridy++;
        JButton multiplayerButton = createStyledButton("MULTIPLAYER", new Color(46, 139, 87));
        multiplayerButton.addActionListener(e -> parent.showMultiplayerPanel());
        add(multiplayerButton, gbc);
    }

    private void addHighscoresButton(GridBagConstraints gbc) {
        gbc.gridy++;
        JButton highscoresButton = createStyledButton("HIGH SCORES", new Color(139, 0, 139));
        highscoresButton.addActionListener(e -> showHighscores());
        add(highscoresButton, gbc);
    }

    private void addSettingsButton(GridBagConstraints gbc) {
        gbc.gridy++;
        JButton settingsButton = createStyledButton("SETTINGS", new Color(255, 140, 0));
        settingsButton.addActionListener(e -> showSettings());
        add(settingsButton, gbc);
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                        0, 0, baseColor.brighter(),
                        0, getHeight(), baseColor.darker());
                g2.setPaint(gradient);
                int arc = 15;
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));
                g2.setColor(baseColor.darker().darker());
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth()-2, getHeight()-2, arc, arc));
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRoundRect(2, 2, getWidth()-4, getHeight()/2, arc, arc);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.setColor(new Color(0, 0, 0, 100));
                g2.drawString(getText(), x+1, y+1);
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 255, 255, 120)),
                        BorderFactory.createEmptyBorder(7, 19, 7, 19)
                ));
                SoundManager.playSound("/button_hover.wav");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            }
            @Override
            public void mousePressed(MouseEvent e) {
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 0, 0, 100)),
                        BorderFactory.createEmptyBorder(8, 20, 8, 20)
                ));
            }
        });
        return button;
    }

    private void setupKeyboardShortcut() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start");
        getActionMap().put("start", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });
    }

    private void playMenuMusic() {
        SoundManager.playMusic("/menu_music.wav");
    }

    private void startGame() {
        SoundManager.playSound("/button_click.wav");

        String playerName = "Player1";
        int difficulty = 3;
        int shipType = 0;

        Component[] components = ((JPanel)getComponent(1)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JTextField) {
                playerName = ((JTextField)comp).getText().trim();
                if (playerName.isEmpty()) playerName = "Player1";
            }
            else if (comp instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>)comp;
                if (combo.getItemAt(0) instanceof ShipItem) {
                    shipType = ((ShipItem)combo.getSelectedItem()).type;
                } else if (combo.getItemAt(0).toString().equals("Easy")) {
                    switch (combo.getSelectedIndex()) {
                        case 0: difficulty = 1; break;
                        case 1: difficulty = 3; break;
                        case 2: difficulty = 5; break;
                        case 3: difficulty = 7; break;
                    }
                }
            }
        }

        parent.startGame(playerName, difficulty, shipType, false, false, "");
    }

    private void showHighscores() {
        SoundManager.playSound("/button_click.wav");
        parent.showHighscores();
    }

    private void showSettings() {
        SoundManager.playSound("/button_click.wav");
        parent.showSettings();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(10, 10, 30));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}