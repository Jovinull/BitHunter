package main;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

// Classe responsável por tocar efeitos sonoros e músicas do jogo
public class Sound {

    Clip clip; // Controla o áudio que está sendo reproduzido
    URL soundURL[] = new URL[30]; // Armazena os caminhos dos arquivos de som

    // Construtor: carrega os arquivos de áudio no array
    public Sound() {
        soundURL[0] = getClass().getResource("/resource/sounds/BlueBoyAdventure.wav");
        soundURL[1] = getClass().getResource("/resource/sounds/coin.wav");
        soundURL[2] = getClass().getResource("/resource/sounds/powerup.wav");
        soundURL[3] = getClass().getResource("/resource/sounds/unlock.wav");
        soundURL[4] = getClass().getResource("/resource/sounds/fanfare.wav");
        soundURL[5] = getClass().getResource("/resource/sounds/hitmonster.wav");
        soundURL[6] = getClass().getResource("/resource/sounds/receivedamage.wav");
        soundURL[7] = getClass().getResource("/resource/sounds/swingweapon.wav");
    }

    // Prepara o áudio correspondente ao índice informado
    public void setFile(int i) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]); // Lê o áudio
            clip = AudioSystem.getClip(); // Cria um reprodutor de som
            clip.open(ais); // Carrega o áudio no reprodutor
        } catch (Exception e) {
            e.printStackTrace(); // Mostra o erro se o som não puder ser carregado
        }
    }

    // Inicia a reprodução do som
    public void play() {
        clip.start();
    }

    // Reproduz o som em loop contínuo
    public void loop() {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    // Para a reprodução do som
    public void stop() {
        clip.stop();
    }
}
