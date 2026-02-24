package main;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

// Classe responsável por tocar efeitos sonoros e músicas do jogo
public class Sound {

  private Clip clip; // Controla o áudio que está sendo reproduzido
  private final URL[] soundURL = new URL[30]; // Armazena os caminhos dos arquivos de som
  private FloatControl fc; // Controla o volume do áudio
  private int currentIndex = -1;

  // Nível de volume (0 a 5)
  private int volumeScale = 3;

  public static final int MIN_VOLUME_SCALE = 0;
  public static final int MAX_VOLUME_SCALE = 5;

  // Construtor: carrega os arquivos de áudio no array
  public Sound() {
    soundURL[0] = getClass().getResource("/sounds/BlueBoyAdventure.wav");
    soundURL[1] = getClass().getResource("/sounds/coin.wav");
    soundURL[2] = getClass().getResource("/sounds/powerup.wav");
    soundURL[3] = getClass().getResource("/sounds/unlock.wav");
    soundURL[4] = getClass().getResource("/sounds/fanfare.wav");
    soundURL[5] = getClass().getResource("/sounds/hitmonster.wav");
    soundURL[6] = getClass().getResource("/sounds/receivedamage.wav");
    soundURL[7] = getClass().getResource("/sounds/swingweapon.wav");
    soundURL[8] = getClass().getResource("/sounds/levelup.wav");
    soundURL[9] = getClass().getResource("/sounds/cursor.wav");
    soundURL[10] = getClass().getResource("/sounds/burning.wav");
    soundURL[11] = getClass().getResource("/sounds/cuttree.wav");
    soundURL[12] = getClass().getResource("/sounds/gameover.wav");
    soundURL[13] = getClass().getResource("/sounds/stairs.wav");
    soundURL[14] = getClass().getResource("/sounds/sleep.wav");
    soundURL[15] = getClass().getResource("/sounds/blocked.wav");
    soundURL[16] = getClass().getResource("/sounds/parry.wav");
    soundURL[17] = getClass().getResource("/sounds/speak.wav");
    soundURL[18] = getClass().getResource("/sounds/Merchant.wav");
    soundURL[19] = getClass().getResource("/sounds/Dungeon.wav");
    soundURL[20] = getClass().getResource("/sounds/chipwall.wav");
    soundURL[21] = getClass().getResource("/sounds/dooropen.wav");
    soundURL[22] = getClass().getResource("/sounds/FinalBattle.wav");
  }

  // Prepara o áudio correspondente ao índice informado
  public void setFile(int i) {
    try {
      if (i == currentIndex && clip != null && clip.isOpen()) {
        // Reutiliza o clip já carregado para evitar I/O bloqueante no respawn.
        return;
      }

      closeCurrentClip();

      try (AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i])) { // Lê o áudio
      clip = AudioSystem.getClip(); // Cria um reprodutor de som
      clip.open(ais); // Carrega o áudio no reprodutor
      fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN); // Controla o volume
      checkVolume(); // Ajusta o volume conforme volumeScale
      currentIndex = i;
      }
    } catch (Exception e) {
      e.printStackTrace(); // Mostra o erro se o som não puder ser carregado
    }
  }

  // Inicia a reprodução do som
  public void play() {
    if (clip == null) {
      return;
    }
    clip.setFramePosition(0); // garante que começa do início
    clip.start();
  }

  // Reproduz o som em loop contínuo
  public void loop() {
    if (clip == null) {
      return;
    }
    clip.loop(Clip.LOOP_CONTINUOUSLY);
  }

  // Para a reprodução do som
  public void stop() {
    if (clip != null && clip.isRunning()) {
      clip.stop();
    }
  }

  private void closeCurrentClip() {
    if (clip == null) {
      return;
    }
    if (clip.isRunning()) {
      clip.stop();
    }
    clip.close();
    clip = null;
    fc = null;
    currentIndex = -1;
  }

  public void checkVolume() {
    if (fc == null) {
      return;
    }

    float volumeLocal;
    switch (volumeScale) {
      case 0 -> volumeLocal = -80f; // Silencioso
      case 1 -> volumeLocal = -20f; // Muito baixo
      case 2 -> volumeLocal = -12f; // Baixo
      case 3 -> volumeLocal = -5f; // Médio
      case 4 -> volumeLocal = 1f; // Alto
      case 5 -> volumeLocal = 6f; // Muito alto
      default -> volumeLocal = -5f; // Valor padrão
    }
    fc.setValue(volumeLocal); // Aplica o volume calculado
  }

  public int getVolumeScale() {
    return volumeScale;
  }

  public void setVolumeScale(int volumeScale) {
    if (volumeScale < MIN_VOLUME_SCALE) {
      volumeScale = MIN_VOLUME_SCALE;
    } else if (volumeScale > MAX_VOLUME_SCALE) {
      volumeScale = MAX_VOLUME_SCALE;
    }
    this.volumeScale = volumeScale;
    checkVolume();
  }
}
