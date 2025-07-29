package main;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;

// A classe GamePanel é o painel principal do jogo. Ela herda de JPanel e implementa Runnable para poder rodar em uma thread.
public class GamePanel extends JPanel implements Runnable {

    // ==== CONFIGURAÇÕES DE TELA ====
    final int originalTileSize = 16; // Tamanho original de cada tile (bloco), em pixels
    final int scale = 3;             // Fator de escala dos tiles (aumenta o tamanho visual)

    final int tileSize = originalTileSize * scale;       // Tamanho final de cada tile (48x48 pixels)
    final int maxScreenCol = 16;                         // Número máximo de colunas na tela
    final int maxScreenRow = 12;                         // Número máximo de linhas na tela
    final int screenWidth = tileSize * maxScreenCol;     // Largura total da tela do jogo (768 pixels)
    final int screenHeight = tileSize * maxScreenRow;    // Altura total da tela do jogo (576 pixels)

    // Thread responsável por executar o loop do jogo
    Thread gameThread;

    // Construtor da GamePanel: define o tamanho, fundo e performance do painel
    public GamePanel() {
        // Define o tamanho preferido da tela com base na largura e altura calculadas
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));

        // Define a cor de fundo como preta
        this.setBackground(Color.black);

        // Habilita o uso de buffer duplo para evitar flickering na renderização
        this.setDoubleBuffered(true);
    }

    // Método que inicia a thread do jogo
    public void startGameThread() {
        gameThread = new Thread(this);  // Cria uma nova thread, passando a própria classe como Runnable
        gameThread.start();             // Inicia a execução da thread (chama o método run())
    }

    // Método que contém o loop principal do jogo, executado em uma thread separada
    @Override
    public void run() {
        // O conteúdo do loop do jogo será colocado aqui posteriormente.
        // Normalmente isso envolve:
        // - Atualizar o estado do jogo (ex: posição do jogador)
        // - Redesenhar a tela (chamar repaint())
        // - Esperar um tempo para controlar o FPS
    }
}
