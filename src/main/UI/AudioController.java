package UI;

// usando a API Java Sound.
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioController {

    private static Clip backgroundMusicClip; // Referência para controlar o BGM

    // O arquivo deve ser .wav
    // Função de inicializar o bg
    public static void startBGM(String bgmFileName) {
        // Para o BGM atual, se houver
        if (backgroundMusicClip != null) {
            backgroundMusicClip.stop();
            backgroundMusicClip.close();
        }

        try {
            // **Caminho:** Usa File para acessar o sistema de arquivos diretamente.
            File soundFile = new File("resources/audio/" + bgmFileName);

            if (!soundFile.exists()) {
                System.err.println("Arquivo de BGM não encontrado: " + soundFile.getAbsolutePath());
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioStream);

            // Configura o Looping Contínuo
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusicClip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Erro ao reproduzir BGM com Java Sound: " + bgmFileName);
            e.printStackTrace();
        }
    }


    // Define o volume da música de fundo (BGM), sendo volumePercentual Um valor de 0.0 (mudo) a 1.0 (máximo).
    public static void setBGMVolume(double volumePercentual) {
        if (backgroundMusicClip == null) {
            System.err.println("Nenhuma música de fundo está tocando.");
            return;
        }

        try {
            // 1. Obtém o controle de Ganho (Master Gain)
            FloatControl gainControl = (FloatControl) backgroundMusicClip.getControl(FloatControl.Type.MASTER_GAIN);

            // 2. Garante que o valor esteja no intervalo [0.0, 1.0]
            double volume = Math.max(0.0001, volumePercentual); // Garante que nunca seja zero

            // 3. Converte o volume linear (0.0 a 1.0) para a escala logarítmica (dB)
            // A fórmula logarítmica é necessária para a API Java Sound: dB = 20 * log10(volume)
            float decibels = (float) (Math.log10(volume) * 20.0);

            // 4. Define o volume
            // É crucial limitar o valor para não exceder o máximo permitido pelo Clip
            decibels = Math.max(decibels, gainControl.getMinimum());

            gainControl.setValue(decibels);

        } catch (IllegalArgumentException e) {
            System.err.println("O controle de volume MASTER_GAIN não é suportado nesta linha de áudio.");
        }
    }

    // -----------------------------------------------------------------------

    /**
     * Reproduz um efeito sonoro (SFX) a partir do caminho do arquivo.
     * Cria um novo Clip para cada som e o fecha após a reprodução.
     * @param sfxFileName O nome do arquivo SFX (ex: "card_play.wav").
     */
    public static void playSFX(String sfxFileName) {
        try {
            File soundFile = new File("resources/audio/" + sfxFileName);

            if (!soundFile.exists()) {
                System.err.println("Arquivo de SFX não encontrado: " + soundFile.getAbsolutePath());
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Garante que o recurso seja liberado após a reprodução
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Erro ao reproduzir SFX com Java Sound: " + sfxFileName);
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------------
    // Métodos de Controle do BGM

    public static void pauseBGM() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
        }
    }

    public static void resumeBGM() {
        if (backgroundMusicClip != null && !backgroundMusicClip.isRunning()) {
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusicClip.start();
        }
    }
}