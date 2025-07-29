package main;

import javax.swing.JFrame;

// Classe principal que inicia o jogo
public class Main {

    public static void main(String[] args) {
        // Criação da janela principal do jogo
        JFrame window = new JFrame();

        // Define o comportamento ao fechar a janela: termina o programa
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Impede o redimensionamento da janela pelo usuário
        window.setResizable(false);

        // Define o título da janela
        window.setTitle("Bit Hunter");

        // Cria o painel do jogo e adiciona à janela
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        // Ajusta o tamanho da janela com base no tamanho preferido do GamePanel
        window.pack();

        // Centraliza a janela no meio da tela
        window.setLocationRelativeTo(null);

        // Torna a janela visível na tela
        window.setVisible(true);
    }
}
