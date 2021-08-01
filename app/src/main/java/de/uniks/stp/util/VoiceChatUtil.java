package de.uniks.stp.util;

import de.uniks.stp.Constants;

import javax.sound.sampled.*;
import javax.sound.sampled.Mixer.Info;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class VoiceChatUtil {
    public static final AudioFormat AUDIO_FORMAT = new AudioFormat(
        Constants.AUDIOSTREAM_SAMPLE_RATE,
        Constants.AUDIOSTREAM_SAMPLE_SIZE_BITS,
        Constants.AUDIOSTREAM_CHANNEL,
        Constants.AUDIOSTREAM_SIGNED,
        Constants.AUDIOSTREAM_BIG_ENDIAN
    );

	public static List<Mixer> getMixers() {
		Info[] infos = AudioSystem.getMixerInfo();
		List<Mixer> mixers = new ArrayList<>(infos.length);
		for (Info info : infos) {
			Mixer mixer = AudioSystem.getMixer(info);
			mixers.add(mixer);
		}
		return mixers;
	}

	public static List<Line> getAvailableInputLines(Mixer mixer) {
		return getAvailableLines(mixer, mixer.getTargetLineInfo());
	}

	public static List<Line> getAvailableOutputLines(Mixer mixer) {
		return getAvailableLines(mixer, mixer.getSourceLineInfo());
	}

	private static List<Line> getAvailableLines(Mixer mixer, Line.Info[] lineInfos) {
		List<Line> lines = new ArrayList<>(lineInfos.length);
		for (Line.Info lineInfo : lineInfos) {
			Line line;
			line = getLineIfAvailable(mixer, lineInfo);
			if (line != null) lines.add(line);
		}
		return lines;
	}

	public static Line getLineIfAvailable(Mixer mixer, Line.Info lineInfo) {
		try {
			return mixer.getLine(lineInfo);
		} catch (LineUnavailableException ex) {
			return null;
		}
	}

    public static String getMixerHierarchyInfo(Mixer mixer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Mixer: ").append(toString(mixer)).append("\n");

        for (Line line : getAvailableOutputLines(mixer)) {
            sb.append("  OUT: ").append(toString(line)).append("\n");
            appendControls(sb, line);
        }

        for (Line line : getAvailableInputLines(mixer)) {
            sb.append("  IN: ").append(toString(line)).append("\n");
            appendControls(sb, line);
        }

        sb.append("\n");

        return sb.toString();
    }

    private static void appendControls(StringBuilder sb, Line line) {
        boolean opened = open(line);
        for (Control control : line.getControls()) {
            sb.append("    Control: ").append(toString(control)).append("\n");
            if (control instanceof CompoundControl) {
                CompoundControl compoundControl = (CompoundControl) control;
                for (Control subControl : compoundControl.getMemberControls()) {
                    sb.append("      Sub-Control: ").append(toString(subControl)).append("\n");
                }
            }
        }
        if (opened) line.close();
    }

    public static boolean open(Line line) {
		if (line.isOpen()) return false;
		try {
		    line.open();
		} catch (IllegalArgumentException | LineUnavailableException ex) {
			return false;
		}
		return true;
	}

	public static String toString(Control control) {
		if (control == null) return null;
		return control + " (" + control.getType().toString() + ")";
	}

	public static String toString(Line line) {
		if (line == null) return null;
		Line.Info info = line.getLineInfo();
		return info.toString() + " (" + line.getClass().getSimpleName() + ")";
	}

    public static String toString(Mixer mixer) {
        if (mixer == null) return null;
        StringBuilder sb = new StringBuilder();
        Info info = mixer.getMixerInfo();
        sb.append(info.getName());
        sb.append(" (").append(info.getDescription()).append(")");
        sb.append(mixer.isOpen() ? " [open]" : " [closed]");
        return sb.toString();
    }

    public static byte[] adjustVolume(int volume, byte[] audioBuf) {
        /* Do not change anything if volume is not withing acceptable range of 0 - 100.
         Notice that a volume of 100 would not change anything and just return the buffer as is */
        if (volume < 0 || volume >= 100) {
            return audioBuf;
        }
        final double vol = Math.pow(volume / 100d, 2);  //for better volume scaling
        final ByteBuffer wrap = ByteBuffer.wrap(audioBuf).order(ByteOrder.LITTLE_ENDIAN);
        final ByteBuffer dest = ByteBuffer.allocate(audioBuf.length).order(ByteOrder.LITTLE_ENDIAN);

        // Copy metadata
        for (int i = 0; i < Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE; i++) {
            dest.put(wrap.get());
        }

        // PCM
        while (wrap.hasRemaining()) {
            short temp = wrap.getShort();
            temp *= vol;

            byte b1 = (byte) (temp & 0xff);
            byte b2 = (byte) ((temp >> 8) & 0xff);

            dest.put(b1);
            dest.put(b2);
        }
        return dest.array();
    }
}
