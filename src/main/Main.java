package main;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

// Classe principal que inicia o jogo
public class Main {

  public static final JFrame window = new JFrame(); // Janela principal do jogo

  private Main() {
    // Evita instanciação acidental desta classe utilitária
  }

  public static void main(String[] args) {
    // Define o comportamento ao fechar a janela: termina o programa
    window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    // Impede o redimensionamento da janela pelo usuário
    window.setResizable(false);

    // Define o título da janela
    window.setTitle("Bit Hunter");

    // Define o ícone da janela
    setIcon();

    // Cria o painel do jogo e adiciona à janela
    GamePanel gamePanel = new GamePanel();
    window.add(gamePanel);

    // Carrega as configurações do jogo
    gamePanel.config.loadConfig();

    // Aplica modo tela cheia se configurado
    if (gamePanel.isFullScreenOn()) {
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

  private static void setIcon() {
    URL iconUrl = Main.class.getClassLoader().getResource("objects/blueheart.png");
    if (iconUrl != null) {
      ImageIcon icon = new ImageIcon(iconUrl);
      window.setIconImage(icon.getImage());
    }
    // Se for null, apenas não define o ícone (evita possível NullPointerException)
  }
}
