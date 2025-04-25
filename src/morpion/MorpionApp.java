package morpion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MorpionApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainMenu().setVisible(true);
            }
        });
    }
}

class MainMenu extends JFrame {
    private static final long serialVersionUID = 1L;

    public MainMenu() {
        setTitle("Morpion - Menu Principal");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        // Modification du layout pour ne garder que 3 boutons (mode tournoi masqué)
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        JButton newGameButton = new JButton("Nouvelle Partie");
        newGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new MorpionFrame().setVisible(true);
                dispose();
            }
        });

        /* 
        // Le mode Tournoi est désactivé (caché) :
        JButton tournamentButton = new JButton("Mode Tournoi");
        tournamentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new TournamentMode().setVisible(true);
                dispose();
            }
        });
        */

        JButton historyButton = new JButton("Historique des Parties");
        historyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showHistory();
            }
        });

        JButton quitButton = new JButton("Quitter");
        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.add(newGameButton);
        // buttonPanel.add(tournamentButton); // Mode Tournoi désactivé
        buttonPanel.add(historyButton);
        buttonPanel.add(quitButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void showHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader("history.txt"))) {
            StringBuilder history = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                history.append(line).append("\n");
            }
            JOptionPane.showMessageDialog(this, history.toString(), "Historique des Parties", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Aucun historique disponible.", "Historique des Parties", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

/* 
// Classe TournamentMode désactivée (mode Tournoi caché)
class TournamentMode extends JFrame {
    private static final long serialVersionUID = 1L;
    private int rounds = 3; // Nombre de tours dans le tournoi
    private int scorePlayer1 = 0;
    private int scorePlayer2 = 0;
    private String player1Name = "Joueur 1";
    private String player2Name = "Joueur 2";
    private boolean vsComputer = false;

    public TournamentMode() {
        setTitle("Mode Tournoi");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Demande des noms
        player1Name = JOptionPane.showInputDialog(this, "Entrez le nom du Joueur 1:", "Joueur 1", JOptionPane.PLAIN_MESSAGE);
        player2Name = JOptionPane.showInputDialog(this, "Entrez le nom du Joueur 2:", "Joueur 2", JOptionPane.PLAIN_MESSAGE);
        if (player1Name == null || player1Name.trim().isEmpty()) {
            player1Name = "Joueur 1";
        }
        if (player2Name == null || player2Name.trim().isEmpty()) {
            player2Name = "Joueur 2";
        }

        // Demande du mode de jeu
        vsComputer = (JOptionPane.showConfirmDialog(this, "Voulez-vous jouer contre l'ordinateur ?", "Mode de jeu", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);

        // Lancer la boucle du tournoi dans un nouveau thread pour ne pas bloquer l’EDT
        new Thread(new Runnable() {
            public void run() {
                startTournament();
            }
        }).start();
    }

    private void startTournament() {
        for (int i = 1; i <= rounds; i++) {
            final int round = i; // Copie de i dans une variable finale
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(TournamentMode.this,
                        "Début de la partie " + round, "Tournoi", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            final MorpionFrame game = new MorpionFrame(player1Name, player2Name, vsComputer, true);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    game.setVisible(true);
                }
            });

            // Attendre que la fenêtre de jeu se ferme
            while (game.isVisible()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Mettre à jour les scores
            scorePlayer1 += game.getScorePlayer1();
            scorePlayer2 += game.getScorePlayer2();
        }

        final String winner = (scorePlayer1 > scorePlayer2) ? player1Name : player2Name;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(TournamentMode.this,
                    "Tournoi terminé!\n" +
                    player1Name + ": " + scorePlayer1 + " points\n" +
                    player2Name + ": " + scorePlayer2 + " points\n" +
                    "Gagnant: " + winner, "Résultats du Tournoi", JOptionPane.INFORMATION_MESSAGE);
                TournamentMode.this.dispose();
            }
        });
    }
}
*/

class MorpionFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JButton[][] buttons = new JButton[3][3];
    private String[][] board = new String[3][3];
    private String currentPlayer = "X";
    private String player1Name;
    private String player2Name;
    private boolean vsComputer;
    private boolean isTournament;
    private int scorePlayer1 = 0;
    private int scorePlayer2 = 0;
    private JLabel scoreLabel;
    private JComboBox<String> themeComboBox;
    private JButton switchModeButton;
    private Color borderColor = Color.WHITE;

    public MorpionFrame() {
        this("Joueur 1", "Joueur 2", false, false);
    }

    public MorpionFrame(String player1Name, String player2Name, boolean vsComputer, boolean isTournament) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.vsComputer = vsComputer;
        this.isTournament = isTournament;

        setTitle("Morpion");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel gamePanel = new JPanel(new GridLayout(3, 3));
        JPanel controlPanel = new JPanel(new FlowLayout());

        scoreLabel = new JLabel("Score: " + player1Name + " " + scorePlayer1 + " - " + player2Name + " " + scorePlayer2);
        controlPanel.add(scoreLabel);

        JButton newGameButton = new JButton("Nouvelle Partie");
        newGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        controlPanel.add(newGameButton);

        JButton historyButton = new JButton("Historique des Parties");
        historyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showHistory();
            }
        });
        controlPanel.add(historyButton);

        switchModeButton = new JButton(vsComputer ? "Mode: Joueur vs Ordinateur" : "Mode: 2 Joueurs");
        switchModeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toggleMode();
            }
        });
        controlPanel.add(switchModeButton);

        themeComboBox = new JComboBox<String>(new String[] {"Sombre", "Aléatoire"});
        themeComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeTheme();
            }
        });
        controlPanel.add(themeComboBox);

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(gamePanel, BorderLayout.CENTER);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = new CustomButton();
                buttons[i][j].setFont(new Font("Arial", Font.PLAIN, 40));
                buttons[i][j].setOpaque(true);
                buttons[i][j].setBorderPainted(false);
                final int row = i;
                final int col = j;
                buttons[i][j].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (buttons[row][col].getText().isEmpty()) {
                            buttons[row][col].setText(currentPlayer);
                            board[row][col] = currentPlayer;
                            checkWin();
                            currentPlayer = currentPlayer.equals("X") ? "O" : "X";
                            if (vsComputer && currentPlayer.equals("O")) {
                                computerMove();
                            }
                        }
                    }
                });
                gamePanel.add(buttons[i][j]);
            }
        }

        add(mainPanel);
        resetGame();
        changeTheme();
    }

    public int getScorePlayer1() {
        return scorePlayer1;
    }

    public int getScorePlayer2() {
        return scorePlayer2;
    }

    private void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                board[i][j] = "";
            }
        }
        currentPlayer = "X";
        scoreLabel.setText("Score: " + player1Name + " " + scorePlayer1 + " - " + player2Name + " " + scorePlayer2);
    }

    private void showHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader("history.txt"))) {
            StringBuilder history = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                history.append(line).append("\n");
            }
            JOptionPane.showMessageDialog(this, history.toString(), "Historique des Parties", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Aucun historique disponible.", "Historique des Parties", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void toggleMode() {
        vsComputer = !vsComputer;
        if (vsComputer) {
            switchModeButton.setText("Mode: Joueur vs Ordinateur");
            player2Name = "Ordinateur";
        } else {
            switchModeButton.setText("Mode: 2 Joueurs");
            player2Name = "Joueur 2";
        }
        resetGame();
    }

    private void changeTheme() {
        String selectedTheme = (String) themeComboBox.getSelectedItem();
        if (selectedTheme.equals("Sombre")) {
            getContentPane().setBackground(Color.DARK_GRAY);
            borderColor = Color.WHITE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    buttons[i][j].setBackground(Color.DARK_GRAY);
                    buttons[i][j].setForeground(Color.WHITE);
                    ((CustomButton) buttons[i][j]).setBorderColor(borderColor);
                }
            }
        } else {
            Random rand = new Random();
            Color randomColor = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            getContentPane().setBackground(randomColor);
            borderColor = Color.BLACK;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    buttons[i][j].setBackground(randomColor);
                    buttons[i][j].setForeground(Color.BLACK);
                    ((CustomButton) buttons[i][j]).setBorderColor(borderColor);
                }
            }
        }
        repaint();
    }

    private void checkWin() {
        for (int i = 0; i < 3; i++) {
            if (board[i][0].equals(currentPlayer) &&
                board[i][1].equals(currentPlayer) &&
                board[i][2].equals(currentPlayer)) {
                announceWin();
                return;
            }
            if (board[0][i].equals(currentPlayer) &&
                board[1][i].equals(currentPlayer) &&
                board[2][i].equals(currentPlayer)) {
                announceWin();
                return;
            }
        }
        if (board[0][0].equals(currentPlayer) &&
            board[1][1].equals(currentPlayer) &&
            board[2][2].equals(currentPlayer)) {
            announceWin();
            return;
        }
        if (board[0][2].equals(currentPlayer) &&
            board[1][1].equals(currentPlayer) &&
            board[2][0].equals(currentPlayer)) {
            announceWin();
            return;
        }
        if (isBoardFull()) {
            JOptionPane.showMessageDialog(this, "Match nul!", "Fin de la partie", JOptionPane.INFORMATION_MESSAGE);
            if (isTournament) {
                dispose();
            } else {
                resetGame();
            }
        }
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void announceWin() {
        String winnerName = currentPlayer.equals("X") ? player1Name : player2Name;
        JOptionPane.showMessageDialog(this, winnerName + " a gagné!", "Fin de la partie", JOptionPane.INFORMATION_MESSAGE);
        if (currentPlayer.equals("X")) {
            scorePlayer1++;
        } else {
            scorePlayer2++;
        }
        saveGameResult(winnerName);
        if (isTournament) {
            dispose();
        } else {
            resetGame();
        }
    }

    private void saveGameResult(String winnerName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("history.txt", true))) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String date = dateFormat.format(new Date());
            writer.write(date + " - " + player1Name + " vs " + player2Name + " - Gagnant: " + winnerName + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement de la partie.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void computerMove() {
        int[] bestMove = findBestMove();
        buttons[bestMove[0]][bestMove[1]].setText("O");
        board[bestMove[0]][bestMove[1]] = "O";
        checkWin();
        currentPlayer = "X";
    }

    private int[] findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = new int[] { -1, -1 };

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].isEmpty()) {
                    board[i][j] = "O";
                    int score = minimax(board, 0, false);
                    board[i][j] = "";
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove[0] = i;
                        bestMove[1] = j;
                    }
                }
            }
        }
        return bestMove;
    }

    private int minimax(String[][] board, int depth, boolean isMaximizing) {
        if (checkWinForPlayer("O")) return 1;
        if (checkWinForPlayer("X")) return -1;
        if (isBoardFull()) return 0;

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j].isEmpty()) {
                        board[i][j] = "O";
                        int score = minimax(board, depth + 1, false);
                        board[i][j] = "";
                        bestScore = Math.max(score, bestScore);
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j].isEmpty()) {
                        board[i][j] = "X";
                        int score = minimax(board, depth + 1, true);
                        board[i][j] = "";
                        bestScore = Math.min(score, bestScore);
                    }
                }
            }
            return bestScore;
        }
    }

    private boolean checkWinForPlayer(String player) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0].equals(player) && board[i][1].equals(player) && board[i][2].equals(player))
                return true;
            if (board[0][i].equals(player) && board[1][i].equals(player) && board[2][i].equals(player))
                return true;
        }
        if (board[0][0].equals(player) && board[1][1].equals(player) && board[2][2].equals(player))
            return true;
        if (board[0][2].equals(player) && board[1][1].equals(player) && board[2][0].equals(player))
            return true;
        return false;
    }

    private class CustomButton extends JButton {
        private static final long serialVersionUID = 1L;
        private Color borderColor;

        public CustomButton() {
            setContentAreaFilled(false);
        }

        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }
}
