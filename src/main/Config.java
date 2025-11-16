package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {

  private static final Logger LOGGER = Logger.getLogger(Config.class.getName());
  private static final String CONFIG_FILE_NAME = "config.txt";
  private static final String VALUE_ON = "On";
  private static final String VALUE_OFF = "Off";

  private final GamePanel gp;

  public Config(GamePanel gp) {
    this.gp = gp;
  }

  public void saveConfig() {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(CONFIG_FILE_NAME))) {

      // Full screen
      bw.write(gp.isFullScreenOn() ? VALUE_ON : VALUE_OFF);
      bw.newLine();

      // Music volume
      bw.write(String.valueOf(gp.music.volumeScale));
      bw.newLine();

      // SE volume
      bw.write(String.valueOf(gp.se.volumeScale));
      bw.newLine();

      // Language code (ex: pt_BR, en_US)
      bw.write(gp.getLanguageManager().getCurrentLanguageCode());
      bw.newLine();

    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, "Falha ao salvar configurações em " + CONFIG_FILE_NAME, ex);
    }
  }

  public void loadConfig() {
    try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE_NAME))) {

      String s = br.readLine();

      // Full screen
      if (VALUE_ON.equals(s)) {
        gp.setFullScreenOn(true);
      } else if (VALUE_OFF.equals(s)) {
        gp.setFullScreenOn(false);
      }

      // Music volume
      s = br.readLine();
      gp.music.volumeScale = Integer.parseInt(s);

      // SE volume
      s = br.readLine();
      gp.se.volumeScale = Integer.parseInt(s);

      // Language (linha opcional para configs antigos)
      s = br.readLine();
      if (s != null && !s.isBlank()) {
        gp.getLanguageManager().setCurrentLanguage(s.trim());
      }

    } catch (IOException | NumberFormatException ex) {
      LOGGER.log(Level.SEVERE, "Falha ao carregar configurações de " + CONFIG_FILE_NAME, ex);
      // Em caso de erro, garante pelo menos o idioma padrão
      gp.getLanguageManager().setCurrentLanguage(LanguageManager.DEFAULT_LANGUAGE);
    }
  }
}
