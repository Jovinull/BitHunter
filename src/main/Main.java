package main;

import javax.swing.JFrame;

// Classe principal que inicia o jogo
public class Main {

    public static JFrame window; // Janela principal do jogo

    public static void main(String[] args) {
        // Criação da janela principal do jogo
        window = new JFrame();

        // Define o comportamento ao fechar a janela: termina o programa
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Impede o redimensionamento da janela pelo usuário
        window.setResizable(false);

        // Define o título da janela
        window.setTitle("Bit Hunter");

        // Remove bordas e barra de título da janela
        window.setUndecorated(true);

        // Cria o painel do jogo e adiciona à janela
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

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
}
