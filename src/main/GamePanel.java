package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import entity.Entity;
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
    public KeyHandler keyH = new KeyHandler(this); // Captura entrada de teclado
    Sound music = new Sound(); // Gerencia a música do jogo
    Sound se = new Sound(); // Gerencia os efeitos sonoros do jogo
    public CollisionChecker cChecker = new CollisionChecker(this); // Verifica colisões entre entidades e tiles
    public AssetSetter aSetter = new AssetSetter(this); // Configura os objetos do jogo (itens, inimigos, etc.)
    public UI ui = new UI(this); // Interface do usuário (HUD, mensagens, etc.)
    public EventHandler eHandler = new EventHandler(this);
    Thread gameThread; // Thread para rodar o loop do jogo

    // ENTITY AND OBJECTS
    public Player player = new Player(this, keyH); // Jogador principal do jogo
    public SuperObject obj[] = new SuperObject[10]; // Array para armazenar objetos do jogo
    public Entity npc[] = new Entity[10]; // Array para armazenar NPCs (personagens não jogáveis)

    // ==== GAME STATE ====
    public int gameState; // Estado atual do jogo
    public final int titleState = 0; // Estado do título (menu inicial)
    public final int playState = 1; // Estado de jogo (jogando)
    public final int pauseState = 2; // Estado de pausa
    public final int dialogueState = 3; // Estado de diálogo (interações com NPCs)

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
        aSetter.setNPC();

        // Inicia a música de fundo
        // playMusic(0);

        gameState = titleState; // Define o estado inicial do jogo como "menu de título"
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

        // Verifica se o jogo está em estado de "jogando" (ativo)
        if (gameState == playState) {

            player.update(); // Atualiza o jogador: movimentação, colisões, animações etc.

            // Atualiza todos os NPCs que estão ativos no mapa
            for (int i = 0; i < npc.length; i++) {
                if (npc[i] != null) {
                    npc[i].update(); // Executa a lógica do NPC (movimento aleatório, animação, ações)
                }
            }
        }

        // Se o jogo estiver pausado, essa parte é executada
        if (gameState == pauseState) {
            // Lógica para quando o jogo estiver em pausa
            // Ex: animações leves, música ambiente, overlays de menu — se desejar
            // implementar
        }
    }

    // ==== DESENHA NA TELA ====
    // Método responsável por desenhar todo o frame do jogo
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Limpa o painel (fundo) antes de desenhar novos elementos

        // Converte para Graphics2D para aproveitar recursos avançados de renderização
        Graphics2D g2 = (Graphics2D) g;

        // DEBUG
        long drawStart = 0;
        if (keyH.checkDrawTime == true) {
            drawStart = System.nanoTime();
        }

        // Tela de título (menu inicial)
        if (gameState == titleState) {
            ui.draw(g2);
        }

        else {
            // Desenha o mapa: tiles visíveis de acordo com a câmera/jogador
            tileM.draw(g2);

            // Percorre todos os objetos do mundo e desenha os que estiverem instanciados
            for (int i = 0; i < obj.length; i++) {
                if (obj[i] != null) {
                    obj[i].draw(g2, this);
                }
            }

            // Desenha todos os NPCs (personagens não jogáveis) no mundo
            for (int i = 0; i < npc.length; i++) {
                if (npc[i] != null) {
                    npc[i].draw(g2);
                }
            }

            // Desenha o jogador no centro da tela (posição relativa à câmera)
            player.draw(g2);

            // UI
            ui.draw(g2);
        }

        // DEBUG
        if (keyH.checkDrawTime == true) {
            long drawEnd = System.nanoTime(); // Marca o fim do desenho
            long passed = drawEnd - drawStart; // Calcula o tempo gasto para desenhar
            g2.setColor(Color.white); // Define a cor do texto de debug
            g2.drawString("Draw Time: " + passed + " ns", 10, 400); // Exibe o tempo de desenho no canto da tela
            System.out.println("Draw Time: " + passed + " ns"); // Log no console
        }

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
