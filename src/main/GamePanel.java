package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

// A classe GamePanel é o painel principal do jogo. Ela herda de JPanel e implementa Runnable para poder rodar em uma thread.
public class GamePanel extends JPanel implements Runnable {

    // ==== CONFIGURAÇÕES DE TELA ====
    final int originalTileSize = 16; // Tamanho original de cada tile (bloco), em pixels
    final int scale = 3; // Fator de escala dos tiles (aumenta o tamanho visual)

    final int tileSize = originalTileSize * scale; // Tamanho final de cada tile (48x48 pixels)
    final int maxScreenCol = 16; // Número máximo de colunas na tela
    final int maxScreenRow = 12; // Número máximo de linhas na tela
    final int screenWidth = tileSize * maxScreenCol; // Largura total da tela do jogo (768 pixels)
    final int screenHeight = tileSize * maxScreenRow; // Altura total da tela do jogo (576 pixels)

    int FPS = 60; // Taxa de quadros por segundo
    KeyHandler keyH = new KeyHandler(); // Objeto que escuta eventos de teclado

    // Posição inicial do jogador
    int playerX = 100;
    int playerY = 100;
    int playerSpeed = 4; // Velocidade de movimento do jogador

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

        this.addKeyListener(keyH); // Adiciona o manipulador de teclas
        this.setFocusable(true); // Garante que o GamePanel pode receber foco de teclado

    }

    // Método que inicia a thread do jogo
    public void startGameThread() {
        gameThread = new Thread(this); // Cria uma nova thread, passando a própria classe como Runnable
        gameThread.start(); // Inicia a execução da thread (chama o método run())
    }

    // Método que contém o loop principal do jogo, executado em uma thread separada
    @Override
    public void run() {

        // === CONFIGURAÇÕES DE FPS ===
        double drawInterval = 1000000000.0 / FPS; // Intervalo desejado entre frames (em nanossegundos)
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0; // Acumulador de tempo para medir 1 segundo
        int drawCount = 0; // Conta quantos frames foram desenhados no último segundo

        // === LOOP PRINCIPAL DO JOGO ===
        while (gameThread != null) {

            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update(); // Atualiza a lógica (movimento, colisão, etc.)
                repaint(); // Redesenha a tela
                delta--;
                drawCount++;
            }

            // A cada segundo, imprime o FPS no console e reinicia o contador
            if (timer >= 1000000000) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update() {
        // Movimento baseado nas teclas pressionadas
        if (keyH.upPressed) {
            playerY -= playerSpeed;
        } else if (keyH.downPressed) {
            playerY += playerSpeed;
        } else if (keyH.leftPressed) {
            playerX -= playerSpeed;
        } else if (keyH.rightPressed) {
            playerX += playerSpeed;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.white); // Define a cor como branca
        g2.fillRect(playerX, playerY, tileSize, tileSize); // Desenha o jogador como um quadrado

        g2.dispose(); // Libera recursos gráficos
    }
}
