package venturas.app.audio;

import java.io.*;
import javax.sound.sampled.*;

public class AudioUtil {

	public static AudioFormat getAudioFormat(String fileName) throws FileNotFoundException, IOException, UnsupportedAudioFileException {
		try (FileInputStream in = new FileInputStream(new File(fileName))) {
            BufferedInputStream bis = new BufferedInputStream(in);
            AudioInputStream ais = AudioSystem.getAudioInputStream(bis);
            AudioFormat format = ais.getFormat();
            ais.close();
			return format;
        }
	}

	public static RandomAccessFile getRandomAccessFile(String fileName) throws FileNotFoundException {
		File soundFile = new File(fileName);
		if (!soundFile.exists() || !soundFile.isFile()) {
            throw new IllegalArgumentException("not a file: " + soundFile);
		}
		return new RandomAccessFile(fileName, "r");
	}
}
