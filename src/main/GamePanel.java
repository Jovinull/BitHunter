package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import entity.Player;
import object.SuperObject;
import tile.TileManager;

// Painel principal do jogo. Responsável por configurar, atualizar e desenhar o jogo.
// Implementa Runnable para rodar o loop de jogo em uma thread separada.
public class GamePanel extends JPanel implements Runnable {

    // ==== CONFIGURAÇÕES DE TELA (visível) ====
    final int originalTileSize = 16; // Tamanho base de um tile (16x16 pixels)
    final int scale = 3; // Fator de escala aplicado ao tile

    public final int tileSize = originalTileSize * scale; // Tile final com escala (48x48 pixels)
    public final int maxScreenCol = 16; // Número de colunas visíveis na tela
    public final int maxScreenRow = 12; // Número de linhas visíveis na tela
    public final int screenWidth = tileSize * maxScreenCol; // Largura total da tela (768 px)
    public final int screenHeight = tileSize * maxScreenRow; // Altura total da tela (576 px)

    // ==== CONFIGURAÇÕES DO MUNDO ====
    public final int maxWorldCol = 50; // Quantidade total de colunas no mundo
    public final int maxWorldRow = 50; // Quantidade total de linhas no mundo

    // ==== CONFIGURAÇÃO DE FPS ====
    int FPS = 60; // Quadros por segundo

    // ==== COMPONENTES DO JOGO ====
    TileManager tileM = new TileManager(this); // Gerencia o carregamento e desenho dos tiles
    KeyHandler keyH = new KeyHandler(); // Captura entrada de teclado
    Sound music = new Sound(); // Gerencia a música do jogo
    Sound se = new Sound(); // Gerencia os efeitos sonoros do jogo
    public CollisionChecker cChecker = new CollisionChecker(this); // Verifica colisões entre entidades e tiles
    public AssetSetter aSetter = new AssetSetter(this); // Configura os objetos do jogo (itens, inimigos, etc.)
    Thread gameThread; // Thread para rodar o loop do jogo

    // ENTITY AND OBJECTS
    public Player player = new Player(this, keyH); // Jogador principal do jogo
    public SuperObject obj[] = new SuperObject[10]; // Array para armazenar objetos do jogo

    // ==== CONSTRUTOR ====
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight)); // Define tamanho da tela
        this.setBackground(Color.black); // Cor de fundo
        this.setDoubleBuffered(true); // Habilita buffer duplo para evitar flickering

        this.addKeyListener(keyH); // Liga o controle de teclado ao painel
        this.setFocusable(true); // Permite que o painel receba foco do teclado
    }

    // Prepara e inicializa elementos principais do jogo antes de começar o loop
    public void setupGame() {
        // Instancia e posiciona todos os objetos (chaves, portas, baús)
        aSetter.setObject();

        // Inicia a música de fundo
        playMusic(0);
    }

    // ==== INICIALIZA A THREAD DO JOGO ====
    public void startGameThread() {
        gameThread = new Thread(this); // Cria a thread com o próprio painel como Runnable
        gameThread.start(); // Inicia o método run() na thread
    }

    // ==== LOOP PRINCIPAL DO JOGO ====
    @Override
    public void run() {

        // Controla o tempo entre os frames com base no FPS
        double drawInterval = 1000000000.0 / FPS; // Tempo alvo entre cada frame (em nanossegundos)
        double delta = 0;
        long lastTime = System.nanoTime(); // Marca o tempo do frame anterior
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update(); // Atualiza lógica do jogo (movimento, eventos, etc.)
                repaint(); // Redesenha a tela (chama paintComponent)
                delta--;
                drawCount++;
            }

            // A cada segundo, mostra o FPS no console e reinicia o contador
            if (timer >= 1000000000) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    // ==== ATUALIZA A LÓGICA DO JOGO ====
    public void update() {
        player.update(); // Atualiza o jogador (movimentação e animação)
    }

    // ==== DESENHA NA TELA ====
    // Método responsável por desenhar todo o frame do jogo
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Limpa o painel (fundo) antes de desenhar novos elementos

        // Converte para Graphics2D para aproveitar recursos avançados de renderização
        Graphics2D g2 = (Graphics2D) g;

        // Desenha o mapa: tiles visíveis de acordo com a câmera/jogador
        tileM.draw(g2);

        // Percorre todos os objetos do mundo e desenha os que estiverem instanciados
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] != null) {
                obj[i].draw(g2, this);
            }
        }

        // Desenha o jogador no centro da tela (posição relativa à câmera)
        player.draw(g2);

        // Libera recursos do contexto gráfico após o desenho
        g2.dispose();
    }

    public void playMusic(int index) {

        music.setFile(index); // Define o arquivo de som a ser tocado
        music.play(); // Toca o som
        music.loop(); // Reproduz em loop até ser parado
    }

    public void stopMusic() {

        music.stop(); // Para a música atual
    }

    public void playSE(int index) {

        se.setFile(index); // Define o arquivo de efeito sonoro
        se.play(); // Toca o efeito sonoro
    }
}
