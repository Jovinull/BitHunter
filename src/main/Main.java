package main;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

// Classe principal que inicia o jogo
public class Main {

  public static final JFrame window = new JFrame(); // Janela principal do jogo

  public static void main(String[] args) {
    // Criação da janela principal do jogo

    // Define o comportamento ao fechar a janela: termina o programa
    window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    // Impede o redimensionamento da janela pelo usuário
    window.setResizable(false);

    // Define o título da janela
    window.setTitle("Bit Hunter");

    new Main().setIcon();

    // Cria o painel do jogo e adiciona à janela
    GamePanel gamePanel = new GamePanel();
    window.add(gamePanel);

    gamePanel.config.loadConfig(); // Carrega as configurações do jogo
    if (gamePanel.fullScreenOn) {
      window.setUndecorated(true);
    }

    // Ajusta o tamanho da janela com base no tamanho preferido do GamePanel
    window.pack();

    // Centraliza a janela no meio da tela
    window.setLocationRelativeTo(null);

    // Torna a janela visível na tela
    window.setVisible(true);

    // Configura o jogo (carrega objetos, etc.)
    gamePanel.setupGame();

    // Inicia a thread do jogo (começa o loop do jogo)
    gamePanel.startGameThread();
  }

  public void setIcon() {
    ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("objects/blueheart.png"));
    window.setIconImage(icon.getImage());
  }
}
