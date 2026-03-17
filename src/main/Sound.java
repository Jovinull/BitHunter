package main;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Gerencia reprodução de áudio (músicas e efeitos sonoros).
 * <p>
 * Todos os clips são pré-carregados na inicialização para evitar
 * I/O bloqueante durante a gameplay. A troca entre sons é instantânea.
 * </p>
 */
public class Sound {

  private static final Logger LOG = Logger.getLogger(Sound.class.getName());
  private static final int MAX_SOUNDS = 30;

  private final URL[] soundURL = new URL[MAX_SOUNDS];
  private final Clip[] clips = new Clip[MAX_SOUNDS];
  private final FloatControl[] volumeControls = new FloatControl[MAX_SOUNDS];

  private int currentIndex = -1;
  private int volumeScale = 3;

  public static final int MIN_VOLUME_SCALE = 0;
  public static final int MAX_VOLUME_SCALE = 5;

  /** Construtor: registra URLs e pré-carrega todos os clips. */
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

    preloadAll();
  }

  /**
   * Pré-carrega todos os clips na memória.
   * Chamado uma vez na inicialização — elimina I/O durante gameplay.
   */
  private void preloadAll() {
    for (int i = 0; i < MAX_SOUNDS; i++) {
      if (soundURL[i] == null) continue;

      try {
        AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
        Clip c = AudioSystem.getClip();
        c.open(ais);
        ais.close();

        clips[i] = c;
        volumeControls[i] = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
      } catch (Exception e) {
        LOG.log(Level.WARNING, "Falha ao pré-carregar som " + i, e);
      }
    }
  }

  /**
   * Seleciona o clip a ser reproduzido.
   * Apenas para o clip anterior e troca o ponteiro — sem I/O.
   */
  public void setFile(int i) {
    if (i == currentIndex) return;

    // Para o clip que estava tocando
    if (currentIndex >= 0 && clips[currentIndex] != null) {
      clips[currentIndex].stop();
    }

    currentIndex = i;
    checkVolume();
  }

  /** Inicia a reprodução do som selecionado. */
  public void play() {
    if (currentIndex < 0 || clips[currentIndex] == null) return;
    clips[currentIndex].setFramePosition(0);
    clips[currentIndex].start();
  }

  /** Reproduz o som em loop contínuo. */
  public void loop() {
    if (currentIndex < 0 || clips[currentIndex] == null) return;
    clips[currentIndex].loop(Clip.LOOP_CONTINUOUSLY);
  }

  /** Para a reprodução do som atual. */
  public void stop() {
    if (currentIndex >= 0 && clips[currentIndex] != null && clips[currentIndex].isRunning()) {
      clips[currentIndex].stop();
    }
  }

  /** Ajusta o volume do clip atualmente selecionado. */
  public void checkVolume() {
    if (currentIndex < 0 || volumeControls[currentIndex] == null) return;

    float volumeLocal = switch (volumeScale) {
      case 0 -> -80f;
      case 1 -> -20f;
      case 2 -> -12f;
      case 3 -> -5f;
      case 4 -> 1f;
      case 5 -> 6f;
      default -> -5f;
    };
    volumeControls[currentIndex].setValue(volumeLocal);
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

    // Aplica volume a todos os clips carregados
    for (int i = 0; i < MAX_SOUNDS; i++) {
      if (volumeControls[i] != null) {
        float v = switch (this.volumeScale) {
          case 0 -> -80f;
          case 1 -> -20f;
          case 2 -> -12f;
          case 3 -> -5f;
          case 4 -> 1f;
          case 5 -> 6f;
          default -> -5f;
        };
        volumeControls[i].setValue(v);
      }
    }
  }
}
