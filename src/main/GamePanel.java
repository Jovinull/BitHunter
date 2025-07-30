package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import entity.Player;
import tile.TileManager;

// A classe GamePanel é o painel principal do jogo. Ela herda de JPanel e implementa Runnable para rodar em uma thread separada.
public class GamePanel extends JPanel implements Runnable {

    // ==== CONFIGURAÇÕES DE TELA ====
    final int originalTileSize = 16; // Tamanho original de cada tile (bloco), em pixels
    final int scale = 3; // Fator de escala dos tiles (aumenta o tamanho visual)

    public final int tileSize = originalTileSize * scale; // Tamanho final de cada tile (48x48 pixels)
    public final int maxScreenCol = 16; // Número máximo de colunas na tela
    public final int maxScreenRow = 12; // Número máximo de linhas na tela
    public final int screenWidth = tileSize * maxScreenCol; // Largura total da tela do jogo (768 pixels)
    public final int screenHeight = tileSize * maxScreenRow; // Altura total da tela do jogo (576 pixels)

    int FPS = 60; // Taxa de quadros por segundo (frames per second)

    TileManager tileM = new TileManager(this); // Gerenciador de tiles responsável por carregar e desenhar o mapa

    KeyHandler keyH = new KeyHandler(); // Objeto que escuta os eventos de teclado

    Thread gameThread; // Thread responsável pelo loop do jogo

    Player player = new Player(this, keyH); // Instância do jogador, usando o painel e os controles

    // Construtor da GamePanel: define tamanho, fundo e configurações de entrada
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight)); // Define o tamanho do painel
        this.setBackground(Color.black); // Cor de fundo preta
        this.setDoubleBuffered(true); // Usa buffer duplo para evitar flickering (tremores na imagem)

        this.addKeyListener(keyH); // Adiciona o controle de teclado ao painel
        this.setFocusable(true); // Permite que o painel receba foco e detecte as teclas
    }

    // Inicia a thread do jogo, que executará o método run()
    public void startGameThread() {
        gameThread = new Thread(this); // Cria nova thread com this como Runnable
        gameThread.start(); // Inicia a execução (chama run())
    }

    // Loop principal do jogo: atualiza lógica e redesenha a tela com base no FPS
    @Override
    public void run() {

        // === CONFIGURAÇÕES DE FPS ===
        double drawInterval = 1000000000.0 / FPS; // Intervalo entre frames em nanosegundos (ex: 60 FPS ≈ 16.6ms)
        double delta = 0;
        long lastTime = System.nanoTime(); // Tempo do último frame
        long currentTime;
        long timer = 0; // Contador de tempo para medir 1 segundo
        int drawCount = 0; // Quantos frames foram desenhados nesse segundo

        // === LOOP PRINCIPAL DO JOGO ===
        while (gameThread != null) {

            currentTime = System.nanoTime(); // Tempo atual do frame
            delta += (currentTime - lastTime) / drawInterval; // Acumula o tempo decorrido
            timer += (currentTime - lastTime); // Acumula para calcular FPS real
            lastTime = currentTime; // Atualiza referência de tempo

            // Se já passou tempo suficiente, atualiza e redesenha
            if (delta >= 1) {
                update(); // Atualiza a lógica do jogo (movimentação, colisões etc.)
                repaint(); // Requisição de redesenho (chama paintComponent)
                delta--;
                drawCount++;
            }

            // A cada 1 segundo, imprime o FPS e reseta contadores
            if (timer >= 1000000000) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    // Atualiza a lógica do jogo — neste caso, delega para o objeto player
    public void update() {
        player.update();
    }

    // Método responsável por redesenhar os elementos gráficos na tela
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Limpa o painel antes de desenhar novamente

        Graphics2D g2 = (Graphics2D) g; // Conversão para Graphics2D (mais recursos)

        tileM.draw(g2); // Desenha o mapa de tiles antes de desenhar o jogador

        player.draw(g2); // Desenha o jogador na tela

        g2.dispose(); // Libera os recursos do objeto gráfico após uso
    }
}
